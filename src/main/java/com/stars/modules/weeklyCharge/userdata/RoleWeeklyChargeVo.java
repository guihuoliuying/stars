package com.stars.modules.weeklyCharge.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxie on 2017/5/5.
 */
public class RoleWeeklyChargeVo extends DbRow {

    private long roleId;
    private int roleTotalCharge;    //活动期间的累计充值,单位：元
    private int weeklyLevel;        //触发周累计充值时的角色等级
    private int weeklyVipLevel;     //触发周累计充值时的vip等级
    private int curActId;           //当前活动id
    private List<Integer> sendAwardList = new ArrayList<>();

    public RoleWeeklyChargeVo() {

    }
    public RoleWeeklyChargeVo(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getRoleTotalCharge() {
        return roleTotalCharge;
    }

    public void setRoleTotalCharge(int roleTotalCharge) {
        this.roleTotalCharge = roleTotalCharge;
    }

    public void addTotalCharge(int money){
        if(money <= 0) return;
        this.roleTotalCharge += money;
    }

    public int getWeeklyLevel() {
        return weeklyLevel;
    }

    public void setWeeklyLevel(int weeklyLevel) {
        this.weeklyLevel = weeklyLevel;
    }

    public int getWeeklyVipLevel() {
        return weeklyVipLevel;
    }

    public void setWeeklyVipLevel(int weeklyVipLevel) {
        this.weeklyVipLevel = weeklyVipLevel;
    }

    public int getCurActId() {
        return curActId;
    }

    public void setCurActId(int curActId) {
        this.curActId = curActId;
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

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleweeklycharge", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleweeklycharge", "`roleid`=" + roleId);
    }

}

