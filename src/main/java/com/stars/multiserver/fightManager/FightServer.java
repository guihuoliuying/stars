package com.stars.multiserver.fightManager;


public class FightServer {
	
	private int serverId;
	
	private String ip;
	
	private int port;
	
	/**
	 * 服务的负载
	 * 粗略的以人数为标志
	 */
	private int load;
	
	private long lastSyn;
	
	private byte leve;
	
	private boolean connect;
	
	public FightServer(int serverId){
		this.serverId = serverId;
		load = 0;
		lastSyn = System.currentTimeMillis();
		connect = false;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getLoad() {
		return load;
	}

	public void setLoad(int load) {
		this.load = load;
	}
	
	public int addLoad(int add){
		load+=add;
		return load;
	}

	public long getLastSyn() {
		return lastSyn;
	}

	public void setLastSyn(long lastSyn) {
		this.lastSyn = lastSyn;
	}

	public byte getLeve() {
		return leve;
	}

	public void setLeve(byte leve) {
		this.leve = leve;
	}

	public boolean isConnect() {
		return connect;
	}

	public void setConnect(boolean connect) {
		this.connect = connect;
	}
}
