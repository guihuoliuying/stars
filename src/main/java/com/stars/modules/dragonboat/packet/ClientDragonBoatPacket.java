package com.stars.modules.dragonboat.packet;

import com.stars.modules.dragonboat.DragonBoatConst;
import com.stars.modules.dragonboat.DragonBoatManager;
import com.stars.modules.dragonboat.DragonBoatPacketSet;
import com.stars.modules.dragonboat.define.StepTime;
import com.stars.modules.dragonboat.prodata.DragonBoatVo;
import com.stars.modules.dragonboat.userdata.RoleBetOnDragonBoatPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.DragonBoatRankPo;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class ClientDragonBoatPacket extends Packet {
    private byte subType;
    /**
     * 下发活动数据
     */
    public static final byte SEND_ACTIVITY_DATA = 0;
    public static final byte SEND_RANK = 1;
    /**
     * 下发押注信息
     */
    public static final byte SEND_BET_ON_TIPS = 2;
    public static final byte UPDATE_ONE_RANK = 3;
    public static final byte SEND_REWARD_PREVIEW = 4;
    private String ruleDesc;
    private String timeDesc;
    private Map<Long, List<AbstractRankPo>> rankPoMap;
    private List<String> stageTimes;

    private StepTime stepTime;
    private Integer maxSelectTimes = DragonBoatManager.maxSelectTimes;
    private Integer mySelectTimes = 0;
    private Integer betOnedDragonBoatId = 0;
    private Integer betOnedDragonBoatSpeed = 0;
    private int betOnedDragonBoatRank = 0;
    private String betOnedDragonBoatName = "";
    private Map<Long, RoleBetOnDragonBoatPo> historyRecord;
    private int isNotify = 0;

    public ClientDragonBoatPacket(byte subType) {
        this.subType = subType;
    }

    public ClientDragonBoatPacket() {
    }

    @Override
    public short getType() {
        return DragonBoatPacketSet.C_DragonBoat;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case SEND_ACTIVITY_DATA: {
                writeActivityData(buff);
            }
            break;
            case SEND_RANK: {
                writeAllRankData(buff);
            }
            break;
            case SEND_BET_ON_TIPS: {
                buff.writeInt(betOnedDragonBoatId);
            }
            break;
            case UPDATE_ONE_RANK: {
                writeRefreshRank(buff);
            }
            break;
            case SEND_REWARD_PREVIEW: {
                writeRewardPreview(buff);
            }
            break;
        }
    }

    /**
     * 写入奖励预览
     *
     * @param buff
     */
    private void writeRewardPreview(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(DragonBoatManager.rankRewardMap.size());
        for (Map.Entry<Integer, String> entry : DragonBoatManager.rankRewardMap.entrySet()) {
            buff.writeInt(entry.getKey());//排名
            buff.writeString(entry.getValue());//奖励
        }
    }


    /**
     * 下发历史排行榜数据
     *
     * @param buff
     */
    private void writeAllRankData(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(rankPoMap.size());
        for (Map.Entry<Long, List<AbstractRankPo>> entry : rankPoMap.entrySet()) {
            buff.writeString(entry.getKey() + "");
            int betOnedDragonBoatId = 0;
            RoleBetOnDragonBoatPo betOnDragonBoat = historyRecord.get(entry.getKey());
            if (betOnDragonBoat != null) {
                betOnedDragonBoatId = betOnDragonBoat.getDragonBoatId();
            }
            buff.writeInt(betOnedDragonBoatId);//本轮我的押注龙舟id
            List<AbstractRankPo> rankPoList = entry.getValue();
            buff.writeInt(rankPoList.size());
            for (int rank = 1; rank <= rankPoList.size(); rank++) {
                DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) rankPoList.get(rank - 1);
                buff.writeInt(dragonBoatRankPo.getDragonBoatId());
                buff.writeString(dragonBoatRankPo.getDragonBoat().getName());
                buff.writeInt(dragonBoatRankPo.getSpeed());
                buff.writeString(DragonBoatManager.rankRewardMap.get(rank));
            }
        }


    }

    /**
     * 下发活动数据
     *
     * @param buff
     */
    private void writeActivityData(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(stepTime.getStatus());
        buff.writeInt(stepTime.getStep());
        buff.writeString(stepTime.getTimestamp() + "");
        buff.writeInt(maxSelectTimes);
        buff.writeInt(mySelectTimes);
        buff.writeInt(isNotify);
        /**
         * 下一步的阶段
         */
        switch (stepTime.getStep()) {
            case DragonBoatConst.BET_ON: {
                writeDragonBoatList(buff);
            }
            break;
            case DragonBoatConst.RACING: {
                buff.writeInt(betOnedDragonBoatId);
                buff.writeString(betOnedDragonBoatName);
                writeDragonBoatList(buff);
            }
            break;
            case DragonBoatConst.WAITING: {

                buff.writeInt(betOnedDragonBoatId);
                buff.writeInt(betOnedDragonBoatSpeed);
                buff.writeString(betOnedDragonBoatName);
                writeOneRank(buff);
            }
            break;
            case DragonBoatConst.SHOW: {
                buff.writeString(stepTime.getLastTime() + "");
                buff.writeInt(betOnedDragonBoatId);
                buff.writeInt(betOnedDragonBoatSpeed);
                buff.writeString(betOnedDragonBoatName);
                writeOneRank(buff);

            }
            break;
            case DragonBoatConst.FINISH: {

                if (betOnedDragonBoatId != 0) {
                    buff.writeInt(1);
                    buff.writeInt(betOnedDragonBoatId);
                    buff.writeInt(betOnedDragonBoatRank);
                } else {
                    buff.writeInt(0);
                }
                writeOneRank(buff);
            }
            break;
        }

    }

    /**
     * 写入龙舟列表
     *
     * @param buff
     */
    private void writeDragonBoatList(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(DragonBoatManager.dragonBoats.size());
        for (DragonBoatVo dragonBoat : DragonBoatManager.dragonBoats) {
            buff.writeInt(dragonBoat.getDragonBoatId());
            buff.writeString(dragonBoat.getName());
            buff.writeString(dragonBoat.getStayEffect());
            buff.writeString(dragonBoat.getSpeedEffect());
            buff.writeString(dragonBoat.getFinishEffect());
            buff.writeString(dragonBoat.getImg());
            buff.writeInt(dragonBoat.getOrder());
        }
    }

    @Override
    public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public void writeOneRank(com.stars.network.server.buffer.NewByteBuffer buff) {
        Long activityKey = ServiceHelper.opDragonBoatService().getActivityKey();
        List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_DRAGON_BOAT, 10, activityKey);
        buff.writeInt(rankPoList.size());
        for (int rank = 1; rank <= rankPoList.size(); rank++) {
            DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) rankPoList.get(rank - 1);
            DragonBoatVo dragonBoat = dragonBoatRankPo.getDragonBoat();
            buff.writeInt(dragonBoat.getDragonBoatId());
            buff.writeString(dragonBoat.getName());
            buff.writeString(dragonBoat.getStayEffect());
            buff.writeString(dragonBoat.getSpeedEffect());
            buff.writeString(dragonBoat.getFinishEffect());
            buff.writeString(dragonBoat.getImg());
            buff.writeInt(dragonBoat.getOrder());
            buff.writeInt(dragonBoatRankPo.getSpeed());
            buff.writeInt(dragonBoatRankPo.getUpNum());
            buff.writeInt(dragonBoatRankPo.getDownNum());
        }
    }

    /**
     * 刷新列表数据
     *
     * @param buff
     */
    public void writeRefreshRank(NewByteBuffer buff) {
        Long activityKey = ServiceHelper.opDragonBoatService().getActivityKey();
        List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_DRAGON_BOAT, 10, activityKey);
        buff.writeInt(rankPoList.size());
        for (int rank = 1; rank <= rankPoList.size(); rank++) {
            DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) rankPoList.get(rank - 1);
            DragonBoatVo dragonBoat = dragonBoatRankPo.getDragonBoat();
            buff.writeInt(dragonBoat.getDragonBoatId());
            buff.writeInt(dragonBoatRankPo.getSpeed());
            buff.writeInt(dragonBoatRankPo.getUpNum());
            buff.writeInt(dragonBoatRankPo.getDownNum());
        }
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public String getTimeDesc() {
        return timeDesc;
    }

    public void setTimeDesc(String timeDesc) {
        this.timeDesc = timeDesc;
    }


    public List<String> getStageTimes() {
        return stageTimes;
    }

    public void setStageTimes(List<String> stageTimes) {
        this.stageTimes = stageTimes;
    }


    public StepTime getStepTime() {
        return stepTime;
    }

    public void setStepTime(StepTime stepTime) {
        this.stepTime = stepTime;
    }

    public Integer getMaxSelectTimes() {
        return maxSelectTimes;
    }

    public void setMaxSelectTimes(Integer maxSelectTimes) {
        this.maxSelectTimes = maxSelectTimes;
    }

    public Integer getMySelectTimes() {
        return mySelectTimes;
    }

    public void setMySelectTimes(Integer mySelectTimes) {
        this.mySelectTimes = mySelectTimes;
    }

    public Integer getBetOnedDragonBoatId() {
        return betOnedDragonBoatId;
    }

    public void setBetOnedDragonBoatId(Integer betOnedDragonBoatId) {
        this.betOnedDragonBoatId = betOnedDragonBoatId;
    }

    public Integer getBetOnedDragonBoatSpeed() {
        return betOnedDragonBoatSpeed;
    }

    public void setBetOnedDragonBoatSpeed(Integer betOnedDragonBoatSpeed) {
        this.betOnedDragonBoatSpeed = betOnedDragonBoatSpeed;
    }

    public int getBetOnedDragonBoatRank() {
        return betOnedDragonBoatRank;
    }

    public void setBetOnedDragonBoatRank(int betOnedDragonBoatRank) {
        this.betOnedDragonBoatRank = betOnedDragonBoatRank;
    }

    public String getBetOnedDragonBoatName() {
        return betOnedDragonBoatName;
    }

    public void setBetOnedDragonBoatName(String betOnedDragonBoatName) {
        this.betOnedDragonBoatName = betOnedDragonBoatName;
    }

    public void setRankPoMap(Map<Long, List<AbstractRankPo>> rankMap) {
        this.rankPoMap = rankMap;
    }

    public Map<Long, RoleBetOnDragonBoatPo> getHistoryRecord() {
        return historyRecord;
    }

    public void setHistoryRecord(Map<Long, RoleBetOnDragonBoatPo> historyRecord) {
        this.historyRecord = historyRecord;
    }

    public void setIsNotify(int isNotify) {
        this.isNotify = isNotify;
    }

    public int getIsNotify() {
        return isNotify;
    }
}
