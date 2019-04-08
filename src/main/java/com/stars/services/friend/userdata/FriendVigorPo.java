package com.stars.services.friend.userdata;


import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2016/11/14.
 */
public class FriendVigorPo extends DbRow {

    public FriendVigorPo() {
    }

    public FriendVigorPo(long roleId) {
        this.roleId = roleId;
        this.dailySendVigorList = new ArrayList<>();
    }

    private long roleId;
    private int dailySendVigorTimes;            //今日赠送体力次数
    private int dailyReceiveVigorTimes;         //今日接收体力次数
    private List<Long> dailySendVigorList;      //今日赠送体力名单

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getDailySendVigorTimes() {
        return dailySendVigorTimes;
    }

    public void setDailySendVigorTimes(int dailySendVigorTimes) {
        this.dailySendVigorTimes = dailySendVigorTimes;
    }

    public int getDailyReceiveVigorTimes() {
        return dailyReceiveVigorTimes;
    }

    public void setDailyReceiveVigorTimes(int dailyReceiveVigorTimes) {
        this.dailyReceiveVigorTimes = dailyReceiveVigorTimes;
    }

    public List<Long> getDailySendVigorList() {
        if(dailySendVigorList == null){
            dailySendVigorList = new ArrayList<>();
        }
        return dailySendVigorList;
    }

    public void setDailySendVigorList(List<Long> dailySendVigorList) {
        this.dailySendVigorList = dailySendVigorList;
    }

    public String getDailySendVigorStr() {
        return StringUtil.makeString(dailySendVigorList,',');
    }

    public void setDailySendVigorStr(String dailySendVigorStr) throws Exception {
        if(StringUtil.isNotEmpty(dailySendVigorStr)) {
            this.dailySendVigorList = StringUtil.toArrayList(dailySendVigorStr, Long.class, ',');
        }
    }

    public void addDailySendVigorTimes(){
        this.dailySendVigorTimes++;
    }

    public void addDailyReceiveVigorTimes(){
        this.dailyReceiveVigorTimes++;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "friendvigor", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `friendvigor` where `roleid`=" + roleId;
    }

    public boolean dailyReset(){
        boolean hasChange = false;
        if(dailyReceiveVigorTimes!=0){
            dailyReceiveVigorTimes = 0;
            hasChange = true;
        }
        if(dailySendVigorTimes!=0){
            dailySendVigorTimes = 0;
            hasChange = true;
        }
        if(StringUtil.isNotEmpty(dailySendVigorList)) {
            dailySendVigorList.clear();
            hasChange = true;
        }
        return hasChange;
    }
}
