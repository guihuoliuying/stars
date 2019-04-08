package com.stars.core.recordmap;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/7/26.
 */
public class RoleRecord extends DbRow {

    public static final byte TYPE_NIL = -1;
    public static final byte TYPE_STR = 0;
    public static final byte TYPE_MAP = 1;
    public static final byte TYPE_LIST = 2;

    private byte type;
    private long roleId;
    private String recordKey;
    private String recordVal;
    private Map<String, String> valMap;
    private List<String> valList;

    public RoleRecord() {
        type = TYPE_NIL;
    }

    public RoleRecord(byte type, long roleId, String recordKey, String recordVal) {
        this.type = type;
        this.roleId = roleId;
        this.recordKey = recordKey;
        if (type == TYPE_STR) {
            this.recordVal = recordVal;
        } else if (type == TYPE_MAP) { // 哈希表
            if (recordVal != null && !recordVal.trim().equals("")) {
                try {
                    valMap = StringUtil.toMap(recordVal, String.class, String.class, '=', ',');
                } catch (Exception e) {
                    throw new IllegalArgumentException("malformed format of recordVal: " + recordVal);
                }
            } else {
                valMap = new HashMap<>();
            }
        } else if (type == TYPE_LIST) { // 线性表
            if (recordVal != null && !recordVal.trim().equals("")) {
                try {
                    valList = StringUtil.toArrayList(recordVal, String.class, ',');
                } catch (Exception e) {
                    throw new IllegalArgumentException("malformed format of recordVal: " + recordVal);
                }
            } else {
                valList = new ArrayList<>();
            }
        } else {
            throw new IllegalArgumentException("unsupported type: " + type);
        }
    }

    public byte getType() {
        return type;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }

    public String getRecordVal() {
        return recordVal;
    }

    public void setRecordVal(String recordVal) {
        try {
            if (type == TYPE_NIL) {
                if (recordVal.trim().startsWith("{")) {
                    type = TYPE_MAP;
                    valMap = StringUtil.toMap(recordVal, String.class, String.class, '=', ',');
                } else if (recordVal.trim().startsWith("[")) {
                    type = TYPE_LIST;
                    valList = StringUtil.toArrayList(recordVal, String.class, ',');
                } else {
                    type = TYPE_STR;
                    this.recordVal = recordVal;
                }
            } else if (type == TYPE_STR) {
                this.recordVal = recordVal;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getValMap() {
        return valMap;
    }

    public List<String> getValList() {
        return valList;
    }

    public String getValFromMap(String field) {
        if (type == TYPE_MAP) {
            return valMap.get(field);
        }
        throw new IllegalStateException("incorrect type: " + type);
    }

    public void setValToMap(String field, String value) {
        if (type == TYPE_MAP) {
            valMap.put(field, value);
        }
    }

    public void clearValueMap() {
        if (type == TYPE_MAP) {
            valMap.clear();
        } else {
            throw new IllegalStateException("incorrect type: " + type);
        }
    }

    @Override
    public String getChangeSql() {
        try {
            if (type == TYPE_MAP) {
                recordVal = StringUtil.makeString2(valMap, '=', ',');
            } else if (type == TYPE_LIST) {
                recordVal = StringUtil.makeString2(valList, ',');
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolerecords",
                "`roleid`='" + roleId + "' and `recordkey`='" + recordKey + "'");
    }

    @Override
    public String getDeleteSql() {
        return "delete from `rolerecords` where `roleid`='" + roleId + "' and `recordkey`='" + recordKey + "'";
    }

    @Override
    public String toString() {
        return '\'' + recordVal + '\'';
    }
}
