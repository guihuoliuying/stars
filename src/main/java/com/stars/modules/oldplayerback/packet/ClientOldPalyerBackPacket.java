package com.stars.modules.oldplayerback.packet;

import com.stars.modules.oldplayerback.OldPalyerBackPacketSet;
import com.stars.modules.oldplayerback.pojo.RewardPosition;
import com.stars.modules.oldplayerback.usrdata.OldPlayerRewardPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/14.
 */
public class ClientOldPalyerBackPacket extends Packet {
    private short subType;
    public static final short SEND_REWARD_SHOW = 1;//下发奖励展示
    public static final short SEND_REWARD_POSITION = 2;//领奖位置
    public static final short SEND_ACTIVITY_FINISH = 3;//活动结束
    private OldPlayerRewardPo oldPlayerRewardPo;
    private Map<Integer, RewardPosition> rewardMap;
    private int position;
    private Map<Integer, Integer> reward;
    private String activityDate;

    public ClientOldPalyerBackPacket() {
    }

    public ClientOldPalyerBackPacket(short subType) {
        this.subType = subType;
    }

    @Override
    public short getType() {
        return OldPalyerBackPacketSet.C_OLDPLAYERBACK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_REWARD_SHOW: {
                buff.writeInt(oldPlayerRewardPo.getTakeRewardMap().size());
                for (RewardPosition rewardPosition : rewardMap.values()) {
                    buff.writeInt(rewardPosition.getPosition());//天数
                    buff.writeInt(oldPlayerRewardPo.getTakeRewardMap().get(rewardPosition.getPosition()));//领取状态：1，可领取；0，不可领取
                    buff.writeInt(rewardPosition.getGroupId());//奖励dropgroupid
                    buff.writeInt(rewardPosition.getIsRare());//稀有框：1表示稀有，0表示普通
                    buff.writeString(rewardPosition.getImage());
                }
                buff.writeString(activityDate);
            }
            break;
            case SEND_REWARD_POSITION: {
                buff.writeInt(position);
                buff.writeInt(reward.size());
                for (Map.Entry<Integer, Integer> entry : reward.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public OldPlayerRewardPo getOldPlayerRewardPo() {
        return oldPlayerRewardPo;
    }

    public void setOldPlayerRewardPo(OldPlayerRewardPo oldPlayerRewardPo) {
        this.oldPlayerRewardPo = oldPlayerRewardPo;
    }

    public Map<Integer, RewardPosition> getRewardMap() {
        return rewardMap;
    }

    public void setRewardMap(Map<Integer, RewardPosition> rewardMap) {
        this.rewardMap = rewardMap;
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public void setReward(Map<Integer, Integer> reward) {
        this.reward = reward;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }

}
