package com.stars.modules.daily;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.event.DailyAwardCheckEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.daily.listener.DailyAwardCheckListener;
import com.stars.modules.daily.listener.DailyBackCityListener;
import com.stars.modules.daily.listener.DailyFunctionEventListener;
import com.stars.modules.daily.prodata.DailyAwardVo;
import com.stars.modules.daily.prodata.DailyBallStageVo;
import com.stars.modules.daily.prodata.DailyFightScoreModuleVo;
import com.stars.modules.daily.prodata.DailyVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.scene.event.BackCityEvent;

import java.util.*;


@DependOn({MConst.Data})
public class DailyModuleFactory extends AbstractModuleFactory<DailyModule> {

	public DailyModuleFactory(){
		super(new DailyPacketSet());
	}
	
	@Override
    public void init() throws Exception {
		
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	eventDispatcher.reg(DailyFuntionEvent.class, new DailyFunctionEventListener(module));
		eventDispatcher.reg(BackCityEvent.class, new DailyBackCityListener(module));
		eventDispatcher.reg(DailyAwardCheckEvent.class,new DailyAwardCheckListener(module));

    }
	
	@Override
    public DailyModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		return new DailyModule(id, self, eventDispatcher, map);
    }
	
	@Override
    public void loadProductData() throws Exception {
		intDailyInfoPro(); //加载日常数据
		intFightScoreModulePro(); //加载推荐战力相关产品数据
		intDailyAwardPro(); //加载每日奖励信息
		intDailyBallPro(); //加载斗魂珠信息
    }

	private void intDailyInfoPro() throws Exception{
		List<DailyVo> dailyVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, DailyVo.class, "select * from dailyinfo");
		if (dailyVoList == null) {
			throw new Exception("daily 策划数据没有");
		}
		Map<Byte,Map<Short,DailyVo>> map = new HashMap<>();
		Map<Short,DailyVo> map1 = null;
		Map<Short,DailyVo> dailyVoMap = new HashMap<>();
		Iterator iterator = dailyVoList.iterator();
		while(iterator.hasNext()){  //遍历数据
			DailyVo dailyVo = (DailyVo)iterator.next();
			for(Byte tagId:dailyVo.getTagList()){ //一个活动可能多个标签
				map1 = map.get(tagId);
				if(map1 == null){
					map1 = new HashMap<>();
				}
				map1.put(dailyVo.getDailyid(),dailyVo);
				map.put(tagId,map1);
			}
			dailyVoMap.put(dailyVo.getDailyid(),dailyVo);
		}
		DailyManager.setDailyVoByTagMap(map);
		DailyManager.setDailyVoMap(dailyVoMap);
	}

	private void intFightScoreModulePro() throws Exception{
		//加载推荐战力相关产品数据
		String sql = "select * from dailyfightscoremodule";
		List<DailyFightScoreModuleVo> dailyFightScoreModuleVoList;
		dailyFightScoreModuleVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, DailyFightScoreModuleVo.class,sql);
		if(dailyFightScoreModuleVoList == null){
			throw new Exception("dailyfightscoremodule 策划数据没有");
		}
		Map<String,DailyFightScoreModuleVo> dailyFightScoreModuleVoHashMap = new HashMap<>();
		for(DailyFightScoreModuleVo dailyFightScoreModuleVo:dailyFightScoreModuleVoList){ // key : sysname + level
			String key = dailyFightScoreModuleVo.getSysName() + "_" + dailyFightScoreModuleVo.getOpenDays();
			dailyFightScoreModuleVoHashMap.put(key,dailyFightScoreModuleVo);
		}
		DailyManager.setDailyFightScoreModuleVoMap(dailyFightScoreModuleVoHashMap);

	}

	private void intDailyAwardPro() throws Exception{
		String sql = "select * from dailyaward";
		Map<Integer, DailyAwardVo> dailyAwardVoMap;
		dailyAwardVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "dailyawardid", DailyAwardVo.class,sql);
		if(dailyAwardVoMap == null){
			throw new Exception("dailyaward 策划数据没有");
		}
		List<DailyAwardVo> superAwardList = new ArrayList<>();
		List<DailyAwardVo> mutipleAwardList = new ArrayList<>();
		for(DailyAwardVo dailyAwardVo:dailyAwardVoMap.values()){
			if(dailyAwardVo.getAwardType() == DailyManager.SUPER_AWARD){
				superAwardList.add(dailyAwardVo);
			}else{
				mutipleAwardList.add(dailyAwardVo);
			}
		}
		DailyManager.setDailyAwardVoMap(dailyAwardVoMap);
		DailyManager.setSuperAwardList(superAwardList);
		DailyManager.setMultipleAwardList(mutipleAwardList);
		String sendAwardSwitch = DataManager.getCommConfig("daily_send_award_switch","0");
		if(sendAwardSwitch.equals("1")){
			DailyManager.setSendAwardSwitch(true);
		}else{
			DailyManager.setSendAwardSwitch(false);
		}

		String superAwardTypeCheckStr = DataManager.getCommConfig("daily_super_award_condition","5+10");
		String[] array = superAwardTypeCheckStr.split("[+]");
		DailyManager.setPreOpenDayForBetterSuperAward(Integer.parseInt(array[0]));
		DailyManager.setPreOpenDayForBestSuperAward(Integer.parseInt(array[1]));
	}

	private void intDailyBallPro() throws Exception{
		String sql = "select * from dailyballstage";
		Map<Integer, DailyBallStageVo> dailyBallStageVoMap;
		dailyBallStageVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "level", DailyBallStageVo.class, sql);
		if (dailyBallStageVoMap == null){
			throw new Exception("dailyballstage 策划数据没有");
		}

		Map<Integer,Integer> maxStarMap = new HashMap<>();
		int maxLevel = 0;
		Integer maxStageStar = 0;
		for(DailyBallStageVo dailyBallStageVo:dailyBallStageVoMap.values()){
			maxStageStar = maxStarMap.get(dailyBallStageVo.getStage());
			if(maxStageStar == null || maxStageStar < dailyBallStageVo.getStar()){
				maxStarMap.put(dailyBallStageVo.getStage(),dailyBallStageVo.getStar());
			}

			if(maxLevel < dailyBallStageVo.getLevel()){
				maxLevel = dailyBallStageVo.getLevel();
			}
		}

		DailyManager.setDailyBallStageVoMap(dailyBallStageVoMap);
		DailyManager.setDailyBallStageMaxStarMap(maxStarMap);
		DailyManager.setMaxDailyBallLevel(maxLevel);
	}
}
