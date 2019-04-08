package com.stars.server.login2;

import com.stars.server.login2.asyncdb.AsyncDbManager;
import com.stars.server.login2.asyncdb.DbObjectState;
import com.stars.server.login2.model.manager.LAccountManager;
import com.stars.server.login2.model.pojo.LAccount;
import com.stars.util.LogUtil;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhaowenshuo on 2016/2/22.
 */
public class AsyncDbTest {

    public static CountDownLatch latch = null;

    public static void main(String[] args) throws Exception {
    	LogUtil.init();
        AsyncDbManager.init();
        LoginServer2.callbackMap.size();
//        AsyncDbManager.exec(99, "select * from account where unique_id = '001_00001'", new AsyncDbCallback() {
//            @Override
//            public void onCalled(AsyncDbResult result) {
//                System.out.println("Testing...");
//                System.out.println(result.getResultSet());
//            }
//        });

//        AsyncDbManager.exec(99, "insert into account values ('1', 1, '1_1', '2011-11-11 11:11:11', '2011-11-11 11:11:11')", new AsyncDbCallback() {
//            @Override
//            public void onCalled(AsyncDbResult result) {
//                System.out.println("Testing...");
//                System.out.println(result.getResultSet());
//            }
//        });
//
//        AsyncDbManager.exec(99, "insert into account values ('1', 1, '1_1', '2011-11-11 11:11:11', '2011-11-11 11:11:11')", new AsyncDbCallback() {
//            @Override
//            public void onCalled(AsyncDbResult result) {
//                System.out.println("Testing...");
//                System.out.println(result.getResultSet());
//            }
//        });

//        LAccount account = new LAccount();
//        account.setUniqueId("A");
//        account.setChannelId(1);
//        account.setAccount("AA");
//        account.setRegTimestamp(new Timestamp(System.currentTimeMillis()));
//        account.setLoginTimestamp(new Timestamp(System.currentTimeMillis()));
//        account.setState(DbObjectState.NEW);
//
//        LAccountManager.putIfAbsent(account.getUniqueId(), account);

        long s = System.currentTimeMillis();
        latch = new CountDownLatch(1000);
        for (int i = 0; i < 1000; i++) {
            LAccount account = new LAccount();
            account.setUniqueId(Integer.toString(i));
            account.setChannelId(1);
            account.setAccount(Integer.toString(i));
            account.setRegTimestamp(new Timestamp(System.currentTimeMillis()));
            account.setLoginTimestamp(new Timestamp(System.currentTimeMillis()));
            account.setState(DbObjectState.NEW);

            LAccountManager.putIfAbsent(account.getUniqueId(), account);
        }
        latch.await();
        long e = System.currentTimeMillis();
        System.out.println(e - s);
    }

}
