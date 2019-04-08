package com.stars.modules.dailyCharge.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class RoleDailyCharge extends DbRow {
    private long roleId;
    private int totalCharge;    //活动期间的累计充值,单位：元
    private int dailyLevel;     //触发日累计充值时的角色等级
    private int dailyVipLevel;  //触发日累计充值时的vip等级
    private int curActId;       //当前活动id
    private List<Integer> sendAwardList = new ArrayList<>();

    public RoleDailyCharge() {
    }

    public RoleDailyCharge(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public void addTotalCharge(int money){
        if(money <= 0) return;
        this.totalCharge += money;
    }

    public int getDailyLevel() {
        return dailyLevel;
    }

    public void setDailyLevel(int dailyLevel) {
        this.dailyLevel = dailyLevel;
    }

    public int getDailyVipLevel() {
        return dailyVipLevel;
    }

    public void setDailyVipLevel(int dailyVipLevel) {
        this.dailyVipLevel = dailyVipLevel;
    }

    public List<Integer> getSendAwardList() {
        return sendAwardList;
    }

    public boolean hasSendAward(int id){
        if(StringUtil.isEmpty(sendAwardList)) return false;
        return sendAwardList.contains(id);
    }

    public void recordSendAward(int id){
        if(sendAwardList == null){
            sendAwardList = new ArrayList<>();
        }
        if(sendAwardList.contains(id)) return;
        sendAwardList.add(id);
    }

    public String getSendAward() {
        return StringUtil.makeString(sendAwardList,',');
    }

    public void setSendAward(String sendAward) throws Exception{
        this.sendAwardList = StringUtil.toArrayList(sendAward,Integer.class,',');
    }

    public int getCurActId() {
        return curActId;
    }

    public void setCurActId(int curActId) {
        this.curActId = curActId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roledailycharge", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roledailycharge", "`roleid`=" + roleId);
    }
}
