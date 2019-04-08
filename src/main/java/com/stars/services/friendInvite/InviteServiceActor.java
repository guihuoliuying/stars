package com.stars.services.friendInvite;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.core.persist.DbRowDao;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.modules.friendInvite.userdata.RoleBeInvitePo;
import com.stars.modules.friendInvite.userdata.RoleInvitePo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Properties;

/**
 * Created by chenxie on 2017/6/12.
 */
public class InviteServiceActor extends ServiceActor implements InviteService {

    private DbRowDao dao;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.InviteService, this);
        dao = new DbRowDao("invite", DBUtil.DB_COMMON);
    }

    @Override
    public void insert(DbRow dbRow) {
        dao.insert(dbRow);
        dao.flush();
    }

    @Override
    public void update(DbRow dbRow) {
        dao.update(dbRow);
        dao.flush();
    }

    @Override
    public void bindInviteCode(RoleInvitePo roleInvitePoFrom, RoleBeInvitePo roleBeInvitePo) {
        dao.update(roleInvitePoFrom);
        dao.update(roleBeInvitePo);
        dao.flush();
        // 通知邀请方
//        ServiceHelper.roleService().notice(roleInvitePoFrom.getServerId(), roleInvitePoFrom.getRoleId(), new BindInviteCodeEvent(roleInvitePoFrom.getRoleId()));
        BootstrapConfig config = ServerManager.getServer().getConfig();
        Properties props = config.getProps().get("rmchat");
        int serverId = Integer.parseInt(props.getProperty("serverId"));
        LogUtil.info("跨服通知邀请方开始,聊天服ID:{}", serverId);
        MainRpcHelper.rmChatService().bindInviteCode(
                serverId, roleInvitePoFrom.getServerId(), roleInvitePoFrom.getRoleId(), roleBeInvitePo.getRoleId());
        LogUtil.info("跨服通知邀请方结束");
    }

    @Override
    public void printState() {

    }

    @Override
    public void save() {
//        dao.flush();
    }

}
