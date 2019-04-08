package com.stars.modules.elitedungeon.prodata;

import com.stars.util.StringUtil;

/**
 * Created by gaopeidian on 2017/3/8.
 */
public class EliteDungeonVo {
    private int eliteid;
    private String name;
    private String showitem;
    private String icon;
    private String iconposition;
    private int worldid;
    private int activedungeon;
    private int stageid;
    private int reward;
    private int helpreward;
    private int firstreward;
    private int vigorcost;
    private int bossmonster;
    private int advisefightscore;
    private int adviselevel;
    private String desc;
    private String modelscale;
    private String machlevelrange;
    private int[] levelRange;
      
    /* 内存数据 */
    private int winDropId = -1;
    private int lossDropId = -1;
    
    public int getEliteId() {
        return eliteid;
    }

    public void setEliteId(int value) {
        this.eliteid = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;       	
    }
    
    public String getShowItem() {
        return showitem;
    }

    public void setShowItem(String value) {
        this.showitem = value;       	
    }
    
    public String getIcon() {
        return icon;
    }

    public void setIcon(String value) {
        this.icon = value;       	
    }
    
    public String getIconPosition() {
        return iconposition;
    }

    public void setIconPosition(String value) {
        this.iconposition = value;       	
    }
    
    public int getWorldid() {
        return worldid;
    }

    public void setWorldId(int value) {
        this.worldid = value;
    }
    
    public int getActiveDungeon() {
        return activedungeon;
    }

    public void setActiveDungeon(int value) {
        this.activedungeon = value;
    }
    
    public int getStageId() {
        return stageid;
    }

    public void setStageId(int value) {
        this.stageid = value;
    }
    
    public int getReward() {
        return reward;
    }

    public void setReward(int value) {
        this.reward = value;
        
//        if (reward == null || reward.equals("") || reward.equals("0")) {
//			return;
//		}
//		String[] ts = reward.split("\\+");
//		if (ts.length >= 2) {
//			this.winDropId = Integer.parseInt(ts[0]);
//			this.lossDropId = Integer.parseInt(ts[1]);
//		}
    }
    
    public int getHelpreward() {
        return helpreward;
    }

    public void setHelpreward(int value) {
        this.helpreward = value;
    }
    
    public int getFirstreward() {
        return firstreward;
    }

    public void setFirstreward(int value) {
        this.firstreward = value;
    }
    
    public int getVigorCost() {
        return vigorcost;
    }

    public void setVigorCost(int value) {
        this.vigorcost = value;
    }
    
    public int getBossMonster() {
        return bossmonster;
    }

    public void setBossMonster(int value) {
        this.bossmonster = value;
    }
    
    public int getAdviseFightscore() {
        return advisefightscore;
    }

    public void setAdviseFightscore(int value) {
        this.advisefightscore = value;
    }
    
    public int getAdviseLevel() {
        return adviselevel;
    }

    public void setAdviseLevel(int value) {
        this.adviselevel = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String value) {
        this.desc = value;       	
    }
    
    public String getModelScale() {
        return modelscale;
    }

    public void setModelScale(String value) {
        this.modelscale = value;       	
    }
   
    public String getMachlevelrange() {
		return machlevelrange;
	}

	public void setMachlevelrange(String machlevelrange) throws Exception {
		this.machlevelrange = machlevelrange;
		if(StringUtil.isNotEmpty(machlevelrange)){			
			levelRange = StringUtil.toArray(machlevelrange, int[].class, '+');
		}
	}

	public int[] getLevelRange() {
		return levelRange;
	}

	public void setLevelRange(int[] levelRange) {
		this.levelRange = levelRange;
	}

	/* 内存数据 */
    public int getWinDropId() {
        return winDropId;
    }
    
    public int getLossDropId() {
        return lossDropId;
    }
}
