package com.stars.modules.friend.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/8/11.
 */
public class ServerContacts extends PlayerPacket {
    public static final byte contactList = 0x00;//联系人列表
    public static final byte checkFriend = 0x01;//查看了某个联系人
    public static final byte isOpen = 0x02;//聊天窗口是否打开
    private byte subtype;
    private String friendId;
    private byte open;
    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FriendPacketSet.S_CONTACTS));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        ChatModule module = module(MConst.Chat);
        switch (subtype){
            case contactList:
                ServiceHelper.friendService().sendContactsList(getRoleId());
                break;
            case checkFriend:
                module.removeChatList(Long.valueOf(friendId));
                break;
            case isOpen:
                module.chatIsOpen(open);
                break;
        }

    }

    @Override
    public short getType() {
        return FriendPacketSet.S_CONTACTS;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype){
            case contactList:
                break;
            case checkFriend:
                friendId=buff.readString();
                break;
            case isOpen:
                open = buff.readByte();
        }
    }
}
