package com.stars.server.login2.task;

import com.stars.server.login2.asyncdb.DbObjectState;
import com.stars.server.login2.model.manager.LAccountManager;
import com.stars.server.login2.model.pojo.LAccount;

import java.sql.Timestamp;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class LGetDataTask extends LoginTask {

    private String uniqueId;
    private com.stars.server.login2.model.pojo.LAccount account;
    private long loginTimestamp;

    public LGetDataTask(String uniqueId, com.stars.server.login2.model.pojo.LAccount account, long loginTimestamp) {
        this.uniqueId = uniqueId;
        this.account = account;
        this.loginTimestamp = loginTimestamp;
    }

    @Override
    public void run0() {
        if (account == null) {
            account = new LAccount();
            account.setUniqueId(uniqueId);
            account.setRegTimestamp(new Timestamp(loginTimestamp));
            account.setState(DbObjectState.NEW);
        }
        if (com.stars.server.login2.model.manager.LAccountManager.putIfAbsent(uniqueId, account) != null) {
            account = com.stars.server.login2.model.manager.LAccountManager.get(uniqueId);
        }
        synchronized (account) {
            account.setLoginTimestamp(new Timestamp(loginTimestamp));
            /* 状态更新 */
            switch (account.getState()) {
                case UNCHANGED: account.setState(DbObjectState.CHANGED); break;
                case CHANGED: break;
                case SAVING: account.setState(DbObjectState.UPDATED); break;
                case UPDATED: break;
                case NEW: break;
                case NEW_SAVING: account.setState(DbObjectState.NEW_UPDATED); break;
                case NEW_UPDATED: break;
            }
            LAccountManager.putIfAbsent(uniqueId, account); // 避免定时清理线程干掉
        }
    }
}
