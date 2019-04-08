package com.stars.modules.dragonboat.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by huwenjun on 2017/5/15.
 */
public class RoleBetOnDragonBoatPo extends DbRow {
    private Long roleId;
    private Long stageTime;
    private int dragonBoatId;

    public RoleBetOnDragonBoatPo() {
    }

    public RoleBetOnDragonBoatPo(Long roleId, Long stageTime, int dragonBoatId) {
        this.roleId = roleId;
        this.stageTime = stageTime;
        this.dragonBoatId = dragonBoatId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolebetondragonboat", " roleid=" + roleId + " and stagetime=" + stageTime);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolebetondragonboat", " roleid=" + roleId + " and stagetime=" + stageTime);
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getStageTime() {
        return stageTime;
    }

    public void setStageTime(Long stageTime) {
        this.stageTime = stageTime;
    }

    public int getDragonBoatId() {
        return dragonBoatId;
    }

    public void setDragonBoatId(int dragonBoatId) {
        this.dragonBoatId = dragonBoatId;
    }
}
