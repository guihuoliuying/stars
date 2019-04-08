package com.stars.services.chat.filter;

import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.services.ServiceUtil;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;

public class SystemChannelFilter extends ChatFilter {

	public SystemChannelFilter(String flag, ChatServiceActor serviceActor) {
		super();
		setFlag(flag);
		setServiceActor(serviceActor);
	}

	@Override
	public Object filter(Object object) {
		if (object instanceof ChatMessage) {
			ChatMessage chatMessage = (ChatMessage)object;
			if (chatMessage.getChannel() != ChatManager.CHANNEL_SYSTEM) {
				return chatMessage;
			}
			ClientChatMessage cm = new ClientChatMessage((ChatMessage)object);
			ServiceUtil.sendPacketToOnline(cm, null);
			//this.gotOnMessageCar(chatMessage);
			return null;
		}
		return object;
	}

}
