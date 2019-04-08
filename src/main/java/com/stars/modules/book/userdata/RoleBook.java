package com.stars.modules.book.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class RoleBook extends DbRow {
    private long roleId;
    private short kickTimes;
    private short beKickTimes;
    private int beLastKickTime;
    private String holeStr;
    private Set<Byte> holeSet = new HashSet<>();

    public RoleBook(){

    }

    public RoleBook(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return this.roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public short getKickTimes() {
        return this.kickTimes;
    }

    public void setKickTimes(short kickTimes) {
        this.kickTimes = kickTimes;
    }

    public short getBeKickTimes() {
        return beKickTimes;
    }

    public void setBeKickTimes(short beKickTimes) {
        this.beKickTimes = beKickTimes;
    }

    public int getBeLastKickTime() {
        return beLastKickTime;
    }

    public void setBeLastKickTime(int beLastKickTime) {
        this.beLastKickTime = beLastKickTime;
    }

    public String getHoleStr() {
        StringBuffer buffer = new StringBuffer();
        if (holeSet.size() > 0) {
            for (Byte pos:holeSet) {
                if (buffer.length() > 0) {
                    buffer.append("|");
                }
                buffer.append(""+ pos);
            }
        }
        return buffer.toString();
    }

    public void setHoleStr(String holeStr) {
        this.holeStr = holeStr;
        if (null != this.holeStr && !"".equals(this.holeStr)) {
            String str[] = holeStr.split("[|]");
            for (String s:str) {
                holeSet.add(Byte.valueOf(s));
            }
        }
    }

    public Set<Byte> getHoleSet() {
        return holeSet;
    }

    public void openHole(byte hole) {
        holeSet.add(hole);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolebook", " `roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return null;
    }
}
