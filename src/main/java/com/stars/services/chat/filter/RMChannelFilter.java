package com.stars.services.chat.filter;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.MainRpcHelper;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;

import java.util.Properties;

public class RMChannelFilter extends WorldChannelFilter {
	public RMChannelFilter(String flag,ChatServiceActor serviceActor){
		super(flag, serviceActor);
	}
	@Override
	public Object filter0(Object object) {
		if (object instanceof ChatMessage) {
			ChatMessage chatMessage = (ChatMessage)object;
			if (chatMessage.getChannel() != ChatManager.CHANNEL_RM) {
				return chatMessage;
			}
			BootstrapConfig config = ServerManager.getServer().getConfig();
			Properties props = config.getProps().get("rmchat");
			int serverId = Integer.parseInt(props.getProperty("serverId"));
			chatMessage.setServerId(config.getServerId());
			MainRpcHelper.rmChatService().chatContent(serverId, chatMessage);
			this.gotOnMessageCar(chatMessage);
			return null;
		}
		return object;
	}
	
	public void sendRMMessage(ChatMessage chatMessage){
		this.gotOnMessageCar(chatMessage);
	}
}
