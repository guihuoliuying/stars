package com.stars.multiserver.fightManager;

import com.stars.services.fightServerManager.FSManagerService;

public class RMFSManagerRPCHelper {
	static FSManagerService fsManagerService;
	public static FSManagerService fsManagerService(){
		return fsManagerService;
	}
}
