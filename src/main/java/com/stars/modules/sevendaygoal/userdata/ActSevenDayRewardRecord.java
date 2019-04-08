package com.stars.modules.sevendaygoal.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/12/6.
 */
public class ActSevenDayRewardRecord extends DbRow {
    /**
     * 当roleId为-1时，则为全服的奖励领取次数
     */
	private long roleId;
    private int operateActId;
    private int goalId;
    private int gotCount;
    
    public ActSevenDayRewardRecord() {
    	
    }
    
    public ActSevenDayRewardRecord(long roleId , int operateActId , int goalId , int gotCount) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.goalId = goalId;
        this.gotCount = gotCount;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "actsevendayrewardrecord", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and goalId=" + this.getGoalId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("actsevendayrewardrecord", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and goalId=" + this.getGoalId());
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
    
    public int getGoalId(){
    	return goalId;
    }
    
    public void setGoalId(int value){
    	this.goalId = value;
    }
    
    public int getGotCount(){
    	return gotCount;
    }
    
    public void setGotCount(int value){
    	this.gotCount = value;
    }
}
