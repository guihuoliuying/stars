package com.stars.modules.camp.usrdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/7/4.
 */
public class RoleCampTimesPo extends DbRow {
    private long roleId;
    private String activitytimes;
    private String missiontimes;//id+time|id+time
    private Integer dailyRewardTimes;//每日俸禄领取：0表示未领取，1表示已领取
    private String canGetAward = "";//id+id;
    private int cityFightNum;//今日挑战次数（齐楚之战）
    private int donateCount = 0;
    private int campFightScore = 0;//阵营大作战积分
    private String campFightScoreReward = "";//阵营大作战积分
    private Map<Integer, Integer> activityTimeMap;
    private Map<Integer, Integer> missionTimeMap;
    private Set<Integer> canGetAwardSet = new HashSet<>();
    private Map<Integer, Integer> campFightScoreRewardMap = new HashMap<>();
    private int takeSingleRewardTime;//单日单场积分奖励次数

    public RoleCampTimesPo(long roleId, String activitytimes, String missiontimes) {
        this.roleId = roleId;
        setActivitytimes(activitytimes);
        setMissiontimes(missiontimes);
        this.dailyRewardTimes = 0;
    }

    public RoleCampTimesPo() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolecamptimes", " roleid=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolecamptimes", " roleid=" + roleId);
    }

    /**
     * 获取今日活动参与次数
     *
     * @return
     */
    public Map<Integer, Integer> getActivityJoinTimesMap() {
        return activityTimeMap;
    }

    /**
     * 添加今日活动的参与次数
     *
     * @param activityId
     */
    public void addActivityJoinTimes(int activityId) {
        activityTimeMap.put(activityId, getJoinTimesByActId(activityId) + 1);
        activitytimes = StringUtil.makeString(activityTimeMap, '+', '|');
    }

    /**
     * 添加今日活动的完成次数
     *
     * @param missionId
     */
    public void addMissionJoinTimes(int missionId) {
        missionTimeMap.put(missionId, getJoinTimesByMisId(missionId) + 1);
        missiontimes = StringUtil.makeString(missionTimeMap, '+', '|');
    }

    public void addAwardNotGet(int missionId) {
        canGetAwardSet.add(missionId);
        canGetAward = StringUtil.makeString(canGetAwardSet, '+');
    }

    public Set<Integer> getCanGetAwardSet() {
        return canGetAwardSet;
    }

    public void delAwardNotGet(int missionId) {
        canGetAwardSet.remove(missionId);
        canGetAward = StringUtil.makeString(canGetAwardSet, '+');
    }

    public boolean canGet(int missionId) {
        return canGetAwardSet.contains(missionId);
    }

    /**
     * 获取今日该活动的参与次数
     *
     * @param activityId
     * @return
     */
    public Integer getJoinTimesByActId(int activityId) {
        Integer times = activityTimeMap.get(activityId);
        if (times == null) {
            times = 0;
        }
        return times;
    }

    /**
     * 获取今日该活动的参与次数
     *
     * @param missionId
     * @return
     */
    public Integer getJoinTimesByMisId(int missionId) {
        Integer times = missionTimeMap.get(missionId);
        if (times == null) {
            times = 0;
        }
        return times;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getActivitytimes() {
        return activitytimes;
    }

    public void setActivitytimes(String activitytimes) {
        this.activitytimes = activitytimes;
        activityTimeMap = StringUtil.toMap(activitytimes, Integer.class, Integer.class, '+', '|');
    }

    public String getMissiontimes() {
        return missiontimes;
    }

    public void setMissiontimes(String missiontimes) {
        this.missiontimes = missiontimes;
        missionTimeMap = StringUtil.toMap(missiontimes, Integer.class, Integer.class, '+', '|');
    }

    public Integer getDailyRewardTimes() {
        return dailyRewardTimes;
    }

    public void setDailyRewardTimes(Integer dailyRewardTimes) {
        this.dailyRewardTimes = dailyRewardTimes;
    }

    public int getCityFightNum() {
        return cityFightNum;
    }

    public void setCityFightNum(int cityFightNum) {
        this.cityFightNum = cityFightNum;
    }

    public String getCanGetAward() {
        return canGetAward;
    }

    public void setCanGetAward(String canGetAward) {
        this.canGetAward = canGetAward;
        try {
            canGetAwardSet = StringUtil.toHashSet(canGetAward, Integer.class, '+');
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getDonateCount() {
        return donateCount;
    }

    public void setDonateCount(int donateCount) {
        this.donateCount = donateCount;
    }

    public void reset() {
        this.activitytimes = "";
        this.missiontimes = "";
        this.canGetAward = "";
        this.dailyRewardTimes = 0;
        this.cityFightNum = 0;
        this.donateCount = 0;
        this.campFightScore = 0;
        this.campFightScoreReward = "";
        this.takeSingleRewardTime = 0;
        activityTimeMap.clear();
        missionTimeMap.clear();
        canGetAwardSet.clear();
        campFightScoreRewardMap.clear();
    }

    public void addDonateCount(int money) {
        donateCount += money;
    }

    public void addCampFightScore(int score) {
        this.campFightScore += score;
    }

    public int getCampFightScore() {
        return campFightScore;
    }

    public void setCampFightScore(int campFightScore) {
        this.campFightScore = campFightScore;
    }

    public String getCampFightScoreReward() {
        return campFightScoreReward;
    }

    public void setCampFightScoreReward(String campFightScoreReward) {
        this.campFightScoreReward = campFightScoreReward;
        campFightScoreRewardMap = StringUtil.toMap(campFightScoreReward, Integer.class, Integer.class, '+', '|');
    }

    public void takeScoreReward(int score) {
        campFightScoreRewardMap.put(score, 1);
        this.campFightScoreReward = StringUtil.makeString(campFightScoreRewardMap, '+', '|');
    }

    public boolean canTakeScoreReward(int score) {
        Integer flag = campFightScoreRewardMap.get(score);
        return flag == null;
    }

    public Integer getCampFightScoreRewardState(int campFightScore) {
        Integer state = campFightScoreRewardMap.get(campFightScore);
        if (state == null) {
            state = 0;
        }
        return state;
    }

    public void addTakeSingleRewardTime() {
        takeSingleRewardTime++;
    }

    public int getTakeSingleRewardTime() {
        return takeSingleRewardTime;
    }

    public void setTakeSingleRewardTime(int takeSingleRewardTime) {
        this.takeSingleRewardTime = takeSingleRewardTime;
    }
}
