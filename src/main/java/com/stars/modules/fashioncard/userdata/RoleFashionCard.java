package com.stars.modules.fashioncard.userdata;

import com.stars.core.attr.Attribute;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class RoleFashionCard extends DbRow {
    private long roleId;
    private String fashionState;// id+state|id+state
    private long lastAnnounceTimestamp;//上一次全服跑马灯时间
    private String putOned;//曾经穿过，恶心的红点

    private Attribute roleAttribute = new Attribute();
    private int power;

    private Map<Integer, Integer> fashionStateMap = new HashMap<>();
    private Set<Integer> putOnedSet = new HashSet<>();

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getFashionState() {
        return fashionState;
    }

    public void setFashionState(String fashionState) {
        this.fashionState = fashionState;
        if (fashionState == null || fashionState.equals("")) return;
        this.fashionStateMap = StringUtil.toMap(fashionState, Integer.class, Integer.class, '+', '|');
    }

    public long getLastAnnounceTimestamp() {
        return lastAnnounceTimestamp;
    }

    public void setLastAnnounceTimestamp(long lastAnnounceTimestamp) {
        this.lastAnnounceTimestamp = lastAnnounceTimestamp;
    }

    public Map<Integer, Integer> getFashionStateMap() {
        return fashionStateMap;
    }

    public void changeFashionState(int fashionCardId, int state) {
        this.fashionStateMap.put(fashionCardId, state);
        this.fashionState = StringUtil.makeString(this.fashionStateMap, '+', '|');
    }

    public Attribute getRoleAttribute() {
        return roleAttribute;
    }

    public void setRoleAttribute(Attribute roleAttribute) {
        this.roleAttribute = roleAttribute;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getPutOned() {
        return putOned;
    }

    public void addPutOn(int fashionCardId) {
        this.putOnedSet.add(fashionCardId);
        this.putOned = StringUtil.makeString(putOnedSet, '+');
    }

    public void setPutOned(String putOned) {
        this.putOned = putOned;
        if (fashionState == null || fashionState.equals("")) return;
        try {
            this.putOnedSet = StringUtil.toHashSet(putOned, Integer.class, '+');
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Integer> getPutOnedSet() {
        return putOnedSet;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolefashioncard", " roleid = " + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from rolefashioncard where roleid = " + this.roleId;
    }
}
