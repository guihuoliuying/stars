package com.stars.modules.mind.prodata;

import com.stars.core.attr.Attribute;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 心法等级表Vo数据;
 * Created by gaopeidian on 2016/9/21.
 */
public class MindLevelVo {
    private int mindId;
    private int level;
    private int stage;
    private String resistance;
    private String itemposition;
    private String attribute;
    private String material;
    private int fightscore;
    private int color;
    private String eff;
    
    /* 内存数据 */
    //private List<Integer> resistenceList = new ArrayList<Integer>();
    private List<Integer> itemPositionList = new ArrayList<Integer>();
    private Map<Integer, Integer> materialMap = new HashMap();
    
    private Attribute voAttribute = new Attribute();
    private Attribute resistAttribute = new Attribute();
    
    public int getMindId() {
        return mindId;
    }

    public void setMindId(int mindId) {
        this.mindId = mindId;
    }
    
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
    
    public String getResistance() {
        return resistance;
    }

//    public void setResistance(String resistance) {
//        this.resistance = resistance;
//        if (StringUtil.isEmpty(resistance) || "0".equals(resistance)) {
//            return;
//        }
//        String[] temp = resistance.split("\\+");
//        if (temp.length >= 2) {
//			resistenceList.clear();
//			resistenceList.add(Integer.parseInt(temp[0]));
//			resistenceList.add(Integer.parseInt(temp[1]));
//		}
//    }
    
    public void setResistance(String resistance) {
    	this.resistance = resistance;
    	resistAttribute.strToAttribute(this.resistance);
    }

    public String getItemPosition() {
        return itemposition;
    }

    public void setItemPosition(String itemposition) {
        this.itemposition = itemposition;
        if (StringUtil.isEmpty(itemposition) || "0".equals(itemposition)) {
            return;
        }
        String[] temp = itemposition.split("\\+");
        if (temp.length >= 2) {
        	itemPositionList.clear();
        	itemPositionList.add(Integer.parseInt(temp[0]));
        	itemPositionList.add(Integer.parseInt(temp[1]));
		}
    }
    
    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        voAttribute.strToAttribute(this.attribute);
    }
    
    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
        if (StringUtil.isEmpty(material) || "0".equals(material)) {
            return;
        }
        materialMap.clear();
        String[] temp = material.split(",");
        for(int  i = 0, len = temp.length; i < len; i++){
            String valueArr[] = temp[i].split("\\+");
            if(valueArr.length >= 2){
                this.materialMap.put(Integer.parseInt(valueArr[0]), Integer.parseInt(valueArr[1]));
            }
        }
    }
    
    public int getFightscore() {
        return fightscore;
    }

    public void setFightscore(int fightscore) {
        this.fightscore = fightscore;
    }
    
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
    
    public String getEff() {
        return eff;
    }

    public void setEff(String eff) {
        this.eff = eff;
    }
    
//    public List<Integer> getResistenceList(){
//    	return resistenceList;
//    }
    
    public List<Integer> getItemPositionList(){
    	return itemPositionList;
    }
    
    public Map<Integer, Integer> getMaterialMap(){
    	return this.materialMap;
    }
    
    public Attribute getVoAttribute() {
        return voAttribute;
    }
    
    public Attribute getResistAttribute() {
        return resistAttribute;
    }
}
