package com.stars.modules.fashioncard.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.fashioncard.FashionCardModule;
import com.stars.modules.fashioncard.FashionCardPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public class ServerFashionCard extends PlayerPacket {

    private static final byte REQ_VIEW = 0x00;//打开界面请求数据
    private static final byte REQ_PUTON = 0x01;//穿上
    private static final byte REQ_TAKEOFF = 0x02;//脱下

    private byte subType;

    private int fahsionCardId;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        switch (subType) {
            case REQ_VIEW:

                break;
            case REQ_PUTON:
                fahsionCardId = buff.readInt();
                break;
            case REQ_TAKEOFF:
                fahsionCardId = buff.readInt();
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        FashionCardModule cardModule = module(MConst.FashionCard);
        switch (subType) {
            case REQ_VIEW:
                cardModule.view();
                break;
            case REQ_PUTON:
                cardModule.putOnFashionCard(fahsionCardId);
                break;
            case REQ_TAKEOFF:
                cardModule.takeOffFashionCard(fahsionCardId, true);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return FashionCardPacketSet.S_FASHION_CARD;
    }
}
