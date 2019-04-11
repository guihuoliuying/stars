package com.stars.core.gmpacket;


import com.stars.core.gmpacket.email.*;
import com.stars.core.gmpacket.giftpackage.CreateGiftPackageGm;
import com.stars.core.gmpacket.giftpackage.ExchangeGiftPackageGm;
import com.stars.core.gmpacket.specialaccount.AddSpecialAccountGm;
import com.stars.core.gmpacket.specialaccount.DelSpecialAccountGm;
import com.stars.core.gmpacket.specialaccount.QuerySpecialAccountDetailGm;
import com.stars.core.gmpacket.specialaccount.QuerySpecialAccountListGm;
import com.stars.core.gmpacket.util.QueryHotUpdateInfoGm;
import com.stars.server.main.gmpacket.GmPacketManager;

/**
 * Created by liuyuheng on 2016/12/9.
 */
public class GmPacketDefine {
    // 运维GM接口号
    public static int OPEN_CLOSE_ENTRY = 1001;// 开启/关闭入口
    public static int KICK_OFF_PLAYER = 1002;// 踢玩家下线
    public static int SAVE_DATA = 1003;// 保存游戏数据
    public static int QUERY_ONLINE_PLAYER_COUNT = 1004;// 查看在线人数
    public static int OPEN_OR_CLOSE_WHITE_LIST = 1005;    // 打开或者关闭白名单
    public static int ADD_OR_DEL_WHITE_LIST = 1006;    // 添加或删除白名单
    public static int SINGLE_AREA_NUM_UP_LIMIT = 1007;//设置单区人数上限
    public static int QUEEY_SERVER_STATUS = 1009;//查询服务状态
    public static int QUERY_KICK_OFF_PROGRESS = 1010;//查询数据保存进度
    public static int SET_SERVER_OPENTIME = 1051;//设置开服时间
    public static int LOOK_SERVER_OPENTIME = 1052;//查看开服时间
    public static int GAME_DATARELOAD = 1011;//数据重载
    public static int SET_LOGINTIPS = 1053;
    public static int SET_CHARGE_SWITCH = 1008; // 充值开关
    public static int QUERY_HOT_UPDATE_INFO = 1055;
    public static int FAMILY_WAR_TRIGGER = 1054;
    public static int ANNOUNCE_ALL_PLAYER = 1080; //走马灯通知所有玩家

    public static int HOTUPDATE_COMMAND = 9998;    //热更commManager

    //运营Gm接口号
    public static int QUERY_PLAYER_BASIC_INFO = 20001;    //查询玩家基本信息
    public static int QUEEY_SERVER_STATUS0 = 20002;//查询服务状态
    public static int PUBLISH_OR_EDIT_LOGINBOARD = 20003;//登陆内公告发布、修改
    public static int QUERY_LOGINBOARD = 20004;//登陆内公告发布、修改
    public static int PUBLISH_OR_EDIT_GAMEBOARD = 20005;//游戏内公告发布、修改
    public static int QUERY_GAMEBOARD = 20006;//游戏内公告查询
    public static int DEL_GAMBOARD = 20007;//游戏内删除公告
    public static int PUBLISH_EDIT_LOOPNOTICE = 20008;// 循环公告发布、修改
    public static int QUERY_LOOPNOTICE = 20009;// 循环公告查询
    public static int DELETE_LOOPNOTICE = 20010;// 循环公告删除
    public static int BLOCK_ACCOUNT = 20023;// 封号
    public static int RELEASE_BLOCK_ACCOUNT = 20024;// 解封
    public static int FORBID_ROLE_CHAT = 20025;// 设置禁言
    public static int RELEASE_ROLE_CHAT = 20026;// 解除禁言
    public static int REDUCE_ROLE_GOLD = 20027;// 减少充值货币(元宝,绑定元宝)
    public static int REDUCE_ROLE_MONEY = 20028;// 减少游戏币
    public static int QUERY_PLAYER_DETAIL = 20030;     //玩家详细信息查询
    public static int CREATE_GIFT_PACKAGE = 20038;// 生成礼包
    public static int EXCHANGE_GIFT_PACKAGE = 20039;// 兑换礼包
    public static int MODIFY_ROLE_LEVEL = 20075;// 修改角色等级
    public static int ADD_SPECIAL_ACCOUNT = 28005;//
    public static int DEL_SPECIAL_ACCOUNT = 28006;//
    public static int QUERY_SPECIAL_ACCOUNT_LIST = 28007;//
    public static int QUERY_SPECIAL_ACCOUNT_DETAIL = 28008;//
    public static int ACCOUNT_TRANSFER = 29000;//账号转移
    public static int ACCOUNT_TRANSFER_BACK = 29001;//账号转移回滚

    public static int ALL_EMAIL_SEND = 20014; // 发送全服邮件
    public static int ALL_EMAIL_EDIT = 20015; // 修改全服邮件状态
    public static int ALL_EMAIL_VIEW = 20040; // 查询全服邮件
    public static int EMAIL_SEND = 20016; // 发送邮件
    public static int GM_MESSAGE_REDPOINT = 20019; // 客服回复通知红点提醒
    public static int GM_COMMAND = 20020; // gm命令执行
    public static int EMAIL_VIEW = 20021; // 查询邮件
    public static int EMAIL_DELE = 20022; // 发送邮件

    public static int QUERY_PLAYER_BAG = 20035; // 查询玩家包裹
    public static int DEL_PLAYER_BAG = 20036; // 删除玩家包裹
    public static int QUERY_PLAYER_EQUIP = 20037; // 查询玩家身上穿戴的装备
    public static int QUERY_PLAYER_FRIEND = 20032; // 玩家好友查询

    public static int QUERY_PLAYER_RANK_INFO = 20071; // 玩家排行榜
    public static int QUERY_FAMILY_RANK_INFO = 20072; // 公会排行榜
    public static int QUERY_FAMILY_INFO = 20073; // 公会信息
    public static int QUERY_FAMILY_MENBER_INFO = 20074; // 公会成员信息
    public static int UCCAR_QUERY_USER_ROLE_BY_ACCOUNT = 30000;//UC直通车gm 查询用户角色通过uid
    public static int UCCAR_GIFT_SEND = 30001;//UC直通车gm 礼包发放
    public static int UCCAR_GIFT_CHECK = 30002;//UC直通车gm 礼包检测

    public static void regHandler() {

    }

    public static void reg() {
        GmPacketManager.regGmRequestHandler(OPEN_CLOSE_ENTRY, SwitchEntranceGm.class);
        GmPacketManager.regGmRequestHandler(KICK_OFF_PLAYER, KickOffPlayerGm.class);
        GmPacketManager.regGmRequestHandler(SAVE_DATA, SaveDataGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_ONLINE_PLAYER_COUNT, QueryOnlineCountGm.class);
        GmPacketManager.regGmRequestHandler(SINGLE_AREA_NUM_UP_LIMIT, SingleAreaNumUpLimitGm.class);
        GmPacketManager.regGmRequestHandler(OPEN_OR_CLOSE_WHITE_LIST, WhiteListOpenOrCloseGm.class);
        GmPacketManager.regGmRequestHandler(ADD_OR_DEL_WHITE_LIST, WhiteListAddOrDelGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_KICK_OFF_PROGRESS, QueryKickOffPlayerProgressGm.class);
        GmPacketManager.regGmRequestHandler(QUEEY_SERVER_STATUS, QueryServerStatusGm.class);
        GmPacketManager.regGmRequestHandler(GM_COMMAND, CommandGm.class);
        GmPacketManager.regGmRequestHandler(HOTUPDATE_COMMAND, HotUpdateCommGm.class);
        GmPacketManager.regGmRequestHandler(PUBLISH_OR_EDIT_GAMEBOARD, PublishOrEditGameboardGm.class);
        GmPacketManager.regGmRequestHandler(PUBLISH_OR_EDIT_LOGINBOARD, PublishOrEditLoginGameboardGm.class);
        GmPacketManager.regGmRequestHandler(DEL_GAMBOARD, DelGameboardGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_PLAYER_DETAIL, QueryPlayerDetailInfoGm.class);
        GmPacketManager.regGmRequestHandler(EMAIL_SEND, EmailSendGm.class);
        GmPacketManager.regGmRequestHandler(EMAIL_VIEW, EmailViewGm.class);
        GmPacketManager.regGmRequestHandler(EMAIL_DELE, EmailDeleGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_PLAYER_BASIC_INFO, QueryPlayerBasicInfoGm.class);
        GmPacketManager.regGmRequestHandler(FORBID_ROLE_CHAT, ForbidRoleChatGm.class);
        GmPacketManager.regGmRequestHandler(RELEASE_ROLE_CHAT, ReleaseRoleChatGm.class);
        GmPacketManager.regGmRequestHandler(QUEEY_SERVER_STATUS0, QueryServerStatusGm0.class);
        GmPacketManager.regGmRequestHandler(BLOCK_ACCOUNT, BlockAccountGm.class);
        GmPacketManager.regGmRequestHandler(RELEASE_BLOCK_ACCOUNT, ReleaseBlockAccountGm.class);
        GmPacketManager.regGmRequestHandler(SET_SERVER_OPENTIME, SetServerOpenTImeGM.class);
        GmPacketManager.regGmRequestHandler(LOOK_SERVER_OPENTIME, LookServerOpenTimeGM.class);
        GmPacketManager.regGmRequestHandler(GAME_DATARELOAD, GamedataReloadGM.class);
        GmPacketManager.regGmRequestHandler(CREATE_GIFT_PACKAGE, CreateGiftPackageGm.class);
        GmPacketManager.regGmRequestHandler(EXCHANGE_GIFT_PACKAGE, ExchangeGiftPackageGm.class);
        GmPacketManager.regGmRequestHandler(SET_LOGINTIPS, SetLoginTips.class);
        GmPacketManager.regGmRequestHandler(REDUCE_ROLE_GOLD, ReduceRoleGoldGm.class);
        GmPacketManager.regGmRequestHandler(REDUCE_ROLE_MONEY, ReduceRoleMoneyGm.class);
        GmPacketManager.regGmRequestHandler(PUBLISH_EDIT_LOOPNOTICE, PublishEditLoopNoticeGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_LOOPNOTICE, QueryLoopNoticeGm.class);
        GmPacketManager.regGmRequestHandler(DELETE_LOOPNOTICE, DeleteLoopNoticeGm.class);
        GmPacketManager.regGmRequestHandler(MODIFY_ROLE_LEVEL, ModifyRoleLevelGm.class);
        GmPacketManager.regGmRequestHandler(ADD_SPECIAL_ACCOUNT, AddSpecialAccountGm.class);
        GmPacketManager.regGmRequestHandler(DEL_SPECIAL_ACCOUNT, DelSpecialAccountGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_SPECIAL_ACCOUNT_LIST, QuerySpecialAccountListGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_SPECIAL_ACCOUNT_DETAIL, QuerySpecialAccountDetailGm.class);
        GmPacketManager.regGmRequestHandler(ALL_EMAIL_SEND, AllEmailSendGm.class);
        GmPacketManager.regGmRequestHandler(ALL_EMAIL_EDIT, AllEmailUpdateGm.class);
        GmPacketManager.regGmRequestHandler(ALL_EMAIL_VIEW, AllEmailQueryGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_PLAYER_BAG, QueryPlayerBag.class);
        GmPacketManager.regGmRequestHandler(DEL_PLAYER_BAG, DelPlayerBag.class);
        GmPacketManager.regGmRequestHandler(QUERY_PLAYER_FRIEND, QueryPlayerFriend.class);
        GmPacketManager.regGmRequestHandler(QUERY_FAMILY_INFO, QueryFamilyInfo.class);
        GmPacketManager.regGmRequestHandler(QUERY_FAMILY_MENBER_INFO, QueryFamilyMemberInfo.class);
        GmPacketManager.regGmRequestHandler(QUERY_FAMILY_RANK_INFO, QueryFamilyRankInfo.class);
        GmPacketManager.regGmRequestHandler(QUERY_PLAYER_RANK_INFO, QueryPlayerRankInfo.class);
        GmPacketManager.regGmRequestHandler(GM_MESSAGE_REDPOINT, SendGmRedpoint.class);
        GmPacketManager.regGmRequestHandler(SET_CHARGE_SWITCH, ChargeSwitchGm.class);
        GmPacketManager.regGmRequestHandler(QUERY_HOT_UPDATE_INFO, QueryHotUpdateInfoGm.class);
        GmPacketManager.regGmRequestHandler(FAMILY_WAR_TRIGGER, FamilywarTriggerGm.class);
        GmPacketManager.regGmRequestHandler(UCCAR_QUERY_USER_ROLE_BY_ACCOUNT, UCCar_QueryUserRoleGm.class);
        GmPacketManager.regGmRequestHandler(UCCAR_GIFT_SEND, UCCar_sendGift.class);
        GmPacketManager.regGmRequestHandler(UCCAR_GIFT_CHECK, UCCar_checkGift.class);
        GmPacketManager.regGmRequestHandler(ANNOUNCE_ALL_PLAYER, AnnounceGm.class);
        GmPacketManager.regGmRequestHandler(ACCOUNT_TRANSFER, AccountTransferGm.class);
        GmPacketManager.regGmRequestHandler(ACCOUNT_TRANSFER_BACK, AccountTransferBackGm.class);
    }
}
