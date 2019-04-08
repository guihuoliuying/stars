package com.stars.modules.demologin.message;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginModule;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/6/20.
 */
public class BaseLoginMsg extends PlayerPacket {

    public BaseLoginMsg() {
    }

    @Override
    public short getType() {
        return 0;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket(Player player) {
        LoginModule module = (LoginModule) moduleMap().get("login");
        module.handle(this,player);
    }
}