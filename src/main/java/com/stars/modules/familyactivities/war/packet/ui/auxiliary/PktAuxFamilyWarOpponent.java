package com.stars.modules.familyactivities.war.packet.ui.auxiliary;

/**
 * Created by zhaowenshuo on 2016/12/20.
 */
public class PktAuxFamilyWarOpponent {

    private long roleId;
    private String roleName;
    private int fightScore;
    private int modelId;
    private byte camp;

    public PktAuxFamilyWarOpponent(long roleId, String roleName, int fightScore, int modelId, byte camp) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.fightScore = fightScore;
        this.modelId = modelId;
        this.camp = camp;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public byte getCamp() {
        return camp;
    }

    public void setCamp(byte camp) {
        this.camp = camp;
    }
}
