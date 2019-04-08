package com.stars.modules.foreshow.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2016/10/28.
 */
public class ForeShowStatePo extends DbRow {
    private long roleid;
    private String openname;
    private int openstate;//1：开放已表现，2：开放未表现，3：未开放

    public ForeShowStatePo() {
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(openname);
    }

    public long getRoleid() {
        return roleid;
    }

    public void setRoleid(long roleid) {
        this.roleid = roleid;
    }

    public String getOpenname() {
        return openname;
    }

    public void setOpenname(String openname) {
        this.openname = openname;
    }

    public int getOpenstate() {
        return openstate;
    }

    public void setOpenstate(int openstate) {
        this.openstate = openstate;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "foreshowstate", "`roleid`=" + roleid + " and `openname`='" + openname + "'");
    }

    @Override
    public String getDeleteSql() {
        return "delete from `foreshowstate` where `roleid`=" + roleid + " and `openname`='" + openname + "'";
    }
}
