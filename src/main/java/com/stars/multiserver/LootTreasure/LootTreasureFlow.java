package com.stars.multiserver.LootTreasure;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootTreasureFlow extends ActivityFlow {

	public static final int STEP_READY_START = 3; // 在STEP_START时触发，通知客户端活动准备开始;
	public static final int STEP_START_2 = 4; // 第二场开始
	public static final int STEP_END_2 = 5; // 第二场结束
	public static final int STEP_READY_START_2 = 6; // 第二场在STEP_START时触发，通知客户端活动准备开始;

	private Map<String, LTActor> ltActorMap ;

	public Map<String, LTActor> getLtActorMap(){
		return ltActorMap;
	}

	public LootTreasureFlow(){
		ltActorMap = new HashMap<>();
	}

	@Override
	public String getActivityFlowName() {
		return "lootTreasureFlow";
	}

	@Override
	public void onTriggered(int step, boolean isRedo) {
		switch (step) {
			case STEP_START_CHECK:
				if (between(STEP_START, STEP_END) || between(STEP_START_2, STEP_END_2)) {
					startActivity();
				}
				break;
			case STEP_START: case STEP_START_2:
				startActivity();
				break;
		}
	}

	private void startActivity(){
		List<LootSectionVo> lootSectionVoList = LootTreasureManager.getLootSectionVoList();
		LogUtil.info("夺宝活动section数据个数:"+lootSectionVoList.size());
		for (LootSectionVo lootSectionVo : lootSectionVoList) {
			String id = String.valueOf(lootSectionVo.getLevelsection());
			LogUtil.info("夺宝创建actor step 1: "+id);
			Actor a = ActorServer.getActorSystem().getActor(id);
			if (a != null) {
				a.tell(new StopLTActor(), Actor.noSender);
				ActorServer.getActorSystem().removeActor(id);
			}
			LogUtil.info("夺宝创建actor step 2: "+id);
			a = new LTActor(id);
			LogUtil.info("夺宝创建actor step 3: "+id);
			ltActorMap.put(id, (LTActor)a);
			LogUtil.info("建立actor  "+id+" , "+String.valueOf(a));
			ActorServer.getActorSystem().addActor(id, a);
			((LTActor)a).getLootTreasure().startRunner();
		}
	}

	public long getEndTimeStamp(){
		long now = System.currentTimeMillis();
		long end1 = getTimeStamp(STEP_END);
		if (now <= end1) {
			return end1;
		}
		return getTimeStamp(STEP_END_2);
//		return getTimeStamp(STEP_END);
	}


	public long getStartTimeStamp(){
		long now = System.currentTimeMillis();
		if (now <= getTimeStamp(STEP_END)) {
			return getTimeStamp(STEP_START);
		}
		return getTimeStamp(STEP_READY_START_2);
//		return getTimeStamp(STEP_START);
	}


	private long getTimeStamp(int step){
		String cronExpr = this.configMap.get(step);
		Calendar stepCalendar = ActivityFlowUtil.getTodayCalendar(cronExpr);
		return stepCalendar.getTimeInMillis();
	}

}
