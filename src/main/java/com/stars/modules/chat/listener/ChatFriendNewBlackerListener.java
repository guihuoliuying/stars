package com.stars.modules.chat.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.friend.event.FriendNewBlackerEvent;
import com.stars.services.ServiceHelper;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class ChatFriendNewBlackerListener extends AbstractEventListener<ChatModule> {

    public ChatFriendNewBlackerListener(ChatModule module) {
        super(module);
    }


    @Override
    public void onEvent(Event event) {
        FriendNewBlackerEvent blackerEvent = (FriendNewBlackerEvent) event;
        ServiceHelper.chatService().addChatBlacker(module().id(), blackerEvent.getBlackerId());
    }
}
