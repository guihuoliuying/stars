package com.stars.modules.sevendaygoal;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.modules.MConst;
import com.stars.modules.buddy.event.BuddyUpgradeEvent;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.gem.event.GemFightScoreChangeEvent;
import com.stars.modules.guest.event.GuestAttributeChangeEvent;
import com.stars.modules.newequipment.event.EquipStarChangeEvent;
import com.stars.modules.newequipment.event.EquipStrengthChangeEvent;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.opentime.ActOpenTime4;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.ride.event.RideLevelUpEvent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.sevendaygoal.event.RewardCountChangeEvent;
import com.stars.modules.sevendaygoal.packet.ClientGoalData;
import com.stars.modules.sevendaygoal.prodata.SevenDayGoalVo;
import com.stars.modules.sevendaygoal.userdata.ActSevenDayFinishCount;
import com.stars.modules.sevendaygoal.userdata.ActSevenDayRewardRecord;
import com.stars.modules.skill.event.SkillBatchLvUpEvent;
import com.stars.modules.skill.event.SkillLevelUpEvent;
import com.stars.modules.skill.event.SkillPositionChangeEvent;
import com.stars.util.LogUtil;

import java.util.*;


/**
 * Created by gaopeidian on 2016/12/5.
 */
public class SevenDayGoalModule extends AbstractModule implements OpActivityModule {
	/**
	 * 当前正在进行的活动
	 * 为null则当前无正在进行的活动
	 */
    private SevenDayGoalActivity curActivity = null;
	
    //所有活动的用户数据
	/**
	 * 活动目标完成次数数据
	 * <活动id，<目标类型，完成记录>>
	 */
	private Map<Integer , Map<Integer, ActSevenDayFinishCount>> sevenDayFinishCountMap = new HashMap<Integer , Map<Integer, ActSevenDayFinishCount>>();
	
	/**
	 * 活动领奖记录
	 * <活动id，<目标奖励id，领取记录>>
	 */
	private Map<Integer , Map<Integer, ActSevenDayRewardRecord>> sevenDayRewardRecordMap = new HashMap<Integer , Map<Integer, ActSevenDayRewardRecord>>();
	
	public SevenDayGoalModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super(MConst.SevenDayGoal, id, self, eventDispatcher, moduleMap);
	}
	
	@Override
	public void onCreation(String name_, String account_) throws Throwable {
		
    }
	
    @Override
    public void onDataReq() throws Exception {
        //加载用户数据
    	String sql1 = "select * from `actsevendayfinishcount` where `roleid`=" + id();
        List<ActSevenDayFinishCount> finishCounts = DBUtil.queryList(DBUtil.DB_USER, ActSevenDayFinishCount.class, sql1);
        if (finishCounts != null && finishCounts.size() > 0) {
			for (ActSevenDayFinishCount finishCount : finishCounts) {
				int activityId = finishCount.getOperateActId();
				Map<Integer, ActSevenDayFinishCount> map = sevenDayFinishCountMap.get(activityId);
				if (map == null) {
					map = new HashMap<Integer, ActSevenDayFinishCount>();
					sevenDayFinishCountMap.put(activityId, map);
				}
				map.put(finishCount.getType(), finishCount);				
			}
		}
        
      	String sql2 = "select * from `actsevendayrewardrecord` where `roleid`=" + id();
        List<ActSevenDayRewardRecord> rewardRecords = DBUtil.queryList(DBUtil.DB_USER, ActSevenDayRewardRecord.class, sql2);
        if (rewardRecords != null && rewardRecords.size() > 0) {
			for (ActSevenDayRewardRecord record : rewardRecords) {
				int activityId = record.getOperateActId();
				Map<Integer, ActSevenDayRewardRecord> map = sevenDayRewardRecordMap.get(activityId);
				if (map == null) {
					map = new HashMap<Integer, ActSevenDayRewardRecord>();
					sevenDayRewardRecordMap.put(activityId, map);
				}
				map.put(record.getGoalId(), record);				
			}
		}
    }   
	
    @Override
	public void onInit(boolean isCreation){
		int initActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_SevenDayGoal);
		opOnInit(initActivityId);
		
		checkActivityData();
		
		//标记需要计算红点
	    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
	}
    
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
    	OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
        if (redPointIds.contains(Integer.valueOf(RedPointConst.SEVEN_DAY_GOAL))) {
            if (curActivity != null && operateActivityModule.isShow(curActivity.getId())) {
            	redPointMap.put(RedPointConst.SEVEN_DAY_GOAL, curActivity.getHasRewardDayString());     
			}else{
				redPointMap.put(RedPointConst.SEVEN_DAY_GOAL, null);  
			}   
        }
    }
    
    /**
     * 是否在活动有效时间内
     * @param openTime
     * @param openDays
     * @return
     */
	public boolean isEffectiveTime() {
		int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_SevenDayGoal);
		return isEffectiveTime(curActivityId);
	}
	
	public boolean isEffectiveTime(int activityId) {
		OperateActVo actVo = OperateActivityManager.getOperateActVo(activityId);
		if (actVo == null) return false;
		ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
		if (!(openTimeBase instanceof ActOpenTime4)) return true;
		ActOpenTime4 openTime4 = (ActOpenTime4)openTimeBase;
		RoleModule roleModule = module(MConst.Role);
		int createRoleDays = roleModule.getRoleCreatedDays();
		return openTime4.isEffectiveTime(openTime4, createRoleDays);
	}
    
    @Override
	public int getCurShowActivityId() {
    	int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_SevenDayGoal);
    	if (curActivityId != -1) {
    		OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
			OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
			if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap()) && isEffectiveTime()) {
				return curActivityId;
			}
		}
    	
    	return -1;
	}
    
    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_SevenDayGoal);
        if (curActivityId == -1) return (byte)0;
        
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte)0;
        
        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte)0;
        
        if (labelDisappearBase instanceof NeverDisappear) {
			return (byte)1;
		}else if(labelDisappearBase instanceof DisappearByDays){
        	ActOpenTimeBase openTime = operateActVo.getActOpenTimeBase();
            if (!(openTime instanceof ActOpenTime4)) return (byte)0;
            
            ActOpenTime4 actOpenTime4 = (ActOpenTime4)openTime;
            int startDays = actOpenTime4.getStartDays();
            RoleModule roleModule = module(MConst.Role);
    		int createRoleDays = roleModule.getRoleCreatedDays();
            int continueDays = createRoleDays - startDays + 1;
            int canContinueDays = ((DisappearByDays)labelDisappearBase).getDays();
            return continueDays > canContinueDays ? (byte)0 : (byte)1;
        }else if (labelDisappearBase instanceof DisappearByTime) {
			Date date = ((DisappearByTime)labelDisappearBase).getDate();
			return date.getTime() < new Date().getTime() ? (byte)0 : (byte)1;
		}  
        
        return (byte)0;
    }
    
	public void opOnInit(int initActivityId) {
		if (initActivityId == -1) {
			if (curActivity != null) {
				curActivity.close();
				curActivity = null;
			}
		}else{
			if (curActivity != null && curActivity.getId() != initActivityId) {
				curActivity.close();
				curActivity = null;
			}else if (curActivity != null && curActivity.getId() == initActivityId) {
				return;
			}
			
			if (curActivity == null) {
				OperateActVo vo = OperateActivityManager.getOperateActVo(initActivityId);
				if (vo == null) {
					com.stars.util.LogUtil.info("SevenDayGoalModule.opOnInit get no OperateActVo,operateActId=" + initActivityId);
					return;
				}				
				curActivity = new SevenDayGoalActivity(initActivityId, this);
			}		
		}
	}
	
	public void opOnOpen(int openActivityId) {
		if (curActivity != null && curActivity.getId() != openActivityId) {
			curActivity.close();
			curActivity = null;
		}else if (curActivity != null && curActivity.getId() == openActivityId) {
			return;
		}
		
		if (curActivity == null) {
			OperateActVo vo = OperateActivityManager.getOperateActVo(openActivityId);
			if (vo == null) {
				LogUtil.info("SevenDayGoalModule.opOnOpen get no OperateActVo,operateActId=" + openActivityId);
				return;
			}				
			curActivity = new SevenDayGoalActivity(openActivityId, this);
		}		
	}

	public void opOnClose(int closeActivityId) {
		if (curActivity != null && curActivity.getId() == closeActivityId) {
			curActivity.close();
			curActivity = null;
		}			
	}

	/**
	 * 检查活动数据，把没在进行中的活动数据清掉
	 */
	private void checkActivityData(){
		int curActivityId = -1;
		if (curActivity != null) {
			curActivityId = curActivity.getId();
		}
		
		//活动目标完成次数数据
		Iterator<Map.Entry<Integer , Map<Integer, ActSevenDayFinishCount>>> it = sevenDayFinishCountMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer , Map<Integer, ActSevenDayFinishCount>> entry = it.next();
			int activityId = entry.getKey();
			if (activityId != curActivityId) {
				Map<Integer, ActSevenDayFinishCount> finishCounts = entry.getValue();
				for (ActSevenDayFinishCount finishCount : finishCounts.values()) {
					context().delete(finishCount);
				}
				
				it.remove();
			}
		}
			
		//活动奖励数据
		Iterator<Map.Entry<Integer , Map<Integer, ActSevenDayRewardRecord>>> it2 = sevenDayRewardRecordMap.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry<Integer , Map<Integer, ActSevenDayRewardRecord>> entry = it2.next();
			int activityId = entry.getKey();
			if (activityId != curActivityId) {
				Map<Integer, ActSevenDayRewardRecord> records = entry.getValue();
				for (ActSevenDayRewardRecord record : records.values()) {
					context().delete(record);
				}
				
				it2.remove();
			}
		}		
	}
	
	public void handleOperateActivityEvent(OperateActivityEvent event){
		int opType = event.getActivityType();
		if (opType == OperateActivityConstant.ActType_SevenDayGoal) {
			byte flag = event.getFlag();
			int activityId = event.getActivityId();
			if (flag == OperateActivityEvent.Flag_Open_Activity) {
				opOnOpen(activityId);
				//标记需要计算红点
			    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
			}else if (flag == OperateActivityEvent.Flag_Close_Activity) {
				opOnClose(activityId);
				//标记需要计算红点
			    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
			}
		}
	}
	
	public void handleOperateActivityFlowEvent(OperateActivityFlowEvent event){
		if (event.getStepType() == OperateActivityConstant.FLOW_STEP_NEW_DAY) {//跨天
			if (curActivity != null) {
				curActivity.sendOpenServerDayAndMaxDay();
			}
			
			//标记需要计算红点
		    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
		}
	}
	
	public void handleRoleLevelUp(){
		if (curActivity != null) {
			curActivity.sendFinishCount(SevenDayGoalType.Role_Level_Goal.getGoalType());
		}
		
		//标记需要计算红点
	    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
	}
	
	public void handleForeShowChange(ForeShowChangeEvent foreShowChangeEvent){
		//若触发开放坐骑系统，则需要更新一下坐骑目标
		if (foreShowChangeEvent.getMap().containsKey(ForeShowConst.RIDE)) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Ride_Stage_Goal.getGoalType());
			}
		}
		
		//标记需要计算红点
	    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
	}
	
	public void handleEvent(Event event){
//		if (event instanceof RideLevelUpEvent) {
//			if (curActivity != null) {
//				RideLevelUpEvent rideLevelUpEvent = (RideLevelUpEvent)event;
//				int addCount = 0;
//				if (rideLevelUpEvent.getPrevLevelId() != 0) {
//					addCount = rideLevelUpEvent.getCurrLevelId() - rideLevelUpEvent.getPrevLevelId();
//				}
//				curActivity.addFinishCount(SevenDayGoalType.Ride_Level_Up_Goal.getGoalType(), addCount);
//				curActivity.sendFinishCount(SevenDayGoalType.Ride_Level_Up_Goal.getGoalType());
//			}
//		}
		if (event instanceof RideLevelUpEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Ride_Stage_Goal.getGoalType());
			}
		}else if (event instanceof EquipStrengthChangeEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Equipment_Strengthen_Level_Goal.getGoalType());
			}
		}else if (event instanceof EquipStarChangeEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Equipment_Star_Level_Goal.getGoalType());
			}
		}else if (event instanceof BuddyUpgradeEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Buddy_Level_Goal.getGoalType());
			}
		}else if (event instanceof GemFightScoreChangeEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Gem_Fight_Score_Goal.getGoalType());
			}
		}else if (event instanceof SkillLevelUpEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Skill_Level_Goal.getGoalType());
			}
		}else if (event instanceof SkillBatchLvUpEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Skill_Level_Goal.getGoalType());
			}
		}else if (event instanceof SkillPositionChangeEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Skill_Level_Goal.getGoalType());
			}
		}else if (event instanceof FightScoreChangeEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Fight_Score_Goal.getGoalType());
			}
		}else if (event instanceof RewardCountChangeEvent) {
			RewardCountChangeEvent rewardCountChangeEvent = (RewardCountChangeEvent)event;
			ClientGoalData clientGoalData = new ClientGoalData();
			clientGoalData.setFlag(ClientGoalData.Flag_Update_Left_Get_Count);
			clientGoalData.setUpdateGoalId(rewardCountChangeEvent.getGoalId());
			clientGoalData.setUpdateCount(rewardCountChangeEvent.getLeftGetCount());
			send(clientGoalData);
		}else if (event instanceof GuestAttributeChangeEvent) {
			if (curActivity != null) {
				curActivity.sendFinishCount(SevenDayGoalType.Guest_Level_Goal.getGoalType());
			}
		}
		
		//标记需要计算红点
	    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
	}
	
	public Map<String, Module> getModulMap(){
		return moduleMap();
	}
	
	public void getDayRewardsInfo(int activityId , int days){
		if (curActivity == null || curActivity.getId() != activityId) {
			warn("Activity not on");
			return;
		}
			
		if (days == -1) {
			curActivity.sendOpenServerDayAndMaxDay();
			return;
		}
		
		curActivity.getDayRewardsInfo(days);
	}
	
	public void getReward(int activityId , int days , int goalId){
		if (curActivity == null || curActivity.getId() != activityId) {
			warn("Activity not on");
			return;
		}
		
		curActivity.getReward(goalId);
		
		//标记需要计算红点
	    signCalRedPoint(MConst.SevenDayGoal, RedPointConst.SEVEN_DAY_GOAL);
	}
	
	/**
	* 操作数据的方法
	*/
	public Map<Integer, ActSevenDayFinishCount> getFinishCounts(int activityId){
		return sevenDayFinishCountMap.get(activityId);
	}
	
	public Map<Integer, ActSevenDayRewardRecord> getRewardRecords(int activityId){
		return sevenDayRewardRecordMap.get(activityId);
	}
	
	public void insertData(DbRow dbRow){
		if (dbRow instanceof ActSevenDayFinishCount) {
			ActSevenDayFinishCount finishCount = (ActSevenDayFinishCount)dbRow;
			int activityId = finishCount.getOperateActId();
			Map<Integer, ActSevenDayFinishCount> countMap = sevenDayFinishCountMap.get(activityId);
			if (countMap == null) {
				countMap = new HashMap<Integer, ActSevenDayFinishCount>();
				sevenDayFinishCountMap.put(activityId, countMap);
			}
			countMap.put(finishCount.getType(), finishCount);
			
			context().insert(dbRow);
		}else if (dbRow instanceof ActSevenDayRewardRecord) {
			ActSevenDayRewardRecord record = (ActSevenDayRewardRecord)dbRow;
			int activityId = record.getOperateActId();
			Map<Integer, ActSevenDayRewardRecord> recordMap = sevenDayRewardRecordMap.get(activityId);
			if (recordMap == null) {
				recordMap = new HashMap<Integer, ActSevenDayRewardRecord>();
				sevenDayRewardRecordMap.put(activityId, recordMap);
			}
			recordMap.put(record.getGoalId(), record);
			
			context().insert(dbRow);
		}
	}
	
	public void updateData(DbRow dbRow){
		context().update(dbRow);
	}
	
	/**
	 * 获取运营登入登出日志String
	 */
	public String getLoginLogoutLogString(){
		if (curActivity == null) {
			return "";
		}
		
		int nowDay = curActivity.getOpenDays();
		int maxDay = SevenDayGoalManager.getMaxDay(curActivity.getId());
		if (nowDay > maxDay) {
			return "";
		}
			
		StringBuffer stringBuffer = new StringBuffer();
		
		String[] dayString = {"day_one:" , "two_one:" , "three_one:" , "four_one:" , "five_one:" , "six_one:" , "seven_one:"};

		for (int i = 0; i < dayString.length; i++) {
			int day = i + 1;
			Map<Integer, SevenDayGoalVo> dayGoalsVoMap = SevenDayGoalManager.getDayGoalsVoMap(curActivity.getId() , day);
			if (dayGoalsVoMap != null) {
				String key = dayString[i];
				stringBuffer.append(key);
				
				//新建voList并排序
				List<SevenDayGoalVo> voList = new ArrayList<SevenDayGoalVo>();
				for (SevenDayGoalVo vo : dayGoalsVoMap.values()) {
					voList.add(vo);
				}
				Collections.sort(voList);
					
				for (SevenDayGoalVo vo : voList) {
					int logStatus = curActivity.getGoalStatus(vo.getGoalId());
					stringBuffer.append(logStatus);
					stringBuffer.append("@");
				}
				
				stringBuffer.append("#");
			}
		}
		
		return stringBuffer.toString();
	}
}

