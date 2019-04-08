package com.stars.modules.authentic.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.authentic.AuthenticPacketSet;
import com.stars.modules.authentic.userdata.RoleAuthenticPo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2016/12/29.
 */
public class ClientRoleAuth extends PlayerPacket {
    private RoleAuthenticPo roleAuthenticPo;
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        roleAuthenticPo.writeToBuff(buff);
    }

    @Override
    public short getType() {
        return AuthenticPacketSet.C_ROLEAUTH;
    }

    public void setRoleAuthenticPo(RoleAuthenticPo roleAuthenticPo) {
        this.roleAuthenticPo = roleAuthenticPo;
    }
}
