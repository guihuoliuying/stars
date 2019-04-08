package com.stars.modules.newredbag.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.newredbag.NewRedbagModule;
import com.stars.modules.newredbag.NewRedbagPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class ServerNewRedbag extends PlayerPacket {

    private final static byte SEND = 1; // 发红包
    private final static byte VIEW_MAIN = 2;    // 红包主界面
    private final static byte GET = 3;  // 抢红包
    private final static byte RECORD = 4;   // 抢红包记录
    private final static byte RECORD_DETAIL = 5;    // 红包记录详细信息

    private byte reqType;
    private int redbagId;   // 红包id
    private int padding;    // 玩家增加的价值
    private int count;      // 红包个数
    private String redbagKey;   // 红包的唯一key
    private int index;

    @Override
    public void execPacket(Player player) {
        NewRedbagModule module = module(MConst.NewRedbag);
        switch (reqType) {
            case SEND:
                module.sendRedbag(redbagId, padding, count);
                break;
            case VIEW_MAIN:
                module.viewMain();
                break;
            case GET:
                module.get(redbagKey);
                break;
            case RECORD:
                module.redbagRecord(index);
                break;
            case RECORD_DETAIL:
                module.recordDetail(redbagKey);
                break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        switch (reqType) {
            case SEND:
                redbagId = buff.readInt();
                count = buff.readInt();
                padding = buff.readInt();
                break;
            case GET:
                redbagKey = buff.readString();
                break;
            case RECORD:
                index = buff.readInt();
                break;
            case RECORD_DETAIL:
                redbagKey = buff.readString();
                break;
        }
    }

    @Override
    public short getType() {
        return NewRedbagPacketSet.S_NEW_REDBAG;
    }
}
