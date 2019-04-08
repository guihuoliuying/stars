package com.stars.modules.luckycard.usrdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class RoleLuckyCard extends DbRow {
    private long roleId;
    private int cardId;
    private int notHit;

    public RoleLuckyCard(long roleId, int cardId) {
        this.roleId = roleId;
        this.cardId = cardId;
    }

    public RoleLuckyCard() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleluckycard", String.format(" roleid=%d and cardid=%d ", roleId, cardId));
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleluckycard", String.format(" roleid=%d and cardid=%d ", roleId, cardId));
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }


    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getNotHit() {
        return notHit;
    }

    public void setNotHit(int notHit) {
        this.notHit = notHit;
    }

    public void addNotHit() {
        notHit++;
    }
}
