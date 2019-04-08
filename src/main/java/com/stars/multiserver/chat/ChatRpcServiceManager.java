package com.stars.multiserver.chat;

import com.stars.multiserver.chat.chatrpcservice.RMChatService;
import com.stars.multiserver.chat.chatrpcservice.RMChatServiceActor;
import com.stars.multiserver.daregod.DareGodService;
import com.stars.multiserver.daregod.DareGodServiceActor;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceManager;

/**
 * Created by chenkeyu on 2016/11/8.
 */
public class ChatRpcServiceManager extends ServiceManager {
    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit(SConst.RMChatService, new RMChatServiceActor());
        registerAndInit(SConst.DareGodService, newService(new DareGodServiceActor()));
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(RMChatService.class, getService(SConst.RMChatService));
        exportService(DareGodService.class, getService(SConst.DareGodService));
        initRpcHelper(ChatRpcHelper.class);
    }

    @Override
    public void runScheduledJob() throws Throwable {
        ServiceHelper.dareGodService().save();
    }

}
