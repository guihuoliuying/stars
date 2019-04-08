package com.stars.modules.newsignin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newsignin.NewSigninPacketSet;
import com.stars.modules.newsignin.userdata.RoleSigninPo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017/2/6 10:48
 */
public class ClientRoleSignin extends PlayerPacket {
    private RoleSigninPo roleSigninPo;
    private int isOpen;
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewSigninPacketSet.C_SignIn;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(isOpen);
        roleSigninPo.writeToBuffer(buff);
    }

    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }

    public void setRoleSigninPo(RoleSigninPo roleSigninPo) {
        this.roleSigninPo = roleSigninPo;
    }
}
