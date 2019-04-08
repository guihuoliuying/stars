package com.stars.services.chat.filter;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;

import java.util.Set;

public class FamilyChannelFilter extends ChatFilter {

    public FamilyChannelFilter(String flag, ChatServiceActor serviceActor) {
        super();
        setFlag(flag);
        setServiceActor(serviceActor);
    }

    @Override
	public Object filter(Object object) {
		if (object instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) object;
			if (msg.getChannel() != ChatManager.CHANNEL_FAMILY) {
				return msg;
			}
			ChatServiceActor actor = getServiceActor();
			long senderId = msg.getSenderId();
			long familyId = msg.getReceiver();
			Set<Long> memberSet = actor.getFamilyList(familyId);
			if (memberSet == null) {
//				ServiceUtil.sendText(senderId, "不存在家族");
				return null;
			}
			if (senderId != 0 && !memberSet.contains(senderId)) {
//				ServiceUtil.sendText(senderId, "你不属于该家族");
				return null;
			}
			ClientChatMessage packet = new ClientChatMessage(msg);
			for (long memberId : memberSet) {
				if (getServiceActor().isBlacker(msg.getReceiver(), msg.getSenderId())) {
					continue;
				}

				PlayerUtil.send(memberId, packet);
			}
			return null;
		}
		return object;
	}

}
