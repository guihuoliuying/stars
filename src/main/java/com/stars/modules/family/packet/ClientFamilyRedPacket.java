package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyManager;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.welfare.redpacket.FamilyRedPacketData;
import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketMemberPo;
import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketRecordPo;
import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketSeizedRecordPo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/7.
 */
public class ClientFamilyRedPacket extends PlayerPacket {

    public static final byte SUBTYPE_ALL_INFO = 0x00; //
    public static final byte SUBTYPE_SELF_INFO = 0x01; // 自身
    public static final byte SUBTYPE_NOTIFY = 0x0F; // 通知有红包可抢

    public static final byte SUBTYPE_GIVE = 0x10; // 派发红包
    public static final byte SUBTYPE_SEIZE = 0x11; // 抢夺红包

    private byte subtype;
    private FamilyRedPacketData data;
    private FamilyRedPacketRecordPo recordPo;
    private FamilyRedPacketMemberPo memberRedPacketPo;
    private FamilyRedPacketSeizedRecordPo seizedRecordPo;

    public ClientFamilyRedPacket() {
    }

    public ClientFamilyRedPacket(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_RED_PACKET;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_ALL_INFO:
                writeAllInfo(buff);
                break;
            case SUBTYPE_SELF_INFO:
                writeSelfInfo(buff);
                break;
            case SUBTYPE_NOTIFY:
                writeNofity(buff);
                break;
            case SUBTYPE_SEIZE:
                writeSeizedInfo(buff);
                break;
        }
    }

    private void writeAllInfo(com.stars.network.server.buffer.NewByteBuffer buff) {
        // 自身信息
        buff.writeInt(memberRedPacketPo.getOwnedCount()); // 拥有红包的个数
        buff.writeInt(memberRedPacketPo.getGivenCount()); // 派发红包的个数
        buff.writeInt(memberRedPacketPo.getSeizedCount()); // 抢夺红包的个数
        // 红包记录
        int now = now();
        buff.writeByte((byte) data.getRecordPoList().size()); // 红包记录数
        for (FamilyRedPacketRecordPo recordPo : data.getRecordPoList()) {
            buff.writeString(Long.toString(recordPo.getRedPacketId())); // 红包id
            buff.writeString(recordPo.getGiverName()); // 派发者id
            buff.writeInt(recordPo.getCount()); // 红包个数
            buff.writeInt(recordPo.getSeizedCount()); // 已抢个数
            buff.writeInt(recordPo.getTimestamp()); // 派发时间戳
            int restTime = FamilyManager.rpTimeout - (now - recordPo.getTimestamp());
            buff.writeInt(restTime < 0 ? 0 : restTime); // 剩余时间

            // 抢红包记录
            buff.writeShort((short) recordPo.getSeizedRecordPoList().size());
            for (FamilyRedPacketSeizedRecordPo seizedRecordPo : recordPo.getSeizedRecordPoList()) {
                writeSeizedInfo(buff, seizedRecordPo);
            }
        }
    }

    private void writeSeizedInfo(com.stars.network.server.buffer.NewByteBuffer buff, FamilyRedPacketSeizedRecordPo seizedRecordPo) {
        buff.writeString(seizedRecordPo.getSeizerName()); // 抢夺者的名字
        if (seizedRecordPo.getSeizedToolMap() == null) {
            buff.writeByte((byte) 0);
        } else {
            buff.writeByte((byte) seizedRecordPo.getSeizedToolMap().size());
            for (Map.Entry<Integer, Integer> toolEntry : seizedRecordPo.getSeizedToolMap().entrySet()) {
                buff.writeInt(toolEntry.getKey()); // 道具id
                buff.writeInt(toolEntry.getValue()); // 道具数量
            }
        }
    }

    private void writeSelfInfo(com.stars.network.server.buffer.NewByteBuffer buff) {
        // 自身信息
        buff.writeInt(memberRedPacketPo.getOwnedCount()); // 拥有红包的个数
        buff.writeInt(memberRedPacketPo.getGivenCount()); // 派发红包的个数
        buff.writeInt(memberRedPacketPo.getSeizedCount()); // 抢夺红包的个数
    }

    private void writeNofity(com.stars.network.server.buffer.NewByteBuffer buff) {
        int restTime = 1000 - (now() - recordPo.getTimestamp());
        buff.writeString(Long.toString(recordPo.getRedPacketId())); // 红包id
        buff.writeString(Long.toString(recordPo.getGiverId())); // 派发者id
        buff.writeString(recordPo.getGiverName()); // 派发者名称
        buff.writeInt(recordPo.getCount()); // 红包个数
        buff.writeInt(recordPo.getSeizedCount()); // 已抢个数
        buff.writeInt(recordPo.getTimestamp()); // 派发时间戳
        buff.writeInt(restTime < 0 ? 0 : restTime); // 剩余时间
    }

    private void writeSeizedInfo(NewByteBuffer buff) {
        if (seizedRecordPo == null || seizedRecordPo.getSeizedToolMap() == null) {
            buff.writeByte((byte) 0);
        } else {
            Map<Integer, Integer> toolMap = seizedRecordPo.getSeizedToolMap();
            buff.writeByte((byte) toolMap.size());
            for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
            }
        }
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public byte getSubtype() {
        return subtype;
    }

    public void setSubtype(byte subtype) {
        this.subtype = subtype;
    }

    public FamilyRedPacketData getData() {
        return data;
    }

    public void setData(FamilyRedPacketData data) {
        this.data = data;
    }

    public FamilyRedPacketRecordPo getRecordPo() {
        return recordPo;
    }

    public void setRecordPo(FamilyRedPacketRecordPo recordPo) {
        this.recordPo = recordPo;
    }

    public FamilyRedPacketMemberPo getMemberRedPacketPo() {
        return memberRedPacketPo;
    }

    public void setMemberRedPacketPo(FamilyRedPacketMemberPo memberRedPacketPo) {
        this.memberRedPacketPo = memberRedPacketPo;
    }

    public FamilyRedPacketSeizedRecordPo getSeizedRecordPo() {
        return seizedRecordPo;
    }

    public void setSeizedRecordPo(FamilyRedPacketSeizedRecordPo seizedRecordPo) {
        this.seizedRecordPo = seizedRecordPo;
    }
}
