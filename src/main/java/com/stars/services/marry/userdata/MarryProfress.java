package com.stars.services.marry.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * 表白数据
 * Created by zhouyaohui on 2016/12/2.
 */
public class MarryProfress extends DbRow {

    private String uniquekey;   // key roleid+roleid+timestamp
    private long target;        // 被表白者
    private long profressor;    // 表白者
    private byte state;     // 状态

    public String getUniquekey() {
        return uniquekey;
    }

    public void setUniquekey(String uniquekey) {
        this.uniquekey = uniquekey;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    public long getProfressor() {
        return profressor;
    }

    public void setProfressor(long profressor) {
        this.profressor = profressor;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "marryprofress", "uniquekey = '" + uniquekey + "'");
    }

    @Override
    public String getDeleteSql() {
        return "delete from `marryprofress` where `uniquekey`= '" + uniquekey + "'";
    }
}
