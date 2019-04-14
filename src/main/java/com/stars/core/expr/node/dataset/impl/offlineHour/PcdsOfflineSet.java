package com.stars.core.expr.node.dataset.impl.offlineHour;

import com.stars.AccountRow;
import com.stars.core.expr.node.dataset.PushCondData;
import com.stars.core.expr.node.dataset.PushCondDataSet;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by huwenjun on 2017/7/14.
 */
public class PcdsOfflineSet extends PushCondDataSet {
    private Iterator<PcdsOffline> iterator;

    public PcdsOfflineSet() {
    }

    public PcdsOfflineSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        LoginModule loginModule = module(MConst.Login);
        try {
            AccountRow accountRow = loginModule.getAccountRow();
            List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
            Map<Long, LoginRow> loginRowMap = accountRow.getLoginRowMap();
            List<PcdsOffline> pcdsOfflineList = new ArrayList<>();
            for (AccountRole accountRole : relativeRoleList) {
                String roleIdStr = accountRole.getRoleId();
                long roleId = Long.parseLong(roleIdStr);
                LoginRow loginRow = loginRowMap.get(roleId);
                Date lastLoginTime = new Date(loginRow.getLastLoginTimestamp());
                long id = loginModule.id();
                if (accountRole.getRoleId().equals(id + "")) {
                    lastLoginTime = new Date(loginRow.getLastLastLoginTimestamp());
                }
                int offlineHours = DateUtil.getHoursBetweenTwoDates(lastLoginTime, new Date());
                PcdsOffline pcdsOffline = new PcdsOffline(offlineHours, accountRole.roleLevel, accountRole.fightScore);
                pcdsOfflineList.add(pcdsOffline);
            }
            this.iterator = pcdsOfflineList.iterator();
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }

    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return iterator.next();
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsOffline.fieldSet();
    }
}
