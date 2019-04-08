package com.stars.modules.giftcome520.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.giftcome520.GiftComeModule;
import com.stars.modules.giftcome520.GiftComePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/4/15.
 */
public class ServerGiftComePacket extends PlayerPacket {
    private byte subType;
    public final static byte TAKE_REWARD = 1;
    public final static byte REQ_UI_RESOURCE = 2;//请求界面资源

    @Override
    public void execPacket(Player player) {
        switch (subType) {
            case TAKE_REWARD: {
                GiftComeModule giftComeModule = module(MConst.GiftCome520);
                giftComeModule.takeReward();
            }
            break;
            case REQ_UI_RESOURCE: {
                GiftComeModule giftComeModule = module(MConst.GiftCome520);
                giftComeModule.sendUIResource();
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
    }

    @Override
    public short getType() {
        return GiftComePacketSet.S_GiftCome;
    }
}
