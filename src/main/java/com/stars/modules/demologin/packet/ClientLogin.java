package com.stars.modules.demologin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/6/17.
 */
public class ClientLogin extends PlayerPacket {
    private boolean success;// 是否成功

    public ClientLogin() {
    }

    public ClientLogin(boolean success) {
        this.success = success;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LoginPacketSet.C_DEMO_LOGIN;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte((byte) (success ? 1 : 0));
    }
}
