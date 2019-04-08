package com.stars.modules.marry.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.marry.MarryManager;
import com.stars.modules.marry.MarryPacketSet;
import com.stars.modules.marry.prodata.MarryRing;
import com.stars.modules.marry.prodata.MarryRingLvl;
import com.stars.modules.marry.userdata.RoleRing;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.marry.userdata.Marry;
import com.stars.services.marry.userdata.MarryRole;
import com.stars.services.marry.userdata.WeddingResponse;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class ClientMarry extends PlayerPacket {

    public static final byte CLAIM_LIST = 1; // 宣言列表
    public static final byte PROFRESS = 2;  // 表白
    public static final byte PROFRESS_LIST = 3; // 表白列表
    public static final byte APPOINTMENT = 4;   // 预约
    public static final byte STATE = 6;     // 自身状态
    public static final byte WEDDING_STATE = 7;   // 豪华婚礼进行状态: 准备 开始 结束
    public static final byte BREAK_MARRY = 8;   // 决裂
    public static final byte APPOINTMENT_INFO = 9;  // 预约信息
    public static final byte MARRY_INFO = 10;   // 婚姻信息
    public static final byte WEDDING_INFO = 11; // 婚礼信息
    public static final byte ENTER_WEDDING = 12;    // 进入豪华婚礼场景
    public static final byte CANDY_ACTIVITY = 13; // 喜糖活动
    public static final byte FIREWORKS_ACTIVITY = 14;   // 烟花活动
    public static final byte REDBAG_ACTIVITY = 15;  // 红包活动
    public static final byte SHIP_INFO = 16;    // 情谊信息
    public static final byte CLAIM = 17;    // 宣言
    public static final byte WEDDING_ACTIVITY_INFO = 18;    // 婚礼活动信息
    public static final byte WEDDING_ICON = 19;    // 登陆下发婚宴图标
    public static final byte WEDDING_LIST = 20;    // 正在举行婚宴列表
    public static final byte MARRY_RING_INFO = 21;    //戒指信息
    public static final byte APPOINT_CHECK = 22;    //预约检测
    public static final byte SEARCH_CLAIM_LIST = 23; //搜索列表
    public static final byte SHOW_MARRY_BATTLE_DETAIL = 24; //打开情义副本界面
    public static final byte SHOW_BUY_MARRY_FASHION_ICON = 25; //是否显示购买结婚时装Icon
    public static final byte SHOW_BUY_MARRY_FASION_INFO = 26; //显示购买界面
    public static final byte MARRY_FASHION_INFO = 27; //返回结婚时装id

    // 宣言列表
    private byte resType;
    private MarryRole self;
    private List<MarryRole> claimList;

    // 表白
    private byte profressType;
    private long maker;
    private long profressId;
    private String profressName = "";
    private int profressJob;
    private int profressLevel;

    // 表白列表
    private List<MarryRole> profressList;

    // 预约
    private long appointmentId;
    private String appointmentName;
    private byte appointmentType;        // 0,1,2,3,4
    private int appointStamp;
    private byte gender;

    // 自身状态
    private byte state;
    private byte isWedding;
    private Set<Long> targetList;
    private String otherName;
    private byte isSpecailAccount; // 0不是 1是

    // 婚礼状态
    private int beginRemain;
    private byte weddingState;
    private MarryRole orderRole;
    private MarryRole otherRole;

    // 决裂
    private String breakName;
    private byte breakType;
    private long breakId;
    private byte marryState;

    // 预约信息
    private int timeStamp;
    private byte dayOfWeek;
    private Map<Integer, String> appointmentInfo;

    // 婚姻信息
    private MarryRole marryRole;
    private int friendShip;

    // 婚礼信息
    private int beginStamp;
    private String weddingName;

    // 进入豪华婚礼场景
    private String position;
    private int sceneId;

    // 喜糖活动
    private byte candyType;
    private int candyStamp;
    private String candyPos;
    private Map<Integer, Integer> candyTool;
    private long candyHolder;

    // 烟花活动
    private Long fireworksCustomer;
    private String custonerName;
    private Map<Integer, Integer> fireworksTool;

    // 红包活动
    private byte redbagType;
    private long redbagSender;
    private String redbagSenderName;
    private Map<Integer, Integer> redbagTool;

    // 情谊信息
    private MarryRole other;
    private Marry marry;
    private int fashionId;
    private int weaponId;
    private int weddingBeginStamp;

    // 宣言
    private byte claimResult;
    private String claim;

    // 婚礼活动信息
    private String roleId1;
    private String roleId2;
    private int currentStamp;
    private int lastRedbagStamp;
    private int lastCandyStamp;
    private int lastFireworksStamp;
    private int redbagTimes;
    private int fireworksTimes;
    private int remainTime;

    // 婚宴图标
    private byte icon;    // 0、隐藏 1、显示

    // 婚宴唯一key
    private String marryKey;

    // 正在举行婚宴列表
    private int total;
    private List<WeddingResponse> weddingList;

    //戒指信息
    List<RoleRing> ringList;

    //预约检测
    private byte checkResult;   // 0、玩家在线 1、玩家不在线

    //情义副本详情页
    int remainMarryBattleTime; //剩余情义副本次数
    Map<Integer,Integer> awardMap; //奖励

    //显示购买结婚时装按钮
    private byte isShowBuyIcon;
    //购买结婚时装界面
    private int fashionItemId;
    private int itemCount;
    private int reqItemId;
    private int reqCost;


    public void setAppointStamp(int appointStamp) {
        this.appointStamp = appointStamp;
    }

    public void setWeddingBeginStamp(int weddingBeginStamp) {
        this.weddingBeginStamp = weddingBeginStamp;
    }

    public void setWeddingState(byte weddingState) {
        this.weddingState = weddingState;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public void setOrderRole(MarryRole orderRole) {
        this.orderRole = orderRole;
    }

    public void setOtherRole(MarryRole otherRole) {
        this.otherRole = otherRole;
    }

    public void setRedbagTimes(int redbagTimes) {
        this.redbagTimes = redbagTimes;
    }

    public void setFireworksTimes(int fireworksTimes) {
        this.fireworksTimes = fireworksTimes;
    }

    public void setRemainTime(int remainTime) {
        this.remainTime = remainTime;
    }

    public void setCurrentStamp(int currentStamp) {
        this.currentStamp = currentStamp;
    }

    public void setLastRedbagStamp(int lastRedbagStamp) {
        this.lastRedbagStamp = lastRedbagStamp;
    }

    public void setLastCandyStamp(int lastCandyStamp) {
        this.lastCandyStamp = lastCandyStamp;
    }

    public void setLastFireworksStamp(int lastFireworksStamp) {
        this.lastFireworksStamp = lastFireworksStamp;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public void setClaimResult(byte claimResult) {
        this.claimResult = claimResult;
    }

    public void setIsWedding(byte isWedding) {
        this.isWedding = isWedding;
    }

    public void setMarryState(byte marryState) {
        this.marryState = marryState;
    }

    public void setFashionId(int fashionId) {
        this.fashionId = fashionId;
    }

    public void setWeaponId(int weaponId) {
        this.weaponId = weaponId;
    }

    public void setOther(MarryRole other) {
        this.other = other;
    }

    public void setMarry(Marry marry) {
        this.marry = marry;
    }

    public void setFireworksCustomer(Long fireworksCustomer) {
        this.fireworksCustomer = fireworksCustomer;
    }

    public void setCustonerName(String custonerName) {
        this.custonerName = custonerName;
    }

    public void setFireworksTool(Map<Integer, Integer> fireworksTool) {
        this.fireworksTool = fireworksTool;
    }

    public void setRedbagTool(Map<Integer, Integer> redbagTool) {
        this.redbagTool = redbagTool;
    }

    public void setRedbagSenderName(String redbagSenderName) {
        this.redbagSenderName = redbagSenderName;
    }

    public void setRedbagType(byte redbagType) {
        this.redbagType = redbagType;
    }

    public void setRedbagSender(long redbagSender) {
        this.redbagSender = redbagSender;
    }

    public void setCandyHolder(long candyHolder) {
        this.candyHolder = candyHolder;
    }

    public void setCandyTool(Map<Integer, Integer> candyTool) {
        this.candyTool = candyTool;
    }

    public void setCandyPos(String candyPos) {
        this.candyPos = candyPos;
    }

    public void setCandyType(byte candyType) {
        this.candyType = candyType;
    }

    public void setCandyStamp(int candyStamp) {
        this.candyStamp = candyStamp;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setWeddingName(String weddingName) {
        this.weddingName = weddingName;
    }

    public void setBeginStamp(int beginStamp) {
        this.beginStamp = beginStamp;
    }

    public void setMarryRole(MarryRole marryRole) {
        this.marryRole = marryRole;
    }

    public void setAppointmentInfo(Map<Integer, String> appointmentInfo) {
        this.appointmentInfo = appointmentInfo;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setDayOfWeek(byte dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setBreakName(String breakName) {
        this.breakName = breakName;
    }

    public void setBreakId(long breakId) {
        this.breakId = breakId;
    }

    public void setBreakType(byte breakType) {
        this.breakType = breakType;
    }

    public void setBeginRemain(int beginRemain) {
        this.beginRemain = beginRemain;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public void setTargetList(Set<Long> targetList) {
        this.targetList = targetList;
    }

    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setAppointmentName(String appointmentName) {
        this.appointmentName = appointmentName;
    }

    public void setAppointmentType(byte appointmentType) {
        this.appointmentType = appointmentType;
    }

    public void setProfressList(List<MarryRole> profressList) {
        this.profressList = profressList;
    }

    public void setMaker(long maker) {
        this.maker = maker;
    }

    public void setProfressId(long profressId) {
        this.profressId = profressId;
    }

    public void setProfressName(String profressName) {
        this.profressName = profressName;
    }

    public void setProfressJob(int profressJob) {
        this.profressJob = profressJob;
    }

    public void setProfressLevel(int profressLevel) {
        this.profressLevel = profressLevel;
    }

    public void setProfressType(byte profressType) {
        this.profressType = profressType;
    }

    public void setSelf(MarryRole self) {
        this.self = self;
    }

    public void setClaimList(List<MarryRole> claimList) {
        this.claimList = claimList;
    }

    public void setResType(byte resType) {
        this.resType = resType;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public String getMarryKey() {return this.marryKey;}

    public void setMarryKey(String marryKey) {this.marryKey = marryKey;}

    public int getFriendShip() {return this.friendShip;}

    public void setFriendShip(int friendShip) {this.friendShip = friendShip;}

    public void setIcon(byte icon) {this.icon = icon;}

    public void setTotal(int total) {this.total = total;}

    public void setWeddingList(List<WeddingResponse> weddingList) {this.weddingList = weddingList;}

    public void setRingList(List<RoleRing> ringList) {this.ringList = ringList;}

    public void setCheckResult(byte checkResult) {this.checkResult = checkResult;}

    public void setRoleId1(String roleId1) {this.roleId1 = roleId1;}

    public void setRoleId2(String roleId2) {this.roleId2 = roleId2;}

    public void setIsSpecailAccount(byte isSpecailAccount) {this.isSpecailAccount = isSpecailAccount;}

    public int getRemainMarryBattleTime() {
        return remainMarryBattleTime;
    }

    public void setRemainMarryBattleTime(int remainMarryBattleTime) {
        this.remainMarryBattleTime = remainMarryBattleTime;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public void setAwardMap(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    public byte getIsShowBuyIcon() {
        return isShowBuyIcon;
    }

    public void setIsShowBuyIcon(byte isShowBuyIcon) {
        this.isShowBuyIcon = isShowBuyIcon;
    }

    public int getFashionItemId() {
        return fashionItemId;
    }

    public void setFashionItemId(int fashionItemId) {
        this.fashionItemId = fashionItemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getReqItemId() {
        return reqItemId;
    }

    public void setReqItemId(int reqItemId) {
        this.reqItemId = reqItemId;
    }

    public int getReqCost() {
        return reqCost;
    }

    public void setReqCost(int reqCost) {
        this.reqCost = reqCost;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(resType);
        if (resType == CLAIM_LIST) {
            buff.writeInt(claimList.size());
            for (MarryRole claim : claimList) {
                buff.writeString(String.valueOf(claim.getRoleId()));
                buff.writeString(claim.getName());
                buff.writeString(claim.getClaim());
                buff.writeInt(claim.getFight());
                buff.writeInt(claim.getLevel());
                buff.writeInt(claim.getReqLevel());
                buff.writeInt(claim.getJobId());
                buff.writeInt(claim.getPopularity());
            }
        }
        if (resType == PROFRESS) {
            buff.writeString(String.valueOf(maker));
            buff.writeString(String.valueOf(profressId));
            buff.writeByte(profressType);
            // 只在同意表白的时候下面的值才是有用的
            buff.writeString(profressName);
            buff.writeInt(profressJob);
            buff.writeInt(profressLevel);
        }
        if (resType == PROFRESS_LIST) {
            buff.writeString(self.getClaim() == null ? "" : self.getClaim());
            buff.writeInt(self.getReqLevel());
            buff.writeInt(self.getPopularity());
            buff.writeInt(profressList.size());
            for (MarryRole marryRole : profressList) {
                buff.writeString(String.valueOf(marryRole.getRoleId()));
                buff.writeString(marryRole.getName());
                buff.writeInt(marryRole.getJobId());
                buff.writeInt(marryRole.getLevel());
                buff.writeInt(marryRole.getFight());
                buff.writeString(marryRole.getClaim());
            }
        }
        if (resType == STATE) {
            buff.writeByte(state);
            buff.writeByte(isWedding);
            buff.writeInt(targetList.size());
            for (long id : targetList) {
                buff.writeString(String.valueOf(id));
            }
            buff.writeString(otherName);
            buff.writeString(marryKey);
            buff.writeByte(gender);
            buff.writeByte(isSpecailAccount);
        }
        if (resType == APPOINTMENT) {
            buff.writeString(String.valueOf(appointmentId));
            buff.writeString(appointmentName);
            buff.writeByte(appointmentType);
            buff.writeInt(DateUtil.getSecondTime());
            buff.writeInt(appointStamp);
            buff.writeByte(this.gender);
            buff.writeString(this.marryKey);
        }
        if (resType == WEDDING_STATE) {
            buff.writeString(orderRole.getName());
            buff.writeString(otherRole.getName());
            buff.writeByte(weddingState);
            buff.writeString(String.valueOf(otherRole.getRoleId()));
            buff.writeString(String.valueOf(orderRole.getRoleId()));
            buff.writeInt(beginRemain);
            buff.writeInt(otherRole.getJobId());
            buff.writeInt(otherRole.getLevel());
            buff.writeInt(orderRole.getJobId());
            buff.writeInt(orderRole.getLevel());
        }
        if (resType == BREAK_MARRY) {
            buff.writeString(String.valueOf(breakId));
            buff.writeString(breakName);
            buff.writeByte(breakType);
            buff.writeByte(marryState);
        }
        if (resType == APPOINTMENT_INFO) {
            buff.writeInt(timeStamp);
            buff.writeByte(dayOfWeek);
            buff.writeInt(appointmentInfo.size());
            for (Map.Entry<Integer, String> entry : appointmentInfo.entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeString(entry.getValue());
            }
        }
        if (resType == MARRY_INFO) {
            buff.writeString(marryRole.getName());
            buff.writeString(String.valueOf(marryRole.getRoleId()));
            buff.writeInt(marryRole.getJobId());
            buff.writeInt(marryRole.getLevel());
            buff.writeInt(friendShip);
        }
        if (resType == WEDDING_INFO) {
            buff.writeInt(beginStamp);
            buff.writeString(weddingName);
        }
        if (resType == ENTER_WEDDING) {
            buff.writeString(position);
            buff.writeInt(sceneId);
            buff.writeString(marryKey);
        }
        if (resType == CANDY_ACTIVITY) {
            buff.writeByte(candyType);
            if (candyType == MarryManager.CANDY_ACTIVITY_BEGIN) {
                buff.writeInt(candyStamp);
            }
            if (candyType == MarryManager.CANDY_ACTIVITY_CLICK) {
                buff.writeString(candyPos);
                buff.writeString(String.valueOf(candyHolder));
                buff.writeInt(candyTool.size());
                for (Map.Entry<Integer, Integer> entry : candyTool.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
        }
        if (resType == FIREWORKS_ACTIVITY) {
            buff.writeString(String.valueOf(fireworksCustomer));
            buff.writeString(String.valueOf(custonerName));
            buff.writeInt(fireworksTool.size());
            for (Map.Entry<Integer, Integer> entry : fireworksTool.entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
            }
        }

        if (resType == REDBAG_ACTIVITY) {
            buff.writeByte(redbagType);
            if (redbagType == MarryManager.REDBAG_ACTIVITY_SEND) {
                buff.writeString(String.valueOf(redbagSender));
                buff.writeString(redbagSenderName);
                buff.writeInt(redbagTool.size());
                for (Map.Entry<Integer, Integer> entry : redbagTool.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
            if (redbagType == MarryManager.REDBAG_ACTIVITY_GET) {
                buff.writeInt(redbagTool.size());
                for (Map.Entry<Integer, Integer> entry : redbagTool.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
        }
        if (resType == SHIP_INFO) {
            buff.writeInt(marry.getMarryStamp());
            buff.writeString(String.valueOf(other.getRoleId()));
            buff.writeString(other.getName());
            buff.writeInt(other.getJobId());
            buff.writeInt(other.getLevel());
            buff.writeInt(other.getFight());
            buff.writeInt(fashionId);
            buff.writeInt(weaponId);
            buff.writeInt(marry.getBreakCount());
            buff.writeInt(marry.getLastBreakStamp());
            buff.writeInt(DateUtil.getSecondTime());
            buff.writeInt(weddingBeginStamp);
            buff.writeInt(marry.getLastSuccessAppointTyte());
        }
        if (resType == CLAIM) {
            buff.writeByte(claimResult);
            buff.writeString(claim);
        }

        if (resType == WEDDING_ACTIVITY_INFO) {
            buff.writeString(roleId1);
            buff.writeString(roleId2);
            buff.writeInt(currentStamp);
            buff.writeInt(lastCandyStamp);
            buff.writeInt(lastRedbagStamp);
            buff.writeInt(lastFireworksStamp);
            buff.writeInt(remainTime);
            buff.writeInt(redbagTimes);
            buff.writeInt(fireworksTimes);
        }
        if (resType == WEDDING_ICON) {
            buff.writeByte(icon);
        }
        if (resType == WEDDING_LIST) {
            buff.writeInt(total);
            buff.writeInt(weddingList.size());
            for (WeddingResponse we : weddingList) {
                buff.writeString(we.roleName1);
                buff.writeInt(we.jobId1);
                buff.writeInt(we.level1);
                buff.writeString(we.roleName2);
                buff.writeInt(we.jobId2);
                buff.writeInt(we.level2);
                buff.writeString(we.marryKey);
                buff.writeInt(we.remainTime);
                buff.writeByte(we.icon);
            }
        }
        if (resType == MARRY_RING_INFO) {
            buff.writeInt(ringList.size());
            for (RoleRing ring:ringList) {
                MarryRing marryRing = MarryManager.getMarryRingVo(ring.getRingId());
                MarryRingLvl marryringlvl = MarryManager.getMarryRingLvVo(ring.getRingId(),ring.getLevel());
                if (null == marryRing || null == marryringlvl) continue;
                buff.writeInt(ring.getPos());
                buff.writeInt(ring.getRingId());
                buff.writeShort(ring.getLevel());
                buff.writeString(marryRing.getName());
                buff.writeString(marryRing.getIcon());
                buff.writeString(marryRing.getDesc());
                buff.writeByte(marryRing.getQuality());
                buff.writeString(marryringlvl.getAttr());
                buff.writeInt(marryringlvl.getFightPower());
            }
        }
        if (resType == APPOINT_CHECK) {
            buff.writeByte(checkResult);
        }
        if(resType == SEARCH_CLAIM_LIST) {
            buff.writeInt(claimList.size());
            for (MarryRole claim : claimList) {
                buff.writeString(String.valueOf(claim.getRoleId()));
                buff.writeString(claim.getName());
                buff.writeString(claim.getClaim());
                buff.writeInt(claim.getFight());
                buff.writeInt(claim.getLevel());
                buff.writeInt(claim.getReqLevel());
                buff.writeInt(claim.getJobId());
                buff.writeInt(claim.getPopularity());
            }
        }
        if(resType == SHOW_MARRY_BATTLE_DETAIL){
            buff.writeInt(remainMarryBattleTime); //剩余情义副本次数
            if(StringUtil.isEmpty(awardMap)){ //下发奖励道具
               buff.writeInt(0);
            }else{
                buff.writeInt(awardMap.size());
                Iterator iter = awardMap.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<Integer,Integer> entry = (Map.Entry<Integer,Integer>) iter.next();
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
        }
        if(resType == SHOW_BUY_MARRY_FASHION_ICON){
            buff.writeByte(isShowBuyIcon);
        }
        if(resType == SHOW_BUY_MARRY_FASION_INFO){
            buff.writeInt(fashionItemId);
            buff.writeInt(itemCount);
            buff.writeInt(reqItemId);
            buff.writeInt(reqCost);
        }
        if(resType == MARRY_FASHION_INFO){
            buff.writeInt(fashionId);
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MarryPacketSet.C_MARRY;
    }
}
