package com.stars.modules.newserversign;

import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.newserversign.packet.ClientNewServerSign;
import com.stars.modules.newserversign.prodata.NewServerSignVo;
import com.stars.modules.newserversign.userdata.ActSignRewardRecord;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.ActOpenTime4;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolModule;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by gaopeidian on 2016/12/15.
 */
public class NewServerSignActivity {
	private int activityId;
	private NewServerSignModule module;
	
	//用户数据
	private Map<Integer, ActSignRewardRecord> signRewardsMap = null;
	
	public NewServerSignActivity(int activityId , NewServerSignModule module){
		this.activityId = activityId;
		this.module = module;
		initUserData();
	}
	
	private void initUserData(){
		signRewardsMap = module.getRewardRecords(this.activityId);
		if (signRewardsMap == null) {
			signRewardsMap = new HashMap<Integer, ActSignRewardRecord>();
		}
	}
	
	public void reset(){
		for (ActSignRewardRecord actSignRewardRecord : signRewardsMap.values()) {
			actSignRewardRecord.setIsGot((byte)0);
			module.updateData(actSignRewardRecord);
		}   
	}
	
	public void close(){

	}
	
	public void getRewardsInfo(){
		Map<Integer, NewServerSignVo> activityVos = NewServerSignManager.getActivityVosMap(activityId);
		if (activityVos == null) {
			module.warn("Get_no_sign_reward_product_data");
			return;
		}
		
		int openDays = getOpenDays();
		
		Map<Integer, NewServerSignVo> signRewardVosMap = new HashMap<Integer, NewServerSignVo>();
		Map<Integer, Byte> signRewardStatusMap = new HashMap<Integer, Byte>();
		
		for (NewServerSignVo vo : activityVos.values()) {
			int signRewardId = vo.getNewServerSignId();
			
			signRewardVosMap.put(signRewardId, vo);
			
			byte status = ActSignRewardRecord.Reward_Status_Cannot_get;
			ActSignRewardRecord record = getActSignRewardRecord(signRewardId);
			if (record.getIsGot() == (byte)1) {
				status = ActSignRewardRecord.Reward_Status_Have_Got;
			}else{
				int days = vo.getDays();
				if (days < openDays) {
					status = ActSignRewardRecord.Reward_Status_Out_Of_Date;
				}else if (days == openDays) {
					status = ActSignRewardRecord.Reward_Status_Can_get;
				}else if (days > openDays) {
					status = ActSignRewardRecord.Reward_Status_Cannot_get;
				}
			}
			
			signRewardStatusMap.put(signRewardId, status);
		}
		
		Date startDate = new Date();
		Date endDate = new Date();
		OperateActVo vo = OperateActivityManager.getOperateActVo(activityId);
		if (vo != null) {
			ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
			if ((openTimeBase != null) && (openTimeBase instanceof ActOpenTime4)) {
				ActOpenTime4 time = (ActOpenTime4)openTimeBase;
				RoleModule roleModule = (RoleModule)module.getModulMap().get(MConst.Role);
				Date createDate = DateUtil.toDate(roleModule.getRoleCreatedTime());
				startDate = ActOpenTime4.getStartDate(time, createDate);
				endDate = ActOpenTime4.getEndDate(time, createDate);
			}
		}
		
		ClientNewServerSign clientNewServerSign = new ClientNewServerSign();
		clientNewServerSign.setFlag(ClientNewServerSign.Flag_Get_Reward_Info);
		clientNewServerSign.setRewardsVoMap(signRewardVosMap);
		clientNewServerSign.setRewardsStatusMap(signRewardStatusMap);
		clientNewServerSign.setCostMap(OperateActivityManager.operateActResignCost);
		clientNewServerSign.setStartTimeStamp(startDate.getTime());
		clientNewServerSign.setEndTimeStamp(endDate.getTime());
		clientNewServerSign.setOpenDay(getOpenDays());
		module.send(clientNewServerSign);
	}
	
	public int getReward(int newServerSignId){
		NewServerSignVo vo = NewServerSignManager.getNewServerSignVo(newServerSignId);
		if (vo == null) {
			module.warn("获取不到配置数据");
			return -1;
		}
		
		ActSignRewardRecord record = getActSignRewardRecord(newServerSignId);
		if (record.getIsGot() == (byte)1){//已领取
			module.warn("奖励已领取");
			return -1;
		}
		
		int openDays = getOpenDays();		
		int days = vo.getDays();
		
		if (days > openDays) {//时间未到，不能领取
			module.warn("还不能领取");
			return -1;
		}
		
		ToolModule toolModule = (ToolModule)module.getModulMap().get(MConst.Tool);
		if (days < openDays) {//已过期，需要扣补签的消耗
			Map<Integer, Integer> costMap = OperateActivityManager.operateActResignCost;
			if (!toolModule.deleteAndSend(costMap, EventType.NEWSERVERSIGN.getCode())) {
				module.warn("道具不足");
				return -1;
			}
		}
		
		DropModule dropModule = (DropModule)module.getModulMap().get(MConst.Drop);
		Map<Integer, Integer> rewardMap = dropModule.executeDrop(vo.getReward(), 1,true);
		Map<Integer, Integer> realGetMap = toolModule.addAndSend(rewardMap, NewServerSignConstant.newServerSignCSConst, EventType.NEWSERVERSIGN.getCode());
		record.setIsGot((byte)1);
		module.updateData(record);

		ClientNewServerSign clientNewServerSign = new ClientNewServerSign();
		clientNewServerSign.setFlag(ClientNewServerSign.Flag_Update_Reward_Status);
		clientNewServerSign.setNewServerSignId(newServerSignId);
		clientNewServerSign.setStatus(ActSignRewardRecord.Reward_Status_Have_Got);
		clientNewServerSign.setDisplayAward(realGetMap);
		clientNewServerSign.setOpenDay(getOpenDays());
		module.send(clientNewServerSign);
		
		printDynamicLog(days, days < openDays ? (byte)2 : (byte)1 , openDays);
		
		return days;
	}
	
	public int getId(){
		return activityId;
	}
	
	ActSignRewardRecord getActSignRewardRecord(int newServerSignId){
		ActSignRewardRecord record = signRewardsMap.get(newServerSignId);
		if (record == null) {
			record = new ActSignRewardRecord(module.id() , activityId , newServerSignId , (byte)0);
			signRewardsMap.put(newServerSignId, record);
			module.insertData(record);
		}
		
		return record;
	}
	
//	public int getOpenDays(){
//		OperateActVo actVo = OperateActivityManager.getOperateActVo(activityId);
//		if (actVo == null) {
//			LogUtil.info("NewServerSignActivity.getOpenDays get no OperateActVo,activityId=" + activityId);
//			return 0;
//		}
//		
//		ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
//		if (openTimeBase.getOpenTimeType() != ActOpenTimeBase.OpenTimeType1) {
//			LogUtil.info("NewServerSignActivity.getOpenDays time no suitable,activityId=" + activityId);
//			return DataManager.getServerDays();//若无配置，则返回开服时间
//		}
//		
//		ActOpenTime1 openTime1 = (ActOpenTime1)openTimeBase;
//		Date startDate = openTime1.getStartDate();
//		Date nowDate = new Date();
//		
//		int differDays = DateUtil.getRelativeDifferDays(startDate, nowDate);
//		int openDays = differDays + 1;
//		return openDays;
//	}
	
	public int getOpenDays(){
		OperateActVo actVo = OperateActivityManager.getOperateActVo(activityId);
		if (actVo == null) {
			com.stars.util.LogUtil.info("NewServerSignActivity.getOpenDays get no OperateActVo,activityId=" + activityId);
			return 0;
		}
		
		ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
		if (!(openTimeBase instanceof ActOpenTime4)) {
			LogUtil.info("NewServerSignActivity.getOpenDays time no suitable,activityId=" + activityId);
			return 0;
		}
		
		ActOpenTime4 openTime4 = (ActOpenTime4)openTimeBase;
		RoleModule roleModule = (RoleModule)module.getModulMap().get(MConst.Role);
		int createRoleDays = roleModule.getRoleCreatedDays();
		int openDays = ActOpenTime4.getOpenDays(openTime4, createRoleDays);
		
		return openDays;
	}
	
	/**
	 * 是否有可领取的奖励
	 * @return 
	 */
	public boolean hasReward(){
		if (!module.isEffectiveTime(activityId)) {
			return false;
		}
		
		Map<Integer, NewServerSignVo> activityVos = NewServerSignManager.getActivityVosMap(activityId);
		if (activityVos != null) {
			int openDays = getOpenDays();
			for (NewServerSignVo vo : activityVos.values()) {
				int signRewardId = vo.getNewServerSignId();
				byte status = ActSignRewardRecord.Reward_Status_Cannot_get;
				ActSignRewardRecord record = getActSignRewardRecord(signRewardId);
				if (record.getIsGot() == (byte)1) {
					status = ActSignRewardRecord.Reward_Status_Have_Got;
				}else{
					int days = vo.getDays();
					if (days < openDays) {
						status = ActSignRewardRecord.Reward_Status_Out_Of_Date;
					}else if (days == openDays) {
						status = ActSignRewardRecord.Reward_Status_Can_get;
					}else if (days > openDays) {
						status = ActSignRewardRecord.Reward_Status_Cannot_get;
					}
				}
				
				if (status == ActSignRewardRecord.Reward_Status_Can_get) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 是否有可补签的奖励
	 * @return 
	 */
	public boolean hasReGetReward(){
		if (!module.isEffectiveTime(activityId)) {
			return false;
		}
		
		Map<Integer, NewServerSignVo> activityVos = NewServerSignManager.getActivityVosMap(activityId);
		if (activityVos != null) {
			int openDays = getOpenDays();
			for (NewServerSignVo vo : activityVos.values()) {
				int signRewardId = vo.getNewServerSignId();
				byte status = ActSignRewardRecord.Reward_Status_Cannot_get;
				ActSignRewardRecord record = getActSignRewardRecord(signRewardId);
				if (record.getIsGot() == (byte)1) {
					status = ActSignRewardRecord.Reward_Status_Have_Got;
				}else{
					int days = vo.getDays();
					if (days < openDays) {
						status = ActSignRewardRecord.Reward_Status_Out_Of_Date;
					}else if (days == openDays) {
						status = ActSignRewardRecord.Reward_Status_Can_get;
					}else if (days > openDays) {
						status = ActSignRewardRecord.Reward_Status_Cannot_get;
					}
				}
				
				if (status == ActSignRewardRecord.Reward_Status_Out_Of_Date) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public byte getSignRewardStatus(int signRewardId , int openDays){
		NewServerSignVo vo = NewServerSignManager.getNewServerSignVo(signRewardId);
		if (vo == null) {
			return ActSignRewardRecord.Reward_Status_Cannot_get;
		}
		
		byte status = ActSignRewardRecord.Reward_Status_Cannot_get;
		ActSignRewardRecord record = getActSignRewardRecord(signRewardId);
		if (record.getIsGot() == (byte)1) {
			status = ActSignRewardRecord.Reward_Status_Have_Got;
		}else{
			int days = vo.getDays();
			if (days < openDays) {
				status = ActSignRewardRecord.Reward_Status_Out_Of_Date;
			}else if (days == openDays) {
				status = ActSignRewardRecord.Reward_Status_Can_get;
			}else if (days > openDays) {
				status = ActSignRewardRecord.Reward_Status_Cannot_get;
			}
		}
		
		return status;
	}
	
	public void printDynamicLog(int signDay , byte signType , int nowDay){
		ServerLogModule serverLogModule = (ServerLogModule)module.getModulMap().get(MConst.ServerLog);
		
		//sign type log
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("number@sign_type:");
		stringBuffer.append(signDay);
		stringBuffer.append("@");
		stringBuffer.append(signType);
		
		stringBuffer.append("#");
		
		//sign stat log
		stringBuffer.append("number@sign_stat:");
		Map<Integer, NewServerSignVo> activityVoMap = NewServerSignManager.getActivityVosMap(getId());
		if (activityVoMap != null) {
			List<NewServerSignVo> voList = new ArrayList<NewServerSignVo>(activityVoMap.values());
			Collections.sort(voList);
			
			for (NewServerSignVo vo : voList) {
				int day = vo.getDays();
				byte status = getSignRewardStatus(vo.getNewServerSignId(), nowDay);
				byte logStatus = 0;
		        if (status == ActSignRewardRecord.Reward_Status_Have_Got) {
					logStatus = 1;
				}				
		        stringBuffer.append(day);
		        stringBuffer.append("@");
		        stringBuffer.append(logStatus);
		        stringBuffer.append("&");
			}
		}
		
		serverLogModule.dynamic_4_Log(ThemeType.DYNAMIC_NEW_SERVER_SIGN.getThemeId(), "sign", stringBuffer.toString(), "0");
	}
}
