package com.stars.services.callboss.cache;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/9/5.
 */
public class CallBossCache implements Cloneable {
    private int bossId;// bossid
    private long callRoleId;// 召唤者角色Id
    private String roleName;// 召唤者名称
    private byte selectRewardId;// 召唤者选择奖励组id
    private volatile byte status;// 召唤boss状态
    private long callTime;// 召唤时间戳
    private int receiveDamage;// 已收到伤害

    public CallBossCache(int bossId, byte status) {
        this.bossId = bossId;
        this.status = status;
        this.callRoleId = 0;
        this.roleName = "";
        this.selectRewardId = 0;
    }

    public CallBossCache copy() {
        try {
            return (CallBossCache) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("CallBossCache克隆失败", e);
        }
        return null;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(bossId);
        buff.writeString(roleName);
        buff.writeString(String.valueOf(callRoleId));
        buff.writeByte(selectRewardId);
        buff.writeByte(status);
        buff.writeInt((int) (Math.floor(System.currentTimeMillis() - callTime) / 1000.0));// 召唤到现在已消耗时间
    }

    public int getBossId() {
        return bossId;
    }

    public void setBossId(int bossId) {
        this.bossId = bossId;
    }

    public long getCallRoleId() {
        return callRoleId;
    }

    public void setCallRoleId(long callRoleId) {
        this.callRoleId = callRoleId;
    }

    public byte getSelectRewardId() {
        return selectRewardId;
    }

    public void setSelectRewardId(byte selectRewardId) {
        this.selectRewardId = selectRewardId;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }

    public int getReceiveDamage() {
        return receiveDamage;
    }

    public void setReceiveDamage(int receiveDamage) {
        this.receiveDamage = receiveDamage;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
