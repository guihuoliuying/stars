package com.stars.modules.pk.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.marry.summary.MarrySummaryComponent;
import com.stars.modules.pk.PKPacketSet;
import com.stars.modules.pk.userdata.InvitorCache;
import com.stars.modules.pk.userdata.RolePKRecord;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/2.
 */
public class ClientPKOption extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte RECEIVE_INVITE_LIST = 1;// 收到的邀请列表
    public static final byte PVP_RECORD_LIST = 2;// 切磋记录
    public static final byte NEW_INVITE = 3;// 收到新的邀请

    /* 参数 */
    private Map<Long, InvitorCache> receiveInviteMap;// 收到的邀请列表
    private Collection<RolePKRecord> rolePKRecordList;// 切磋记录

    public ClientPKOption() {
    }

    public ClientPKOption(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return PKPacketSet.Client_PK_Option;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case RECEIVE_INVITE_LIST:
                writeReceiveInviteList(buff);
                break;
            case PVP_RECORD_LIST:
                writePkRecordList(buff);
                break;
            case NEW_INVITE:
                writeReceiveInviteList(buff);
        }
    }

    private void writeReceiveInviteList(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (receiveInviteMap == null ? 0 : receiveInviteMap.size());
        buff.writeShort(size);
        if (size == 0)
            return;
        for (InvitorCache invitorCache : receiveInviteMap.values()) {
            RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    invitorCache.getInvitorId(), "role");
            buff.writeLong(invitorCache.getInvitorId());// 邀请者roleId
            buff.writeString(invitorCache.getInvitorName());// 名称
            buff.writeInt(rsc.getRoleJob());// 职业
            buff.writeInt(rsc.getRoleLevel());// 等级
            buff.writeInt(rsc.getFightScore());// 战力
            buff.writeLong(invitorCache.getCreateTimestamp());// 邀请时间戳
            FamilySummaryComponent fsc = (FamilySummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    invitorCache.getInvitorId(), "family");
            buff.writeString(fsc.getFamilyName());// 家族名称
        }
    }

    private void writePkRecordList(NewByteBuffer buff) {
        byte size = (byte) (rolePKRecordList == null ? 0 : rolePKRecordList.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (RolePKRecord rolePKRecord : rolePKRecordList) {
            buff.writeString(String.valueOf(rolePKRecord.getEnemyId()));// 对手id
            buff.writeByte(rolePKRecord.getResult());// 胜负结果
            buff.writeLong(rolePKRecord.getCreateTimestamp());// 创建时间
            RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    rolePKRecord.getEnemyId(), "role");
            buff.writeString(rsc.getRoleName());// 名称
            buff.writeInt(rsc.getRoleJob());// 职业
            buff.writeInt(rsc.getRoleLevel());// 等级
            buff.writeInt(rsc.getFightScore());// 战力
            Summary summary = ServiceHelper.summaryService().getSummary(rolePKRecord.getEnemyId());
            MarrySummaryComponent comp = (MarrySummaryComponent) summary.getComponent(SummaryConst.C_MARRY);
            if(comp != null){
                buff.writeByte(comp.getMarryState());
            }else{
                buff.writeByte((byte)0);
            }
        }
    }

    public void setReceiveInviteMap(Map<Long, InvitorCache> receiveInviteMap) {
        this.receiveInviteMap = receiveInviteMap;
    }

    public void setRolePKRecordList(Collection<RolePKRecord> rolePKRecordList) {
        this.rolePKRecordList = rolePKRecordList;
    }

    public void setNewInvite(InvitorCache invitorCache) {
        this.receiveInviteMap = new HashMap<>();
        receiveInviteMap.put(invitorCache.getInvitorId(), invitorCache);
    }
}
