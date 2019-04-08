package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.gem.GemPacketSet;
import com.stars.modules.role.RoleModule;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求装备提升的vo数据;
 * Created by panzhenfeng on 2016/7/19.
 */
public class ServerGemTishenVo extends PlayerPacket {

    private byte equipmentType = -1;
    private int level = 0;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        level = buff.readInt();
        equipmentType = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        RoleModule roleModule = (RoleModule)this.module(MConst.Role);
        int jobId = roleModule.getRoleRow().getJobId();
        ClientGemTishenVo clientTishenVo = new ClientGemTishenVo();
        clientTishenVo.addGemLevelVo(level);
        PlayerUtil.send(getRoleId(), clientTishenVo);
    }

    @Override
    public short getType() {
        return GemPacketSet.S_EQUIPMENT_TISHEN_VO;
    }
}
