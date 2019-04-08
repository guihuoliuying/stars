package com.stars.server.login.manager;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public class LoginManager {
    public static LoginManager manager = new LoginManager();

//    public void login(GameSession session, LoginInfo loginInfo) {
//        // 验证黑名单/是否注册
//        boolean cbr = ValidateManager.manager.checkInBlackAndIsReg(session, loginInfo.getAccount());
//        if(!cbr){
//            return;
//        }
//        // 验证码
//        ValidateManager.manager.validateAuthCode(session, loginInfo.getAuthcode());
////        LoginDataPool.INSTANCE.putMacMap(loginInfo.getMac(), System.currentTimeMillis());
//        // 不带sdk
//        if ("".equals(loginInfo.getChannelId()) && "".equals(loginInfo.getChannelId_sub())) {
//            ValidateManager.manager.validateWithoutSDK(session, loginInfo.getAccount(), loginInfo.getPassword());
//
//        } else if (loginInfo.isChannelChecked()) {// 渠道已验证 fixme: 不能直接相信客户端
//            // fixme: 渠道会给我们一个公钥用来解密的吧，能够解密就确信他到渠道那里验证过了
//            CreationManager.manager.createAccountAndRoleId(session, loginInfo.getAccount(), loginInfo.getPassword());
//
//        } else {// 渠道未验证 todo: 到渠道那里验证
//            ValidateManager.manager.validateFromChannel(loginInfo);
//        }
//    }
//
//    public void loadAccount(CountDownLatch latch) {
//        long dbKey = CommonDbInfo.getDbId("common", true);// todo:固定dbId
//        String sql = "select * from `user`";
//        // 加载账号
//        LoginServerUtil.getCommClient().request(new SqlDataReqPacket(dbKey, sql), new SelectAccountCallback(latch));
//    }
//
//    public void loadRoleId(CountDownLatch latch) {
//        long dbKey = CommonDbInfo.getDbId("common", true);// todo:固定dbId
//        String sql = "select * from `account`";
//        // 加载roleId
//        LoginServerUtil.getCommClient().request(new SqlDataReqPacket(dbKey, sql), new SelectRoleIdCallback(latch));
//    }
//
//    public void sendCheckSuc(GameSession session, String account, long roleId) {
//        ClientLoginCheck clientLoginCheck = new ClientLoginCheck(LoginConstant.LOGINCHECK_SUC, account, roleId,
//                LoginConstant.returnIp, LoginConstant.returnPort);
//        LoginServerHandler.sendToClient(session, clientLoginCheck);
//    }
//
//    public void sendCheckFail(GameSession session) {
//        ClientLoginCheck clientLoginCheck = new ClientLoginCheck(LoginConstant.LOGINCHECK_FAIL);
//        LoginServerHandler.sendToClient(session, clientLoginCheck);
//    }
//}
//
//class SelectAccountCallback implements DbProxyCallback {
//    private CountDownLatch latch;
//
//    public SelectAccountCallback(CountDownLatch latch) {
//        this.latch = latch;
//    }
//
//    @Override
//    public void onCalled(Object responseMsg) {
//        SqlData sqlData = ((SqlDataRespPacket) responseMsg).getSqlData();
//        List<RowData> list = sqlData.getMultiRowResult();
//        for (RowData rowData : list) {
//            String account = rowData.getString("account");
//            String password = rowData.getString("password");
//            AccountInfo accountInfo = LoginDataPool.INSTANCE.getAccountInfo(account);
//            if (accountInfo == null) {
//                accountInfo = new AccountInfo();
//            }
//            accountInfo.setAccount(account);
//            accountInfo.setPwdMd5(password);
//            LoginDataPool.INSTANCE.putAccountInfo(accountInfo);
//        }
//        CoreLogger.info("load account over");
//        latch.countDown();
//    }
//
//    @Override
//    public void onFailed(Object failedMsg) {
//        CoreLogger.info("load account error!!!!!");
//        latch.countDown();
//    }
//}
//
//class SelectRoleIdCallback implements DbProxyCallback {
//    private CountDownLatch latch;
//
//    public SelectRoleIdCallback(CountDownLatch latch) {
//        this.latch = latch;
//    }
//
//    @Override
//    public void onCalled(Object responseMsg) {
//        SqlData sqlData = ((SqlDataRespPacket) responseMsg).getSqlData();
//        List<RowData> list = sqlData.getMultiRowResult();
//        for (RowData rowData : list) {
//            String account = rowData.getString("account");
//            long roleId = Long.parseLong(rowData.getString("roleid"));
//            AccountInfo accountInfo = LoginDataPool.INSTANCE.getAccountInfo(account);
//            if (accountInfo == null) {
//                accountInfo = new AccountInfo();
//            }
//            accountInfo.setAccount(account);
//            accountInfo.setRoleId(roleId);
//            LoginDataPool.INSTANCE.putAccountInfo(accountInfo);
//        }
//        CoreLogger.info("load roleId over");
//        latch.countDown();
//    }
//
//    @Override
//    public void onFailed(Object failedMsg) {
//        CoreLogger.info("load roleId error!!!!!");
//        latch.countDown();
//    }
}