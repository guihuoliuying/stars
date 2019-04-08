package com.stars.modules.poem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.poem.PoemPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;


/**
 * Created by gaopeidian on 2017/1/9.
 */
public class ClientPoemBoss extends PlayerPacket {
    private DungeoninfoVo bossDungeonInfoVo;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return PoemPacketSet.C_POEM_BOSS;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(bossDungeonInfoVo.getDungeonId());//dungeonId
    	buff.writeInt(bossDungeonInfoVo.getWorldId());//第几章
    	buff.writeInt(bossDungeonInfoVo.getStep());//第几节
    	buff.writeString(bossDungeonInfoVo.getShowBoss());//boss模型
    	buff.writeInt(bossDungeonInfoVo.getShowBossScale());//boss模型scale
    	buff.writeInt(bossDungeonInfoVo.getRecommend());//推荐战力
    	buff.writeString(bossDungeonInfoVo.getFirstDrop());//首次掉落奖励
    	buff.writeString(bossDungeonInfoVo.getOriginText());//text
    	buff.writeString(bossDungeonInfoVo.getName());//关卡名
    }
    
    public void setBossDungeonInfoVo(DungeoninfoVo value){
    	this.bossDungeonInfoVo = value;
    }
}