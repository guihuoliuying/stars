package com.stars.modules.chat.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.friend.event.FriendDelBlackerEvent;
import com.stars.services.ServiceHelper;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class ChatFriendDelBlackerListener extends AbstractEventListener<ChatModule> {

    public ChatFriendDelBlackerListener(ChatModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FriendDelBlackerEvent blackerEvent = (FriendDelBlackerEvent) event;
        ServiceHelper.chatService().removeChatBlacker(module().id(), blackerEvent.getBlackerId());
    }
}
