package com.stars.multiserver.daregod.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.daregod.DareGodManager;
import com.stars.multiserver.daregod.DareGodConst;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class RoleDareGod extends DbRow implements Comparable<RoleDareGod> {
    private long roleId;
    private int serverId;
    private String roleName;

    private long damage;//当天总伤害
    private int fightScore;//战力

    private int fightType;//当前所处战力段,每次重置都是0，第一次打开界面战斗时计算
    private String targetDrop;//可领取 or 未领取 or 不可领取的伤害奖励 int,1 or 0 or -1|...;

    private int canFightTimes;//还可以挑战多少次
    private int buyTimes;//已购买次数

    private int fashionId;
    private int jobId;

    private Map<Integer, Integer> targetDropMap = new HashMap<>();//可领取 or 未领取 or 不可领取的伤害奖励 int,1 or 0 or -1|...;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public long getDamage() {
        return damage;
    }

    public void addDamage(long damage) {
        this.damage += damage;
    }

    public void setDamage(long damage) {
        this.damage = damage;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getFightType() {
        return fightType;
    }

    public void setFightType(int fightType) {
        this.fightType = fightType;
    }

    public String getTargetDrop() {
        return targetDrop;
    }

    public void setTargetDrop(String targetDrop) {
        this.targetDrop = targetDrop;
        this.targetDropMap = StringUtil.toMap(targetDrop, Integer.class, Integer.class, '+', '|');
    }

//    public String getDamageDrop() {
//        return damageDrop;
//    }
//
//    public void setDamageDrop(String damageDrop) {
//        this.damageDrop = damageDrop;
//        this.damageDropMap = StringUtil.toMap(damageDrop, Long.class, Integer.class, '+', '|');
//    }

    public int getCanFightTimes() {
        return canFightTimes;
    }

    public void setCanFightTimes(int canFightTimes) {
        this.canFightTimes = canFightTimes;
    }

    public int getBuyTimes() {
        return buyTimes;
    }

    public void setBuyTimes(int buyTimes) {
        this.buyTimes = buyTimes;
    }

    public void addBuyAndCanFightTimes(int time) {
        this.buyTimes += time;
        this.canFightTimes += time;
    }

    public int getFashionId() {
        return fashionId;
    }

    public void setFashionId(int fashionId) {
        this.fashionId = fashionId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public Set<Integer> getUnGetAward() {
        Set<Integer> tmpSet = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : targetDropMap.entrySet()) {
            if (entry.getValue() == DareGodConst.UN_GETED) {
                tmpSet.add(entry.getKey());
            }
        }
        return tmpSet;
    }

    public Map<Integer, Integer> getTargetDropMap() {
        return targetDropMap;
    }

    public void updateDamageDropState(int targetId, int state) {
        targetDropMap.put(targetId, state);
        this.targetDrop = StringUtil.makeString(targetDropMap, '+', '|');
    }

    public void onResetDaily() {
        targetDropMap.clear();
        this.targetDrop = StringUtil.makeString(targetDropMap, '+', '|');
        buyTimes = 0;
        damage = 0L;
        fightType = 0;
        canFightTimes = DareGodManager.DARE_FREE_TIMES;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "roledaregod", " `roleid`=" + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from roledaregod where roleid=" + this.roleId;
    }

    @Override
    public int compareTo(RoleDareGod o) {
        if (roleId == o.roleId) return 0;
        if (damage < o.damage) return 1;
        if (damage > o.damage) return -1;
        return (int) (roleId - o.roleId);
    }
}
