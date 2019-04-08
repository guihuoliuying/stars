package com.stars.modules.newserverfightscore.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newserverfightscore.NewServerFightPacketSet;
import com.stars.modules.newserverfightscore.prodata.NewServerFightScoreVo;
import com.stars.modules.newserverfightscore.userdata.ActRoleNsFightScore;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/7.
 */
public class ClientNSFightScore extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SEND_ALL_DATA = 1;// 下发数据(产品数据+用户数据)
    public static final byte UPDATE_REWARD_RECORD = 2;// 更新领奖记录
    public static final byte HISTRY_RANK = 3;// 历史排行榜

    /* 参数 */
    private int curActId;// 当前活动Id
    private Map<Integer, NewServerFightScoreVo> voMap;// 产品数据
    private ActRoleNsFightScore roleNsFightScore;// 用户数据
    private List<AbstractRankPo> historyRank;// 历史排行榜
    private Map<Integer, Byte> nsfsRewardStatus;// 奖励领取状态
    private long endTime;

    public ClientNSFightScore() {
    }

    public ClientNSFightScore(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewServerFightPacketSet.C_NCFIGHTSCORE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SEND_ALL_DATA:
                writeAllData(buff);
                break;
            case UPDATE_REWARD_RECORD:
                buff.writeInt(curActId);
                buff.writeString(roleNsFightScore.getRewardRecord());
                break;
            case HISTRY_RANK:
                /**
                 * 取消历史排行榜
                 */
//                writeHistoryRank(buff);
                break;
        }
    }

    private void writeAllData(NewByteBuffer buff) {
        buff.writeInt(curActId);
        buff.writeLong(endTime);
        byte size = (byte) (voMap == null ? 0 : voMap.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (NewServerFightScoreVo vo : voMap.values()) {
            vo.writeToBuff(buff);
            buff.writeByte(nsfsRewardStatus.get(vo.getRewardId()));
        }
    }

    private void writeHistoryRank(NewByteBuffer buff) {
        short size = (short) (historyRank == null ? 0 : historyRank.size());
        buff.writeShort(size);
        if (size == 0)
            return;
        for (AbstractRankPo roleRankPo : historyRank) {
            roleRankPo.writeToBuffer(RankConstant.RANKID_FIGHTSCORE, buff);
        }
    }

    public void setCurActId(int curActId) {
        this.curActId = curActId;
    }

    public void setVoMap(Map<Integer, NewServerFightScoreVo> voMap) {
        this.voMap = voMap;
    }

    public void setRoleNsFightScore(ActRoleNsFightScore roleNsFightScore) {
        this.roleNsFightScore = roleNsFightScore;
    }

    public void setHistoryRank(List<AbstractRankPo> historyRank) {
        this.historyRank = historyRank;
    }

    public void setNsfsRewardStatus(Map<Integer, Byte> nsfsRewardStatus) {
        this.nsfsRewardStatus = nsfsRewardStatus;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
