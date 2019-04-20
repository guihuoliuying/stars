package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;

/**
 * Created by zhaowenshuo on 2016/8/13.
 */
public class ServerBlacker extends PlayerPacket {

    public static final byte SUBTYPE_LIST = 0x00;

    //    public static final byte SUBTYPE_ADD = 0x10; // 增加
    public static final byte SUBTYPE_ADD = 0x1; // 增加
    public static final byte SUBTYPE_DELETE = 0x2; // 删除

    private byte subtype;
    private long blackerId;

    @Override
    public void execPacket(Player player) {
        switch (subtype) {
            case SUBTYPE_LIST:
                ServiceHelper.friendService().sendBlackList(getRoleId());
                break;
            case SUBTYPE_ADD:
                ServiceHelper.friendService().addToBlackList(getRoleId(), blackerId);
                break;
            case SUBTYPE_DELETE:
                ServiceHelper.friendService().removeFromBlackList(getRoleId(), blackerId);
                break;
        }
    }

    @Override
    public short getType() {
        return FriendPacketSet.S_BLACKER;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case SUBTYPE_ADD:
            case SUBTYPE_DELETE:
                blackerId = Long.parseLong(buff.readString());
                break;
        }
    }
}
