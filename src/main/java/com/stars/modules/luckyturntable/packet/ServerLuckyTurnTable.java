package com.stars.modules.luckyturntable.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.luckyturntable.LuckyTurnTableModule;
import com.stars.modules.luckyturntable.LuckyTurnTablePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class ServerLuckyTurnTable extends PlayerPacket {
    public static final byte view = 0x00;
    public static final byte turn = 0x01;
    public static final byte reCall = 0x02;
    private byte subType;
    private int id;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        if (subType == reCall) {
            id = buff.readInt();
        }
    }

    @Override
    public void execPacket(Player player) {
        LuckyTurnTableModule tableModule = module(MConst.LuckyTurnTable);
        switch (subType) {
            case view:
                tableModule.viewUi();//打开界面
                break;
            case turn:
                tableModule.turnTable();//抽奖
                break;
            case reCall:
                tableModule.announceAndAddLuckyList(id);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return LuckyTurnTablePacketSet.S_LUCKYTURNTABLE;
    }
}
