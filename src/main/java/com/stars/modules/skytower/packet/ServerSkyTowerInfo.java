package com.stars.modules.skytower.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.skytower.SkyTowerConstant;
import com.stars.modules.skytower.SkyTowerModule;
import com.stars.modules.skytower.SkyTowerPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求自身的镇妖塔数据;
 * Created by panzhenfeng on 2016/8/10.
 */
public class ServerSkyTowerInfo  extends PlayerPacket {
    private byte type ;
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        type = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        SkyTowerModule skyTowerModule = (SkyTowerModule)this.module(MConst.SkyTower);
        switch (type){
            case SkyTowerConstant.REQUEST_INFO_TYPE://请求信息;
                //do nothing;
                break;
            default:
                skyTowerModule.requestGetAwards(type);
                break;
        }
        skyTowerModule.syncToClientSkyTowerInfo();
    }

    @Override
    public short getType() {
        return SkyTowerPacketSet.S_SKYTOWER_INFO;
    }
}
