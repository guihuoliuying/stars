package com.stars.core.gmpacket;


import com.stars.core.gmpacket.util.QueryHotUpdateInfoGm;
import com.stars.server.main.gmpacket.GmPacketManager;

/**
 * Created by liuyuheng on 2016/12/9.
 */
public class GmPacketDefine {
    // 运维GM接口号
    public static int KICK_OFF_PLAYER = 1002;// 踢玩家下线
    public static int SAVE_DATA = 1003;// 保存游戏数据
    public static int QUERY_HOT_UPDATE_INFO = 1055;

    public static int HOTUPDATE_COMMAND = 9998;    //热更commManager
    public static int GM_COMMAND = 20020; // gm命令执行


    public static void regHandler() {

    }

    public static void reg() {
        GmPacketManager.regGmRequestHandler(KICK_OFF_PLAYER, KickOffPlayerGm.class);
        GmPacketManager.regGmRequestHandler(SAVE_DATA, SaveDataGm.class);
        GmPacketManager.regGmRequestHandler(GM_COMMAND, CommandGm.class);
        GmPacketManager.regGmRequestHandler(HOTUPDATE_COMMAND, HotUpdateCommGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_HOT_UPDATE_INFO, QueryHotUpdateInfoGm.class);
    }
}
