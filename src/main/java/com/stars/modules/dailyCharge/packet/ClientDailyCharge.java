package com.stars.modules.dailyCharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.dailyCharge.DailyChargePacketSet;
import com.stars.modules.dailyCharge.prodata.DailyChargeInfo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.List;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class ClientDailyCharge extends PlayerPacket {

    public static final byte RESP_VIEW = 0x01; // 查看xx界面

    private int totalCharge;
    private byte subtype;
    private List<DailyChargeInfo> list;
    private long beginTimes;
    private long endTimes;

    public ClientDailyCharge() {
    }

    public ClientDailyCharge(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_VIEW:
                writeViewToBuff(buff);
                break;
        }
    }

    private void writeViewToBuff(NewByteBuffer buff) {
        buff.writeInt(totalCharge);
        buff.writeLong(beginTimes);
        buff.writeLong(endTimes);
        if(StringUtil.isEmpty(list)){
            buff.writeShort((short) 0);
        }else{
            buff.writeShort((short)list.size());
            for(DailyChargeInfo info : list){
                info.writeToBuff(buff);
            }
        }
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public void setList(List<DailyChargeInfo> list) {
        this.list = list;
    }

    public void setBeginTimes(long beginTimes) {
        this.beginTimes = beginTimes;
    }

    public void setEndTimes(long endTimes) {
        this.endTimes = endTimes;
    }

    @Override
    public short getType() {
        return DailyChargePacketSet.C_DAILYCHARGE;
    }

    @Override
    public void execPacket(Player player) {

    }
}
