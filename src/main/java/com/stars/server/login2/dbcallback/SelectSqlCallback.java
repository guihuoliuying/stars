package com.stars.server.login2.dbcallback;

import com.stars.server.login2.asyncdb.AsyncDbCallback;
import com.stars.server.login2.asyncdb.AsyncDbResult;
import com.stars.server.login2.asyncdb.DbObjectState;
import com.stars.server.login2.model.pojo.LAccount;
import com.stars.server.login2.task.LGetDataTask;
import com.stars.util.ExecuteManager;
import com.stars.util.LogUtil;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/2/22.
 */
public class SelectSqlCallback extends AsyncDbCallback {

    private String uniqueId;
    private int retryTimes = 0;

    public SelectSqlCallback(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public void onCalled(AsyncDbResult result) {
        if (result.isTimeout()) {
        	com.stars.util.LogUtil.info("查询账号[{}]超时", uniqueId);
        } else if (result.isSuccess()) {
            LAccount account = null;
            Map<String, Object> map = result.getResultSet();
            if (map != null && map.size() > 0) {
                account = new LAccount();
                account.setUniqueId((String) map.get("unique_id"));
                account.setChannelId((Integer) map.get("channel_id"));
                account.setAccount((String) map.get("account"));
                account.setRegTimestamp((Timestamp) map.get("reg_timestamp"));
                account.setLoginTimestamp((Timestamp) map.get("login_timestamp"));
                account.setState(DbObjectState.UNCHANGED);
            }
            ExecuteManager.execute(new LGetDataTask(uniqueId, account, System.currentTimeMillis()));
        } else {
        	LogUtil.info("查询账号[" + uniqueId + "]失败", result.getCause());
        }
    }
}
