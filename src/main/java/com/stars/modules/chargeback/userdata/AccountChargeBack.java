package com.stars.modules.chargeback.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * 仅付费测有效
 * Created by huwenjun on 2017/3/20.
 */
public class AccountChargeBack extends DbRow {
    private String account;//账户名称
    private int vipexp;//vip经验
    private int yb;//累计元宝数量
    private int monthcard;//是否有月卡

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getVipexp() {
        return vipexp;
    }

    public void setVipexp(int vipexp) {
        this.vipexp = vipexp;
    }

    public int getYb() {
        return yb;
    }

    public void setYb(int yb) {
        this.yb = yb;
    }

    public int getMonthcard() {
        return monthcard;
    }

    public void setMonthcard(int monthcard) {
        this.monthcard = monthcard;
    }


    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "accountchargeback", "account='" + account + "'");
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("accountchargeback", "account='" + account + "'");
    }
}
