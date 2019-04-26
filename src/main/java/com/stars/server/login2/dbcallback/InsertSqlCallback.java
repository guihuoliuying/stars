package com.stars.server.login2.dbcallback;

import com.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import com.stars.server.login2.asyncdb.AsyncDbCallback;
import com.stars.server.login2.asyncdb.AsyncDbResult;
import com.stars.server.login2.model.manager.LAccountManager;
import com.stars.server.login2.model.pojo.LAccount;
import com.stars.util.LogUtil;

import static com.stars.server.login2.asyncdb.DbObjectState.*;

/**
 * Created by zhaowenshuo on 2016/2/22.
 */
public class InsertSqlCallback extends AsyncDbCallback {

    private String uniqueId;

    public InsertSqlCallback(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public void onCalled(AsyncDbResult result) {
        if (result.isTimeout()) {
        	com.stars.util.LogUtil.info("插入账号[{}]超时", uniqueId);
            // 修改状态
            LAccount account = LAccountManager.get(uniqueId);
            if (account != null) {
                synchronized (account) {
                    // 修改状态
                    switch (account.getState()) {
                        case NEW_SAVING: case NEW_UPDATED:
                            account.setState(NEW);
                            break;
                    }
                }
            }
        } else if (result.isSuccess()) {
            int updatedCount = result.getUpdatedCount();
            // 注意：updatedCount是否需要判断
            LAccount account = LAccountManager.get(uniqueId);
            if (account != null) {
                synchronized (account) {
                    // 修改状态
                    switch (account.getState()) {
                        case NEW_SAVING:
                            account.setState(UNCHANGED);
                            break;
                        case NEW_UPDATED:
                            account.setState(CHANGED);
                            break;
                    }
                }
            }
        } else {
        	LogUtil.info("插入账号[" + uniqueId + "]失败", result.getCause());
            LAccount account = LAccountManager.get(uniqueId);
            // 修改状态
            // 先处理主键重复的失败
            Throwable cause = result.getCause();
            if (cause != null && cause instanceof MySQLTimeoutException) {
                if (account != null) {
                    synchronized (account) {
                        // 修改状态
                        switch (account.getState()) {
                            case NEW_SAVING: case NEW_UPDATED:
                                account.setState(CHANGED);
                                break;
                        }
                    }
                }
            } else {
                if (account != null) {
                    synchronized (account) {
                        // 修改状态
                        switch (account.getState()) {
                            case NEW_SAVING: case NEW_UPDATED:
                                account.setState(NEW);
                                break;
                        }
                    }
                }
            }
        }
//        AsyncDbTest.latch.countDown();
    }

}
