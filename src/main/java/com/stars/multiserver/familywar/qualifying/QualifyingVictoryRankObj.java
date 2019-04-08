package com.stars.multiserver.familywar.qualifying;

import com.stars.util.ranklist.RankObj;

/**
 * Created by chenkeyu on 2017-05-20.
 */
public class QualifyingVictoryRankObj extends RankObj {
    private int serverId;
    private String familyName;
    private int victoryCount;
    private int defeatCount;
    private long fightScore;

    public QualifyingVictoryRankObj(String key, long points) {
        super(key, points);
    }

    public QualifyingVictoryRankObj(String key, long points, int serverId, String familyName, int victoryCount, int defeatCount, long fightScore) {
        super(key, points);
        this.serverId = serverId;
        this.familyName = familyName;
        this.victoryCount = victoryCount;
        this.defeatCount = defeatCount;
        this.fightScore = fightScore;
    }

    @Override
    public int compareTo(RankObj other) {
        int ret = super.compareTo(other);
        if (ret == 0 && (other instanceof QualifyingVictoryRankObj)) {
            if (this.victoryCount > ((QualifyingVictoryRankObj) other).victoryCount) {
                ret = -1;
            } else if (this.victoryCount < ((QualifyingVictoryRankObj) other).victoryCount) {
                ret = 1;
            } else if (this.fightScore > ((QualifyingVictoryRankObj) other).fightScore) {
                ret = -1;
            } else if (this.fightScore < ((QualifyingVictoryRankObj) other).fightScore) {
                ret = 1;
            } else {
                ret = this.familyName.compareTo(((QualifyingVictoryRankObj) other).getFamilyName());
            }
        }
        return ret;
    }

    public long getFightScore() {
        return fightScore;
    }

    public int getServerId() {
        return serverId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int getVictoryCount() {
        return victoryCount;
    }

    public int getDefeatCount() {
        return defeatCount;
    }

    public void addVictory() {
        this.victoryCount++;
    }

    public void addDefeat() {
        this.defeatCount++;
    }

    @Override
    public String toString() {
        return familyName + "-" + victoryCount + "-" + defeatCount + getPoints();
    }
}
