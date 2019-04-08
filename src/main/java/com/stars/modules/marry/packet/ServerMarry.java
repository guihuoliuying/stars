package com.stars.modules.marry.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.marry.MarryManager;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.marry.MarryPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class ServerMarry extends PlayerPacket {

    public final static byte PROFRESS_LIST = 1;   // 表白请求列表
    public final static byte CLAIM = 2;         // 宣言
    public final static byte CLAIMLIST = 3;     // 宣言列表
    public final static byte PROFRESS = 4;      // 表白
    public final static byte STATE = 5;     // 自身状态
    public final static byte APPOINTMENT = 6;   // 预约
    public final static byte BREAK = 7;     // 决裂
    public final static byte APPOINT_INFO = 8;  // 预约信息
    public final static byte MARRY_INFO = 9;   // 婚姻信息()
    public final static byte WEDDING_INFO = 10; // 婚礼信息
    public final static byte ENTER_WEDDING = 11;    // 进入豪华婚礼场景
    public final static byte CANDY_ACTIVITY = 12;   // 喜糖活动
    public final static byte FIREWORKS_ACTIVITY = 13;    // 烟花活动
    public final static byte REDBAG_ACTIVITY = 14;  // 红包活动
    public final static byte BACK_CITY = 15;    // 回城
    public final static byte SHIP_INFO = 16;    // 情谊信息
    public final static byte SHIP_DUNGEON = 17; // 组队界面
    public final static byte FIGHT = 18;    // 进入战斗
    public final static byte LOGIN_BREAK_CHECK = 19;    // 登陆检查决裂
    public final static byte WEDDING_ACTIVITY_INFO = 20; // 婚礼活动
    public final static byte SEARCH = 21;   // 搜索
    public final static byte WEDDING_LIST = 22;   // 婚宴列表
    public final static byte MARRY_RING_INFO = 23;// 戒指信息
    public final static byte APPOINT_CHECK = 24;// 预约检测
    public final static byte SHOW_MARRY_BATTLE = 25; //显示情义副本详情页
    public final static byte ENTER_MARRY_BATTLE = 26; //请求进入情义副本
    public final static byte MARRY_FASHION_STATE = 27; //请求是否拥有结婚时装(是否出现购买按钮)
    public final static byte GET_MARRY_FASHION_INFO = 28; //显示购买结婚时装界面
    public final static byte BUY_MARRY_FASHION = 29; //购买结婚时装

    private byte reqType;

    // 宣言
    private String claim;
    private int reqLevel;

    // 宣言列表
    private int claimIndex;
    private int claimEndIndex;

    // 表白
    private byte profressType;
    private byte way;
    private long profressTarget;

    // 预约
    private byte appType;
    private byte gender;  // 预约增加性别字段

    // 决裂
    private byte breakType;

    // 喜糖活动
    private String position;
    private int candyStamp;

    // 红包活动
    private byte redbagType;
    private long senderId;

    // 搜索
    private String searchName;

    // 婚宴列表
    private int startIndex;
    private int endIndex;

    // 戒指详细信息
    private int pos;

    // 婚宴唯一key
    private String marryKey;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        if (reqType == CLAIM) {
            claim = buff.readString();
            reqLevel = buff.readInt();
        }
        if (reqType == CLAIMLIST) {
            claimIndex = buff.readInt();
            claimEndIndex = buff.readInt();
        }
        if (reqType == PROFRESS) {
            profressTarget = Long.valueOf(buff.readString());
            profressType = buff.readByte();
            way = buff.readByte();
        }
        if (reqType == APPOINTMENT) {
            appType = buff.readByte();
            gender = buff.readByte();
        }
        if (reqType == BREAK) {
            breakType = buff.readByte();
        }
        if (reqType == CANDY_ACTIVITY) {
            position = buff.readString();
            candyStamp = buff.readInt();
            marryKey = buff.readString();
        }
        if (reqType == REDBAG_ACTIVITY) {
            redbagType = buff.readByte();
            marryKey = buff.readString();
            if (redbagType == MarryManager.REDBAG_ACTIVITY_GET) {
                senderId = Long.valueOf(buff.readString());
            }
        }
        if (reqType == SEARCH) {
            searchName = buff.readString();
        }
        if (reqType == ENTER_WEDDING) {
            marryKey = buff.readString();
        }
        if (reqType == WEDDING_ACTIVITY_INFO) {
            marryKey = buff.readString();
        }
        if (reqType == FIREWORKS_ACTIVITY) {
            marryKey = buff.readString();
        }
        if (reqType == WEDDING_LIST) {
            startIndex = buff.readInt();
            endIndex = buff.readInt();
        }
        if (reqType == SHOW_MARRY_BATTLE) {

        }
        if (reqType == ENTER_MARRY_BATTLE) {

        }
        if (reqType == MARRY_FASHION_STATE){

        }
        if (reqType == GET_MARRY_FASHION_INFO){

        }
        if (reqType == BUY_MARRY_FASHION){

        }
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", MarryPacketSet.S_MARRY));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        if (reqType == STATE) {
            ServiceHelper.marryService().getState(getRoleId());
        }
        if (reqType == PROFRESS_LIST) {
            ServiceHelper.marryService().profressList(getRoleId());
        }
        if (reqType == CLAIMLIST) {
            ServiceHelper.marryService().claimList(getRoleId(), claimIndex, claimEndIndex);
        }
        if (reqType == LOGIN_BREAK_CHECK) {
            ServiceHelper.marryService().loginBreakCheck(getRoleId());
        }
        if (reqType == ENTER_WEDDING) {
            MarryModule marryModule = module(MConst.Marry);
            marryModule.enterWeddingScene(marryKey);
        }
        if (reqType == BACK_CITY) {
            MarryModule marryModule = module(MConst.Marry);
            marryModule.backCity();
        }
        if (reqType == CANDY_ACTIVITY) {
            ServiceHelper.marryService().openCandy(getRoleId(), position, candyStamp, marryKey);
        }
        if (reqType == FIREWORKS_ACTIVITY) {
            ServiceHelper.marryService().fireworks(getRoleId(), marryKey);
        }
        if (reqType == REDBAG_ACTIVITY) {
            if (redbagType == MarryManager.REDBAG_ACTIVITY_SEND) {
                ServiceHelper.marryService().sendRedbag(getRoleId(), marryKey);
            }
            if (redbagType == MarryManager.REDBAG_ACTIVITY_GET) {
                ServiceHelper.marryService().getRedbag(getRoleId(), senderId, marryKey);
            }
        }
        if (reqType == WEDDING_LIST) {
            ServiceHelper.marryService().weddingList(getRoleId(), startIndex, endIndex);
        }
        if (reqType == WEDDING_ACTIVITY_INFO) {
            ServiceHelper.marryService().weddingActivityInfo(getRoleId(), marryKey);
        }
        if (reqType == APPOINT_INFO) {
            ServiceHelper.marryService().appointmentInfo(getRoleId());
        }
        if (reqType == MARRY_INFO) {
            ServiceHelper.marryService().marryInfo(getRoleId());
        }
        if (reqType == WEDDING_INFO) {
            ServiceHelper.marryService().weddingInfo(getRoleId());
        }
        if (reqType == SHIP_INFO) {
            ServiceHelper.marryService().shipInfo(getRoleId());
        }
        if (reqType == SHIP_DUNGEON) {
//            ServiceHelper.marryService().sendShipDungeon(getRoleId());
        }
        if (reqType == FIGHT) {
            ServiceHelper.marryService().fight(getRoleId());
        }
        if (reqType == SEARCH) {
            ServiceHelper.marryService().search(getRoleId(), searchName);
        }
        if (reqType == MARRY_RING_INFO) {
            MarryModule mm = (MarryModule) module(MConst.Marry);
            mm.getRingList();
        }
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.MARRY)) {
            return;
        }
        if (reqType == CLAIM) {
            ServiceHelper.marryService().claim(getRoleId(), claim, reqLevel);
        }
        if (reqType == PROFRESS) {
            if (profressType == MarryManager.PROFRESS_SUCCESS || profressType == MarryManager.PROFRESS_FAILED) {
                ServiceHelper.marryService().profressResponse(getRoleId(), profressTarget, profressType);
            } else {
                ServiceHelper.marryService().profress(getRoleId(), profressTarget, way);
            }
        }
        if (reqType == APPOINTMENT) {
            if (appType == MarryManager.GENERAL_WEDDING || appType == MarryManager.LUXURIOUS_WEDDING) {
                ServiceHelper.marryService().appointment(getRoleId(), gender, appType);
                return;
            }
            ServiceHelper.marryService().appointmentResPonse(getRoleId(), appType);
        }
        if (reqType == BREAK) {
            ServiceHelper.marryService().breakMarry(getRoleId(), breakType);
        }
        if (reqType == APPOINT_CHECK) {
            ServiceHelper.marryService().appointmentCheck(getRoleId());
        }
        if (reqType == SHOW_MARRY_BATTLE) {
            MarryModule marryModule = (MarryModule) moduleMap().get(MConst.Marry);
            marryModule.showMarryBattle();
        }
        if (reqType == ENTER_MARRY_BATTLE) {
            MarryModule marryModule = (MarryModule) moduleMap().get(MConst.Marry);
//            marryModule.enterMarryBattle();
        }
        if (reqType == MARRY_FASHION_STATE){
            MarryModule marryModule = (MarryModule) moduleMap().get(MConst.Marry);
            marryModule.sendMarryFashionState();
        }
        if (reqType == GET_MARRY_FASHION_INFO){
            MarryModule marryModule = (MarryModule) moduleMap().get(MConst.Marry);
            marryModule.sendBuyMarryFashionInfo();
        }
        if (reqType == BUY_MARRY_FASHION){
            MarryModule marryModule = (MarryModule) moduleMap().get(MConst.Marry);
            marryModule.buyMarryFashion();
        }
    }

    @Override
    public short getType() {
        return MarryPacketSet.S_MARRY;
    }
}
