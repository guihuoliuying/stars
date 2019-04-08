package com.stars.services.family.welfare.redpacket.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/9/6.
 */
public class FamilyRedPacketMemberPo extends DbRow {

    private long familyId; // 家族id
    private long roleId; // 玩家id
    private int ownedCount; // 拥有的数量
    private int givenCount; // 派发的数量
    private int seizedCount; // 抢了的数量

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familyredpacketmember", "`familyid`=" + familyId + " and `roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familyredpacketmember` where `familyid`=" + familyId + " and `roleid`=" + roleId;
    }

    /* Db Data Getter/Setter */
    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getGivenCount() {
        return givenCount;
    }

    public void setGivenCount(int givenCount) {
        this.givenCount = givenCount;
    }

    public int getOwnedCount() {
        return ownedCount;
    }

    public void setOwnedCount(int ownedCount) {
        this.ownedCount = ownedCount;
    }

    public int getSeizedCount() {
        return seizedCount;
    }

    public void setSeizedCount(int seizedCount) {
        this.seizedCount = seizedCount;
    }
}
