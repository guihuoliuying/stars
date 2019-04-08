package com.stars.multiserver.familywar.knockout;

import com.stars.modules.scene.fightdata.FighterEntity;

/**
 * Created by zhaowenshuo on 2016/11/28.
 */
public class KnockoutFamilyMemberInfo {
    private int mainServerId;
    private long familyId;
    private long memberId;
    private byte postId;
    private String name;
    private int level;
    private int fightScore;

    private FighterEntity fighterEntity;
    private byte joinedState;

    private String battleId;//当前所处于的battleId

    public int getMainServerId() {
        return mainServerId;
    }

    public void setMainServerId(int mainServerId) {
        this.mainServerId = mainServerId;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public byte getPostId() {
        return postId;
    }

    public void setPostId(byte postId) {
        this.postId = postId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public FighterEntity getFighterEntity() {
        return fighterEntity;
    }

    public void setFighterEntity(FighterEntity fighterEntity) {
        this.fighterEntity = fighterEntity;
    }

    public byte getJoinedState() {
        return joinedState;
    }

    public void setJoinedState(byte joinedState) {
        this.joinedState = joinedState;
    }
}
