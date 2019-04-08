package com.stars.modules.cg.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.cg.CgManager;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class RoleCg extends DbRow {
    private long roleId;// '角色Id'
    private String cgId;// '引导id'
    private byte finished;// '是否执行表现'

    public RoleCg() {
    }

    public RoleCg(long roleId, String cgId) {
        this.roleId = roleId;
        this.cgId = cgId;
        this.finished = CgManager.CG_STATE_NOT_FINISH;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(cgId);
        buff.writeByte(finished);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolecg", " `roleid`=" + roleId + " and `cgid`=" + cgId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleinduct", " `roleid`=" + roleId + " and `cgid`=" + cgId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getCgId() {
        return cgId;
    }

    public void setCgId(String cgId) {
        this.cgId = cgId;
    }

    public byte getFinished() {
        return finished;
    }

    public void setFinished(byte finished) {
        this.finished = finished;
    }
}
