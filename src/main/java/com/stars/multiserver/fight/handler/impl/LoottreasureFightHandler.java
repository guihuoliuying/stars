package com.stars.multiserver.fight.handler.impl;

import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.packet.NewFighterToFightActor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LoottreasureFightHandler extends FightHandler {

	private int ltActor;
	private int ltRoom;
	
	private Map<Long, byte[]>newFighters;
	@Override
	public void onFightCreationSucceeded(int fightServerId, int fromServerId,
			String fightId, Object args) {
		String s = (String)args;
		String[] ss = s.split("[|]");
		FightRPC.rmltService().doCreateFightActorBack(fromServerId, Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), fightId);
	}

	@Override
	public void onFightCreationFailed(int fightServerId, int fromServerId,
			String fightId, Object args, Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFighterAddingSucceeded(int fightServerId, int fromServerId,
			String fightId, Set<Long> entitySet) {
		FightRPC.rmltService().addFighterToFightActorBack(this.fromServerId, ltActor, ltRoom, entitySet);
	}

	@Override
	public void onFighterAddingFailed(int fightServerId, int fromServerId,
			String fightId, Set<Long> entitySet, Throwable cause) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void handleLuaFrameData(long frameCount, LuaFrameData data,
			Object[] rawData) {
		if (data.getDamage() != null || data.getDead() != null) {
			FightRPC.rmltService().doFightFramData(this.fromServerId, ltActor, ltRoom, data);
		}
	}

	@Override
	public void onFightStopFailed(int fightServerId, int fromServerId,
			String fightId, Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(Object args) {
		String s = (String)args;
		String[] ss = s.split("[|]");
		ltActor = Integer.parseInt(ss[0]);
		ltRoom = Integer.parseInt(ss[1]);
		newFighters = new HashMap<Long, byte[]>();
	}

	@Override
	public void handleFightStop(int fightServerId, int fromServerId,
			String fightId) {
		
	}

	@Override
	public void handleFighterOffline(long roleId) {
		FightRPC.rmltService().doFightOffline(this.fromServerId, ltActor, ltRoom, roleId);
	}
	
	@Override
	public void handleFighterExit(long roleId) {
		FightRPC.rmltService().doExistFight(this.fromServerId, ltActor, ltRoom, roleId);
	}
	
	public void handleSwitchRoom(long roleId, byte roomType){
		FightRPC.rmltService().doSwitchRoom(this.fromServerId, ltActor, roleId, roomType);
	}
	
	@Override
	public void handChangeConn(long roleId) {
		byte[] data = newFighters.get(roleId);
		if (data != null) {
			actor.addServerOrder(data);
			newFighters.remove(roleId);
		}
	}
	
	@Override
	public void handNewFighter(AddNewfighterToFightActor aNewfighterToFightActor) {
		NewFighterToFightActor newers = aNewfighterToFightActor.getNewer();
		//中途加入的玩家需要处理
		Map<Long, Byte> fighterOlMap = newers.getFightersMap();
		Set<Long> set = new HashSet<>();
		Long fighterId;
		//注意：这里应该只有一个元素的;
		for(Map.Entry<Long, Byte> kvp : fighterOlMap.entrySet()){
			fighterId = kvp.getKey();
			set.add(fighterId);
			//注意: 离线的不需要加入这里了;
			if(kvp.getValue() == (byte)0){
				addFighterId(fighterId);
				newFighters.put(fighterId, newers.getData());
			}else{
				//注意: 离线的才发，在线的不需要发; 将NewFighterToFightActor的fighters改为map,记录是否在线;
				actor.addServerOrder(newers.getData());
			}
		}
		onFighterAddingSucceeded(MultiServerHelper.getServerId(), aNewfighterToFightActor.getServerId(), fightId, set);
	}

}
