package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

public class ClientDaily5v5TeamPoints extends PlayerPacket {
	
	private long myTeamPoints;
    private long enemyTeamPoints;
    
    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeLong(myTeamPoints);
        buff.writeLong(enemyTeamPoints);
        LogUtil.info("daily5v5|更新双方队伍积分数据|我方家族积分:{},敌方家族积分:{}", myTeamPoints, enemyTeamPoints);
    }

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Client_Daily5v5TeamPoints;
	}

	public long getMyTeamPoints() {
		return myTeamPoints;
	}

	public void setMyTeamPoints(long myTeamPoints) {
		this.myTeamPoints = myTeamPoints;
	}

	public long getEnemyTeamPoints() {
		return enemyTeamPoints;
	}

	public void setEnemyTeamPoints(long enemyTeamPoints) {
		this.enemyTeamPoints = enemyTeamPoints;
	}

}
