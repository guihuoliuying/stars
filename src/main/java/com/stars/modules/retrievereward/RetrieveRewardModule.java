package com.stars.modules.retrievereward;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.prodata.DailyVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.retrievereward.event.PreDailyRecordResetEvent;
import com.stars.modules.retrievereward.prodata.RetrieveRewardVo;
import com.stars.modules.retrievereward.userdata.ActRetrieveRewardRecord;
import com.stars.modules.retrievereward.userdata.PreDailyRecord;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class RetrieveRewardModule extends AbstractModule implements OpActivityModule {
	/**
	 * 当前正在进行的活动
	 * 为null则当前无正在进行的活动
	 */
    private RetrieveRewardActivity curActivity = null;
	
    //所有活动的用户数据
    /**
	 * 角色前一天参与日常活动的次数记录(与活动无关的)
	 */
	private PreDailyRecord preDailyRecord = null;
	
	/**
	 * 活动找回奖励数据
	 * <活动id，<奖励id，奖励记录>>
	 */
	private Map<Integer , Map<Integer, ActRetrieveRewardRecord>> retrieveRewardMap = new HashMap<Integer , Map<Integer, ActRetrieveRewardRecord>>();
	
	public RetrieveRewardModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super(MConst.RetrieveReward, id, self, eventDispatcher, moduleMap);
	}
	
	@Override
	public void onCreation(String name_, String account_) throws Throwable {
		preDailyRecord = new PreDailyRecord(id() , RetrieveRewardManager.getDefaultPreDailyRecordMap() , new Date().getTime());
        this.context().insert(preDailyRecord);
    }

    @Override
    public void onDataReq() throws Exception {
        //加载用户数据
    	String sql1 = "select * from `actrevieverewardrecord` where `roleid`=" + id();
        List<ActRetrieveRewardRecord> rewardRecords = DBUtil.queryList(DBUtil.DB_USER, ActRetrieveRewardRecord.class, sql1);
        if (rewardRecords != null && rewardRecords.size() > 0) {
			for (ActRetrieveRewardRecord record : rewardRecords) {
				int activityId = record.getOperateActId();
				Map<Integer, ActRetrieveRewardRecord> recordMap = retrieveRewardMap.get(activityId);
				if (recordMap == null) {
					recordMap = new HashMap<Integer, ActRetrieveRewardRecord>();
					retrieveRewardMap.put(activityId, recordMap);
				}
				recordMap.put(record.getRewardId(), record);				
			}
		}
    	
    	preDailyRecord = DBUtil.queryBean(DBUtil.DB_USER, PreDailyRecord.class,
                "select * from predailyrecord where roleid = " + id());
	    if (preDailyRecord == null) {
	       preDailyRecord = new PreDailyRecord(id() , RetrieveRewardManager.getDefaultPreDailyRecordMap() , new Date().getTime());
	       this.context().insert(preDailyRecord);
        }  
    }   
	
    @Override
	public void onInit(boolean isCreation){
		int initActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_RetrieveReward);
		opOnInit(initActivityId);
		
		checkActivityData();
		
		//标记需要计算红点
	    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
	}
    
    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
    	if (curActivity != null) {
			curActivity.reset();
			curActivity.sendRewards();
		}	
    	
    	//标记需要计算红点
	    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
    }
   
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
    	OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
        if (redPointIds.contains(Integer.valueOf(RedPointConst.RETRIEVE_REWARD))) {
            if (curActivity != null && curActivity.hasReward() && operateActivityModule.isShow(curActivity.getId())) {
            	redPointMap.put(RedPointConst.RETRIEVE_REWARD, "");
			}else{
				redPointMap.put(RedPointConst.RETRIEVE_REWARD, null);
			}                  
        }
    }
    
    @Override
	public int getCurShowActivityId() {
    	int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_RetrieveReward);
    	if (curActivityId != -1) {
    		OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
			OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
			if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap())
					&& getIsOpenAnyRetrieveDailyYesterday()) {
				return curActivityId;
			}
		}
    	
    	return -1;
	}
    
    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_RetrieveReward);
        if (curActivityId == -1) return (byte)0;
        
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte)0;
        
        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte)0;
        
        if (labelDisappearBase instanceof NeverDisappear) {
			return (byte)1;
		}else if(labelDisappearBase instanceof DisappearByDays){
            int openServerDays = DataManager.getServerDays();
            int continueDays = openServerDays;
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
					com.stars.util.LogUtil.info("RetrieveRewardModule.opOnInit get no OperateActVo,operateActId=" + initActivityId);
					return;
				}				
				curActivity = new RetrieveRewardActivity(initActivityId, this);
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
				LogUtil.info("RetrieveRewardModule.opOnOpen get no OperateActVo,operateActId=" + openActivityId);
				return;
			}				
			curActivity = new RetrieveRewardActivity(openActivityId, this);
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
		
		//活动奖励数据
		Iterator<Map.Entry<Integer , Map<Integer, ActRetrieveRewardRecord>>> it = retrieveRewardMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer , Map<Integer, ActRetrieveRewardRecord>> entry = it.next();
			int activityId = entry.getKey();
			if (activityId != curActivityId) {
				Map<Integer, ActRetrieveRewardRecord> records = entry.getValue();
				for (ActRetrieveRewardRecord record : records.values()) {
					context().delete(record);
				}
				
				it.remove();
			}
		}
	}
	
	public void handleOperateActivityEvent(OperateActivityEvent event){
		int opType = event.getActivityType();
		if (opType == OperateActivityConstant.ActType_RetrieveReward) {
			byte flag = event.getFlag();
			int activityId = event.getActivityId();
			if (flag == OperateActivityEvent.Flag_Open_Activity) {
				opOnOpen(activityId);
				//标记需要计算红点
			    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
			}else if (flag == OperateActivityEvent.Flag_Close_Activity) {
				opOnClose(activityId);
				//标记需要计算红点
			    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
			}
		}
	}
	
	public void handlePreDailyRecordReset(PreDailyRecordResetEvent event){
		Map<Short, Integer> tempMap = event.getRecordMap();
    	Map<Short, Integer> preDailyRecordMap = new HashMap<Short, Integer>();
    	
    	ForeShowModule foreShowModule = (ForeShowModule)module(MConst.ForeShow);
    	
    	//int difDays = DateUtil.getRelativeDifferDays(new Date(preDailyRecord.getLastResetTimeStamp()), new Date());
    	
    	Date lastResetDate = new Date(preDailyRecord.getLastResetTimeStamp());
    	Date nowDate = new Date();
    	int difDays = DateUtil.getRelativeDifferDays(lastResetDate, nowDate);
    	int resetClock = RetrieveRewardConstant.resetClock;
    	
    	Calendar calendar = Calendar.getInstance();
		calendar.setTime(lastResetDate);
		int hour1 = calendar.get(Calendar.HOUR_OF_DAY);
		calendar.setTime(nowDate);
		int hour2 = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour1 < resetClock) {
			difDays = difDays + 1;
		}
		if (hour2 < resetClock) {
			difDays = difDays - 1;
		}
    	
    	Map<Short, DailyVo> dailyVos = DailyManager.getDailyVoMap();
    	for (DailyVo dailyVo : dailyVos.values()) {
			short dailyId = dailyVo.getDailyid();
			int doneCount = -1;
			String systemName = dailyVo.getOpenName();//"DailyWindow_" + dailyId;
			if (foreShowModule.isOpen(systemName)) {				
				doneCount = 0;				
				if (difDays >= 2) {
					doneCount = 0;
				}else if (tempMap.containsKey(dailyId)) {
					doneCount = tempMap.get(dailyId);
				}
			}
			
			preDailyRecordMap.put(dailyId, doneCount);
		}
		
		
		preDailyRecord.setRecordMap(preDailyRecordMap);
		preDailyRecord.setLastResetTimeStamp(new Date().getTime());
		context().update(preDailyRecord);
		
		if (curActivity != null) {
			curActivity.sendRewards();
			
			//标记需要计算红点
		    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
		}	
	}
	
	public void handleRoleLevelUp(){
		//标记需要计算红点
	    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
	}
	
	public void handleForeShowChange(){
		//标记需要计算红点
	    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
	}
	
	public void sendRewards(int activityId){
		if (curActivity == null || curActivity.getId() != activityId) {
			warn("Activity not on");
			return;
		}
		
		curActivity.sendRewards();
		
		//标记需要计算红点
	    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
	}
		
	public void getReward(int activityId , int rewardId){
		if (curActivity == null || curActivity.getId() != activityId) {
			warn("Activity not on");
			return;
		}
			
		curActivity.getReward(rewardId);
		
		//标记需要计算红点
	    signCalRedPoint(MConst.RetrieveReward, RedPointConst.RETRIEVE_REWARD);
	}
	
	public Map<String, Module> getModulMap(){
		return moduleMap();
	}
	
	/**
	* 操作数据的方法
	*/
	public Map<Integer, ActRetrieveRewardRecord> getRewardRecords(int activityId){
		return retrieveRewardMap.get(activityId);
	}
	
	public PreDailyRecord getPreDailyRecord(){
		return preDailyRecord;
	}
	
	public boolean getIsOpenAnyRetrieveDailyYesterday(){
		Map<Integer, RetrieveRewardVo> retrieveRewardVos = RetrieveRewardManager.getRetrieveRewardVoMap();
		if (retrieveRewardVos != null) {
			for (RetrieveRewardVo vo : retrieveRewardVos.values()) {
				short dailyId = vo.getDailyid();
				if (getIsYesterdayOpen(dailyId)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean getIsYesterdayOpen(short dailyId){
		Map<Short, Integer> recordMap = preDailyRecord.getRecordMap();
		if (recordMap != null && recordMap.containsKey(dailyId)) {
			int preCount = recordMap.get(dailyId);
			if (preCount != -1) {
				return true;
			}
		}
			
		return false;		
	}
	
	public void insertData(DbRow dbRow){
		if (dbRow instanceof ActRetrieveRewardRecord) {
			ActRetrieveRewardRecord record = (ActRetrieveRewardRecord)dbRow;
			int activityId = record.getOperateActId();
			Map<Integer, ActRetrieveRewardRecord> recordMap = retrieveRewardMap.get(activityId);
			if (recordMap == null) {
				recordMap = new HashMap<Integer, ActRetrieveRewardRecord>();
				retrieveRewardMap.put(activityId, recordMap);
			}
			recordMap.put(record.getRewardId(), record);
			
			context().insert(dbRow);
		}
	}
	
	public void updateData(DbRow dbRow){
		context().update(dbRow);
	}
}

