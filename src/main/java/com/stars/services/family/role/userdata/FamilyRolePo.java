package com.stars.services.family.role.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public class FamilyRolePo extends DbRow {

    private long roleId; // 玩家id
    private long familyId; // 家族id
    private int contribution; // 贡献值

    public FamilyRolePo() {
    }

    public FamilyRolePo(long roleId, long familyId, int contribution) {
        this.roleId = roleId;
        this.familyId = familyId;
        this.contribution = contribution;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familyrole", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familyrole` where `roleid`=" + roleId;
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

    public int getContribution() {
        return contribution;
    }

    public void setContribution(int contribution) {
        this.contribution = contribution;
    }

}
