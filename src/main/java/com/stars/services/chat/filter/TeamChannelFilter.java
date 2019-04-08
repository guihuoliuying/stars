package com.stars.services.chat.filter;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.services.ServiceUtil;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;
import com.stars.util.LogUtil;

import java.util.Set;

/**
 * Created by wuyuxing on 2017/4/10.
 */
public class TeamChannelFilter extends ChatFilter {

    public TeamChannelFilter(String flag, ChatServiceActor serviceActor) {
        super();
        setFlag(flag);
        setServiceActor(serviceActor);
    }

    @Override
    public Object filter(Object object) {
        if (object instanceof ChatMessage) {
            ChatMessage msg = (ChatMessage) object;
            if (msg.getChannel() != ChatManager.CHANNEL_TEAM) {
                return msg;
            }
            ChatServiceActor actor = getServiceActor();
            long senderId = msg.getSenderId();
            long teamId = msg.getReceiver();
            Set<Long> memberSet = actor.getTeamList(teamId);
            LogUtil.info("队伍成员:{}", memberSet);
            if (memberSet == null) {
                ServiceUtil.sendText(senderId, "不存在队伍");
                return null;
            }
            if (senderId != 0 && !memberSet.contains(senderId)) {
                return null;
            }
            ClientChatMessage packet = new ClientChatMessage(msg);
            for (long memberId : memberSet) {
                if (getServiceActor().isBlacker(memberId, msg.getSenderId())) {
                    continue;
                }

                PlayerUtil.send(memberId, packet);
            }
            return null;
        }
        return object;
    }
}
