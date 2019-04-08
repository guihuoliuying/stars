package com.stars.services;


import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.modules.chat.Connect2RMChatServerCallBack;
import com.stars.modules.loottreasure.Connect2RMLootServerCallback;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.camp.CampLocalFightService;
import com.stars.multiserver.camp.*;
import com.stars.multiserver.daily5v5.Daily5v5Service;
import com.stars.multiserver.daily5v5.Daily5v5ServiceActor;
import com.stars.multiserver.familywar.FamilyWarLocalService;
import com.stars.multiserver.familywar.FamilyWarService;
import com.stars.multiserver.familywar.*;
import com.stars.services.ArroundPlayer.ArroundServiceActor;
import com.stars.services.accounttransfer.AccountTransferServiceActor;
import com.stars.services.actloopreset.ActLoopResetServiceActor;
import com.stars.services.advertInf.AdvertInfServiceActor;
import com.stars.services.baseteam.BaseTeamServiceActor;
import com.stars.services.bestcp.OpBestCPServiceActor;
import com.stars.services.book.BookServiceActor;
import com.stars.services.callboss.CallBossServiceActor;
import com.stars.services.chat.ChatService;
import com.stars.services.chat.ChatServiceActor;
import com.stars.services.dragonboat.OpDragonBoatServiceActor;
import com.stars.services.elitedungeon.EliteDungeonServiceActor;
import com.stars.services.family.activities.bonfire.FamilyBonFireServiceActor;
import com.stars.services.family.activities.entry.FamilyActEntryServiceImpl;
import com.stars.services.family.activities.invade.FamilyActInvadeActor;
import com.stars.services.family.activities.treasure.FamilyTreasureActor;
import com.stars.services.family.event.FamilyEventServiceActor;
import com.stars.services.family.main.FamilyMainServiceActor;
import com.stars.services.family.role.FamilyRoleServiceActor;
import com.stars.services.family.task.FamilyTaskServiceActor;
import com.stars.services.family.welfare.redpacket.FamilyRedPacketServiceActor;
import com.stars.services.fightServerManager.Conn2FightManagerServerCallBack;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightServerManager.FSRPCNetExceptionTask;
import com.stars.services.friend.FriendServiceActor;
import com.stars.services.friendInvite.InviteServiceActor;
import com.stars.services.guest.GuestServiceActor;
import com.stars.services.id.IdServiceImpl;
import com.stars.services.levelSpeedUp.LevelSpeedUpServiceActor;
import com.stars.services.localservice.LocalService;
import com.stars.services.localservice.LocalServiceActor;
import com.stars.services.loottreasure.LootTreasureService;
import com.stars.services.loottreasure.LootTreasureServiceActor;
import com.stars.services.luckycard.LuckyCardServiceActor;
import com.stars.services.luckydraw.LuckyDrawServiceActor;
import com.stars.services.luckyturntable.LuckyTurnTableServiceActor;
import com.stars.services.mail.EmailServiceActor;
import com.stars.services.marry.MarryServiceActor;
import com.stars.services.mooncake.MoonCakeServiceActor;
import com.stars.services.multicommon.MultiCommonService;
import com.stars.services.multicommon.MultiCommonServiceActor;
import com.stars.services.newofflinepvp.NewOfflinePvpServiceActor;
import com.stars.services.newredbag.NewRedbagServiceActor;
import com.stars.services.newserverfightscore.NewServerFightScoreServiceActor;
import com.stars.services.newservermoney.NewServerMoneyServiceActor;
import com.stars.services.newserverrank.NewServerRankServiceActor;
import com.stars.services.offlinepvp.OfflinePvpServiceActor;
import com.stars.services.opactchargescore.OpActChargeSocreActor;
import com.stars.services.opactfamilyfightscore.OpActFamilyFightScoreActor;
import com.stars.services.opactfightscore.OpActFightScoreActor;
import com.stars.services.opactkickback.OpActKickBackActor;
import com.stars.services.opactsceondkill.OpActSecondKillServiceActor;
import com.stars.services.operateactivity.OperateActivityServiceActor;
import com.stars.services.pay.Conn2PayServerCallBack;
import com.stars.services.pay.PayService;
import com.stars.services.pay.PayServiceActor;
import com.stars.services.pvp.PVPService;
import com.stars.services.pvp.PVPServiceActor;
import com.stars.services.rank.RankActor;
import com.stars.services.role.RoleService;
import com.stars.services.role.RoleServiceImpl;
import com.stars.services.runeDungeon.RuneDungeonServiceActor;
import com.stars.services.sendvigour.SendVigourServiceActor;
import com.stars.services.sevendaygoal.SevenDayGoalServiceActor;
import com.stars.services.shop.ShopServiceImp;
import com.stars.services.skyrank.SkyRankKFService;
import com.stars.services.skyrank.SkyRankKFServiceActor;
import com.stars.services.skyrank.SkyRankLocalService;
import com.stars.services.skyrank.SkyRankLocalServiceActor;
import com.stars.services.summary.SummaryServiceActor;
import com.stars.services.teamdungeon.TeamDungeonServiceActor;
import com.stars.services.weeklyCharge.WeeklyChargeServiceActor;
import com.stars.services.weeklygift.WeeklyGiftServiceActor;
import com.stars.util.LogUtil;

import static com.stars.services.SConst.*;

/**
 * Created by zhaowenshuo on 2016/10/25.
 */
public class MainServerServiceManager extends ServiceManager {

    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit(IdService, new IdServiceImpl()); // id生成服务
        registerAndInit(RoleService, new RoleServiceImpl()); // 玩家相关服务（发包，发事件，执行packet）
        registerAndInit(LocalService, newService(new LocalServiceActor())); // 本地服务
        registerAndInit(SummaryService, newDispatchService(SummaryServiceActor.class, 8)); // 摘要数据：核心业务，需要更多的资源
        registerAndInit(EmailService, newDispatchService(EmailServiceActor.class, 8)); // 邮件：核心业务，需要更多的资源
        registerAndInit(FriendService, newDispatchService(FriendServiceActor.class, 2)); // 好友
        registerAndInit(ChatService, newService(new ChatServiceActor())); // 聊天
        registerAndInit(ArroundPlayerService, newService(new ArroundServiceActor())); // 周围玩家
        registerAndInit(TeamService, newService(new BaseTeamServiceActor())); // 组队
        registerAndInit(TeamDungeonService, newService(new TeamDungeonServiceActor())); // 组队关卡
        registerAndInit(EliteDungeonService, newService(new EliteDungeonServiceActor())); // 精英关卡
        registerAndInit(RankService, newService(new RankActor())); // 排行榜
        registerAndInit(PkService, newService(new PVPServiceActor())); // 切磋
        registerAndInit(FamilyRoleService, newDispatchService(FamilyRoleServiceActor.class, 2)); // 家族 - 个人
        registerAndInit(FamilyMainService, newDispatchService(FamilyMainServiceActor.class, 2)); // 家族
        registerAndInit(FamilyRedPacketService, newDispatchService(FamilyRedPacketServiceActor.class, 2)); // 家族 - 红包（废弃）
        registerAndInit(FamilyEventService, newDispatchService(FamilyEventServiceActor.class, 2)); // 家族 - 日志
        registerAndInit(FamilyActEntryService, new FamilyActEntryServiceImpl()); // 家族 - 活动入口
        registerAndInit(FamilyActInvadeService, newService(new FamilyActInvadeActor())); // 家族 - 入侵
        registerAndInit(FamilyBonFireService, newService(new FamilyBonFireServiceActor())); // 家族 - 篝火
        registerAndInit(FamilyTaskService, newService(new FamilyTaskServiceActor())); // 家族 - 任务
        registerAndInit(FamilyWarLocalService, newService(new FamilyWarLocalServiceActor())); // 家族 - 家族战（本地）
        registerAndInit(FamilyWarService, newService(new FamilyWarServiceActor())); // 家族 - 家族战
//        registerAndInit(FamilyWarQualifyingService, newService(new FamilyWarQualifyingServiceActor()));
        registerAndInit(ShopService, new ShopServiceImp()); // 商店
        registerAndInit(CallBossService, newService(new CallBossServiceActor())); // 召唤BOSS
        registerAndInit(OfflinePvpService, newService(new OfflinePvpServiceActor())); // 离线PVP
        registerAndInit(LootTreasureService, newService(new LootTreasureServiceActor())); // 夺宝
        registerAndInit(MarryService, newService(new MarryServiceActor())); // 结婚
//        registerAndInit(EscortService, newService(new EscortServiceActor())); // 运镖
        registerAndInit(OperateActivityService, newService(new OperateActivityServiceActor())); // 运营活动
        registerAndInit(SevenDayGoalService, newService(new SevenDayGoalServiceActor())); // 七日目标
        registerAndInit(NewServerRankService, newService(new NewServerRankServiceActor())); // 新服排行榜
        registerAndInit(FSManagerService, newService(new FSManagerServiceActor())); // 战斗管理服
        // registerAndInit(TPGLocalService, newService(new
        // TPGLocalServiceActor()));
        registerAndInit(NewServerMoneyService, newService(new NewServerMoneyServiceActor())); // 新服撒钱
        registerAndInit(NewServerFightScoreService, newService(new NewServerFightScoreServiceActor())); // 新服战力
        registerAndInit(GuestService, newService(new GuestServiceActor())); // 门客
        registerAndInit(NewRedbagService, newService(new NewRedbagServiceActor())); // 新家族红包
        registerAndInit(FamilyTreasureService, newService(new FamilyTreasureActor())); // 家族探宝
        registerAndInit(NewOfflinePvpService, newService(new NewOfflinePvpServiceActor())); // 新离线pvp（演武场）
        registerAndInit(OpActFamilyFightSocre, newService(new OpActFamilyFightScoreActor())); // 运营活动 - 家族战力
        registerAndInit(OpActFightSocre, newService(new OpActFightScoreActor())); // 运营活动 - 战力
        registerAndInit(PayService, newService(new PayServiceActor())); // 支付服
        registerAndInit(SendVigourService, new SendVigourServiceActor()); // 送体力
        registerAndInit(AdvertInfService, new AdvertInfServiceActor()); // 通知银汉广告服
        registerAndInit(OpenActKickBack, new OpActKickBackActor()); // 消费返利
        registerAndInit(OpenActChargeScore, new OpActChargeSocreActor()); // 付费榜
//        registerAndInit(FamilyEscortService, newService(new FamilyEscortServiceActor())); // 家族运镖 废弃
        registerAndInit(OpBestCpService, newService(new OpBestCPServiceActor())); // 最佳CP
        registerAndInit(WeeklyChargeService, newService(new WeeklyChargeServiceActor())); // 最佳CP
        registerAndInit(SkyRankLocalService, newService(new SkyRankLocalServiceActor())); //天梯排行本服
        registerAndInit(SkyRankKFService, newService(new SkyRankKFServiceActor())); //天梯排行跨服
        registerAndInit(OpDragonBoatService, newService(new OpDragonBoatServiceActor())); //  龙舟
        registerAndInit(BookService, newService(new BookServiceActor())); // 典籍
        registerAndInit(Daily5v5Service, newService(new Daily5v5ServiceActor())); // 日常5v5 主服
        registerAndInit(InviteService, newService(new InviteServiceActor())); // 好友邀请
        registerAndInit(LevelSpeedUpService, new LevelSpeedUpServiceActor()); // 等级加速
        registerAndInit(WeeklyGiftService, new WeeklyGiftServiceActor());
        registerAndInit(RuneDungeonService, newService(new RuneDungeonServiceActor())); // 挑战副本（符文副本）
        registerAndInit(campLocalMainService, newService(new CampLocalMainServiceActor())); // 阵营
        registerAndInit(CampCityFightService, newService(new CampCityFightServiceActor())); // 阵营 齐楚之争
        registerAndInit(CampLocalFightService, newService(new CampLocalFightServiceActor())); // 阵营大作战
        registerAndInit(LuckyTurnTableService, newService(new LuckyTurnTableServiceActor()));
        registerAndInit(LuckyDrawService, newService(new LuckyDrawServiceActor()));//幸运抽奖
        registerAndInit(MultiCommonService, newService(new MultiCommonServiceActor()));
        registerAndInit(OpActSecondKillService, newService(new OpActSecondKillServiceActor()));
        registerAndInit(MoonCakeService, newService(new MoonCakeServiceActor()));
        registerAndInit(LuckyCardService, newService(new LuckyCardServiceActor()));
        registerAndInit(AccountTransferService, new AccountTransferServiceActor());
        registerAndInit(ActLoopResetService, newService(new ActLoopResetServiceActor()));
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(RoleService.class, getService(RoleService)); // 暴露服务
        exportService(ChatService.class, getService(ChatService));
        exportService(PVPService.class, getService(PkService));
        exportService(LootTreasureService.class, getService(LootTreasureService));
        exportService(LocalService.class, getService(LocalService));
//        exportService(EscortService.class, getService(EscortService));
        exportService(FamilyWarLocalService.class, getService(FamilyWarLocalService));
        exportService(FamilyWarService.class, getService(FamilyWarService));
        exportService(FSManagerService.class, getService(FSManagerService));
        exportService(PayService.class, getService(PayService));
//        exportService(FamilyEscortService.class, getService(FamilyEscortService));//废弃
        exportService(SkyRankLocalService.class, getService(SkyRankLocalService));
        exportService(SkyRankKFService.class, getService(SkyRankKFService));
        exportService(Daily5v5Service.class, getService(Daily5v5Service));
        exportService(CampLocalMainService.class, getService(campLocalMainService));
        exportService(CampLocalFightService.class, getService(CampLocalFightService));
        exportService(MultiCommonService.class, getService(MultiCommonService));
        initRpcHelper(MainRpcHelper.class); // 初始化helper
        connectServer("multi"); // 连接斗神殿
        connectServer("rmchat", new Connect2RMChatServerCallBack());
        connectServer(BootstrapConfig.LOOTTREASURE, new Connect2RMLootServerCallback());
        connectServer(BootstrapConfig.FAMILYWAR, new Conn2FamilywarServerCallBack());
        connectServer("camp", new Conn2CampCallBack());
        int commonId = ServerManager.getServer().getConfig().getServerId();
        int managerServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps()
                .get(BootstrapConfig.FIGHTMANAGER).getProperty("serverId"));
        connectServer(BootstrapConfig.FIGHTMANAGER,
                new Conn2FightManagerServerCallBack(MainRpcHelper.rmfsManagerService(), commonId, managerServerId),
                new FSRPCNetExceptionTask(BootstrapConfig.FIGHTMANAGER, BootstrapConfig.FIGHTMANAGER1,
                        MainRpcHelper.rmfsManagerService()));

        connectServer(BootstrapConfig.PAYSERVER, new Conn2PayServerCallBack());//支付服
        connectServer(BootstrapConfig.PAYSERVER1, new Conn2PayServerCallBack());
        connectServer(BootstrapConfig.SKYRANK); // 连接天梯服
        connectServer(BootstrapConfig.DAILY5V5);
    }

    @Override
    public void runScheduledJob() throws Throwable {
        ServiceHelper.summaryService().save();
        ServiceHelper.emailService().save();
        ServiceHelper.friendService().save();
        ServiceHelper.rankService().save();
        ServiceHelper.familyMainService().save();
        ServiceHelper.familyRoleService().save();
        ServiceHelper.familyRedPacketService().save();
        ServiceHelper.familyEventService().save();
        ServiceHelper.shopService().save();
        ServiceHelper.marryService().save();
        // ServiceHelper.operateActivityService().save();
        ServiceHelper.sevenDayGoalService().save();
        ServiceHelper.chatService().save();
        ServiceHelper.guestService().onSchedule();
        ServiceHelper.familyTreasureService().save();
        ServiceHelper.newOfflinePvpService().save();
        ServiceHelper.familyTaskService().save();
        ServiceHelper.familyWarService().save();
        ServiceHelper.inviteService().save();
        ServiceHelper.eliteDungeonService().save();
        ServiceHelper.weeklyGiftService().save();
        ServiceHelper.campLocalMainService().save();
        try {
            ServiceHelper.daily5v5Service().announceTips();
        } catch (Exception e) {
            LogUtil.error("日常5v5提示报错", e);
        }
//		/* 打印状态 */
//        ServiceHelper.teamDungeonService().printState();
//        ServiceHelper.summaryService().printState();
//        ServiceHelper.operateActivityService().printState();
//        ServiceHelper.newOfflinePvpService().printState();
//        ServiceHelper.marryService().printState();
//        ServiceHelper.chatService().printState();
//        ServiceHelper.baseTeamService().printState();
//        ServiceHelper.advertInfService().printState();
//
//        ServiceHelper.eliteDungeonService().printState();
//        ServiceHelper.sevenDayGoalService().printState();
//        ServiceHelper.newServerFightScoreService().printState();
    }
}
