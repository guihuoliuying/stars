package com.stars.modules.mind.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/9/21.
 */
public class RoleMind extends DbRow {
    private long roleId;
    private int mindId;
    private int mindLevel;
    
    public RoleMind() {
    	
    }
    
    public RoleMind(long roleId , int mindId , int mindLevel) {
        this.roleId = roleId;
        this.mindId = mindId;
        this.mindLevel = mindLevel;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolemind", " roleid=" + this.getRoleId() + " and mindId=" + this.getMindId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolemind", " roleid=" + this.getRoleId() + " and mindId=" + this.getMindId());
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getMindId(){
    	return mindId;
    }
    
    public void setMindId(int value){
    	this.mindId = value;
    }
    
    public int getMindLevel(){
    	return mindLevel;
    }
    
    public void setMindLevel(int value){
    	this.mindLevel = value;
    }
}
