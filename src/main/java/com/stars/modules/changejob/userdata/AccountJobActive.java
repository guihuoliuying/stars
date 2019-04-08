package com.stars.modules.changejob.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by huwenjun on 2017/5/27.
 */
public class AccountJobActive extends DbRow {
    private String account;
    private Integer jobId;

    public AccountJobActive() {
    }

    public AccountJobActive(String account, Integer jobId) {
        this.account = account;
        this.jobId = jobId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "accountjobactive", String.format(" account='%s' ", account));
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("accountjobactive", String.format(" account='%s' ", account));
    }
}
