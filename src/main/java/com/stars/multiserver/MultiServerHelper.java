package com.stars.multiserver;

import com.stars.bootstrap.ServerManager;
import com.stars.db.DBUtil;
import com.stars.modules.pk.packet.ModifyConnectorRoute;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MultiServerHelper {

    /**
     * 主服pvpservice连接到战斗服
     */
    public static String SOCKETCLIENT_FLAG_PVPSERVICE2FIGHTSERVER = "pvpserverice2fightserver";
    /**
     * 主服夺宝service连接到夺宝服
     */
    public static String SOCKETCLIENT_FLAG_LOOTSERVICE2LOOTSERVER = "lootservice2lootserver";
    /**
     * 夺宝服连接到战斗服
     */
    public static String SOCKETCLIENT_FLAG_LOOTSERVER2FIGHTSERVER = "lootserver2fightserver";

    public static void modifyConnectorRoute(long roleId, int aimServer) {
        com.stars.util.LogUtil.info("切换连接, roleId={}, toServerId={}", roleId, aimServer);
        ModifyConnectorRoute modifyConnectorRoute = new ModifyConnectorRoute();
        modifyConnectorRoute.setServerId(aimServer);
        modifyConnectorRoute.setRoleId(roleId);
        GameSession gs = SessionManager.getSessionMap().get(roleId);
        PacketManager.send(gs, modifyConnectorRoute);
    }

    public static int getServerId() {
        return com.stars.bootstrap.ServerManager.getServer().getConfig().getServerId();
    }

    public static int getDisplayServerId() {
        return com.stars.bootstrap.ServerManager.getServer().getConfig().getServerId() % 1000;
    }

    public static int getDisplayServerId(int serverId) {
        return serverId % 1000;
    }

    public static int getFightingMasterServer() {
        return Integer.valueOf(com.stars.bootstrap.ServerManager.getServer().getConfig().getProps().get("multi").getProperty("serverId"));
    }

    /**
     * 获取阵营服
     *
     * @return
     */
    public static int getCampServerId() {
        Properties campProperties = com.stars.bootstrap.ServerManager.getServer().getConfig().getProps().get("camp");
        String serverIdStr = campProperties.getProperty("serverId");
        return Integer.parseInt(serverIdStr);
    }

    public static int getChatServerId() {
        Properties campProperties = com.stars.bootstrap.ServerManager.getServer().getConfig().getProps().get("rmchat");
        String serverIdStr = campProperties.getProperty("serverId");
        return Integer.parseInt(serverIdStr);
    }

    public static boolean isFightingMasterServer() {
        return com.stars.bootstrap.ServerManager.getServer().getConfig().getBusinessName().endsWith("FightingMasterStartup");
    }

    public static String getServerName() {
        return I18n.get("common.serverName", getDisplayServerId());
//		return Integer.toString(getDisplayServerId());
    }

    public static String getServerName(int serverId) {
        return I18n.get("common.serverName", getDisplayServerId(serverId));
//		return Integer.toString(getDisplayServerId(serverId));
    }

    /**
     * 获取专区id
     * <p>
     * <p>
     * 1：自运营
     * 2：IOS
     * 3：硬核
     * 4：米宝
     *
     * @return
     */
    public static int getAreaId() {
        int serverId = getServerId();
        if (10000 < serverId && serverId < 20000) {
            if (10001 <= serverId && serverId <= 12999) {
                return 1;
            }
            if (13001 <= serverId && serverId <= 14999) {
                return 2;
            }
            if (15001 <= serverId && serverId <= 16999) {
                return 3;
            }
            if (17001 <= serverId && serverId <= 18999) {
                return 4;
            }
        } else {
//            throw new RuntimeException("游戏服id应该大于10000小于20000，请确认");
            LogUtil.info("获取区id警告：游戏服id应该大于10000小于20000，请确认,默认米宝：4");
            return 4;
        }
        return 0;
    }

    /**
     * @throws Exception 从数据库里加载公共服的配置
     */
    public static void loadPublicServerConfig() throws Exception {

//    	  `id` varchar(5) NOT NULL,
//    	  `type` varchar(64) NOT NULL,
//    	  `name` varchar(64) NOT NULL,
//    	  `ip` varchar(64) NOT NULL,
//    	  `port` varchar(5) NOT NULL
        List<com.stars.util._HashMap> list = DBUtil.queryList(DBUtil.DB_LOGIN, com.stars.util._HashMap.class, "select * from pserverinfo");
        ConcurrentHashMap<String, Properties> props = com.stars.bootstrap.ServerManager.getServer().getConfig().getProps();
        ConcurrentHashMap<String, Properties> pubProps = ServerManager.getServer().getConfig().getPubProps();
//    	 <serverName>服务1</serverName>
//         <serverId>1</serverId>
//         <serverIp>127.0.0.1</serverIp>
//         <serverPort>9004</serverPort>
        for (com.stars.util._HashMap _HashMap : list) {
            String key = _HashMap.getString("type");
            if (key.equals("fightServer") || key.equals("")) {
                continue;
            }

            Properties properties = new Properties();
            properties.put("serverId", _HashMap.getString("id"));
            properties.put("serverIp", _HashMap.getString("ip"));
            properties.put("serverPort", _HashMap.getString("port"));
            properties.put("serverType", key);
            props.put(key, properties);
            if (!key.equals("payserver") && !key.equals("payserver1")) {
                pubProps.put(key, properties);
            }
        }
    }
}
