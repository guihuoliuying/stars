package com.stars.modules.bravepractise.prodata;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/16.
 */
public class BraveInfoVo {
    private int braveId;
    private String levelSection;
    private int group;
    private int odds;  
    private int award;
    private String showAward;
    private int count;   
   
    /* 内存数据 */
    private int minLevel = 0;
    private int maxLevel = 0;
    private Map<Integer, Integer> showAwardMap = new LinkedHashMap<>();
    
    public int getBraveId() {
        return braveId;
    }

    public void setBraveId(int value) {
        this.braveId = value;
    }
    
    public String getLevelSection() {
        return levelSection;
    }

    public void setLevelSection(String value) {
        this.levelSection = value;
        
        if (levelSection == null || levelSection.equals("") || levelSection.equals("0")) {
			return;
		}
		String sts[] = levelSection.split("\\+");
		if (sts.length >= 2) {
			if (!sts[0].equals("")) {
				minLevel = Integer.parseInt(sts[0]);
			}
			if (!sts[1].equals("")) {
				maxLevel = Integer.parseInt(sts[1]);
			}
		}
    }
    
    public int getGroup() {
        return group;
    }

    public void setGroup(int value) {
        this.group = value;
    }
    
    public int getOdds() {
        return odds;
    }

    public void setOdds(int value) {
        this.odds = value;
    }
    
    public int getAward() {
        return award;
    }

    public void setAward(int value) {
        this.award = value;
    }
    
    public String getShowAward() {
        return showAward;
    }

    public void setShowAward(String value) {
        this.showAward = value;
        
        if (showAward == null || showAward.equals("") || showAward.equals("0")) {
			return;
		}
		String[] sts = showAward.split("\\|");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("\\+");
			if (ts.length >= 2) {
				showAwardMap.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
			}
		}
    }
    
    public int getCount() {
        return count;
    }

    public void setCount(int value) {
        this.count = value;
    }
    
    public int getMinLevel(){
    	return minLevel;
    }
    
    public int getMaxLevel(){
    	return maxLevel;
    }
    
    public Map<Integer, Integer> getShowAwardMap(){
    	return showAwardMap;
    }
}
