package com.stars.modules.chat.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.chat.ChatPacketSet;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.chat.ChatMessage;
import com.stars.util.LogUtil;

public class ServerChatMessage extends PlayerPacket {

    private byte channel;
    private long receiver;
    private String content;
    private byte containsObject; // 是否有道具/表情

    public ServerChatMessage() {

    }

    @Override
    public short getType() {
        return ChatPacketSet.Server_ChatMessage;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        // TODO Auto-generated method stub

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        channel = buff.readByte();
//		senderId = buff.readLong();
//		senderName = buff.readString();
        receiver = Long.parseLong(buff.readString());
        content = buff.readString();
        containsObject = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", ChatPacketSet.Server_ChatMessage));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        ChatMessage cm = new ChatMessage();
        cm.setChannel(channel);
        cm.setContent(content);
        cm.setReceiver(receiver);
        cm.setContainsObject(containsObject == Packet.TRUE);
        cm.setTimestamp(System.currentTimeMillis());
//		cm.setServerId(RpcUtil.getServerIdFromRoleId(getRoleId()));
//		cm.setSenderId(senderId);
//		cm.setSenderName(senderName);
        ChatModule module = (ChatModule) module(MConst.Chat);
        module.chatMessage(cm);
    }
}
