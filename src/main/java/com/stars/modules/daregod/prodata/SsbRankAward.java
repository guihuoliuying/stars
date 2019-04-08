package com.stars.modules.daregod.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class SsbRankAward {
    private int id;
    private String rank;
    private int award;
    private int fightingType;

    private int minRank;
    private int maxRank;

    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeInt(fightingType);//战力段
        buffer.writeInt(minRank);//最小排名
        buffer.writeInt(maxRank);//最大排名
        buffer.writeInt(award);//奖励
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
        if (rank.equals("0")) return;
        String[] tmp = rank.split("-");
        this.minRank = Integer.parseInt(tmp[0]);
        this.maxRank = Integer.parseInt(tmp[1]);
    }

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public int getFightingType() {
        return fightingType;
    }

    public void setFightingType(int fightingType) {
        this.fightingType = fightingType;
    }

    public boolean matchRank(int rank) {
        return (rank <= maxRank && rank >= minRank) || (rank == 0 && this.rank.equals("0"));
    }
}
