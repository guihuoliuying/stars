package com.stars.modules.tool.func.impl;

import com.stars.AccountRow;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2017/6/9.
 */
public class MergeServerVipUpdatedToolFunc extends ToolFunc {

    public MergeServerVipUpdatedToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {

    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        return new ToolFuncResult(true, null);
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        try {
            LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
            long roleId = loginModule.id();
            com.stars.util.LogUtil.info("合区Vip经验补偿|开始|roleId:{}", roleId);
            AccountRow account = LoginModuleHelper.getOrLoadAccount(loginModule.getAccount(), null);
            if (account.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                try {
                    int preVipLevel = account.getVipLevel();
                    int vipExp = account.getVipExp();
                    //接下来计算VIP等级
                    int maxVipLevel = preVipLevel + 1;
                    try {
                        VipinfoVo vipinfoVo = VipManager.getVipinfoVo(maxVipLevel);
                        while (vipinfoVo != null && vipinfoVo.getReqExp() <= vipExp) {
                            account.setVipLevel(maxVipLevel);
                            maxVipLevel++;
                            vipinfoVo = VipManager.getVipinfoVo(maxVipLevel);
                        }
                        if (preVipLevel != account.getVipLevel()) {
                            //刷新充值角色的vip等级
                            Player player = PlayerSystem.get(roleId);
                            if (player != null) {
                                com.stars.util.LogUtil.info("合区Vip经验补偿|使用道具|VipLevelupEvent|roleId:{}|prevLv:{}|curLv:{}", roleId, preVipLevel, account.getVipLevel());
                                ServiceHelper.roleService().notice(roleId, new VipLevelupEvent(preVipLevel, account.getVipLevel()));
                            }
                        }
                    } catch (Exception e) {
                        com.stars.util.LogUtil.error("合区Vip经验补偿|使用道具|异常|roleId:" + roleId + "|exp:" + vipExp + "|preLv:" + preVipLevel + "|curLv:" + maxVipLevel , e);
                    }
                    //入库
                    DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getUpdateSql(DBUtil.DB_USER, account, "account", "name='" + account.getName() + "'"));
                    com.stars.util.LogUtil.info("合区Vip经验补偿|完成|roleId:{}|prevLv:{}|curLv:{}", roleId, preVipLevel, account.getVipLevel());
                } catch (Exception e) {
                    com.stars.util.LogUtil.error("合区Vip经验补偿|异常|roleId:" + roleId, e);
                } finally {
                    account.getLoginLock().unlock();
                }
            }
        }catch(Exception e){
            LogUtil.error("合区Vip经验补偿|异常", e);
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(71311560664L >> 22);
    }
}
