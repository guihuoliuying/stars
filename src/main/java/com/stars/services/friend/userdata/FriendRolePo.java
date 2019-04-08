package com.stars.services.friend.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/27.
 */
public class FriendRolePo extends DbRow {

    private long roleId;
    private String name;
    private int jobId;
    private int level;
    private int fightScore;
    private int sendFlower;
    private int receiveFlower;
    private byte dailyFirstSendFlower;  //每日首次送花标识
    private long lastDailyResetTime;    //上次每日重置时间
    private int offlineTimestamp;

    public FriendRolePo() {
    }

    public FriendRolePo(long roleId, String name, int jobId, int level, int fightScore) {
        this.roleId = roleId;
        this.name = name;
        this.jobId = jobId;
        this.level = level;
        this.fightScore = fightScore;
        this.offlineTimestamp = (int) (System.currentTimeMillis() / 1000);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "friendrole", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `friendrole` where `roleid`=" + roleId;
    }

    /* Db Data Getter/Setter */
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getOfflineTimestamp() {
        return offlineTimestamp;
    }

    public void setOfflineTimestamp(int offlineTimestamp) {
        this.offlineTimestamp = offlineTimestamp;
    }

    public int getSendFlower() {
        return sendFlower;
    }

    public void setSendFlower(int sendFlower) {
        this.sendFlower = sendFlower;
    }

    public int getReceiveFlower() {
        return receiveFlower;
    }

    public void setReceiveFlower(int receiveFlower) {
        this.receiveFlower = receiveFlower;
    }

    public void addSendFlower(int count){
        if(count<=0) return;
        this.sendFlower += count;
    }

    public void addReceiveFlower(int count){
        if(count<=0) return;
        this.receiveFlower += count;
    }

    public byte getDailyFirstSendFlower() {
        return dailyFirstSendFlower;
    }

    public void setDailyFirstSendFlower(byte dailyFirstSendFlower) {
        this.dailyFirstSendFlower = dailyFirstSendFlower;
    }

    public void resetDailyFirstSendFlower(){
        this.dailyFirstSendFlower = 0;
    }

    public void addDailyFirstSendFlower(){
        this.dailyFirstSendFlower++;
    }

    public long getLastDailyResetTime() {
        return lastDailyResetTime;
    }

    public void setLastDailyResetTime(long lastDailyResetTime) {
        this.lastDailyResetTime = lastDailyResetTime;
    }
}
