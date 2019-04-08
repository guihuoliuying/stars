package com.stars.services.friend.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/10.
 */
public class ContactsPo extends DbRow {

    private long roleId;
    private long contactsId;
    private String contactsName;
    private int lastContactsTimestamp;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getContactsId() {
        return contactsId;
    }

    public void setContactsId(long contactsId) {
        this.contactsId = contactsId;
    }

    public String getContactsName() {
        return contactsName;
    }

    public void setContactsName(String contactsName) {
        this.contactsName = contactsName;
    }

    public int getLastContactsTimestamp() {
        return lastContactsTimestamp;
    }

    public void setLastContactsTimestamp(int lastContactsTimestamp) {
        this.lastContactsTimestamp = lastContactsTimestamp;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "friendcontacts", "`roleid`=" + roleId + " and `contactsid`=" + contactsId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `friendcontacts` where `roleid`=" + roleId + " and `contactsid`=" + contactsId;
    }
}
