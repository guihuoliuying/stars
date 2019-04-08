package com.stars.modules.familyTask.prodata;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class FamilyMissionInfo {
    private int id;
    private String image;
    private String name;
    private String describe;
    private String group;
    private Map<Integer,Integer> groupMap;
    private int award;
    private int odds;
    private String reqRoleLv;
    private int minRoleLevel;
    private int maxRoleLevel;
    private String reqFamilyLv;
    private int minFamilyLevel;
    private int maxFamilyLevel;
//    private String helpDesc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
        this.groupMap = StringUtil.toMap(group,Integer.class,Integer.class,'+','|');
    }

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public String getReqRoleLv() {
        return reqRoleLv;
    }

    public void setReqRoleLv(String reqRoleLv) {
        this.reqRoleLv = reqRoleLv;
        if(StringUtil.isEmpty(reqRoleLv) || "0".equals(reqRoleLv)) return;
        String[] array = reqRoleLv.split("\\+");
        this.minRoleLevel = Integer.parseInt(array[0]);
        this.maxRoleLevel = Integer.parseInt(array[1]);
    }

    public String getReqFamilyLv() {
        return reqFamilyLv;
    }

    public void setReqFamilyLv(String reqFamilyLv) {
        this.reqFamilyLv = reqFamilyLv;
        if(StringUtil.isEmpty(reqFamilyLv) || "0".equals(reqFamilyLv)) return;
        String[] array = reqFamilyLv.split("\\+");
        this.minFamilyLevel = Integer.parseInt(array[0]);
        this.maxFamilyLevel = Integer.parseInt(array[1]);
    }

//    public String getHelpDesc() {
//        return helpDesc;
//    }
//
//    public void setHelpDesc(String helpDesc) {
//        this.helpDesc = helpDesc;
//    }

    public boolean matchRoleLevel(int roleLevel){
        return (this.minRoleLevel == 0 || this.minRoleLevel <= roleLevel) && (this.maxRoleLevel == 0 || this.maxRoleLevel >= roleLevel);
    }

    public boolean matchFamilyLevel(int familyLevel){
        return (this.minFamilyLevel == 0 || this.minFamilyLevel <= familyLevel) && (this.maxFamilyLevel == 0 || this.maxFamilyLevel >= familyLevel);
    }

    public Map<Integer, Integer> getGroupMap() {
        return groupMap;
    }
}
