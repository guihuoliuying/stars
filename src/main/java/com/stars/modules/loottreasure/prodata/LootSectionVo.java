package com.stars.modules.loottreasure.prodata;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panzhenfeng on 2016/10/10.
 */
public class LootSectionVo {
    private int levelsection;
    private int matching;
    private int stageid;
    private int monsterid;
    private String show;
    private String award;
    private int buff;

    //解析的数据;
    private  int minLevelSectionLevel = 0;
    public Map<Integer, Integer> awardMap = new HashMap<>();
    public void setMinLevelSectionLevel(int value){
        minLevelSectionLevel = value;
    }

    public int getMinLevelSectionLevel(){
        return minLevelSectionLevel;
    }

    public boolean isInRange(int level){
        return level >= minLevelSectionLevel && level <= levelsection;
    }

    public int getLevelsection() {
        return levelsection;
    }

    public void setLevelsection(int levelsection) {
        this.levelsection = levelsection;
    }

    public int getMatching() {
        return matching;
    }

    public void setMatching(int matching) {
        this.matching = matching;
    }

    public int getStageid() {
        return stageid;
    }

    public void setStageid(int stageid) {
        this.stageid = stageid;
    }

    public int getMonsterid() {
        return monsterid;
    }

    public void setMonsterid(int monsterid) {
        this.monsterid = monsterid;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        awardMap.clear();
        String[] arr = this.award.split("\\|");
        String[] valueArr = null;
        for(int i = 0, len = arr.length; i<len; i++){
            valueArr = arr[i].split("\\+");
            awardMap.put(Integer.parseInt(valueArr[0]), Integer.parseInt(valueArr[1]));
        }
    }

    public int getBuff() {
        return buff;
    }

    public void setBuff(int buff) {
        this.buff = buff;
    }
}
