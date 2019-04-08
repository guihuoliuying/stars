package com.stars.modules.skyrank;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmManager;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.skyrank.event.SkyRankDailyAwardEvent;
import com.stars.modules.skyrank.event.SkyRankGradAwardEvent;
import com.stars.modules.skyrank.event.SkyRankLogEvent;
import com.stars.modules.skyrank.event.SkyRankScoreHandleEvent;
import com.stars.modules.skyrank.gm.SkyRankGmHandler;
import com.stars.modules.skyrank.listener.SkyRankListener;
import com.stars.modules.skyrank.prodata.*;
import com.stars.services.skyrank.SkyRankLocalServiceActor;

import java.util.*;

/**
 * 天梯排行
 * 
 * @author xieyuejun
 *
 */
public class SkyRankModuleFactory extends AbstractModuleFactory<SkyRankModule> {

	public SkyRankModuleFactory() {
		super(new SkyRankPacketSet());
	}

	@Override
	public SkyRankModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		return new SkyRankModule(MConst.SkyRank, id, self, eventDispatcher, moduleMap);
	}
	
	public void init(){
		GmManager.reg("skyrank", new SkyRankGmHandler());
	}
	
	/**
	 * 加载赛季时间
	 * @throws Exception
	 */
	public void loadSeasonTime() throws Exception {
		List<SkyRankSeasonVo> tmpSkyRankSeasonList = new LinkedList<>();
		tmpSkyRankSeasonList = DBUtil.queryList(DBUtil.DB_PRODUCT, SkyRankSeasonVo.class, "select * from skyranktime");
		Map<Integer,SkyRankSeasonVo> skyRankSeasonMap = new HashMap<>();
		for (SkyRankSeasonVo seasonVo : tmpSkyRankSeasonList) {
			seasonVo.init();
			skyRankSeasonMap.put(seasonVo.getSkyRankTimeid(), seasonVo);	
		}
		Collections.sort(tmpSkyRankSeasonList);
		long now = System.currentTimeMillis();
		for (SkyRankSeasonVo seasonVo : tmpSkyRankSeasonList) {
			if(now < seasonVo.getFinishedTime()){
				SkyRankManager.getManager().setNowSeasonId(seasonVo.getSkyRankTimeid());
				break;
			}
		}
		SkyRankManager.getManager().setSkyRankSeasonList(tmpSkyRankSeasonList);
		SkyRankManager.getManager().setSkyRankSeasonMap(skyRankSeasonMap);
	}
	
	

	@Override
	public void loadProductData() throws Exception {
		Map<Short, SkyRankScoreVo> tmpHighLadderScoreMap = new HashMap<Short, SkyRankScoreVo>();
		tmpHighLadderScoreMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "rankscoretype", SkyRankScoreVo.class,
				"select * from skyrankscore");
		SkyRankManager.getManager().setSkyRankScoreMap(tmpHighLadderScoreMap);

		Map<Integer, SkyRankGradVo> skyRankGradMap = new HashMap<Integer, SkyRankGradVo>();

		skyRankGradMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "skyrankgradid", SkyRankGradVo.class, "select * from skyrankgrad");
		List<SkyRankGradVo> skyRankGradList = new ArrayList<>();
		skyRankGradList.addAll(skyRankGradMap.values());
		Collections.sort(skyRankGradList);
		SkyRankManager.getManager().setSkyRankGradMap(skyRankGradMap);
		SkyRankManager.getManager().setSkyRankGradList(skyRankGradList);
		
		int size = skyRankGradList.size();
		int stage = 0;
		String gradName = "";
		Map<Integer, int[]> skyRankGradStageMap = new HashMap<>();
		int[] stageInfo = null;
		for(int i=size-1;i>=0;i--){
			SkyRankGradVo skyRankGradVo = skyRankGradList.get(i);
			if(!skyRankGradVo.getName().equals(gradName)){
				stage += 1;
				gradName = skyRankGradVo.getName();
			}
			if(i==0){
				stageInfo = new int[]{stage, -1};
			}else{				
				stageInfo = new int[]{stage, skyRankGradVo.getStar()};
			}
			skyRankGradStageMap.put(skyRankGradVo.getSkyRankGradId(), stageInfo);
		}
		SkyRankManager.getManager().setSkyRankGradStageMap(skyRankGradStageMap); 

		List<SkyRankAwardVo> awardList = new LinkedList<SkyRankAwardVo>();
		awardList = DBUtil.queryList(DBUtil.DB_PRODUCT, SkyRankAwardVo.class, "select * from skyrankreward");
		SkyRankManager.getManager().setAwardList(awardList);

		Map<Integer, SkyRankDailyAwardVo> rankDailyAwardMap = new HashMap<Integer, SkyRankDailyAwardVo>();
		
		Map<Integer, SkyRankUpAwardVo> rankUpAwardMap = new HashMap<Integer, SkyRankUpAwardVo>();

		List<SkyRankSeasonRankAwardVo> rankAwardList = new LinkedList<>();

		Map<Integer, SkyRankSeasonGradAwardVo> seasonGradAwardMap = new HashMap<>();

		int maxAwardRank =0;
		
		for (SkyRankAwardVo rav : awardList) {
			if(rav.getType() ==SkyRankAwardVo.TYPE_DAILY){
				SkyRankDailyAwardVo rankDailyVo = new SkyRankDailyAwardVo();
				rankDailyVo.setGradId(Integer.parseInt(rav.getParam()));
				rankDailyVo.setDropId(rav.getReward());
				rankDailyVo.setUid(rav.getSkyRankRewardId());
				rankDailyAwardMap.put(rankDailyVo.getGradId(), rankDailyVo);
			}else if (rav.getType() ==SkyRankAwardVo.TYPE_GRADUP ) {

				SkyRankUpAwardVo rankUpVo = new SkyRankUpAwardVo();
				rankUpVo.setGradId(Integer.parseInt(rav.getParam()));
				rankUpVo.setDropId(rav.getReward());
				rankUpAwardMap.put(rankUpVo.getGradId(), rankUpVo);

			} else if (rav.getType() == SkyRankAwardVo.TYPE_RANK) {

				SkyRankSeasonRankAwardVo rankAwardVo = new SkyRankSeasonRankAwardVo();
				String[] range = rav.getParam().split("\\+");
				rankAwardVo.setDropId(rav.getReward());
				rankAwardVo.setLower(Integer.parseInt(range[0]));
				rankAwardVo.setUpper(Integer.parseInt(range[1]));
				rankAwardList.add(rankAwardVo);
				if(rankAwardVo.getUpper() >maxAwardRank){
					maxAwardRank = rankAwardVo.getUpper();
				}
			} else if (rav.getType() == SkyRankAwardVo.TYPE_GRAD) {
				SkyRankSeasonGradAwardVo gav = new SkyRankSeasonGradAwardVo();
				gav.setDropId(rav.getReward());
				gav.setGradId(Integer.parseInt(rav.getParam()));
				seasonGradAwardMap.put(gav.getGradId(), gav);
			}
		}
		
		SkyRankConfig.config.AWARD_MAX_RANK = maxAwardRank ;
		SkyRankLocalServiceActor.MAX_CACHE = maxAwardRank+1000;
		
		Collections.sort(rankAwardList);
		
		SkyRankManager.getManager().setRankDailyAwardMap(rankDailyAwardMap);
		SkyRankManager.getManager().setRankUpAwardMap(rankUpAwardMap);
		SkyRankManager.getManager().setRankAwardList(rankAwardList);
		SkyRankManager.getManager().setSeasonGradAwardMap(seasonGradAwardMap);

		loadSeasonTime();
		SkyRankConfig.config.init();
	}
	
	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		SkyRankListener listener = 	new SkyRankListener((SkyRankModule) module);
		eventDispatcher.reg(SkyRankGradAwardEvent.class, listener);
		eventDispatcher.reg(SkyRankScoreHandleEvent.class, listener);
		eventDispatcher.reg(SkyRankLogEvent.class, listener);
		eventDispatcher.reg(RoleRenameEvent.class,listener);
		eventDispatcher.reg(SkyRankDailyAwardEvent.class,listener);
	}

}
