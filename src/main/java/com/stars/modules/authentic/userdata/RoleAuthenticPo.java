package com.stars.modules.authentic.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2016/12/23.
 */
public class RoleAuthenticPo extends DbRow {
    private long roleId;            //玩家Id
    private int moneyFreeCount;     //金币鉴宝免费次数
    //    private int goldFreeCount;      //元宝鉴宝免费次数
    private int moneyEnsureCount;   //金币保底次数
    private int goldEnsureCount;    //元宝保底次数
    private int moneyCount;         //当天金币鉴宝次数
    private int goldCount;          //当天元宝鉴宝次数
    private long moneyTime;         //上一次金币鉴宝时间
    private long goldTime;          //上一次元宝鉴宝时间
    private int newbeeMoneyCount;   //新手金币鉴宝次数
    private int newbeeGoldCount;    //新手元宝鉴宝次数

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(moneyFreeCount);
//        buff.writeInt(goldFreeCount);
        buff.writeInt(moneyEnsureCount);
        buff.writeInt(goldEnsureCount);
        buff.writeInt(moneyCount);
        buff.writeInt(goldCount);
        buff.writeLong(moneyTime);
        buff.writeLong(goldTime);
    }

    public RoleAuthenticPo() {

    }

    public RoleAuthenticPo(long roleId) {
        this.roleId = roleId;
        moneyFreeCount = 0;
//        goldFreeCount = 0;
        moneyEnsureCount = 0;
        moneyCount = 0;
        goldCount = 0;
        moneyTime = 0;
        goldTime = 0;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getMoneyFreeCount() {
        return moneyFreeCount;
    }

    public void setMoneyFreeCount(int moneyFreeCount) {
        this.moneyFreeCount = moneyFreeCount;
    }

    /*public int getGoldFreeCount() {
        return goldFreeCount;
    }

    public void setGoldFreeCount(int goldFreeCount) {
        this.goldFreeCount = goldFreeCount;
    }*/

    public int getMoneyEnsureCount() {
        return moneyEnsureCount;
    }

    public void setMoneyEnsureCount(int moneyEnsureCount) {
        this.moneyEnsureCount = moneyEnsureCount;
    }

    public int getGoldEnsureCount() {
        return goldEnsureCount;
    }

    public void setGoldEnsureCount(int goldEnsureCount) {
        this.goldEnsureCount = goldEnsureCount;
    }

    public int getMoneyCount() {
        return moneyCount;
    }

    public void setMoneyCount(int moneyCount) {
        this.moneyCount = moneyCount;
    }

    public int getGoldCount() {
        return goldCount;
    }

    public int getNewbeeMoneyCount() {
        return newbeeMoneyCount;
    }

    public void setNewbeeMoneyCount(int newbeeMoneyCount) {
        this.newbeeMoneyCount = newbeeMoneyCount;
    }

    public int getNewbeeGoldCount() {
        return newbeeGoldCount;
    }

    public void setNewbeeGoldCount(int newbeeGoldCount) {
        this.newbeeGoldCount = newbeeGoldCount;
    }

    public void setGoldCount(int goldCount) {
        this.goldCount = goldCount;
    }

    public long getMoneyTime() {
        return moneyTime;
    }

    public void setMoneyTime(long moneyTime) {
        this.moneyTime = moneyTime;
    }

    public long getGoldTime() {
        return goldTime;
    }

    public void setGoldTime(long goldTime) {
        this.goldTime = goldTime;
    }

    public RoleAuthenticPo setMoneyFreeCountInc() {
        this.moneyFreeCount++;
        return this;
    }

    public RoleAuthenticPo setNewbeeGoldCountInc(){
        this.newbeeGoldCount++;
        return this;
    }

    public RoleAuthenticPo setNewbeeMoneyCountInc(){
        this.newbeeMoneyCount++;
        return this;
    }

    /*public RoleAuthenticPo setGoldFreeCountInc() {
        this.goldFreeCount++;
        return this;
    }*/

    public RoleAuthenticPo setMoneyEnsureCountInc() {
        this.moneyEnsureCount++;
        return this;
    }

    public RoleAuthenticPo setGoldEnsureCountInc() {
        this.goldEnsureCount++;
        return this;
    }

    public RoleAuthenticPo setMoneyCountInc() {
        this.moneyCount++;
        return this;
    }

    public RoleAuthenticPo setGoldCountInc() {
        this.goldCount++;
        return this;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleauthentic", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `roleauthentic` where `roleid`=" + roleId;
    }
}
