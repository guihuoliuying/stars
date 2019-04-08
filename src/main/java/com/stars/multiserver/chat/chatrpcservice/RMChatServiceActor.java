package com.stars.multiserver.chat.chatrpcservice;

import com.stars.modules.friendInvite.event.BindInviteCodeEvent;
import com.stars.multiserver.chat.ChatRpcHelper;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.services.chat.ChatMessage;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.HashSet;

/**
 * Created by chenkeyu on 2016/11/8.
 */
public class RMChatServiceActor extends ServiceActor implements RMChatService {

	private HashSet<Integer>chatServers;

	public RMChatServiceActor(){
		chatServers = new HashSet<Integer>();
	}

    @Override
    public void init() throws Throwable {
    	ServiceSystem.getOrAdd(SConst.RMChatService, this);
    }

	@Override
	public void printState() {

	}

	@Override
    public void chatContent(int serverId, ChatMessage cm) {
        if (chatServers.size() <= 0) {
			return;
		}
        for (Integer integer : chatServers) {
        	if (integer == cm.getServerId()) {
				continue;
			}
            try {
                ChatRpcHelper.getChatService().rmChatMessage(integer, cm);
            } catch (Throwable cause) {
                LogUtil.error("跨服聊天异常, " + cause.getMessage());
            }
		}
    }

    @Override
    public void registerChatServer(int serverId,int comFrom) {
    	LogUtil.info("server:"+comFrom+" register rmchat");
    	chatServers.add(comFrom);
    }

    @Override
    public void bindInviteCode(int serverId, int mainServerId, long inviterId, long inviteeId) {
        LogUtil.info("跨服通知邀请方方法开始|serverId:{}|inviterId:{}|inviteeId:{}", serverId, inviterId, inviteeId);
        if (chatServers.size() <= 0) {
            return;
        }
        for (Integer id : chatServers) {
            if(id == mainServerId){
                LogUtil.info("跨服通知邀请方方法成功");
                ChatRpcHelper.getRoleService().notice(mainServerId, inviterId, new BindInviteCodeEvent(inviterId, inviteeId));
            }
        }
        LogUtil.info("跨服通知邀请方方法结束");
    }

}
