package com.stars.multiserver.familywar.knockout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaowenshuo on 2016/11/28.
 */
public class KnockoutFamilyInfo implements Comparable<KnockoutFamilyInfo> {
    private long familyId; // 家族id
    private int rank;    //在排行榜上的排名
    private String familyName; // 家族名字
    private Map<Long, KnockoutFamilyMemberInfo> memberMap;
    private long totalFightScore;
    private AtomicInteger totalSupport;

    private int mainServerId;

    private Set<Long> applicationSheet; // 报名名单
    private Set<Long> teamSheet; // 参赛名单
    private long masterId;//族长Id

    private int seq; // 序列
    private int state; // 状态
    private String battleId;
    private boolean isWinner;

    public KnockoutFamilyInfo() {
        memberMap = new HashMap<>();
        applicationSheet = new HashSet<>();
        teamSheet = new HashSet<>();
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Map<Long, KnockoutFamilyMemberInfo> getMemberMap() {
        return memberMap;
    }

    public void setMemberMap(Map<Long, KnockoutFamilyMemberInfo> memberMap) {
        this.memberMap = memberMap;
    }

    public long getTotalFightScore() {
        return totalFightScore;
    }

    public void setTotalFightScore(long totalFightScore) {
        this.totalFightScore = totalFightScore;
    }

    public int getMainServerId() {
        return mainServerId;
    }

    public void setMainServerId(int mainServerId) {
        this.mainServerId = mainServerId;
    }

    public Set<Long> getApplicationSheet() {
        return applicationSheet;
    }

    public void setApplicationSheet(Set<Long> applicationSheet) {
        this.applicationSheet = applicationSheet;
    }

    public Set<Long> getTeamSheet() {
        return teamSheet;
    }

    public void setTeamSheet(Set<Long> teamSheet) {
        this.teamSheet = teamSheet;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public int compareTo(KnockoutFamilyInfo o) {
        return (int) (o.totalFightScore - this.totalFightScore);
    }

    public int getTotalSupport() {
        return totalSupport.get();
    }

    public void setTotalSupport(int totalSupport) {
        this.totalSupport.set(totalSupport);
    }

    public synchronized void addSupport() {
        this.totalSupport.getAndIncrement();
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean isWinner) {
        this.isWinner = isWinner;
    }

    public long getMasterId() {
        return masterId;
    }

    public void setMasterId(long masterId) {
        this.masterId = masterId;
    }
}
