package com.stars.services.accounttransfer.po;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by huwenjun on 2017/10/19.
 */
public class AccountTransferCount extends DbRow {
    private String newAccount;
    private String oldAccount;
    private int count;


    public AccountTransferCount() {
    }

    public AccountTransferCount(String newAccount, String oldAccount) {
        this.newAccount = newAccount;
        this.oldAccount = oldAccount;
    }

    public String getNewAccount() {
        return newAccount;
    }

    public void setNewAccount(String newAccount) {
        this.newAccount = newAccount;
    }

    public String getOldAccount() {
        return oldAccount;
    }

    public void setOldAccount(String oldAccount) {
        this.oldAccount = oldAccount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "accounttransfercount", String.format(" newaccount='%s' and oldaccount='%s';", newAccount, oldAccount));
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("accounttransfercount", String.format(" newaccount='%s' and oldaccount='%s';", newAccount, oldAccount));
    }
}
