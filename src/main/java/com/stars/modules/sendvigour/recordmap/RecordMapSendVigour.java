package com.stars.modules.sendvigour.recordmap;

import com.stars.core.module.ModuleContext;
import com.stars.core.recordmap.RecordMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by gaopeidian on 2017/3/29.
 */

public class RecordMapSendVigour{
	//内存数据
	//<stepId , getCount>
    private Map<Integer , Integer> getRewardRecord = new HashMap<Integer, Integer>();

    protected ModuleContext context;
    protected RecordMap recordMap;
    
    public RecordMapSendVigour(ModuleContext context) {
    	this.context = context;
        this.recordMap = this.context.recordMap();
        initRecordMapData();
    }

    private void getData(){
    	setRewardRecord(recordMap.getString("SendVigour.getRewardRecord", ""));
    }
    
    private void setData(){
    	recordMap.setString("SendVigour.getRewardRecord", getRewardRecordStr(getRewardRecord));
    }
    
    private void initRecordMapData(){
    	getData();
    	setData();
    }
    
    private void setRewardRecord(String value){
    	getRewardRecord.clear();
        if (value == null || value.equals("") || value.equals("0")) {
			return;
		}
		String sts[] = value.split("\\|");
		for (String temp : sts) {
			String st[] = temp.split("\\=");
			if (st.length >= 2) {
				int stepId = Integer.parseInt(st[0]);
				int getCount = Integer.parseInt(st[1]);
				getRewardRecord.put(stepId, getCount);
			}
		}
    }
    
    private String getRewardRecordStr(Map<Integer, Integer> getRewardRecordMap){
        StringBuilder sb = new StringBuilder();
        
        int size = getRewardRecordMap.size();
        int index = 0;
        Set<Entry<Integer, Integer>> entrySet = getRewardRecordMap.entrySet();
        for (Entry<Integer, Integer> entry : entrySet) {
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			index ++;
			if (index < size) {
				sb.append("|");
			}
		}
        
    	return sb.toString();
    }
    
    public Map<Integer, Integer> getRewardRecord(){
    	return this.getRewardRecord;
    }
    
    public void setRewardRecord(Map<Integer, Integer> value){
    	this.getRewardRecord = value;
    	setData();
    }
    
    public void reset(){
    	this.getRewardRecord.clear();
    	setData();
    }
}
