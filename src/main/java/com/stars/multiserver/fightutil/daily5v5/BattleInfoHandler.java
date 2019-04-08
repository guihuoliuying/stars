package com.stars.multiserver.fightutil.daily5v5;

import com.stars.network.server.packet.Packet;

public interface BattleInfoHandler {
	
	public void tips(int serverId, long roleId);
	
	public void sendPacketEvent(int serverId, long roleId, Packet packet);
	
	public void updateMorale(long campId, int morale);
	
	public void updateElitePoints(String attackerUid, long points);
	
	public void sendBattleFightUpdateInfo();

}
