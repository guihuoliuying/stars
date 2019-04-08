package com.stars.multiserver;

import com.stars.multiserver.LootTreasure.RMLTService;
import com.stars.multiserver.camp.CampRemoteFightService;
import com.stars.multiserver.camp.CampRemoteMainService;
import com.stars.multiserver.chat.chatrpcservice.RMChatService;
import com.stars.multiserver.daily5v5.Daily5v5MatchService;
import com.stars.multiserver.daregod.DareGodService;
import com.stars.multiserver.familywar.FamilyWarQualifyingService;
import com.stars.multiserver.familywar.FamilyWarRemoteService;
import com.stars.multiserver.familywar.FamilyWarService;
import com.stars.multiserver.familywar.FamilywarRankService;
import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.multiserver.payServer.RMPayServerService;
import com.stars.multiserver.rpctest.echo.EchoService;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.fightingmaster.FightingMasterService;
import com.stars.services.skyrank.SkyRankKFService;

/**
 * Created by zhaowenshuo on 2016/11/4.
 */
public class MainRpcHelper {

    static EchoService echoService;
    static FightBaseService fightBaseService;
    static RMChatService rmChatService;
    static FightingMasterService fightingMasterService;
    static RMLTService rmltService;
    static RMFSManagerService rmfsManagerService;
    static RMPayServerService rmPayServerService;
    static FamilyWarService familyWarService;
    static FamilyWarRemoteService familyWarRemoteService;
    static FamilyWarQualifyingService familyWarQualifyingService;
    static SkyRankKFService skyRankKFService;
    static Daily5v5MatchService daily5v5MatchService;
    static CampRemoteMainService campRemoteMainService;
    static CampRemoteFightService campRemoteFightService;
    static FamilywarRankService familywarRankService;
    static DareGodService dareGodService;

    public static DareGodService dareGodService() {
        return dareGodService;
    }

    public static FamilywarRankService familywarRankService() {
        return familywarRankService;
    }

    public static FamilyWarQualifyingService familyWarQualifyingService() {
        return familyWarQualifyingService;
    }

    public static FamilyWarRemoteService familyWarRemoteService() {
        return familyWarRemoteService;
    }

    public static FamilyWarService familyWarService() {
        return familyWarService;
    }

    public static EchoService echoService() {
        return echoService;
    }

    public static FightBaseService fightBaseService() {
        return fightBaseService;
    }

    public static RMChatService rmChatService() {
        return rmChatService;
    }

    public static FightingMasterService fightingMasterService() {
        return fightingMasterService;
    }

    public static RMLTService rmltService() {
        return rmltService;
    }

    public static RMFSManagerService rmfsManagerService() {
        return rmfsManagerService;
    }

    public static RMPayServerService rmPayServerService() {
        return rmPayServerService;
    }

    public static SkyRankKFService skyRankKFService() {
        return skyRankKFService;
    }

    public static Daily5v5MatchService daily5v5MatchService() {
        return daily5v5MatchService;
    }

    public static CampRemoteMainService campRemoteMainService() {
        return campRemoteMainService;
    }

    public static CampRemoteFightService campRemoteFightService() {
        return campRemoteFightService;
    }
}
