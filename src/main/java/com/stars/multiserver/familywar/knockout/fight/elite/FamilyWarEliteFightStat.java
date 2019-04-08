package com.stars.multiserver.familywar.knockout.fight.elite;

import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleStat;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/12.
 */
public class FamilyWarEliteFightStat{

    private long camp1FamilyId;
    private long camp2FamilyId;
    private long winnerFamilyId;
    private long loserFamilyId;
    private String camp1FamilyName;
    private String camp2FamilyName;
    private long camp1TotalPoints;
    private long camp2TotalPoints;
    private int camp1Morale; // 阵营1士气
    private int camp2Morale; // 阵营2士气
    private Map<Long, FamilyWarEliteFightPersonalStat> personalStatMap;

    /**
     * 单纯是结算时显示用的
     */
    private double camp1TowerHp;
    private double camp2TowerHp;
    private long camp1FightScore;
    private long camp2FightScore;

    public FamilyWarEliteFightStat(long camp1FamilyId, String camp1FamilyName, long camp2FamilyId, String camp2FamilyName, int camp1MoraleCache, int camp2MoraleCache) {
        this.camp1FamilyId = camp1FamilyId;
        this.camp1FamilyName = camp1FamilyName;
        this.camp2FamilyId = camp2FamilyId;
        this.camp2FamilyName = camp2FamilyName;

        this.camp1TotalPoints = 0;
        this.camp2TotalPoints = 0;
        this.camp1Morale = camp1MoraleCache;
        this.camp2Morale = camp2MoraleCache;
        this.personalStatMap = new HashMap<>();
    }

    public void addPersonalStat(long fighterId, String fighterName, byte camp) {
        personalStatMap.put(fighterId, new FamilyWarEliteFightPersonalStat(fighterId, fighterName, camp, 0, 0, 0, 0, 0));
    }

    public void updatePersonalStat(FamilyWarKnockoutBattle battle, int mainServerId, long fighterId, int killDelta,
                                   int deadDelta, int assistDelta, int comboKillCount, long pointsDelta, boolean playOrAi) {
        FamilyWarEliteFightPersonalStat personalStat = personalStatMap.get(fighterId);
        if (personalStat == null) {
            return;
        }
        personalStat.killCount += killDelta;
        personalStat.deadCount += deadDelta;
        personalStat.assistCount += assistDelta;
        if (comboKillCount > personalStat.maxComboKillCount) {
            personalStat.maxComboKillCount = comboKillCount;
        }
        personalStat.points += pointsDelta;
        ClientFamilyWarBattleStat packet = new ClientFamilyWarBattleStat();
        packet.setMyKillCount(personalStat.getKillCount());
        packet.setMyDeadCount(personalStat.getDeadCount());
        packet.setMyAssistCount(personalStat.getAssistCount());
        if (playOrAi) {
            battle.roleService().send(mainServerId, fighterId, packet);
        }
    }

    public void updateFamilyPoints(long familyId, long points) {
        if (familyId == camp1FamilyId) {
            camp1TotalPoints += points;
        } else {
            camp2TotalPoints += points;
        }
        LogUtil.info("familywar|更新我方家族:{}积分:{}", familyId, familyId == camp1FamilyId ? camp1TotalPoints : camp2TotalPoints);
    }

    public void updateMorale(byte camp, int moraleDelta) {
        switch (camp) {
            case FamilyWarConst.K_CAMP1:
                camp1Morale += moraleDelta;
                camp1Morale = camp1Morale < 0 ? 0 : camp1Morale;
                break;
            case FamilyWarConst.K_CAMP2:
                camp2Morale += moraleDelta;
                camp2Morale = camp2Morale < 0 ? 0 : camp2Morale;
                break;
        }
    }

    public long getWinnerFamilyId() {
        return winnerFamilyId;
    }

    public void setWinnerFamilyId(long winnerFamilyId) {
        this.winnerFamilyId = winnerFamilyId;
    }

    public long getLoserFamilyId() {
        return loserFamilyId;
    }

    public void setLoserFamilyId(long loserFamilyId) {
        this.loserFamilyId = loserFamilyId;
    }

    public long getCamp1FamilyId() {
        return camp1FamilyId;
    }

    public long getCamp2FamilyId() {
        return camp2FamilyId;
    }

    public String getCamp1FamilyName() {
        return camp1FamilyName;
    }

    public String getCamp2FamilyName() {
        return camp2FamilyName;
    }

    public long getCamp1TotalPoints() {
        return camp1TotalPoints;
    }

    public void setCamp1TotalPoints(long camp1TotalPoints) {
        this.camp1TotalPoints = camp1TotalPoints;
    }

    public void setCamp2TotalPoints(long camp2TotalPoints) {
        this.camp2TotalPoints = camp2TotalPoints;
    }

    public long getCamp2TotalPoints() {
        return camp2TotalPoints;
    }

    public int getCamp1Morale() {
        return camp1Morale;
    }

    public int getCamp2Morale() {
        return camp2Morale;
    }

    public Map<Long, FamilyWarEliteFightPersonalStat> getPersonalStatMap() {
        return personalStatMap;
    }

    public double getCamp1TowerHp() {
        return camp1TowerHp;
    }

    public void setCamp1TowerHp(double camp1TowerHp) {
        this.camp1TowerHp = camp1TowerHp;
    }

    public double getCamp2TowerHp() {
        return camp2TowerHp;
    }

    public void setCamp2TowerHp(double camp2TowerHp) {
        this.camp2TowerHp = camp2TowerHp;
    }

    public long getCamp1FightScore() {
        return camp1FightScore;
    }

    public void setCamp1FightScore(long camp1FightScore) {
        this.camp1FightScore = camp1FightScore;
    }

    public long getCamp2FightScore() {
        return camp2FightScore;
    }

    public void setCamp2FightScore(long camp2FightScore) {
        this.camp2FightScore = camp2FightScore;
    }
}
