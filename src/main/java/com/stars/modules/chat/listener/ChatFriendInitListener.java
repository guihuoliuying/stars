package com.stars.modules.chat.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.friend.event.FriendInitEvent;
import com.stars.services.ServiceHelper;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class ChatFriendInitListener extends AbstractEventListener<ChatModule> {

    public ChatFriendInitListener(ChatModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FriendInitEvent initEvent = (FriendInitEvent) event;
        ServiceHelper.chatService().putChatBlackers(module().id(), initEvent.getBlackList());
    }
}
