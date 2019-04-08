package com.stars.modules.newserversign.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newserversign.NewServerSignPacketSet;
import com.stars.modules.newserversign.prodata.NewServerSignVo;
import com.stars.modules.newserversign.userdata.ActSignRewardRecord;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.vowriter.BuffUtil;

import java.util.Map;
import java.util.Set;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientNewServerSign extends PlayerPacket {
    public static final byte Flag_Get_Reward_Info = 0;
    public static final byte Flag_Update_Reward_Status = 1;
    public static final byte Flag_ACTIVITY_STATUS = 2;

    private byte flag;

    //flag = 0
    private Map<Integer, NewServerSignVo> rewardsVoMap;
    private Map<Integer, Byte> rewardsStatusMap;
    private Map<Integer, Integer> costMap;
    private Map<Integer, Integer> displayAward;
    private long startTimeStamp;
    private long endTimeStamp;
    private int openDay;

    //flag = 1
    private int newServerSignId;
    private byte status;
    private boolean isOpen;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewServerSignPacketSet.C_NEW_SERVER_SIGN;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(flag);
        switch (flag) {
            case Flag_Get_Reward_Info:
                writeRewardInfo(buff);
                break;
            case Flag_Update_Reward_Status:
                writeRewardStatus(buff);
                break;
            case Flag_ACTIVITY_STATUS: {
                buff.writeInt(isOpen ? 1 : 0);
            }
            break;
            default:
                break;
        }
    }

    private void writeDisplayAward(NewByteBuffer buff) {
        BuffUtil.writeIntMapToBuff(buff, displayAward);
    }

    private void writeRewardInfo(NewByteBuffer buff) {
        buff.writeInt(openDay);
        short size = (short) (rewardsVoMap == null ? 0 : rewardsVoMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (NewServerSignVo vo : rewardsVoMap.values()) {
                vo.writeToBuff(buff);
                int newServerSignId = vo.getNewServerSignId();
                byte rewardStatus = ActSignRewardRecord.Reward_Status_Cannot_get;
                if (rewardsStatusMap != null && rewardsStatusMap.containsKey(newServerSignId)) {
                    rewardStatus = rewardsStatusMap.get(newServerSignId);
                }
                buff.writeByte(rewardStatus);
            }
        }

        //补签消耗
        short size2 = (short) (costMap == null ? 0 : costMap.size());
        buff.writeShort(size2);
        if (size2 != 0) {
            Set<Map.Entry<Integer, Integer>> entrySet = costMap.entrySet();
            for (Map.Entry<Integer, Integer> entry : entrySet) {
                int itemId = entry.getKey();
                int count = entry.getValue();
                buff.writeInt(itemId);
                buff.writeInt(count);
            }
        }

        //活动开始时间
        buff.writeString(Long.toString(startTimeStamp));
        //活动结束时间
        buff.writeString(Long.toString(endTimeStamp));
    }

    private void writeRewardStatus(NewByteBuffer buff) {
        buff.writeInt(openDay);
        buff.writeInt(newServerSignId);
        buff.writeByte(status);
        BuffUtil.writeIntMapToBuff(buff, displayAward);
    }

    public void setFlag(byte value) {
        this.flag = value;
    }

    public void setRewardsVoMap(Map<Integer, NewServerSignVo> value) {
        this.rewardsVoMap = value;
    }

    public void setRewardsStatusMap(Map<Integer, Byte> value) {
        this.rewardsStatusMap = value;
    }

    public void setCostMap(Map<Integer, Integer> value) {
        this.costMap = value;
    }

    public void setStartTimeStamp(long value) {
        this.startTimeStamp = value;
    }

    public void setEndTimeStamp(long value) {
        this.endTimeStamp = value;
    }

    public void setNewServerSignId(int value) {
        this.newServerSignId = value;
    }

    public void setStatus(byte value) {
        this.status = value;
    }

    public void setDisplayAward(Map<Integer, Integer> displayAward) {
        this.displayAward = displayAward;
    }

    public int getOpenDay() {
        return openDay;
    }

    public void setOpenDay(int openDay) {
        this.openDay = openDay;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}