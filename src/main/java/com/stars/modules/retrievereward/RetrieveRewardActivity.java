package com.stars.modules.retrievereward;

import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.retrievereward.packet.ClientRetrieveReward;
import com.stars.modules.retrievereward.prodata.RetrieveRewardVo;
import com.stars.modules.retrievereward.userdata.ActRetrieveRewardRecord;
import com.stars.modules.retrievereward.userdata.PreDailyRecord;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaopeidian on 2016/12/15.
 */
public class RetrieveRewardActivity {
	private int activityId;
	private RetrieveRewardModule module;
	
	//用户数据
	private Map<Integer, ActRetrieveRewardRecord> retrieveRewardMap = null;
	
	public RetrieveRewardActivity(int activityId , RetrieveRewardModule module){
		this.activityId = activityId;
		this.module = module;
		initUserData();
	}
	
	private void initUserData(){
		retrieveRewardMap = module.getRewardRecords(this.activityId);
		if (retrieveRewardMap == null) {
			retrieveRewardMap = new HashMap<Integer, ActRetrieveRewardRecord>();
		}
	}
	
	public void reset(){
		for (ActRetrieveRewardRecord actRetrieveRewardRecord : retrieveRewardMap.values()) {
			actRetrieveRewardRecord.setRevieveCount(0);
			module.updateData(actRetrieveRewardRecord);
		}    
	}
	
	public void close(){

	}
	
	public void sendRewards(){
		Map<Integer, Integer> rewards = new HashMap<Integer, Integer>();
		Map<Integer, Byte> rewardStatus = new HashMap<Integer, Byte>();
		
		Map<Integer, RetrieveRewardVo> retrieveRewardVos = RetrieveRewardManager.getRetrieveRewardVoMap();
		if (retrieveRewardVos != null) {
			for (RetrieveRewardVo vo : retrieveRewardVos.values()) {
				int rewardId = vo.getRetrieveRewardId();
				
				if (!module.getIsYesterdayOpen(vo.getDailyid())) {
					continue;
				}				
				rewards.put(rewardId, getLeftCount(rewardId));
				rewardStatus.put(rewardId, getIsCanGet(rewardId));
			}
		}
		
		ClientRetrieveReward clientRetrieveReward = new ClientRetrieveReward();
		clientRetrieveReward.setFlag(ClientRetrieveReward.Flag_Reward_Info);
		clientRetrieveReward.setRetrieveRewardMap(rewards);
		clientRetrieveReward.setRetrieveRewardStatusMap(rewardStatus);
		module.send(clientRetrieveReward);
	}
	
	public void getReward(int rewardId){
		RetrieveRewardVo vo = RetrieveRewardManager.getRetrieveRewardVo(rewardId);
		if (vo == null) {
			module.warn("获取不到改奖励的产品数据");
			return;
		}
	
		if (!module.getIsYesterdayOpen(vo.getDailyid())) {
			module.warn("昨日未开放，不可领取");
			return;
		}	
		
		if (getIsCanGet(rewardId) != 1) {
			module.warn("不可领取");
			return;
		}	
		
		int leftCount = getLeftCount(rewardId);
		if (leftCount <= 0) {
			module.warn("无可找回次数");
			return;
		}
		
		ToolModule toolModule = (ToolModule)(module.getModulMap().get(MConst.Tool));
		
		Map<Integer, Integer> costMap = vo.getCostMap();		
		Map<Integer, Integer> finalCost = new HashMap<Integer, Integer>();
		Set<Map.Entry<Integer, Integer>> entrySet0 = costMap.entrySet();
		for (Map.Entry<Integer, Integer> entry0 : entrySet0) {
			finalCost.put(entry0.getKey(), entry0.getValue() * leftCount);
		}	
		if (!toolModule.deleteAndSend(finalCost , EventType.RETRIEVEREWARD.getCode())) {
			module.warn("Tool not enough");
			return;
		}
		
		DropModule dropModule = (DropModule)(module.getModulMap().get(MConst.Drop));
		Map<Integer, Integer> reward = dropModule.executeDrop(vo.getGroupId() , 1 , true);
		Map<Integer, Integer> finalReward = new HashMap<Integer, Integer>();
		Set<Map.Entry<Integer, Integer>> entrySet = reward.entrySet();
		for (Map.Entry<Integer, Integer> entry : entrySet) {
			finalReward.put(entry.getKey(), entry.getValue() * leftCount);
		}
		//toolModule.addAndSend(finalReward , RetrieveRewardConstant.retriveRewardCSConst , EventType.RETRIEVEREWARD.getCode());
		Map<Integer, Integer> getReward = toolModule.addAndSend(finalReward , EventType.RETRIEVEREWARD.getCode());
		//发获奖提示到客户端
		ClientAward clientAward = new ClientAward(getReward);
		module.send(clientAward);
		
		//更新奖励记录数据并下发到客户端
        Map<Integer, Integer> updateRewards = new HashMap<Integer, Integer>();
        Map<Integer, Byte> rewardStatus = new HashMap<Integer, Byte>();
		
		Map<Integer, RetrieveRewardVo> rewardVoMap = RetrieveRewardManager.getRetrieveRewardVoMap();
		for (RetrieveRewardVo tempVo : rewardVoMap.values()) {
			if (tempVo.getDailyid() == vo.getDailyid()) {
				int temprewardId = tempVo.getRetrieveRewardId();
				ActRetrieveRewardRecord tempRecord = getActRetrieveRewardRecord(temprewardId);
				tempRecord.setRevieveCount(tempRecord.getRevieveCount() + leftCount);
				module.updateData(tempRecord);
				
				if (!module.getIsYesterdayOpen(tempVo.getDailyid())) {
					continue;
				}				
				updateRewards.put(temprewardId, getLeftCount(temprewardId));
				rewardStatus.put(temprewardId, getIsCanGet(temprewardId));
			}
		}
        
		ClientRetrieveReward clientRetrieveReward = new ClientRetrieveReward();
		clientRetrieveReward.setFlag(ClientRetrieveReward.Flag_Get_Reward);
		clientRetrieveReward.setUpdateRewardMap(updateRewards);
		clientRetrieveReward.setRetrieveRewardStatusMap(rewardStatus);
		module.send(clientRetrieveReward);	
	
	}
	
	ActRetrieveRewardRecord getActRetrieveRewardRecord(int rewardId){
		ActRetrieveRewardRecord record = retrieveRewardMap.get(rewardId);
		if (record == null) {
			record = new ActRetrieveRewardRecord(module.id() , activityId , rewardId , 0);
			retrieveRewardMap.put(rewardId, record);
			module.insertData(record);
		}
		
		return record;
	}
	
	public int getLeftCount(int rewardId){
		RetrieveRewardVo vo = RetrieveRewardManager.getRetrieveRewardVo(rewardId);
		if (vo == null) {
			return 0;
		}
		
		ActRetrieveRewardRecord record = getActRetrieveRewardRecord(rewardId);	
		PreDailyRecord preDailyRecord = module.getPreDailyRecord();
		int yesCount = preDailyRecord.getPreDailyRecord(vo.getDailyid());
		if (yesCount == -1) {
			return 0;
		}
		
		int maxCount = vo.getCount();				
		int retrieveCount = record.getRevieveCount();				
		int leftCount = maxCount - yesCount - retrieveCount;
		
		return leftCount >= 0 ? leftCount : 0;	
	}
	
	public byte getIsCanGet(int rewardId){
		RetrieveRewardVo vo = RetrieveRewardManager.getRetrieveRewardVo(rewardId);
		if (vo == null) {
			return 0;
		}
		PreDailyRecord preDailyRecord = module.getPreDailyRecord();
		int yesCount = preDailyRecord.getPreDailyRecord(vo.getDailyid());
		if (yesCount == -1) {
			return 0;
		}
		
		int maxCount = vo.getCount();				
		if (yesCount >= maxCount) {
			return 0;
		}else{
			return 1;
		}
	}
	
	public int getId(){
		return activityId;
	}
	
	/**
	 * 是否有可领取的奖励
	 * @return 
	 */
	public boolean hasReward(){
		PreDailyRecord preDailyRecord = module.getPreDailyRecord();
		Map<Integer, RetrieveRewardVo> retrieveRewardVos = RetrieveRewardManager.getRetrieveRewardVoMap();
		if (retrieveRewardVos != null) {
			for (RetrieveRewardVo vo : retrieveRewardVos.values()) {
				int rewardId = vo.getRetrieveRewardId();
				
				ActRetrieveRewardRecord record = getActRetrieveRewardRecord(rewardId);	
				int yesCount = preDailyRecord.getPreDailyRecord(vo.getDailyid());
				if (yesCount == -1) {
					continue;
				}
				
				int maxCount = vo.getCount();				
				int retrieveCount = record.getRevieveCount();				
				int leftCount = maxCount - yesCount - retrieveCount;
								
				if (leftCount > 0) {
					return true;
				}
			}
		}
		
		return false;
	}
}
