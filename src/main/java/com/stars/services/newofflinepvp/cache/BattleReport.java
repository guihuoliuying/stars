package com.stars.services.newofflinepvp.cache;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-03-09 15:32
 */
public class BattleReport implements Comparable<BattleReport> {
    private long roleId;
    private long timeStamp;
    private byte initiativeOrPassivity;//主动1；被动0
    private byte victoryOrDefeat;//胜利1：失败0
    private int rank;
    private String otherName;

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeLong(timeStamp);
        buff.writeByte(initiativeOrPassivity);
        buff.writeByte(victoryOrDefeat);
        buff.writeInt(rank);
        buff.writeString(otherName);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public byte getInitiativeOrPassivity() {
        return initiativeOrPassivity;
    }

    public void setInitiativeOrPassivity(byte initiativeOrPassivity) {
        this.initiativeOrPassivity = initiativeOrPassivity;
    }

    public byte getVictoryOrDefeat() {
        return victoryOrDefeat;
    }

    public void setVictoryOrDefeat(byte victoryOrDefeat) {
        this.victoryOrDefeat = victoryOrDefeat;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    @Override
    public int compareTo(BattleReport o) {
        return this.timeStamp < o.timeStamp ? -1 : 1;
    }
}
