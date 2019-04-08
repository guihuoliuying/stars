package com.stars.modules.role.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 职业表;
 * Created by panzhenfeng on 2016/6/22.
 */
public class Job {
    private int jobId;
    private int modelres;
    private String originequipment;
    private String pose;
    private int roleinfoscale;
    private int fightarea;
    private List<Integer> pSkillList;
    private String passskill;
    private List<Integer> bornPassSkill;

    public Job() {

    }

    public void writeToBuffer(NewByteBuffer buff) {
         buff.writeString(this.getPose());
         buff.writeInt(this.getRoleinfoscale());
         buff.writeInt(this.getFightarea());
    }

    public int getModelres() {
        return modelres;
    }

    public void setModelres(int modelres) {
        this.modelres = modelres;
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

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getFightarea() {
        return fightarea;
    }

    public void setFightarea(int fightarea) {
        this.fightarea = fightarea;
    }

    public String getPassskill() {
        return passskill;
    }

    public void setPassskill(String passskill) {
        this.passskill = passskill;
        this.pSkillList = new LinkedList<>();
        String[] strs = passskill.split("\\+");
        for (int i = 0; i < strs.length; i++) {
            this.pSkillList.add(Integer.parseInt(strs[i]));
        }
    }

    public List<Integer> getPSkillList(){
        return this.pSkillList;
    }
    
    public void setBornPassSkill(String bornPassSkill){
    	if (bornPassSkill == null || bornPassSkill.equals("") || bornPassSkill.equals("0")) {
			return;
		}
    	String[] strs = bornPassSkill.split("[+]");
    	this.bornPassSkill = new ArrayList<Integer>();
        for (int i = 0; i < strs.length; i++) {
            this.bornPassSkill.add(Integer.parseInt(strs[i]));
        }
    }
    
    public List<Integer> getBornPassSkill(){
    	return this.bornPassSkill;
    }

    public String getOriginequipment() {
        return originequipment;
    }

    public void setOriginequipment(String originequipment) {
        this.originequipment = originequipment;
    }

}
