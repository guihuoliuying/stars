package com.stars.modules.base.condition.dataset.activedjob;

import com.stars.AccountRow;
import com.stars.core.expr.node.dataset.ExprData;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.base.condition.dataset.BaseExprDataSet;
import com.stars.modules.demologin.LoginModule;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2017/6/14.
 */
public class PcdsActivedJobSet extends BaseExprDataSet {

    private List<Integer> activedJobs;
    private Iterator<Integer> iterator;

    public PcdsActivedJobSet() {
    }

    public PcdsActivedJobSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
        try {
            AccountRow accountRow = loginModule.getAccountRow();
            if (accountRow != null && accountRow.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                try {
                    activedJobs = new ArrayList<>(accountRow.getActivedJobs());
                    iterator = activedJobs.iterator();
                } finally {
                    accountRow.getLoginLock().unlock();
                }
            }
        } catch (Exception e) {
            LogUtil.error("精准推送|条件异常|roleId:" + loginModule.id(), e);
        }

    }

    @Override
    public boolean hasNext() {
        return iterator != null && iterator.hasNext();
    }

    @Override
    public ExprData next() {
        return new PcdsActivedJob(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsActivedJob.fieldSet();
    }
}
