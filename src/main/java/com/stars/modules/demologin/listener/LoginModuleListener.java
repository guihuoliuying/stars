package com.stars.modules.demologin.listener;

import com.stars.AccountRow;
import com.stars.core.clientpatch.ClientPatchEvent;
import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.packet.ClientPatch;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.role.event.RoleLevelUpEvent;

/**
 * Created by zhaowenshuo on 2017/1/22.
 */
public class LoginModuleListener extends AbstractEventListener<LoginModule> {

    public LoginModuleListener(LoginModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RoleLevelUpEvent) {
            AccountRow accountRow = null;
            try {
                accountRow = LoginModuleHelper.getOrLoadAccount(module().getAccount(),null);
            } catch (Exception e) {

            }
            if (accountRow != null) {
                AccountRole accountRole = accountRow.getAccountRole(module().id());
                if (accountRole != null) {
                    accountRole.roleLevel = ((RoleLevelUpEvent) event).getNewLevel();
                }
            }
        }
        if (event instanceof ClientPatchEvent) {
            module().send(new ClientPatch());
        }
    }
}
