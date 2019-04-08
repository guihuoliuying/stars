package com.stars.modules.role.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 资源表;
 * Created by panzhenfeng on 2016/6/22.
 */
public class Resource {

    private int id;
    private String model;
    private String highmodel;
    private int hitsize;
    private String skill;
    private List<Integer> skillList;
    private int movespeed;
    private int uiposition;
    private int turnSpeed;
    private String headIcon;
    private int npctalkmodel;
    private List<Integer>bornSkill;
    private List<Integer>normalSkill;
    private String walkSound;

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(this.getModel());
        buff.writeInt(this.getHitsize());
        buff.writeString(this.getSkill());
        buff.writeInt(this.getMovespeed());
        buff.writeInt(this.getUiposition());
        buff.writeInt(this.getTurnSpeed());
        buff.writeString(this.getHeadIcon());
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getHitsize() {
        return hitsize;
    }

    public void setHitsize(int hitsize) {
        this.hitsize = hitsize;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
        skillList = new LinkedList<>();
        String[] strs = skill.split("\\|");
        String[] tempStr;
        for (int i = 0; i < strs.length; i++) {
            tempStr = strs[i].split("\\+");
            for (int j = 0; j < tempStr.length; j++) {
                this.skillList.add(Integer.parseInt(tempStr[j]));
            }
        }
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

    public List<Integer> getSkillList() {
        return this.skillList;
    }

    public int getTurnSpeed() {
        return turnSpeed;
    }

    public void setTurnSpeed(int turnSpeed) {
        this.turnSpeed = turnSpeed;
    }

    public String getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }


	public int getNpctalkmodel() {
		return npctalkmodel;
	}


	public void setNpctalkmodel(int npctalkmodel) {
		this.npctalkmodel = npctalkmodel;
	}

    public String getHighmodel() {
        return highmodel;
    }

    public void setHighmodel(String highmodel) {
        this.highmodel = highmodel;
    }
    
    public void setBornSkill(String skillStr){
    	if (skillStr == null || skillStr.equals("") || skillStr.equals("0")) {
			return;
		}
    	this.bornSkill = new ArrayList<Integer>();
    	String[] sts = skillStr.split("[+]");
    	for (String id : sts) {
			bornSkill.add(Integer.parseInt(id));
		}
    }
    
    public List<Integer>getBornSkill(){
    	return this.bornSkill;
    }


	public List<Integer> getNormalSkill() {
		return normalSkill;
	}


	public void setNormalSkill(String normalSkill) {
		if (normalSkill == null || normalSkill.equals("") || normalSkill.equals("0")) {
			return;
		}
		this.normalSkill = new ArrayList<Integer>();
		String[] sts = normalSkill.split("[+]");
		for (String id:sts) {
			this.normalSkill.add(Integer.parseInt(id));
		}
	}

    public String getWalkSound() {
        return walkSound;
    }

    public void setWalkSound(String walkSound) {
        this.walkSound = walkSound;
    }
}
