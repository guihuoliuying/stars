package com.stars.coreManager;

import com.stars.AccountRow;
import com.stars.startup.MainStartup;
import com.stars.util.LogUtil;

import java.util.LinkedList;
import java.util.List;
//自动清理账号任务
public class ClearAccountTask implements Runnable {
    @Override
    public void run() {
        LogUtil.info("定时触发自动清理账号任务进行...");

        try {
            List<String> removalAccountList = new LinkedList<>();
            long now = System.currentTimeMillis();
            for (AccountRow accountRow : MainStartup.accountMap.values()) {
                if (accountRow.getLoginLock().tryLock()) {
                    long currentRoleId = accountRow.getCurrentRoleId();
                    long lastLoginTime = accountRow.getLastLoginTime();
                    try {
                        long elapsed = now - lastLoginTime;
                        if (currentRoleId <= 0 && elapsed > 12 * 3600 * 1000) {
                            MainStartup.accountMap.remove(accountRow.getName());
                            removalAccountList.add(accountRow.getName());
                            LogUtil.info("账号清理|account:{}|currentRoleId:{}|elapsed:{}",
                                    accountRow.getName(), currentRoleId, elapsed);
                        }
                    } catch (Throwable t) {
                        LogUtil.error("账号清理|异常|account:" + accountRow.getName() + "|roleId:" + currentRoleId, t);
                    } finally {
                        accountRow.getLoginLock().unlock();
                    }
                }
            }
            LogUtil.info("账号清理|完成|removalList:{}", removalAccountList);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

    }
}