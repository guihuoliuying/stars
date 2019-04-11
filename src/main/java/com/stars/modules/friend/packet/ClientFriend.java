package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.friend.summary.FriendFlowerSummaryComponent;
import com.stars.services.friend.userdata.*;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/13.
 */
public class ClientFriend extends PlayerPacket {

    public static final byte SUBTYPE_FRIEND_LIST = 0x00; // 好友列表
    public static final byte SUBTYPE_FRIEND_NOTIFY_ONLINE = 0x01; // 好友上线
    public static final byte SUBTYPE_FRIEND_NOTIFY_OFFLINE = 0x02; // 好友下线
    public static final byte SUBTYPE_FRIEND_NOTIFY_ADD = 0x03; // 好友新增
    public static final byte SUBTYPE_FRIEND_NOTIFY_DEL = 0x04; // 好友删除
    public static final byte SUBTYPE_VIEW_FLOWER_RECORD = 0x05; // 收/送 花记录界面
    public static final byte SUBTYPE_SYN_SEND_FLOWER_RECORD = 0x06; // 同步送花记录
    public static final byte SUBTYPE_SYN_RECEIVE_FLOWER_RECORD = 0x07; // 同步收花记录
    public static final byte SUBTYPE_SEND_FLOWER_SUCCESS_UI = 0x08; // 送花成功界面
    public static final byte SUBTYPE_SEND_FLOWER_UI = 0x09;     // 送花选择界面

    public static final byte SUBTYPE_APPLICATION_LIST = 0x10; // 申请列表

    public static final byte SUBTYPE_APPLICATION_NOTIFY_ADD = 0x11; // 申请新增
    public static final byte SUBTYPE_APPLICATION_NOTIFY_DEL = 0x12; // 申请删除
    public static final byte SUBTYPE_UPDATE_FRIEND_LIST = 0x13; // 刷新好友列表

    private byte subtype;
    private long friendId;
    private long applicantId;
    private FriendPo friendPo;
    private FriendVigorPo friendVigorPo;
    private Map<Long, FriendPo> friendPoMap;
    private FriendApplicationPo applicationPo;
    private Map<Long, FriendApplicationPo> applicationPoMap;
    private List<ItemVo> itemList;
    private int sendFlower;
    private int receiveFlower;
    private List<SendFlowerRecordPo> sendFlowerList;        //送花记录
    private List<ReceiveFlowerRecordPo> receiveFlowerList;  //收花记录
    private SendFlowerRecordPo sendFlowerRecord;
    private ReceiveFlowerRecordPo receiveFlowerRecord;

    private int flowerCount;
    private int addCount;
    private int fighting;
    private String name;
    private String familyName;
    private int job;
    private int level;
    private byte dailyFirstSendFlower;
    private int addFlowerCount;


    public ClientFriend() {
    }

    public ClientFriend(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FriendPacketSet.C_FRIEND;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_FRIEND_LIST:
                writeFriendPoMap(buff);
                break;
            case SUBTYPE_FRIEND_NOTIFY_ONLINE:case SUBTYPE_FRIEND_NOTIFY_OFFLINE:
                writeFriendId(buff);
                break;
            case SUBTYPE_FRIEND_NOTIFY_ADD:
                writeFriendPo(buff);
                break;
            case SUBTYPE_FRIEND_NOTIFY_DEL:
                writeFriendId(buff);
                break;
            case SUBTYPE_APPLICATION_LIST:
                writeApplicationPoMap(buff);
                break;
            case SUBTYPE_APPLICATION_NOTIFY_ADD:
                writeApplicationPo(buff);
                break;
            case SUBTYPE_APPLICATION_NOTIFY_DEL:
                writeApplicantId(buff);
                break;
            case SUBTYPE_VIEW_FLOWER_RECORD:
                writeFlowerRecords(buff);
                break;
            case SUBTYPE_SYN_SEND_FLOWER_RECORD:
                buff.writeInt(sendFlower);
                buff.writeInt(receiveFlower);
                sendFlowerRecord.writeToBuff(buff);
                break;
            case SUBTYPE_SYN_RECEIVE_FLOWER_RECORD:
                buff.writeInt(sendFlower);
                buff.writeInt(receiveFlower);
                receiveFlowerRecord.writeToBuff(buff);
                break;
            case SUBTYPE_SEND_FLOWER_SUCCESS_UI:
                writeSendFlowerSuccessUI(buff);
                break;
            case SUBTYPE_SEND_FLOWER_UI:
                buff.writeByte(dailyFirstSendFlower);
                break;
            case SUBTYPE_UPDATE_FRIEND_LIST:
                writeFriendPoMap(buff);
                break;
        }
    }

    private void writeSendFlowerSuccessUI(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(name);
        buff.writeString(familyName);
        buff.writeInt(fighting);
        buff.writeInt(flowerCount);
        buff.writeInt(addCount);
        buff.writeInt(job);
        buff.writeInt(level);
    }

    private void writeFlowerRecords(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(sendFlower);
        buff.writeInt(receiveFlower);

        if(StringUtil.isEmpty(sendFlowerList)){
            buff.writeByte((byte)0);
        }else{
            buff.writeByte((byte)sendFlowerList.size());
            for(SendFlowerRecordPo record:sendFlowerList){
                record.writeToBuff(buff);
            }
        }

        if(StringUtil.isEmpty(receiveFlowerList)){
            buff.writeByte((byte)0);
        }else{
            buff.writeByte((byte)receiveFlowerList.size());
            for(ReceiveFlowerRecordPo record:receiveFlowerList){
                record.writeToBuff(buff);
            }
        }
    }

    //赠送体力状态: 0待赠送 1可接收 2已赠送
    private byte getDailyVigorType(FriendPo friendPo,FriendVigorPo friendVigorPo){
        if(friendPo!=null && friendPo.getDailyGetVigorType() == 1) {
            return 1;//可接收
        }
        if(friendVigorPo != null && friendVigorPo.getDailySendVigorList() != null &&
                friendVigorPo.getDailySendVigorList().contains(friendPo.getFriendId())) {
            return 2;//2已赠送
        }
        return 0;//0待赠送
    }

    //是否已赠送体力: 0未赠送 1已赠送
    private byte getHasSendVigor(FriendPo friendPo,FriendVigorPo friendVigorPo){
        if(friendVigorPo != null && friendVigorPo.getDailySendVigorList() != null &&
                friendVigorPo.getDailySendVigorList().contains(friendPo.getFriendId())) {
            return 1;//1已赠送
        }
        return 0;//0未赠送
    }

    private void writeFriendPo(com.stars.network.server.buffer.NewByteBuffer buff) {
        Summary summary = ServiceHelper.summaryService().getSummary(friendPo.getFriendId());
        RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        FriendFlowerSummaryComponent flowerComponet = (FriendFlowerSummaryComponent) summary.getComponent(SummaryConst.C_FRIEND_FLOWER);
        buff.writeString(Long.toString(friendPo.getFriendId()));
        buff.writeInt(friendPo.getIntimacy());      //亲密度
        buff.writeInt(flowerComponet==null?0:flowerComponet.getReceiveFlowerCount());//收花数
        buff.writeByte(getDailyVigorType(friendPo, friendVigorPo));//赠送体力状态: 0待赠送 1可接收 2已赠送  新增好友默认为待赠送状态
        buff.writeByte(getHasSendVigor(friendPo, friendVigorPo));//是否已赠送体力: 0未赠送 1已赠送
        buff.writeInt(component.getRoleJob());
        buff.writeString(component.getRoleName());
        buff.writeInt(component.getRoleLevel());
        buff.writeInt(component.getFightScore());
        buff.writeInt(summary.getOfflineTimestamp()); // 当前时间和离线时间的差值
    }

    /* 粘包 */
    private void writeFriendPoMap(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(friendVigorPo.getDailySendVigorTimes());
        buff.writeInt(friendVigorPo.getDailyReceiveVigorTimes());
        if (friendPoMap == null) {
            buff.writeByte((byte) 0);
            return;
        }
        List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(new ArrayList<Long>(friendPoMap.keySet()));
        Map<Long, Summary> summaryMap = new HashMap<>();
        for (Summary summary : summaryList) {
            summaryMap.put(summary.getRoleId(), summary);
        }
        buff.writeByte((byte) friendPoMap.size());
        for (FriendPo friendPo : friendPoMap.values()) {
            Summary summary = summaryMap.get(friendPo.getFriendId()); // fixme: 处理为空的情况
            RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
            FriendFlowerSummaryComponent flowerComponet = (FriendFlowerSummaryComponent) summary.getComponent(SummaryConst.C_FRIEND_FLOWER);
            buff.writeString(Long.toString(friendPo.getFriendId()));
            buff.writeInt(friendPo.getIntimacy());      //亲密度
            int receiveCount = flowerComponet==null?0:flowerComponet.getReceiveFlowerCount();
            buff.writeInt(receiveCount + addFlowerCount);//收花数
            buff.writeByte(getDailyVigorType(friendPo, friendVigorPo));//赠送体力状态: 0待赠送 1可接收 2已赠送
            buff.writeByte(getHasSendVigor(friendPo, friendVigorPo));//是否已赠送体力: 0未赠送 1已赠送
            buff.writeInt(component.getRoleJob());
            buff.writeString(friendPo.getFriendName());
            buff.writeInt(component.getRoleLevel());
            buff.writeInt(component.getFightScore());
            buff.writeInt(summary.getOfflineTimestamp()); // 当前时间和离线时间的差值
        }
    }

    private void writeFriendId(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(friendId));
    }

    private void writeApplicationPoMap(com.stars.network.server.buffer.NewByteBuffer buff) {
        if (applicationPoMap == null) {
            buff.writeByte((byte) 0);
            return;
        }
        buff.writeByte((byte) applicationPoMap.size());
        for (FriendApplicationPo applicationPo : applicationPoMap.values()) {
            buff.writeString(Long.toString(applicationPo.getApplicantId()));
            buff.writeInt(applicationPo.getApplicantJobId());
            buff.writeString(applicationPo.getApplicantName());
            buff.writeInt(applicationPo.getApplicantLevel());
            // fixme: 临时方案
            RoleSummaryComponent component = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    applicationPo.getApplicantId(), MConst.Role);
            buff.writeInt(component == null ? 0 : component.getFightScore()); // 战力
            buff.writeInt(applicationPo.getAppliedTimestamp()); // 申请时间戳
        }
    }

    private void writeApplicationPo(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(applicationPo.getApplicantId()));
        buff.writeInt(applicationPo.getApplicantJobId());
        buff.writeString(applicationPo.getApplicantName());
        buff.writeInt(applicationPo.getApplicantLevel());
        RoleSummaryComponent component = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                applicationPo.getApplicantId(), MConst.Role);
        buff.writeInt(component == null ? 0 : component.getFightScore()); // 战力
        buff.writeInt(applicationPo.getAppliedTimestamp()); // 申请时间戳

    }

    private void writeApplicantId(NewByteBuffer buff) {
        buff.writeString(Long.toString(applicantId));
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /* Getter/Setter */

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(long applicantId) {
        this.applicantId = applicantId;
    }

    public Map<Long, FriendPo> getFriendPoMap() {
        return friendPoMap;
    }

    public void setFriendPoMap(Map<Long, FriendPo> friendPoMap) {
        this.friendPoMap = friendPoMap;
    }

    public Map<Long, FriendApplicationPo> getApplicationPoMap() {
        return applicationPoMap;
    }

    public FriendPo getFriendPo() {
        return friendPo;
    }

    public void setFriendPo(FriendPo friendPo) {
        this.friendPo = friendPo;
    }

    public void setApplicationPoMap(Map<Long, FriendApplicationPo> applicationPoMap) {
        this.applicationPoMap = applicationPoMap;
    }

    public FriendApplicationPo getApplicationPo() {
        return applicationPo;
    }

    public void setFriendVigorPo(FriendVigorPo friendVigorPo) {
        this.friendVigorPo = friendVigorPo;
    }

    public void setApplicationPo(FriendApplicationPo applicationPo) {
        this.applicationPo = applicationPo;
    }

    public List<ItemVo> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemVo> itemList) {
        this.itemList = itemList;
    }

    public void setSendFlower(int sendFlower) {
        this.sendFlower = sendFlower;
    }

    public void setReceiveFlower(int receiveFlower) {
        this.receiveFlower = receiveFlower;
    }

    public void setSendFlowerList(List<SendFlowerRecordPo> sendFlowerList) {
        this.sendFlowerList = sendFlowerList;
    }

    public void setReceiveFlowerList(List<ReceiveFlowerRecordPo> receiveFlowerList) {
        this.receiveFlowerList = receiveFlowerList;
    }

    public void setSendFlowerRecord(SendFlowerRecordPo sendFlowerRecord) {
        this.sendFlowerRecord = sendFlowerRecord;
    }

    public void setReceiveFlowerRecord(ReceiveFlowerRecordPo receiveFlowerRecord) {
        this.receiveFlowerRecord = receiveFlowerRecord;
    }

    public int getAddCount() {
        return addCount;
    }

    public void setAddCount(int addCount) {
        this.addCount = addCount;
    }

    public int getFighting() {
        return fighting;
    }

    public void setFighting(int fighting) {
        this.fighting = fighting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public int getFlowerCount() {
        return flowerCount;
    }

    public void setFlowerCount(int flowerCount) {
        this.flowerCount = flowerCount;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setDailyFirstSendFlower(byte dailyFirstSendFlower) {
        this.dailyFirstSendFlower = dailyFirstSendFlower;
    }

    public void setAddFlowerCount(int addFlowerCount) {
        this.addFlowerCount = addFlowerCount;
    }
}
