package com.stars.services.family.activities.invade.cache;

import com.stars.modules.familyactivities.invade.FamilyInvadeManager;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/10/19.
 */
public class AwardBoxCache {
    private String awardBoxUId;// 唯一Id
    private int npcId;// 使用npcId
    private int[] position;// 坐标
    private long createTimestamp;// 刷出时间
    private String positionStr;// 坐标

    public AwardBoxCache(String awardBoxUId, int npcId, int[] position, long timestamp) {
        this.awardBoxUId = awardBoxUId;
        this.npcId = npcId;
        this.position = position;
        this.positionStr = FamilyInvadeManager.positionToStr(position);
        this.createTimestamp = timestamp;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(awardBoxUId);
        buff.writeInt(npcId);
        buff.writeString(positionStr);
        buff.writeInt((int) Math.floor((FamilyInvadeManager.awardBoxShow -
                (System.currentTimeMillis() - createTimestamp)) / 1000.0));// 剩余时间(秒)
    }

    public String getAwardBoxUId() {
        return awardBoxUId;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getPosX() {
        return position[0];
    }

    public int getPosZ() {
        return position[2];
    }
}
