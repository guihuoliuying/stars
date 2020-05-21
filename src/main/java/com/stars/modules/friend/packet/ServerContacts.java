package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

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
