package com.stars.multiserver.LootTreasure;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.ServerConnSessionManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractLootTreasure {
	
	public static byte STEP_WAIT = 0;//等待阶段
	public static byte STEP_FIGHT = 1;//PVE战斗阶段
	public static byte STEP_OVER = 2; //结束阶段
	/**
	 * 参与夺宝的人
	 */
	public Map<Long,Looter>looters;
	/**
	 * 掉线列表，等待处理
	 */
	public Set<Long> offlineLooters;
	
	public LTActor ltActor;
	
	public byte step;
	
	public long nextStepTime;

	public LTDamageRank ltDamageRank;
	
	public AbstractLootTreasure(LTActor ltActor){
		this.ltActor = ltActor;
		looters = new HashMap<Long, Looter>();
		offlineLooters = new HashSet<Long>();
	}

	/**
	 * 判断玩家是否在线;
	 * @param roleId
	 * @return
	 */
	public boolean isOnline(long roleId){
		if(this.looters.containsKey(roleId) && this.offlineLooters.contains(roleId)==false){
			return true;
		}
		return false;
	}

	public void addLooter(Looter looter){
		looters.put(looter.getId(), looter);
	}
	
	public void sendToClient(Looter looter,Packet p){
		GameSession gs = SessionManager.getSessionMap().get(looter.getId());
		PacketManager.send(gs, p);
	}

	public void sendToClient(long roleId, Packet p){
		GameSession gs = SessionManager.getSessionMap().get(roleId);
		PacketManager.send(gs, p);
	}
	
	public void sendToServer(Looter looter,Packet p){
		GameSession gs = ServerConnSessionManager.get(looter.getServerId());
		PacketManager.send(gs, p);
	}
	
	
	public Map<Long, Looter> getLooters() {
		return looters;
	}

	public void setLooters(Map<Long, Looter> looters) {
		this.looters = looters;
	}

	public void setOfflineLooters(Set<Long> offlineLooters){
		this.offlineLooters = offlineLooters;
	}

	abstract void onReceived(Object message);
	abstract void newLooterCome(int serverId, String serverName, FighterEntity fEntity, int jobId);
	abstract void stopSelf();
	abstract void startRunner();
	public void revive(long roleId){

	}

	class RunEvent{
		private int eId;
		public RunEvent(int id){
			this.eId = id;
		}
		public int geteId() {
			return eId;
		}
	}
}
