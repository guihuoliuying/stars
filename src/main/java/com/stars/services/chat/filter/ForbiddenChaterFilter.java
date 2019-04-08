package com.stars.services.chat.filter;

import com.stars.services.ServiceUtil;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;
import com.stars.services.chat.userdata.ForbiddenChater;

/**
 * Created by liuyuheng on 2017/1/16.
 */
public class ForbiddenChaterFilter extends ChatFilter {

    public ForbiddenChaterFilter(String flag, ChatServiceActor serviceActor) {
        setFlag(flag);
        setServiceActor(serviceActor);
    }

    @Override
    public Object filter(Object object) {
        if (object instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage)object;
            if (getServiceActor().isForbidden(chatMessage.getSenderId())) {
                ForbiddenChater forbiddenChater = getServiceActor().forbiddenChaters.get(chatMessage.getSenderId());
                ServiceUtil.sendText(chatMessage.getSenderId(), "chat_shieldfindservice", forbiddenChater.getReason());
                return null;
            }
        }
        return object;
    }
}
