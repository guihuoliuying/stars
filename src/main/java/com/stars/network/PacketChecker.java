package com.stars.network;

import com.stars.modules.achievement.AchievementPacketSet;
import com.stars.modules.activeweapon.ActiveWeaponPacketSet;
import com.stars.modules.archery.ArcheryPacketSet;
import com.stars.modules.arroundPlayer.ArroundPlayerPacketSet;
import com.stars.modules.authentic.AuthenticPacketSet;
import com.stars.modules.baby.BabyPacketSet;
import com.stars.modules.baseteam.BaseTeamPacketSet;
import com.stars.modules.bestcp520.BestCPPacketSet;
import com.stars.modules.book.BookPacketSet;
import com.stars.modules.bravepractise.BravePractisePacketSet;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.modules.camp.CampPackset;
import com.stars.modules.cg.CgPacketSet;
import com.stars.modules.changejob.ChangeJobPacketSet;
import com.stars.modules.chargeback.ChargeBackPacketSet;
import com.stars.modules.chargegift.ChargeGiftPacketSet;
import com.stars.modules.chargepreference.ChargePrefPacketSet;
import com.stars.modules.chat.ChatPacketSet;
import com.stars.modules.collectphone.CollectPhonePacketSet;
import com.stars.modules.countDown.CountDownPacketSet;
import com.stars.modules.customerService.CustomerServicePacketSet;
import com.stars.modules.daily.DailyPacketSet;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.modules.dailyCharge.DailyChargePacketSet;
import com.stars.modules.daregod.DareGodPacketSet;
import com.stars.modules.deityweapon.DeityWeaponPacketSet;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.modules.discountgift.DiscountGiftPacketSet;
import com.stars.modules.dragonboat.DragonBoatPacketSet;
import com.stars.modules.dungeon.DungeonPacketSet;
import com.stars.modules.elitedungeon.EliteDungeonPacketSet;
import com.stars.modules.email.EmailPacketSet;
import com.stars.modules.escort.EscortPacketSet;
import com.stars.modules.everydaycharge.EverydayChargePacketSet;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.modules.familyTask.FamilyTaskPacketSet;
import com.stars.modules.familyactivities.treasure.FamilyTreasurePacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.fashion.FashionPacketSet;
import com.stars.modules.fashioncard.FashionCardPacketSet;
import com.stars.modules.fightingmaster.FightingMasterPacketSet;
import com.stars.modules.foreshow.ForeShowPacketSet;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.modules.friendInvite.InvitePacketSet;
import com.stars.modules.friendShare.SharePacketSet;
import com.stars.modules.gameboard.GameboardPacketSet;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.modules.gem.GemPacketSet;
import com.stars.modules.getway.GetWayPacketSet;
import com.stars.modules.giftcome520.GiftComePacketSet;
import com.stars.modules.gm.GmPacketSet;
import com.stars.modules.guardofficial.GuardOfficialPacketSet;
import com.stars.modules.guest.GuestPacketSet;
import com.stars.modules.induct.InductPacketSet;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.modules.luckycard.LuckyCardPacketSet;
import com.stars.modules.luckydraw.LuckyDrawPacketSet;
import com.stars.modules.luckyturntable.LuckyTurnTablePacketSet;
import com.stars.modules.marry.MarryPacketSet;
import com.stars.modules.masternotice.MasterNoticePacketSet;
import com.stars.modules.mind.MindPacketSet;
import com.stars.modules.mooncake.MoonCakePacketSet;
import com.stars.modules.name.NamePacketSet;
import com.stars.modules.newdailycharge.NewDailyChargePacketSet;
import com.stars.modules.newequipment.NewEquipmentPacketSet;
import com.stars.modules.newfirstrecharge.NewFirstRechargePackets;
import com.stars.modules.newofflinepvp.NewOfflinePvpPacketSet;
import com.stars.modules.newredbag.NewRedbagPacketSet;
import com.stars.modules.newserverfightscore.NewServerFightPacketSet;
import com.stars.modules.newservermoney.NewServerMoneyPacketSet;
import com.stars.modules.newserverrank.NewServerRankPacketSet;
import com.stars.modules.newserversign.NewServerSignPacketSet;
import com.stars.modules.newsignin.NewSigninPacketSet;
import com.stars.modules.offlinepvp.OfflinePvpPacketSet;
import com.stars.modules.oldplayerback.OldPalyerBackPacketSet;
import com.stars.modules.onlinereward.OnlineRewardPacketSet;
import com.stars.modules.opactchargescore.OpActChargeScorePacketSet;
import com.stars.modules.opactfamilyfightscore.OpActFamilyFightScorePacketSet;
import com.stars.modules.opactfightscore.OpActFightScorePacketSet;
import com.stars.modules.opactkickback.OpActKickBackPacketSet;
import com.stars.modules.opactsecondskill.OpActSecondKillPacketSet;
import com.stars.modules.operateactivity.OperateActivityPacketSet;
import com.stars.modules.optionalbox.OptionalBoxPacketSet;
import com.stars.modules.pk.PKPacketSet;
import com.stars.modules.poem.PoemPacketSet;
import com.stars.modules.poemdungeon.PoemDungeonPacketSet;
import com.stars.modules.popUp.PopUpPacketSet;
import com.stars.modules.quwudu.QuwuduPacketSet;
import com.stars.modules.raffle.RafflePacketSet;
import com.stars.modules.rank.RankPacketSet;
import com.stars.modules.redpoint.RedPointPacketSet;
import com.stars.modules.refine.RefinePacketSet;
import com.stars.modules.relationoperation.RelationOperationPacketSet;
import com.stars.modules.retrievereward.RetrieveRewardPacketSet;
import com.stars.modules.ride.RidePacketSet;
import com.stars.modules.role.RolePacketSet;
import com.stars.modules.runeDungeon.RuneDungeonPacketSet;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.sendvigour.SendVigourPacketSet;
import com.stars.modules.serverfund.ServerFundPacketSet;
import com.stars.modules.sevendaygoal.SevenDayGoalPacketSet;
import com.stars.modules.shop.ShopPacketSet;
import com.stars.modules.skill.SkillPacketSet;
import com.stars.modules.skyrank.SkyRankPacketSet;
import com.stars.modules.skytower.SkyTowerPacketSet;
import com.stars.modules.soul.SoulPacketSet;
import com.stars.modules.system.SystemPacketSet;
import com.stars.modules.task.TaskPacketSet;
import com.stars.modules.teamdungeon.TeamDungeonPacketSet;
import com.stars.modules.title.TitlePacketSet;
import com.stars.modules.tool.ToolPacketSet;
import com.stars.modules.truename.TrueNamePacketSet;
import com.stars.modules.trump.TrumpPacketSet;
import com.stars.modules.vip.VipPacketSet;
import com.stars.modules.weeklyCharge.WeeklyChargePacketSet;
import com.stars.modules.weeklygift.WeeklyGiftPacketSet;
import com.stars.modules.welfareaccount.WelfareAccountPacketSet;
import com.stars.modules.wordExchange.WordExchangePacketSet;
import com.stars.network.server.packet.Packet;

import java.util.*;

/**
 * <h1>数据包命名规范</h1>
 * <h2>服务间数据包（类命名）</h2>
 * <ul>
 * <li>*G2fPkt -- 游戏服到战斗服</li>
 * <li>*F2gPkt -- 战斗服到游戏服</li>
 * <li>*G2gPkt -- 游戏服到游戏服</li>
 * <li>*G2cPkt -- 游戏服到公共服</li>
 * <li>*C2gPkt -- 公共服到游戏服</li>
 * </ul>
 * <p>
 * <h2>客户端/服务端（包括公共服）数据包（类命名）</h2>
 * <ul>
 * <li>Server* -- 客户端请求服务端</li>
 * <li>Client* -- 服务端下发到客户端</li>
 * </ul>
 * <p>
 * <h1>PacketType编码规范：</h1>
 * <p>按模块区分，每个模块至少分配4个协议编码（编码范围0x0001-0x6FFF,从0x0001开始，按顺序递增，协议多的模块
 * 可分配n*4个,需在编码范围写清楚），没有用到的数字保留，其他模块不可占用，模块之间按顺序递增分配，
 * 不可跳过，模块协议前后需要写清楚模块开始和结束，编码范围,例子如下：</p>
 * <p>
 * <h1>分配规则</h1>
 * <ul>
 * <li>0x0001 - 0x000F 16  系统（心跳/通用提示/登录/创角）</li>
 * <li>0x0010 - 0x001F 16  人物</li>
 * <li>0x0020 - 0x002F 16  取名</li>
 * <li>0x0030 - 0x003F 16  道具</li>
 * <li>0x0040 - 0x004F 16  NPC</li>
 * <li>0x0050 - 0x005F 16  关卡</li>
 * <li>0x0060 - 0x006F 16  装备</li>
 * <p>
 * <li>0x7B00 - 0x7B3F 64  战斗</li>
 * <li>0x7C00 - 0x7CFF 256 保留</li>
 * <li>0x7D00 - 0x7DFF 256 GM</li>
 * <li>0x7E00 - 0x7EFF 256 测试</li>
 * <li>0x7F00 - 0x7FFF 256 底层</li>
 * </ul>
 */
public class PacketChecker {

    private static Set<Short> packetTypeWithoutCacheSet = new HashSet<>();

    public static void check() throws Exception {

        Map<Short, Class<? extends com.stars.network.server.packet.Packet>> packetTypes = new HashMap<>();
        byte[] bitmap = new byte[Short.MAX_VALUE];

        /* 注意协议号的顺序（至少是4的倍数） */
        check((short) 0x0000, (short) 0x000F, packetTypes, bitmap, new LoginPacketSet()); // 登录
        check((short) 0x0010, (short) 0x0019, packetTypes, bitmap, new RolePacketSet()); // 人物

        check((short) 0x0020, (short) 0x0025, packetTypes, bitmap, new NamePacketSet());// 取名
        check((short) 0x0026, (short) 0x0029, packetTypes, bitmap, new ChatPacketSet());//聊天
        check((short) 0x002a, (short) 0x002d, packetTypes, bitmap, new DailyPacketSet());//活跃度
        check((short) 0x002e, (short) 0x0030, packetTypes, bitmap, new ArroundPlayerPacketSet());//周围玩家
        check((short) 0x0031, (short) 0x0035, packetTypes, bitmap, new ToolPacketSet()); // 道具
        check((short) 0x0036, (short) 0x003A, packetTypes, bitmap, new TaskPacketSet());//任务

        check((short) 0x0040, (short) 0x0043, packetTypes, bitmap, new TitlePacketSet());// 称号
        check((short) 0x0044, (short) 0x0049, packetTypes, bitmap, new SkillPacketSet());//技能

        check((short) 0x0050, (short) 0x0055, packetTypes, bitmap, new DungeonPacketSet()); // 关卡
        check((short) 0x0056, (short) 0x0059, packetTypes, bitmap, new InductPacketSet());// 引导

        check((short) 0x0060, (short) 0x006F, packetTypes, bitmap, new GemPacketSet()); // 装备
        check((short) 0x0070, (short) 0x0079, packetTypes, bitmap, new BuddyPacketSet()); // 伙伴

        check((short) 0x0080, (short) 0x0083, packetTypes, bitmap, new SkyTowerPacketSet()); // 镇妖塔;
//		check((short) 0x0084, (short) 0x008F, packetTypes, bitmap, new SearchTreasurePacketSet()); // 仙山夺宝;
        check((short) 0x0090, (short) 0x0091, packetTypes, bitmap, new ShopPacketSet()); // 金币商城

        check((short) 0x0100, (short) 0x010F, packetTypes, bitmap, new PKPacketSet()); //pk
        check((short) 0x0110, (short) 0x0113, packetTypes, bitmap, new RelationOperationPacketSet()); // 关系面板
//      check((short) 0x0114, (short) 0x011F, packetTypes, bitmap, new GameCavePacketSet()); //洞府
        check((short) 0x0120, (short) 0x0127, packetTypes, bitmap, new RidePacketSet()); // 坐骑

        check((short) 0x0130, (short) 0x0137, packetTypes, bitmap, new TrumpPacketSet());    // 法宝
        check((short) 0x0138, (short) 0x013F, packetTypes, bitmap, new MindPacketSet());    // 心法
        check((short) 0x0140, (short) 0x0141, packetTypes, bitmap, new TeamDungeonPacketSet());// 组队副本
        check((short) 0x0142, (short) 0x014F, packetTypes, bitmap, new BaseTeamPacketSet());// 组队
        check((short) 0x0150, (short) 0x0155, packetTypes, bitmap, new FashionPacketSet());    // 时装
        check((short) 0x0156, (short) 0x0159, packetTypes, bitmap, new AchievementPacketSet());    // 成就
        check((short) 0x0160, (short) 0x0163, packetTypes, bitmap, new ForeShowPacketSet());//系统开放预告
//		check((short) 0x0166, (short) 0x016F, packetTypes, bitmap, new GemPacketSet());//宝石
        check((short) 0x0164, (short) 0x0167, packetTypes, bitmap, new NewSigninPacketSet());//签到系统
        check((short) 0x0168, (short) 0x016F, packetTypes, bitmap, new BravePractisePacketSet());//勇者试炼
//		check((short) 0x0170, (short) 0x0173, packetTypes, bitmap, new RedPointPacketSet());//红点
        check((short) 0x0171, (short) 0x017A, packetTypes, bitmap, new MasterNoticePacketSet());//皇榜悬赏
        check((short) 0x017B, (short) 0x017F, packetTypes, bitmap, new RedPointPacketSet());//红点
        check((short) 0x0180, (short) 0x0183, packetTypes, bitmap, new NewEquipmentPacketSet());//新装备系统
        check((short) 0x0184, (short) 0x0187, packetTypes, bitmap, new MarryPacketSet());    // 结婚系统
        check((short) 0x0188, (short) 0x018F, packetTypes, bitmap, new VipPacketSet());    // vip(贵族系统)
        check((short) 0x0190, (short) 0x0197, packetTypes, bitmap, new OperateActivityPacketSet());    // 运营活动系统
        check((short) 0x0198, (short) 0x019B, packetTypes, bitmap, new OnlineRewardPacketSet());    // 在线奖励活动
        check((short) 0x019C, (short) 0x019F, packetTypes, bitmap, new EscortPacketSet());    // 运镖系统
        check((short) 0x01A0, (short) 0x01A4, packetTypes, bitmap, new RetrieveRewardPacketSet());    // 奖励找回活动
        check((short) 0x01A5, (short) 0x01AD, packetTypes, bitmap, new SevenDayGoalPacketSet());    // 七日目标活动
        check((short) 0x01AE, (short) 0x01B4, packetTypes, bitmap, new NewServerRankPacketSet());    // 新服冲级活动
        check((short) 0x01B5, (short) 0x01B8, packetTypes, bitmap, new NewServerSignPacketSet());    // 新服签到活动
        check((short) 0x01B9, (short) 0x01BC, packetTypes, bitmap, new AuthenticPacketSet());//鉴宝
        check((short) 0x01C0, (short) 0x01C3, packetTypes, bitmap, new GuestPacketSet());    // 门客系统
        check((short) 0x01C4, (short) 0x01C7, packetTypes, bitmap, new NewServerMoneyPacketSet());    // 新服活动-撒钱
        check((short) 0x01C8, (short) 0x01DB, packetTypes, bitmap, new NewServerFightPacketSet());    // 新服活动-冲战力
        check((short) 0x01DC, (short) 0x01DD, packetTypes, bitmap, new GameboardPacketSet());//游戏公告
        check((short) 0x01DE, (short) 0x01E6, packetTypes, bitmap, new PoemPacketSet());//诗歌系统
        check((short) 0x01E7, (short) 0x020A, packetTypes, bitmap, new GameCavePacketSet()); //新洞府
        check((short) 0x0210, (short) 0x0213, packetTypes, bitmap, new NewRedbagPacketSet());    // 新版家族红包
        check((short) 0x0214, (short) 0x0217, packetTypes, bitmap, new ChargeBackPacketSet());    // 充值返利
        check((short) 0x0218, (short) 0x0219, packetTypes, bitmap, new QuwuduPacketSet());    // 端午驱五毒
        check((short) 0x021B, (short) 0x021E, packetTypes, bitmap, new ServerFundPacketSet());//开服基金
        check((short) 0x021F, (short) 0x0221, packetTypes, bitmap, new EverydayChargePacketSet());//每日首充
        check((short) 0x0222, (short) 0x0229, packetTypes, bitmap, new SendVigourPacketSet());//送体力活动
        check((short) 0x022A, (short) 0x022F, packetTypes, bitmap, new ChargePrefPacketSet()); // 充值特惠
        check((short) 0x0230, (short) 0x0239, packetTypes, bitmap, new EliteDungeonPacketSet()); // 组队精英副本
        check((short) 0x023A, (short) 0x023D, packetTypes, bitmap, new WelfareAccountPacketSet()); // 福利号
        check((short) 0x023E, (short) 0x0241, packetTypes, bitmap, new GiftComePacketSet()); // 礼尚往来520
        check((short) 0x0242, (short) 0x024F, packetTypes, bitmap, new FamilyEscortPacketSet()); // 家族运镖
        check((short) 0x0250, (short) 0x0253, packetTypes, bitmap, new BestCPPacketSet()); // 最佳组合520
        check((short) 0x0254, (short) 0x0259, packetTypes, bitmap, new RafflePacketSet()); // 元宝抽奖
        check((short) 0x025A, (short) 0x025D, packetTypes, bitmap, new DragonBoatPacketSet()); // 赛龙舟
        check((short) 0x025E, (short) 0x0264, packetTypes, bitmap, new PoemDungeonPacketSet()); // 诗歌副本
        check((short) 0x0266, (short) 0x026A, packetTypes, bitmap, new BookPacketSet()); // 典籍
        check((short) 0x026B, (short) 0x026E, packetTypes, bitmap, new ChargeGiftPacketSet()); // 充值送礼
        check((short) 0x026F, (short) 0x0275, packetTypes, bitmap, new SkyRankPacketSet()); // 天梯
        check((short) 0x0276, (short) 0x027B, packetTypes, bitmap, new WeeklyChargePacketSet()); // 周累计充值
        check((short) 0x027C, (short) 0x027F, packetTypes, bitmap, new DiscountGiftPacketSet()); // 优惠豪礼
        check((short) 0x0280, (short) 0x0283, packetTypes, bitmap, new ChangeJobPacketSet());//转职
        check((short) 0x0284, (short) 0x0287, packetTypes, bitmap, new SharePacketSet()); // 朋友圈分享
        check((short) 0x0288, (short) 0x0289, packetTypes, bitmap, new ArcheryPacketSet()); //射箭活动
        check((short) 0x028A, (short) 0x028D, packetTypes, bitmap, new InvitePacketSet()); //好友邀请
        check((short) 0x028E, (short) 0x0291, packetTypes, bitmap, new TrueNamePacketSet()); //实名认证
        check((short) 0x0292, (short) 0x029F, packetTypes, bitmap, new PlaceholderPacketSet()); // 符文装备体验副本
        check((short) 0x02A0, (short) 0x02A3, packetTypes, bitmap, new ActiveWeaponPacketSet());//活跃神兵
        check((short) 0x02A4, (short) 0x02A7, packetTypes, bitmap, new GetWayPacketSet()); // 获取途径
        check((short) 0x02A8, (short) 0x02A9, packetTypes, bitmap, new CustomerServicePacketSet()); // vip玩家信息记录
        check((short) 0x02AA, (short) 0x02AC, packetTypes, bitmap, new RuneDungeonPacketSet()); // 符文副本（挑战副本）
        check((short) 0x02AD, (short) 0x02AF, packetTypes, bitmap, new CountDownPacketSet()); // 活动预告（倒计时）
        check((short) 0x02B0, (short) 0x02CF, packetTypes, bitmap, new CampPackset()); // 阵营
        check((short) 0x02D0, (short) 0x02D3, packetTypes, bitmap, new NewDailyChargePacketSet());//新的日累计充值
        check((short) 0x02D4, (short) 0x02D7, packetTypes, bitmap, new GuardOfficialPacketSet());//守护官职
        check((short) 0x02D8, (short) 0x02DB, packetTypes, bitmap, new LuckyTurnTablePacketSet());//幸运转盘
        check((short) 0x02DC, (short) 0x02DF, packetTypes, bitmap, new OldPalyerBackPacketSet());//老玩家回归
        check((short) 0x02E0, (short) 0x02EF, packetTypes, bitmap, new BabyPacketSet());
        check((short) 0x02F0, (short) 0x02F1, packetTypes, bitmap, new RefinePacketSet());
        check((short) 0x02F2, (short) 0x02F5, packetTypes, bitmap, new LuckyDrawPacketSet());//幸运抽奖
        check((short) 0x02F6, (short) 0x02F9, packetTypes, bitmap, new NewFirstRechargePackets());//首充每日奖励
        check((short) 0x02FA, (short) 0x02FD, packetTypes, bitmap, new CollectPhonePacketSet());//手机号码收集
        check((short) 0x02FE, (short) 0x0301, packetTypes, bitmap, new MoonCakePacketSet());//接月饼小游戏
        check((short) 0x0302, (short) 0x0305, packetTypes, bitmap, new LuckyCardPacketSet());//幸运卡牌
        check((short) 0x0306, (short) 0x0309, packetTypes, bitmap, new FashionCardPacketSet());//时装化身
        check((short) 0x0310, (short) 0x0313, packetTypes, bitmap, new SoulPacketSet());//元神系统
        check((short) 0x0314, (short) 0x0317, packetTypes, bitmap, new OptionalBoxPacketSet());//道具自选
        /* 公共业务 */
        check((short) 0x6000, (short) 0x6007, packetTypes, bitmap, new EmailPacketSet()); // 邮件
        check((short) 0x6008, (short) 0x601F, packetTypes, bitmap, new FriendPacketSet()); // 好友
        check((short) 0x6020, (short) 0x604F, packetTypes, bitmap, new FamilyPacketSet()); // 家族（基础）
        check((short) 0x6050, (short) 0x6057, packetTypes, bitmap, new RankPacketSet()); // 排行榜
        check((short) 0x6058, (short) 0x6067, packetTypes, bitmap, new CallBossPacketSet()); // 召唤boss
        check((short) 0x6068, (short) 0x6167, packetTypes, bitmap, new PlaceholderPacketSet()); // 家族（活动）
        check((short) 0x6170, (short) 0x6177, packetTypes, bitmap, new OfflinePvpPacketSet());// 离线pvp(演武场)
//		check((short) 0x6180, (short) 0x618F, xxxxx, xxxx, xxxx);// 离线pvp(演武场)//此段用于跨服功能
        check((short) 0x6190, (short) 0x619F, packetTypes, bitmap, new LootTreasurePacketSet()); // 野外夺宝（活动）
        check((short) 0x61A0, (short) 0x61A7, packetTypes, bitmap, new FightingMasterPacketSet());    // 斗神殿
        check((short) 0x61AA, (short) 0x61AF, packetTypes, bitmap, new DeityWeaponPacketSet());    // 神兵;
        check((short) 0x61B0, (short) 0x61DF, packetTypes, bitmap, new FamilyActWarPacketSet()); // 家族战
//		check((short) 0x61E0, (short) 0x61FF, packetTypes, bitmap, new TeamPVPGamePacketSet()); // 组队pvp
        check((short) 0x6200, (short) 0x6204, packetTypes, bitmap, new FamilyTreasurePacket());//家族探宝
        check((short) 0x6205, (short) 0x6208, packetTypes, bitmap, new NewOfflinePvpPacketSet());//新版竞技场
        check((short) 0x6209, (short) 0x620C, packetTypes, bitmap, new CgPacketSet());//CG
        check((short) 0x6210, (short) 0x6213, packetTypes, bitmap, new WordExchangePacketSet());//CG
        check((short) 0x6214, (short) 0x6218, packetTypes, bitmap, new OpActFamilyFightScorePacketSet());//家族战力冲榜
        check((short) 0x6219, (short) 0x621C, packetTypes, bitmap, new OpActFightScorePacketSet());
        check((short) 0x6220, (short) 0x6223, packetTypes, bitmap, new PopUpPacketSet());
        check((short) 0x6224, (short) 0x6228, packetTypes, bitmap, new FamilyTaskPacketSet());
        check((short) 0x6229, (short) 0x622C, packetTypes, bitmap, new DailyChargePacketSet());
        check((short) 0x622D, (short) 0x622E, packetTypes, bitmap, new OpActChargeScorePacketSet());//充值榜
        check((short) 0x622F, (short) 0x6230, packetTypes, bitmap, new OpActKickBackPacketSet());//消费返利
        check((short) 0x6231, (short) 0x6243, packetTypes, bitmap, new Daily5v5PacketSet()); // 日常5v5
        check((short) 0x6244, (short) 0x6247, packetTypes, bitmap, new WeeklyGiftPacketSet());
        check((short) 0x6248, (short) 0x6249, packetTypes, bitmap, new OpActSecondKillPacketSet());//限时秒杀
        check((short) 0x624A, (short) 0x624D, packetTypes, bitmap, new DareGodPacketSet());


		/* 特殊 */
        check((short) 0x7B00, (short) 0x7B3F, packetTypes, bitmap, new ScenePacketSet());// 场景
        //0x7C00 - 0x7CFF 登陆服


        check((short) 0x7D00, (short) 0x7DFF, packetTypes, bitmap, new GmPacketSet()); // GM

        // 0x7E00 - 0x7EFF 256 测试
        check((short) 0x7F00, (short) 0x7FFF, packetTypes, bitmap, new SystemPacketSet()); // 底层


		/*
         * 设置不需缓存的PacketType
		 */
        addPacketTypeWithoutCache(ArroundPlayerPacketSet.Client_ArroundPlayer); // 周围玩家
        addPacketTypeWithoutCache(ArroundPlayerPacketSet.Server_Heartbeat); // 心跳(上行)
        addPacketTypeWithoutCache(ArroundPlayerPacketSet.Client_Heartbeat); // 心跳(下行)
        addPacketTypeWithoutCache(ChatPacketSet.Server_ChatMessage); // 聊天(上行)
        addPacketTypeWithoutCache(ChatPacketSet.Client_ChatMessage); // 聊天(下行)
        addPacketTypeWithoutCache(NewServerRankPacketSet.C_NEW_SERVER_RANK); // 新服排名
        addPacketTypeWithoutCache(LoginPacketSet.S_RECONNECT); // 重连(上行)
        addPacketTypeWithoutCache(LoginPacketSet.C_RECONNECT); // 重连(下行)
    }


    public static void check(short min, short max, Map<Short, Class<? extends com.stars.network.server.packet.Packet>> packetTypes,
                             byte[] bitmap, PacketSet... sets) throws Exception {

        for (int i = min; i < max; i++) {
            if (bitmap[i] == 1) {
                throw new RuntimeException("预定PacketType范围有重复(" + min
                        + ", " + max + ")");
            }
            bitmap[i] = 1;
        }
        for (PacketSet set : sets) {
            List<Class<? extends com.stars.network.server.packet.Packet>> al = set.getPacketList();
            for (Class<? extends com.stars.network.server.packet.Packet> clazz : al) {
                com.stars.network.server.packet.Packet packet = clazz.newInstance();
                short type = packet.getType();
                Class<? extends Packet> oldClazz = packetTypes.get(type);
                if (oldClazz != null) {
                    throw new RuntimeException(
                            "协议类型重复: " + Integer.toHexString(type) + ", "
                                    + clazz.getSimpleName() + ", " + oldClazz.getSimpleName());
                }
                packetTypes.put(type, clazz);

                if (type < min || type > max) {
                    throw new RuntimeException("协议类型越界: " + clazz.getName() + "," + type + ", (" + min + "," + max + ")");
                }

            }
        }
    }

    public static void addPacketTypeWithoutCache(short packetType) {
        packetTypeWithoutCacheSet.add(packetType);
    }

    public static boolean needCache(short packetType) {
        return !packetTypeWithoutCacheSet.contains(packetType);
    }
}
