package com.stars.modules.elitedungeon.recordmap;

import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.recordmap.RecordMap;
import com.stars.modules.MConst;
import com.stars.modules.elitedungeon.EliteDungeonManager;
import com.stars.modules.role.RoleModule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaopeidian on 2017/3/8.
 */

public class RecordMapEliteDungeon{
	//内存数据
	private Set<Integer> enterEliteDungeons = new HashSet<Integer>();
	private Set<Integer> passedEliteDungeons = new HashSet<Integer>();
	private int playCount = 0;
	private int rewardTimes = 0;
	private int helpTimes = 0;

    protected Map<String, Module> moduleMap;
    protected ModuleContext context;
    protected RecordMap recordMap;
    
    public RecordMapEliteDungeon(Map<String, Module> moduleMap , ModuleContext context) {
    	this.moduleMap = moduleMap;
    	this.context = context;
        this.recordMap = this.context.recordMap();
        initRecordMapData();
    }
   
    private void initRecordMapData(){
    	setEnterEliteDungeonsByStr(recordMap.getString("EliteDungeon.enterEliteDungeons", ""));
    	setPassedEliteDungeonsByStr(recordMap.getString("EliteDungeon.passedEliteDungeons", ""));
    	setPlayCount(Integer.parseInt(recordMap.getString("EliteDungeon.playCount", "0")));
    	
    	RoleModule roleModule = (RoleModule)moduleMap.get(MConst.Role);
    	int roleLevel = roleModule.getLevel();
    	int defaultRewardTimes = EliteDungeonManager.getRewardTimesByLevel(roleLevel);
    	int defaultHelpTimes = EliteDungeonManager.getRewardTimesByLevel(roleLevel);
    	setRewardTimes(Integer.parseInt(recordMap.getString("EliteDungeon.rewardTimes", Integer.toString(defaultRewardTimes))));
    	setHelpTimes(Integer.parseInt(recordMap.getString("EliteDungeon.helpTimes", Integer.toString(defaultHelpTimes))));
    }

    public Set<Integer> getEnterEliteDungeons(){
    	return enterEliteDungeons;
    }
    
    public void setEnterEliteDungeons(Set<Integer> value){
    	enterEliteDungeons = value;
    	recordMap.setString("EliteDungeon.enterEliteDungeons", getEnterEliteDungeonsStr());
    }
    
    public Set<Integer> getPassedEliteDungeons(){
    	return passedEliteDungeons;
    }
    
    public void setPassedEliteDungeons(Set<Integer> value){
    	passedEliteDungeons = value;
    	recordMap.setString("EliteDungeon.passedEliteDungeons", getPassedEliteDungeonsStr());
    }
    
    public int getPlayCount(){
    	return playCount;
    }
    
    public void setPlayCount(int value){
    	playCount = value;
    	recordMap.setString("EliteDungeon.playCount", Integer.toString(playCount));
    }
    
    public int getRewardTimes(){
    	return rewardTimes;
    }
    
    public void setRewardTimes(int value){
    	rewardTimes = value;
    	recordMap.setString("EliteDungeon.rewardTimes", Integer.toString(rewardTimes));
    }
    
    public int getHelpTimes(){
    	return helpTimes;
    }
    
    public void setHelpTimes(int value){
    	helpTimes = value;
    	recordMap.setString("EliteDungeon.helpTimes", Integer.toString(helpTimes));
    }
    
    private String getEnterEliteDungeonsStr(){
    	StringBuffer buffer = new StringBuffer("");
    	
    	int size = enterEliteDungeons.size();
    	int index = 0;
    	for (Integer eliteId : enterEliteDungeons) {
    		buffer.append(eliteId);
			if (index != size - 1) {
				buffer.append("+");
			}
    		index++;
		}
    	
    	return buffer.toString();
    }
    
    private void setEnterEliteDungeonsByStr(String str){
    	enterEliteDungeons.clear();
    	
    	if (str == null || str.equals("") || str.equals("0")) {
			return;
		}
    	
    	String[] ts = str.split("\\+");
		for (String uts : ts) {
			enterEliteDungeons.add(Integer.parseInt(uts));
		}
    }
    
    private String getPassedEliteDungeonsStr(){
    	StringBuffer buffer = new StringBuffer("");
    	
    	int size = passedEliteDungeons.size();
    	int index = 0;
    	for (Integer eliteId : passedEliteDungeons) {
    		buffer.append(eliteId);
			if (index != size - 1) {
				buffer.append("+");
			}
    		index++;
		}
    	
    	return buffer.toString();
    }
    
    private void setPassedEliteDungeonsByStr(String str){
    	passedEliteDungeons.clear();
    	
    	if (str == null || str.equals("") || str.equals("0")) {
			return;
		}
    	
    	String[] ts = str.split("\\+");
		for (String uts : ts) {
			passedEliteDungeons.add(Integer.parseInt(uts));
		}
    }
    
    public void reset(){
    	setPlayCount(0);
    	
    	RoleModule roleModule = (RoleModule)moduleMap.get(MConst.Role);
    	int roleLevel = roleModule.getLevel();
    	int _rewardTimes = EliteDungeonManager.getRewardTimesByLevel(roleLevel);
    	int _helpTimes = EliteDungeonManager.getHelpTimesByLevel(roleLevel);
    	
    	setRewardTimes(_rewardTimes);
    	setHelpTimes(_helpTimes);
    }
}
