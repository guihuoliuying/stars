package com.stars.modules.onlinereward;

import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.onlinereward.packet.ClientOnlineReward;
import com.stars.modules.onlinereward.packet.ClientOnlineRewardCountDown;
import com.stars.modules.onlinereward.prodata.OnlineRewardVo;
import com.stars.modules.onlinereward.userdata.ActOnlineRewardRecord;
import com.stars.modules.onlinereward.userdata.ActOnlineTime;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/15.
 */
public class OnlineRewardActivity {
	private int activityId;
	private OnlineRewardModule module;
	
	//用户数据
	private Map<Integer, ActOnlineRewardRecord> onlineRewardMap = null;
	private ActOnlineTime onlineTime = null;
	
	private long lastOnlineTimestamp = 0;
	
	public OnlineRewardActivity(int activityId , OnlineRewardModule module){
		this.activityId = activityId;
		this.module = module;
		initUserData();
	}
	
	private void initUserData(){
		onlineRewardMap = module.getRewardRecords(this.activityId);
		if (onlineRewardMap == null) {
			onlineRewardMap = new HashMap<Integer, ActOnlineRewardRecord>();
		}
		
		onlineTime = module.getOnlineTime(activityId);
		if (onlineTime == null) {
			onlineTime = new ActOnlineTime(module.id(), this.activityId, 0);
			module.insertData(onlineTime);
		}
		
		lastOnlineTimestamp = new Date().getTime();
	}
	
	public void online(){
		lastOnlineTimestamp = new Date().getTime();
	}
	
	public void offline(){
		if (lastOnlineTimestamp != 0) {
			int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
			onlineTime.setOnlineTime(onlineTime.getOnlineTime() + passSecond);
			module.updateData(onlineTime);
		}
		lastOnlineTimestamp = 0;
	}
	
	public void reset(){
		for (ActOnlineRewardRecord actOnlineRewardRecord : onlineRewardMap.values()) {
			actOnlineRewardRecord.setIsGot((byte)0);
			module.updateData(actOnlineRewardRecord);
		}
		
		onlineTime.setOnlineTime(0);
		module.updateData(onlineTime);
		
		lastOnlineTimestamp = new Date().getTime();	    
	}
	
	public void close(){
		if (lastOnlineTimestamp != 0) {
			int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
			onlineTime.setOnlineTime(onlineTime.getOnlineTime() + passSecond);
			module.updateData(onlineTime);
		}
		
		lastOnlineTimestamp = 0;
	}
	
	public void sendRewards(){
		int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
		int _onLineTime = onlineTime.getOnlineTime() + passSecond;
		
		Map<Integer, Byte> rewards = new HashMap<Integer, Byte>();
		
		Map<Integer, OnlineRewardVo> onlineRewardVos = OnlineRewardManager.getOnlineRewardVoMap();
		if (onlineRewardVos != null) {
			for (OnlineRewardVo vo : onlineRewardVos.values()) {
				byte status = getOnlineRewardStatus(vo, _onLineTime);
				rewards.put(vo.getOnlinerewardid(), status);
			}
		}
		
		ClientOnlineReward clientOnlineReward = new ClientOnlineReward();
		clientOnlineReward.setFlag(ClientOnlineReward.Flag_Reward_Info);
		clientOnlineReward.setOnlineRewardMap(rewards);
		clientOnlineReward.setOnlineTime(_onLineTime);
		module.send(clientOnlineReward);
	}
	
	public void sendReward(int rewardId){
		int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
		int _onLineTime = onlineTime.getOnlineTime() + passSecond;
		
		Map<Integer, Byte> rewards = new HashMap<Integer, Byte>();
		
		OnlineRewardVo vo = OnlineRewardManager.getOnlineRewardVo(rewardId);
		if (vo == null) {
			module.warn("获取不到产品数据");
			return;
		}
		
		byte status = getOnlineRewardStatus(vo, _onLineTime);
		rewards.put(rewardId, status);
		
		ClientOnlineReward clientOnlineReward = new ClientOnlineReward();
		clientOnlineReward.setFlag(ClientOnlineReward.Flag_Reward_Info);
		clientOnlineReward.setOnlineRewardMap(rewards);
		clientOnlineReward.setOnlineTime(_onLineTime);
		module.send(clientOnlineReward);
	}
	
	public void getReward(int rewardId){
		OnlineRewardVo vo = OnlineRewardManager.getOnlineRewardVo(rewardId);
		if (vo == null) {
			module.warn("获取不到产品数据");
			return;
		}
		
		ActOnlineRewardRecord record = onlineRewardMap.get(rewardId);
		if (record == null) {
			record = new ActOnlineRewardRecord(module.id() , activityId , rewardId , (byte)0);
			onlineRewardMap.put(rewardId, record);
			
			module.insertData(record);
		}
		
		if (record.getIsGot() == (byte)1) {
			module.warn("奖励已领取过");
			return;
		}
		
		int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
		int _onLineTime = onlineTime.getOnlineTime() + passSecond;
		int needSecond = vo.getMinute() * 60;
		if (_onLineTime < needSecond) {
			module.warn("在线时间不足");
			return;
		}
			
		DropModule dropModule = (DropModule)(module.getModulMap().get(MConst.Drop));
		Map<Integer, Integer> reward = dropModule.executeDrop(vo.getGroupId() , 1 , true);
		ToolModule toolModule = (ToolModule)(module.getModulMap().get(MConst.Tool));
		Map<Integer, Integer> map = toolModule.addAndSend(reward,EventType.ONLINEAWARD.getCode());
		//发获奖提示到客户端
		ClientAward clientAward = new ClientAward(map);
		module.send(clientAward);
		
		//设置领奖状态
		record.setIsGot((byte)1);
		module.updateData(record);
		
		//下发消息到客户端
		ClientOnlineReward clientOnlineReward = new ClientOnlineReward();
		clientOnlineReward.setFlag(ClientOnlineReward.Flag_Get_Reward);
		clientOnlineReward.setGetRewardId(rewardId);
		module.send(clientOnlineReward);
		
		sendReward(rewardId);		
	}
	
	public byte getOnlineRewardStatus(OnlineRewardVo rewardVo , int _onLineTime){
		int rewardId = rewardVo.getOnlinerewardid();
		byte status = OnlineRewardConstant.rewardStatusNotFinish;
		int needSecond = rewardVo.getMinute() * 60;
		if (_onLineTime >= needSecond) {
			status = OnlineRewardConstant.rewardStatusFinish;
		}
		if (onlineRewardMap.containsKey(rewardId)) {
			ActOnlineRewardRecord actOnlineRewardRecord = onlineRewardMap.get(rewardId);
			if(actOnlineRewardRecord.getIsGot() == (byte)1){
				status = OnlineRewardConstant.rewardStatusGot;
			}
		}
		
		return status;
	}
	
	public int getOnlineTimeSecond(){
		if (lastOnlineTimestamp <= 0) return onlineTime.getOnlineTime();

		int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
		int _onLineTime = onlineTime.getOnlineTime() + passSecond;
		return _onLineTime;
	}
	
	/**
	 * 当前无可领取奖励且存在倒计时时，发送最近一个到达倒计时的剩余时间
	 */
	public void sendCountDownTime(){
		int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
		int _onLineTime = onlineTime.getOnlineTime() + passSecond;
	    
		int minTime = 0;
		if (!hasReward()) {
			Map<Integer, OnlineRewardVo> onlineRewardVos = OnlineRewardManager.getOnlineRewardVoMap();
			if (onlineRewardVos != null) {
				for (OnlineRewardVo vo : onlineRewardVos.values()) {
					byte status = getOnlineRewardStatus(vo, _onLineTime);
					if (status == OnlineRewardConstant.rewardStatusNotFinish) {
						int needSecond = vo.getMinute() * 60;
						int leftTime = needSecond - _onLineTime;
						if (minTime == 0) {
							minTime = leftTime;
						}else{
							minTime = leftTime < minTime ? leftTime : minTime;
						}
					}
				}
			}
		}
		
		ClientOnlineRewardCountDown clientOnlineRewardCountDown = new ClientOnlineRewardCountDown();
		clientOnlineRewardCountDown.setTime(minTime);
		module.send(clientOnlineRewardCountDown);		
	}
	
	public int getId(){
		return activityId;
	}
	
	/**
	 * 是否有可领取的奖励
	 * @return 
	 */
	public boolean hasReward(){
		int passSecond = (int)((new Date().getTime() - lastOnlineTimestamp)/1000);
		int _onLineTime = onlineTime.getOnlineTime() + passSecond;
		
		Map<Integer, OnlineRewardVo> onlineRewardVos = OnlineRewardManager.getOnlineRewardVoMap();
		if (onlineRewardVos != null) {
			for (OnlineRewardVo vo : onlineRewardVos.values()) {
				byte status = getOnlineRewardStatus(vo, _onLineTime);
				if (status == OnlineRewardConstant.rewardStatusFinish) {//有可领取的奖励
					return true;
				}
			}
		}
		
		return false;
	}
}
