package com.stars.modules.newserverrank.prodata;

import com.stars.services.family.FamilyPost;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/17.
 */
public class NewServerRankVo implements Comparable<NewServerRankVo> {
    public static final int RankTypeRoleLevel = 1;

    public static final int RoleLevelEmailId = 21001;

    private int newServerRankId;
    private int operateActId;
    private int type;
    private String rankRange;
    private String reward;
    private String showReward;

    //内存数据
    private int rankStart = -1;
    private int rankEnd = -1;

    private Map<Byte, Integer> familyFightScoreRankMap;

    public int getNewServerRankId() {
        return newServerRankId;
    }

    public void setNewServerRankId(int value) {
        this.newServerRankId = value;
    }

    public int getOperateActId() {
        return operateActId;
    }

    public void setOperateActId(int value) {
        this.operateActId = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int value) {
        this.type = value;
    }

    public String getRankRange() {
        return rankRange;
    }

    public void setRankRange(String value) {
        this.rankRange = value;

        if (rankRange == null || rankRange.equals("") || rankRange.equals("0")) {
            return;
        }
        String sts[] = rankRange.split("\\+");
        if (sts.length >= 2) {
            if (!sts[0].equals("")) {
                rankStart = Integer.parseInt(sts[0]);
            }
            if (!sts[1].equals("")) {
                rankEnd = Integer.parseInt(sts[1]);
            }
        }
    }

    public int getRankStart() {
        return rankStart;
    }

    public int getRankEnd() {
        return rankEnd;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
        String[] rewardStr = reward.split("\\+");
        if (rewardStr.length == 4) {
            familyFightScoreRankMap = new HashMap<>();
            familyFightScoreRankMap.put(FamilyPost.MASTER_ID, Integer.parseInt(rewardStr[0]));
            familyFightScoreRankMap.put(FamilyPost.ASSISTANT_ID, Integer.parseInt(rewardStr[1]));
            familyFightScoreRankMap.put(FamilyPost.ELDER_ID, Integer.parseInt(rewardStr[2]));
            familyFightScoreRankMap.put(FamilyPost.MEMBER_ID, Integer.parseInt(rewardStr[3]));
        }
    }

    public Map<Byte, Integer> getFamilyFightScoreRankMap() {
        return familyFightScoreRankMap;
    }

    public String getShowReward() {
        return showReward;
    }

    public void setShowReward(String showReward) {
        this.showReward = showReward;
    }

    /**
     * 按活动rankStart从小到大排
     */
    @Override
    public int compareTo(NewServerRankVo o) {
        if (this.getRankStart() < o.getRankStart()) {
            return -1;
        } else if (this.getRankStart() > o.getRankStart()) {
            return 1;
        } else {
            return 0;
        }
    }
}
