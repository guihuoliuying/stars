package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.gem.GemManager;
import com.stars.modules.gem.GemModule;
import com.stars.modules.gem.GemPacketSet;
import com.stars.modules.gem.prodata.GemLevelVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzhenfeng on 2016/11/25.
 */
public class ServerGemAllGemComposeInfo extends PlayerPacket {
    private List<Integer> gemLevelIdList = new ArrayList<>();

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        int gemLevelIdCount = buff.readInt();
        for(int i = 0; i<gemLevelIdCount; i++){
            gemLevelIdList.add(buff.readInt());
        }
    }

    @Override
    public void execPacket(Player player) {
        ClientGemAllComposeInfo clientGemAllComposeInfo = new ClientGemAllComposeInfo();
        int waitToComposeId = 0;
        GemLevelVo waitToComposeGemLevelVo = null;
        int canComposeId = 0;
        int canComposeCount = 0;
        GemModule gemModule = (GemModule)module(MConst.GEM);
        for(int i = 0, len = gemLevelIdList.size(); i<len; i++){
            waitToComposeId = gemLevelIdList.get(i);
            waitToComposeGemLevelVo = GemManager.getNextGemLevelId(waitToComposeId);
            if(waitToComposeGemLevelVo != null){
                canComposeId = waitToComposeGemLevelVo.getItemId();
                canComposeCount = gemModule.getCanComposeGemCount(canComposeId);
                clientGemAllComposeInfo.addCanComposedGemLevelIdInfo(waitToComposeId, canComposeId, canComposeCount);
            }
        }
        PlayerUtil.send(getRoleId(), clientGemAllComposeInfo);
    }

    @Override
    public short getType() {
        return GemPacketSet.S_EQUIPMENT_ALLGEM_COMPOSEINFO;
    }
}