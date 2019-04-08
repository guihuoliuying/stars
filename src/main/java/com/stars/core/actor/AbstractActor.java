package com.stars.core.actor;

import com.stars.util.log.CoreLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现了Actor接口，提供基础功能
 * <p/>
 * Created by zhaowenshuo on 2014/12/29.
 */
public abstract class AbstractActor implements Actor {

    private static final int IDLE = 0; // 空闲状态：Actor的邮箱中没有消息
    private static final int RUNNING = 1; // 运行状态：Actor的邮箱中有消息
    private static final int STOP = 2; // 停止状态

    private String name;
    private ActorSystem system; // 承载actor的系统
    private BlockingQueue<Mail> mailbox; // 消息队列（邮箱）
    private Runnable task = new ActorTask(); // 任务：循环处理消息队列中的消息
    private DeadMessageHandler deadMessageHandler; // 死消息的处理者
    private AtomicInteger state = new AtomicInteger(IDLE); // 状态

    private boolean isDone = false;
    private final Object stopLock = new Object();
    private volatile Thread currThread = null;

    public AbstractActor() {
        this(512);
    }

    public AbstractActor(ActorSystem system) {
        this();
        system.addActor(this);
    }

    public AbstractActor(int queueSize) {
        this.mailbox = new ArrayBlockingQueue<>(queueSize);
    }

    public AbstractActor(BlockingQueue<Mail> queue) {
        this.mailbox = queue;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setSystem(ActorSystem system) {
        if (this.system != null) {
            throw new IllegalStateException("Actor had already assigned an ActorSystem: "
                    + system.toString());
        }
        if (system == null) {
            throw new NullPointerException("ActorSystem can not be null");
        }
        this.system = system;
    }

    @Override
    public final void tell(Object message, Actor sender) {
        if (state.get() == STOP) {
            throw new IllegalStateException("Actor already stop");
        }

        sender = (sender == null) ? Actor.noSender : sender;
        Mail mail = new Mail(message, sender);
        if (mailbox.offer(mail)) {
            CoreLogger.trace("actor[{}] tell a task", getName());
            int currState = state.get();
            if (currState == IDLE && state.compareAndSet(IDLE, RUNNING)) {
                system.submit(task);
                CoreLogger.trace("actor[{}] start to run tasks", getName());
            } else if (currState == STOP) {
                synchronized (stopLock) {
                    if (isDone && mailbox.contains(mail)) {
                        throw new IllegalStateException("Actor already stop");
                    }
                }
            }
        } else {
            throw new IllegalStateException("Queue is full");
        }

    }

    @Override
    public final void stop() {
        if (currThread != Thread.currentThread()) {
            String msg = "actorstop: v5_1_15: currThread=" + currThread
                    + ", Thread.currentThread()=" + Thread.currentThread()
                    + ", state=" + state.get()
                    + ", actor=" + this;
            CoreLogger.error(msg, new Exception());
        }
        state.set(STOP);
        synchronized (stopLock) {
            isDone = true;
            if (deadMessageHandler != null) {
                Mail mail = null;
                while ((mail = mailbox.poll()) != null) {
                    try {
                        deadMessageHandler.handle(mail.message, mail.sender);
                    } catch (Throwable cause) {
                        CoreLogger.error("actor: error while processing a message", cause);
                    }
                }
            }
            system.removeActor(AbstractActor.this);
        }
        CoreLogger.trace("actor[{}] stop to run tasks", getName());
    }

    @Override
    public final void setDeadMessageHandler(DeadMessageHandler handler) {
        if (this.system == null && this.deadMessageHandler == null) {
            this.deadMessageHandler = handler;
        }
    }

    @Override
    public abstract void onReceived(Object message, Actor sender);


    protected void postStop() {
        // default implementation: do nothing
    }

    protected int mailboxSize() {
        return mailbox.size();
    }

    private class ActorTask implements Runnable {
        @Override
        public void run() {
            try {
                currThread = Thread.currentThread();
                Mail mail = null;
                while ((mail = mailbox.poll()) != null) {
                    try {
                        onReceived(mail.message, mail.sender);
                        if (isDone) {
                            break;
                        }
                    } catch (Throwable cause) {
                        CoreLogger.error("actor: error while processing a message", cause);
                    }
                }
                currThread = null;
                if (state.compareAndSet(RUNNING, IDLE)) {
                    if (mailbox.peek() != null && state.compareAndSet(IDLE, RUNNING)) {
                        try {
                            system.submit(this); // 应该对Actor的数量有所限制... 使其绝少机会
                            CoreLogger.trace("actor[{}] continue to run tasks", getName());
                        } catch (RejectedExecutionException e) {
                            state.compareAndSet(RUNNING, IDLE); // 还原状态，部分任务会卡住
                            CoreLogger.error("actor[" + getName() + "]: error while submiting task", e);
                        }
                    } else {
                        CoreLogger.trace("actor[{}] exit to run tasks(maybe idle)", getName()); // 此刻不一定为IDLE，可能为RUNNING和STOP（可能是抢不到状态）
                    }

                } else {
                    CoreLogger.trace("actor[{}] exit to run tasks(stop)", getName());
                }
            } catch (Throwable cause) {
                CoreLogger.error("actor: error while processing tasks", cause);
            }
        }
    }

    public class Mail {
        private Object message;
        private Actor sender;

        public Mail(Object message, Actor sender) {
            this.message = message;
            this.sender = sender;
        }
    }

}
