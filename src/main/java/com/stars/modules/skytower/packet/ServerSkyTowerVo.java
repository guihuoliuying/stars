package com.stars.modules.skytower.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.skytower.SkyTowerConstant;
import com.stars.modules.skytower.SkyTowerManager;
import com.stars.modules.skytower.SkyTowerPacketSet;
import com.stars.modules.skytower.prodata.SkyTowerVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求镇妖塔的vo数据;
 * Created by panzhenfeng on 2016/8/10.
 */
public class ServerSkyTowerVo  extends PlayerPacket {
    private byte type;
    private String[] layerIdStrArr = null;
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        String tmpStr = buff.readString();
        layerIdStrArr = tmpStr.split("\\+");
        type = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        ClientSkyTowerVo clientSkyTowerVo = new ClientSkyTowerVo(type);
        SkyTowerVo skyTowerVo = null;
        int tmpRequestLayerId = 0;
        for(int i = 0, len = layerIdStrArr.length; i<len; i++){
            tmpRequestLayerId = Integer.parseInt(layerIdStrArr[i]);
            if(SkyTowerConstant.REQUEST_VO_NORMAL == type){
                clientSkyTowerVo.addSkyTowerVo(tmpRequestLayerId);
            }else if(SkyTowerConstant.REQUEST_VO_NEXT_CHALLENGE == type){
                //获取当前层级的下一等级含有挑战奖励的vo数据;
                skyTowerVo = SkyTowerManager.getNextChallengeSkyTowerVo(tmpRequestLayerId, false);
            }
            clientSkyTowerVo.addSkyTowerVo(skyTowerVo);
        }
        PlayerUtil.send(getRoleId(), clientSkyTowerVo);
    }

    @Override
    public short getType() {
        return SkyTowerPacketSet.S_SKYTOWER_VO;
    }
}
