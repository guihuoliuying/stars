package com.stars.server.login.manager;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public class ValidateManager {
    public static ValidateManager manager = new ValidateManager();

    /**
     * 登录请求间隔
     *
     * @param mac
     * @return
     */
//    public void checkInterval(GameSession session, String mac) {
//        long lastLogin = LoginDataPool.INSTANCE.getMacMap().get(mac);
//        boolean result = (System.currentTimeMillis() - lastLogin) > LoginConstant.LOGIN_INTERVAL;
//        if (!result) {
//            // 返回警告
//            LoginServerHandler.sendToClient(session, new ClientWarning("account is in black list"));
//            LoginManager.manager.sendCheckFail(session);
//            return;
//        }
//    }

    /**
     * 验证黑名单/注册
     *
     * @param account
     * @return
     */
//    public boolean checkInBlackAndIsReg(GameSession session, String account) {
//        boolean result = true;
//        if (LoginDataPool.INSTANCE.isInBlack(account)) {
//            // 返回警告
//            LoginServerHandler.sendToClient(session, new ClientWarning("account is in black list"));
//            LoginManager.manager.sendCheckFail(session);
//            result = false;
//        }
//        if (!LoginDataPool.INSTANCE.isRegister(account)) {
//            // 返回警告
//            LoginServerHandler.sendToClient(session, new ClientWarning("account does not register"));
//            LoginManager.manager.sendCheckFail(session);
//            result = false;
//        }
//        return result;
//    }
//
//    public void validateAuthCode(GameSession session, String code) {
//
//    }
//
//    /* 渠道验证
//    *  注意异步超时
//    * */
//    public void validateFromChannel(LoginInfo loginInfo) {
//
//    }
//
//    public void validateWithoutSDK(GameSession session, String account, String password) {
//        String pwdMd5 = Md5Util.getMD5Str(password);
//        AccountInfo accountInfo = LoginDataPool.INSTANCE.getAccountInfo(account);
//        if (!pwdMd5.equals(accountInfo.getPwdMd5())) {
//            // 返回警告
//            LoginServerHandler.sendToClient(session, new ClientWarning("password error"));
//            LoginManager.manager.sendCheckFail(session);
//            return;
//        }
//        if (accountInfo.getRoleId() == 0) {
//            // 创建roleId
//            CreationManager.manager.createRoleId(session, account);
//        } else {
//            CoreLogger.info("login check suc");
//            // 验证成功 返回roleId
//            LoginManager.manager.sendCheckSuc(session, accountInfo.getAccount(), accountInfo.getRoleId());
//        }
//    }
}
