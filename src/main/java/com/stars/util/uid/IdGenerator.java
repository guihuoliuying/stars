package com.stars.util.uid;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by zws on 2015/5/8.
 */
public class IdGenerator {

    static final int START = 0; // 开始状态
    static final int END = 1; // 结束状态

    private ConcurrentMap<String, com.stars.util.uid.IdAllocator> allocators = new ConcurrentHashMap<>();
    private Queue<String> errorMessageQueue = new ConcurrentLinkedQueue<>();
    private volatile int state = START;

    public void register(String name, com.stars.util.uid.IdAllocator allocator) {
        if (state == START) {
            allocator.setName(name);
            allocators.put(name, allocator);
        } else {
            throw new IllegalStateException("");
        }
    }

    public com.stars.util.uid.IdAllocator get(String name) {
        return allocators.get(name);
    }

    public void init() throws InterruptedException {
        state = END;
        CountDownLatch latch = new CountDownLatch(allocators.size());
        for (com.stars.util.uid.IdAllocator allocator : allocators.values()) {
            allocator.setErrorMessageQueue(errorMessageQueue);
            allocator.setCountDownLatch(latch);
            new Thread(new IdAllocatorInitTask(allocator)).start(); // 并行初始化
        }
        // 等待初始化完成（如果超时，就抛出异常）
        if (!latch.await(30000, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("id generator initialization timeout");
        }
        // 检查初始化时是否发生错误
        if (errorMessageQueue.size() > 0) {
            throw new RuntimeException("error occur while id generator initialization: " +
                    errorMessageQueue.toString());
        }
    }

    public long newId(String key) {
        return allocators.get(key).newId();
    }

    private class IdAllocatorInitTask implements Runnable {

        private com.stars.util.uid.IdAllocator allocator;

        public IdAllocatorInitTask(IdAllocator allocator) {
            this.allocator = allocator;
        }

        @Override
        public void run() {
            try {
                // todo: add some log
                allocator.init();
            } catch (Throwable t) {
                // todo:
            }
        }
    }

}
