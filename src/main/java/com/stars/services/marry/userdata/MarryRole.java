package com.stars.services.marry.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class MarryRole extends DbRow {

    private long roleId;
    private String name;    // 角色名字
    private int jobId;    // 模型id
    private int fight;      // 战力
    private int level;      // 等级
    private String claim = "";   // 宣言
    private int reqLevel;   // 要求等级
    private int claimStamp;  // 发布宣言的时间
    private String marryKey = "";    // 结婚key
    private int popularity; // 人气
    private byte dungeon;   // 副本次数

    public byte getDungeon() {
        return dungeon;
    }

    public void setDungeon(byte dungeon) {
        this.dungeon = dungeon;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public String getMarryKey() {
        return marryKey;
    }

    public void setMarryKey(String marryKey) {
        this.marryKey = marryKey;
    }

    public int getClaimStamp() {
        return claimStamp;
    }

    public void setClaimStamp(int claimStamp) {
        this.claimStamp = claimStamp;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getFight() {
        return fight;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(int reqLevel) {
        this.reqLevel = reqLevel;
    }

    public void addDungeon() {
        dungeon += 1;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "marryrole", "roleid = " + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }

    @Override
    public String toString() {
        return "MarryRole{" +
                "roleId=" + roleId +
                ", dungeon=" + dungeon +
                "} " + super.toString();
    }
}
