package com.stars.multiserver.chat.chatrpcservice;

import com.stars.services.Service;
import com.stars.services.chat.ChatMessage;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2016/11/8.
 */
public interface RMChatService extends Service, ActorService {
	
	@AsyncInvocation
    void chatContent(int serverId,ChatMessage message);
	
	@AsyncInvocation
	void registerChatServer(int serverId,int comFrom);

    @AsyncInvocation
    void bindInviteCode(int serverId, int mainServerId, long inviterId, long inviteeId);
}
