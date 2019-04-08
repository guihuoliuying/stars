package com.stars.multiserver.familywar.knockout.fight.normal;

import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleStat;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/22.
 */
public class FamilyWarNormalFightStat {

    private long winnerFamilyId;
    private long loserFamilyId;
    private long camp1FamilyId;
    private long camp2FamilyId;
    private String camp1FamilyName;
    private String camp2FamilyName;
    private int camp1ServerId;
    private int camp2ServerId;

    private Map<Long, FamilyWarNormalFightPersonalStat> personalStatMap = new HashMap<>();
    private Map<Long, Map<Integer, Integer>> personalToolMap = new HashMap<>();

    /**
     * 单纯是结算时显示用的
     */
    private double camp1TowerHp;
    private double camp2TowerHp;
    private long camp1FightScore;
    private long camp2FightScore;

    public void addPersonalStat(long fighterId, String fighterName, byte camp, int hp) {
        personalStatMap.put(fighterId, new FamilyWarNormalFightPersonalStat(fighterId, fighterName, camp, 0, hp));
    }

    public void updatePersonalStat(long fighterId, long pointsDelta) {
        FamilyWarNormalFightPersonalStat personalStat = personalStatMap.get(fighterId);
        if (personalStat == null) {
            return;
        }
        personalStat.points += pointsDelta;
    }

    public void updatePersonalStat(FamilyWarKnockoutBattle battle, int mainServerId, long fighterId, int killDelta,
                                   int deadDelta, int assistDelta, int comboKillCount, long pointsDelta, boolean playOrAi) {
        FamilyWarNormalFightPersonalStat personalStat = personalStatMap.get(fighterId);
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

    public void addPersonalToolMap(long fighterId, Map<Integer, Integer> toolMap) {
        personalToolMap.put(fighterId, toolMap);
    }

    public Map<Long, FamilyWarNormalFightPersonalStat> getPersonalStatMap() {
        return personalStatMap;
    }

    public long getWinnerFamilyId() {
        return winnerFamilyId;
    }

    public long getLoserFamilyId() {
        return loserFamilyId;
    }

    public void setWinnerFamilyId(long winnerFamilyId) {
        this.winnerFamilyId = winnerFamilyId;
    }

    public void setLoserFamilyId(long loserFamilyId) {
        this.loserFamilyId = loserFamilyId;
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

    public long getCamp1FamilyId() {
        return camp1FamilyId;
    }

    public void setCamp1FamilyId(long camp1FamilyId) {
        this.camp1FamilyId = camp1FamilyId;
    }

    public long getCamp2FamilyId() {
        return camp2FamilyId;
    }

    public void setCamp2FamilyId(long camp2FamilyId) {
        this.camp2FamilyId = camp2FamilyId;
    }

    public String getCamp1FamilyName() {
        return camp1FamilyName;
    }

    public void setCamp1FamilyName(String camp1FamilyName) {
        this.camp1FamilyName = camp1FamilyName;
    }

    public String getCamp2FamilyName() {
        return camp2FamilyName;
    }

    public void setCamp2FamilyName(String camp2FamilyName) {
        this.camp2FamilyName = camp2FamilyName;
    }

    public int getCamp1ServerId() {
        return camp1ServerId;
    }

    public void setCamp1ServerId(int camp1ServerId) {
        this.camp1ServerId = camp1ServerId;
    }

    public int getCamp2ServerId() {
        return camp2ServerId;
    }

    public void setCamp2ServerId(int camp2ServerId) {
        this.camp2ServerId = camp2ServerId;
    }

    public Map<Long, Map<Integer, Integer>> getPersonalToolMap() {
        return personalToolMap;
    }
}
