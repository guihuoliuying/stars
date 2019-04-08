package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.gem.GemModule;
import com.stars.modules.gem.GemPacketSet;
import com.stars.modules.role.RoleModule;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求装备提升操作;
 * Created by panzhenfeng on 2016/6/30.
 */
public class ServerGemTishenOpr extends PlayerPacket {

    private byte equipmentType = -1;
    private byte tishenOprType = -1;
    private String extendParam;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        tishenOprType = buff.readByte();
        equipmentType = buff.readByte();
        extendParam = buff.readString();
    }

    @Override
    public void execPacket(Player player) {
        GemModule gemModule = (GemModule) this.module(MConst.GEM);
        RoleModule roleModule = (RoleModule) this.module(MConst.Role);
        gemModule.requestTishen(equipmentType, tishenOprType, roleModule.getRoleRow().getJobId(), extendParam);
    }

    @Override
    public short getType() {
        return GemPacketSet.S_GEM_TISHEN_OPR;
    }
}
