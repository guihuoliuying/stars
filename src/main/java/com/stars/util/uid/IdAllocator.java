package com.stars.util.uid;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zws on 2015/5/8.
 */
public abstract class IdAllocator {

    private String name;
    private Queue<String> errorMessageQueue;
    private CountDownLatch latch;

    final void setName(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    final void setErrorMessageQueue(Queue<String> queue) {
        this.errorMessageQueue = queue;
    }

    final void setCountDownLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    protected final void finishInit() {
        latch.countDown();
    }

    protected final void error(String msg) {
        errorMessageQueue.add("error occur while initializing id allocator " + name + ": " + msg);
    }

    protected final void error(Throwable cause) {
        CharArrayWriter caw = new CharArrayWriter(512);
        cause.printStackTrace(new PrintWriter(caw));
        error(caw.toString());
    }

    protected abstract void init();

    /**
     * 返回新生成的唯一id
     * 注意：需要保证线程安全
     * @return
     */
    public abstract long newId();

    public abstract void unsafeSet(long id);

}
