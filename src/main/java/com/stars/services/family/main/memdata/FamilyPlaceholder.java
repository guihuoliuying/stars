package com.stars.services.family.main.memdata;

import com.stars.core.db.DbRow;
import com.stars.services.family.main.userdata.FamilyApplicationPo;

/**
 * Created by zhaowenshuo on 2016/8/26.
 */
public class FamilyPlaceholder extends DbRow implements Cloneable {

    public static final byte STATE_CONFIRMING = 0; // 确认中
    public static final byte STATE_ONGOING = 1; // 进行中

    private long roleId;
    private int timestamp;
    private byte state;
    private FamilyApplicationPo applicationPo;

    public FamilyPlaceholder() {
    }

    public FamilyPlaceholder(long roleId, FamilyApplicationPo applicationPo) {
        this.roleId = roleId;
        this.applicationPo = applicationPo;
    }

    @Override
    public String getChangeSql() {
        return null;
    }

    @Override
    public String getDeleteSql() {
        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FamilyPlaceholder other = (FamilyPlaceholder) super.clone();
        other.applicationPo = (FamilyApplicationPo) applicationPo.clone();
        return other;
    }

    /* Db Mem Getter/Setter */
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public FamilyApplicationPo getApplicationPo() {
        return applicationPo;
    }

    public void setApplicationPo(FamilyApplicationPo applicationPo) {
        this.applicationPo = applicationPo;
    }
}
