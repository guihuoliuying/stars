package com.stars.modules.luckydraw.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class RoleLuckyDrawPo extends DbRow {
    private long roleId;
    private int awardId;
    private int hitTime;
    private int notHitTime;
    private int type;//对应活动类型

    public RoleLuckyDrawPo(long roleId, int awardId, int type) {
        this.roleId = roleId;
        this.awardId = awardId;
        this.type = type;
    }

    public RoleLuckyDrawPo() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleluckydraw", "roleid =" + roleId + " and awardid=" + awardId+" and type="+type);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleluckydraw", "roleid =" + roleId + " and awardid=" + awardId+" and type="+type);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getHitTime() {
        return hitTime;
    }

    public void setHitTime(int hitTime) {
        this.hitTime = hitTime;
    }

    public int getNotHitTime() {
        return notHitTime;
    }

    public void setNotHitTime(int notHitTime) {
        this.notHitTime = notHitTime;
    }

    public void reset() {
        notHitTime = 0;
    }

    public void addNotHit() {
        notHitTime++;
    }

    public void addHitTime() {
        hitTime++;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
