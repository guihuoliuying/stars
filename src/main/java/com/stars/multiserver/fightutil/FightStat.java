package com.stars.multiserver.fightutil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-04 18:21
 */
public class FightStat {
    private long camp1Id;
    private long camp2Id;
    private long winnerCampId;
    private long loserCampId;
    private long camp1TotalPoints;
    private long camp2TotalPoints;
    private int camp1Morale; // 阵营1士气
    private int camp2Morale; // 阵营2士气
    private Map<Long, FightPersonalStat> personalStatMap;

    public FightStat(long camp1Id, long camp2Id) {
        this.camp1Id = camp1Id;
        this.camp2Id = camp2Id;
        this.personalStatMap = new HashMap<>();
    }

    public void addPersonalStat(long fighterId, String fighterName, byte camp) {
        personalStatMap.put(fighterId, new FightPersonalStat(fighterId, fighterName, camp, 0, 0, 0, 0, 0));
    }

    public FightStat updateCampPoints(long campId, long points) {
        if (campId == camp1Id) {
            camp1TotalPoints += points;
        } else {
            camp2TotalPoints += points;
        }
        return this;
    }

    public FightStat updateCampMorale(long campId, int moraleDelta) {
        if (campId == camp1Id) {
            camp1Morale += moraleDelta;
            camp1Morale = camp1Morale < 0 ? 0 : camp1Morale;
        } else {
            camp2Morale += moraleDelta;
            camp2Morale = camp2Morale < 0 ? 0 : camp2Morale;
        }
        return this;
    }

    public FightPersonalStat updatePersonalStat(int mainServerId, long fighterId, int killDelta, int deadDelta, int assistDelta, int comboKillCount, long pointsDelta) {
        FightPersonalStat personalStat = personalStatMap.get(fighterId);
        if (personalStat == null) {
            return null;
        }
        personalStat.killCount += killDelta;
        personalStat.deadCount += deadDelta;
        personalStat.assistCount += assistDelta;
        if (comboKillCount > personalStat.maxComboKillCount) {
            personalStat.maxComboKillCount = comboKillCount;
        }
        personalStat.points += pointsDelta;
        return personalStat;
    }

    public long getCamp1Id() {
        return camp1Id;
    }

    public void setCamp1Id(long camp1Id) {
        this.camp1Id = camp1Id;
    }

    public long getCamp2Id() {
        return camp2Id;
    }

    public void setCamp2Id(long camp2Id) {
        this.camp2Id = camp2Id;
    }

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

    public long getCamp1TotalPoints() {
        return camp1TotalPoints;
    }

    public void setCamp1TotalPoints(long camp1TotalPoints) {
        this.camp1TotalPoints = camp1TotalPoints;
    }

    public long getCamp2TotalPoints() {
        return camp2TotalPoints;
    }

    public void setCamp2TotalPoints(long camp2TotalPoints) {
        this.camp2TotalPoints = camp2TotalPoints;
    }

    public int getCamp1Morale() {
        return camp1Morale;
    }

    public void setCamp1Morale(int camp1Morale) {
        this.camp1Morale = camp1Morale;
    }

    public int getCamp2Morale() {
        return camp2Morale;
    }

    public void setCamp2Morale(int camp2Morale) {
        this.camp2Morale = camp2Morale;
    }

    public Map<Long, FightPersonalStat> getPersonalStatMap() {
        return personalStatMap;
    }

    public void setPersonalStatMap(Map<Long, FightPersonalStat> personalStatMap) {
        this.personalStatMap = personalStatMap;
    }
}
