package com.stars.modules.familyactivities.expedition.prodata;

import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/11.
 */
public class FamilyExpeditionVo {

    private int expeditionId;
    private int step;
    private String image;
    private int stageId;
    private String award;
    private String name;
    private String description;
    private String descAward;
    private String chooseAward;
    private int familyMoneyAward;
    private int reqFamilyLevel;

    /* mem data */
    private List<FamilyActExpeditionStarAwardVo> starAwardList; // 条件类型+数量+itemid+数量
    private Map<Integer, Integer> chooseAwardMap;

    /* mem getter/setter */
    public List<FamilyActExpeditionStarAwardVo> getStarAwardList() {
        return starAwardList;
    }

    public Map<Integer, Integer> getChooseAwardMap() {
        return chooseAwardMap;
    }

    /* db getter/setter */
    public int getExpeditionId() {
        return expeditionId;
    }

    public void setExpeditionId(int expeditionId) {
        this.expeditionId = expeditionId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) throws Exception {
        this.award = award;
        this.starAwardList = new ArrayList<>();
        String[] array = award.split("\\|");
        if (array.length != 3) {
            throw new IllegalArgumentException("条件奖励的数量不为3");
        }
        for (String s : array) {
            int[] starAward = StringUtil.toArray(s, int[].class, '+');
            if (starAward.length != 4) {
                throw new IllegalArgumentException(s);
            }
            this.starAwardList.add(new FamilyActExpeditionStarAwardVo(starAward));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescAward() {
        return descAward;
    }

    public void setDescAward(String descAward) {
        this.descAward = descAward;
    }

    public String getChooseAward() {
        return chooseAward;
    }

    public void setChooseAward(String chooseAward) throws Exception {
        this.chooseAward = chooseAward;
        if (chooseAward == null || chooseAward.trim().equals("") || chooseAward.trim().equals("0")) {
            this.chooseAwardMap = new HashMap<>();
        } else {
            this.chooseAwardMap = StringUtil.toMap(chooseAward, Integer.class, Integer.class, '+', '|');
        }
    }

    public int getFamilyMoneyAward() {
        return familyMoneyAward;
    }

    public void setFamilyMoneyAward(int familyMoneyAward) {
        this.familyMoneyAward = familyMoneyAward;
    }

    public int getReqFamilyLevel() {
        return reqFamilyLevel;
    }

    public void setReqFamilyLevel(int reqFamilyLevel) {
        this.reqFamilyLevel = reqFamilyLevel;
    }
}
