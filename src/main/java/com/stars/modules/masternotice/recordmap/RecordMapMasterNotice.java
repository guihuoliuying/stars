package com.stars.modules.masternotice.recordmap;

import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.recordmap.RecordMap;
import com.stars.modules.MConst;
import com.stars.modules.masternotice.MasterNoticeConstant;
import com.stars.modules.masternotice.MasterNoticeManager;
import com.stars.modules.masternotice.MasterNoticeModule;

import java.util.*;

/**
 * Created by gaopeidian on 2016/11/21.
 */

public class RecordMapMasterNotice{
	//内存数据
	private byte isEverIn = 0;//是否点开过皇榜页面(第一次点开皇榜页面的时候，需要生成固定的几条皇榜任务)
	private int doneCount = 0;
    private int freeRefreshCount = 0; 
    private int costRefreshCount = 0;
    private long autoRefreshTimestamp = 0;//自动刷新的时间戳，若为0，则表示没自动刷新过
    
    private List<Integer> finishGroups = new ArrayList<Integer>();
    
    Map<Integer, MasterNoticeData> noticesMap = new HashMap<Integer, MasterNoticeData>();

    protected Map<String, Module> moduleMap;
    protected ModuleContext context;
    protected RecordMap recordMap;
    
    public RecordMapMasterNotice(Map<String, Module> moduleMap , ModuleContext context) {
    	this.moduleMap = moduleMap;
    	this.context = context;
        this.recordMap = this.context.recordMap();
        initRecordMapData();
    }

    private void getData(){
    	MasterNoticeModule mnModule = (MasterNoticeModule)moduleMap.get(MConst.MasterNotice);
    	this.isEverIn = Byte.parseByte(recordMap.getString("MasterNotice.isEverIn", Byte.toString((byte)0)));
    	this.doneCount = Integer.parseInt(recordMap.getString("MasterNotice.doneCount", Integer.toString(0)));
    	this.freeRefreshCount = Integer.parseInt(recordMap.getString("MasterNotice.freeRefreshCount", Integer.toString(MasterNoticeManager.freeRefreshCount)));
    	this.costRefreshCount = Integer.parseInt(recordMap.getString("MasterNotice.costRefreshCount", Integer.toString(MasterNoticeManager.costRefreshCount)));
    	this.autoRefreshTimestamp = Long.parseLong(recordMap.getString("MasterNotice.autoRefreshTimestamp", Long.toString(new Date().getTime())));    	 	
    	setFinishGroupsByStr(recordMap.getString("MasterNotice.finishGroups", getFinishGroupsStr(new ArrayList<Integer>())));
    	setNoticesMapByStr(recordMap.getString("MasterNotice.noticesMap", getNoticesMapStr(mnModule.getFirstMasterNotices())));
    }
    
    private void setData(){
    	setIsEverIn(this.isEverIn);
    	setDoneCount(this.doneCount);
        setFreeRefreshCount(this.freeRefreshCount);
        setCostRefreshCount(this.costRefreshCount);
        setAutoRefreshTimestamp(this.autoRefreshTimestamp);
        setFinishGroups(this.finishGroups);
        setNoticesMap(this.noticesMap);
    }
   
    private void initRecordMapData(){
    	getData();
    	setData();
    }
    
    public void setIsEverIn(byte value){
    	this.isEverIn = value;
    	recordMap.setString("MasterNotice.isEverIn", Byte.toString(this.isEverIn));
    }
    
    public byte getIsEverIn(){
    	return this.isEverIn;
    }
    
    public void setDoneCount(int value){
    	this.doneCount = value;
    	recordMap.setString("MasterNotice.doneCount", Integer.toString(this.doneCount));
    }
    
    public int getDoneCount(){
    	return this.doneCount;
    }
    
    public void setFreeRefreshCount(int value){
    	this.freeRefreshCount = value;
    	recordMap.setString("MasterNotice.freeRefreshCount", Integer.toString(this.freeRefreshCount));
    }
    
    public int getFreeRefreshCount(){
    	return this.freeRefreshCount;
    }
    
    public void setCostRefreshCount(int value){
    	this.costRefreshCount = value;
    	recordMap.setString("MasterNotice.costRefreshCount", Integer.toString(this.costRefreshCount));
    }
    
    public int getCostRefreshCount(){
    	return this.costRefreshCount;
    }
    
    public void setAutoRefreshTimestamp(long value){
    	this.autoRefreshTimestamp = value;
    	recordMap.setString("MasterNotice.autoRefreshTimestamp", Long.toString(this.autoRefreshTimestamp));
    }
    
    public long getAutoRefreshTimestamp(){
    	return this.autoRefreshTimestamp;
    }
    
    public void setFinishGroups(List<Integer> value){
    	this.finishGroups = value;
    	recordMap.setString("MasterNotice.finishGroups", getFinishGroupsStr(this.finishGroups));
    }
    
    public List<Integer> getFinishGroups(){
    	return this.finishGroups;
    }
    
    public void setNoticesMap(Map<Integer, MasterNoticeData> value){
    	this.noticesMap = value;
    	recordMap.setString("MasterNotice.noticesMap", getNoticesMapStr(this.noticesMap));
    }
    
    public Map<Integer, MasterNoticeData> getNoticesMap(){
    	return this.noticesMap;
    }
    
    private void setFinishGroupsByStr(String value){
    	String groupsStr = value;
    	finishGroups.clear();
        if (groupsStr == null || groupsStr.equals("") || groupsStr.equals("0")) {
			return;
		}
		String sts[] = groupsStr.split("\\+");
		for (String temp : sts) {
			if (!temp.equals("")) {
				finishGroups.add(Integer.parseInt(temp));
			}
		}
    }
    
    
    private String getFinishGroupsStr(List<Integer> groups){
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
    
      
    private void setNoticesMapByStr(String value){
    	noticesMap.clear();
        if (value == null || value.equals("") || value.equals("0")) {
			return;
		}
        
    	String[] sts = value.split("\\|");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("\\+");
			if (ts.length >= 3) {
				int noticeId = Integer.parseInt(ts[0]);					
				byte status = Byte.parseByte(ts[1]);
				int process = Integer.parseInt(ts[2]);
				MasterNoticeData noticeData = new MasterNoticeData(noticeId, status, process);
				noticesMap.put(noticeId, noticeData);
			}
		}
    }
    
    
    private String getNoticesMapStr(Map<Integer, MasterNoticeData> datas){
        StringBuilder sb = new StringBuilder();
        
        int size = datas.size();
        int index = 0;
        for (MasterNoticeData data : datas.values()) {
			int noticeId = data.getNoticeId();					
			byte status = data.getStatus();
			int process = data.getProcess();
			
			sb.append(noticeId);
			sb.append("+");
			sb.append(status);
			sb.append("+");
			sb.append(process);
			
			index ++;
			if (index < size) {
				sb.append("|");
			}
		}
        
    	return sb.toString();
    }
      
   
    
    public void reset(boolean isInit){
    	MasterNoticeModule mnModule = (MasterNoticeModule)moduleMap.get(MConst.MasterNotice);
        int doneCount = 0;
        int freeRefreshCount = MasterNoticeManager.freeRefreshCount;
        int costRefreshCount = MasterNoticeManager.costRefreshCount;
        List<Integer> finishGroups = new ArrayList<Integer>();
        Map<Integer, MasterNoticeData> noticesMap;
        if (isInit) {
			noticesMap = mnModule.getFirstMasterNotices();
		}else{
			noticesMap = mnModule.getNewMasterNotices(new ArrayList<Integer>() , MasterNoticeConstant.noticeCount);		        
		}
        setDoneCount(doneCount);
        setFreeRefreshCount(freeRefreshCount);
        setCostRefreshCount(costRefreshCount);
        setFinishGroups(finishGroups);
        setNoticesMap(noticesMap);
    }
}
