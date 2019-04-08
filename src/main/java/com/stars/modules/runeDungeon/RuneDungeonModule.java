package com.stars.modules.runeDungeon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.runeDungeon.packet.ClientRuneDungeon;
import com.stars.modules.runeDungeon.packet.ServerRuneDungeon;
import com.stars.modules.runeDungeon.proData.RuneDungeonStageInfo;
import com.stars.modules.runeDungeon.proData.RuneDungeonVo;
import com.stars.modules.runeDungeon.userData.FriendHelpChaInfo;
import com.stars.modules.runeDungeon.userData.RuneDungeonPo;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.Summary;
import com.stars.util.DateUtil;
import com.stars.util.RandomUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author huzhipeng
 * 符文副本（PVE挑战副本）
 */
public class RuneDungeonModule extends AbstractModule {

	public RuneDungeonModule(long id, Player self, EventDispatcher eventDispatcher,
                             Map<String, Module> moduleMap) {
		super("RuneDungeon", id, self, eventDispatcher, moduleMap);
	}
	
	private RuneDungeonPo runeDungeonPo;
	
	private byte fightState;
	
	@Override
	public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
		runeDungeonPo.setHelpAwardTimes(0);
		context().update(runeDungeonPo);
	}
	
	@Override
	public void onDataReq() throws Throwable {
		String sql = "select * from rolerunedungeon where roleid = "+id();
		runeDungeonPo = DBUtil.queryBean(DBUtil.DB_USER, RuneDungeonPo.class, sql);
		if(runeDungeonPo==null){
			int tokendungeonId = RuneDungeonManager.runeDungeonList.get(0).getTokendungeonId();
			runeDungeonPo = new RuneDungeonPo(id(), tokendungeonId);
			context().insert(runeDungeonPo);
		}
	}
	
	@Override
	public void onCreation(String name, String account) throws Throwable {
		int tokendungeonId = RuneDungeonManager.runeDungeonList.get(0).getTokendungeonId();
		runeDungeonPo = new RuneDungeonPo(id(), tokendungeonId);
		context().insert(runeDungeonPo);
	}
	
	@Override
	public void onInit(boolean isCreation) throws Throwable {
		if(!isCreation){
			List<Object> offlineAwardList = ServiceHelper.runeDungeonService().getOfflineAward(id());
			if(offlineAwardList==null){
				return;
			}
			long updateTime = (Long)offlineAwardList.get(0);
			if(updateTime>runeDungeonPo.getHelpRewardUpdateTime()){
				runeDungeonPo.setHelpRewardUpdateTime(updateTime);
				runeDungeonPo.setHelpReward((Map<Integer, Long>)offlineAwardList.get(1));
				runeDungeonPo.setHelpAwardTimes((Integer)offlineAwardList.get(2));
				runeDungeonPo.setHaveHelpPlayerSet((Set<Long>)offlineAwardList.get(3));
			}
		}
	}
	
	public void execHandle(ServerRuneDungeon packet){
		byte opType = packet.getOpType();
		if(opType==RuneDungeonManager.REQ_UI_INFO){
			openUI(packet.getPlayType());
		}else if(opType==RuneDungeonManager.SELECT_UPDATE_MAIN_UI){
			selectDungeon(packet.getPlayType(), packet.getDungeonId());
		}else if(opType==RuneDungeonManager.REQ_SELECT_UI_INFO){
			getDungeonInfo(packet.getPlayType());
		}else if(opType==RuneDungeonManager.REQ_START_FIGHT){
			startFight(packet.getPlayType(), packet.getFriendId(), packet.getDungeonId());
		}else if(opType==RuneDungeonManager.RESET_DUNGEON){
			resetTeamDungeon(packet.getDungeonId());
		}else if(opType==RuneDungeonManager.GET_HELP_REWARD){
			getHelpReward();
		}else if(opType==RuneDungeonManager.REQ_HELP_AWARD_INFO){
			sendHelpRewardInfo();
		}
	}
	
	//打开符文副本UI(PVE挑战副本)
	public void openUI(byte type){
		if(runeDungeonPo==null) return;
		
		//测试用
//		runeDungeonPo.getHelpMap().clear();
		
		
		int dungeonId = runeDungeonPo.getDungeonId();
		ClientRuneDungeon packet = new ClientRuneDungeon();
		packet.setOpType(RuneDungeonManager.SEND_UI_INFO);
		packet.setPlayType(type);
		packet.setDungeonId(dungeonId);
		packet.setMyRoleId(id());
		packet.setSingleCha(runeDungeonPo.getSingleCha());
		if(type==RuneDungeonManager.TEAM_PLAY){
			Map<Long, Integer> helpMap = runeDungeonPo.getHelpMap();
			Iterator<Entry<Long, Integer>> iterator = helpMap.entrySet().iterator();
			int time = 0;
			Entry<Long, Integer> entry = null;
			int currentTime = DateUtil.getCurrentTimeInt();
			Map<Long, Integer> coolingMap = new HashMap<>();
			int passTime = 0;
			for(;iterator.hasNext();){
				entry = iterator.next();
				time = entry.getValue();
				passTime = currentTime-time;
				if(passTime<RuneDungeonManager.RelaxTime){
					coolingMap.put(entry.getKey(), RuneDungeonManager.RelaxTime-passTime);
				}else{
					iterator.remove();
				}
			} 
			packet.setCoolingMap(coolingMap);
			Map<Integer, List<Integer>> teamStageIdMap = runeDungeonPo.getTeamStageIdMap();
			List<Integer> stageIdList = teamStageIdMap.get(dungeonId);
			Map<Integer, FriendHelpChaInfo> teamChaMap = runeDungeonPo.getTeamChaMap();
			if(stageIdList==null){
				stageIdList = randStage(dungeonId);
				teamStageIdMap.put(dungeonId, stageIdList);
			}
			FriendHelpChaInfo friendHelpChaInfo = teamChaMap.get(dungeonId);
			if(friendHelpChaInfo==null){
				friendHelpChaInfo = new FriendHelpChaInfo();
				friendHelpChaInfo.setDungeonId(dungeonId);
				teamChaMap.put(dungeonId, friendHelpChaInfo);
			}
			context().update(runeDungeonPo);
			packet.setChaStep(friendHelpChaInfo.getChaStep());
			packet.setAngerLevel(friendHelpChaInfo.getAngerLevel());
			packet.setTeamStageIdList(stageIdList);
		}
		send(packet);
	}
	
	//打开特定副本
	public void selectDungeon(byte type, int dungeonId){
		ClientRuneDungeon packet = new ClientRuneDungeon();
		packet.setOpType(RuneDungeonManager.SEND_UI_INFO);
		packet.setPlayType(type);
		packet.setDungeonId(dungeonId);
		packet.setMyRoleId(id());
		packet.setSingleCha(runeDungeonPo.getSingleCha());
		if(dungeonId<runeDungeonPo.getDungeonId()){
			int size = RuneDungeonManager.runeDungeonMap.get(dungeonId).getStageIdList().size();
			packet.setSingleCha(size);
		}
		if(type==RuneDungeonManager.TEAM_PLAY){
			Map<Long, Integer> helpMap = runeDungeonPo.getHelpMap();
			Iterator<Entry<Long, Integer>> iterator = helpMap.entrySet().iterator();
			int time = 0;
			Entry<Long, Integer> entry = null;
			int currentTime = DateUtil.getCurrentTimeInt();
			Map<Long, Integer> coolingMap = new HashMap<>();
			int passTime = 0;
			for(;iterator.hasNext();){
				entry = iterator.next();
				time = entry.getValue();
				passTime = currentTime-time;
				if(passTime<RuneDungeonManager.RelaxTime){
					coolingMap.put(entry.getKey(), RuneDungeonManager.RelaxTime-passTime);
				}else{
					iterator.remove();
				}
			} 
			packet.setCoolingMap(coolingMap);
			Map<Integer, List<Integer>> teamStageIdMap = runeDungeonPo.getTeamStageIdMap();
			List<Integer> stageIdList = teamStageIdMap.get(dungeonId);
			Map<Integer, FriendHelpChaInfo> teamChaMap = runeDungeonPo.getTeamChaMap();
			if(stageIdList==null){
				stageIdList = randStage(dungeonId);
				teamStageIdMap.put(dungeonId, stageIdList);
			}
			FriendHelpChaInfo friendHelpChaInfo = teamChaMap.get(dungeonId);
			if(friendHelpChaInfo==null){
				friendHelpChaInfo = new FriendHelpChaInfo();
				friendHelpChaInfo.setDungeonId(dungeonId);
				teamChaMap.put(dungeonId, friendHelpChaInfo);
			}
			context().update(runeDungeonPo);
			packet.setChaStep(friendHelpChaInfo.getChaStep());
			packet.setAngerLevel(friendHelpChaInfo.getAngerLevel());
			packet.setTeamStageIdList(stageIdList);
		}
		send(packet);
	}
	
	/**
	 * 重置对应副本数据    （怒气、轮数、进度 等）
	 * @param dungeonId
	 */
	public void resetTeamDungeon(int dungeonId){
		FriendHelpChaInfo chaInfo = runeDungeonPo.getTeamChaMap().get(dungeonId);
		chaInfo.setAngerLevel(0);
		chaInfo.setChaStep(0);
		chaInfo.setKillRun(0);
		List<Integer> randStageList = randStage(dungeonId);
		runeDungeonPo.getTeamStageIdMap().put(dungeonId, randStageList);
		context().update(runeDungeonPo);
		selectDungeon(RuneDungeonManager.TEAM_PLAY, dungeonId);
	}
	
	//开始战斗
	public void startFight(byte type, long friendId, int dungeonId){
		if(!RuneDungeonManager.runeDungeonMap.containsKey(dungeonId)) return;
		RuneDungeonScene scene = (RuneDungeonScene)SceneManager.newScene(SceneManager.SCENETYPE_RUNE_DUNGEON);
		Integer stageId = 0;
		RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
		int buffId = 0;
		int addNum = 0;
		if(type==RuneDungeonManager.SINGLE_PLAY){//单人挑战
			int singleCha = runeDungeonPo.getSingleCha();
			int nowDungeonId = runeDungeonPo.getDungeonId();//副本id
			if(dungeonId!=nowDungeonId){
				send(new ClientText("非当前可挑战副本"));
				return;
			}
			List<Integer> stageIdList = runeDungeonVo.getStageIdList();
			stageId = stageIdList.get(singleCha);
		}else{
			Map<Long, Integer> helpMap = runeDungeonPo.getHelpMap();
			int currentTime = DateUtil.getCurrentTimeInt();
			Map<Integer, List<Integer>> teamStageIdMap = runeDungeonPo.getTeamStageIdMap();
			List<Integer> stageIdList = teamStageIdMap.get(dungeonId);
			Map<Integer, FriendHelpChaInfo> teamChaMap = runeDungeonPo.getTeamChaMap();
			if(stageIdList==null){
				stageIdList = randStage(dungeonId);
				teamStageIdMap.put(dungeonId, stageIdList);
			}
			FriendHelpChaInfo friendHelpChaInfo = teamChaMap.get(dungeonId);
			if(friendHelpChaInfo==null){
				friendHelpChaInfo = new FriendHelpChaInfo();
				friendHelpChaInfo.setDungeonId(dungeonId);
				teamChaMap.put(dungeonId, friendHelpChaInfo);
			}
			int chaStep = friendHelpChaInfo.getChaStep();//进度
			stageId = stageIdList.get(chaStep);//
			if(friendId>0){				
				if(helpMap.containsKey(friendId)){
					int time = helpMap.get(friendId);
					if((currentTime-time)<RuneDungeonManager.RelaxTime){
						send(new ClientText("该好友无法助战，还在冷却中"));
						return;
					}
				}
				helpMap.put(friendId, currentTime);
				scene.addToFiendList(friendId);
				//添加好友战斗对象
				Summary summary = ServiceHelper.summaryService().getSummary(friendId);
				Map<String, FighterEntity> entityMap = FighterCreator.createBySummary(FighterEntity.CAMP_SELF, summary);
				StageinfoVo stageVo = SceneManager.getStageVo(stageId);
				for(FighterEntity entity : entityMap.values()){
					entity.setPosition(stageVo.getPosition());
					entity.setRotation(stageVo.getRotation());
				}
				scene.addFriendFighter(entityMap);
			}
			context().update(runeDungeonPo);
			int angerLevel = friendHelpChaInfo.getAngerLevel();
			if(angerLevel>0){
				buffId = RuneDungeonManager.Boss_Buff;
				addNum = angerLevel;
			}
		}
		if(!scene.canEnter(moduleMap(), stageId)){
			return;
		}
        //检测体力
		RoleModule roleModlue = module(MConst.Role);
		int vigor = roleModlue.getRoleRow().getVigor();
		if(vigor<runeDungeonVo.getReqpower()){
			return;
		}
			
		scene.setAddNum(addNum);
		scene.setBuffId(buffId);
		scene.setDungeonId(dungeonId);
		scene.setPlayType(type);
		SceneModule sceneModule = module(MConst.Scene);
		sceneModule.enterScene(scene, SceneManager.SCENETYPE_RUNE_DUNGEON, stageId, stageId);
		ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_RUNE_DUNGEON, 1));
		ServerLogModule serverLogModule = module(MConst.ServerLog);
		serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_42.getThemeId(), stageId);
	}
	
	/**
	 * 随机关卡
	 * @param dungeonId //副本id
	 * @return
	 */
	private List<Integer> randStage(int dungeonId){
		RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
		List<Integer> proStageIdList = runeDungeonVo.getStageIdList();
		List<Integer> tempList = new ArrayList<>(proStageIdList);
		List<Integer> stageIdList = new ArrayList<>();
		int size = tempList.size();
		for(int i=0;i<size;i++){
			int rand = RandomUtil.rand(0, tempList.size()-1);
			stageIdList.add(tempList.get(rand));
			tempList.remove(rand);
		}
		return stageIdList;
	}
	
	/**
	 * 切换副本界面信息
	 */
	public void getDungeonInfo(byte type){
		int dungeonId = runeDungeonPo.getDungeonId();
		List<Object[]> list = new ArrayList<>();
		if(type==RuneDungeonManager.SINGLE_PLAY){
			int size = RuneDungeonManager.runeDungeonList.size();
			RuneDungeonVo dungeonVo = null;
			List<Integer> stageIdList = null;
			RuneDungeonStageInfo stageInfo = null;
			Integer stageId = 0;
			for(int i=0;i<size;i++){
				dungeonVo = RuneDungeonManager.runeDungeonList.get(i);
				if(dungeonVo.getTokendungeonId()<dungeonId){
					stageIdList = dungeonVo.getStageIdList();
					stageId = stageIdList.get(stageIdList.size()-1);
					stageInfo = dungeonVo.getStageInfoMap().get(stageId);
					list.add(new Object[]{dungeonVo.getTokendungeonId(), dungeonVo.getDungeonname(), 
							dungeonVo.getRecommendlevel(), stageInfo.getRecommend()});
				}else if(dungeonVo.getTokendungeonId()==dungeonId){
					stageIdList = dungeonVo.getStageIdList();
					int singleCha = runeDungeonPo.getSingleCha();
					int index = singleCha;
					if(singleCha>=stageIdList.size()){
						index = stageIdList.size()-1;
					}
					stageId = stageIdList.get(index);
					stageInfo = dungeonVo.getStageInfoMap().get(stageId);
					list.add(new Object[]{dungeonVo.getTokendungeonId(), dungeonVo.getDungeonname(), 
							dungeonVo.getRecommendlevel(), stageInfo.getRecommend()});
				}else{
					break;
				}
			}
		}else{
			int size = RuneDungeonManager.runeDungeonList.size();
			Map<Integer, FriendHelpChaInfo> teamChaMap = runeDungeonPo.getTeamChaMap();
			Map<Integer, List<Integer>> teamStageIdMap = runeDungeonPo.getTeamStageIdMap();
			RuneDungeonVo dungeonVo = null;
			List<Integer> stageIdList = null;
			RuneDungeonStageInfo stageInfo = null;
			FriendHelpChaInfo chaInfo = null;
			Integer stageId = 0;
			int tokendungeonId = 0;
			int chaStep = 0;
			for(int i=0;i<size;i++){
				dungeonVo = RuneDungeonManager.runeDungeonList.get(i);
				tokendungeonId = dungeonVo.getTokendungeonId();
				if(tokendungeonId<=dungeonId){
					if(teamChaMap.containsKey(tokendungeonId)){
						chaInfo = teamChaMap.get(tokendungeonId);
						chaStep = chaInfo.getChaStep();
						stageId = teamStageIdMap.get(tokendungeonId).get(chaStep);
						stageInfo = dungeonVo.getStageInfoMap().get(stageId);
						list.add(new Object[]{dungeonVo.getTokendungeonId(), dungeonVo.getDungeonname(), 
								dungeonVo.getRecommendlevel(), stageInfo.getRecommend()});
					}else{
						stageIdList = dungeonVo.getStageIdList();
						stageId = stageIdList.get(stageIdList.size()-1);
						stageInfo = dungeonVo.getStageInfoMap().get(stageId);
						list.add(new Object[]{dungeonVo.getTokendungeonId(), dungeonVo.getDungeonname(), 
								dungeonVo.getRecommendlevel(), stageInfo.getRecommend()});
					}
				}
			}
		}
		ClientRuneDungeon packet = new ClientRuneDungeon();
		packet.setOpType(RuneDungeonManager.SEND_DUNGEON_INFO);
		packet.setDungeonInfolist(list);
		send(packet);
	}
	
	//战斗结算 结算
	public void fightEnd(byte result, byte type, int dungeonId, int logStageId, long useTime){
		ClientRuneDungeon packet = new ClientRuneDungeon();
		packet.setOpType(RuneDungeonManager.FIGHT_END);
		ServerLogModule serverLogModule = module(MConst.ServerLog);
		if(result==SceneManager.STAGE_FAIL){//失败
			packet.setFightResult((byte)0);
			serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_42.getThemeId(), 
					serverLogModule.makeJuci(), ThemeType.ACTIVITY_42.getThemeId(), logStageId, useTime);
		}else if(result==SceneManager.STAGE_VICTORY){//胜利
			//扣除体力
			RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
			ToolModule toolModule = module(MConst.Tool);
			toolModule.deleteAndSend(ToolManager.VIGOR, runeDungeonVo.getReqpower(), EventType.RUNE_DUNGEON.getCode());

			packet.setFightResult((byte)1);
			DropModule dropModule = module(MConst.Drop);
			if(type==RuneDungeonManager.SINGLE_PLAY){
				dungeonId = runeDungeonPo.getDungeonId();
				int singleCha = runeDungeonPo.getSingleCha();
				Integer stageId = runeDungeonVo.getStageIdList().get(singleCha);
				RuneDungeonStageInfo stageInfo = runeDungeonVo.getStageInfoMap().get(stageId);
				int killAward = stageInfo.getKillAward();
				Map<Integer, Integer> toolMap = dropModule.executeDrop(killAward, 1, true);
				toolModule.addAndSend(toolMap, EventType.RUNE_DUNGEON.getCode());
				packet.setPassAward(toolMap);
				//检测是否是最后一个boss
				if(singleCha==runeDungeonVo.getStageIdList().size()-1){//开启下一个副本
					int size = RuneDungeonManager.runeDungeonList.size();
					int tokendungeonId = 0;
					int newDungeonId = dungeonId;
					for(int i=0;i<size;i++){
						tokendungeonId = RuneDungeonManager.runeDungeonList.get(i).getTokendungeonId();
						if(tokendungeonId>dungeonId){
							newDungeonId = tokendungeonId;
							break;
						}
					}
					runeDungeonPo.setDungeonId(newDungeonId);
					int singlecompletedrop = runeDungeonVo.getSinglecompletedrop();
					Map<Integer, Integer> allPassMap = dropModule.executeDrop(singlecompletedrop, 1, true);
					toolModule.addAndSend(allPassMap, EventType.RUNE_DUNGEON.getCode());
					packet.setAllPassAward(allPassMap);
					if(newDungeonId>dungeonId){//通知客户端开启下一级
						runeDungeonPo.setSingleCha(0);
						ClientRuneDungeon noticePacket = new ClientRuneDungeon();
						noticePacket.setOpType(RuneDungeonManager.NOTICE_NEXT);
						noticePacket.setDungeonId(newDungeonId);
						send(noticePacket);
					}
					if(newDungeonId==dungeonId){
						runeDungeonPo.setSingleCha(singleCha+1);
					}
				}else{
					singleCha += 1;
					runeDungeonPo.setSingleCha(singleCha);//进度
				}
			}else{
				FriendHelpChaInfo chaInfo = runeDungeonPo.getTeamChaMap().get(dungeonId);
				if(chaInfo==null) return;
				int chaStep = chaInfo.getChaStep();
				List<Integer> stageIdList = runeDungeonPo.getTeamStageIdMap().get(dungeonId);
//				RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
				int angerLevel = chaInfo.getAngerLevel();
				List<Integer> multiKilldropList = runeDungeonVo.getMultiKilldropList();
				int index = angerLevel;
				if(index>=multiKilldropList.size()){
					index = multiKilldropList.size()-1;
				}
				Integer killAwardId = multiKilldropList.get(index);
				Map<Integer, Integer> killAwardMap = dropModule.executeDrop(killAwardId, 1, true);
				toolModule.addAndSend(killAwardMap, EventType.RUNE_DUNGEON.getCode());
				packet.setPassAward(killAwardMap);
				//检测是否是最后一个boss
				if(chaStep==stageIdList.size()-1){//重新刷新
					int killRun = chaInfo.getKillRun();
					chaInfo.setKillRun(killRun+1);
					chaInfo.setChaStep(0);
					List<Integer> multicompletedropList = runeDungeonVo.getMulticompletedropList();
					if(killRun>=multicompletedropList.size()){
						killRun = multicompletedropList.size()-1;
					}
					Integer awardId = multicompletedropList.get(killRun);
					Map<Integer, Integer> runPassMap = dropModule.executeDrop(awardId, 1, true);
					toolModule.addAndSend(runPassMap, EventType.RUNE_DUNGEON.getCode());
					packet.setAllPassAward(runPassMap);
					//刷新副本
					List<Integer> randStageList = randStage(dungeonId);
					runeDungeonPo.getTeamStageIdMap().put(dungeonId, randStageList);
				}else{
					chaStep += 1;
					chaInfo.setChaStep(chaStep);
				}
				chaInfo.setAngerLevel(angerLevel+1);
			}
			context().update(runeDungeonPo);
			selectDungeon(type, dungeonId);
			serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_42.getThemeId(), 
					serverLogModule.makeJuci(), ThemeType.ACTIVITY_42.getThemeId(), logStageId, useTime);
		}
		send(packet);
	}
	
	public void addHelpRward(Map<Integer, Integer> toolMap, long beHelpId){
		int helpAwardTimes = runeDungeonPo.getHelpAwardTimes();
		if(helpAwardTimes>=RuneDungeonManager.HelpAwardLimit) return;
		runeDungeonPo.getHaveHelpPlayerSet().add(beHelpId);
		runeDungeonPo.setHelpAwardTimes(helpAwardTimes+1);
		runeDungeonPo.setHelpRewardUpdateTime(DateUtil.getCurrentTimeLong());
		Map<Integer, Long> helpReward = runeDungeonPo.getHelpReward();
		Iterator<Entry<Integer, Integer>> iterator = toolMap.entrySet().iterator();
		Entry<Integer, Integer> entry = null;
		int itemId = 0;
		for(;iterator.hasNext();){
			entry = iterator.next();
			itemId = entry.getKey();
			if(helpReward.containsKey(itemId)){
				helpReward.put(itemId, helpReward.get(itemId)+entry.getValue());
			}else{
				helpReward.put(itemId, (long)entry.getValue());
			}
		}
		context().update(runeDungeonPo);
	}
	
	public void sendHelpRewardInfo(){
		Map<Integer, Long> helpReward = runeDungeonPo.getHelpReward();
		if(StringUtil.isEmpty(helpReward)) return;
		Set<Long> haveHelpPlayerSet = runeDungeonPo.getHaveHelpPlayerSet();
		Map<Integer, Long> tempHelpReward = new HashMap<>(helpReward);
		ClientRuneDungeon packet = new ClientRuneDungeon();
		Set<Long> friendSet = new HashSet<>(haveHelpPlayerSet);
		packet.setOpType(RuneDungeonManager.HELP_AWARD_UI);
		packet.setHelpReward(tempHelpReward);
		packet.setFriendSet(friendSet);
		send(packet);
	}
	
	public void getHelpReward(){
		runeDungeonPo.getHaveHelpPlayerSet().clear();
		Map<Integer, Long> helpReward = runeDungeonPo.getHelpReward();
		Map<Integer, Long> tempHelpReward = new HashMap<>(helpReward);
		helpReward.clear();
		context().update(runeDungeonPo);
		Iterator<Entry<Integer, Long>> iterator = tempHelpReward.entrySet().iterator();
		Entry<Integer, Long> entry = null;
		List<Map<Integer, Integer>> toolMapList = new ArrayList<>();
		Map<Integer, Integer> toolMap = null;
		long count = 0;
		for(;iterator.hasNext();){
			entry = iterator.next();
			count = entry.getValue();
			while(count>Integer.MAX_VALUE){
				int size = toolMapList.size();
				if(size==0){
					toolMap = new HashMap<>();
					toolMapList.add(toolMap);
					toolMap.put(entry.getKey(), Integer.MAX_VALUE);
				}else{					
					for(int i=0;i<size;i++){
						toolMap = toolMapList.get(i);
						if(toolMap.containsKey(entry.getKey())){
							continue;
						}
						toolMap = new HashMap<>();
						toolMapList.add(toolMap);
						toolMap.put(entry.getKey(), Integer.MAX_VALUE);
					}
				}
				count = count-Integer.MAX_VALUE;
			}
			if(count>0){				
				toolMap = new HashMap<>();
				toolMapList.add(toolMap);
				toolMap.put(entry.getKey(), (int)count);
			}
		}
		ToolModule toolModule = module(MConst.Tool);
		for(Map<Integer, Integer> rewardMap : toolMapList){			
			toolModule.addAndSend(rewardMap, EventType.RUNE_DUNGEON.getCode());
			//发获奖提示到客户端
			ClientAward awardPacket = new ClientAward(rewardMap);
			send(awardPacket);
		}
	}

}
