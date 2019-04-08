package com.stars.modules.newredbag.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newredbag.NewRedbagPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.newredbag.userdata.RoleFamilyRedbag;
import com.stars.services.newredbag.userdata.RoleFamilyRedbagGet;
import com.stars.services.newredbag.userdata.RoleFamilyRedbagSend;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class ClientNewRedbag extends PlayerPacket {

    public final static byte SEND = 1;  // 发红包
    public final static byte VIEW_MAIN = 2; // 红包主界面信息
    public final static byte RECORD = 4;    // 红包记录
    public final static byte RECORD_DETAIL = 5; // 红包记录详细信息

    private byte resType;

    private Map<Integer, RoleFamilyRedbag> myRedbag;
    private List<RoleFamilyRedbag> otherRedbag;
    private int remainSelfCount;
    private List<RoleFamilyRedbagSend> recordList;
    private List<RoleFamilyRedbagGet> detailList;
    private String redbagKey;

    public void setDetailList(List<RoleFamilyRedbagGet> detailList) {
        this.detailList = detailList;
    }

    public void setRecordList(List<RoleFamilyRedbagSend> recordList) {
        this.recordList = recordList;
    }

    public void setResType(byte resType) {
        this.resType = resType;
    }

    public void setMyRedbag(Map<Integer, RoleFamilyRedbag> myRedbag) {
        this.myRedbag = myRedbag;
    }

    public void setOtherRedbag(List<RoleFamilyRedbag> otherRedbag) {
        this.otherRedbag = otherRedbag;
    }

    public void setRemainSelfCount(int remainSelfCount) {
        this.remainSelfCount = remainSelfCount;
    }

    public void setRedbagKey(String redbagKey) {
        this.redbagKey = redbagKey;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(resType);
        switch (resType) {
            case SEND:
                buff.writeInt(recordList.size());
                for (RoleFamilyRedbagSend send : recordList) {
                    writeSendToBuff(send, buff);
                }
                break;
            case VIEW_MAIN:
                buff.writeInt(myRedbag.size());
                for (RoleFamilyRedbag redbag : myRedbag.values()) {
                    buff.writeInt(redbag.getRedbagId());
                    buff.writeInt(redbag.getCount());
                }
                buff.writeInt(otherRedbag.size());
                for (RoleFamilyRedbag redbag : otherRedbag) {
                    buff.writeInt(redbag.getRedbagId());
                    buff.writeInt(redbag.getCount());
                    buff.writeString(redbag.getName());
                }
                buff.writeInt(remainSelfCount);
                break;
            case RECORD:
                buff.writeInt(recordList.size());
                for (RoleFamilyRedbagSend send : recordList) {
                    writeSendToBuff(send, buff);
                }
                break;
            case RECORD_DETAIL:
                buff.writeString(redbagKey);
                buff.writeInt(detailList.size());
                for (RoleFamilyRedbagGet get : detailList) {
                    buff.writeString(String.valueOf(get.getRoleId()));
                    buff.writeString(get.getName());
                    buff.writeInt(get.getValue());
                    buff.writeInt(get.getJobId());
                }
        }
    }

    private void writeSendToBuff(RoleFamilyRedbagSend send, NewByteBuffer buff) {
        buff.writeString(send.getUniqueKey());
        buff.writeString(String.valueOf(send.getSenderId()));
        buff.writeString(send.getRoleName());
        buff.writeInt(send.getRedbagId());
        buff.writeInt(send.getStamp());
        buff.writeInt(send.getCurIndex());
        buff.writeInt(send.getCount());
        buff.writeInt(send.getValue());
        buff.writeInt(send.getJobId());
    }

    @Override
    public short getType() {
        return NewRedbagPacketSet.C_NEW_REDBAG;
    }

    @Override
    public void execPacket(Player player) {

    }
}
