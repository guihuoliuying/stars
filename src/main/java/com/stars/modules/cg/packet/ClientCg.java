package com.stars.modules.cg.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.cg.CgPacketSet;
import com.stars.modules.cg.userdata.RoleCg;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class ClientCg extends PlayerPacket {

    private Map<String, RoleCg> map;

    public ClientCg() {
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return CgPacketSet.C_CG;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        short size = (short) (map == null ? 0 : map.size());
        buff.writeShort(size);
        if (size == 0) return;
        for (RoleCg roleCg : map.values()) {
            roleCg.writeToBuff(buff);
        }
    }

    public void setMap(Map<String, RoleCg> map) {
        this.map = map;
    }
}