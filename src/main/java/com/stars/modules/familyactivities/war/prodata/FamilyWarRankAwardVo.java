package com.stars.modules.familyactivities.war.prodata;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/29.
 */
public class FamilyWarRankAwardVo {

    private int id;
    private int period;
    private String rank;
    private int post; // 并非完全等价于家族职位（族长，参战，非参战）
    private String award;
    private int templateId;

    private int minRank;
    private int maxRank;
    private Map<Integer, Integer> toolMap;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) throws Exception {
        this.rank = rank;
        int[] rankArray = StringUtil.toArray(rank, int[].class, '+');
        this.minRank = rankArray[0];
        this.maxRank = rankArray[1];
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        this.toolMap = StringUtil.toMap(award, Integer.class, Integer.class, '+', '|');
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getMinRank() {
        return minRank;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public Map<Integer, Integer> getToolMap() {
        return toolMap;
    }

    public int getObjType() {
        return post;
    }

}
