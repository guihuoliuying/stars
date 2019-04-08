package com.stars.services.id;

import com.stars.core.db.DBUtil;
import com.stars.util.LogUtil;
import com.stars.util.uid.IdAllocator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class FamilyIdAllocator extends IdAllocator {

    private long serverId;
    private AtomicLong sequenceId;

    public FamilyIdAllocator(long serverId) {
        if (serverId >= 65536) {
            throw new IllegalArgumentException();
        }
        this.serverId = serverId << 22; // 低22位为自增序列
    }

    @Override
    protected void init() {
        try {
            long maxId = DBUtil.queryCount(DBUtil.DB_USER, "SELECT MAX(`familyid` & 4194303) FROM `family`");
            sequenceId = new AtomicLong(maxId != -1 ? maxId : 0);
            LogUtil.info("家族ID：服务ID={}, 序列={}", (serverId >> 22), maxId);
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
        if (tempId >= 4_194_304L) {
            throw new IllegalStateException();
        }
        return serverId | tempId;
    }

    @Override
    public void unsafeSet(long id) {
        throw new UnsupportedOperationException();
    }
}
