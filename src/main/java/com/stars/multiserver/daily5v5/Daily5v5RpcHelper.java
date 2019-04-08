package com.stars.multiserver.daily5v5;

import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.role.RoleService;

public class Daily5v5RpcHelper {
	
	static Daily5v5Service daily5v5Service;
	
//	static Daily5v5MatchService daily5v5MatchService;
	
	static RMFSManagerService rmfsManagerService;
	
	static FightBaseService fightBaseService;
	
	static RoleService roleService;
	
	public static Daily5v5Service daily5v5Service(){
		return daily5v5Service;
	}
	
//	public Daily5v5MatchService daily5v5MatchService(){
//		return daily5v5MatchService;
//	}
	
	public static FightBaseService fightBaseService() {
        return fightBaseService;
    }
	
	public static RMFSManagerService rmfsManagerService(){
		return rmfsManagerService;
	}
	
	public static RoleService roleService(){
		return roleService;
	}

}
