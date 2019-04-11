package com.stars.multiserver.fight;

import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.role.RoleService;

/**
 * Created by zhouyaohui on 2016/11/8.
 */
public class FightRPC {

    static RoleService roleService;
    static FightBaseService fightBaseService;
    static RMFSManagerService rmfsManagerService;


    public static RoleService roleService() {
        return roleService;
    }

    public static FightBaseService fightBaseService() {
        return fightBaseService;
    }

    public static RMFSManagerService rmfsManagerService() {
        return rmfsManagerService;
    }

}
