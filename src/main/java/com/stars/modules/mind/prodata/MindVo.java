package com.stars.modules.mind.prodata;

import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 心法表Vo数据;
 * Created by gaopeidian on 2016/9/21.
 */
public class MindVo {
    private int mindId;
    private int mindType;
    private int order;
    private String preMind;
    private String icon;
    private String name;
    private String desc;
    private String active;
    private String activecost;
    
    /* 内存数据 */
    private Map<Integer, Integer> activecostMap = new HashMap();
    private List<MindActiveData> activeDatas = new ArrayList<MindActiveData>();
    
    public int getMindId() {
        return mindId;
    }

    public void setMindId(int mindId) {
        this.mindId = mindId;
    }
    
    public int getMindType() {
        return mindType;
    }

    public void setMindType(int mindType) {
        this.mindType = mindType;
    }
    
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getPreMind() {
        return preMind;
    }

    public void setPreMind(String preMind) {
        this.preMind = preMind;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
     
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
        if (StringUtil.isEmpty(active) || "0".equals(active)) {
            return;
        }
        activeDatas.clear();
        String[] temp = active.split(",");
        for(int  i = 0, len = temp.length; i < len; i++){
            String valueArr[] = temp[i].split("\\+");
            if(valueArr.length >= 2){
               int typeId = Integer.parseInt(valueArr[0]);
               int param = Integer.parseInt(valueArr[1]);
               MindActiveData data = new MindActiveData(typeId, param);
               activeDatas.add(data);
            }
        }
    }
    
    public String getActivecost() {
        return activecost;
    }

    public void setActivecost(String activecost) {
        this.activecost = activecost;
        if (StringUtil.isEmpty(activecost) || "0".equals(activecost)) {
            return;
        }
        activecostMap.clear();
        String[] temp = activecost.split(",");
        for(int  i = 0, len = temp.length; i < len; i++){
            String valueArr[] = temp[i].split("\\+");
            if(valueArr.length >= 2){
                this.activecostMap.put(Integer.parseInt(valueArr[0]), Integer.parseInt(valueArr[1]));
            }
        }
    }
    
    public Map<Integer, Integer> getActivecostMap(){
    	return this.activecostMap;
    }
    
    public List<MindActiveData> getActiveDatas(){
    	return this.activeDatas;
    }
    
//    public String getActiveDesc(){
//    	if (activeDatas.size() > 0) {
//			MindActiveData data = activeDatas.get(0);
//			return data.desc;
//		}
//    	
//    	return "";
//    }
}
