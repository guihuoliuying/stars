package com.stars.server.login2.task;

import com.stars.server.login2.LoginServer2;
import com.stars.server.login2.asyncdb.AsyncDbManager;
import com.stars.server.login2.asyncdb.DbObjectState;
import com.stars.server.login2.dbcallback.SelectSqlCallback;
import com.stars.server.login2.helper.LHashHelper;
import com.stars.server.login2.helper.LTokenHelper;
import com.stars.server.login2.helper.LoginNetwork;
import com.stars.server.login2.model.manager.LAccountManager;
import com.stars.server.login2.model.manager.LZoneManager;
import com.stars.server.login2.model.pojo.LAccount;
import com.stars.server.login2.model.pojo.LZone;
import com.stars.server.login2.model.pojo.LZoneServer;
import com.stars.server.login2.sdk.core.LSdkVerifyResult;
import com.stars.server.login2.sdk.core.LVerifyContext;
import com.stars.util.LogUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class LVerifyCallbackTask extends LoginTask {

    private LVerifyContext context;
    private LSdkVerifyResult result;

    public LVerifyCallbackTask(LVerifyContext context, LSdkVerifyResult result) {
        this.context = context;
        this.result = result;
    }

    @Override
    public void run0() {
        if (LoginServer2.callbackMap.remove(context) != null) {
            if (result.isTimeout()) {
                com.stars.server.login2.helper.LoginNetwork.fail(context.nettyChannel(), "login_err_timeout");
                com.stars.util.LogUtil.info("账号[{}:{}]登录失败[系统超时]", context.channel().name(), result.getUserID());

            } else if (result.isSuccess()) {
                // 生成全局唯一账号
                String uniqueId = context.channel().name() + "_" + result.getUserID();
                // 生成时间戳
                long timestamp = System.currentTimeMillis();

                // 生成登录令牌 todo: 缺其他信息（渠道号/特权/VIP）
                String token = LTokenHelper.makeToken(uniqueId, timestamp);
                // send it back and close channel
                com.stars.server.login2.helper.LoginNetwork.succeed(context.nettyChannel(), token, getConnectorList(uniqueId));
                com.stars.util.LogUtil.info("账号[{}:{}]登录成功, token={}", context.channel().name(), result.getUserID(), token);

                /* 更新数据 */
                LAccount account = LAccountManager.get(uniqueId);
                if (account != null) {
                    //
                    synchronized (account) {
                        account.setLoginTimestamp(new Timestamp(timestamp)); // 设置更新时间
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
                    }
                } else {
                    // 查找数据
                    String sql = "select * from account where unique_id='" + uniqueId + "'";
                    try {
                        AsyncDbManager.exec(LHashHelper.getDbId(uniqueId), sql, new SelectSqlCallback(uniqueId));
                    } catch (RejectedExecutionException e) {
                    	com.stars.util.LogUtil.error("DB线程繁忙, 丢弃请求: " + sql, e);
                    }
                }

            } else {
                String message = "login_err_busying";
                String causeString = "unknown";
                if (result.getCause() != null) {
                    Object cause = result.getCause();
                    if (cause instanceof Throwable) {
                        causeString = cause.getClass().getSimpleName() + ": " + ((Throwable) cause).getMessage();
                    } else if (cause instanceof String) {
                        causeString = (String) cause;
                        message = causeString;
                    }
                }
                LoginNetwork.fail(context.nettyChannel(), message);
                LogUtil.info("账号[{}:{}]登录失败[验证失败], cause={}", context.channel().name(), result.getUserID(), causeString);
            }
            // 关闭连接
            if (context.nettyChannel().isActive()) {
                context.nettyChannel().close();
            }
        }
    }

    private List<com.stars.server.login2.model.pojo.LZoneServer> getConnectorList(String uniqueId) {
        int hashCode = uniqueId.hashCode();
        List<com.stars.server.login2.model.pojo.LZoneServer> list = new ArrayList<>();
        for (com.stars.server.login2.model.pojo.LZone zone : com.stars.server.login2.model.manager.LZoneManager.zones()) {
            list.add(getConnector(zone.getId(), hashCode));
        }
        return list;
    }

    private LZoneServer getConnector(int zoneId, int hashCode) {
        LZone zone = LZoneManager.get(zoneId);
        int index = hashCode %  zone.getServerList().size();
        return zone.getServerList().get(index);
    }
}
