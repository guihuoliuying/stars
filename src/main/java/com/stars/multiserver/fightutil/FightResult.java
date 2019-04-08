package com.stars.multiserver.fightutil;

/**
 * Created by chenkeyu on 2017-05-04 19:58
 */
public class FightResult {
    private long winnerCampId;
    private long loserCampId;
    private FightStat stat;

    public long getWinnerCampId() {
        return winnerCampId;
    }

    public void setWinnerCampId(long winnerCampId) {
        this.winnerCampId = winnerCampId;
    }

    public long getLoserCampId() {
        return loserCampId;
    }

    public void setLoserCampId(long loserCampId) {
        this.loserCampId = loserCampId;
    }

    public FightStat getStat() {
        return stat;
    }

    public void setStat(FightStat stat) {
        this.stat = stat;
    }
}
