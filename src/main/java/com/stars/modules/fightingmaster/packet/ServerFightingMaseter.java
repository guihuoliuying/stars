package com.stars.modules.fightingmaster.packet;

import com.stars.modules.fightingmaster.FightingMasterPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * Created by zhouyaohui on 2016/11/7.
 */
public class ServerFightingMaseter extends Packet {

    public final static byte MATCH = 1;
    public final static byte ENTERFIGHT = 2;
    public final static byte BACKCITY = 3;
    public final static byte RANK = 4;
    public final static byte FIVEAWARD = 5;
    public final static byte RETRY_MATCH = 6;   // 再次匹配
    public final static byte FORCE_MATCH = 7;   // 强行匹配机器人
    public final static byte CANCEL_MATCH = 8;  // 取消匹配

    private byte reqType;

    private String fightId;

    private int rankId;

    public int getRankId() {
        return rankId;
    }

    public String getFightId() {
        return fightId;
    }

    public byte getReqType() {
        return reqType;
    }

    public void setReqType(byte reqType) {
        this.reqType = reqType;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        if (reqType == ENTERFIGHT) {
            fightId = buff.readString();
        }
        if (reqType == RANK) {
            rankId = buff.readInt();
        }
    }

    @Override
    public void execPacket() {
    }

    @Override
    public short getType() {
        return FightingMasterPacketSet.S_FIGHTINGMASTER;
    }
}
