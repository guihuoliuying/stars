package com.stars.services.chat.filter;

import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;

/**
 * Created by zhaowenshuo on 2017/4/14.
 */
public class TeamInvitationChannelFilter extends WorldChannelFilter {

    public TeamInvitationChannelFilter(String flag, ChatServiceActor serviceActor) {
        super(flag, serviceActor);
    }

    @Override
    public Object filter0(Object object) {
        if (object instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage)object;
            if (chatMessage.getChannel() != ChatManager.CHANNEL_TEAM_INVITATION) {
                return chatMessage;
            }
            this.gotOnMessageCar(chatMessage);
            return null;
        }
        return object;
    }
}
