package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.Map;

public class ClientDaily5v5FightUpdateInfo extends PlayerPacket {
	
	private int camp1Morale;
    private int camp1BuffId;
    private int camp1BuffLevel;
    private long camp1Points;
    private int camp2Morale;
    private int camp2BuffId;
    private int camp2BuffLevel;
    private long camp2Points;
    private Map<String, EliteFightTower> towerMap;
    
    public ClientDaily5v5FightUpdateInfo() {
		// TODO Auto-generated constructor stub
	}

	public ClientDaily5v5FightUpdateInfo(int camp1Morale, int camp1BuffId, long camp1Points,
			int camp2Morale, int camp2BuffId, long camp2Points,
			Map<String, EliteFightTower> towerMap) {
		super();
		this.camp1Morale = camp1Morale;
		this.camp1BuffId = camp1BuffId;
		this.camp1Points = camp1Points;
		this.camp2Morale = camp2Morale;
		this.camp2BuffId = camp2BuffId;
		this.camp2Points = camp2Points;
		this.towerMap = towerMap;
	}
	
	@Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(camp1Morale); // 阵营1的士气
        buff.writeInt(camp1BuffId);//阵营1buffid
//        buff.writeInt(camp1BuffLevel);
        buff.writeInt(1);
        buff.writeString(Long.toString(camp1Points));//阵营1总积分
        buff.writeInt(camp2Morale); // 阵营2的士气
        buff.writeInt(camp2BuffId);
//        buff.writeInt(camp2BuffLevel);
        buff.writeInt(1);
        buff.writeString(Long.toString(camp2Points));//阵营2总积分
        buff.writeInt(towerMap.size()); // 大小
        LogUtil.info("daily5v5|camp1Morale:{},camp1BuffId:{},camp1Points:{},camp2Morale:{},camp2BuffId:{},camp2Points:{}",
                camp1Morale, camp1BuffId, camp1Points, camp2Morale, camp2BuffId, camp2Points);
        for (EliteFightTower tower : towerMap.values()) {
            buff.writeString(tower.getUid()); // 塔的唯一id
            buff.writeInt(tower.getHp()); // 塔的血量
        }

    }

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Client_Daily5v5FightUpdateInfo;
	}

}
