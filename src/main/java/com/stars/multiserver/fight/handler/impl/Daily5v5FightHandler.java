package com.stars.multiserver.fight.handler.impl;

import com.stars.modules.daily5v5.packet.ServerDaily5v5Revive;
import com.stars.modules.daily5v5.packet.ServerDaily5v5UseBuff;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightBlock;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.daily5v5.Daily5v5FightArgs;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightHandler;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.fightbase.FightBaseService;
import com.stars.util.MapUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Daily5v5FightHandler extends PhasesPkFightHandler {
	
	private Map<String, HashMap<String, Integer>> accumulatedDamageMap = new HashMap<>();
	
	private boolean canceledDynamicBlock = false;
	
	private long createTimeStamp;
	
	private Map<String, Integer> fighterSeverIdMap;

	@Override
	public void init0(Object obj) {
		Daily5v5FightArgs args = (Daily5v5FightArgs)obj;
		this.createTimeStamp = args.getCreateTimestamp();
		this.fighterSeverIdMap = args.getFighterSeverIdMap();
		registerPassThroughPacketType(ServerDaily5v5Revive.class);
		registerPassThroughPacketType(ServerDaily5v5UseBuff.class);
	}

	@Override
	public void onFightCreationSucceeded0(int fightServerId, int fromServerId, String fightId, Object args) {
		FightRPC.daily5v5MatchService().rpcOnFightCreated(fromServerId, fightId, true);
	}

	@Override
	public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args,
			Throwable cause) {
		FightRPC.daily5v5MatchService().rpcOnFightCreated(fromServerId, fightId, false);
	}

	@Override
	public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
		Iterator<Long> iterator = entitySet.iterator();
		if(iterator.hasNext()){
			Long roleId = iterator.next();
			FightRPC.daily5v5MatchService().onFighterAddingSucceeded(fromServerId, fightServerId, fightId, roleId);
		}
	}

	@Override
	public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet,
			Throwable cause) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handleClientPreloadFinished(long roleId) {
		FightRPC.daily5v5MatchService().handleClientPreloadFinished(fromServerId, fightId, roleId);
	}

	@Override
	public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleFightStop(int fightServerId, int fromServerId, String fightId) {
		Iterator<Entry<String, Integer>> iterator = fighterSeverIdMap.entrySet().iterator();
		Entry<String, Integer> entry = null;
		long roleId = 0;
		for(;iterator.hasNext();){
			entry = iterator.next();
			roleId = Long.valueOf(entry.getKey());
//			MultiServerHelper.modifyConnectorRoute(roleId, entry.getValue());
//			FightRPC.roleService().exec(entry.getValue(), roleId, new ServerExitFight());
		}
	}

	@Override
	public void handleFighterOffline(long roleId) {
		if(fighterSeverIdMap.containsKey(String.valueOf(roleId))){		
			fighterIdSet.remove(roleId);
			FightRPC.daily5v5MatchService().handleFighterQuit(fromServerId, fightId, roleId);
		}
	}
	
	@Override
	public void handleFighterExit(long roleId) {
		if(fighterSeverIdMap.containsKey(String.valueOf(roleId))){			
			Integer mainServerId = fighterSeverIdMap.get(String.valueOf(roleId));
			MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
	        FightRPC.roleService().exec(mainServerId, roleId, new ServerExitFight());
	        fighterIdSet.remove(roleId);
	        FightRPC.daily5v5MatchService().handleFighterQuit(fromServerId, fightId, roleId);
		}
	}
	
	@Override
	public void handleFighterExitToFamilySafeScene(long roleId) {
		handleFighterExit(roleId);
	}
	
	@Override
	public void handChangeConn(long roleId) {
		arrivalFighterIdSet.add(roleId);
		FightRPC.daily5v5MatchService().handChangeConn(fromServerId, "", fightId, String.valueOf(roleId));
	}
	
	@Override
	public void handleDamage(long frameCount, Map<String, HashMap<String, Integer>> damageMap) {
		// 不每帧返回
        for (Map.Entry<String, HashMap<String, Integer>> entry : damageMap.entrySet()) {
            HashMap<String, Integer> map = accumulatedDamageMap.get(entry.getKey());
            if (map == null) {
                accumulatedDamageMap.put(entry.getKey(), entry.getValue());
            } else {
                MapUtil.add(map, entry.getValue());
            }
        }
//		FightRPC.daily5v5MatchService().handleFightDamage(fromServerId, "", fightId, damageMap);
	}
	
	@Override
	public void handleDead(long frameCount, Map<String, String> deadMap) {
		FightRPC.daily5v5MatchService().handleFightDead(fromServerId, "", fightId, deadMap);
	}
	
	@Override
	public void handleTimeOut(long frameCount, HashMap<String, String> hpInfo) {
		try {
			FightRPC.daily5v5MatchService().handleTimeOut(fromServerId, fightId, hpInfo);
		} catch (Exception e) {
			//匹配服挂了
			for(String roleId : fighterSeverIdMap.keySet()){				
				handleFighterExit(Long.parseLong(roleId));
			}
			FightBaseService service = (FightBaseService)ServiceHelper.getManager().getService(SConst.FightBaseService);
			service.stopFight(fightServerId, handlerType, fromServerId, fightId);
		}
	}
	
	@Override
	public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {
		if (frameCount % 30 == 0 && !accumulatedDamageMap.isEmpty()) {
            FightRPC.daily5v5MatchService().handleFightDamage(
                    fromServerId, "", fightId, accumulatedDamageMap);
            accumulatedDamageMap.clear();
        }
        if (frameCount % 30 == 0 && !canceledDynamicBlock) {
        	if (System.currentTimeMillis() >= createTimeStamp + FamilyActWarManager.DYNAMIC_BLOCK_TIME * 1000) {
        		ClientFamilyWarBattleFightBlock packet = createCancelBlockPacket();
        		if (packet != null) {
        			for (Long fighterId : fighterIdSet) {
        				PacketManager.send(fighterId, packet);
        			}
        			canceledDynamicBlock = true;
        		}
        	}
        }
	}
	
	/**
     * 关闭所有动态阻挡
     */
    public ClientFamilyWarBattleFightBlock createCancelBlockPacket() {
    	StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
    	if (stageVo == null) return null;
    	Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
        }
        ClientFamilyWarBattleFightBlock blockPacket = new ClientFamilyWarBattleFightBlock(blockStatus);
    	return blockPacket;
    }
    
    @Override
    public void handleMessage(Object message) {
    	super.handleMessage(message);
    	Packet packet = (Packet) message;
    	if(packet instanceof ServerDaily5v5Revive){
    		ServerDaily5v5Revive req = (ServerDaily5v5Revive)packet;
    		FightRPC.daily5v5MatchService().handleRevive(fromServerId, "", fightId, String.valueOf(req.getRoleId()));
    	}else if(packet instanceof ServerDaily5v5UseBuff){
    		ServerDaily5v5UseBuff req = (ServerDaily5v5UseBuff)packet;
    		FightRPC.daily5v5MatchService().handleUseBuff(fromServerId, fightId, req.getRoleId(), req.getEffectId());
    	}
    }

}
