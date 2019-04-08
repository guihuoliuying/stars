package com.stars.modules.bravepractise.recordmap;

import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.recordmap.RecordMap;
import com.stars.modules.MConst;
import com.stars.modules.bravepractise.BravePractiseManager;
import com.stars.modules.bravepractise.BravePractiseModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 角色的勇者试炼每日需要重置的数据;
 * Created by gaopeidian on 2016/11/16.
 */

public class RecordMapBravePractise{
	//内存数据
	private int doneCount = 0;
    private int totalCount = 0; 
    private List<Integer> groups = new ArrayList<Integer>();

    protected Map<String, Module> moduleMap;
    protected ModuleContext context;
    protected RecordMap recordMap;
    
    public RecordMapBravePractise(Map<String, Module> moduleMap , ModuleContext context) {
    	this.moduleMap = moduleMap;
    	this.context = context;
        this.recordMap = this.context.recordMap();
        initRecordMapData();
    }

    private void getData(){
    	BravePractiseModule bpModule = (BravePractiseModule)moduleMap.get(MConst.BravePractise);
    	//setCountDataStr(recordMap.getString("BravePractise.countDataStr", getCountDataStr(0, bpModule.calculateTotalCount())));
    	setCountDataStr(recordMap.getString("BravePractise.countDataStr", getCountDataStr(0, BravePractiseManager.bravePractiseCount)));
    	setGroupsStr(recordMap.getString("BravePractise.groupsStr", getGroupsStr(new ArrayList<Integer>())));
    }
    
    private void setData(){
    	recordMap.setString("BravePractise.countDataStr", getCountDataStr(doneCount , totalCount));
        recordMap.setString("BravePractise.groupsStr", getGroupsStr(groups));
    }
    
    private void initRecordMapData(){
    	getData();
    	setData();
    }
    
    private void setCountDataStr(String value){
    	String countDataStr = value;
        
        if (countDataStr == null || countDataStr.equals("") || countDataStr.equals("0")) {
			return;
		}
		String sts[] = countDataStr.split("\\+");
		if (sts.length >= 2){
			if (!sts[0].equals("")) {
				doneCount = Integer.parseInt(sts[0]);
			}
			if (!sts[1].equals("")) {
				totalCount = Integer.parseInt(sts[1]);
			}
		}
    }
    
    private String getCountDataStr(int doneCount , int totalCount){
        StringBuilder sb = new StringBuilder();
    	
    	//添加已完成次数
    	sb.append(doneCount);
    	
    	sb.append("+");
    	
    	//添加答对次数
    	sb.append(totalCount);
    	
    	return sb.toString();
    }
    
    private void setGroupsStr(String value){
    	String groupsStr = value;
    	groups.clear();
        if (groupsStr == null || groupsStr.equals("") || groupsStr.equals("0")) {
			return;
		}
		String sts[] = groupsStr.split("\\+");
		for (String temp : sts) {
			if (!temp.equals("")) {
				groups.add(Integer.parseInt(temp));
			}
		}
    }
    
    private String getGroupsStr(List<Integer> groups){
        StringBuilder sb = new StringBuilder();
        
        int size = groups.size();
        int index = 0;
        for (Integer group : groups) {
			sb.append(group);
			index ++;
			if (index < size) {
				sb.append("+");
			}
		}
        
    	return sb.toString();
    }
    
//    public void setTotalCount(int value){
//    	this.totalCount = value;
//    	setData();
//    }
    
//    public int getTotalCount(){
//    	return this.totalCount;
//    }
    
    public void setDoneCount(int value){
    	this.doneCount = value;
    	setData();
    }
    
    public int getDoneCount(){
    	return this.doneCount;
    }
    
    public void setGroups(List<Integer> value){
    	this.groups = value;
    	setData();
    }
    
    public List<Integer> getGroups(){
    	return this.groups;
    }
    
    public void reset(){
    	BravePractiseModule bpModule = (BravePractiseModule)moduleMap.get(MConst.BravePractise);
    	this.doneCount = 0;
    	//this.totalCount = bpModule.calculateTotalCount();
    	this.totalCount = BravePractiseManager.bravePractiseCount;
    	this.groups.clear();
    	setData();
    }
}
