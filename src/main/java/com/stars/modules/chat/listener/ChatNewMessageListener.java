package com.stars.modules.chat.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.MConst;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.chat.event.ChatNewMessageEvent;
import com.stars.modules.redpoint.RedPointConst;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class ChatNewMessageListener implements EventListener {
    private ChatModule module;
    public ChatNewMessageListener(ChatModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        ChatNewMessageEvent chatNewMessageEvent = (ChatNewMessageEvent) event;
        module.addChatList(chatNewMessageEvent.getSenderId());
        module.signCalRedPoint(MConst.Chat, RedPointConst.FRIEND_CHAT);
    }
}
