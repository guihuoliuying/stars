package com.stars.server.login2.task;

import java.util.concurrent.CountDownLatch;

//import com.stars.util.db.DbUtil;

/**
 * Created by zhaowenshuo on 2016/2/24.
 */
public class LLoadDataTask implements Runnable {

    private long dbId;
    private CountDownLatch latch;

    public LLoadDataTask(long dbId, CountDownLatch latch) {
        this.dbId = dbId;
        this.latch = latch;
    }

    @Override
    public void run() {
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//        try {
//            conn = DbUtil.getConn(dbId);
//            stmt = conn.createStatement();
//            stmt.setFetchSize(50000);
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis() - LoginServer2.config.getLong("expired", 24 * 3600) * 1000);
//            String sql = "select * from `account` where `login_timestamp` >= '" + timestamp.toString() + "'";
//            LogUtil.info("加载账号数据SQL: {}", sql);
//            rs = stmt.executeQuery(sql);
//            int count = 0;
//            Map<String, LAccount> resultMap = new HashMap<>(2048);
//            while (rs.next()) {
//                LAccount account = new LAccount();
//                account.setUniqueId(rs.getString("unique_id"));
//                account.setChannelId(rs.getInt("channel_id"));
//                account.setAccount(rs.getString("account"));
//                account.setLoginTimestamp(rs.getTimestamp("login_timestamp"));
//                account.setRegTimestamp(rs.getTimestamp("reg_timestamp"));
//                account.setState(DbObjectState.UNCHANGED);
//                resultMap.put(account.getUniqueId(), account);
//                if (++count % 1024 == 0) { // 每1024个存一次
//                    LAccountManager.putAll(resultMap);
//                    resultMap.clear();
//                }
//            }
//            if (resultMap.size() > 0) {
//                LAccountManager.putAll(resultMap);
//                resultMap.clear();
//            }
//        } catch (Exception e) {
//            // todo: 需要设置一个标志位
//        	LogUtil.error("加载账号数据异常", e);
//        } finally {
//            if (rs != null) try { rs.close(); } catch (Exception e) {}
//            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
//            if (conn != null) try { conn.close(); } catch (Exception e) {}
//            latch.countDown();
//        }
    }
}
