package com.stars.modules.onlinereward.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/12/6.
 */
public class ActOnlineTime extends DbRow {
    private long roleId;
    private int operateActId;
    private int onlineTime;
    
    public ActOnlineTime() {
    	
    }
    
    public ActOnlineTime(long roleId , int operateActId , int onlineTime) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.onlineTime = onlineTime;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "actonlinetime", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("actonlinetime", " roleid=" + this.getRoleId() 
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
    
    public int getOnlineTime(){
    	return onlineTime;
    }
    
    public void setOnlineTime(int value){
    	this.onlineTime = value;
    }
}
