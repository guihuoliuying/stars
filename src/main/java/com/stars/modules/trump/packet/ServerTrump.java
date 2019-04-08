package com.stars.modules.trump.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.trump.TrumpModule;
import com.stars.modules.trump.TrumpPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhouyaohui on 2016/9/20.
 */
public class ServerTrump extends PlayerPacket {
    /** 常量 */
    public final static byte IDLE = 0;
    public final static byte OPEN_MAIN = 1; // 请求基本数据
    public final static byte SINGLE = 2;    // 请求某一级的法宝数据
    public final static byte UPGRADE = 3;   // 请求升级
    public final static byte PUTON = 4;     // 穿戴
    public final static byte TAKEOFF = 5;   // 卸下
//    public final static byte CLICK = 6;//点击了某个法宝

    private byte opType;

    private int trumpId;
    private short level;

    @Override
    public short getType() {
        return TrumpPacketSet.SERVER_TRUMP;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        opType = buff.readByte();
        if (opType == SINGLE) {
            trumpId = buff.readInt();
            level = buff.readShort();
        }
        if (opType == UPGRADE) {
            trumpId = buff.readInt();
        }
        if (opType == PUTON) {
            trumpId = buff.readInt();
        }
        if (opType == TAKEOFF) {
            trumpId = buff.readInt();
        }
        /*if(opType == CLICK){
            trumpId = buff.readInt();
        }*/
    }

    @Override
    public void execPacket(Player player) {
        TrumpModule trumpModule = (TrumpModule) module(MConst.Trump);
        if (opType == OPEN_MAIN) {
            trumpModule.sendTrumpUserData();
            trumpModule.sendTrumpLevelVo();
        }

        if (opType == SINGLE) {
            trumpModule.sendTrumpLevelVoSingle(trumpId, level);
        }

        if (opType == UPGRADE) {
            trumpModule.levelUp(trumpId);
        }

        if (opType == PUTON) {
            trumpModule.putOn(trumpId);
        }

        if (opType == TAKEOFF) {
            trumpModule.takeOff(trumpId);
        }
//        if (opType == CLICK){
//            trumpModule.oldTrump(trumpId);
//        }
    }
}
