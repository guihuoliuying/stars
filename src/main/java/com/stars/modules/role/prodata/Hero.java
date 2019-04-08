package com.stars.modules.role.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 英雄表,用于记录选择的英雄的基础数据如，模型名字，展示待机动作，血条高度等等.
 * Created by panzhenfeng on 2016/6/20.
 */
public class Hero{
    private int heroId;
    private byte job;
    private String model;
    private int hitSize;
    private String skill;
    private int movespeed;
    private int uiposition;
    private String equipment;
    private String pose;
    private int roleinfoscale;

    public Hero() {
    }


    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(this.getJob());
        buff.writeString(this.getModel());
        buff.writeInt(this.getHitSize());
        buff.writeString(this.getSkill());
        buff.writeInt(this.getMovespeed());
        buff.writeInt(this.getUiposition());
        buff.writeString(this.getEquipment());
        buff.writeString(this.getPose());
        buff.writeInt(this.getRoleinfoscale());
    }


    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public byte getJob() {
        return job;
    }

    public void setJob(byte job) {
        this.job = job;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getHitSize() {
        return hitSize;
    }

    public void setHitSize(int hitSize) {
        this.hitSize = hitSize;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public int getMovespeed() {
        return movespeed;
    }

    public void setMovespeed(int movespeed) {
        this.movespeed = movespeed;
    }

    public int getUiposition() {
        return uiposition;
    }

    public void setUiposition(int uiposition) {
        this.uiposition = uiposition;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getPose() {
        return pose;
    }

    public void setPose(String pose) {
        this.pose = pose;
    }

    public int getRoleinfoscale() {
        return roleinfoscale;
    }

    public void setRoleinfoscale(int roleinfoscale) {
        this.roleinfoscale = roleinfoscale;
    }
}
