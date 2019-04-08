package com.stars.modules.sevendaygoal.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/12/6.
 */
public class ActSevenDayFinishCount extends DbRow {
    private long roleId;
    private int operateActId;
    private int type;
    private int finishCount;
    
    public ActSevenDayFinishCount() {
    	
    }
    
    public ActSevenDayFinishCount(long roleId , int operateActId , int type , int finishCount) {
        this.roleId = roleId;
        this.operateActId = operateActId;
        this.type = type;
        this.finishCount = finishCount;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "actsevendayfinishcount", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and type=" + this.getType());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("actsevendayfinishcount", " roleid=" + this.getRoleId() 
        		+ " and operateActId=" + this.getOperateActId() + " and type=" + this.getType());
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
    
    public int getType(){
    	return type;
    }
    
    public void setType(int value){
    	this.type = value;
    }
    
    public int getFinishCount(){
    	return finishCount;
    }
    
    public void setFinishCount(int value){
    	this.finishCount = value;
    }
}
