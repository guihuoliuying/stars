package com.stars.modules.bestcp520.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class RoleBestCPReward extends DbRow {
    private long roleId;
    private String date;//领奖日期
    private String rewardTime;//领奖次数
    private int voteSum;//当日投票次数
    private Map<Integer,Integer> rewardMap;

    public RoleBestCPReward() {

    }

    public RoleBestCPReward(long roleId, String date, String rewardTime, int voteSum) {
        this.roleId = roleId;
        this.date = date;
        setRewardTime(rewardTime);
        this.voteSum = voteSum;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRewardTime() {
        return rewardTime;
    }

    public void setRewardTime(String rewardTime) {
        rewardMap = StringUtil.toMap(rewardTime, Integer.class, Integer.class, '+', '|');
        this.rewardTime = rewardTime;
    }

    public Map<Integer,Integer> getRewardMap() {
        return rewardMap;
    }

    public void putReward(Integer group, int rewarded) {
        rewardMap.put(group, rewarded);
        rewardTime = StringUtil.makeString(rewardMap, '+', '|');
    }

    public int getVoteSum() {
        return voteSum;
    }

    public void setVoteSum(int voteSum) {
        this.voteSum = voteSum;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolebestcpreward", "roleid=" + roleId + " and date='" + date + "'");
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolebestcpreward", "roleid=" + roleId + " and date='" + date + "'");
    }

    public static void main(String[] args) {
        RoleBestCPReward roleBestCPReward=new RoleBestCPReward(1,"","",1);
        roleBestCPReward.putReward(1,1);
        roleBestCPReward.putReward(2,0);
        System.out.println(roleBestCPReward.rewardTime);
    }
}
