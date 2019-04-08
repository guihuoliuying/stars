package com.stars.modules.demologin.packet;

import com.stars.AccountRow;
import com.stars.ServerVersion;
import com.stars.core.clientpatch.PatchManager;
import com.stars.core.gmpacket.BlockAccountGm;
import com.stars.core.gmpacket.SwitchEntranceGm;
import com.stars.core.gmpacket.WhiteListOpenOrCloseGm;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.demologin.LoginManager;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.modules.demologin.message.LogExitMessage;
import com.stars.modules.demologin.message.LoginSyncMsg;
import com.stars.modules.demologin.message.SqueezeMsg;
import com.stars.modules.demologin.userdata.AccountPassWord;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.BlockAccount;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.startup.MainStartup;
import com.stars.util.*;
import com.stars.core.actor.Actor;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 1.
 * Created by liuyuheng on 2016/6/16.
 */
public class ServerLogin extends Packet {
    private int channelId;
    private String token;
    private boolean isReconnect;
    private boolean isPressTest = false;//是否白名单


    @Override
    public short getType() {
        return LoginPacketSet.S_DEMO_LOGIN;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
//        this.channelId = buff.readInt();
        this.token = buff.readString();
        this.isReconnect = false;
    }

    @Override
    public void execPacket() {
        com.stars.util.LogUtil.info("登陆|账号验证|请求登录|token:{}", token);
        // 防止频繁处理同一连接的登录请求
        if (session.getAttribute("serverloginSession") != null) {
            com.stars.network.server.packet.PacketManager.send(session, new ClientText("请勿频繁点击"));
            Integer count = (Integer) session.getAttribute("serverlogin.count");
            if (count == null) {
                session.putAttribute("serverlogin.count", 1);
            }
            if (count <= 3) {
                session.putAttribute("serverlogin.count", count + 1);
            } else {
                closeSessionWhileException("登陆|账号验证|点击频繁", "请勿频繁点击", null);
            }
            return;
        }
        AccountPassWord apw = new AccountPassWord();
        long start = 0, end = 0;
        try {
            start = System.currentTimeMillis();
            LoginInfo loginInfo = JsonUtil.fromJson(token, LoginInfo.class);
            //设置platform平台值
            loginInfo.setPhoneSystem(loginInfo.getPhoneSystem());//phoneSystem里一起赋值

            // 判断服务入口
            if (isEntranceClosed() && !isWhiteList(loginInfo.getUid())) {
                com.stars.util.LogUtil.info("登陆|账号验证|account:{}|uid:{}|异常:{}",
                        loginInfo.getAccount(), loginInfo.getUid(), "服务入口关闭");
                com.stars.network.server.packet.PacketManager.send(session, new ClientText(LoginManager.loginTips));
                return;
            }

            /* 特殊处理: 360旧端处理 start */
            int serverId = MultiServerHelper.getServerId();
            if (serverId >= 10000 && serverId <= 10045) {
                if (loginInfo != null && loginInfo.getChannel().contains("12000@")) {
                    closeSessionWhileException(
                            "登陆|账号验证|SDK|account:" + apw.getAccount() + "|异常:360旧端登陆",
                            "360旧专区数据已转移，请下载最新客户端前往思美人15区", null);
                    return;
                }
            }
            /* 特殊处理: 360旧端处理 end */

            // 取账号AccountRow
            AccountRow accountRow = null;
            if (isDrirectConnected(loginInfo)) {
                // 直连
                initDirectConnectedLoginInfo(loginInfo); // 初始化直连
                apw.setAccount(loginInfo.getAccount());
                apw.setPassword(com.stars.util.Md5Util.getMD5Str(loginInfo.getPassword()));
                com.stars.util.LogUtil.info("登陆|账号验证|直连|account:{}|password:{}", apw.getAccount(), apw.getPassword());
                accountRow = LoginModuleHelper.getOrLoadAccount(apw.getAccount(), apw.getPassword());
                if (accountRow == null && !isPressTest) {
                    com.stars.util.LogUtil.info("登陆|账号验证|直连|account:{}|异常:{}", loginInfo.getAccount(), "密码错误");
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText("密码错误"));
                    return;
                }

            } else {
                // SDK(经过登陆服验证), account对应uid, password对应sid
                apw.setAccount(loginInfo.getUid() + "#" + loginInfo.getChannel().split("@")[0]); // UID#主渠道号
                apw.setPassword(loginInfo.getSid());
                apw.setUid(loginInfo.getUid());
                com.stars.util.LogUtil.info("登陆|账号验证|SDK|account:{}|password:{}", apw.getAccount(), apw.getPassword());
                if (apw.getPassword().length() <= 4) {
                    closeSessionWhileException(
                            "登陆|账号验证|SDK|account:" + apw.getAccount() + "|异常:密码长度至少为4",
                            "密码长度至少为4", null);
                    return;
                }
                if (isWhiteList(apw.getAccount())) {
                    ServerLogConst.console.info(MessageFormat.format("登陆|账号验证|SDK|account:{0}|白名单", apw.getAccount()));
                    accountRow = LoginModuleHelper.getOrLoadAccount(apw.getAccount(), null);
                } else {
                    String resp = LoginUtil.checkKey(apw.getUid(), apw.getPassword());
                    if (resp != null) {
                        closeSessionWhileException(
                                "登陆|账号验证|SDK|account:" + apw.getAccount() + "|异常:" + resp, resp, null); // todo: 格式化
                        return;
                    }
                    accountRow = LoginModuleHelper.getOrLoadAccount(apw.getAccount(), null);
                }

                String verStr = loginInfo.getVerision();
                String[] version = verStr.split("[.]");
                if (version.length != 3) {
                    closeSessionWhileException("版本号格式不正确:" + loginInfo.getVerision(), "版本号不正确", null);
                    return;
                }
                int bigVersion = Integer.valueOf(version[0]);
                int smallVersion = Integer.valueOf(version[1]);
//                if (ServerVersion.getBigVersion() != bigVersion ||
//                        ServerVersion.getSmallVersion() != smallVersion) {
//                    closeSessionWhileException("版本号不一致:" + verStr, "版本号不一致", null);
//                    return;
//                }
                if (bigVersion < ServerVersion.getBigVersion()) {
                    closeSessionWhileException("版本号不一致:" + verStr, "不支持版本", null);
                    return;
                }
            }

            if (accountRow == null) { // 新账号
                com.stars.util.LogUtil.info("登陆|账号验证|account:{}|注册新账号", apw.getAccount());
                accountRow = new AccountRow(apw.getAccount(), getAccountChannel(loginInfo));
                accountRow.setPassword(apw.getPassword());
                accountRow.setPalform(loginInfo.getPlatForm());
                DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, accountRow, "account"));
                AccountRow tempAccountRow = MainStartup.accountMap.putIfAbsent(apw.getAccount(), accountRow);
                if (tempAccountRow != null && tempAccountRow != accountRow) {
                    accountRow = tempAccountRow;
                }
                ServerLogModule.static_core_gamesvr(loginInfo, DateUtil.formatDateTime(accountRow.getFirstLoginTimestamp()), 0, ThemeType.REGEDIT.getOperateName(), ThemeType.REGEDIT.getOperateId(), "");
            } else {
                com.stars.util.LogUtil.info("登陆|账号验证|account:{}|已有账号", apw.getAccount());
            }

            // 封号判断
            if (BlockAccountGm.isAccountBlock(apw.getAccount())) {
                BlockAccount blockAccount = BlockAccountGm.getBlockAccount(apw.getAccount());
                ClientBlockAccount packet = new ClientBlockAccount((int) (blockAccount.getExpireTime() / 1000),
                        blockAccount.getReason());
                com.stars.network.server.packet.PacketManager.send(session, packet); // todo: 是否需要断开连接?!
                com.stars.util.LogUtil.info("登陆|账号验证|account:{}|异常:封号:{}:{}",
                        apw.getAccount(), blockAccount.getReason(), blockAccount.getExpireTime() / 1000);
                return;
            }

            // 设置登陆的相关信息
            if (accountRow.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) { // 防止因死锁或其他原因导致等待，但不符合测试情况
                long roleId = accountRow.getCurrentRoleId();
                try {
                    // 判断是否存在挤号（同步处理）
                    if (roleId > 0) {
                        com.stars.util.LogUtil.info("登陆|账号验证|account:{}|挤号处理|currentRoleId:{}", apw.getAccount(), roleId);
                        tellMessage(roleId, new SqueezeMsg()); // 异步，不等待
                    }
                    List<AccountRole> roleList = accountRow.getRelativeRoleList();
                    if (roleList != null && roleList.size() > 0) {
                        for (AccountRole accountRole : roleList) {
                            if (accountRole == null || accountRole.getRoleId().equals("")) {
                                continue;
                            }
                            Player p = null;
                            try {
                                p = PlayerSystem.get(Long.parseLong(accountRole.getRoleId()));
                            } catch (Exception e) {
                                com.stars.util.LogUtil.error(e.getMessage(), e);
                            }
                            if (p != null) {
                                p.tell(new LogExitMessage(), com.stars.core.actor.Actor.noSender);
                            }
                        }
                    }
                    accountRow.setLoginToken(token); // 设置登录Token
                    setAndSendReconnectToken(accountRow); // 设置/发送重连token

                    getSession().putAttribute("loginToken", token);
                    getSession().putAttribute("reconnectToken", accountRow.getToken());
                    getSession().putAttribute("serverloginSession", "true");
                    getSession().setAccount(apw.getAccount());

                    accountRow.setLastLoginTime(System.currentTimeMillis());
                    accountRow.setLoginInfo(loginInfo);
                    accountRow.setPalform(loginInfo.getPlatForm());
                    patch(); // 客户端数据补丁
                    sendRoleList(accountRow); // 这里下发角色列表信息
                    sendCreationCd(accountRow); // 这里下发创角CD
                    end = System.currentTimeMillis();
                    com.stars.util.LogUtil.info("登录|账号验证|account:{}|验证完成|耗时:{}|accountRow:{}|session.loginToken:{}|session.reconnectToken:{}|session.connId:{}",
                            apw.getAccount(), end - start, accountRow, token, accountRow.getToken(), session.getConnectionId());
                } catch (Throwable t) {
                    closeSessionWhileException(
                            "登陆|账号验证|account:" + apw.getAccount() + "|异常:" + t.getMessage(), "请求异常", t);
                } finally {
                    accountRow.getLoginLock().unlock();
                }
            } else {
                closeSessionWhileException(
                        "登陆|账号验证|account:" + apw.getAccount() + "|异常:其他设备正在登录", "其他设备正在登录", null);
            }
        } catch (Throwable t) {
            closeSessionWhileException("登陆|账号验证|account:" + apw.getAccount() + "|异常:" + t.getMessage(), "请求异常", t);
        }
    }

    public void closeSessionWhileException(String logStr, String infoStr, Throwable cause) {
        if (cause == null) {
            com.stars.util.LogUtil.error(logStr);
        } else {
            com.stars.util.LogUtil.error(logStr, cause);
        }
        com.stars.network.server.packet.PacketManager.send(session, new ClientText(infoStr == null ? "请求异常" : infoStr));
        PacketManager.closeFrontend(session);
    }

    public void tellMessage(long roleId, LoginSyncMsg message) throws InterruptedException {
        tellMessage(PlayerSystem.get(roleId), roleId, message);
    }

    public void tellMessage(Player player, long roleId, LoginSyncMsg message) throws InterruptedException {
        if (player != null) {
            player.tell(message, Actor.noSender);
        } else {
            LogUtil.error("登录异常，Actor不存在，roleId=" + roleId);
//            throw new RuntimeException("登录异常，Actor不存在，roleId=" + player.id());
        }
    }

    private boolean isEntranceClosed() {
        ServerLogConst.console.info("serverState=" + LoginModuleHelper.serverState);
        return LoginModuleHelper.serverState == SwitchEntranceGm.CLOSE;
    }

    private boolean isWhiteList(String accountName) {
        return WhiteListOpenOrCloseGm.isWhiteList(accountName);
    }

    private boolean isDrirectConnected(LoginInfo loginInfo) {
        return loginInfo.getUid() == null && loginInfo.getSid() == null;
    }

    private void initDirectConnectedLoginInfo(LoginInfo loginInfo) {
        loginInfo.setPhoneNet("银汉wifi");
        loginInfo.setPhoneSystem("Android");
        if (loginInfo.getChannel() == null || loginInfo.getChannel().trim().equals("")) {
            loginInfo.setChannel("1@2@1026");
        }
        loginInfo.setVerision("0.0.0");
        loginInfo.setIp("127.0.0.1");
        loginInfo.setPlatForm("4");
        loginInfo.setImei("iemi1234");
        loginInfo.setMac("sdfsfsddddfdeefsdfa");
        loginInfo.setUid("yh8787");
        loginInfo.setPhoneType("大米手机");
        loginInfo.setNet("不是电信");
        loginInfo.setGameId("1054");
        loginInfo.setUserId("user23");
        loginInfo.setIdfa("version:56844");
    }

    private String getAccountChannel(LoginInfo loginInfo) {
        return loginInfo.getChannel() == null ? "ios" : loginInfo.getChannel();
    }

    private void setAndSendReconnectToken(AccountRow accountRow) {
        String randomToken = RandomUtil.getRandomString(16);
        accountRow.setToken(randomToken);
        send(new ClientReconnect(randomToken));
    }

    private void sendRoleList(AccountRow accountRow) throws SQLException {
        ClientAccountRoleList packet = new ClientAccountRoleList(accountRow);
        send(packet);
    }

    private void sendCreationCd(AccountRow accountRow) {
        ClientAccountRoleList clientAccountRoleList2 = new ClientAccountRoleList();
        clientAccountRoleList2.setType(ClientAccountRoleList.TYPE_CREATEROLECD);
        clientAccountRoleList2.setReaminCreateRoleCd(LoginModuleHelper.getCreateRoleRemainCd(accountRow));
        send(clientAccountRoleList2);
    }

    private void patch() {
        if (PatchManager.needPatch) {
            send(new ClientPatch());
        }
    }
}
