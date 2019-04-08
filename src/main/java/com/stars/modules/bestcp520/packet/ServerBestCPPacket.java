package com.stars.modules.bestcp520.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.bestcp520.BestCPModule;
import com.stars.modules.bestcp520.BestCPPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class ServerBestCPPacket extends PlayerPacket {
    private byte subType;
    private int cpId;
    private int voteNum;
    public static final byte TAKE_REWARD = 1;//领奖
    public static final byte BEST_CP_RANK = 2;//最佳组合排行榜
    public static final byte BEST_CP_VOTER_RANK = 3;//最佳组合的个人投票排行榜
    public static final byte VOTE = 4;//投票
    public static final byte REQ_REWARD_TIME = 5;//请求领奖记录
    public static final byte REQ_ACTIVITY_UI = 6;//请求活动配置

    public ServerBestCPPacket() {
    }


    @Override
    public short getType() {
        return BestCPPacketSet.S_BEST_CP;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

        subType = buff.readByte();
        switch (subType) {
            case TAKE_REWARD: {//领奖
                voteNum = buff.readInt();
            }
            break;
            case BEST_CP_VOTER_RANK:
            case VOTE: {//投票
                cpId =buff.readInt();
            }
            break;
        }
    }

    @Override
    public void execPacket(Player player) {
        BestCPModule bestCPModule = module(MConst.BestCP520);
        switch (subType) {
            case TAKE_REWARD: {//领奖
                bestCPModule.takeReward(voteNum);
            }
            break;
            case BEST_CP_RANK: {//最佳组合排行榜
                bestCPModule.sendBestCPRank();
            }
            break;
            case BEST_CP_VOTER_RANK: {//最佳组合的个人投票排行榜
                bestCPModule.sendBestCPVoterRank(cpId);
            }
            break;
            case VOTE: {//投票
                bestCPModule.vote(cpId);
            }
            break;
            case REQ_REWARD_TIME: {//请求领奖记录
                bestCPModule.sendCanTakeReward();
            }break;
            case REQ_ACTIVITY_UI:{
                bestCPModule.sendActivityUI();
            }break;
        }
    }

    @Override
    public void execPacket() {

    }
}
