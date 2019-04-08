package com.stars.multiserver.familywar.rank;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by chenkeyu on 2017-07-04.
 */
public class DissolveFamily extends DbRow {
    private long familyId;

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "dissolvefamily", " `familyid`=" + this.familyId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from dissolvefamily where `familyid`=" + this.familyId;
    }
}
