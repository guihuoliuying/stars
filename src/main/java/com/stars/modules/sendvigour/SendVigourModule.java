package com.stars.modules.sendvigour;

import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.sendvigour.event.SendVigourActEvent;
import com.stars.modules.sendvigour.packet.ClientSendVigourData;
import com.stars.modules.sendvigour.recordmap.RecordMapSendVigour;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.services.ServiceHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by gaopeidian on 2017/3/29.
 */
public class SendVigourModule extends AbstractModule {
	RecordMapSendVigour record = null;
	
    public SendVigourModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("送体力", id, self, eventDispatcher, moduleMap);
    }
    
    @Override
    public void onCreation(String name_, String account_) throws Throwable {
    	initRecordMap();
    }
    
    @Override
    public void onDataReq() throws Exception {
    	initRecordMap();
    }
    
    private void initRecordMap() {
		record = new RecordMapSendVigour(context());
    } 
    
    @Override
    public void onInit(boolean isCreation) {
        sendData();
        
        //标记需要计算红点
	    signCalRedPoint(MConst.SendVigour, RedPointConst.SEND_VIGOUR);
    }
    
    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
    	if (record != null) {
			record.reset();
		}
    	
    	sendData();
    	
    	//标记需要计算红点
	    signCalRedPoint(MConst.SendVigour, RedPointConst.SEND_VIGOUR);
    }
    
	@Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.SEND_VIGOUR))) {
            if (canGetReward()) {
            	redPointMap.put(RedPointConst.SEND_VIGOUR, "");
			}else{
				redPointMap.put(RedPointConst.SEND_VIGOUR, null);
			}                   
        }
    }
    
    public void sendData(){
    	int curStep = ServiceHelper.sendVigourService().getCurStepId();
    	Map<Integer, Integer> recordMap = record.getRewardRecord();
    	byte getStatus = 0;
    	if (curStep != -1 && SendVigourManager.STEP_MAP.containsKey(curStep)) {
			int getCount = 0;
			if (recordMap.containsKey(curStep)) {
				getCount = recordMap.get(curStep);
			}
			if (getCount <= 0) {
				getStatus = 1;
			}
		}
    	
    	String cronexpr = "";
    	byte isTomorrow = 0;
    	if (getStatus == 0) {//奖励不可领，则找下一次可领取时间段,没有的话就明天
    		Map<Integer, String> configMap = DataManager.getActivityFlowConfig(SendVigourManager.ACTIVITY_FLOW_ID);
    		Set<Entry<Integer, Integer>> entrySet = SendVigourManager.STEP_MAP.entrySet();
    		isTomorrow = 1;
    		int index = 0;
        	for (Entry<Integer, Integer> entry : entrySet) {//找到第一个结束时间点在当前时间之后的，且不是curStep的    			
        		int startStep = entry.getKey();
        		String tempCronexpr = "";
        		if (configMap.containsKey(startStep)) {
        			tempCronexpr = configMap.get(startStep);
				}	
        		if (index == 0) {
        			cronexpr = tempCronexpr;					
					index ++;
				}
        		
        		int endStep = entry.getValue();
    			if (configMap.containsKey(endStep)) {
					Calendar calendar = ActivityFlowUtil.getTodayCalendar(configMap.get(endStep));
					if (startStep > curStep && calendar.getTime().getTime() > System.currentTimeMillis()) {
						cronexpr = tempCronexpr;
						isTomorrow = 0;
						break;
					}
				}
    		}
		}
    	
    	//发送消息到客户端
    	ClientSendVigourData clientSendVigourData = new ClientSendVigourData();
    	clientSendVigourData.setGetStatus(getStatus);
    	clientSendVigourData.setCronexpr(cronexpr);
    	clientSendVigourData.setIsTomorrow(isTomorrow);
    	send(clientSendVigourData);
    }
    
    public boolean canGetReward(){
    	int curStep = ServiceHelper.sendVigourService().getCurStepId();
    	if (curStep == -1) {
			return false;
		}
    	
    	if (!SendVigourManager.STEP_MAP.containsKey(curStep)) {
    		return false;
		}
    	
    	Map<Integer, Integer> getRewardMap = record.getRewardRecord();
    	int getCount = 0;
		if (getRewardMap.containsKey(curStep)) {
			getCount = getRewardMap.get(curStep);
		}
    	
    	if (getCount >= 1) {
    		return false;
		}
    	
    	return true;
    }
    
    public void getVigourReward(){
    	int curStep = ServiceHelper.sendVigourService().getCurStepId();
    	if (curStep == -1) {
    		warn("不在时间段内");
			return;
		}
    	
    	if (!SendVigourManager.STEP_MAP.containsKey(curStep)) {
    		warn("无该时间段");
    		return;
		}
    	
    	Map<Integer, Integer> getRewardMap = record.getRewardRecord();
    	int getCount = 0;
		if (getRewardMap.containsKey(curStep)) {
			getCount = getRewardMap.get(curStep);
		}
    	
    	if (getCount >= 1) {
			warn("已领取");
			return;
		}
    	
    	//加体力
    	ToolModule toolModule = (ToolModule)module(MConst.Tool);
    	Map<Integer, Integer> getMap = toolModule.addAndSend(ToolManager.VIGOR, SendVigourManager.SEND_VIGOUR, EventType.SEND_VIGOUR.getCode());
		
    	//发获奖提示到客户端
    	ClientAward clientAward = new ClientAward(getMap);
		send(clientAward);		
    	
    	//改领取次数
    	getRewardMap.put(curStep, getCount + 1);
    	record.setRewardRecord(getRewardMap);
    	
    	sendData();
    	
    	//标记需要计算红点
	    signCalRedPoint(MConst.SendVigour, RedPointConst.SEND_VIGOUR);
    }
    
    public void handleSendVigourActEvent(SendVigourActEvent sendVigourActEvent){
    	sendData();
    	
    	//标记需要计算红点
	    signCalRedPoint(MConst.SendVigour, RedPointConst.SEND_VIGOUR);
    }
}
