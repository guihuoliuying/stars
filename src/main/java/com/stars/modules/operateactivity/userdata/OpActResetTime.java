package com.stars.modules.operateactivity.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/12/12.
 */
public class OpActResetTime extends DbRow {
    private long roleId;
    private int operateActId;
    private long lastResetTimeStamp;
    
    public OpActResetTime() {
    	
    }
    
    public OpActResetTime(long roleId , int operateActId , long lastResetTimeStamp) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.lastResetTimeStamp = lastResetTimeStamp;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "opactresettime", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("opactresettime", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId());
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
    
    public long getLastResetTimeStamp(){
    	return lastResetTimeStamp;
    }
    
    public void setLastResetTimeStamp(long value){
    	this.lastResetTimeStamp = value;
    }
}
