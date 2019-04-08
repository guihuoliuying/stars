package com.stars.modules.dragonboat.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.dragonboat.DragonBoatModule;
import com.stars.modules.dragonboat.DragonBoatPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class ServerDragonBoatPacket extends PlayerPacket {
    /**
     * 请求现在的页面状态
     */
    private static final byte REQ_ACTIVITY_STATUS_DATA = 0;
    /**
     * 请求历史排行榜
     */
    private static final byte REQ_HISTTORY_RANK = 1;
    /**
     * 下注
     */
    private static final byte BET_ON = 2;
    /**
     * 投票
     */
    private static final byte VOTE = 3;
    /**
     * 更新投票数据排行榜
     */
    private static final byte UPDATE_RANK = 4;
    /**
     * 下发预览奖励
     */
    private static final byte REQ_REWARD_PREVIE = 5;
    private byte subType;
    private Integer dragonBoatId;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        switch (subType) {
            case BET_ON: {
                dragonBoatId = buff.readInt();
            }
            break;
            case VOTE: {
                dragonBoatId = buff.readInt();
            }
            break;

        }
    }

    @Override
    public void execPacket(Player player) {
        DragonBoatModule module = module(MConst.DragonBoat);
        switch (subType) {
            case REQ_ACTIVITY_STATUS_DATA: {
                module.sendActivityData(false);
            }
            break;
            case REQ_HISTTORY_RANK: {
                module.sendAllRank();
            }
            break;
            case BET_ON: {
                module.betOn(dragonBoatId);
            }
            break;
            case VOTE: {
                module.vote(dragonBoatId);
            }
            break;
            case UPDATE_RANK: {
                module.updateOneRank();
            }
            break;
            case REQ_REWARD_PREVIE: {
                module.sendRewardPreview();
            }
            break;
        }
    }

    @Override
    public short getType() {
        return DragonBoatPacketSet.S_DragonBoat;
    }
}
