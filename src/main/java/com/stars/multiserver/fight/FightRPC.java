package com.stars.multiserver.fight;

import com.stars.multiserver.LootTreasure.RMLTService;
import com.stars.multiserver.camp.CampRemoteFightService;
import com.stars.multiserver.daily5v5.Daily5v5MatchService;
import com.stars.multiserver.familywar.FamilyWarLocalService;
import com.stars.multiserver.familywar.FamilyWarQualifyingService;
import com.stars.multiserver.familywar.FamilyWarRemoteService;
import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.multiserver.teamPVPGame.TPGLocalService;
import com.stars.services.escort.EscortService;
import com.stars.services.familyEscort.FamilyEscortService;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.fightingmaster.FightingMasterService;
import com.stars.services.pvp.PVPService;
import com.stars.services.role.RoleService;

/**
 * Created by zhouyaohui on 2016/11/8.
 */
public class FightRPC {

    static RoleService roleService;
    static FightBaseService fightBaseService;
    static FightingMasterService fightingMasterService;
    static PVPService pvpService;
    static RMLTService rmltService;
    static EscortService escortService;
    static FamilyWarLocalService familyWarLocalService;
    static TPGLocalService tpgLocalService;
    static RMFSManagerService rmfsManagerService;
    static FamilyEscortService familyEscortService;
    static FamilyWarQualifyingService familyWarQualifyingService;
    static FamilyWarRemoteService familyWarRemoteService;
    static Daily5v5MatchService daily5v5MatchService;
    static CampRemoteFightService campRemoteFightService;

    public static FamilyWarRemoteService familyWarRemoteService() {
        return familyWarRemoteService;
    }

    public static FamilyWarQualifyingService familyWarQualifyingService() {
        return familyWarQualifyingService;
    }

    public static RoleService roleService() {
        return roleService;
    }

    public static FightBaseService fightBaseService() {
        return fightBaseService;
    }

    public static FightingMasterService fightingMasterService() {
        return fightingMasterService;
    }

    public static PVPService pvpService() {
        return pvpService;
    }

    public static RMLTService rmltService() {
        return rmltService;
    }

    public static EscortService escortService() {
        return escortService;
    }

    public static FamilyWarLocalService familyWarLocalService() {
        return familyWarLocalService;
    }

    public static TPGLocalService tpgLocalService() {
        return tpgLocalService;
    }

    public static RMFSManagerService rmfsManagerService() {
        return rmfsManagerService;
    }

    public static FamilyEscortService familyEscortService() {
        return familyEscortService;
    }

    public static Daily5v5MatchService daily5v5MatchService() {
        return daily5v5MatchService;
    }

    public static CampRemoteFightService campRemoteFightService() {
        return campRemoteFightService;
    }
}
