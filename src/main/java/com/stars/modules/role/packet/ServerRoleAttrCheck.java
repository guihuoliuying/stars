package com.stars.modules.role.packet;

import com.stars.core.attr.Attribute;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.RolePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by daiyaorong on 2017/3/28.
 */
public class ServerRoleAttrCheck extends PlayerPacket {

    private Attribute attribute;

    public ServerRoleAttrCheck(){
        attribute = new Attribute();
    }

    public short getType() {
        return RolePacketSet.S_ROLEATTR_CHECK;
    }

    public void readFromBuffer(NewByteBuffer buff) {
        attribute.readFightAtrFromBuffer(buff);
    }

    @Override
    public void execPacket(Player player) {
        ((RoleModule)module(MConst.Role)).roleAttrCheckReq(attribute);
    }
}
