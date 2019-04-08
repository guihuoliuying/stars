package com.stars.modules.achievement.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.achievement.AchievementManager;
import com.stars.modules.achievement.AchievementPacketSet;
import com.stars.modules.achievement.prodata.AchievementStageVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.achievement.userdata.AchievementStagePo;
import com.stars.modules.rank.RankManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.AchievementRankPo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class ClientAchievement extends PlayerPacket {

    public static final byte RES_DATA = 1;
    public static final byte RES_AWARD = 2;
    public static final byte RES_STAGEUP_RESULT = 3;
    public static final byte RES_VIEW_RANK = 4;
    public static final byte RES_GET_PERFECT_AWARD_RESULT = 5;
    public static final byte RES_STAGE_AWARD_STATUS = 6; //可领取奖励的段位集

    private byte resType;
    private Map<Integer, AchievementRow> roleAchievement;
    private AchievementStagePo roleAchievementStagePo;
    private String award;
    private int achievementId;
    private int rankRate;
    private byte result;
    private int stage;
    private Set<Integer> stageSet;

    List<AbstractRankPo> rankList;

    public void setAchievementId(int achievementId) {
        this.achievementId = achievementId;
    }

    public void setResType(byte resType) {
        this.resType = resType;
    }

    public void setRoleAchievement(Map<Integer, AchievementRow> roleAchievement) {
        this.roleAchievement = roleAchievement;
    }

    public void setAward(String award) {
        this.award = award;
    }

    public AchievementStagePo getRoleAchievementStagePo() {
        return roleAchievementStagePo;
    }

    public void setRoleAchievementStagePo(AchievementStagePo roleAchievementStagePo) {
        this.roleAchievementStagePo = roleAchievementStagePo;
    }

    public void setRankRate(int rankRate) {
        this.rankRate = rankRate;
    }


    public void setRankList(List<AbstractRankPo> rankList) {
        this.rankList = rankList;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public void setStageSet(Set<Integer> stageSet) {
        this.stageSet = stageSet;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(resType);
        if (resType == RES_DATA) {
            Map<Integer, AchievementStageVo> achievementStageVoMap = AchievementManager.getAchievementStageVoMap();
            buff.writeInt(achievementStageVoMap.size()); //总段位数
            for(AchievementStageVo achievementStageVo: achievementStageVoMap.values()){
                int stage = achievementStageVo.getStage();
                buff.writeInt(stage); //段位id
                buff.writeByte(roleAchievementStagePo.getMaxActiveStage() >= stage ?(byte)1 :(byte)0); //是否激活该段位 1--已开启 0-- 未开启
                if(roleAchievementStagePo.getMaxActiveStage() >= stage && roleAchievementStagePo.getStageScoreMap().get(stage) != null){
                    buff.writeInt(roleAchievementStagePo.getStageScoreMap().get(stage)); //该段位成就值
                }else{
                    buff.writeInt(0);
                }
                buff.writeByte(roleAchievementStagePo.getCommonAwardMap().get(stage) == null ? (byte)0 : (byte)1);
                buff.writeByte(roleAchievementStagePo.getPerfectAwardMap().get(stage) == null ? (byte)0 : (byte)1);
            }

            buff.writeInt(roleAchievement.size()); // 成就
            for (AchievementRow row : roleAchievement.values()) {
                buff.writeInt(row.getAchievementId());
                buff.writeByte(row.getState());
                buff.writeString(row.getProcessing());
            }
            buff.writeInt(roleAchievementStagePo.getTotalStageScore()); //总的成就积分
            buff.writeInt(rankRate);

        }
        if (resType == RES_AWARD) {
            buff.writeInt(achievementId);
            buff.writeString(award);
        }

        if (resType == RES_STAGEUP_RESULT){
            //buff.writeByte(result);
        }

        if (resType == RES_VIEW_RANK){
            int size = rankList.size();
            buff.writeInt(size);
            for(AbstractRankPo po: rankList){
                AchievementRankPo rankPo = (AchievementRankPo) po;
                buff.writeInt(rankPo.getRank());  //排名
                buff.writeString(rankPo.getName());  //名字
                buff.writeInt(rankPo.getFightScore()); //战力
                AchievementStageVo stageVo = AchievementManager.getAchievementStageVoByStage(rankPo.getStage());
                if(stageVo != null){
                    buff.writeString(stageVo.getName());  //成就段位
                }else{
                    buff.writeString("");
                }
                buff.writeInt(rankPo.getAchieveScore()); //成就值
                RankAwardVo awardVo = RankManager.getRankAwardVo(RankConstant.RANKID_ACHIEVEMENT,rankPo.getRank());
                if(awardVo != null){
                    buff.writeString(awardVo.getAward()); //成就奖励
                }else{
                    buff.writeString("");
                }
            }
        }
        if (resType == RES_GET_PERFECT_AWARD_RESULT){
            buff.writeInt(stage);
        }
        if (resType == RES_STAGE_AWARD_STATUS){
            buff.writeInt(stageSet.size());
            for(Integer stage: stageSet){
                buff.writeInt(stage);
            }
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return AchievementPacketSet.C_ACHIEVEMENT;
    }
}
