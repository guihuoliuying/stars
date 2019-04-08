package com.stars.modules.daily5v5;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.daily5v5.event.Daily5v5CancelMatchingEvent;
import com.stars.modules.daily5v5.event.Daily5v5FightEndEvent;
import com.stars.modules.daily5v5.event.Daily5v5MatchingSuccessEvent;
import com.stars.modules.daily5v5.event.Daily5v5MessageEvent;
import com.stars.modules.daily5v5.gm.Daily5v5GmHandler;
import com.stars.modules.daily5v5.listener.Daily5v5Lisener;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.multiserver.daily5v5.Daily5v5Manager;
import com.stars.multiserver.daily5v5.data.Daily5v5MoraleVo;
import com.stars.multiserver.daily5v5.data.FivePvpMerge;
import com.stars.multiserver.daily5v5.data.MatchFloat;
import com.stars.multiserver.daily5v5.data.PvpExtraEffect;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stars.modules.data.DataManager.getCommConfig;

public class Daily5v5ModuleFactory extends AbstractModuleFactory<Daily5v5Module> {

	public Daily5v5ModuleFactory() {
		super(new Daily5v5PacketSet());
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		GmManager.reg("daily5v5Fight", new Daily5v5GmHandler());
	}
	
	@Override
	public Daily5v5Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		return new Daily5v5Module(id, self, eventDispatcher, map);
	}
	
	@Override
	public void loadProductData() throws Exception {
		loadMatchFloat();
		loadCommondefine();
		loadFivePvpMerge();
		loadPvpExtraEffect();
		loadMoraleVoList();
	}
	
	private void loadMatchFloat() throws Exception{
		String sql = "select * from matchfloat";
		List<MatchFloat> matchFloatList = DBUtil.queryList(DBUtil.DB_PRODUCT, MatchFloat.class, sql);
		int size = matchFloatList.size();
		MatchFloat matchFloat = null;
		List<MatchFloat> list = null;
		for(int i=0;i<size;i++){
			matchFloat = matchFloatList.get(i);
			list = Daily5v5Manager.matchFloatMap.get(matchFloat.getType());
			if(list==null){
				list = new ArrayList<>();
				Daily5v5Manager.matchFloatMap.put(matchFloat.getType(), list);
			}
			list.add(matchFloat);
		}
	}
	
	private void loadCommondefine() throws Exception{
		String str = DataManager.getCommConfig("dailyfivepvp_mach_scorecorrect");
		Map<Integer, Integer> map = StringUtil.toMap(str, Integer.class, Integer.class, '+', ',');
		Daily5v5Manager.fixMap = map;
		String failedtimes = DataManager.getCommConfig("dailyfivepvp_failedtimes");
		Daily5v5Manager.MAX_STEP = Integer.parseInt(failedtimes);//扩展次数
		String activityflow = DataManager.getCommConfig("dailyfivepvp_activityflow");
		Daily5v5Manager.ActFlow = StringUtil.toArray(activityflow, int[].class, '+');
//		String finialreward = DataManager.getCommConfig("dailyfivepvp_finialreward");
//		Daily5v5Manager.FinalReward = StringUtil.toArray(finialreward, int[].class, '+');
		String highreward = DataManager.getCommConfig("dailyfivepvp_finialreward_high");
		Daily5v5Manager.highReward = StringUtil.toArray(highreward, int[].class, '+');
		String lowreward = DataManager.getCommConfig("dailyfivepvp_finialreward_low");
		Daily5v5Manager.lowReward = StringUtil.toArray(lowreward, int[].class, '+');
		String blankreward = DataManager.getCommConfig("dailyfivepvp_finialreward_blank");
		Daily5v5Manager.blankReward = StringUtil.toArray(blankreward, int[].class, '+');
		String pvpwarCount = DataManager.getCommConfig("fivepvpwar_count");
		Daily5v5Manager.gainsCounts = StringUtil.toArray(pvpwarCount, int[].class, '+');
		
		String singlematchtime = DataManager.getCommConfig("dailyfivepvp_singlemachtime");
		Daily5v5Manager.SingleMatchTime = Integer.parseInt(singlematchtime);
		String teammachtime = DataManager.getCommConfig("dailyfivepvp_teammachtime");
		Daily5v5Manager.TEAM_MATCHING_TIME = Integer.parseInt(teammachtime);
		String match_battlewait = DataManager.getCommConfig("dailyfivepvp_mach_battlewait");
		Daily5v5Manager.START_REMIND_TIME = Integer.parseInt(match_battlewait);
		String reborn_time = DataManager.getCommConfig("fivepvpwar_reborn_time");
		Map<Integer, Integer> rebornMap = StringUtil.toMap(reborn_time, Integer.class, Integer.class, '+', ',');
		Daily5v5Manager.reliveTimeMap = rebornMap;
//		Daily5v5Manager.Daily5v5TotalCount = DataManager.getCommConfig("fivepvpwar_count", (byte)2);
//		//战斗
		Daily5v5Manager.DamagePercent = DataManager.getCommConfig("fivepvpwar_damagepercent", 10.0)/100.0;
		String coefficient_a = DataManager.getCommConfig("fivepvpwar_coefficient_a");
		Daily5v5Manager.Coefficient_A = StringUtil.toArray(coefficient_a, int[].class, '+');
		String pointscore = DataManager.getCommConfig("fivepvpwar_pointscore");
		Daily5v5Manager.pointsDeltaOfDestoryTower = Integer.parseInt(pointscore);
		String coefficient_b = DataManager.getCommConfig("fivepvpwar_coefficient_b");
		Daily5v5Manager.moraleDeltaOfKillFighterInEliteFight = Integer.parseInt(coefficient_b);
		String pointmoraleadd = DataManager.getCommConfig("fivepvpwar_pointmoraleadd");
		Daily5v5Manager.moraleDeltaOfDestoryTower = Integer.parseInt(pointmoraleadd);
		String pointmoralesub = DataManager.getCommConfig("fivepvpwar_pointmoralesub");
		Daily5v5Manager.moraleDeltaOfLosingTower = Integer.parseInt(pointmoralesub);
		String killnotice = DataManager.getCommConfig("fivepvpwar_killnotice");
		Daily5v5Manager.KillNotice = Integer.parseInt(killnotice);
		DataManager.getCommConfig("dailyfivepvp_activityflow");
		Map<Integer, Byte> towertypeMap = StringUtil.toMap(getCommConfig("fivepvpwar_towertype"), Integer.class, Byte.class, '+', '|');
		Daily5v5Manager.towerTypeMap = towertypeMap;
		String coefficient_z = getCommConfig("fivepvpwar_coefficient_z");
        String[] coefficient = coefficient_z.split("\\+");
        Daily5v5Manager.coefficient_hp = Integer.parseInt(coefficient[0]);
        Daily5v5Manager.coefficient_attack = Integer.parseInt(coefficient[1]);
        Daily5v5Manager.coefficient_defense = Integer.parseInt(coefficient[2]);
        Daily5v5Manager.coefficient_hit = Integer.parseInt(coefficient[3]);
        Daily5v5Manager.coefficient_avoid = Integer.parseInt(coefficient[4]);
        Daily5v5Manager.coefficient_crit = Integer.parseInt(coefficient[5]);
        Daily5v5Manager.coefficient_anticrit = Integer.parseInt(coefficient[6]);
		Daily5v5Manager.TIPS_INTERVAL = DataManager.getCommConfig("fivepvp_tvtips_interval", 300_000) * 60 * 1000;
        
        //活动流程初始化
        Map<Integer, String> dayFlowMap = new HashMap<>();
        int newStep = 1;
        Map<Integer, String> activityFlowConfig = null;
        String conf = "";
        for(int activityId: Daily5v5Manager.ActFlow){
        	activityFlowConfig = DataManager.getActivityFlowConfig(activityId);
        	int size = activityFlowConfig.size();
        	for(int i=1;i<=size;i++){
        		conf = activityFlowConfig.get(i);
        		dayFlowMap.put(newStep, conf);
        		newStep++;
        	}
        }
        Daily5v5Manager.dayFlowMap = dayFlowMap;
	}
	
	private void loadFivePvpMerge() throws Exception{
		String sql = "select * from fivepvpmerge";
		Map<Integer, FivePvpMerge> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "jobid", FivePvpMerge.class, sql);
		Daily5v5Manager.pvpMergeMap = map;
	}
	
	private void loadPvpExtraEffect() throws Exception{
		String sql = "select * from pvpextraeffect";
		Map<Integer, PvpExtraEffect> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "fivepvpvipeffid", PvpExtraEffect.class, sql);
		Daily5v5Manager.pvpExtraEffectMap = map;
		int effectType = 0;
		int level = 0;
		Integer maxLevel = null;
		List<PvpExtraEffect> list = null;
		for(PvpExtraEffect pvpEffect : map.values()){
			effectType = pvpEffect.getEffecttype();
			maxLevel = Daily5v5Manager.effectMaxLevel.get(effectType);
			level = pvpEffect.getLevel();
			if(maxLevel==null){
				maxLevel = level;
			}else{
				if(maxLevel<level){
					maxLevel = level;
				}
			}
			Daily5v5Manager.effectMaxLevel.put(effectType, maxLevel);
			
			list = Daily5v5Manager.effectTypeMap.get(effectType);
			if(list==null){
				list = new ArrayList<>();
				Daily5v5Manager.effectTypeMap.put(effectType, list);
			}
			list.add(pvpEffect);
		}
	}
	
	public void loadMoraleVoList() throws SQLException{
		List<Daily5v5MoraleVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, Daily5v5MoraleVo.class, "select * from `fivepvpwarmorale`");
		Daily5v5Manager.moraleVoList = list;
	}
	
	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		Daily5v5Lisener listener = new Daily5v5Lisener(module);
		
		eventDispatcher.reg(Daily5v5MatchingSuccessEvent.class, listener);
		eventDispatcher.reg(Daily5v5FightEndEvent.class, listener);
		eventDispatcher.reg(Daily5v5MessageEvent.class, listener);
		eventDispatcher.reg(Daily5v5CancelMatchingEvent.class, listener);
	}

}
