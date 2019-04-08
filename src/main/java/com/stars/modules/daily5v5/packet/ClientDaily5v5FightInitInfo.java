package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

public class ClientDaily5v5FightInitInfo extends PlayerPacket {
	
	private Map<String, EliteFightTower> towerMap;

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Client_Daily5v5FightInitInfo;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(towerMap.size()); // 大小
        for (EliteFightTower tower : towerMap.values()) {
            buff.writeString(tower.getUid()); // 唯一id
            buff.writeByte(tower.getCamp()); // 阵营
            buff.writeByte(tower.getType()); // 类型
            buff.writeInt(tower.getMaxHp()); // 最大血量
            buff.writeString(tower.getPos()); // 位置
        }
	}

	public Map<String, EliteFightTower> getTowerMap() {
		return towerMap;
	}

	public void setTowerMap(Map<String, EliteFightTower> towerMap) {
		this.towerMap = towerMap;
	}

}
