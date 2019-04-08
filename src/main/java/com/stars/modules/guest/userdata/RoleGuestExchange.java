package com.stars.modules.guest.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhouyaohui on 2017/1/11.
 */
public class RoleGuestExchange extends DbRow {

    private long roleId;
    private String name;    // 求助者名字
    private long familyId;  // 家族id
    private int guestId;    // 门客id
    private int level;      // 门客的等级
    private int stamp;      // 求助时间戳
    private int itemId;    // 求助门客碎片id
    private int askCount;   // 求助的数量
    private String askClaim;    // 求助语
    private int giveCount;  // 收到的数量
    private String giveStr; // 数据库字段，代码中不用，这里占位

    private Set<Long> giveSet = new HashSet<>();

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getStamp() {
        return stamp;
    }

    public void setStamp(int stamp) {
        this.stamp = stamp;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getAskCount() {
        return askCount;
    }

    public void setAskCount(int askCount) {
        this.askCount = askCount;
    }

    public String getAskClaim() {
        return askClaim;
    }

    public void setAskClaim(String askClaim) {
        this.askClaim = askClaim;
    }

    public int getGiveCount() {
        return giveCount;
    }

    public void setGiveCount(int giveCount) {
        this.giveCount = giveCount;
    }

    public String getGiveStr() {
        StringBuilder builder = new StringBuilder();
        for (long id : giveSet) {
            builder.append(id).append("&");
        }
        if (builder.length() != 0) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder.toString();
    }

    public void setGiveStr(String giveStr) {
        if (StringUtil.isEmpty(giveStr)) {
            return;
        }
        String[] ids = giveStr.split("[&]");
        for (String id : ids) {
            giveSet.add(Long.valueOf(id));
        }
    }

    public Set<Long> getGiveSet() {
        return giveSet;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleguestexchange", "roleid = " + roleId + " and stamp = " + stamp);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }
}
