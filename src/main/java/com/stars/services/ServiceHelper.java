package com.stars.services;

import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.multiserver.camp.*;
import com.stars.multiserver.daily5v5.Daily5v5MatchService;
import com.stars.multiserver.daily5v5.Daily5v5Service;
import com.stars.multiserver.daregod.DareGodService;
import com.stars.multiserver.familywar.*;
import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.multiserver.teamPVPGame.TPGLocalService;
import com.stars.multiserver.teamPVPGame.TPGRemoteService;
import com.stars.services.ArroundPlayer.ArroundPlayerService;
import com.stars.services.accounttransfer.AccountTransferService;
import com.stars.services.actloopreset.ActLoopResetService;
import com.stars.services.advertInf.AdvertInfService;
import com.stars.services.baseteam.BaseTeamService;
import com.stars.services.bestcp.OpBestCPService;
import com.stars.services.book.BookService;
import com.stars.services.callboss.CallBossService;
import com.stars.services.chat.ChatService;
import com.stars.services.dragonboat.OpDragonBoatService;
import com.stars.services.elitedungeon.EliteDungeonService;
import com.stars.services.escort.EscortService;
import com.stars.services.family.activities.bonfire.FamilyBonFireService;
import com.stars.services.family.activities.entry.FamilyActEntryService;
import com.stars.services.family.activities.invade.FamilyActInvadeService;
import com.stars.services.family.activities.treasure.FamilyTreasureService;
import com.stars.services.family.event.FamilyEventService;
import com.stars.services.family.main.FamilyMainService;
import com.stars.services.family.role.FamilyRoleService;
import com.stars.services.family.task.FamilyTaskService;
import com.stars.services.family.welfare.redpacket.FamilyRedPacketService;
import com.stars.services.familyEscort.FamilyEscortService;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.fightingmaster.FightingMasterService;
import com.stars.services.friend.FriendService;
import com.stars.services.friendInvite.InviteService;
import com.stars.services.guest.GuestService;
import com.stars.services.id.IdService;
import com.stars.services.levelSpeedUp.LevelSpeedUpService;
import com.stars.services.localservice.LocalService;
import com.stars.services.loottreasure.LootTreasureService;
import com.stars.services.luckycard.LuckyCardService;
import com.stars.services.luckydraw.LuckyDrawService;
import com.stars.services.luckyturntable.LuckyTurnTableService;
import com.stars.services.mail.EmailService;
import com.stars.services.marry.MarryService;
import com.stars.services.mooncake.MoonCakeService;
import com.stars.services.multicommon.MultiCommonService;
import com.stars.services.newofflinepvp.NewOfflinePvpService;
import com.stars.services.newredbag.NewRedbagService;
import com.stars.services.newserverfightscore.NewServerFightScoreService;
import com.stars.services.newservermoney.NewServerMoneyService;
import com.stars.services.newserverrank.NewServerRankService;
import com.stars.services.offlinepvp.OfflinePvpService;
import com.stars.services.opactchargescore.OpActChargeScore;
import com.stars.services.opactfamilyfightscore.OpActFamilyFightScore;
import com.stars.services.opactfightscore.OpActFightScore;
import com.stars.services.opactkickback.OpActKickBack;
import com.stars.services.opactsceondkill.OpActSecondKillService;
import com.stars.services.operateactivity.OperateActivityService;
import com.stars.services.pay.PayService;
import com.stars.services.pvp.PVPService;
import com.stars.services.rank.RankService;
import com.stars.services.role.RoleService;
import com.stars.services.runeDungeon.RuneDungeonService;
import com.stars.services.sendvigour.SendVigourService;
import com.stars.services.sevendaygoal.SevenDayGoalService;
import com.stars.services.shop.ShopServiceImp;
import com.stars.services.skyrank.SkyRankKFService;
import com.stars.services.skyrank.SkyRankLocalService;
import com.stars.services.summary.SummaryService;
import com.stars.services.teamdungeon.TeamDungeonService;
import com.stars.services.weeklyCharge.WeeklyChargeService;
import com.stars.services.weeklygift.WeeklyGiftService;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/7/14.
 */
public class ServiceHelper {

    public static ConcurrentMap<Short, ServiceActor> serviceMapByPacketType = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, ServiceActor> serviceMapByName = new ConcurrentHashMap<>();

    private static ServiceManager manager;
    private static Field[] fields = ServiceHelper.class.getDeclaredFields();

    public static void init(ServiceManager manager) throws Throwable {
        ServiceHelper.manager = manager;
        manager.init();
        initService();
        setupScheduleTask();
    }

    public static void initService() throws Exception {
        for (Field f : fields) {
            if (Service.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                f.set(ServiceHelper.class, manager.getService(f.getName()));
            }
        }
    }

    static void initField(String fieldName) throws Exception {
        for (Field f : fields) {
            if (f.getName().equals(fieldName)) {
                f.setAccessible(true);
                f.set(ServiceHelper.class, manager.getService(f.getName()));
            }
        }
    }

    /**
     * 手动调用保存
     *
     * @return
     */
    public static boolean executeSave() {
        try {
            manager.runScheduledJob();
        } catch (Throwable throwable) {
            LogUtil.error("", throwable);
            return false;
        }
        return true;
    }

    private static void setupScheduleTask() {
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.ServiceHelper,
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            manager.runScheduledJob();
                        } catch (Throwable e) {
                            LogUtil.error("", e);
                        }
                    }
                }, 5, 30, TimeUnit.SECONDS);
    }

    /* 服务字段属性 */
    static IdService idService;
    static RoleService roleService;
    static EmailService emailService;
    static FriendService friendService;
    static ChatService chatService;
    static SummaryService summaryService;
    static ArroundPlayerService arroundPlayerService;
    static RankService rankService;
    static PVPService pkService;
    static FamilyRoleService familyRoleService;
    static FamilyMainService familyMainService;
    static FamilyRedPacketService familyRedPacketService;
    static FamilyEventService familyEventService;
    static FamilyActEntryService familyActEntryService;
    static FamilyBonFireService familyBonFireService;
    static FamilyActInvadeService familyActInvadeService;
    static ShopServiceImp shopService;
    static CallBossService callBossService;
    static OfflinePvpService offlinePvpService;
    static LootTreasureService lootTreasureService;
    static FightingMasterService fightingMasterService;
    static BaseTeamService baseTeamService;
    static TeamDungeonService teamDungeonService;
    static EliteDungeonService eliteDungeonService;
    static MarryService marryService;
    static TPGLocalService tpgLocalService;
    static TPGRemoteService tpgRemoteService;
    static EscortService escortService;
    static OperateActivityService operateActivityService;
    static SevenDayGoalService sevenDayGoalService;
    static NewServerRankService newServerRankService;
    static FamilyWarLocalService familyWarLocalService;
    static FamilyWarService familyWarService;
    static FamilyWarQualifyingService familyWarQualifyingService;
    static FamilyWarRemoteService familyWarRemoteService;
    static RMFSManagerService rmfManagerService;
    static FSManagerService fsManagerService;
    static NewServerMoneyService newServerMoneyService;
    static NewServerFightScoreService newServerFightScoreService;
    static GuestService guestService;
    static FamilyTreasureService familyTreasureService;
    static NewRedbagService newRedbagService;
    static NewOfflinePvpService newOfflinePvpService;
    static PayService payService;
    static OpActFamilyFightScore opActFamilyFightScore;
    static OpActFightScore opActFightScore;
    static SendVigourService sendVigourService;
    static FamilyTaskService familyTaskService;
    static AdvertInfService advertInfService;
    static Daily5v5MatchService daily5v5MatchService;
    static Daily5v5Service daily5v5Service;
    static FamilyEscortService familyEscortService;
    static OpActChargeScore opActChargeScore;
    static OpActKickBack opActKickBack;
    static OpBestCPService opBestCPService;
    static WeeklyChargeService weeklyChargeService;
    static SkyRankLocalService skyRankLocalService;
    static SkyRankKFService skyRankKFService;
    static OpDragonBoatService opDragonBoatService;
    static BookService bookService;
    static LocalService localService;
    static InviteService inviteService;
    static LevelSpeedUpService levelSpeedUpService;
    static FamilywarRankService familywarRankService;
    static RuneDungeonService runeDungeonService;
    static WeeklyGiftService weeklyGiftService;
    static CampLocalMainService campLocalMainService;
    static CampRemoteMainService campRemoteMainService;
    static CampCityFightService campCityFightService;
    static LuckyTurnTableService luckyTurnTableService;
    static CampRemoteFightService campRemoteFightService;
    static CampLocalFightService campLocalFightService;
    static LuckyDrawService luckyDrawService;
    static DareGodService dareGodService;
    static MultiCommonService multiCommonService;
    static OpActSecondKillService opActSecondKillService;
    static MoonCakeService moonCakeService;
    static LuckyCardService luckyCardService;
    static AccountTransferService accountTransferService;
    static ActLoopResetService actLoopResetService;

    /* 访问方法 */
    public static IdService idService() {
        return idService;
    }

    public static RoleService roleService() {
        return roleService;
    }

    public static EmailService emailService() {
        return emailService;
    }

    public static FriendService friendService() {
        return friendService;
    }

    public static ChatService chatService() {
        return chatService;
    }

    public static SummaryService summaryService() {
        return summaryService;
    }

    public static ArroundPlayerService arroundPlayerService() {
        return arroundPlayerService;
    }

    public static RankService rankService() {
        return rankService;
    }

    public static PVPService pkService() {
        return pkService;
    }

    public static FamilyRoleService familyRoleService() {
        return familyRoleService;
    }

    public static FamilyMainService familyMainService() {
        return familyMainService;
    }

    public static FamilyRedPacketService familyRedPacketService() {
        return familyRedPacketService;
    }

    public static FamilyEventService familyEventService() {
        return familyEventService;
    }

    public static FamilyActEntryService familyActEntryService() {
        return familyActEntryService;
    }

    public static ShopServiceImp shopService() {
        return shopService;
    }

    public static CallBossService callBossService() {
        return callBossService;
    }

    public static OfflinePvpService offlinePvpService() {
        return offlinePvpService;
    }

    public static LootTreasureService lootTreasureService() {
        return lootTreasureService;
    }

    public static FamilyActInvadeService familyActInvadeService() {
        return familyActInvadeService;
    }

    public static FamilyBonFireService familyBonFireService() {
        return familyBonFireService;
    }

    public static FightingMasterService fightingMasterService() {
        return fightingMasterService;
    }

    public static BaseTeamService baseTeamService() {
        return baseTeamService;
    }

    public static TeamDungeonService teamDungeonService() {
        return teamDungeonService;
    }

    public static EliteDungeonService eliteDungeonService() {
        return eliteDungeonService;
    }

    public static MarryService marryService() {
        return marryService;
    }

    public static TPGLocalService tpgLocalService() {
        return tpgLocalService;
    }

    public static TPGRemoteService tpgRemoteService() {
        return tpgRemoteService;
    }

    public static EscortService escortService() {
        return escortService;
    }

    public static FamilyWarLocalService familyWarLocalService() {
        return familyWarLocalService;
    }

    public static OperateActivityService operateActivityService() {
        return operateActivityService;
    }

    public static SevenDayGoalService sevenDayGoalService() {
        return sevenDayGoalService;
    }

    public static NewServerRankService newServerRankService() {
        return newServerRankService;
    }

    public static RMFSManagerService rmfManagerService() {
        return rmfManagerService;
    }

    public static FSManagerService fsManagerService() {
        return fsManagerService;
    }

    public static NewServerMoneyService newServerMoneyService() {
        return newServerMoneyService;
    }

    public static NewServerFightScoreService newServerFightScoreService() {
        return newServerFightScoreService;
    }

    public static GuestService guestService() {
        return guestService;
    }

    public static FamilyTreasureService familyTreasureService() {
        return familyTreasureService;
    }

    public static NewOfflinePvpService newOfflinePvpService() {
        return newOfflinePvpService;
    }

    public static OpActFamilyFightScore opActFamilyFightScore() {
        return opActFamilyFightScore;
    }

    public static OpActFightScore opActFightScore() {
        return opActFightScore;
    }

    public static NewRedbagService newRedbagService() {
        return newRedbagService;
    }

    public static FamilyWarService familyWarService() {
        return familyWarService;
    }

    public static ServiceManager getManager() {
        return manager;
    }

    public static PayService payService() {
        return payService;
    }

    public static SendVigourService sendVigourService() {
        return sendVigourService;
    }

    public static FamilyTaskService familyTaskService() {
        return familyTaskService;
    }

    public static AdvertInfService advertInfService() {
        return advertInfService;
    }

    public static Daily5v5MatchService daily5v5MatchService() {
        return daily5v5MatchService;
    }

    public static Daily5v5Service daily5v5Service() {
        return daily5v5Service;
    }

    public static OpActChargeScore opActChargeScore() {
        return opActChargeScore;
    }

    public static OpActKickBack opActKickBack() {
        return opActKickBack;
    }

    public static FamilyEscortService familyEscortService() {
        return familyEscortService;
    }

    public static Service getServiceByName(String serviceName) {
        return manager.getService(serviceName);
    }

    public static OpBestCPService opBestCPService() {
        return opBestCPService;
    }

    public static WeeklyChargeService weeklyChargeService() {
        return weeklyChargeService;
    }

    public static SkyRankLocalService skyRankLocalService() {
        return skyRankLocalService;
    }

    public static SkyRankKFService skyRankKFService() {
        return skyRankKFService;
    }

    public static BookService bookService() {
        return bookService;
    }

    public static OpDragonBoatService opDragonBoatService() {
        return opDragonBoatService;
    }

    public static FamilyWarQualifyingService familyWarQualifyingService() {
        return familyWarQualifyingService;
    }

    public static FamilyWarRemoteService familyWarRemoteService() {
        return familyWarRemoteService;
    }

    public static LocalService localService() {
        return localService;
    }

    public static InviteService inviteService() {
        return inviteService;
    }

    public static LevelSpeedUpService levelSpeedUpService() {
        return levelSpeedUpService;
    }

    public static FamilywarRankService familywarRankService() {
        return familywarRankService;
    }

    public static RuneDungeonService runeDungeonService() {
        return runeDungeonService;
    }

    public static WeeklyGiftService weeklyGiftService() {
        return weeklyGiftService;
    }

    public static CampLocalMainService campLocalMainService() {
        return campLocalMainService;
    }

    public static CampRemoteMainService campRemoteMainService() {
        return campRemoteMainService;
    }

    public static CampCityFightService campCityFightService() {
        return campCityFightService;
    }

    public static LuckyTurnTableService luckyTurnTableService() {
        return luckyTurnTableService;
    }

    public static CampRemoteFightService campRemoteFightService() {
        return campRemoteFightService;
    }

    public static CampLocalFightService campLocalFightService() {
        return campLocalFightService;
    }

    public static LuckyDrawService luckyDrawService() {
        return luckyDrawService;
    }

    public static DareGodService dareGodService() {
        return dareGodService;
    }

    public static MultiCommonService multiCommonService() {
        return multiCommonService;
    }

    public static OpActSecondKillService opActSecondKillService() {
        return opActSecondKillService;
    }

    public static MoonCakeService moonCakeService() {
        return moonCakeService;
    }

    public static LuckyCardService luckyCardService() {
        return luckyCardService;
    }

    public static AccountTransferService accountTransferService() {
        return accountTransferService;
    }
    public static ActLoopResetService actLoopResetService() {
        return actLoopResetService;
    }
}
