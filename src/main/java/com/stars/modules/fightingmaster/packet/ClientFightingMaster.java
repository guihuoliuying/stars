package com.stars.modules.fightingmaster.packet;

import com.stars.modules.fightingmaster.FightingMasterPacketSet;
import com.stars.multiserver.fightingmaster.Fighter;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.fightingmaster.data.RoleFightingMaster;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/11/18.
 */
public class ClientFightingMaster extends Packet {

    public final static byte VIEW_MAIN = 1; // 主界面
    public final static byte VIEW_MATCH = 2;    // 匹配界面
    public final static byte VIEW_FIVEAWARD = 3;    // 五战奖励
    public final static byte VIEW_RANK = 4;     // 排行榜
    public final static byte RETRY_MATCH = 5;   // 再次匹配
    public final static byte BATTLE_FAILED = 6; // 战斗创建失败
    public final static byte FIGHT_TIMES = 7;//每日战斗次数

    private byte resType;

    private short fightTimes;   // 每日战斗次数
    private byte fiveReward;    // 五战奖励：0 未领取 1 已领取
    private int disScore;       // 显示积分
    private int rank;         // 排名
    private Map<Integer, Integer> award;    // 跨榜奖励

    private String fightId;
    private Fighter fighter;

    private int rankId;
    private List<RoleFightingMaster> subRank;

    private int waitTime;

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public void setSubRank(List<RoleFightingMaster> subRank) {
        this.subRank = subRank;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public void setFighter(Fighter fighter) {
        this.fighter = fighter;
    }

    public void setFightTimes(short fightTimes) {
        this.fightTimes = fightTimes;
    }

    public void setFiveReward(byte fiveReward) {
        this.fiveReward = fiveReward;
    }

    public void setDisScore(int disScore) {
        this.disScore = disScore;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setAward(Map<Integer, Integer> award) {
        this.award = award;
    }

    public void setResType(byte resType) {
        this.resType = resType;
    }

    @Override
    public short getType() {
        return FightingMasterPacketSet.C_FIGHTINGMASTER;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(resType);
        if (resType == VIEW_MAIN) {
            buff.writeShort(fightTimes);
            buff.writeByte(fiveReward);
            buff.writeInt(disScore);
            buff.writeInt(rank);
            if (award == null) {
                buff.writeShort((short) 0);
            } else {
                buff.writeShort((short) award.size());
                for (Map.Entry<Integer, Integer> entry : award.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
        }

        if (resType == VIEW_MATCH) {
            buff.writeString(fightId);
            buff.writeString(fighter.getRoleId());
            buff.writeInt(fighter.getCharactor().getLevel());
            buff.writeString(fighter.getFamilyName());
            buff.writeString(fighter.getCharactor().getName());
            buff.writeInt(fighter.getServerId());
            buff.writeInt(fighter.getCharactor().getFightScore());
            buff.writeInt(fighter.getCharactor().getModelId());
            buff.writeString(fighter.getCharactor().getServerName());
        }

        if (resType == VIEW_RANK) {
            buff.writeInt(rankId);
            if (subRank == null) {
                buff.writeInt(0);
            } else {
                buff.writeInt(subRank.size());
                for (RoleFightingMaster item : subRank) {
                    buff.writeInt(item.getRank());
                    buff.writeInt(item.getServerId());
                    buff.writeString(item.getName());
                    buff.writeInt(item.getDisScore());
                    buff.writeInt(item.getFightScore());
                    buff.writeInt(item.getLevel());
                    buff.writeString(item.getServerName());
                }
            }
        }

        if (resType == RETRY_MATCH) {
            buff.writeInt(waitTime);
        }

        if (resType == VIEW_FIVEAWARD) {
            if (award == null) {
                buff.writeShort((short) 0);
            } else {
                buff.writeShort((short) award.size());
                for (Map.Entry<Integer, Integer> entry : award.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
        }

        if (resType == FIGHT_TIMES) {
            buff.writeShort(fightTimes);
            LogUtil.info("fightTimes:{}", fightTimes);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }
}
