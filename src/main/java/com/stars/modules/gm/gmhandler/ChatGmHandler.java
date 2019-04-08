package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/7/22.
 */
public class ChatGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        String message = "";
        if (args != null && args.length > 0) {
            message = args[0];
        }
        ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_WORLD, 0L, 0L, message, false);
    }
}
