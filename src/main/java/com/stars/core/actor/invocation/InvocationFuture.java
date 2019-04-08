package com.stars.core.actor.invocation;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaowenshuo on 2016/6/14.
 */
public class InvocationFuture implements Future {

    public static final int INVOKING = 0;
    public static final int DONE = 1;

    private final CountDownLatch latch;
    private final boolean isDispatchAll;
    private final AtomicInteger state = new AtomicInteger(INVOKING);
    private Object value;
    private Throwable throwable;

    public InvocationFuture() {
        latch = new CountDownLatch(1);
        isDispatchAll = false;
    }

    public InvocationFuture(int count) {
        latch = new CountDownLatch(count);
        isDispatchAll = true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (latch.await(timeout, unit)) {
            if (throwable != null) {
                return throwable;
            }
            return value;
        }
        return new TimeoutException();
    }

    public void set(Object value) {
        try {
            if (!isDispatchAll) {
                if (state.compareAndSet(INVOKING, DONE)) {
                    this.value = value;
                }

        /* 对集合类操作进行合并 */
            } else {
                if (value != null) {
                    if (value instanceof Collection) {
                        synchronized (this) {
                            if (this.value == null) {
                                Class clazz = value.getClass();
                                this.value = clazz.newInstance();
                                state.compareAndSet(INVOKING, DONE);
                            }
                            ((Collection) this.value).addAll((Collection) value);
                        }
                    } else if (value instanceof Map) {
                        synchronized (this) {
                            if (this.value == null) {
                                Class clazz = value.getClass();
                                this.value = clazz.newInstance();
                                state.compareAndSet(INVOKING, DONE);
                            }
                            ((Map) this.value).putAll((Map) value);
                        }
                    } else {
                        if (state.compareAndSet(INVOKING, DONE)) {
                            this.value = value;
                        }
                    }
                } else {
                    if (state.compareAndSet(INVOKING, DONE)) {
                        this.value = value;
                    }
                }
            }
        } catch (Exception e) {

        }
        latch.countDown();
    }

    public void setThrowable(Throwable throwable) {
        if (state.compareAndSet(INVOKING, DONE)) {
            this.throwable = throwable;
//            throwable.printStackTrace();
        }
        latch.countDown();
    }
}
