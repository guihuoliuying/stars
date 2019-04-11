package com.stars.modules.demologin.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.role.RoleModule;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/7/27.
 */
public class ResetDailyGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {

        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
        String account = loginModule.getAccount();
        roleModule.warn("账号:'" + account + "'进行每日重置");
        ServiceHelper.chatService().chat(
                roleModule.getRoleRow().getName(), ChatManager.CHANNEL_WORLD, roleId, 0L,
                "账号:'" + account + "'进行每日重置", false);

        LoginModuleHelper.resetDaily(false);
        LoginModuleHelper.FiveOClockResetDaily(false);
        // 排行榜每日发奖
        ServiceHelper.familyMainService().resetDaily();
        // 排行榜每日重置
        // 召唤boss每日重置
        // 离线pvp每日重置
        //重置所有活动
        //ServiceHelper.operateActivityService().resetAllActivitise();
    }
}
