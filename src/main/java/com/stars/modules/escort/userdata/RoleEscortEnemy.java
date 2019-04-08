package com.stars.modules.escort.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by wuyuxing on 2016/12/5.
 */
public class RoleEscortEnemy extends DbRow {
    private long roleId;
    private long enemyId;   //仇人id
    private long lastTime;  //持续时间(结束时间)

    public RoleEscortEnemy() {
    }

    public RoleEscortEnemy(long roleId, long enemyId, long lastTime) {
        this.roleId = roleId;
        this.enemyId = enemyId;
        this.lastTime = lastTime;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(long enemyId) {
        this.enemyId = enemyId;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public void addLastTime(long lastTime){
        if(lastTime<=0) return;
        this.lastTime += lastTime;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleescortenemy", "`roleid`=" + roleId + " and `enemyid`=" + enemyId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleescortenemy", "`roleid`=" + roleId + " and `enemyid`=" + enemyId);
    }

    public boolean isTimeOut(long now){
        return lastTime <= now;
    }
}
