package com.stars.modules.bravepractise;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.bravepractise.packet.ClientBravePageInfo;
import com.stars.modules.bravepractise.packet.ClientBravePassAward;
import com.stars.modules.bravepractise.prodata.BraveInfoVo;
import com.stars.modules.bravepractise.recordmap.RecordMapBravePractise;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyAwardCheckEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.drop.DropModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;
import com.stars.modules.task.prodata.TaskVo;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.util.I18n;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by gaopeidian on 2016/11/16.
 */
public class BravePractiseModule extends AbstractModule {
	RecordMapBravePractise record = null;
	
	public BravePractiseModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super(MConst.BravePractise, id, self, eventDispatcher, moduleMap);
	}
	
	private void initRecordMap() throws SQLException {
		record = new RecordMapBravePractise(moduleMap() , context());
    } 
	
    @Override
    public void onCreation(String name_, String account_) throws Throwable {
    	initRecordMap();
    }
    
    @Override
    public void onDataReq() throws Exception {
    	initRecordMap();
    }
    
    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
    	if (record != null) {
			record.reset();
		}
    	
    	sendBravePageInfo();
    }
    
//    public int calculateTotalCount(){
//    	RoleModule roleModule = module(MConst.Role);
//    	int level = roleModule.getLevel();
//    	
//    	BraveInfoVo braveInfoVo = getOneBraveInfo(level);
//    	if (braveInfoVo != null) {
//    		return braveInfoVo.getCount();
//		}
//    	
//    	return 0;
//    }
    
    public void joinBravePractise(){
    	List<Integer> groups = record.getGroups();
    	if (groups.size() > 0) {
    		warn("brave_you_have_join");
			return;
		}
    	
    	setNewTask();
    }

	public void finishAllTaskByGold(){
		if(record.getDoneCount() > 0) return;
		List<Integer> groups = record.getGroups();
		if (groups.size() > 0) {
			warn("brave_you_have_join");
			return;
		}
		RoleModule roleModule = module(MConst.Role);
		BraveInfoVo braveInfoVo = getRandomBraveInfo(roleModule.getLevel(), groups);
		if (braveInfoVo == null) {
			warn("no_brave_product_data");
			return;
		}

		VipModule vipModule = module(MConst.Vip);
		VipinfoVo vipInfoVo = vipModule.getCurVipinfoVo();
		if(vipInfoVo == null) return;
		if(vipInfoVo.getBraveAuto() != 1) return;//没有权限立刻完成

		ToolModule toolModule = module(MConst.Tool);
		if(!toolModule.deleteAndSend(VipManager.FINISH_BRAVE_COST,EventType.FINISH_BRAVE_BY_GOLD.getCode())){
			warn(I18n.get("family.bonfire.hasNoGold"));
			return;
		}

		record.setDoneCount(BravePractiseManager.bravePractiseCount);
		eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_BRAVE_PRACTISE, BravePractiseManager.bravePractiseCount));
		eventDispatcher().fire(new DailyAwardCheckEvent());

		DropModule dropModule = module(MConst.Drop);
		Map<Integer,Integer> awardMap = dropModule.executeDrop(VipManager.FINISH_BRAVE_DROP_GROUP,1,true);
		toolModule.addAndSend(awardMap,EventType.FINISH_BRAVE_BY_GOLD.getCode());

		ClientBravePassAward clientBravePassAward = new ClientBravePassAward();
		clientBravePassAward.setRewardMap(awardMap);
		clientBravePassAward.setType((byte)1);
		send(clientBravePassAward);

		sendBravePageInfo();
	}
    
    public void submitTask(int taskId){
    	TaskVo taskVo = TaskManager.getTaskById(taskId);
    	if (taskVo != null && taskVo.getNextTaskId() == 0) {//是一组任务中的最后一个,则这组任务完成
    		finishGroup();
		}
    }
    
    public void finishGroup(){
    	int doneCount = record.getDoneCount() + 1;
    	int totalCount = BravePractiseManager.bravePractiseCount;
    	
    	if (doneCount > totalCount) {
    		warn("brave_time_out");
			return;
		}
    	
    	record.setDoneCount(doneCount);
    	eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_BRAVE_PRACTISE, 1));
    	
    	if (doneCount < totalCount) {
			setNewTask();
		}else{
			//发奖励 并通知客户端
			Map<Integer, Integer> award;
			BraveInfoVo bInfoVo = getCurTaskGroupBraveInfoVo();
			if (bInfoVo != null) {
				DropModule dropModule = (DropModule)module(MConst.Drop);
				award = dropModule.executeDrop(bInfoVo.getAward(), 1,true);
			}else{
				award = new HashMap<Integer, Integer>();
			}
			
			ToolModule toolModule = (ToolModule)module(MConst.Tool);
			Map<Integer,Integer> map = toolModule.addAndSend(award, EventType.SUBMITTASK.getCode());
			ClientBravePassAward clientBravePassAward = new ClientBravePassAward();
			clientBravePassAward.setRewardMap(map);
			send(clientBravePassAward);
		}
    }
    
    public void setNewTask(){
    	List<Integer> groups = record.getGroups();
    	
    	RoleModule roleModule = (RoleModule)module(MConst.Role);
    	BraveInfoVo braveInfoVo = getRandomBraveInfo(roleModule.getLevel(), groups);
    	if (braveInfoVo == null) {
    		warn("no_brave_product_data");
			return;
		}
    	
    	TaskVo taskVo = TaskManager.getFirstTaskByGroup(braveInfoVo.getGroup());
    	if (taskVo == null) {
    		warn("no_task_product_data");
			return;
		}
    	
    	TaskModule taskModule = (TaskModule)module(MConst.Task);
    	if(taskModule.checkTask(taskVo.getId(), false)){
    		groups.add(braveInfoVo.getBraveId());
    		record.setGroups(groups);
    	}else{
    	}
    }
    
    public int getDoneCount(){
    	if (record != null) {
    		return record.getDoneCount();
		}
    	return 0;
    }
    
    public BraveInfoVo getOneBraveInfo(int level){
    	Map<Integer, BraveInfoVo> braveInfos = BravePractiseManager.getBraveInfosByLevel(level);
    	if (braveInfos != null && braveInfos.size() > 0) {
			for (BraveInfoVo bInfo : braveInfos.values()) {
				return bInfo;
			}
		}
    	
    	return null;
    }
    
    public BraveInfoVo getRandomBraveInfo(int level , List<Integer> groups){
    	Map<Integer, BraveInfoVo> braveInfos = new HashMap<Integer, BraveInfoVo>();
    	
    	Map<Integer, BraveInfoVo> tempBraveInfos = BravePractiseManager.getBraveInfosByLevel(level);
    	for (BraveInfoVo infoVo : tempBraveInfos.values()) {
			boolean isIn = false;
			for (Integer braveId : groups) {
				if (braveId == infoVo.getBraveId()) {
					isIn = true;
					break;
				}
			}
			
			if (!isIn) {
				braveInfos.put(infoVo.getBraveId(), infoVo);
			}
		}
    		
    	int totalOdds = 0;
    	for (BraveInfoVo braveInfoVo : braveInfos.values()) {
			totalOdds += braveInfoVo.getOdds();
		}
    	
    	Random random = new Random();
    	int randomInt = random.nextInt(totalOdds) + 1;
    	
    	int index = 0;
    	for (BraveInfoVo braveInfoVo : braveInfos.values()) {
			index += braveInfoVo.getOdds();
			if (index >= randomInt) {
				return braveInfoVo;
			}
		}
    	
    	return null;
    }
    
    public BraveInfoVo getCurTaskGroupBraveInfoVo(){
    	List<Integer> groups = record.getGroups();
		if (groups != null && groups.size() > 0) {
			int size = groups.size();
			int braveInfoId = groups.get(size - 1);
			BraveInfoVo braveInfoVo = BravePractiseManager.getBraveInfoVo(braveInfoId);
			return braveInfoVo;
		}
		
		RoleModule roleModule = module(MConst.Role);
    	int level = roleModule.getLevel();
		BraveInfoVo bInfoVo = getOneBraveInfo(level);
		return bInfoVo;
    } 
    
    public void sendBravePageInfo(){
    	int totalCount = BravePractiseManager.bravePractiseCount;
    	
    	Map<Integer, Integer> award;
    	BraveInfoVo bInfoVo = getCurTaskGroupBraveInfoVo();
    	if (bInfoVo != null) {
			award = bInfoVo.getShowAwardMap();
		}else{
			award = new HashMap<Integer, Integer>();
		}
    	
    	ClientBravePageInfo clientBravePageInfo = new ClientBravePageInfo();
    	clientBravePageInfo.setRewardMap(award);
    	clientBravePageInfo.setLeftCount(totalCount - record.getDoneCount());
    	//clientBravePageInfo.setTotalCount(record.getTotalCount());
    	send(clientBravePageInfo);
    }
}

