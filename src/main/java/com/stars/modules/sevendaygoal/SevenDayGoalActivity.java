package com.stars.modules.sevendaygoal;

import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.drop.DropModule;
import com.stars.modules.gem.GemModule;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.ActOpenTime4;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.ride.RideModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.sevendaygoal.packet.ClientGoalData;
import com.stars.modules.sevendaygoal.packet.ClientSevenDayGetReward;
import com.stars.modules.sevendaygoal.prodata.SevenDayGoalVo;
import com.stars.modules.sevendaygoal.userdata.ActSevenDayFinishCount;
import com.stars.modules.sevendaygoal.userdata.ActSevenDayRewardRecord;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/17.
 */
public class SevenDayGoalActivity {
	private int activityId;
	private SevenDayGoalModule module;
	
	//用户数据
	//<目标类型，目标完成记录>
	private Map<Integer, ActSevenDayFinishCount> finishCountMap = null;
	//<目标id，目标奖励领取记录>
	private Map<Integer, ActSevenDayRewardRecord> recordRewardMap = null;
	
	public SevenDayGoalActivity(int activityId , SevenDayGoalModule module){
		this.activityId = activityId;
		this.module = module;
		initUserData();
	}
	
	private void initUserData(){
		finishCountMap = module.getFinishCounts(activityId);
		if (finishCountMap == null) {
			finishCountMap = new HashMap<Integer, ActSevenDayFinishCount>();
		}
		
		recordRewardMap = module.getRewardRecords(activityId);
		if (recordRewardMap == null) {
			recordRewardMap = new HashMap<Integer, ActSevenDayRewardRecord>();
		}
	}
	
	public void reset(){
		for (ActSevenDayFinishCount actSevenDayFinishCount : finishCountMap.values()) {
			actSevenDayFinishCount.setFinishCount(0);
			module.updateData(actSevenDayFinishCount);
		} 
		
		for (ActSevenDayRewardRecord record : recordRewardMap.values()) {
			record.setGotCount(0);
			module.updateData(record);
		}  
	}
	
	public void close(){

	}
	
    public void getDayRewardsInfo(int days){
    	int openServerDays = getOpenDays();
    	
    	if (days == -1) {
			days = openServerDays;
		}
    	
    	if (days > openServerDays) {
			module.warn("operateact_sevendaytarget_openday" , Integer.toString(days));
			return;
		}
    	
		Map<Integer, SevenDayGoalVo> dayGoalsVoMap = SevenDayGoalManager.getDayGoalsVoMap(activityId , days);
		if (dayGoalsVoMap == null) {
			module.warn("get_no_day_product_data");
			return;
		}
		
		//rewardMap leftGetCountMap
		Map<Integer, Byte> rewardMap = new HashMap<Integer, Byte>();
		Map<Integer , Integer> leftGetCountMap = new HashMap<Integer, Integer>();
		for (SevenDayGoalVo vo : dayGoalsVoMap.values()) {
			int goalId = vo.getGoalId();
			byte isGot = 0;
			ActSevenDayRewardRecord record = getActSevenDayRewardRecord(goalId);
			if (record.getGotCount() >= 1) {
				isGot = 1;
			}
			int leftGetCount = ServiceHelper.sevenDayGoalService().getLeftRewardCount(activityId , goalId);
			
			rewardMap.put(goalId, isGot);
			leftGetCountMap.put(goalId, leftGetCount);
		}
		
		//finishMap
		Map<Integer, Integer> finishMap = new HashMap<Integer, Integer>();
    	for (SevenDayGoalType goalType : SevenDayGoalType.values()) {
			int type = goalType.getGoalType();
			int finishCount = getFinishCountByType(type);
			finishMap.put(type, finishCount);
		}
		
		ClientGoalData clientGoalData = new ClientGoalData();
		clientGoalData.setFlag(ClientGoalData.Flag_Day_Rewards_Info);
		clientGoalData.setDays(days);
		clientGoalData.setRewardMap(rewardMap);
		clientGoalData.setLeftCountMap(leftGetCountMap);
		clientGoalData.setFinishCountMap(finishMap);
		module.send(clientGoalData);
	}
    
    public void sendFinishCount(int goalType){
    	Map<Integer, Integer> finishMap = new HashMap<Integer, Integer>();
    	
		int finishCount = getFinishCountByType(goalType);
			
		finishMap.put(goalType, finishCount);
		   	
    	ClientGoalData clientGoalData = new ClientGoalData();
    	clientGoalData.setFlag(ClientGoalData.Flag_Update_Finish_Count);
    	clientGoalData.setFinishCountMap(finishMap);
    	//clientGoalData.setUpdateType(goalType);
    	//clientGoalData.setUpdateFinishCount(finishCount);
        module.send(clientGoalData);
    }
    
    public void sendOpenServerDayAndMaxDay(){
    	int openServerDay = getOpenDays();
    	int maxDay = SevenDayGoalManager.getMaxDay(activityId);
    	
		ClientGoalData clientGoalData = new ClientGoalData();
		clientGoalData.setFlag(ClientGoalData.Flag_Update_OpenServer_Day);
		clientGoalData.setOpenServerDay(openServerDay);
		clientGoalData.setMaxDay(maxDay);
		module.send(clientGoalData);
	}
    
    public void getReward(int goalId){
		SevenDayGoalVo vo = SevenDayGoalManager.getSevenDayGoalVo(goalId);
		if (vo == null) {
			module.warn("获取产品数据失败");
			return;
		}

		int finishCount = getFinishCountByType(vo.getGoalType());
		int goalNum = vo.getGoalNum();
		if (finishCount < goalNum) {
			module.warn("未达到条件");
			return;
		}
		
		ActSevenDayRewardRecord record = getActSevenDayRewardRecord(goalId);
		if (record.getGotCount() > 0) {
			module.warn("您已领取");
			return;
		}
		
		byte getRet = ServiceHelper.sevenDayGoalService().getReward(activityId , goalId , module.id());;
		
		if (getRet == 0) {
			return;
		}
		
		DropModule dropModule = (DropModule)(module.getModulMap().get(MConst.Drop));
		Map<Integer, Integer> reward = dropModule.executeDrop(vo.getGroupId() , 1 , true);
		ToolModule toolModule = (ToolModule)module.getModulMap().get(MConst.Tool);
		toolModule.addAndSend(reward, SevenDayGoalConstant.sevenDayGoalCSConst , EventType.SEVENDAYGOAL.getCode());
		
		record.setGotCount(record.getGotCount() + 1);
		module.updateData(record);
		
		int days = vo.getDays();
		byte isGot = 1;
		ClientSevenDayGetReward clientSevenDayGetReward = new ClientSevenDayGetReward();
		clientSevenDayGetReward.setGoalId(goalId);
		clientSevenDayGetReward.setDays(days);
		clientSevenDayGetReward.setIsGot(isGot);
		module.send(clientSevenDayGetReward);
	}
    
	ActSevenDayFinishCount getActSevenDayFinishCount(int goalType){
		ActSevenDayFinishCount finishCount = finishCountMap.get(goalType);
		if (finishCount == null) {
			finishCount = new ActSevenDayFinishCount(module.id() , activityId , goalType , 0);
			finishCountMap.put(finishCount.getType(), finishCount);
			module.insertData(finishCount);
		}
		
		return finishCount;
    }
		
	ActSevenDayRewardRecord getActSevenDayRewardRecord(int goalId){
		ActSevenDayRewardRecord record = recordRewardMap.get(goalId);
		if (record == null) {
			record = new ActSevenDayRewardRecord(module.id() , activityId , goalId , 0);
			recordRewardMap.put(record.getGoalId(), record);
			module.insertData(record);
		}
		
		return record;
	}
	
	public int getId(){
		return activityId;
	}

	public void addFinishCount(int goalType , int addCount){
//		ActSevenDayFinishCount finishCount = getActSevenDayFinishCount(SevenDayGoalType.Ride_Level_Up_Goal.getGoalType());
//		finishCount.setFinishCount(finishCount.getFinishCount() + addCount);
//		module.updateData(finishCount);
	}
	
	public int getFinishCountByType(int goalType){
//		if (goalType == SevenDayGoalType.Ride_Level_Up_Goal.getGoalType()) {
//			ActSevenDayFinishCount finishCount = getActSevenDayFinishCount(goalType);
//			return finishCount.getFinishCount();
//		}
		if (goalType == SevenDayGoalType.Ride_Stage_Goal.getGoalType()) {
			RideModule rideModule = (RideModule)module.getModulMap().get(MConst.Ride);
			return rideModule.getRideStage();
		}else if (goalType == SevenDayGoalType.Equipment_Strengthen_Level_Goal.getGoalType()) {
			NewEquipmentModule newEquipmentModule = (NewEquipmentModule)module.getModulMap().get(MConst.NewEquipment);
			return newEquipmentModule.getRoleTotalStrengthLevel();
		}else if (goalType == SevenDayGoalType.Equipment_Star_Level_Goal.getGoalType()) {
			NewEquipmentModule newEquipmentModule = (NewEquipmentModule)module.getModulMap().get(MConst.NewEquipment);
			return newEquipmentModule.getRoleTotalStarLevel();
		}else if (goalType == SevenDayGoalType.Buddy_Level_Goal.getGoalType()) {
			BuddyModule buddyModule = (BuddyModule)module.getModulMap().get(MConst.Buddy);
			return buddyModule.allBuddyLevelSum();
		}else if (goalType == SevenDayGoalType.Gem_Fight_Score_Goal.getGoalType()) {
			GemModule gemModule = (GemModule)module.getModulMap().get(MConst.GEM);
			return gemModule.getGemFightScore();
		}else if (goalType == SevenDayGoalType.Skill_Level_Goal.getGoalType()) {
			SkillModule skillModule = (SkillModule)module.getModulMap().get(MConst.Skill);
			return skillModule.getUseSkillLvTotal();
		}else if (goalType == SevenDayGoalType.Fight_Score_Goal.getGoalType()) {
			RoleModule roleModule = (RoleModule)module.getModulMap().get(MConst.Role);
			return roleModule.getFightScore();
		}else if (goalType == SevenDayGoalType.Role_Level_Goal.getGoalType()) {
			RoleModule roleModule = (RoleModule)module.getModulMap().get(MConst.Role);
			return roleModule.getRoleRow().getLevel();
		}else if (goalType == SevenDayGoalType.Guest_Level_Goal.getGoalType()) {
			GuestModule guestModule = (GuestModule)module.getModulMap().get(MConst.Guest);
			return guestModule.getGuestAllLevel();
		}else{
			return 0;
		}
	}
	
	public String getHasRewardDayString(){
		if (!module.isEffectiveTime(activityId)) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder("");
		
		int openServerDay = getOpenDays();
		Map<Integer, Map<Integer, SevenDayGoalVo>> vosMap = SevenDayGoalManager.getSevenDayVoMap(activityId);
		for (Map<Integer, SevenDayGoalVo> dayVo : vosMap.values()) {
			for (SevenDayGoalVo vo : dayVo.values()) {
				if (openServerDay < vo.getDays()) {//还没到开启的天数
					continue;
				}
				
				int goalId = vo.getGoalId();
				int goalType = vo.getGoalType();
				int goalNum = vo.getGoalNum();
				int finishCount = getFinishCountByType(goalType);
				if (goalNum > finishCount) {//目标未达成
					continue;
				}

				ActSevenDayRewardRecord record = getActSevenDayRewardRecord(goalId);
				if (record.getGotCount() >= 1) {//奖励已领取
					continue;
				}
				
				int leftGetCount = ServiceHelper.sevenDayGoalService().getLeftRewardCount(activityId , goalId);
				if (leftGetCount <= 0 && leftGetCount != -1) {//全服剩余领取次数为0,-1则为无限制
					continue;
				}
				
				//可领取
				builder.append(vo.getDays()).append("+");
			}
		}
			
		return builder.toString().isEmpty() ? null : builder.toString();
	}
	
	public int getOpenDays(){
		OperateActVo actVo = OperateActivityManager.getOperateActVo(activityId);
		if (actVo == null) {
			com.stars.util.LogUtil.info("SevenDayGoalActivity.getOpenDays get no OperateActVo,activityId=" + activityId);
			return 0;
		}
		
		ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
		if (!(openTimeBase instanceof ActOpenTime4)) {
			LogUtil.info("SevenDayGoalActivity.getOpenDays time no suitable,activityId=" + activityId);
			return 0;
		}
		
		ActOpenTime4 openTime4 = (ActOpenTime4)openTimeBase;
		RoleModule roleModule = (RoleModule)module.getModulMap().get(MConst.Role);
		int createRoleDays = roleModule.getRoleCreatedDays();
		int openDays = ActOpenTime4.getOpenDays(openTime4, createRoleDays);
		
		return openDays;
	}
	
	
	/**
	 * 获取某个目标的领取状态(打运营日志用)
	 * @return 可领但未领/已领取/不可领取分别标识0/1/2
	 */
	public int getGoalStatus(int goalId){
		int canGet = 0;
		int haveGot = 1;
		int canNotGet = 2;
		
		SevenDayGoalVo vo = SevenDayGoalManager.getSevenDayGoalVo(goalId);
		if (vo == null) {
			return canNotGet;
		}

		ActSevenDayRewardRecord record = getActSevenDayRewardRecord(goalId);
		if (record.getGotCount() > 0) {
			return haveGot;
		}
		
		int finishCount = getFinishCountByType(vo.getGoalType());
		int goalNum = vo.getGoalNum();
		if (finishCount < goalNum) {
			return canNotGet;
		}else{
			return canGet;
		}	
	}
}
