package com.stars.modules.skytower.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.skytower.SkyTowerPacketSet;
import com.stars.modules.skytower.recordmap.RecordMapSkyTower;
import com.stars.network.server.buffer.NewByteBuffer;


/**
 * 响应客户端请求的镇妖塔数据;
 * Created by panzhenfeng on 2016/8/10.
 */
public class ClientSkyTowerInfo extends PlayerPacket {

    private RecordMapSkyTower roleSkyTower;
    public ClientSkyTowerInfo(){}

    public ClientSkyTowerInfo(RecordMapSkyTower roleSkyTower) {
        this.roleSkyTower = roleSkyTower;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SkyTowerPacketSet.C_SKYTOWER_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        roleSkyTower.writeToBuff(buff);
    }

}