package com.stars.services.skyrank;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.GameBootstrap;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.skyrank.SkyRankManager;
import com.stars.modules.skyrank.prodata.SkyRankConfig;
import com.stars.modules.skyrank.prodata.SkyRankSeasonVo;
import com.stars.multiserver.skyrank.SkyrankHelper;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 天梯排行榜（跨服）
 * @author xieyuejun
 *
 */
public class SkyRankKFServiceActor extends ServiceActor implements SkyRankKFService {
	
	public Map<Integer,Map<Long,Integer>> skyRankKfFrankMap = new HashMap<Integer, Map<Long,Integer>>();
	public Map<Long, SkyRankShowData> skyRankDataMap = new HashMap<>();
	public List< SkyRankShowData> skyRankList= new ArrayList<>();


	public static int runInterval = 1;// 检测间隔
	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.SkyRankKFService, this);
		SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SkyKFRank, new SchedulerTask(), runInterval, runInterval,
				TimeUnit.SECONDS);
		
	}
	
	class SchedulerTask implements Runnable {
		@Override
		public void run() {
			if (!GameBootstrap.getServerType().equals(BootstrapConfig.SKYRANK)) {
				return;
			}
			ServiceHelper.skyRankKFService().runUpdate();
		}
	}
	
	public void runUpdate(){
		updateRank();
		printRank(false);
		checkRankAward();
		checkNewSeason();
	}
	
	
	//主动请求到跨服拿排行榜数据，接收跨服同步过来的排行榜数据
	
	//接收跨服发奖，直接到角色身上
	
	public void reqSkyRankData(int serverId,int fromServerId){
        try {
            SkyrankHelper.skyRankLocalService().receiveRankData(fromServerId, skyRankList, skyRankKfFrankMap.get(fromServerId));
        } catch (Exception e) {
//			e.printStackTrace();
        }
    }
	
	public static int SEND_INTERVAL = 1000*60*5;//发奖有效时间区间
	
	

	public int lastSendRankAwardSeasonId = 0;
	public int nowRankSeasonId =0;
	
	/**
	 * 定时发奖 ，只有跨服才会执行
	 */
	public void checkRankAward() {
		try {
			long now = System.currentTimeMillis();
			int nowSeasonId = SkyRankManager.getManager().getNowSeasonId();
			SkyRankSeasonVo nowSeason = SkyRankManager.getManager().getSkyRankSeasonVo(nowSeasonId);
			if (nowSeason == null)
				return;
			if (nowSeasonId != lastSendRankAwardSeasonId && now >= nowSeason.getSendAwardTime()
					&& now < (nowSeason.getSendAwardTime() + SEND_INTERVAL)) {
				lastSendRankAwardSeasonId = nowSeasonId;
				sendRankAward();
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void checkNewSeason(){
		long now = System.currentTimeMillis();
		for (SkyRankSeasonVo seasonVo : SkyRankManager.getManager().getSkyRankSeasonList()) {
			if(now < seasonVo.getFinishedTime()){
				SkyRankManager.getManager().setNowSeasonId(seasonVo.getSkyRankTimeid());
				break;
			}
		}
	}
	
	/**
	 * 赛季排行榜奖励
	 */
	public void sendRankAward(){
		int nowSeasonId = SkyRankManager.getManager().getNowSeasonId();
		int rank = 0;
		try {
			updateRank();
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
		printRank(true);
		
		List<SkyRankShowData> tmpSkyRankList = new ArrayList<>();
		tmpSkyRankList.addAll(skyRankDataMap.values());
		Collections.sort(tmpSkyRankList);
		
		Map<Integer,List<SkyRankShowData>> serverAwardListMap = new HashMap<Integer, List<SkyRankShowData>>();
		for (SkyRankShowData rd : tmpSkyRankList) {
			try {
				rank++;
				rd.setRank(rank);
				// 排行奖励数量
				if (rank > SkyRankConfig.config.AWARD_MAX_RANK) {
					break;
				}
				LogUtil.info("season " + nowSeasonId + " kfskyrank sendRankAward " + rd.toString());
				List<SkyRankShowData> serverAwardList = serverAwardListMap.get(rd.getServerId());
				if (serverAwardList == null) {
					serverAwardList = new ArrayList<SkyRankShowData>();
					serverAwardListMap.put(rd.getServerId(), serverAwardList);
				}
				serverAwardList.add(rd);
			} catch (Throwable e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
		//分服务发奖
		for (Entry<Integer, List<SkyRankShowData>> entry : serverAwardListMap.entrySet()) {
			try {
				SkyrankHelper.skyRankLocalService().receiveRankAward(entry.getKey(), entry.getValue());
			} catch (Throwable e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 获取我的排名
	 */
	public void getMyRankData(int serverId,long roleId,SkyRankShowData myDefalutRank){
		SkyRankShowData myRank =  skyRankDataMap.get(roleId);
		SkyrankHelper.skyRankLocalService().receiveRankData(myDefalutRank.getServerId(), roleId, myRank, myDefalutRank);
	}
	
	
	long lastUpdateTime = 0;
	public static int UPDATE_INTERVAL = 1000;
	public void updateRank() {
		try {
			long now = System.currentTimeMillis();
			if (now - lastUpdateTime < UPDATE_INTERVAL) {
				return;
			}
			
			if(nowRankSeasonId == 0){
				nowRankSeasonId = SkyRankManager.getManager().getNowSeasonId();
			}else{
				//赛季不同，重置排行榜
				if(nowRankSeasonId != SkyRankManager.getManager().getNowSeasonId()){
					nowRankSeasonId = SkyRankManager.getManager().getNowSeasonId();
					List<SkyRankShowData> skyRankList = new ArrayList<>();
					Map<Long, SkyRankShowData> skyRankDataMap = new HashMap<>();
					this.skyRankList = skyRankList;
					this.skyRankDataMap = skyRankDataMap;
					return;
				}
			}
			
			lastUpdateTime = now;
			List<SkyRankShowData> tmpSkyRankList = new ArrayList<>();
			tmpSkyRankList.addAll(skyRankDataMap.values());
			Collections.sort(tmpSkyRankList);

			List<SkyRankShowData> skyRankList = new ArrayList<>();
			Map<Long, SkyRankShowData> skyRankDataMap = new HashMap<>();
			
			Map<Integer,Map<Long,Integer>> skyRankKfFrankMap = new HashMap<Integer, Map<Long,Integer>>();
			
			Map<Long,Integer> serverRankMap;
			int i = 0;
			for (SkyRankShowData rd : tmpSkyRankList) {
				rd.setRank(i+1);
				// 排行显示
				if (i < SkyRankConfig.config.MAX_RANK) {
					skyRankList.add(rd);
				}
				// 排行奖励数量
				if (i < SkyRankConfig.config.AWARD_MAX_RANK) {
					skyRankDataMap.put(rd.getRoleId(), rd);
					//每个服的排名
					serverRankMap = skyRankKfFrankMap.get(rd.getServerId());
					if(serverRankMap  == null){
						serverRankMap = new HashMap<Long, Integer>();
						skyRankKfFrankMap.put(rd.getServerId(), serverRankMap);
					}
					serverRankMap.put(rd.getRoleId(), rd.getRank());
				}
				i++;
			}
			this.skyRankKfFrankMap = skyRankKfFrankMap;
			this.skyRankList = skyRankList;
			this.skyRankDataMap = skyRankDataMap;
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	long lastPrintTime = 0;
	public static long PRINT_INTERVAL = 5*60*1000;

	public void printRank(boolean isSendAward) {
		try {
			long now = System.currentTimeMillis();
			if (!isSendAward && now - lastPrintTime < PRINT_INTERVAL) {
				return;
			}
			lastPrintTime = now;
			if (skyRankList == null)
				return;
			int nowSeasonId = SkyRankManager.getManager().getNowSeasonId();
			for (SkyRankShowData rankData : skyRankList) {
				LogUtil.info(nowSeasonId + " kfskyrank | " + rankData.toString());
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}

	@Override
	public void updateSkyRankData(int serverId,List<SkyRankShowData> skyRankDataList) {
		if (skyRankDataList != null && skyRankDataList.size() > 0) {
			for (SkyRankShowData rd : skyRankDataList) {
				updateSkyRankData(rd);
			}
		}
	}

	public void updateSkyRankData(SkyRankShowData skyRankData) {
		skyRankDataMap.put(skyRankData.getRoleId(), skyRankData);
	}
	
	@Override
	public void updateSkyRankData(int serverId,SkyRankShowData skyRankData) {
		updateSkyRankData(skyRankData);
	}

	@Override
	public void printState() {
		// TODO Auto-generated method stub
		
	}

}
