package com.stars.services.family.activities.invade.cache;

import com.stars.modules.familyactivities.invade.FamilyInvadeManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.activities.invade.FamilyActInvadeConstant;

/**
 * Created by liuyuheng on 2016/10/19.
 */
public class MonsterNpcCache {
    private int uniqueId;
    private int npcId;
    private byte type;// 1=小怪;2=精英怪
    private int[] position;
    private int rotation;
    private byte status;// 状态,0=未触发;1=挑战中
    private String positionStr;// 坐标

    public MonsterNpcCache(int uniqueId, int npcId, byte type, int[] position, int rotation) {
        this.uniqueId = uniqueId;
        this.npcId = npcId;
        this.type = type;
        this.position = position;
        this.positionStr = FamilyInvadeManager.positionToStr(position);
        this.rotation = rotation;
        status = FamilyActInvadeConstant.NPC_AVAILABLE;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(uniqueId);
        buff.writeInt(npcId);
        buff.writeByte(type);
        buff.writeString(positionStr);
        buff.writeInt(rotation);
        buff.writeByte(status);
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public byte getType() {
        return type;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
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
