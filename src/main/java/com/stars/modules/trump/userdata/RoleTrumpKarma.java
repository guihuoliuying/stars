package com.stars.modules.trump.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.trump.TrumpManager;
import com.stars.modules.trump.prodata.TrumpKarmaVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/10/18.
 */
public class RoleTrumpKarma extends DbRow implements Comparable<RoleTrumpKarma> {
    private long roleId;
    private int karmaId;
    private int status;//激活状态：0表示未激活，1表示已激活2，表示可激活

    public RoleTrumpKarma(long roleId, int karmaId) {
        this.roleId = roleId;
        this.karmaId = karmaId;
        setStatus(0);
    }

    public RoleTrumpKarma() {
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getKarmaId() {
        return karmaId;
    }

    public void setKarmaId(int karmaId) {
        this.karmaId = karmaId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public TrumpKarmaVo getTrumpKarma() {
        return TrumpManager.trumpKarmaMap.get(karmaId);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roletrumpkarma", String.format(" roleid=%s and karmaid=%s", roleId, karmaId));
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roletrumpkarma", String.format(" roleid=%s and karmaid=%s", roleId, karmaId));
    }

    public void active() {
        setStatus(1);
    }

    @Override
    public int compareTo(RoleTrumpKarma o) {
        if (o.getStatus() != getStatus()) {
            return o.getStatus() - getStatus();
        }

        return o.getTrumpKarma().getOrder()-getTrumpKarma().getOrder() ;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(karmaId);
        buff.writeInt(status);//激活状态：0表示未激活，1表示已激活2，表示可激活
    }
}
