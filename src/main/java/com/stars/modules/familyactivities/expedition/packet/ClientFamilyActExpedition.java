package com.stars.modules.familyactivities.expedition.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.expedition.FamilyActExpeditionPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/11.
 */
public class ClientFamilyActExpedition extends PlayerPacket {

    public static final byte SUBTYPE_VIEW = 0x00; // 打开界面响应
    public static final byte SUBTYPE_UPDATED_ALL = 0x01; // 界面信息更新
    public static final byte SUBTYPE_UPDATED_STEP = 0x02; // 界面信息更新
    public static final byte SUBTYPE_FIGHT = 0x10;
    public static final byte SUBTYPE_AWARD = 0x20;
    public static final byte SUBTYPE_BUFF = 0x30; // 玩家buff状态更新

    public static final byte STATE_NOT_STARTED = 0x00;
    public static final byte STATE_STARTED = 0x01;
    public static final byte STATE_END = 0x02;

    private byte subtype;
    /* view */
//    private byte state;
    private int maxPassedId; // 已通关的最大难度
    private int availCount; // 可挑战次数
    private int curId; // 当前进行中的难度
    private int curStep; // 当前进行中的小关
    private int displayId1;
    private int displayId2;
    private int displayId3;
    private Map<String, Integer> curPassedIdMap;
    /* buff */
    private Map<Integer, Byte> buffMap;

    public ClientFamilyActExpedition() {
    }

    public ClientFamilyActExpedition(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public short getType() {
        return FamilyActExpeditionPacketSet.C_EXPEDITION;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_VIEW:
                writeView(buff);
                break;
            case SUBTYPE_UPDATED_ALL:
                writeView(buff);
                break;
            case SUBTYPE_AWARD:
                break;
            case SUBTYPE_BUFF:
                writeBuff(buff);
                break;
        }
    }

    private void writeView(NewByteBuffer buff) {
        buff.writeInt(maxPassedId); // 最大通关难度
        buff.writeInt(availCount); // 可挑战次数
        buff.writeInt(curId); // 已选择难度（如果availCount > 0，为-1）
        buff.writeInt(curStep); // 进行中的小关数
        buff.writeInt(displayId1); // 难度1
        buff.writeInt(displayId2); // 难度2
        buff.writeInt(displayId3); // 难度3
//        buff.writeByte((byte) curPassedIdMap.size()); // 今天已通关难度列表的大小
//        for (Integer curPassedId : curPassedIdMap.values()) {
//            buff.writeInt(curPassedId);
//        }
    }

    private void writeBuff(NewByteBuffer buff) {
        buff.writeByte((byte) buffMap.size());
        for (Map.Entry<Integer, Byte> entry : buffMap.entrySet()) {
            buff.writeInt(entry.getKey()); // buff id
            buff.writeByte(entry.getValue()); // 是否已经使用（0 - 没使用，1 - 使用
        }
    }

    private void writeUpdated(NewByteBuffer buff) {

    }


    public void setMaxPassedId(int maxPassedId) {
        this.maxPassedId = maxPassedId;
    }

    public void setAvailCount(int availCount) {
        this.availCount = availCount;
    }

    public void setCurId(int curId) {
        this.curId = curId;
    }

    public void setCurStep(int curStep) {
        this.curStep = curStep;
    }

    public void setDisplayId1(int displayId1) {
        this.displayId1 = displayId1;
    }

    public void setDisplayId2(int displayId2) {
        this.displayId2 = displayId2;
    }

    public void setDisplayId3(int displayId3) {
        this.displayId3 = displayId3;
    }

    public void setCurPassedIdMap(Map<String, Integer> curPassedIdMap) {
        this.curPassedIdMap = curPassedIdMap;
    }

    public void setBuffMap(Map<Integer, Byte> buffMap) {
        this.buffMap = buffMap;
    }
}
