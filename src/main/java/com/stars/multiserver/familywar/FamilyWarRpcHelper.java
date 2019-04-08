package com.stars.multiserver.familywar;

import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.role.RoleService;

/**
 * Created by zhaowenshuo on 2016/12/6.
 */
public class FamilyWarRpcHelper {

    static FightBaseService fightBaseService;
    static RoleService roleService;
    static RMFSManagerService rmfsManagerService;
    static FamilyWarService familyWarService;
    static FamilyWarRemoteService familyWarRemoteService;
    static FamilyWarQualifyingService familyWarQualifyingService;

    public static FightBaseService fightBaseService() {
        return fightBaseService;
    }

    public static FamilyWarService familyWarService() {
        return familyWarService;
    }

    public static FamilyWarQualifyingService familyWarQualifyingService() {
        return familyWarQualifyingService;
    }

    public static RMFSManagerService rmfsManagerService() {
        return rmfsManagerService;
    }

    public static RoleService roleService() {
        return roleService;
    }

    public static FamilyWarRemoteService familyWarRemoteService() {
        return familyWarRemoteService;
    }
}
