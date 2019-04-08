package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.modules.marry.summary.MarrySummaryComponent;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.friend.userdata.BlackerPo;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/13.
 */
public class ClientBlacker extends PlayerPacket {

    public static final byte SUBTYPE_BLACKER_LIST = 0x00;
    public static final byte SUBTYPE_BLACKER_NOTIFY_ADD = 0x01; // 新增通知
    public static final byte SUBTYPE_BLACKER_NOTIFY_DEL = 0x02; // 删除通知
    public static final byte SUBTYPE_BLACKER_NOTIFY_ONLINE = 0x03; // 占坑，还没使用
    public static final byte SUBTYPE_BLACKER_NOTIFY_OFFLINE = 0x04; // 占坑，还没使用

    public static final byte SUBTYPE_BLACKER_SIMPLE_LIST = 0x10; // 黑名单简略列表


    private byte subtype;
    private long blackerId;
    private BlackerPo blackerPo;
    private Map<Long, BlackerPo> blackerPoMap;
    private List<Long> blackerIdList;

    public ClientBlacker() {
    }

    public ClientBlacker(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FriendPacketSet.C_BLACKER;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_BLACKER_LIST:
                writeBlackList(buff);
                break;
            case SUBTYPE_BLACKER_NOTIFY_ADD:
                writeBlackerPo(buff);
                break;
            case SUBTYPE_BLACKER_NOTIFY_DEL:
                writeBlackerId(buff);
                break;
            case SUBTYPE_BLACKER_SIMPLE_LIST:
                writeBlackerIdList(buff);
                break;
        }
    }

    private void writeBlackList(com.stars.network.server.buffer.NewByteBuffer buff) {
        if (blackerPoMap == null) {
            buff.writeByte((byte) 0);
            return;
        }
        List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(new ArrayList<Long>(blackerPoMap.keySet()));
        Map<Long, Summary> summaryMap = new HashMap<>();
        for (Summary summary : summaryList) {
            summaryMap.put(summary.getRoleId(), summary);
        }
        buff.writeByte((byte) blackerPoMap.size());
        for (BlackerPo blackerPo : blackerPoMap.values()) {
            Summary summary = summaryMap.get(blackerPo.getBlackerId()); // fixme: 处理为空的情况
            RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
            buff.writeString(Long.toString(blackerPo.getBlackerId()));
            buff.writeInt(component.getRoleJob());
            buff.writeString(component.getRoleName());
            buff.writeInt(component.getRoleLevel());
            buff.writeInt(component.getFightScore());
            buff.writeInt(summary.getOfflineTimestamp());
            MarrySummaryComponent comp = (MarrySummaryComponent) summary.getComponent(SummaryConst.C_MARRY);
            if(comp != null){
                buff.writeByte(comp.getMarryState());
            }else{
                buff.writeByte((byte)0);
            }
        }
    }

    private void writeBlackerPo(com.stars.network.server.buffer.NewByteBuffer buff) {
        Summary summary = ServiceHelper.summaryService().getSummary(blackerPo.getBlackerId()); // fixme: 处理为空的情况
        RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        buff.writeString(Long.toString(blackerPo.getBlackerId()));
        buff.writeInt(component.getRoleJob());
        buff.writeString(component.getRoleName());
        buff.writeInt(component.getRoleLevel());
        buff.writeInt(component.getFightScore());
        buff.writeInt(summary.getOfflineTimestamp());
        MarrySummaryComponent comp = (MarrySummaryComponent) summary.getComponent(SummaryConst.C_MARRY);
        if(comp != null){
            buff.writeByte(comp.getMarryState());
        }else{
            buff.writeByte((byte)0);
        }
    }

    private void writeBlackerIdList(com.stars.network.server.buffer.NewByteBuffer buff) {
        if (blackerIdList == null) {
            buff.writeByte((byte) 0);
            return;
        }
        buff.writeByte((byte) blackerIdList.size());
        for (Long blackerId : blackerIdList) {
            buff.writeString(Long.toString(blackerId));
        }
    }

    private void writeBlackerId(NewByteBuffer buff) {
        buff.writeString(Long.toString(blackerId));
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public Map<Long, BlackerPo> getBlackerPoMap() {
        return blackerPoMap;
    }

    public void setBlackerPoMap(Map<Long, BlackerPo> blackerPoMap) {
        this.blackerPoMap = blackerPoMap;
    }

    public long getBlackerId() {
        return blackerId;
    }

    public void setBlackerId(long blackerId) {
        this.blackerId = blackerId;
    }

    public BlackerPo getBlackerPo() {
        return blackerPo;
    }

    public void setBlackerPo(BlackerPo blackerPo) {
        this.blackerPo = blackerPo;
    }

    public List<Long> getBlackerIdList() {
        return blackerIdList;
    }

    public void setBlackerIdList(List<Long> blackerIdList) {
        this.blackerIdList = blackerIdList;
    }
}
