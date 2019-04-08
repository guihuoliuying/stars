package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gem.GemPacketSet;
import com.stars.modules.gem.userdata.RoleEquipmentGem;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 返回客户端关于玩家的所有装备强化信息;
 * Created by panzhenfeng on 2016/6/29.
 */
public class ClientRoleGemInfo extends PlayerPacket {
    private RoleEquipmentGem roleGemData = null;

    public ClientRoleGemInfo() {

    }

    public void setData(RoleEquipmentGem roleGemData){
        this.roleGemData = roleGemData;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GemPacketSet.C_ROLE_GEM_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        this.roleGemData.writeToBuff(buff);
    }
}