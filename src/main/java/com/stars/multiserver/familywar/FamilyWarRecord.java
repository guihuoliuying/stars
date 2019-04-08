package com.stars.multiserver.familywar;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

public class FamilyWarRecord extends DbRow {

    private String key;
    private String value;

    public FamilyWarRecord() {
        // TODO Auto-generated constructor stub
    }

    public FamilyWarRecord(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familywarrecords",
                "`key`='" + key + "' and `value`='" + value + "'");
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familywarrecords`";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "FamilyWarRecord:[key=" + key + ",value=" + value + "]";
    }

}
