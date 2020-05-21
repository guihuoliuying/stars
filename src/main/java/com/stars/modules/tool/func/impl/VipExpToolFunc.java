package com.stars.modules.tool.func.impl;

import com.stars.AccountRow;
import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyuxing on 2017/4/2.
 */
public class VipExpToolFunc extends ToolFunc {
    private int exp = 0;

    public VipExpToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式类似：24|1000  1000代表获取的vip经验数量
        if (function == null || "0".equals(function.trim()) || function.equals("")) {
            return;
        }
        String str[] = function.split("\\|");
        exp = Integer.parseInt(str[1]);
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count,
                                Object... args) {
        ToolFuncResult tr;
        tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        return tr;
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        try {
            AccountRow account = loginModule.getAccountRow();
            int addExp = exp * count;
            try {
                if (account.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                    try {
                        int oldVIPLv = account.getVipLevel();
                        int newExp = addExp + account.getVipExp();
                        account.setVipExp(newExp);
                        //接下来计算VIP等级
                        int counter = oldVIPLv + 1;
                        VipinfoVo vipinfoVo = VipManager.getVipinfoVo(counter);
                        while (vipinfoVo != null && vipinfoVo.getReqExp() <= newExp) {
                            account.setVipLevel(counter);
                            counter++;
                            vipinfoVo = VipManager.getVipinfoVo(counter);
                        }
                        if (oldVIPLv != account.getVipLevel()) {
                            //刷新在线角色的vip等级
                            ServiceHelper.roleService().notice(roleModule.id(), new VipLevelupEvent(oldVIPLv, account.getVipLevel()));

                            for (int level = oldVIPLv + 1, curLevel = account.getVipLevel(); level <= curLevel; level++) {
                                // 本服滚屏公告
                                vipinfoVo = VipManager.getVipinfoVo(level);
                                if (vipinfoVo.getLevelUpNoticeType() == VipManager.VIP_LEVELUP_NOTICE_TYPE_LOACL && StringUtil.isNotEmpty(vipinfoVo.getLevelUpNotice())) {
                                    RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(roleModule.getRoleRow().getRoleId(), "role");
                                }
                            }
                        }
                        //入库
                        DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getUpdateSql(DBUtil.DB_USER, account, "account", "name='" + account.getName() + "'"));
                    } catch (Exception e) {
                        com.stars.util.LogUtil.error(e.getMessage(), e);
                    } finally {
                        account.getLoginLock().unlock();
                    }
                }

            } catch (Exception e) {
                com.stars.util.LogUtil.error(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return null;
    }
}
