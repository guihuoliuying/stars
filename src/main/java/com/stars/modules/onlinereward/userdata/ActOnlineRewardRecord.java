package com.stars.modules.onlinereward.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/12/6.
 */
public class ActOnlineRewardRecord extends DbRow {
    private long roleId;
    private int operateActId;
    private int rewardId;
    private byte isGot;
    
    public ActOnlineRewardRecord() {
    	
    }
    
    public ActOnlineRewardRecord(long roleId , int operateActId , int rewardId , byte isGot) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.rewardId = rewardId;
        this.isGot = isGot;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "actonlinerewardrecord", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and rewardId=" + this.getRewardId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("actonlinerewardrecord", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and rewardId=" + this.getRewardId());
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getOperateActId(){
    	return operateActId;
    }
    
    public void setOperateActId(int value){
    	this.operateActId = value;
    }
    
    public int getRewardId(){
    	return rewardId;
    }
    
    public void setRewardId(int value){
    	this.rewardId = value;
    }
    
    public byte getIsGot(){
    	return isGot;
    }
    
    public void setIsGot(byte value){
    	this.isGot = value;
    }
}
