package com.stars.services.id;

import com.stars.db.DBUtil;
import com.stars.util.LogUtil;
import com.stars.util.uid.IdAllocator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务id用16位，（0 ~ 65,535）
 * Created by zhaowenshuo on 2016/7/14.
 */
public class ToolIdAllocator extends IdAllocator {

    private long serverId;
    private AtomicLong sequenceId;

    public ToolIdAllocator(long serverId) {
        if (serverId >= 65536) {
            throw new IllegalArgumentException();
        }
        this.serverId = serverId << 38; // 低38位为自增序列
    }

    @Override
    protected void init() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        long[] maxIdArray = new long[10];
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            executorService.submit(new ToolIdLoader(i, maxIdArray, latch));
        }
        try {
            if (latch.await(30, TimeUnit.SECONDS)) {
                long maxId = Long.MIN_VALUE;
                for (int i = 0; i < 10; i++) {
                    maxId = maxId < maxIdArray[i] ? maxIdArray[i] : maxId;
                }
                sequenceId = new AtomicLong(maxId);
            } else {
//                throw new TimeoutException();
                sequenceId = new AtomicLong(0);
            }
            LogUtil.info("道具ID：服务ID={}, 序列={}", (serverId >> 38), sequenceId.get());
        } catch (InterruptedException e) {
            error(e);
        } finally {
            finishInit();
            executorService.shutdownNow();
        }
    }

    @Override
    public long newId() {
        long tempId = sequenceId.incrementAndGet();
        // 检查生成的ID是否合法
        if (tempId >= 274_877_906_944L) {
            throw new IllegalStateException();
        }
        return serverId | tempId;
    }

    @Override
    public void unsafeSet(long id) {
        throw new UnsupportedOperationException();
    }

    private class ToolIdLoader implements Runnable {
        private int index;
        private long[] maxIdArray;
        private CountDownLatch latch;

        public ToolIdLoader(int index, long[] maxIdArray, CountDownLatch latch) {
            this.index = index;
            this.maxIdArray = maxIdArray;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                long maxId = DBUtil.queryCount(DBUtil.DB_USER, "SELECT MAX(`toolid` & 274877906943) FROM `roletool" + index + "`");
                maxIdArray[index] = maxId != -1 ? maxId : 0;
            } catch (Exception e) {
                error(e);
            } finally {
                latch.countDown();
            }
        }
    }
}
