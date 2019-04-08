package com.stars.modules.dungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.DungeonPacketSet;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/1.
 */
public class ServerProduceDungeon extends PlayerPacket {
    private byte produceDungeonType;// 进入产出副本类型
    private int dailyId;

    @Override
    public void execPacket(Player player) {
        ProduceDungeonVo vo;
        for(Map.Entry<Byte,Map<Integer,ProduceDungeonVo>> entry: DungeonManager.produceDungeonVoMap.entrySet()){
            for(Map.Entry<Integer,ProduceDungeonVo> voEntry:entry.getValue().entrySet()){
                vo = voEntry.getValue();
                if(vo.getDailyId()==this.dailyId){
                    this.produceDungeonType = entry.getKey();
                }
            }
        }
        DungeonModule dungeonModule = (DungeonModule) module(MConst.Dungeon);
        dungeonModule.sendProduceDungeonVo(produceDungeonType);
    }

    @Override
    public short getType() {
        return DungeonPacketSet.S_PRODUCEDUNGEON;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.dailyId = buff.readByte();
    }
}
