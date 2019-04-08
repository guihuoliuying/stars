package com.stars.modules.mooncake.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangerjiang on 2017/9/15.
 */
public class RoleMoonCakePo extends DbRow {
    private long roleId;
    private int iWeekSingleMaxScore;//本周单局最高积分
    //    private int iMoonCakeRank;//我的月饼游戏当前排名
    private int iDaySingleMaxScore;//当天单局最高积分
    private String sTargetReward;//可领取 or 未领取 or 不可领取的积分奖励 or 已领取 long,1 or 0 or -1 or 2|...;
//    private long beginTime;//单局游戏开始时间


    //内存数据
    private Map<Integer, Integer> targetScoreRwdMap = new HashMap<>();//可领取 or 未领取 or 不可领取的积分奖励 or 已领取 long,1 or 0 or -1 or 2|...;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getiWeekSingleMaxScore() {
        return iWeekSingleMaxScore;
    }

    public void setiWeekSingleMaxScore(int iWeekSingleMaxScore) {
        this.iWeekSingleMaxScore = iWeekSingleMaxScore;
    }

//    public int getiMoonCakeRank() {
//        return iMoonCakeRank;
//    }
//
//    public void setiMoonCakeRank(int iMoonCakeRank) {
//        this.iMoonCakeRank = iMoonCakeRank;
//    }

    public int getiDaySingleMaxScore() {
        return iDaySingleMaxScore;
    }

    public void setiDaySingleMaxScore(int iDaySingleMaxScore) {
        this.iDaySingleMaxScore = iDaySingleMaxScore;
    }

    public String getsTargetReward() {
        return sTargetReward;
    }

    public void setsTargetReward(String sTargetReward) {
        this.sTargetReward = sTargetReward;
        if (sTargetReward == null || sTargetReward.equals("")) {
            this.targetScoreRwdMap = new HashMap<>();
            return;
        }
        this.targetScoreRwdMap = StringUtil.toMap(sTargetReward, Integer.class, Integer.class, '+', '|');
    }

    public void resetTarget() {
        this.sTargetReward = "";
        this.targetScoreRwdMap.clear();
    }

//    public long getBeginTime() {
//        return beginTime;
//    }
//
//    public void setBeginTime(long beginTime) {
//        this.beginTime = beginTime;
//    }

    public Map<Integer, Integer> getTargetScoreRwdMap() {
        return targetScoreRwdMap;
    }

    public void updateTargetScoreRwdState(int iTargetId, int iState) {
        targetScoreRwdMap.put(iTargetId, iState);
        this.sTargetReward = StringUtil.makeString(targetScoreRwdMap, '+', '|');
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolemooncake", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolemooncake", "`roleid`=" + roleId);
    }

    @Override
    public String toString() {
        return "RoleMoonCakePo{" +
                "roleId=" + roleId +
                ", iWeekSingleMaxScore=" + iWeekSingleMaxScore +
//                ", iMoonCakeRank=" + iMoonCakeRank +
                ", iDaySingleMaxScore=" + iDaySingleMaxScore +
                ", targetScoreRwdMap=" + targetScoreRwdMap +
                ", sTargetReward='" + sTargetReward + '\'' +
                '}';
    }
}
