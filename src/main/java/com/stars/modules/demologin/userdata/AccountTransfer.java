package com.stars.modules.demologin.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

/**
 * Created by huwenjun on 2017/10/19.
 */
public class AccountTransfer extends DbRow {
    private String newAccount;
    private String oldAccount;
    private long opDate;

    public AccountTransfer(String newAccount) {
        this.newAccount = newAccount;
    }

    public AccountTransfer() {
    }

    public AccountTransfer(String newAccount, String oldAccount, long opDate) {
        this.newAccount = newAccount;
        this.oldAccount = oldAccount;
        this.opDate = opDate;
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

    public long getOpDate() {
        return opDate;
    }

    public void setOpDate(long opDate) {
        this.opDate = opDate;
    }

    /**
     * 无效的转移数据，为了避免再次查库
     *
     * @return
     */
    public boolean isInvalid() {
        return StringUtil.isEmpty(oldAccount);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "accounttransfer", String.format(" newaccount='%s'", newAccount));
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("accounttransfer", String.format(" newaccount='%s' and oldaccount='%s';", newAccount, oldAccount));
    }
}
