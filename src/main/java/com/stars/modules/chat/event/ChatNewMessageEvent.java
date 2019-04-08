package com.stars.modules.chat.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class ChatNewMessageEvent extends Event {
    private long senderId;
    public ChatNewMessageEvent(long senderId){this.senderId=senderId;}
    public long getSenderId(){return senderId;}
}
