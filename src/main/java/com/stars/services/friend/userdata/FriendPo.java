package com.stars.services.friend.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/10.
 */
public class FriendPo extends DbRow {

    private long roleId;
    private long friendId;
    private String friendName;
    private int intimacy;       //亲密度
    private int getFlowerTimes; //收花数量
    private byte dailyGetVigorType;    //今日好友体力领取状态: 0无 1待接收 2已接收

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "friend", "`roleid`=" + roleId + " and `friendid`=" + friendId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `friend` where `roleid`=" + roleId + " and `friendid`=" + friendId;
    }

    public int getIntimacy() {
        return intimacy;
    }

    public void setIntimacy(int intimacy) {
        this.intimacy = intimacy;
    }

    public int getGetFlowerTimes() {
        return getFlowerTimes;
    }

    public void setGetFlowerTimes(int getFlowerTimes) {
        this.getFlowerTimes = getFlowerTimes;
    }

    public byte getDailyGetVigorType() {
        return dailyGetVigorType;
    }

    public void setDailyGetVigorType(byte dailyGetVigorType) {
        this.dailyGetVigorType = dailyGetVigorType;
    }

    public void addIntimacy(int count){
        if(count <=0) return;
        intimacy += count;
    }

    public void addGetFlower(int count){
        if(count <= 0) return;
        getFlowerTimes += count;
    }

    public boolean dailyReset(){
        boolean hasChange = false;
        if(dailyGetVigorType!=0) {
            dailyGetVigorType = 0;
            hasChange = true;
        }
        return hasChange;
    }
}
