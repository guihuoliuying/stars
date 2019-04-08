package com.stars.modules.demologin.packet;

import com.stars.AccountRow;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.modules.demologin.message.LogExitMessage;
import com.stars.modules.demologin.message.OfflineMsg;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2016/11/18.
 */
public class ServerLogout extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        switchRole(player);
    }

    @Override
    public short getType() {
        return LoginPacketSet.S_LOGOUT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    private void switchRole(Player player){
        LoginModule loginModule = (LoginModule) moduleMap().get("login");
        String account = loginModule.getAccount();
        long roleId=0;
        try {
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account,null);
            if(accountRow == null) return;
            roleId  = accountRow.getCurrentRoleId();
            if (roleId > 0) {
                com.stars.util.LogUtil.info("注销处理, account={}, roleId={}", account, roleId);
                player.tell(new OfflineMsg(roleId, session), player);
                session.setRoleId(0);
            }
            //到这里说明这个会话是合法的，加个标识;
            getSession().putAttribute("serverloginSession", "true");
            getSession().setAccount(account);

            ClientLogout clientLogout = new ClientLogout();
            send(clientLogout);
            //这里下发角色列表信息;
            ClientAccountRoleList clientAccountRoleList1 = new ClientAccountRoleList(accountRow);
            send(clientAccountRoleList1);
//            loginModule.logExit();
            player.tell(new LogExitMessage(), null);
        } catch (Throwable t) {
            LogUtil.error("登录流程出错，account=" + account + ", roleId=" + roleId, t);
            com.stars.network.server.packet.PacketManager.send(session, new ClientText("请求异常"));
            PacketManager.closeFrontend(session);
            t.printStackTrace();
        }
    }

}
