package com.stars.server.login.manager;

import com.stars.network.server.session.GameSession;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public class RegisterManager {
    public static RegisterManager manager = new RegisterManager();

    public void register(GameSession session, String account, String password, String mac) {
//        ValidateManager.manager.checkInterval(session, mac);
//        ValidateManager.manager.checkInBlackAndIsReg(session, account);
//        CreationManager.manager.createAccountAndRoleId(session, account, password);
    }
}
