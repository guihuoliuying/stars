package com.stars.multiserver.camp;

import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.role.RoleService;

/**
 * Created by huwenjun on 2017/6/28.
 */
public class CampRpcHelper {
    static CampLocalMainService campLocalMainService;
    static RoleService roleService;
    static FightBaseService fightBaseService;
    static CampLocalFightService campLocalFightService;
    static RMFSManagerService rmfsManagerService;

    public static CampLocalMainService campLocalMainService() {
        return campLocalMainService;
    }

    public static RoleService roleService() {
        return roleService;
    }

    public static FightBaseService fightBaseService() {
        return fightBaseService;
    }

    public static CampLocalFightService campLocalFightService() {
        return campLocalFightService;
    }

    public static RMFSManagerService rmfsManagerService() {
        return rmfsManagerService;
    }
}
