package com.stars.modules.tool.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huwenjun on 2017/6/23.
 */
public class RoleUCGiftRecord extends DbRow {
    private Long roleId;
    private String getDate = "";
    private String kaIds;
    private Set<Integer> kaIdSet = new HashSet<>();


    public RoleUCGiftRecord(Long roleId, String getDate) {
        this.roleId = roleId;
        this.getDate = getDate;
    }

    public RoleUCGiftRecord() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleucgiftrecord", " roleid=" + roleId+" and getdate='"+getDate+"'");
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleucgiftrecord", " roleid=" + roleId+" and getdate='"+getDate+"'");
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getGetDate() {
        return getDate;
    }

    public void setGetDate(String getDate) {
        this.getDate = getDate;
    }

    public String getKaIds() {
        return kaIds;
    }

    public void setKaIds(String kaIds) {
        this.kaIds = kaIds;
        try {
            this.kaIdSet = StringUtil.toHashSet(kaIds, Integer.class, '+');
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public void addKaId(Integer kaId) {
        kaIdSet.add(kaId);
        kaIds = StringUtil.makeString(kaIdSet, '+');
    }

    public boolean canReceiveKaId(Integer kaId) {
        return !kaIdSet.contains(kaId);
    }

    public Set<Integer> getKaIdSet() {
        return kaIdSet;
    }
}
