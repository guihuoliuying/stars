package com.stars.modules.masternotice.prodata;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/19.
 */
public class MasterNoticeVo {
	public static final byte TASK_TYPE_PASS_STAGE = 1;
	public static final byte TASK_TYPE_BUY_GOODS = 2;
	
    private int noticeId;
    private String levelSection;
    private String image;
    private String describe;
    private String type;
    private int odds;   
    private String award; 
   
    /* 内存数据 */
    private int minLevel = 0;
    private int maxLevel = 0;
    private byte taskType = -1;
    private int taskParam = -1;
    private Map<Integer, Integer> awardMap = new HashMap();
    
    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int value) {
        this.noticeId = value;
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
    
    public String getImage() {
        return image;
    }

    public void setImage(String value) {
        this.image = value;
    }
    
    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String value) {
        this.describe = value;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
        
        if (type == null || type.equals("") || type.equals("0")) {
   			return;
   		}
   		String[] sts = type.split("\\+");
   		if (sts.length >= 2) {
			taskType = Byte.parseByte(sts[0]);
			taskParam = Integer.parseInt(sts[1]);
		}
    }
    
    public int getOdds() {
        return odds;
    }

    public void setOdds(int value) {
        this.odds = value;
    }
    
    public String getAward() {
        return award;
    }

    public void setAward(String value) {
        this.award = value;
        
        if (award == null || award.equals("") || award.equals("0")) {
			return;
		}
		String[] sts = award.split("\\|");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("\\+");
			if (ts.length >= 2) {
				awardMap.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
			}
		}
    }
    
    public int getMinLevel(){
    	return minLevel;
    }
    
    public int getMaxLevel(){
    	return maxLevel;
    }
    
    public byte getTaskType(){
    	return taskType;
    }
    
    public int getTaskParam(){
    	return taskParam;
    }
    
    public Map<Integer, Integer> getAwardMap(){
    	return awardMap;
    }
}
