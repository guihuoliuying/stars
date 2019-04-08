package com.stars.services.id;

import com.stars.core.db.DBUtil;
import com.stars.util.LogUtil;
import com.stars.util.uid.IdAllocator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class FamilyRedPacketIdAllocator extends IdAllocator {

    private long serverId;
    private AtomicLong sequenceId;

    public FamilyRedPacketIdAllocator(long serverId) {
        if (serverId >= 65536) {
            throw new IllegalArgumentException();
        }
        this.serverId = serverId << 38; // 低22位为自增序列
    }

    @Override
    protected void init() {
        try {
            long maxId = DBUtil.queryCount(DBUtil.DB_USER, "SELECT MAX(`redpacketid` & 274877906943) FROM `familyredpacketrecord`");
            sequenceId = new AtomicLong(maxId != -1 ? maxId : 0);
            LogUtil.info("家族红包ID：服务ID={}, 序列={}", (serverId >> 38), maxId);
        } catch (Throwable cause) {
            error(cause);
        } finally {
            finishInit();
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
}
