package com.stars.multiserver;

import com.stars.multiserver.chat.chatrpcservice.RMChatService;
import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.multiserver.payServer.RMPayServerService;
import com.stars.multiserver.rpctest.echo.EchoService;
import com.stars.services.fightbase.FightBaseService;

/**
 * Created by zhaowenshuo on 2016/11/4.
 */
public class MainRpcHelper {

    static EchoService echoService;
    static FightBaseService fightBaseService;
    static RMChatService rmChatService;
    static RMFSManagerService rmfsManagerService;
    static RMPayServerService rmPayServerService;

    public static EchoService echoService() {
        return echoService;
    }

    public static FightBaseService fightBaseService() {
        return fightBaseService;
    }

    public static RMChatService rmChatService() {
        return rmChatService;
    }

    public static RMFSManagerService rmfsManagerService() {
        return rmfsManagerService;
    }

    public static RMPayServerService rmPayServerService() {
        return rmPayServerService;
    }

}
