package com.stars.modules.newdailycharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newdailycharge.NewDailyChargePacketSet;
import com.stars.modules.newdailycharge.prodata.NewDailyChargeInfo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.List;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class ClientNewDailyCharge extends PlayerPacket {

    private int totalCharge;
    private List<NewDailyChargeInfo> list;
    private long beginTimes;
    private long endTimes;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(totalCharge);
        buff.writeLong(beginTimes);
        buff.writeLong(endTimes);
        com.stars.util.LogUtil.info("totalCharge:{},beginTimes:{},endTimes:{}", totalCharge, beginTimes, endTimes);
        if (StringUtil.isEmpty(list)) {
            buff.writeShort((short) 0);
        } else {
            buff.writeShort((short) list.size());
            for (NewDailyChargeInfo info : list) {
                info.writeToBuff(buff);
                LogUtil.info(" {} ", info);
            }
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewDailyChargePacketSet.C_NEW_DAILY_CHARGE;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public void setList(List<NewDailyChargeInfo> list) {
        this.list = list;
    }

    public void setBeginTimes(long beginTimes) {
        this.beginTimes = beginTimes;
    }

    public void setEndTimes(long endTimes) {
        this.endTimes = endTimes;
    }
}
