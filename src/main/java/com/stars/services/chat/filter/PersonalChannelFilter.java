package com.stars.services.chat.filter;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.chat.event.ChatNewMessageEvent;
import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceUtil;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;

public class PersonalChannelFilter extends ChatFilter {
	
	public PersonalChannelFilter(String flag, ChatServiceActor serviceActor){
		super();
		setFlag(flag);
		setServiceActor(serviceActor);
	}

	@Override
	public Object filter(Object object) {
		if (object instanceof ChatMessage) {
			ChatMessage chatMessage = (ChatMessage)object;
			if (chatMessage.getChannel() != ChatManager.CHANNEL_PERSONAL) {
				return chatMessage;
			}
			if (getServiceActor().isBlacker(chatMessage.getReceiver(), chatMessage.getSenderId())) {
				ServiceUtil.sendText(chatMessage.getSenderId(), "friend_applytips_blacklist_other");
				return null;
			}
			if (getServiceActor().isBlacker(chatMessage.getSenderId(), chatMessage.getReceiver())) {
				ServiceUtil.sendText(chatMessage.getSenderId(), "friend_applytips_blacklist_self");
				return null;
			}
			ServiceHelper.roleService().notice(chatMessage.getReceiver(),new ChatNewMessageEvent(chatMessage.getSenderId()));
			ClientChatMessage cm = new ClientChatMessage(chatMessage);
			ServiceHelper.chatService().collectPersonalOfflineChatMsg(chatMessage);
			PlayerUtil.send(chatMessage.getSenderId(), cm);
			ServiceHelper.friendService().updateContacts(chatMessage.getSenderId(), chatMessage.getReceiver());
			ServiceHelper.friendService().updateContacts(chatMessage.getReceiver(), chatMessage.getSenderId());
			return null;
		}
		return object;
	}

}
