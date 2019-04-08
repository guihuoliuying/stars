package com.stars.modules.oldplayerback.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.oldplayerback.OldPalyerBackPacketSet;
import com.stars.modules.oldplayerback.OldPlayerBackModule;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/7/14.
 */
public class ServerOldPalyerBackPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_TAKE_REWARD = 1;//领奖
    public static final short REQ_REWARD_SHOW = 2;//奖励展示

    @Override
    public short getType() {
        return OldPalyerBackPacketSet.S_OLDPLAYERBACK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
    }

    @Override
    public void execPacket(Player player) {
        OldPlayerBackModule oldPlayerBackModule = module(MConst.OldPlayerBack);
        switch (subType) {
            case REQ_TAKE_REWARD: {
                oldPlayerBackModule.takeReward();
            }
            break;
            case REQ_REWARD_SHOW:{
                oldPlayerBackModule.reqRewardShow();
            }break;
        }
    }

}
