package com.stars.multiserver.fightManager;

import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RMFSManagerServiceActor extends ServiceActor implements
		RMFSManagerService {
	/**
	 * serverId:FightServer
	 */
	private ConcurrentHashMap<Integer, FightServer>fServerMap;
	
	private Set<Integer>serverSet;
	
	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.RMFManagerService,this);
		fServerMap = new ConcurrentHashMap<Integer, FightServer>();
		serverSet = new HashSet<Integer>();
		StatusCheckMessageSender sender = new StatusCheckMessageSender();
		sender.setRunStatus(true);
		sender.start();
	}

	@Override
	public void printState() {

	}

	@Override
	public void registerFightServer(int serverId, int fServer,
			String fServerIp, int fServerPort,int load) {
		FightServer fightServer = fServerMap.get(fServer);
		if (fightServer == null) {
			fightServer = new FightServer(fServer);
			fightServer.setIp(fServerIp);
			fightServer.setPort(fServerPort);
			fightServer.setLeve((byte)1);
			fServerMap.put(fServer, fightServer);
			LogUtil.info("fightSerer regist serverId="+fServer);
		}
		fightServer.setLoad(load);
		fightServer.setLastSyn(System.currentTimeMillis());
	}
	
	@Override
	public void registerCommonServer(int serverId, int commonServer) {
		serverSet.add(commonServer);
	}
	
	public void sendFightServer2CommonServer(){
		
		for (Integer commonServer : serverSet) {
			try {
				RMFSManagerRPCHelper.fsManagerService().receiveFightServerMap(commonServer, fServerMap);
			} catch (Throwable cause) {
				LogUtil.error("战斗管理服异常, " + cause.getMessage());
			}
		}
	}
		
	/**
	 * 检查战斗服的状态
	 */
	private void checkFightServerStatus(){
		Collection<FightServer>col = fServerMap.values();
		long now = System.currentTimeMillis();
		for (FightServer fightServer : col) {
			if (now - fightServer.getLastSyn() > 2000) {
				LogUtil.info("fightSerer not active serverId="+fightServer.getServerId());
				fServerMap.remove(fightServer.getServerId());
			}
		}
	}
	
	@Override
	public void onReceived0(Object message, Actor sender) {
		if (message instanceof CheckFServerStatusMessage) {
			checkFightServerStatus();
			sendFightServer2CommonServer();
		}
	}
	
	class StatusCheckMessageSender extends Thread{
		private boolean runStatus = false;
		@Override
		public void run() {
			while (runStatus) {
				tell(new CheckFServerStatusMessage(), Actor.noSender);
				try {
					sleep(1000l);
				} catch (Exception e) {
					LogUtil.error(e.getMessage(), e);
				}
			}
			
		}
		public void setRunStatus(boolean runStatus) {
			this.runStatus = runStatus;
		}
	}
}

class CheckFServerStatusMessage{
	public CheckFServerStatusMessage(){}
}
