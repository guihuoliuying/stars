package com.stars.modules.familyactivities.war.packet.ui.auxiliary;

/**
 * Created by zhaowenshuo on 2016/12/15.
 */
public class PktAuxFamilyWarApplicant {

    private long roleId;
    private String name;
    private byte postId;
    private int level;
    private int fightScore;
    private int elapseFromOffline;
    private boolean isOnline;
    private byte qualification; // 资格

    public PktAuxFamilyWarApplicant(long roleId, String name, byte postId, int level, int fightScore, int elapseFromOffline, boolean isOnline, byte qualification) {
        this.roleId = roleId;
        this.name = name;
        this.postId = postId;
        this.level = level;
        this.fightScore = fightScore;
        this.elapseFromOffline = elapseFromOffline;
        this.isOnline = isOnline;
        this.qualification = qualification;
    }

    public PktAuxFamilyWarApplicant(long roleId, String name, byte postId, int level, int fightScore, byte qualification) {
        this.roleId = roleId;
        this.name = name;
        this.postId = postId;
        this.level = level;
        this.fightScore = fightScore;
        this.qualification = qualification;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getPostId() {
        return postId;
    }

    public void setPostId(byte postId) {
        this.postId = postId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getElapseFromOffline() {
        return elapseFromOffline;
    }

    public void setElapseFromOffline(int elapseFromOffline) {
        this.elapseFromOffline = elapseFromOffline;
    }

    public byte getQualification() {
        return qualification;
    }

    public void setQualification(byte qualification) {
        this.qualification = qualification;
    }

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}
