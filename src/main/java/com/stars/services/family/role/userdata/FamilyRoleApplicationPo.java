package com.stars.services.family.role.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class FamilyRoleApplicationPo extends DbRow {

    private long roleId; // 玩家id
    private long familyId; // 家族id

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familyroleapplication", "`roleid`=" + roleId + " and `familyid`=" + familyId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familyroleapplication` where `roleid`=" + roleId + " and `familyid`=" + familyId;
    }

    /* Db Data Getter/Setter */
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }
}
