package com.stars.services.fightServerManager;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.fightManager.FightServer;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.connector.packet.SendPubServerConfig;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FSManagerServiceActor extends ServiceActor implements
		FSManagerService {

	public Map<Byte, FightServerSupplier>fserverSupplierMap;
	
	public static byte FIGHT_SERVER_LEVEL_GAME = 0;
	
	public static byte FIGHT_SERVER_LEVEL_COMM = 1;
	
	private boolean mainServer = false;
	
	private Map<Integer, FightServer>waitSend2Connector;
	
	private long initTime;
	
	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.FSManagerService,this);
		fserverSupplierMap = new HashMap<Byte,FightServerSupplier>();
		fserverSupplierMap.put(FIGHT_SERVER_LEVEL_GAME, new FightServerSupplier(FIGHT_SERVER_LEVEL_GAME));
		fserverSupplierMap.put(FIGHT_SERVER_LEVEL_COMM, new FightServerSupplier(FIGHT_SERVER_LEVEL_COMM));
		String serverType = ServerManager.getServer().getConfig().getServerType();
		if (serverType.equals(BootstrapConfig.MAIN)) {
			mainServer = true;
		}
		waitSend2Connector = new HashMap<Integer,FightServer>();
		initTime = System.currentTimeMillis();
	}

	@Override
	public void printState() {

	}

	@Override
	public int getFightServer(byte level) {
		FightServerSupplier supplier = fserverSupplierMap.get(level);
		if (supplier == null) {
			LogUtil.error("not find supplier with level = "+level);
			return -1;
		}
		return supplier.getFightServer();
	}
	
	@Override
	public void receiveFightServerMap(int serverId,
			ConcurrentHashMap<Integer, FightServer> fsMap) {
		List<FightServer>back = new ArrayList<FightServer>();
		Collection<FightServerSupplier>col = fserverSupplierMap.values();
		for (FightServerSupplier fightServerSupplier : col) {
			if (mainServer) {
				back.addAll(fightServerSupplier.updateFightServer(fsMap));
			}else {
				fightServerSupplier.updateFightServer(fsMap);
			}
		}
		 
		if (mainServer && back.size() > 0) {
			for (FightServer fightServer : back) {
				waitSend2Connector.put(fightServer.getServerId(), fightServer);
			}
		}
		doConnectorConn2FightServer();
	}
	
	@Override
	public void setFightServerNetStatus(byte serverLevel, int serverId,
			boolean status) {
		FightServerSupplier supplier = fserverSupplierMap.get(serverLevel);
		if (supplier == null) {
			LogUtil.error("set net status error because supplier is null");
			return;
		}
		supplier.setFightServerConnect(serverId, status);
	}
	
	/**
	 * 处理连接服连接到战斗服
	 */
	private void doConnectorConn2FightServer(){
		if (waitSend2Connector.size() <= 0) {
			return;
		}
		if (System.currentTimeMillis() - initTime < 300000) {//启服一定时间后再处理，避免连接服还没有连上来
			return;
		}
		ConcurrentSet<Channel>set = SessionManager.getChannelSet();
		if (set.size() <=0) {
			return;
		}
		SendPubServerConfig serverConfig = new SendPubServerConfig();
		Collection<FightServer>col = waitSend2Connector.values();
		ConcurrentHashMap<String, Properties>pubProps = ServerManager.getServer().getConfig().getPubProps();
		for (FightServer fightServer : col) {
			serverConfig.addBackendAddress(fightServer.getServerId(), fightServer.getIp(), fightServer.getPort());
			Properties properties = new Properties();
	   		properties.put("serverId", String.valueOf(fightServer.getServerId()));
			properties.put("serverIp", fightServer.getIp());
	   		properties.put("serverPort", String.valueOf(fightServer.getPort()));
	   		properties.put("serverType", BootstrapConfig.FIGHT);
	   		pubProps.put(String.valueOf(fightServer.getServerId()), properties);
	   		LogUtil.info("add new fightServer serverId="+fightServer.getServerId());
		}
		waitSend2Connector.clear();
		for (Channel channel : set) {
			GameSession gSession = new GameSession();
			gSession.setChannel(channel);
			try {
				PacketManager.send(gSession, serverConfig);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
		
	}
	
	
}
