package com.stars.services.accounttransfer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.stars.core.persist.DbRowDao;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.userdata.AccountTransfer;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.accounttransfer.po.AccountTransferCount;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class AccountTransferServiceActor extends ServiceActor implements AccountTransferService {
    private DbRowDao accountTraansferDao = new DbRowDao();
    public static LoadingCache<String, AccountTransfer> accountTransferCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build(new CacheLoader<String, AccountTransfer>() {
        @Override
        public AccountTransfer load(String account) throws Exception {
            AccountTransfer accountTransfer = DBUtil.queryBean(DBUtil.DB_USER, AccountTransfer.class, String.format("select * from accounttransfer where newaccount='%s';", account));
            if (accountTransfer == null) {
                accountTransfer = new AccountTransfer(account);
            } else {
                LogUtil.info("account transfer->login account {} transfer to {}", accountTransfer.getNewAccount(), accountTransfer.getOldAccount());
            }
            return accountTransfer;
        }
    });

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.AccountTransferService, this);
        SchedulerManager.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ServiceHelper.accountTransferService().cleanUp();
            }
        }, 10, 1, TimeUnit.MINUTES);
    }

    @Override
    public void printState() {

    }


    @Override
    public void transfer(String fromAccount, String toAccount, String reason, AccountTransferCount accountTransferCount) throws SQLException {
        LogUtil.info("account transfer OP: {} transfer to {} -> reason:{}", fromAccount, toAccount, reason);
        AccountTransfer accountTransfer = new AccountTransfer(toAccount, fromAccount, System.currentTimeMillis());
        accountTransferCache.put(toAccount, accountTransfer);
        accountTraansferDao.insert(accountTransfer);
        accountTraansferDao.flush(true, true);
        if (accountTransferCount == null) {
            accountTransferCount = new AccountTransferCount(toAccount, fromAccount);
            accountTransferCount.setInsertStatus();
        } else {
            accountTransferCount.setUpdateStatus();
        }
        accountTransferCount.setCount(accountTransferCount.getCount() + 1);
        DBUtil.execSql(DBUtil.DB_COMMON, accountTransferCount.getChangeSql());


    }

    @Override
    public void transferBack(AccountTransfer accountTransfer) throws SQLException {
        LogUtil.info("account transfer OP: {} transfer back to {}", accountTransfer.getNewAccount(), accountTransfer.getOldAccount());
        accountTransferCache.invalidate(accountTransfer.getNewAccount());
        accountTraansferDao.delete(accountTransfer);
        accountTraansferDao.flush(true, true);
        AccountTransferCount accountTransferCount = DBUtil.queryBean(DBUtil.DB_COMMON, AccountTransferCount.class, String.format("select * from accounttransfercount where newaccount='%s';", accountTransfer.getNewAccount()));
        if (accountTransferCount != null) {
            if (accountTransferCount.getCount() != 0) {
                int count = accountTransferCount.getCount() - 1;
                accountTransferCount.setCount(count);
                if (count == 0) {
                    DBUtil.execSql(DBUtil.DB_COMMON, accountTransferCount.getDeleteSql());
                } else {
                    accountTransferCount.setUpdateStatus();
                    DBUtil.execSql(DBUtil.DB_COMMON, accountTransferCount.getChangeSql());
                }
            }
        }
    }

    @Override
    public void cleanUp() {
        accountTransferCache.cleanUp();
    }
}