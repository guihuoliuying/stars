package com.stars.server.login.manager;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public class CreationManager {
	
    public static CreationManager manager = new CreationManager();

    /* 创建账号 user表 */
//    public void createAccountAndRoleId(GameSession session, String account, String password) {
//        String pwdMd5 = Md5Util.getMD5Str(password);
//        long dbKey = CommonDbInfo.getDbId("common", true);// todo:固定dbId
//        String insertAccountSql = "insert into `user` values('{}','{}')";
//        SimpleStringFormatter ssf = new SimpleStringFormatter(insertAccountSql);
//        String sql = ssf.format(account, pwdMd5);
//        // 创建账号
//        LoginServerUtil.getCommClient().request(new SqlDataReqPacket(dbKey, sql),
//                new CreateAccountCallback(session, account, pwdMd5));
//    }
//
//    /* 创建RoleId account表 */
//    public void createRoleId(GameSession session, String account) {
//        long dbKey = CommonDbInfo.getDbId("common", true);// todo:固定dbId
//        long roleId = LoginServerUtil.getIdGenerator().newId("roleId");
//        String insertSql = "insert into `account` values('{}','{}','{}','{}','{}')";
//        SimpleStringFormatter ssf = new SimpleStringFormatter(insertSql);
//        String sql = ssf.format(account, roleId, "",
//                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()), 0);
//        // 创建RoleId
//        LoginServerUtil.getCommClient().request(new SqlDataReqPacket(dbKey, sql),
//                new CreateRoleIdCallback(session, account, roleId));
//    }
//}
//
//class CreateAccountCallback implements DbProxyCallback {
//    private GameSession session;
//    private String account;
//    private String pwdMd5;
//
//    public CreateAccountCallback(GameSession session, String account, String pwdMd5) {
//        this.session = session;
//        this.account = account;
//        this.pwdMd5 = pwdMd5;
//    }
//
//    @Override
//    public void onCalled(Object responseMsg) {
//        SqlData sqlData = ((SqlDataRespPacket) responseMsg).getSqlData();
//        boolean result = (boolean) sqlData.getSingleRowResult().get("result");
//        if (result) {
//            CoreLogger.info("create account suc");
//            AccountInfo accountInfo = new AccountInfo();
//            accountInfo.setAccount(account);
//            accountInfo.setPwdMd5(pwdMd5);
//            LoginDataPool.INSTANCE.putAccountInfo(accountInfo);
//            CreationManager.manager.createRoleId(session, account);
//        } else {
//            CoreLogger.error("create account fail");
//            LoginManager.manager.sendCheckFail(session);
//        }
//    }
//
//    @Override
//    public void onFailed(Object failedMsg) {
//        FailureRespPacket failPkt = (FailureRespPacket) failedMsg;
//        CoreLogger.info(failPkt.getFailMsg());
//        failPkt.printFailCause();            // 打印错误信息
//        CoreLogger.error("create account fail");
//        LoginManager.manager.sendCheckFail(session);
//    }
//}
//
//class CreateRoleIdCallback implements DbProxyCallback {
//    private GameSession session;
//    private String account;
//    private long roleId;
//
//    public CreateRoleIdCallback(GameSession session, String account, long roleId) {
//        this.session = session;
//        this.account = account;
//        this.roleId = roleId;
//    }
//
//    @Override
//    public void onCalled(Object responseMsg) {
//        SqlData sqlData = ((SqlDataRespPacket) responseMsg).getSqlData();
//        boolean result = (boolean) sqlData.getSingleRowResult().get("result");
//        AccountInfo accountInfo = LoginDataPool.INSTANCE.getAccountInfo(account);
//        if (result) {
//            CoreLogger.info("create roleId suc");
//            accountInfo.setRoleId(roleId);
//            LoginDataPool.INSTANCE.putAccountInfo(accountInfo);
//            // 创建成功 返回roleId
//            LoginManager.manager.sendCheckSuc(session, account, roleId);
//        } else {
//            CoreLogger.error("create roleId fail");
//            accountInfo.setRoleId(0);
//            LoginDataPool.INSTANCE.putAccountInfo(accountInfo);
//            // 创建失败
//            LoginManager.manager.sendCheckFail(session);
//        }
//    }
//
//    @Override
//    public void onFailed(Object failedMsg) {
//        FailureRespPacket failPkt = (FailureRespPacket) failedMsg;
//        CoreLogger.info(failPkt.getFailMsg());
//        failPkt.printFailCause();            // 打印错误信息
//        CoreLogger.error("create roleId fail");
//        AccountInfo accountInfo = LoginDataPool.INSTANCE.getAccountInfo(account);
//        accountInfo.setRoleId(0);
//        LoginDataPool.INSTANCE.putAccountInfo(accountInfo);
//        // 创建失败
//        LoginManager.manager.sendCheckFail(session);
//    }
}
