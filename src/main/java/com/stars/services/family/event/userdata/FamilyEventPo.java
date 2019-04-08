package com.stars.services.family.event.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class FamilyEventPo extends DbRow {

    private long familyId; // 家族id
    private int event; // 事件
    private String params; // 事件参数
    private int timestamp; // 事件戳

    private String[] paramArray; // 事件参数数组

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familyevent", "");
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familyevent` where `familyid`=" + familyId + " and `event`=" + event + " and `timestamp`=" + timestamp;
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(event);
        buff.writeByte((byte) paramArray.length);
        for (String param : paramArray) {
            buff.writeString(param);
        }
        buff.writeInt(timestamp);
    }

    /* Mem Data Getter/Setter */
    public String[] getParamArray() {
        return paramArray;
    }

    public void setParamArray(String[] paramArray) {
        this.paramArray = paramArray;
    }

    /* Db Data Getter/Setter */
    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public String getParams() {
        this.params = StringUtil.makeString(paramArray, '|');
        return this.params;
    }

    public void setParams(String params) {
        this.params = params;
        this.paramArray = params.split("\\|");
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

}
