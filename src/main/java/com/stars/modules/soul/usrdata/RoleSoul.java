package com.stars.modules.soul.usrdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.soul.SoulManager;
import com.stars.modules.soul.prodata.SoulLevel;
import com.stars.modules.soul.prodata.SoulStage;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class RoleSoul extends DbRow {
    private long roleId;
    private int stage = 1;//阶段
    private int type = 1;//部位
    private int level = 0;//级别

    public RoleSoul(long roleId) {
        this.roleId = roleId;
    }

    public RoleSoul() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolesoul", " roleid= " + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolesoul", " roleid= " + roleId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public SoulLevel getSoulLevel() {
        SoulLevel soulLevel = SoulManager.soulTypeMap.get(type).get(level);
        return soulLevel;
    }

    public SoulStage getSoulStage() {
        return SoulManager.soulStageMap.get(stage);
    }
}
