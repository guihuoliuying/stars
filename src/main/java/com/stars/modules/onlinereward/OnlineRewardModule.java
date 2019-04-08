package com.stars.modules.onlinereward;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.onlinereward.prodata.OnlineRewardVo;
import com.stars.modules.onlinereward.userdata.ActOnlineRewardRecord;
import com.stars.modules.onlinereward.userdata.ActOnlineTime;
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
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class OnlineRewardModule extends AbstractModule implements OpActivityModule {
	/**
	 * 当前正在进行的活动
	 * 为null则当前无正在进行的活动
	 */
    private OnlineRewardActivity curActivity = null;
	
    //所有活动的用户数据
    /**
	 * 活动奖励数据
	 * <活动id，<奖励id，奖励记录>>
	 */
	private Map<Integer , Map<Integer, ActOnlineRewardRecord>> onlineRewardMap = new HashMap<Integer , Map<Integer, ActOnlineRewardRecord>>();
	/**
	 * 活动在线数据
	 * <活动id，在线记录>
	 */  
	private Map<Integer, ActOnlineTime> onlineTimesMap = new HashMap<Integer, ActOnlineTime>();

	public OnlineRewardModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super(MConst.OnlineReward, id, self, eventDispatcher, moduleMap);
	}
	
	@Override
    public void onCreation(String name_, String account_) throws Throwable {
    	//这里创建角色的时候不需要做任何加载，因为还没有任何活动数据
    }

    @Override
    public void onDataReq() throws Exception {
        //加载用户数据
    	String sql1 = "select * from `actonlinerewardrecord` where `roleid`=" + id();
        List<ActOnlineRewardRecord> rewardRecords = DBUtil.queryList(DBUtil.DB_USER, ActOnlineRewardRecord.class, sql1);
        if (rewardRecords != null && rewardRecords.size() > 0) {
			for (ActOnlineRewardRecord record : rewardRecords) {
				int activityId = record.getOperateActId();
				Map<Integer, ActOnlineRewardRecord> recordMap = onlineRewardMap.get(activityId);
				if (recordMap == null) {
					recordMap = new HashMap<Integer, ActOnlineRewardRecord>();
					onlineRewardMap.put(activityId, recordMap);
				}
				recordMap.put(record.getRewardId(), record);				
			}
		}
        
        String sql2 = "select * from `actonlinetime` where `roleid`=" + id();
        Map<Integer, ActOnlineTime> tempOnlineTimeMap = DBUtil.queryMap(DBUtil.DB_USER, "operateActId", ActOnlineTime.class, sql2);
        if (tempOnlineTimeMap != null) {
        	onlineTimesMap = tempOnlineTimeMap;
		}		
    }
    
	@Override
	public void onInit(boolean isCreation){
		int initActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_OnlineReward);
		opOnInit(initActivityId);
		
		checkActivityData();
		
		//标记需要计算红点
	    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
	}

	@Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
		if (curActivity != null) {
			curActivity.reset();
			curActivity.sendRewards();
		}	
		
		sendCountDownTime();
		//标记需要计算红点
	    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
    }
	
	@Override
	public void onReconnect() throws Throwable {

		int initActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_OnlineReward);
		opOnInit(initActivityId);

		online();
		//标记需要计算红点
		signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
	}

	@Override
	public void onSyncData() {
		if (curActivity != null) {
			sendCountDownTime();
		}
	} 
	
	@Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
		OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
        if (redPointIds.contains(Integer.valueOf(RedPointConst.ONLINE_REWARD))) {
            if (curActivity != null && curActivity.hasReward() && operateActivityModule.isShow(curActivity.getId())) {
            	redPointMap.put(RedPointConst.ONLINE_REWARD, "");
			}else{
				redPointMap.put(RedPointConst.ONLINE_REWARD, null);
			}                   
        }
    }
	
	@Override
    public void onOffline() {
		if (curActivity != null) {
			curActivity.offline();
		}
    }
	
	@Override
	public int getCurShowActivityId() {
		int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_OnlineReward);
    	if (curActivityId != -1) {
    		OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
			OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
			if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap())) {
				return curActivityId;
			}
		}
    	
    	return -1;
	}
	
    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_OnlineReward);
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
					com.stars.util.LogUtil.info("OnlineRewardModule.opOnInit get no OperateActVo,operateActId=" + initActivityId);
					return;
				}				
				curActivity = new OnlineRewardActivity(initActivityId, this);
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
				LogUtil.info("OnlineRewardModule.opOnOpen get no OperateActVo,operateActId=" + openActivityId);
				return;
			}				
			curActivity = new OnlineRewardActivity(openActivityId, this);
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
		Iterator<Map.Entry<Integer , Map<Integer, ActOnlineRewardRecord>>> it = onlineRewardMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer , Map<Integer, ActOnlineRewardRecord>> entry = it.next();
			int activityId = entry.getKey();
			if (activityId != curActivityId) {
				Map<Integer, ActOnlineRewardRecord> records = entry.getValue();
				for (ActOnlineRewardRecord record : records.values()) {
					context().delete(record);
				}
				
				it.remove();
			}
		}
			
		//活动在线数据
		Iterator<Map.Entry<Integer, ActOnlineTime>> it2 = onlineTimesMap.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry<Integer, ActOnlineTime> entry = it2.next();
			int activityId = entry.getKey();
			if (activityId != curActivityId) {
				ActOnlineTime onlineTime = entry.getValue();
				context().delete(onlineTime);
				
				it2.remove();
			}
		}
	}
	
	public void handleLoginSuccess(){
		online();
	}
	
	public void handleOperateActivityEvent(OperateActivityEvent event){
		int opType = event.getActivityType();
		if (opType == OperateActivityConstant.ActType_OnlineReward) {
			byte flag = event.getFlag();
			int activityId = event.getActivityId();
			if (flag == OperateActivityEvent.Flag_Open_Activity) {
				opOnOpen(activityId);
				sendCountDownTime();
				//标记需要计算红点
			    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
			}else if (flag == OperateActivityEvent.Flag_Close_Activity) {
				opOnClose(activityId);
				sendCountDownTime();
				//标记需要计算红点
			    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
			}
		}
	}
	
	public void online(){
		if (curActivity != null) {
			curActivity.online();
		}
	}
		
	public void handleRoleLevelUp(){
		sendCountDownTime();	
		
		//标记需要计算红点
	    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
	}
	
	public void handleForeShowChange(){
		sendCountDownTime();	
		
		//标记需要计算红点
	    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
	}
	
	public void sendRewards(int activityId){
		if (curActivity == null || curActivity.getId() != activityId) {
			warn("Activity not on");
			return;
		}
		
		curActivity.sendRewards();
		
		//标记需要计算红点
	    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
	}
	
	public void sendReward(int activityId , int rewardId){
		if (curActivity == null || curActivity.getId() != activityId) {
			warn("Activity not on");
			return;
		}
		
		curActivity.sendReward(rewardId);
		
		//标记需要计算红点
	    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
	}
	
	public void getReward(int activityId , int rewardId){
		if (curActivity == null || curActivity.getId() != activityId) {
			warn("Activity not on");
			return;
		}
		
		curActivity.getReward(rewardId);
		
		//标记需要计算红点
	    signCalRedPoint(MConst.OnlineReward, RedPointConst.ONLINE_REWARD);
	}
		
	/**
	 * 当前无可领取奖励且存在倒计时时，发送最近一个到达倒计时的剩余时间
	 */
	public void sendCountDownTime(){
		OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
		if (curActivity != null && getCurShowActivityId() == curActivity.getId() && operateActivityModule.isShow(curActivity.getId())) {
			curActivity.sendCountDownTime();
		}
	}
	
	public Map<String, Module> getModulMap(){
		return moduleMap();
	}
	
	/**
	* 操作数据的方法
	*/
	public Map<Integer, ActOnlineRewardRecord> getRewardRecords(int activityId){
		return onlineRewardMap.get(activityId);
	}
	
	public ActOnlineTime getOnlineTime(int activityId){
		return onlineTimesMap.get(activityId);
	}
	
	public void insertData(DbRow dbRow){
		if (dbRow instanceof ActOnlineRewardRecord) {
			ActOnlineRewardRecord record = (ActOnlineRewardRecord)dbRow;
			int activityId = record.getOperateActId();
			Map<Integer, ActOnlineRewardRecord> recordMap = onlineRewardMap.get(activityId);
			if (recordMap == null) {
				recordMap = new HashMap<Integer, ActOnlineRewardRecord>();
				onlineRewardMap.put(activityId, recordMap);
			}
			recordMap.put(record.getRewardId(), record);
			
			context().insert(dbRow);
		}else if (dbRow instanceof ActOnlineTime) {
			ActOnlineTime onlineTime = (ActOnlineTime)dbRow;
			onlineTimesMap.put(onlineTime.getOperateActId(), onlineTime);
			
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
		
		StringBuffer stringBuffer = new StringBuffer();
		
		//在线时长
		int onlineTimeSecond = curActivity.getOnlineTimeSecond();
		stringBuffer.append("online:");
		stringBuffer.append(onlineTimeSecond);
		stringBuffer.append("#");
		
		//奖励状态
		stringBuffer.append("stall@reward:");
		Map<Integer, OnlineRewardVo> onlineRewardVos = OnlineRewardManager.getOnlineRewardVoMap();
		if (onlineRewardVos != null) {
			for (OnlineRewardVo vo : onlineRewardVos.values()) {
				byte status = curActivity.getOnlineRewardStatus(vo, onlineTimeSecond);
				int rewardId = vo.getOnlinerewardid();
				int logStatus = status == OnlineRewardConstant.rewardStatusGot ? 1 : 0;
				stringBuffer.append(rewardId);
				stringBuffer.append("@");
				stringBuffer.append(logStatus);
				stringBuffer.append("&");
			}
		}
		stringBuffer.append("#");
		
		return stringBuffer.toString();
	}
}

