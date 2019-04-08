package com.stars.modules.demologin.packet;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.module.ModuleManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.redpoint.RedPoints;
import com.stars.coreManager.SaveDBManager;
import com.stars.db.DBUtil;
import com.stars.db.SqlUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.LoginConstant;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.modules.demologin.message.LoginMsg;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.DirtyWords;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.Actor;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 客户端请求创建角色\删除角色\选择角色进入游戏;
 * Created by panzhenfeng on 2016/8/3.
 */
public class ServerRoleOp extends Packet {
    private byte oprType = 0;
    private int jobId; // 这个根据oprType来赋值;
    private long roleId; // 这个根据oprType来赋值;
    private String roleName;

    public ServerRoleOp() {

    }

    @Override
    public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        oprType = buff.readByte();
        String oprIdStr = buff.readString();
        if (LoginConstant.SERVER_LOGIN_OPR_ROLE_TYPE_CREATE == oprType) {
            jobId = Integer.parseInt(oprIdStr);
            roleName = buff.readString().trim();
        } else if (LoginConstant.SERVER_LOGIN_OPR_ROLE_TYPE_SELECTPLAY == oprType || LoginConstant.SERVER_LOGIN_OPR_ROLE_TYPE_DELETE == oprType) {
            roleId = Long.parseLong(oprIdStr);
        }
    }

    @Override
    public void execPacket() {
        AccountRow accountRow = null;
        String account = getSession().getAccount();
        try {
            accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            if (accountRow == null) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        try {
            if (accountRow.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                try {
                    if (!isAuthentication(accountRow)) { //判断会话是否是合法的，因为之前必须进过ServerLogin的处理;
                        com.stars.util.LogUtil.error("创建角色出错, 会话没有经过ServerLogin处理，不合法!");
                        com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "会话过期，请重新登录"));
                        com.stars.network.server.packet.PacketManager.closeFrontend(session);
                        return;
                    }
                    // 1. 挤号处理
                    long currentRoleId = accountRow.getCurrentRoleId();
                    // if currentRoleId == roleId
                    // if currentRoleId != roleId
                    if (currentRoleId != 0) {
                        if (currentRoleId == roleId) {

                        } else {

                        }
                    }
                    account=accountRow.getName();//因为存在账号转移功能了，故账号得用游戏账号uid
                    // 创角/登录/删角
                    switch (oprType) {
                        case LoginConstant.SERVER_LOGIN_OPR_ROLE_TYPE_CREATE:
                            send(new ClientServerDate(openServerDate()));
                            createRole(account, accountRow);
                            break;
                        case LoginConstant.SERVER_LOGIN_OPR_ROLE_TYPE_DELETE:
                            deleteRole(account, accountRow);
                            break;
                        case LoginConstant.SERVER_LOGIN_OPR_ROLE_TYPE_SELECTPLAY:
                            send(new ClientServerDate(openServerDate()));
                            selectRole(account, accountRow);
                            break;
                    }
                } catch (Throwable t) {
                    com.stars.util.LogUtil.error("创建角色出错，account=" + account, t);
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "请求异常"));
                    com.stars.network.server.packet.PacketManager.closeFrontend(session);
                } finally {
                    accountRow.getLoginLock().unlock();
                }
            } else {
                com.stars.util.LogUtil.info("选角|获取锁失败|account:{}", account);
                com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "请求异常"));
                com.stars.network.server.packet.PacketManager.closeFrontend(session);
            }
        } catch (Exception e) {
            com.stars.util.LogUtil.error("创建角色出错, 锁定角色失败");
            com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "请求异常"));
            com.stars.network.server.packet.PacketManager.closeFrontend(session);
        }
    }

    private int openServerDate() {
        return DataManager.getServerDays();
    }

    private boolean isAuthentication(AccountRow accountRow) {
//        String state = (String) getSession().getAttribute("serverloginSession");
//        String loginToken = (String) getSession().getAttribute("loginToken");
        String reconnectToken = (String) getSession().getAttribute("reconnectToken");

//        LogUtil.info("isAuthentication, accountRow={}, account={}, accountRow.loginToken={}, accountRow.reconnectToken={}, session.state={}, session.loginToken={}, session.reconnectToken={}, session.connId={}",
//                accountRow, accountRow.getName(), accountRow.getLoginToken(), accountRow.getToken(), state, loginToken, reconnectToken, session.getConnectionId());

        com.stars.util.LogUtil.info("isAuthentication, accountRow={}, account={}, accountRow.reconnectToken={}, session.reconnectToken={}, session.connId={}",
                accountRow, accountRow.getName(), accountRow.getToken(), reconnectToken, session.getConnectionId());

        /* 只判断重连Token
         * 1. 如果判断登录Token的话，在切换角色的时候存在问题
         * 2. 如果判断state的话，重连后再切换角色会有问题
         */
        return reconnectToken != null && reconnectToken.equals(accountRow.getToken());
    }

    private void syncCreateRoleCd(AccountRow accountRow) {
        long remainCreateRoleCd = LoginModuleHelper.getCreateRoleRemainCd(accountRow);
        ClientAccountRoleList clientAccountRoleList = new ClientAccountRoleList();
        clientAccountRoleList.setType(ClientAccountRoleList.TYPE_CREATEROLECD);
        clientAccountRoleList.setReaminCreateRoleCd(remainCreateRoleCd);
        send(clientAccountRoleList);
    }

    private void createRole(String account, AccountRow accountRow) throws Exception {
        long remainCreateRoleCd = LoginModuleHelper.getCreateRoleRemainCd(accountRow);
        try {
            if (accountRow.getLoginLock().tryLock()) {
                //判断是否处于创角CD中;
                if (remainCreateRoleCd > 0) {
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "createroletime", Integer.toString((int) (Math.ceil((double) remainCreateRoleCd / 1000 / 60)))));
                    syncCreateRoleCd(accountRow);
                    com.stars.util.LogUtil.info("创角|失败:CD中|account:{}", account);
                    return;
                }
                //先判断下当前帐号的角色数量是否足够了;
                int maxCanCreateRoleCount = Integer.parseInt(DataManager.getCommConfig("rolelimit"));
                if (accountRow.getRelativeRoleList() != null && accountRow.getRelativeRoleList().size() >= maxCanCreateRoleCount) {
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "该帐号创建的角色已达上限"));
                    com.stars.util.LogUtil.info("创角|失败:角色已满|account:{}", account);
                    return;
                }
                //判断名字是否合法;
                if (!isNameLegal()) {
                    com.stars.util.LogUtil.info("创角|失败:名字非法|account:{}", account);
                    return;
                }
                //开始创建角色;
                long roleId = ServiceHelper.idService().newRoleId();
                //创建对应的Role数据，然后创建accountRole数据;
                Role role = new Role(roleId);
                //填充用户自定义的数据;
                role.setJobId(jobId);
                role.setName(roleName);
                role.setCreateTime(System.currentTimeMillis());
                //插入新的role数据;
                try {
                    DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, role, "role"));
                } catch (SQLException e) {
                    com.stars.util.LogUtil.info("创角|失败:入库失败|account:{}", account);
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "名字已存在，请换一个名字!"));
                    return;
                }
                // 强制加入创角流程
                Player player = PlayerSystem.get(roleId);
                if (player == null) {
                    player = new Player(roleId);
                    EventDispatcher eventDispatcher = new EventDispatcher();
                    ModuleContext context = new ModuleContext(roleId);
                    RedPoints redPoints = new RedPoints(player);
                    Map<String, Module> moduleMap = ModuleManager.newModuleList(roleId, player, eventDispatcher);
                    LoginModuleHelper.inject(moduleMap, context, redPoints);
                    ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
                    accountRow.sendLog(log);
                    log.accept("roleCreateTime", accountRow.getRelativeRoleTime(roleId + ""));
                    // 创建账号与角色的关联关系
                    AccountRole accountRole = new AccountRole(account, Long.toString(roleId), new Timestamp(System.currentTimeMillis()));
                    accountRole.roleName = roleName;
                    accountRole.roleLevel = 1;
                    accountRole.roleJobId = jobId;
                    accountRole.lastLoginTimestamp = System.currentTimeMillis();
                    context.insert(accountRole);
                    AccountRow tempAccountRow = accountRow.copy();
                    tempAccountRow.setLastCreateRoleTimestamp(System.currentTimeMillis());
                    tempAccountRow.addCreateRoleCount();
                    context.update(tempAccountRow);
                    for (Module module : moduleMap.values()) {
                        LoginModuleHelper.moduleAccountInject(module, accountRow);
                        module.onCreation(roleName, account);
                    }
                    context.flush(true, true); // fixme: exit on failed!
                    // 修改内存的账号数据
                    List<AccountRole> roleList = accountRow.getRelativeRoleList();
                    if (roleList == null) {
                        roleList = new ArrayList<>();
                    }
                    roleList.add(accountRole);
                    accountRow.setRelativeRoleList(roleList);
                    //写入当前创建角色的时间戳;
                    accountRow.setLastCreateRoleTimestamp(System.currentTimeMillis());
                    accountRow.addCreateRoleCount();
                    // 强制写入session
                    session.setRoleId(roleId);
                    //
                    player.init(moduleMap, eventDispatcher, context, redPoints);
                    Player oldPlayer = PlayerSystem.getOrAdd(roleId, player);
                    log.accept("roleid", roleId + "");
                    log.accept("job", jobId + "");
                    log.Log_core_role(ThemeType.ROLEREG.getOperateId(), ThemeType.ROLEREG.getOperateName(), DateUtil.formatDateTime(System.currentTimeMillis()), "");

                    if (oldPlayer == player) {
                        SaveDBManager.addRole2Save(roleId);
                        player.setFrom((byte) 1);
                    } else {
                        player = oldPlayer;
                    }
                }
                if (player != null) {
                    player.tell(new LoginMsg(account, roleId, accountRow.newRoleIdVersion(), session, true), com.stars.core.actor.Actor.noSender);
                } else {
                    com.stars.util.LogUtil.info("创角|伪失败:服务器爆满|account:{}", account);
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText("服务器爆满"));
                }
                addToSpecialAccount(accountRow.getName(), roleId);
                com.stars.util.LogUtil.info("创角|成功|account:{}|roleId:{}", account, roleId);
                //通知银汉广告服务
                ServiceHelper.advertInfService().addToSendMap(roleId, accountRow.getLoginInfo());
                return;
            } else {
                com.stars.util.LogUtil.info("创角|失败:获取锁失败|account:{}", account);
                com.stars.network.server.packet.PacketManager.closeFrontend(session);
            }
        } catch (Throwable t) {
            com.stars.network.server.packet.PacketManager.send(session, new ClientText("创角异常"));
            LogUtil.error("创角异常", t);
        } finally {
            accountRow.getLoginLock().unlock();
        }

    }

    private void deleteRole(String account, AccountRow accountRow) throws SQLException {
        if (isRoleIdValid(accountRow, roleId)) {
            String roleIdStr = Long.toString(roleId);
            //清除角色,这里只移除accountRole;
            List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
            for (int i = 0, len = relativeRoleList.size(); i < len; i++) {
                if (relativeRoleList.get(i).getRoleId().equals(roleIdStr)) {
                    if (relativeRoleList.get(i).roleLevel >= Integer.parseInt(DataManager.getCommConfig("roledeletelimit"))) {
                        com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "roledeletelimit_tips"));
                        return;
                    }
                    //实际库删除;
                    DBUtil.execSql(DBUtil.DB_USER, relativeRoleList.get(i).getDeleteSql());
                    relativeRoleList.remove(i);
                    accountRow.setRelativeRoleList(relativeRoleList);
                    ClientAccountRoleList clientAccountRoleList = new ClientAccountRoleList(accountRow);
                    send(clientAccountRoleList);
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "creatroledelete_succeed"));
                    break;
                }
            }
        } else {
            com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "请求操作的角色ID无效!" + roleId));
        }
    }

    private void selectRole(String account, AccountRow accountRow) throws Throwable {
        if (isRoleIdValid(accountRow, roleId)) {
            Player player = PlayerSystem.get(roleId);
            if (player == null) {
                player = LoginModuleHelper.loadPlayerFromDB(roleId, accountRow);
            }
            if (player != null) {
                addToSpecialAccount(accountRow.getName(), roleId);
                player.tell(new LoginMsg(account, roleId, accountRow.newRoleIdVersion(), session, false), Actor.noSender);
            } else {
                com.stars.network.server.packet.PacketManager.send(session, new ClientText("服务器爆满"));
            }
        } else {
            com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "请求操作的角色ID无效!" + roleId));
        }
    }


    /**
     * 判断名字是否合法;
     */
    private boolean isNameLegal() throws SQLException, UnsupportedEncodingException {
        if (StringUtil.isNotEmpty(roleName)) {
            //判断是否符合限定的长度;
            String[] tmpStrArr = DataManager.getCommConfig("randomname_length").split("\\+");
            int minBytesCount = Integer.parseInt(tmpStrArr[0]);
            int maxBytesCount = Integer.parseInt(tmpStrArr[1]);
            int curRoleNameBytesCount = roleName.length();
            if (curRoleNameBytesCount > maxBytesCount) {
                com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "randomename_toolong"));
                return false;
            }
            if (curRoleNameBytesCount < minBytesCount) {
                com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "randomename_tooshort"));
                return false;
            }
            if (isContainDirtyWords(roleName)) {
                com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "randomename_unablecharacter"));
                return false;
            }
//            for (int i = 0; i < roleName.length(); i++) {
//                char c = roleName.charAt(i);
//                if (!StringUtil.isChineseWithoutPunctuation(c) && !Character.isLetterOrDigit(c)) {
//                    PacketManager.send(session, new ClientText(MConst.CCLogin, "randomename_unablecharacter"));
//                    return false;
//                }
//            }
            if (!StringUtil.isValidString(roleName)) {
                com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "randomename_unablecharacter"));
                return false;
            }
            //判断是否已经有同名的了;
            if (DBUtil.queryCount(DBUtil.DB_USER, "select count(1) from `role` where `name`=\"" + roleName + "\"") > 0) {
                com.stars.network.server.packet.PacketManager.send(session, new ClientText(MConst.CCLogin, "名字已存在，请换一个名字!"));
                return false;
            }
            return true;
        }
        PacketManager.send(session, new ClientText(MConst.CCLogin, "名字不能为空!"));
        return false;
    }

    private boolean isRoleIdValid(AccountRow accountRow, long roleId) {
        List<AccountRole> accountRoleList = accountRow.getRelativeRoleList();
        String roleIdStr = Long.toString(roleId);
        for (int i = 0, len = accountRoleList.size(); i < len; i++) {
            if (accountRoleList.get(i).getRoleId().equals(roleIdStr)) {
                return true;
            }
        }
        return false;
    }

    private boolean isContainDirtyWords(String str_) {
//        return StringUtil.hasSensitiveWordExt1(str_);
        return DirtyWords.checkName(str_);
    }

    @Override
    public short getType() {
        return LoginPacketSet.C_CREATE_ROLE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    private void addToSpecialAccount(String account, long roleId) {
        if (SpecialAccountManager.isSpecialAccount(account)) {
            SpecialAccountManager.addRoleToAccount(account, roleId);
        }
    }
}
