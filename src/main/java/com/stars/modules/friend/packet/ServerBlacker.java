package com.stars.modules.friend.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

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
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FriendPacketSet.S_BLACKER));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
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
