package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.FamilyAuth;

/**
 * Created by zhaowenshuo on 2016/8/30.
 */
public class ClientFamilyAuth extends PlayerPacket {

    private FamilyAuth auth;

    public ClientFamilyAuth() {
    }

    public ClientFamilyAuth(FamilyAuth auth) {
        this.auth = auth;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_AUTH;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toString(auth.getFamilyId())); // 家族id
        buff.writeString(auth.getFamilyName()); // 家族名字
        buff.writeByte(auth.getPost().getId()); // 家族职位
    }

}
