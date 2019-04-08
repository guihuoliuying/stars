package com.stars.multiserver.chat;


import com.stars.services.chat.ChatService;
import com.stars.services.multicommon.MultiCommonService;
import com.stars.services.role.RoleService;

/**
 * Created by chenkeyu on 2016/11/8.
 */
public class ChatRpcHelper {

    static ChatService chatService;
    static RoleService roleService;
    static MultiCommonService multiCommonService;

    public static MultiCommonService getMultiCommonService() {
        return multiCommonService;
    }

    public static ChatService getChatService(){
    	return chatService;
    }

    public static RoleService getRoleService() {
        return roleService;
    }
}
