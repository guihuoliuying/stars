package com.stars.startup;

import com.stars.AccountRow;
import com.stars.ServerStatePrinter;
import com.stars.ServerVersion;
import com.stars.bootstrap.SchedulerHelper;
import com.stars.bootstrap.ServerManager;
import com.stars.core.AccessControl;
import com.stars.core.SystemRecordMap;
import com.stars.core.clientpatch.PatchManager;
import com.stars.core.gmpacket.BlockAccountGm;
import com.stars.core.gmpacket.GmPacketDefine;
import com.stars.core.gmpacket.SwitchEntranceGm;
import com.stars.core.gmpacket.WhiteListOpenOrCloseGm;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.hotupdate.YinHanHotUpdateManager;
import com.stars.core.module.ModuleManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.yinhan.hotupdate.HotUpdateManager;
import com.stars.modules.MConst;
import com.stars.modules.achievement.AchievementModuleFactory;
import com.stars.modules.activeweapon.ActiveWeaponModuleFactory;
import com.stars.modules.archery.ArcheryModuleFactory;
import com.stars.modules.arroundPlayer.ArroundPlayerModuleFactory;
import com.stars.modules.baby.BabyModuleFactory;
import com.stars.modules.baseteam.BaseTeamModuleFactory;
import com.stars.modules.bestcp520.BestCPModuleFactory;
import com.stars.modules.book.BookModuleFactory;
import com.stars.modules.bravepractise.BravePractiseModuleFactory;
import com.stars.modules.buddy.BuddyModuleFactory;
import com.stars.modules.callboss.CallBossModuleFactory;
import com.stars.modules.camp.CampModuleFactory;
import com.stars.modules.changejob.ChangeJobModuleFactory;
import com.stars.modules.chargeback.ChargeBackModuleFactory;
import com.stars.modules.chargegift.ChargeGiftModuleFactory;
import com.stars.modules.chargepreference.ChargePrefModuleFactory;
import com.stars.modules.chat.ChatModuleFactory;
import com.stars.modules.collectphone.CollectPhoneModuleFactory;
import com.stars.modules.countDown.CountDownModuleFactory;
import com.stars.modules.customerService.CustomerServiceModuleFactory;
import com.stars.modules.daily.DailyModuleFactory;
import com.stars.modules.daily5v5.Daily5v5ModuleFactory;
import com.stars.modules.dailyCharge.DailyChargeModuleFactory;
import com.stars.modules.daregod.DareGodModuleFactory;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.deityweapon.DeityWeaponModuleFactory;
import com.stars.modules.demologin.LoginModuleFactory;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.message.OfflineMsg;
import com.stars.modules.demologin.packet.ClientReconnect;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.demologin.packet.ServerReconnect;
import com.stars.modules.discountgift.DiscountGiftModuleFactory;
import com.stars.modules.dragonboat.DragonBoatModuleFactory;
import com.stars.modules.drop.DropModuleFactory;
import com.stars.modules.dungeon.DungeonModuleFactory;
import com.stars.modules.elitedungeon.EliteDungeonModuleFactory;
import com.stars.modules.email.EmailModuleFactory;
import com.stars.modules.everydaycharge.EverydayChargeModuleFactory;
import com.stars.modules.family.FamilyModuleFactory;
import com.stars.modules.familyTask.FamilyTaskModuleFactory;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModuleFactory;
import com.stars.modules.familyactivities.invade.FamilyInvadeModuleFactory;
import com.stars.modules.familyactivities.treasure.FamilyTreasureModuleFactory;
import com.stars.modules.familyactivities.war.FamilyActWarModuleFactory;
import com.stars.modules.fashion.FashionModuleFactory;
import com.stars.modules.fashioncard.FashionCardModuleFactory;
import com.stars.modules.fightingmaster.FightingMasterModuleFactory;
import com.stars.modules.foreshow.ForeShowModuleFactory;
import com.stars.modules.friend.FriendModuleFactory;
import com.stars.modules.friendInvite.InviteModuleFactory;
import com.stars.modules.friendShare.ShareModuleFactory;
import com.stars.modules.gameboard.GameboardModuleFactory;
import com.stars.modules.gamecave.GameCaveModuleFactory;
import com.stars.modules.gem.GemModuleFactory;
import com.stars.modules.getway.GetWayModuleFactory;
import com.stars.modules.giftcome520.GiftComeModuleFactory;
import com.stars.modules.gm.GmModuleFactory;
import com.stars.modules.guardofficial.GuardOfficialModuleFactory;
import com.stars.modules.guest.GuestModuleFactory;
import com.stars.modules.hotUpdate.HotUpdateModuleFactory;
import com.stars.modules.induct.InductModuleFactory;
import com.stars.modules.levelSpeedUp.LevelSpeedUpModuleFactory;
import com.stars.modules.loottreasure.LootTreasureModuleFactory;
import com.stars.modules.luckycard.LuckyCardModuleFactory;
import com.stars.modules.luckydraw.LuckyDrawModuleFactory;
import com.stars.modules.luckydraw1.LuckyDraw1ModuleFactory;
import com.stars.modules.luckydraw2.LuckyDraw2ModuleFactory;
import com.stars.modules.luckydraw3.LuckyDraw3ModuleFactory;
import com.stars.modules.luckydraw4.LuckyDraw4ModuleFactory;
import com.stars.modules.luckyturntable.LuckyTurnTableModuleFactory;
import com.stars.modules.marry.MarryModuleFactory;
import com.stars.modules.masternotice.MasterNoticeModuleFactory;
import com.stars.modules.mind.MindModuleFactory;
import com.stars.modules.mooncake.MoonCakeModuleFactory;
import com.stars.modules.name.NameModuleFactory;
import com.stars.modules.newdailycharge.NewDailyChargeModuleFactory;
import com.stars.modules.newequipment.NewEquipmentModuleFactory;
import com.stars.modules.newfirstrecharge.NewFirstRechargeModuleFactory;
import com.stars.modules.newfirstrecharge1.NewFirstRecharge1ModuleFactory;
import com.stars.modules.newofflinepvp.NewOfflinePvpModuleFactory;
import com.stars.modules.newredbag.NewRedbagModuleFactory;
import com.stars.modules.newserverfightscore.NewServerFightModuleFactory;
import com.stars.modules.newservermoney.NewServerMoneyModuleFactory;
import com.stars.modules.newserverrank.NewServerRankModuleFactory;
import com.stars.modules.newserversign.NewServerSignModuleFactory;
import com.stars.modules.newsignin.NewSigninModuleFactory;
import com.stars.modules.offlinepvp.OfflinePvpModuleFactory;
import com.stars.modules.oldplayerback.OldPlayerBackModuleFactory;
import com.stars.modules.onlinereward.OnlineRewardModuleFactory;
import com.stars.modules.opactbenefittoken.OpActBenefitTokenModuleFactory;
import com.stars.modules.opactchargescore.OpActChargeScoreModuleFactory;
import com.stars.modules.opactfamilyfightscore.OpActFamilyFightScoreModuleFactory;
import com.stars.modules.opactfightscore.OpActFightScoreModuleFactory;
import com.stars.modules.opactkickback.OpActKickBackModuleFactory;
import com.stars.modules.opactsecondskill.OpActSecondKillModuleFactory;
import com.stars.modules.operateCheck.OperateCheckModuleFactory;
import com.stars.modules.operateactivity.OperateActivityModuleFactory;
import com.stars.modules.optionalbox.OptionalBoxModuleFactory;
import com.stars.modules.pk.PKModuleFacotry;
import com.stars.modules.poem.PoemModuleFactory;
import com.stars.modules.poemdungeon.PoemDungeonModuleFactory;
import com.stars.modules.popUp.PopUpModuleFactory;
import com.stars.modules.positionsync.PositionSyncModuleFactory;
import com.stars.modules.push.PushModuleFactory;
import com.stars.modules.quwudu.QuwuduModuleFactory;
import com.stars.modules.raffle.RaffleModuleFactory;
import com.stars.modules.rank.RankModuleFactory;
import com.stars.modules.redpoint.RedPointModuleFactory;
import com.stars.modules.refine.RefineModuleFactory;
import com.stars.modules.relationoperation.RelationOperationModuleFactory;
import com.stars.modules.retrievereward.RetrieveRewardModuleFactory;
import com.stars.modules.ride.RideModuleFactory;
import com.stars.modules.role.RoleModuleFactory;
import com.stars.modules.runeDungeon.RuneDungeonModuleFactory;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.searchtreasure.SearchTreasureModuleFactory;
import com.stars.modules.sendvigour.SendVigourModuleFactory;
import com.stars.modules.serverLog.ServerLogModuleFactory;
import com.stars.modules.serverfund.ServerFundModuleFactory;
import com.stars.modules.sevendaygoal.SevenDayGoalModuleFactory;
import com.stars.modules.shop.ShopModuleFactory;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.modules.skyrank.SkyRankModuleFactory;
import com.stars.modules.skytower.SkyTowerModuleFactory;
import com.stars.modules.soul.SoulModuleFactory;
import com.stars.modules.task.TaskModuleFactory;
import com.stars.modules.teamdungeon.TeamDungeonModuleFactory;
import com.stars.modules.tecentvideo.TencentVideoModuleFactory;
import com.stars.modules.title.TitleModuleFactory;
import com.stars.modules.tool.ToolModuleFactory;
import com.stars.modules.truename.TrueNameModuleFactory;
import com.stars.modules.trump.TrumpModuleFactory;
import com.stars.modules.vip.VipModuleFactory;
import com.stars.modules.weeklyCharge.WeeklyChargeModuleFactory;
import com.stars.modules.weeklygift.WeeklyGiftModuleFactory;
import com.stars.modules.welfareaccount.WelfareAccountModuleFactory;
import com.stars.modules.wordExchange.WordExchangeModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.packet.LuaFrameDataBack;
import com.stars.network.PacketChecker;
import com.stars.network.PacketUtil;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.server.main.message.Disconnected;
import com.stars.services.MainServerServiceManager;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.ExecuteManager;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;
import com.stars.util._HashMap;
import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2015/3/18.
 */
public class MainStartup implements Business {

    public static boolean isOpenGameGm = false; // 是否开启游戏内GM
    public static ConcurrentMap<String, AccountRow> accountMap = new ConcurrentHashMap<>(); // 账号全局表

    public static String serverChannel = "zyy";

    @Override
    public void init() throws Exception {
        com.stars.util.LogUtil.info("初始化主服业务逻辑...");
        try {
            com.stars.util.ExecuteManager.init(32);
            ServerVersion.load();//加载服务版本号
            initHotswapEnv();
            DBUtil.init();// 初始化数据库连接池(proxool)
            MultiServerHelper.loadPublicServerConfig();
            loadServerChannel();
            registerOtherPacket();
            checkPacket(); // 检查协议号，是否全局唯一
            com.stars.bootstrap.SchedulerHelper.init("./config/jobs/quartz.properties");
            SchedulerManager.init(SchedulerManager.scheduledCorePoolSize);
            initModule(); // 初始化模块
            loadProductData(); // 加载数据(产品数据)
            loadSystemRecordMap();
            ActorServer.setActorSystem(new ActorSystem()); // 初始化ActorSystem
            PlayerSystem.init();
            ServiceSystem.init();
            YinHanHotUpdateManager.init();
            ServiceHelper.init(new MainServerServiceManager());
            SchedulerHelper.start();
            SchedulerManager.initScheduler();
//            //输出资源加载文件列表
//            ResoucePrinter.getInstance().writeResourceList();
            WhiteListOpenOrCloseGm.loadWhiteList(); // 加载白名单账号
            BlockAccountGm.loadBlockAccount();// 加载封号账号
            SpecialAccountManager.loadSpecialAccount();//加载特殊账号(充值白名单)
            GmPacketDefine.reg();
            PatchManager.init();
            initGameServerConfig(); // 初始化服务入口
            ServerStatePrinter.init();
        } catch (Throwable cause) {
            com.stars.util.LogUtil.error("MainStartup.init() error ", cause);
            System.exit(1);
        }
    }

    private void initDistEnv() throws Exception {
        // TODO 初始化分布式锁
    }

    public static void initHotswapEnv() throws Exception {
        if (!HotUpdateManager.init(ServerLogConst.console, ServerLogConst.exception)) {
            System.exit(0);
        }
        HotUpdateManager.checkClassFile();
        //热更回调设置
//        HotUpdateManager.setCallBackClassName("com.stars.manager.ServerManager.ServerManagerDataPool");
//        HotUpdateManager.setCallBackMethod("addHotUpdateCount");
    }


    /**
     * 初始化业务模块
     * 依赖模块必须放在前面,优先初始化
     *
     * @throws Exception
     */
    private void initModule() throws Exception {
        ModuleManager.register(MConst.Login, new LoginModuleFactory()); // 登录 -
        ModuleManager.register(MConst.HotUpdate, new HotUpdateModuleFactory()); // 热更模块(处理线上问题)
        ModuleManager.register(MConst.RedPoint, new RedPointModuleFactory()); // 红点
        ModuleManager.register(MConst.Data, new DataModuleFactory());// 通用配置数据commondefine表 OK
        ModuleManager.register(MConst.Gm, new GmModuleFactory()); // gm命令 -
        ModuleManager.register(MConst.Push, new PushModuleFactory()); // 精准推送 OK
        ModuleManager.register(MConst.Role, new RoleModuleFactory()); // 人物 OK
        ModuleManager.register(MConst.Name, new NameModuleFactory()); // 人名 OK
        ModuleManager.register(MConst.Tool, new ToolModuleFactory()); // 道具/背包 OK
        ModuleManager.register(MConst.Skill, new SkillModuleFactory());// 技能 OK(勉强)
        ModuleManager.register(MConst.Scene, new SceneModuleFactory());// 场景 OK
        ModuleManager.register(MConst.Dungeon, new DungeonModuleFactory());// 关卡 OK
        ModuleManager.register(MConst.Drop, new DropModuleFactory());// 掉落 OK
        ModuleManager.register(MConst.Task, new TaskModuleFactory());//任务 OK
        ModuleManager.register(MConst.Title, new TitleModuleFactory());// 称号 OK
        ModuleManager.register(MConst.Induct, new InductModuleFactory());// 引导 OK
        ModuleManager.register(MConst.Chat, new ChatModuleFactory());//聊天 OK
        ModuleManager.register(MConst.SkyTower, new SkyTowerModuleFactory()); //镇妖塔; OK
        ModuleManager.register(MConst.Daily, new DailyModuleFactory());//活跃度或者日常 OK
        ModuleManager.register(MConst.Email, new EmailModuleFactory()); // 邮件 OK -
        ModuleManager.register(MConst.Friend, new FriendModuleFactory()); // 好友 OK -
        ModuleManager.register(MConst.ArroundPlayer, new ArroundPlayerModuleFactory()); // 周围玩家 OK
        ModuleManager.register(MConst.Buddy, new BuddyModuleFactory());// 伙伴 OK
        ModuleManager.register(MConst.SearchTreasure, new SearchTreasureModuleFactory()); // 仙山夺宝 OK
        ModuleManager.register(MConst.Rank, new RankModuleFactory());// 排行榜 OK
        ModuleManager.register(MConst.Family, new FamilyModuleFactory()); // 家族 OK
//        ModuleManager.register(FamilyActExpe, new FamilyActExpeditionModuleFactory()); // 家族活动: 家族远征
        ModuleManager.register(MConst.FamilyActBonfire, new FamilyBonfireModuleFactory()); // 家族篝火 OK
        ModuleManager.register(MConst.FamilyActInvade, new FamilyInvadeModuleFactory());// 家族活动:家族入侵 OK
        ModuleManager.register(MConst.FamilyActTreasure, new FamilyTreasureModuleFactory());//家族活动：家族探宝 OK
        ModuleManager.register(MConst.FamilyActWar, new FamilyActWarModuleFactory()); // 家族活动：家族战 NOT 没上
        ModuleManager.register(MConst.Pk, new PKModuleFacotry()); // PK OK
        ModuleManager.register(MConst.GameCave, new GameCaveModuleFactory()); // 洞府（游园） NOT 没上
        ModuleManager.register(MConst.Shop, new ShopModuleFactory());    // 商店系统 OK
        ModuleManager.register(MConst.CallBoss, new CallBossModuleFactory()); // 召唤boss OK
        ModuleManager.register(MConst.RelationOperation, new RelationOperationModuleFactory()); // 关系操作面板 OK
        ModuleManager.register(MConst.Trump, new TrumpModuleFactory()); // 法宝 NOT 没上
        ModuleManager.register(MConst.Team, new BaseTeamModuleFactory());// 组队 OK
        ModuleManager.register(MConst.TeamDungeon, new TeamDungeonModuleFactory());// 组队副本 OK
        ModuleManager.register(MConst.Ride, new RideModuleFactory()); // 坐骑 OK
        ModuleManager.register(MConst.Mind, new MindModuleFactory()); // 心法(经脉) OK
        ModuleManager.register(MConst.Fashion, new FashionModuleFactory()); // 时装 OK
        ModuleManager.register(MConst.LootTreasure, new LootTreasureModuleFactory()); // 野外夺宝; NOT
        ModuleManager.register(MConst.OfflinePvp, new OfflinePvpModuleFactory()); // 离线pvp(演武场) OK
        ModuleManager.register(MConst.Achievement, new AchievementModuleFactory()); // 成就 OK
        ModuleManager.register(MConst.ForeShow, new ForeShowModuleFactory()); // 系统开放（预告） OK
        ModuleManager.register(MConst.FightingMaster, new FightingMasterModuleFactory());  // 斗神殿 NOT
        ModuleManager.register(MConst.BravePractise, new BravePractiseModuleFactory());  // 勇者试炼 OK
        ModuleManager.register(MConst.NewEquipment, new NewEquipmentModuleFactory());  // 新装备系统 OK
        ModuleManager.register(MConst.GEM, new GemModuleFactory());// 宝石; OK
        ModuleManager.register(MConst.MasterNotice, new MasterNoticeModuleFactory()); // 勇者试炼 OK
        ModuleManager.register(MConst.SignIn, new NewSigninModuleFactory()); // 签到系统 OK
        ModuleManager.register(MConst.Marry, new MarryModuleFactory()); // 结婚系统 OK
        ModuleManager.register(MConst.ServerLog, new ServerLogModuleFactory()); // 日志模块 OK
//        ModuleManager.register(Escort, new EscortModuleFactory()); // 运镖（废弃）
//        ModuleManager.register(TeamPVPGame,new TeamPVPGameModuleFactory());//组队PVP
        ModuleManager.register(MConst.OperateActivity, new OperateActivityModuleFactory()); // 运营活动系统 OK
        ModuleManager.register(MConst.OnlineReward, new OnlineRewardModuleFactory()); // 在线奖励活动 OK
        ModuleManager.register(MConst.RetrieveReward, new RetrieveRewardModuleFactory()); // 奖励找回活动 OK
        ModuleManager.register(MConst.SevenDayGoal, new SevenDayGoalModuleFactory()); // 七日目标活动 OK
        ModuleManager.register(MConst.NewServerRank, new NewServerRankModuleFactory()); // 新服冲级活动 OK
        ModuleManager.register(MConst.NewServerSign, new NewServerSignModuleFactory()); // 新服签到活动 OK
        ModuleManager.register(MConst.Deity, new DeityWeaponModuleFactory()); // 神兵系统; OK
        ModuleManager.register(MConst.Vip, new VipModuleFactory()); // vip(贵族) OK
        ModuleManager.register(MConst.NewServerMoney, new NewServerMoneyModuleFactory()); // 新服活动-撒钱 OK
        ModuleManager.register(MConst.NewServerFightScore, new NewServerFightModuleFactory()); // 新服活动-冲战力 OK
        ModuleManager.register(MConst.Gameboard, new GameboardModuleFactory()); // 游戏公告 OK
        ModuleManager.register(MConst.Poem, new PoemModuleFactory()); // 诗集系统 OK
        ModuleManager.register(MConst.Guest, new GuestModuleFactory()); // 门客系统 OK
        ModuleManager.register(MConst.NewRedbag, new NewRedbagModuleFactory()); // 新版家族红包 OK
        ModuleManager.register(MConst.EliteDungeon, new EliteDungeonModuleFactory()); // 精英副本 OK
//        ModuleManager.register(Cg, new CgModuleFactory());    // CG
        ModuleManager.register(MConst.NewOfflinePvp, new NewOfflinePvpModuleFactory()); // 新竞技场 OK
        ModuleManager.register(MConst.WordExchange, new WordExchangeModuleFactory()); // 集字活动 OK
        ModuleManager.register(MConst.OpActFamilyFightScore, new OpActFamilyFightScoreModuleFactory()); // 家族战力冲榜 OK
        ModuleManager.register(MConst.OpActFightScore, new OpActFightScoreModuleFactory()); // 个人战力冲榜 OK
        ModuleManager.register(MConst.ChargeBack, new ChargeBackModuleFactory()); // 充值返利 OK
        ModuleManager.register(MConst.ServerFund, new ServerFundModuleFactory()); // 开服基金 OK
        ModuleManager.register(MConst.WelfareAccount, new WelfareAccountModuleFactory()); // 福利号 OK
        ModuleManager.register(MConst.GiftCome520, new GiftComeModuleFactory()); // 礼尚往来520 OK
        ModuleManager.register(MConst.BestCP520, new BestCPModuleFactory()); // 最佳组合520 OK
        ModuleManager.register(MConst.DragonBoat, new DragonBoatModuleFactory()); // 端午龙舟 OK
        ModuleManager.register(MConst.Quwudu, new QuwuduModuleFactory()); // 端午驱五毒
        ModuleManager.register(MConst.PopUp, new PopUpModuleFactory()); // 登陆弹窗 OK
        ModuleManager.register(MConst.EverydayCharge, new EverydayChargeModuleFactory()); // 每日首充 OK
        ModuleManager.register(MConst.SendVigour, new SendVigourModuleFactory()); // 送体力 OK
        ModuleManager.register(MConst.DailyCharge, new DailyChargeModuleFactory()); // 日累计充值活动 OK
        ModuleManager.register(MConst.WeeklyCharge, new WeeklyChargeModuleFactory()); // 周累计充值活动 OK
        ModuleManager.register(MConst.ChargePref, new ChargePrefModuleFactory()); // 充值特惠 OK
        ModuleManager.register(MConst.OperateCheck, new OperateCheckModuleFactory()); // CD检测 OK
        ModuleManager.register(MConst.FamilyTask, new FamilyTaskModuleFactory()); // 家族任务 OK
        ModuleManager.register(MConst.OpActChargeScore, new OpActChargeScoreModuleFactory()); // 付费榜 OK
        ModuleManager.register(MConst.OpActKickBack, new OpActKickBackModuleFactory()); // 消费返利 OK
//        ModuleManager.register(FamilyActEscort, new FamilyEscortModuleFactory()); // 家族运镖 废弃
        ModuleManager.register(MConst.Raffle, new RaffleModuleFactory()); // 元宝抽奖 OK
        ModuleManager.register(MConst.TencentVideo, new TencentVideoModuleFactory()); // 元宝抽奖 OK
        ModuleManager.register(MConst.PoemDungeon, new PoemDungeonModuleFactory()); // 诗歌副本
        ModuleManager.register(MConst.Book, new BookModuleFactory()); // 典籍 OK
        ModuleManager.register(MConst.ChargeGift, new ChargeGiftModuleFactory()); // 充值送礼
        ModuleManager.register(MConst.SkyRank, new SkyRankModuleFactory()); // 天梯
        ModuleManager.register(MConst.Daily5v5, new Daily5v5ModuleFactory()); // 日常5v5
        ModuleManager.register(MConst.DiscountGift, new DiscountGiftModuleFactory()); // 优惠豪礼
        ModuleManager.register(MConst.ChangeJob, new ChangeJobModuleFactory()); // 转职
        ModuleManager.register(MConst.Archery, new ArcheryModuleFactory()); // 射箭小游戏
        ModuleManager.register(MConst.TrueName, new TrueNameModuleFactory()); // 实名认证
        ModuleManager.register(MConst.CustomerService, new CustomerServiceModuleFactory()); // 客服服务vip发邮件
        ModuleManager.register(MConst.ActiveWeapon, new ActiveWeaponModuleFactory()); // 活跃神兵
        ModuleManager.register(MConst.FriendShare, new ShareModuleFactory()); // 朋友圈分享
        ModuleManager.register(MConst.FriendInvite, new InviteModuleFactory()); // 好友邀请
        ModuleManager.register(MConst.OpActBenefitToken, new OpActBenefitTokenModuleFactory()); // 符文装备体验副本
        ModuleManager.register(MConst.LevelSpeedUp, new LevelSpeedUpModuleFactory()); // 等级加速
        ModuleManager.register(MConst.GetWay, new GetWayModuleFactory()); // 获取途径
        ModuleManager.register(MConst.PositionSync, new PositionSyncModuleFactory()); // 位置同步
        ModuleManager.register(MConst.WeeklyGift, new WeeklyGiftModuleFactory());
        ModuleManager.register(MConst.CountDown, new CountDownModuleFactory()); //活动预告（倒计时）
        ModuleManager.register(MConst.RuneDungeon, new RuneDungeonModuleFactory()); // 挑战副本（符文副本）
        ModuleManager.register(MConst.Camp, new CampModuleFactory()); // 阵营
        ModuleManager.register(MConst.NewDailyCharge, new NewDailyChargeModuleFactory());//新每日充值
        ModuleManager.register(MConst.GuardOfficial, new GuardOfficialModuleFactory());
        ModuleManager.register(MConst.OldPlayerBack, new OldPlayerBackModuleFactory());//老玩家回归
        ModuleManager.register(MConst.LuckyTurnTable, new LuckyTurnTableModuleFactory());//幸运转盘
        ModuleManager.register(MConst.OpActSecondKill, new OpActSecondKillModuleFactory());//限时秒杀
        ModuleManager.register(MConst.Baby, new BabyModuleFactory());//宝宝模块
        ModuleManager.register(MConst.Refine, new RefineModuleFactory());//道具回收
        ModuleManager.register(MConst.LuckyDraw, new LuckyDrawModuleFactory());//幸运抽奖
        ModuleManager.register(MConst.LuckyDraw1, new LuckyDraw1ModuleFactory());
        ModuleManager.register(MConst.LuckyDraw2, new LuckyDraw2ModuleFactory());
        ModuleManager.register(MConst.LuckyDraw3, new LuckyDraw3ModuleFactory());
        ModuleManager.register(MConst.LuckyDraw4, new LuckyDraw4ModuleFactory());
        ModuleManager.register(MConst.DareGod, new DareGodModuleFactory());//调整女神
        ModuleManager.register(MConst.NewFirstRechargeModule, new NewFirstRechargeModuleFactory());//新每日充值奖励
        ModuleManager.register(MConst.NewFirstRechargeModule1, new NewFirstRecharge1ModuleFactory());
        ModuleManager.register(MConst.CollectPhone, new CollectPhoneModuleFactory());//收集电话号码
        ModuleManager.register(MConst.MoonCake, new MoonCakeModuleFactory());//捡月饼活动
        ModuleManager.register(MConst.LuckyCard, new LuckyCardModuleFactory());//幸运卡牌抽奖
        ModuleManager.register(MConst.FashionCard, new FashionCardModuleFactory());//时装化身
        ModuleManager.register(MConst.OptionalBox, new OptionalBoxModuleFactory());//道具自选
        ModuleManager.register(MConst.Soul, new SoulModuleFactory());//元神系统
        ModuleManager.initDependence();
        ModuleManager.initPacket(); // 初始化数据包
        ModuleManager.init(); // 模块初始化
        com.stars.util.LogUtil.info("完成模块注册");
    }


    private void checkPacket() throws Exception {
        PacketChecker.check();
    }


    private void loadProductData() throws Exception {
        ModuleManager.loadProductData();
    }

    private void loadSystemRecordMap() throws Throwable {
        SystemRecordMap.load();
        // 启动时检查每日重置
        if (System.currentTimeMillis() - SystemRecordMap.dailyResetTimestamp > 24 * 3600 * 1000) {
            com.stars.util.LogUtil.info("启动时每日重置");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);

            SystemRecordMap.update("dailyResetTimestamp", calendar.getTimeInMillis());

//            calendar.set(Calendar.MILLISECOND, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.HOUR, 0);
            SystemRecordMap.update("dateVersion", (int) (calendar.getTimeInMillis() / 1000));
        }

        // 启动时检查每日重置
        if (System.currentTimeMillis() - SystemRecordMap.fiveOClockResetTimestamp > 24 * 3600 * 1000) {
            com.stars.util.LogUtil.info("启动时每日五点重置");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 5);
            SystemRecordMap.update("fiveOClockResetTimestamp", calendar.getTimeInMillis());
        }
    }

    @Override
    public void clear() {

    }


    @Override
    public void dispatch(final com.stars.network.server.packet.Packet packet) {
//        LogUtil.info("收到协议，roleId={}, packetType=0x{}", packet.getRoleId(), Integer.toHexString(packet.getType()));
        /* 访问控制 */
        if (!AccessControl.canAccess(packet.getType())) {
            com.stars.network.server.packet.PacketManager.send(packet.getSession(), new ClientText("功能暂未开放"));
            return;
        }
//        if (RpcManager.handlePacket(packet)) {
//            return;
//        }
        if (packet instanceof ServerReconnect) {//断线重连处理
            ServerReconnect serverReconnect = (ServerReconnect) packet;
            long reconnectRoleId = serverReconnect.getRoleId();
            if (reconnectRoleId != 0) {
                packet.getSession().setRoleId(reconnectRoleId);
            } else { // 选角 & 创角界面不进行断线重连

                return;
            }
        }
        if (packet instanceof com.stars.server.main.message.Disconnected) {
            String accountName = packet.getSession().getAccount();
            if (accountName == null) {
                return;
            }
            com.stars.util.LogUtil.info("发送Disconnected0, account={}", accountName);
            try {
                if (packet.getRoleId() != 0) { // 须比较session（如果存在比较角色的情况下）
                    com.stars.util.LogUtil.info("发送Disconnected1, account={}", accountName);
                    execByPlayer(new OfflineMsg(packet.getRoleId(), packet.getSession()));
                }
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("", t);
            }
        } else {
            final com.stars.network.server.session.GameSession session = packet.getSession();
            if (session != null) {
                if (session.getRoleId() == 0) {
                    execByThread(session, packet);
                } else {
                    // 判断
                    ServiceActor service = ServiceHelper.serviceMapByPacketType.get(packet.getType());
                    if (service != null) {
                        service.tell(packet, com.stars.core.actor.Actor.noSender);
                    } else {
                        execByPlayer(packet);
                    }
                }
            }
        }
    }


    private void execByThread(final GameSession session, final com.stars.network.server.packet.Packet packet) {
        ExecuteManager.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    packet.execPacket();
                } catch (Exception e) {
                    if (!session.isServerSession()) {
                        PacketUtil.error(session);
                    }
                    com.stars.util.LogUtil.error("packet execute error roleid:" + session.getRoleId(), e);
                }
            }
        });
    }

    private boolean execByPlayer(final Packet packet) {
        Player player = PlayerSystem.get(packet.getRoleId());
        if (player != null) {
            if (packet instanceof Disconnected) {
                LogUtil.info("发送Disconnected4, roleId={}", packet.getRoleId());
            }
            player.tell(packet, Actor.noSender);
            return true;
        }
        if (packet instanceof OfflineMsg) {
            ((OfflineMsg) packet).finish(true);
        }
        if (packet instanceof ServerReconnect) {
            com.stars.network.server.packet.PacketManager.send(packet.getSession(), new ClientReconnect(false));
        }
        return false;
    }

    private void registerOtherPacket() throws Exception {
        PacketManager.register(LuaFrameDataBack.class);// 战斗服返回数据
    }

    private void loadServerChannel() {
        try {
            _HashMap map = DBUtil.querySingleMap(DBUtil.DB_LOGIN, "select * from common where common.key='serverchannel'");
            if (map != null && map.size() >= 1) {
                String str = (String) map.get("common.value");
                serverChannel = str.trim();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void initGameServerConfig() {
        Properties prop = com.stars.bootstrap.ServerManager.getServer().getConfig().getProps().get(
                ServerManager.getServer().getConfig().getServer());
        // 游戏服入口
        String openServer = prop.getProperty("openServer");
        if (openServer != null && openServer.equals("true")) {
            LoginModuleHelper.serverState = SwitchEntranceGm.OPEN;
        } else {
            LoginModuleHelper.serverState = SwitchEntranceGm.CLOSE;
        }
        // 游戏服GM
        String openGameGm = prop.getProperty("openGameGm");
        if (openGameGm != null && openGameGm.equals("true")) {
            isOpenGameGm = true;
        } else {
            isOpenGameGm = false;
        }
    }

}
