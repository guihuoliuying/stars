package com.stars.modules.changejob.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by huwenjun on 2017/5/27.
 */
public class RoleChangeJob extends DbRow {
    private Long roleId;//角色id
    private Integer jobId;//职业id
    private Long changeTime;//转职时间

    public RoleChangeJob(Long roleId, Integer jobId, Long changeTime) {
        this.roleId = roleId;
        this.jobId = jobId;
        this.changeTime = changeTime;
    }

    public RoleChangeJob() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolechangejob", String.format("roleid=%d", roleId));
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolechangejob", String.format("roleid=%d", roleId));
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Long getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(Long changeTime) {
        this.changeTime = changeTime;
    }
}
