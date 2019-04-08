package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by daiyaorong on 2017/1/3.
 */
public class ServerAutoFlag extends PlayerPacket {
    private byte flag;
    private byte stageType;

    @Override
    public short getType() {
        return ScenePacketSet.S_AUTOFLAG;
    }

    @Override
    public void execPacket(Player player) {
        RoleModule rm = module(MConst.Role);
        rm.setAutoFightFlag(this.flag, this.stageType);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.flag = buff.readByte();
        this.stageType = buff.readByte();
    }
}
