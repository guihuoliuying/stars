package com.stars.modules.familyactivities.invade.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.invade.FamilyInvadePacket;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/10/21.
 */
public class ClientFamilyInvadeNotice extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte START = 1;// 活动开始
    public static final byte END = 2;// 活动结束

    public ClientFamilyInvadeNotice() {
    }

    public ClientFamilyInvadeNotice(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyInvadePacket.C_INVADE_NOTICE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case START:
                break;
            case END:
                break;
        }
    }
}
