package com.stars.modules.demologin;

import com.stars.core.clientpatch.ClientPatchEvent;
import com.stars.core.event.EventDispatcher;
import com.stars.core.event.EventListener;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.demologin.gm.*;
import com.stars.modules.demologin.listener.LoginModuleListener;
import com.stars.modules.gm.GmManager;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/6/16.
 */
public class LoginModuleFactory extends AbstractModuleFactory<LoginModule> {
    public LoginModuleFactory() {
        super(new LoginPacketSet());
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("accountTransfer", new AccountTransferGmHandler());
        GmManager.reg("resetdaily", new ResetDailyGmHandler());
        GmManager.reg("resetweekly", new ResetWeeklyGmHandler());
        GmManager.reg("resetmonthly", new ResetMonthlyGmHandler());
        GmManager.reg("adduser", new AddUserGmHandler());
        GmManager.reg("offline", new OfflineGmHandler());
        GmManager.reg("tied", new BindUserGmHandler());
        GmManager.reg("packet", new PrintPacketGmHandler());
    }

    @Override
    public LoginModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new LoginModule(id, self, eventDispatcher, map);
    }

    private void loadInNetAccount() throws Exception {
        String sql = "select * from inetaccount";
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        EventListener listener = new LoginModuleListener((LoginModule) module);
        eventDispatcher.reg(RoleLevelUpEvent.class, listener);
        eventDispatcher.reg(ClientPatchEvent.class, listener);
    }
}
