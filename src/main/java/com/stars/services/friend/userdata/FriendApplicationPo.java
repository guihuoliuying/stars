package com.stars.services.friend.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/10.
 */
public class FriendApplicationPo extends DbRow {

    private long applicantId;
    private long objectId;
    private String applicantName;
    private int applicantJobId;
    private int applicantLevel;
    private int appliedTimestamp;

    public long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(long applicantId) {
        this.applicantId = applicantId;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public int getApplicantJobId() {
        return applicantJobId;
    }

    public void setApplicantJobId(int applicantJobId) {
        this.applicantJobId = applicantJobId;
    }

    public int getApplicantLevel() {
        return applicantLevel;
    }

    public void setApplicantLevel(int applicantLevel) {
        this.applicantLevel = applicantLevel;
    }

    public int getAppliedTimestamp() {
        return appliedTimestamp;
    }

    public void setAppliedTimestamp(int appliedTimestamp) {
        this.appliedTimestamp = appliedTimestamp;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "friendapplication", "`objectid`=" + objectId + " and `applicantid`=" + applicantId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `friendapplication` where `objectid`=" + objectId + " and `applicantid`=" + applicantId;
    }
}
