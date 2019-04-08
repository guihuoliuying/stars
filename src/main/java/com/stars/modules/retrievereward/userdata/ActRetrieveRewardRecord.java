package com.stars.modules.retrievereward.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/12/13.
 */
public class ActRetrieveRewardRecord extends DbRow {
    private long roleId;
    private int operateActId;
    private int rewardId;
    private int revieveCount;
    
    public ActRetrieveRewardRecord() {
    	
    }
    
    public ActRetrieveRewardRecord(long roleId , int operateActId , int rewardId , int revieveCount) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.rewardId = rewardId;
        this.revieveCount = revieveCount;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "actrevieverewardrecord", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and rewardId=" + this.getRewardId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("actrevieverewardrecord", " roleid=" + this.getRoleId() 
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
    
    public int getRevieveCount(){
    	return revieveCount;
    }
    
    public void setRevieveCount(int value){
    	this.revieveCount = value;
    }
}
