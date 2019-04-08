package com.stars.server.login2.dbcallback;

import com.stars.server.login2.asyncdb.AsyncDbCallback;
import com.stars.server.login2.asyncdb.AsyncDbResult;
import com.stars.server.login2.model.manager.LAccountManager;
import com.stars.server.login2.model.pojo.LAccount;
import com.stars.util.LogUtil;

import static com.stars.server.login2.asyncdb.DbObjectState.CHANGED;
import static com.stars.server.login2.asyncdb.DbObjectState.UNCHANGED;

/**
 * Created by zhaowenshuo on 2016/2/22.
 */
public class UpdateSqlCallback extends AsyncDbCallback {

    private String uniqueId;

    public UpdateSqlCallback(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public void onCalled(AsyncDbResult result) {
        if (result.isTimeout()) {
        	com.stars.util.LogUtil.info("更新账号[{}]超时", uniqueId);
            // todo: 修改状态
            LAccount account = LAccountManager.get(uniqueId);
            if (account != null) {
                synchronized (account) {
                    // todo: 修改状态
                    switch (account.getState()) {
                        case SAVING: case UPDATED:
                            account.setState(CHANGED);
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
                    // todo: 修改状态
                    switch (account.getState()) {
                        case SAVING:
                            account.setState(UNCHANGED);
                            break;
                        case UPDATED:
                            account.setState(CHANGED);
                            break;
                    }
                }
            }
        } else {
        	LogUtil.info("更新账号[" + uniqueId + "]失败", result.getCause());
            LAccount account = LAccountManager.get(uniqueId);
            if (account != null) {
                synchronized (account) {
                    // todo: 修改状态
                    switch (account.getState()) {
                        case SAVING:
                        case UPDATED:
                            account.setState(CHANGED);
                            break;
                    }
                }
            }
        }
    }

}
