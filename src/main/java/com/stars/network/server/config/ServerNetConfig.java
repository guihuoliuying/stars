package com.stars.network.server.config;

public class ServerNetConfig {

	public static short serverToLauncherThreadCount = 48;
	public static int serverToLauncherConnectCount  = 500;
    public static int launcherToServer_Boss_ThreadCount = 1;
    public static int launcherToServer_Worker_ThreadCount = 50;
	
	public static boolean debug = false;
	
	static{
		if(ConfigSwitch.os_windows){
			serverToLauncherThreadCount = 5;
			serverToLauncherConnectCount  = 5;
		    launcherToServer_Worker_ThreadCount = 5;
		}
	}
}