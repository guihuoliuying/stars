package com.stars.multiserver.daily5v5;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.SchedulerHelper;
import com.stars.bootstrap.ServerManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.daily5v5.event.Daily5v5CancelMatchingEvent;
import com.stars.modules.daily5v5.event.Daily5v5FightEndEvent;
import com.stars.modules.daily5v5.event.Daily5v5MatchingSuccessEvent;
import com.stars.modules.daily5v5.packet.ClientDaily5v5;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.daily5v5.data.Daily5v5MatchingVo;
import com.stars.multiserver.daily5v5.data.MatchingInfo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Daily5v5ServiceActor extends ServiceActor implements Daily5v5Service{

	private Daily5v5Flow flow;
	
	private int machtServerId;

	public static volatile boolean isOpen = false;
	private long lastAnnounceActivityOpenTimestamp;
	
	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.Daily5v5Service, this);
		flow = new Daily5v5Flow();
        synchronized (Daily5v5MatchServiceActor.class) {
			flow.init(SchedulerHelper.getScheduler(), Daily5v5Manager.dayFlowMap);
		}
        BootstrapConfig config = ServerManager.getServer().getConfig();
		Properties props = config.getProps().get(BootstrapConfig.DAILY5V5);
		machtServerId = Integer.parseInt(props.getProperty("serverId"));
	}

	@Override
	public void printState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startMatching(MatchingInfo info) {
		MainRpcHelper.daily5v5MatchService().matching(machtServerId, info);
	}
	
	@Override
	public void cancelMatching(long roleId, boolean isOffline) {
		try {			
			int serverId = MultiServerHelper.getServerId();
			MainRpcHelper.daily5v5MatchService().cancelMatching(machtServerId, serverId, roleId, isOffline);
		} catch (Exception e) {
			if(!isOffline){
				ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.CANCEL_SUCCESS);
				PlayerUtil.send(roleId, packet);
			}
		}
	}
	
	@Override
	public void cancelMatchingResult(int serverId, long roleId) {
		ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.CANCEL_SUCCESS);
		PlayerUtil.send(roleId, packet);
		ServiceHelper.roleService().notice(roleId, new Daily5v5CancelMatchingEvent());
	}
	
	@Override
	public void continueFighting(long roleId){
		int serverId = MultiServerHelper.getServerId();
		MainRpcHelper.daily5v5MatchService().continueFighting(machtServerId, serverId, roleId);
	}
	
	@Override
	public void checkContinue(long roleId) {
		int serverId = MultiServerHelper.getServerId();
		MainRpcHelper.daily5v5MatchService().checkContinue(machtServerId, serverId, roleId);
	}

	@Override
	public void finishMatching(int serverId, long roleId, List<Daily5v5MatchingVo> memberList,
			List<Daily5v5MatchingVo> enermyMemberList, int fightServerId, int matchingSuccessTime) {
		// TODO Auto-generated method stub
		ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.READY_FIGHT);
		packet.setMemberList(memberList);
		packet.setEnermyMemberList(enermyMemberList);
		packet.setMatchingSuccessTime(matchingSuccessTime);
		PlayerUtil.send(roleId, packet);
		MultiServerHelper.modifyConnectorRoute(roleId, fightServerId);
		ServiceHelper.roleService().notice(roleId, new Daily5v5MatchingSuccessEvent(fightServerId));
	}

	@Override
	public void finishFight(int serverId, long roleId, byte result, int time, ClientDaily5v5 packet) {
		Daily5v5FightEndEvent event = new Daily5v5FightEndEvent();
		event.setPacket(packet);
		event.setResult(result);
		event.setTime(time);
		ServiceHelper.roleService().notice(roleId, event);
	}

	@Override
	public void actOpen() {
		isOpen = true;
		sendActPacket(Daily5v5Manager.ACT_OPEN);
        announceTips();
	}

	@Override
	public void actEnt() {
		isOpen = false;
		sendActPacket(Daily5v5Manager.ACT_CLOSE);
	}
	
	private void sendActPacket(byte actState){
		Iterator<AbstractActor> iterator = PlayerSystem.system().getActors().values().iterator();
		AbstractActor actor = null;
		Player player = null;
		for(;iterator.hasNext();){
			actor = iterator.next();
			if (actor instanceof Player) {
				player = (Player) actor;
				//发送活动开启
				ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.ACTIVITY_STATE);
				packet.setActState(actState);
				PlayerUtil.send(player.id(), packet);
			}
		}
	}
	
	@Override
	public void announce(int serverId, long roleId, String name, byte enermy) {
		String gametext = "";
		if(enermy==1){
			gametext = DataManager.getGametext("dailyfivepvp_vipskilltips-towerprotect_other");
		}else{
			gametext = DataManager.getGametext("dailyfivepvp_vipskilltips_towerprotect_self");
		}
		String message = String.format(gametext, name);
//		ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_SYSTEM, 0L, 0L, message, false);
//        ServiceHelper.chatService().announce(message);
		PlayerUtil.send(roleId, new ClientText(message));
	}

	@Override
	public void announceTips() {
		long now = System.currentTimeMillis();
		if (isOpen) {
			if (now - lastAnnounceActivityOpenTimestamp > Daily5v5Manager.TIPS_INTERVAL) {
				lastAnnounceActivityOpenTimestamp = now;
				ServiceHelper.chatService().announce("fivepvp_tvtips_desc");
			}
		}
	}

	@Override
	public void gmHandler(long roleId, String[] args) {
		// TODO Auto-generated method stub
		MainRpcHelper.daily5v5MatchService().gmHandler(machtServerId, roleId, args);
	}

}
