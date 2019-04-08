package com.stars.modules.skytower.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.skytower.SkyTowerManager;
import com.stars.modules.skytower.SkyTowerPacketSet;
import com.stars.modules.skytower.prodata.SkyTowerVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应客户端请求镇妖塔的vo数据;
 * Created by panzhenfeng on 2016/8/10.
 */
public class ClientSkyTowerVo  extends PlayerPacket {

    private byte type;
    private List<SkyTowerVo> skyTowerVoList = new ArrayList<>();

    public ClientSkyTowerVo() {

    }

    public ClientSkyTowerVo(byte type) {
        this.type = type;
    }

    public void addSkyTowerVo(int skytowerId){
        SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById(skytowerId);
        addSkyTowerVo(skyTowerVo);
    }

    public void addSkyTowerVo(SkyTowerVo skyTowerVo){
        if(skyTowerVo != null){
            skyTowerVoList.add(skyTowerVo);
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SkyTowerPacketSet.C_SKYTOWER_VO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        int skyTowerVoSize = skyTowerVoList.size();
        buff.writeInt(skyTowerVoSize);
        for(int i = 0; i<skyTowerVoSize; i++){
            skyTowerVoList.get(i).writeBuff(buff);
        }
        buff.writeByte(this.type);
    }
}