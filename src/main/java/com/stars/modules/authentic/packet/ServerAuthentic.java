package com.stars.modules.authentic.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.authentic.AuthenticModule;
import com.stars.modules.authentic.AuthenticPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2016/12/23.
 */
public class ServerAuthentic extends PlayerPacket {
    private static final byte money = 0x00;//金币鉴宝
    private static final byte gold = 0x01;//元宝鉴宝
    private static final byte view = 0x02;//打开界面

    private byte subtype;
    private int times;

    @Override
    public void execPacket(Player player) {
        AuthenticModule module = module(MConst.Authentic);
        switch (subtype) {
            case money:
                module.moneyAuthentic(times);
                break;
            case gold:
                module.goldAuthentic(times);
                break;
            case view:
                module.flushRoleAuth();
                break;
        }
    }

    @Override
    public short getType() {
        return AuthenticPacketSet.S_AUTHENTIC;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        if (subtype != view) {
            times = buff.readInt();
        }
    }
}
