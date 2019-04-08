package com.stars.services.escort;

import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2016/12/7.
 */
public class Escorter {

    private long leaderId;
    private long roleId;
    private String fightId;
    private FighterEntity playerEntity;
    private List<FighterEntity> otherEntities;
    private boolean isDead;
    private String deadPos;
    private boolean useMask;
    private byte camp;
    private boolean isOffline;
    private int remainRobTimes;
    private int teamId;
    private boolean isRevive;

    public Escorter(String fightId,long roleId,long leaderId) {
        this.fightId = fightId;
        this.roleId = roleId;
        this.leaderId = leaderId;
        this.isDead = false;
        this.useMask = false;
        this.deadPos = "";
        this.isOffline = false;
    }

    public Escorter() {
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public FighterEntity getPlayerEntity() {
        return playerEntity;
    }

    public void setPlayerEntity(FighterEntity playerEntity) {
        this.playerEntity = playerEntity;
    }

    public List<FighterEntity> getOtherEntities() {
        return otherEntities;
    }

    public void addOtherEntities(FighterEntity entitiy) {
        if(otherEntities == null){
            otherEntities = new ArrayList<>();
        }
        otherEntities.add(entitiy);
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean isDead) {
        this.isDead = isDead;
    }

    public String getDeadPos() {
        return deadPos;
    }

    public void setDeadPos(String deadPos) {
        this.deadPos = deadPos;
    }

    public boolean isUseMask() {
        return useMask;
    }

    public void setUseMask(boolean useMask) {
        this.useMask = useMask;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public byte getCamp() {
        return camp;
    }

    public void setCamp(byte camp) {
        this.camp = camp;
    }

    public long getLeaderId() {
        return leaderId;
    }

    public int getRemainRobTimes() {
        return remainRobTimes;
    }

    public void setRemainRobTimes(int remainRobTimes) {
        this.remainRobTimes = remainRobTimes;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean isRevive() {
        return isRevive;
    }

    public void setRevive(boolean isRevive) {
        this.isRevive = isRevive;
    }
}
