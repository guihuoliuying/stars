package com.stars.multiserver.familywar.knockout.fight.normal;

/**
 * Created by zhaowenshuo on 2016/12/22.
 */
public class FamilyWarNormalFightPersonalStat {

    String fighterName;
    long fighterId;
    byte camp;
    int killCount; // 人头
    int deadCount; // 阵亡
    int assistCount;//助攻
    int maxComboKillCount; // 最大连斩数
    long points; // 积分
    int hp;

    public FamilyWarNormalFightPersonalStat(long fighterId, String fighterName, byte camp, long points, int hp) {
        this.fighterId = fighterId;
        this.fighterName = fighterName;
        this.camp = camp;
        this.points = points;
        this.hp = hp;
    }

    public long getFighterId() {
        return fighterId;
    }

    public void setFighterId(long fighterId) {
        this.fighterId = fighterId;
    }

    public String getFighterName() {
        return fighterName;
    }

    public void setFighterName(String fighterName) {
        this.fighterName = fighterName;
    }

    public byte getCamp() {
        return camp;
    }

    public void setCamp(byte camp) {
        this.camp = camp;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
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
}
