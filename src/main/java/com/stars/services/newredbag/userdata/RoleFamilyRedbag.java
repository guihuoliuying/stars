package com.stars.services.newredbag.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class RoleFamilyRedbag extends DbRow {

    private long familyId;  // 家族id
    private long roleId;    // 角色id
    private String name;    // 角色名字
    private int jobId;      // 职业
    private int redbagId;   // 红包id
    private int count;      // 数量


    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getRedbagId() {
        return redbagId;
    }

    public void setRedbagId(int redbagId) {
        this.redbagId = redbagId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    @Override
    public String getChangeSql() {
        StringBuilder builder = new StringBuilder();
        builder.append("familyid = ").append(familyId).append(" and ")
                .append("roleid = ").append(roleId).append(" and ")
                .append("redbagid = ").append(redbagId);
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolefamilyredbag", builder.toString());
    }

    @Override
    public String getDeleteSql() {
        return null;
    }
}
