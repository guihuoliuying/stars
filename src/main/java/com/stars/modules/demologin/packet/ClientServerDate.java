package com.stars.modules.demologin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017/2/13 9:37
 */
public class ClientServerDate extends PlayerPacket {

    private int openServerDate;

    public ClientServerDate(){}

    public ClientServerDate(int openServerDate) {
        this.openServerDate = openServerDate;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LoginPacketSet.C_SERVER_DATE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(openServerDate);
    }
}
