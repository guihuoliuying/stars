package com.stars.modules.achievement.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.achievement.AchievementManager;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.achievement.AchievementPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class ServerAchievement extends PlayerPacket {

    private static final byte REQ_DATA = 1;
    private static final byte REQ_AWARD = 2;
    private static final byte REQ_STAGE_COMMON_AWARD = 3; //请求进阶
    private static final byte REQ_STAGE_PERFECT_AWARD = 4; //请求领取完美奖励
    private static final byte REQ_VIEW_RANK = 5; //请求查看排行榜

    private byte reqType;
    private int achievementId;
    private int stage;


    @Override
    public void execPacket(Player player) {
        AchievementModule achievementModule = module(MConst.Achievement);
        if (reqType == REQ_DATA) {
            achievementModule.view();
        }
        if (reqType == REQ_AWARD) {
            achievementModule.award(achievementId);
        }
        if (reqType == REQ_STAGE_COMMON_AWARD){
            achievementModule.stageAward(stage, AchievementManager.COMMON_STAGE_AWARD);
        }
        if (reqType == REQ_STAGE_PERFECT_AWARD){
            achievementModule.stageAward(stage,AchievementManager.PERFECT_STAGE_AWARD);
        }
        if (reqType == REQ_VIEW_RANK){
            achievementModule.viewRank();
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        if (reqType == REQ_AWARD) {
            achievementId = buff.readInt();
        }
        if (reqType == REQ_STAGE_COMMON_AWARD ||reqType == REQ_STAGE_PERFECT_AWARD) {
            stage = buff.readInt();
        }
        if (reqType == REQ_VIEW_RANK){

        }
    }

    @Override
    public short getType() {
        return AchievementPacketSet.S_ACHIEVEMENT;
    }
}
