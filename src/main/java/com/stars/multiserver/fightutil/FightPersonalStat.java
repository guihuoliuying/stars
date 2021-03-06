package com.stars.multiserver.fightutil;

/**
 * Created by chenkeyu on 2017-05-04 18:23
 */
public class FightPersonalStat {
    String fighterName;
    long fighterId;
    byte camp;
    int killCount; // 人头
    int deadCount; // 阵亡
    int assistCount;//助攻
    int maxComboKillCount; // 最大连斩数
    long points; // 积分

    public FightPersonalStat() {
    }

    public FightPersonalStat(long fighterId,String fighterName,  byte camp, int killCount, int deadCount, int assistCount, int maxComboKillCount, long points) {
        this.fighterName = fighterName;
        this.fighterId = fighterId;
        this.camp = camp;
        this.killCount = killCount;
        this.deadCount = deadCount;
        this.assistCount = assistCount;
        this.maxComboKillCount = maxComboKillCount;
        this.points = points;
    }

    public String getFighterName() {
        return fighterName;
    }

    public void setFighterName(String fighterName) {
        this.fighterName = fighterName;
    }

    public long getFighterId() {
        return fighterId;
    }

    public void setFighterId(long fighterId) {
        this.fighterId = fighterId;
    }

    public byte getCamp() {
        return camp;
    }

    public void setCamp(byte camp) {
        this.camp = camp;
    }

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public int getDeadCount() {
        return deadCount;
    }

    public void setDeadCount(int deadCount) {
        this.deadCount = deadCount;
    }

    public int getAssistCount() {
        return assistCount;
    }

    public void setAssistCount(int assistCount) {
        this.assistCount = assistCount;
    }

    public int getMaxComboKillCount() {
        return maxComboKillCount;
    }

    public void setMaxComboKillCount(int maxComboKillCount) {
        this.maxComboKillCount = maxComboKillCount;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }
}
