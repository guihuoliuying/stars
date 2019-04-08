package com.stars.modules.newserversign.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/12/22.
 */
public class ActSignRewardRecord extends DbRow {
	public static byte Reward_Status_Cannot_get = 0;
	public static byte Reward_Status_Can_get = 1;
	public static byte Reward_Status_Out_Of_Date = 2;
	public static byte Reward_Status_Have_Got = 3;
	
    private long roleId;
    private int operateActId;
    private int newServerSignId;
    private byte isGot;
    
    public ActSignRewardRecord() {
    	
    }
    
    public ActSignRewardRecord(long roleId , int operateActId , int newServerSignId , byte isGot) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.newServerSignId = newServerSignId;
        this.isGot = isGot;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "actsignrewardrecord", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and newServerSignId=" + this.getNewServerSignId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("actsignrewardrecord", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and newServerSignId=" + this.getNewServerSignId());
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
    
    public int getNewServerSignId(){
    	return newServerSignId;
    }
    
    public void setNewServerSignId(int value){
    	this.newServerSignId = value;
    }
    
    public byte getIsGot(){
    	return isGot;
    }
    
    public void setIsGot(byte value){
    	this.isGot = value;
    }
}
