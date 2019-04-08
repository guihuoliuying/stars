package com.stars.multiserver.daily5v5;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.persist.DbRowDao;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.daily5v5.event.Daily5v5CancelMatchingEvent;
import com.stars.modules.daily5v5.event.Daily5v5MatchingSuccessEvent;
import com.stars.modules.daily5v5.event.Daily5v5MessageEvent;
import com.stars.modules.daily5v5.packet.ClientDaily5v5;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDaily5v5;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.daily5v5.data.*;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightArgs;
import com.stars.multiserver.fightutil.daily5v5.BattleData;
import com.stars.multiserver.fightutil.daily5v5.Daily5v5Battle;
import com.stars.network.PacketUtil;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Daily5v5MatchServiceActor extends ServiceActor implements Daily5v5MatchService{
	
//	private Map<Long, Daily5v5MatchingVo> tempMatchingMap = new LinkedHashMap<>();//参与匹配的玩家临时集合
	
	private Map<Long, Daily5v5MatchingVo> matchingMap = new LinkedHashMap<>();//参与匹配的所有玩家集合
	
	private LinkedHashMap<Integer, MatchingTeamVo> teamMatchMap = new LinkedHashMap<>();//在匹配队伍集合
	
	private Map<String, Daily5v5Battle> fightMap = new HashMap<>();
	
	private ConcurrentHashMap<Long, Daily5v5FightingInfo> playerFightingMap = new ConcurrentHashMap<>();
	
	private Set<Long> continueFightEnter = new HashSet<>();
	
	private long idGenerator = 0L; // fightId的自增序列生成器
	
	private DbRowDao dao;
	
	private Daily5v5MatchFlow flow;
	
	@Override
	public void init() throws Throwable {
		dao = new DbRowDao(SConst.Daily5v5MatchService);
        ServiceSystem.getOrAdd(SConst.Daily5v5MatchService, this);
        flow = new Daily5v5MatchFlow();
        synchronized (Daily5v5MatchServiceActor.class) {
			flow.init(SchedulerHelper.getScheduler(), Daily5v5Manager.dayFlowMap);
			//测试用
//			activityStart();
		}
	}

	/**
	 * 加入匹配队列， 开始匹配
	 */
	@Override
	public void matching(int serverId, MatchingInfo info) {
		long roleId = info.getRoleId();
		if(matchingMap.containsKey(roleId)) return;
		if(playerFightingMap.containsKey(roleId)){
			Daily5v5FightingInfo fightInfo = playerFightingMap.get(roleId);
			if(fightInfo!=null){
				Daily5v5Battle daily5v5Battle = fightMap.get(fightInfo.getFightId());
				if(daily5v5Battle!=null){
					Integer mainServerId = daily5v5Battle.getBattleData().getFighterSeverIdMap().get(String.valueOf(roleId));
					Daily5v5RpcHelper.roleService().send(mainServerId, roleId, new ClientDaily5v5(Daily5v5Manager.CONTINUE_FIGHTING));
					return;
				}
				playerFightingMap.remove(roleId);
			}
		}
		//第一版 先用等级代替段位分
//		int fixIntegral = 100;
//		int fixIntegral = fixIntegral(info.getRoleLevel(), info.getWin(), info.getLose());
		int fixIntegral = fixIntegral(info.getIntegal(), info.getWin(), info.getLose());
		if(fixIntegral<0) return;
		Daily5v5MatchingVo vo = new Daily5v5MatchingVo(roleId, info.getRoleName(), info.getRoleLevel(), info.getJob(), 
				info.getLose(), info.getWin(), fixIntegral, info.getServerId(), info.getServerName(), info.getEntity(), 
				info.getTrueFightValue(), info.getInitiativeBuff(), info.getPassivityBuff());
		matchingMap.put(roleId, vo);
		ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.MATCHING_STATE);
		packet.setMatchingState((byte)1);
		Daily5v5RpcHelper.roleService.send(info.getServerId(), roleId, packet);
		LogUtil.info("daily5v5,进入匹配队列, roleId:"+roleId+" , serverId:"+info.getServerId());
	}
	
	/**
	 * 修正段位分
	 * @param integral 段位分
	 * @param win
	 * @param lose
	 * @return 修正后的段位分
	 */
	private int fixIntegral(int integral, int win, int lose){
		int checkKey = 0;
		if(win>0){
			if(win>Daily5v5Manager.WIN_STANDARD){				
				win = Daily5v5Manager.WIN_STANDARD;
			}
			checkKey = win;
		}
		if(lose>0){			
			if(lose>Daily5v5Manager.LOSE_STANDARD){
				lose = Daily5v5Manager.LOSE_STANDARD;
			}
			checkKey = lose*-1;
		}
		Iterator<Integer> iterator = Daily5v5Manager.fixMap.keySet().iterator();
		int abs = -1;
		int tempAbs = 0;
		int key = 0;
		int selectKey = 0;
		for(;iterator.hasNext();){
			key = iterator.next();
			tempAbs = Math.abs(key-checkKey);
			if(abs==-1){
				abs = tempAbs;
				selectKey = key;
			}else if(tempAbs<abs){
				abs = tempAbs;
				selectKey = key;
			}
		}
		Integer persent = Daily5v5Manager.fixMap.get(selectKey);
		integral = integral+integral*persent/100;
		return integral;
	}

	/**
	 * 取消匹配          
	 */
	public void cancelMatching(int serverId, int mainServerId, long roleId, boolean isOffline) {
		if(!matchingMap.containsKey(roleId)){
			if(!playerFightingMap.containsKey(roleId)){			
				if(!isOffline){					
					Daily5v5RpcHelper.daily5v5Service().cancelMatchingResult(mainServerId, roleId);
				}
			}else{
//				ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.MATCHING_STATE);
//				packet.setMatchingState((byte)2);
//				Daily5v5RpcHelper.roleService.send(mainServerId, roleId, packet);
				if(!isOffline){					
					Daily5v5RpcHelper.daily5v5Service().cancelMatchingResult(mainServerId, roleId);
				}
			}
			return;
		} 
		Daily5v5MatchingVo matchingVo = matchingMap.get(roleId);
		//如果有队伍解散队伍
		int teamId = matchingVo.getTeamId();
		if(teamId>0){
			MatchingTeamVo matchingTeamVo = teamMatchMap.get(teamId);
			if(matchingTeamVo==null){
				return;
			}
			if(!StringUtil.isEmpty(matchingTeamVo.getFightId())){
				if(fightMap.containsKey(matchingTeamVo.getFightId())){					
					return;
				}
			}
			dismissTeam(matchingTeamVo);
		}
		matchingMap.remove(roleId);
		//返回取消成功
		if(!isOffline){			
			Daily5v5RpcHelper.daily5v5Service().cancelMatchingResult(matchingVo.getServerId(), roleId);
		}
	}
	
	@Override
	public void continueFighting(int serverId, int mainServerId, long roleId){
		Daily5v5FightingInfo fightInfo = playerFightingMap.get(roleId);
		if(fightInfo!=null){
			Daily5v5Battle daily5v5Battle = fightMap.get(fightInfo.getFightId());
			if(daily5v5Battle!=null){
				long campId = fightInfo.getTeamId();
				continueFightEnter.add(roleId);
				daily5v5Battle.enterFight(MultiServerHelper.getServerId(), campId, roleId);
				return;
			}else{
				Daily5v5RpcHelper.roleService().notice(mainServerId, new Daily5v5MessageEvent());
			}
			playerFightingMap.remove(roleId);
		}else{
			Daily5v5RpcHelper.roleService().notice(mainServerId, roleId, new Daily5v5MessageEvent());
		}
	}
	
	@Override
	public void checkContinue(int serverId, int mainServerId, long roleId) {
		if(playerFightingMap.containsKey(roleId)){
			Daily5v5FightingInfo fightInfo = playerFightingMap.get(roleId);
			if(fightInfo!=null){
				Daily5v5Battle daily5v5Battle = fightMap.get(fightInfo.getFightId());
				if(daily5v5Battle!=null){
					Daily5v5RpcHelper.roleService().send(mainServerId, roleId, new ClientDaily5v5(Daily5v5Manager.CONTINUE_FIGHTING));
					return;
				}
				playerFightingMap.remove(roleId);
			}
		}
	}
	
	@Override
	public void gmHandler(int serverId, long roleId, String[] args) {
		byte opType = Byte.parseByte(args[0]);
		switch (opType) {
		case 1:
			byte num = Byte.parseByte(args[1]);
			if(num>5||num<1){
				num = 5;
			}
			Daily5v5Manager.TEAM_MEMBER_NUM = num;
			break;

		default:
			break;
		}
	}
	
	/**
	 * 定时检测匹配集合
	 */
	public void checkMatching(){
		try {
//			Iterator<Daily5v5MatchingVo> tempIterator = tempMatchingMap.values().iterator();
//			Daily5v5MatchingVo tempVo = null;
//			for(;tempIterator.hasNext();){
//				tempVo = tempIterator.next();
//				if(!matchingMap.containsKey(tempVo.getRoleId())){
//					matchingMap.put(tempVo.getRoleId(), tempVo);
//				}
//			}
			List<MatchFloat> list = Daily5v5Manager.matchFloatMap.get((byte)1);
			Set<Long> keySet = matchingMap.keySet();
			Set<Long> tempSet = new HashSet<>(keySet);
			Iterator<Long> keyiterator = tempSet.iterator();
			int currentTime = DateUtil.getCurrentTimeInt();
			Long roleId = null;
			Daily5v5MatchingVo nowMatchingVo = null;
			int myStartMatchTime = 0;
			int fixIntegral = 0;
			int passTime = 0;
			for(;keyiterator.hasNext();){
				roleId = keyiterator.next();
				try {
					nowMatchingVo = matchingMap.get(roleId);
					if(nowMatchingVo==null) continue;
					myStartMatchTime = nowMatchingVo.getStartMatchTime();
					if(nowMatchingVo.getStep()>1){
						continue;
					}
					passTime = currentTime-myStartMatchTime;
					if(passTime>=Daily5v5Manager.SingleMatchTime){
						//通知客户端
						matchingMap.remove(roleId);
						ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.MATCHING_STATE);
						packet.setMatchingState((byte)2);
						Daily5v5RpcHelper.roleService.send(nowMatchingVo.getServerId(), roleId, packet);
						Daily5v5RpcHelper.roleService().notice(nowMatchingVo.getServerId(), roleId, new Daily5v5CancelMatchingEvent());
						continue;
					}
					fixIntegral = nowMatchingVo.getFixIntegral();
					//匹配成员
					List<Daily5v5MatchingVo> memberList = matchMembers(list, fixIntegral, passTime, roleId, currentTime);
					int memberSize = memberList.size();
					if(memberSize==Daily5v5Manager.TEAM_MEMBER_NUM){
						int mean = 0;
						MatchingTeamVo vo = new MatchingTeamVo();
						int teamId = MatchTeamIdCreator.creatId();
						Daily5v5MatchingVo matchingVo = null;
						for(int i=0;i<memberSize;i++){
							matchingVo = memberList.get(i);
							mean += matchingVo.getFixIntegral()*2/10;
							matchingVo.setTeamId(teamId);
							matchingVo.setStep((byte)2);
							LogUtil.info("daily5v5,进入队伍, roleId:"+matchingVo.getRoleId()+" ,teamId:"+teamId);
						}
						vo.setStartTime(currentTime);
						vo.setMemberList(memberList);
						vo.setIntegral(mean);
						vo.setTeamId(teamId);
						teamMatchMap.put(teamId, vo);
					}
				} catch (Exception e) {
					LogUtil.error("checkMatching fail， roleId:"+roleId, e);
				}
			}
		} catch (Exception e) {
			LogUtil.error("checkMatching fail", e);
		}
	}
	
	/**
	 * 匹配队员
	 * @param list
	 * @param fixIntegral
	 * @param passTime
	 * @param roleId
	 * @param currentTime
	 * @return
	 */
	private List<Daily5v5MatchingVo> matchMembers(List<MatchFloat> list, int fixIntegral, int passTime, long roleId, int currentTime){
		Iterator<Daily5v5MatchingVo> iterator = matchingMap.values().iterator();
		int[] matchFloat = getMatchFloat(list, fixIntegral, passTime);
		LogUtil.info("daily5v5,匹配浮动范围，roleId:"+roleId+", myIntergral:"+fixIntegral+" , min:"+matchFloat[0]+" , max:"+matchFloat[1]
				+" , passTime:"+passTime);
		List<Daily5v5MatchingVo> memberList = new ArrayList<>();
		//先把自己加入
		memberList.add(matchingMap.get(roleId));
		
		Daily5v5MatchingVo matchingVo = null;
		int checkIntegral = 0;
		for(;iterator.hasNext();){
			if(memberList.size()==Daily5v5Manager.TEAM_MEMBER_NUM){
				break;
			}
			matchingVo = iterator.next();
			if(roleId==matchingVo.getRoleId()) continue;
			int startMatchTime = matchingVo.getStartMatchTime();
			int myStep = matchingVo.getStep();
			if(myStep>1){
				continue;
			}
			if((currentTime-startMatchTime)>=Daily5v5Manager.SingleMatchTime){
				//通知客户端
				ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.MATCHING_STATE);
				packet.setMatchingState((byte)2);
				iterator.remove();
				Daily5v5RpcHelper.roleService.send(matchingVo.getServerId(), matchingVo.getRoleId(), packet);
				Daily5v5RpcHelper.roleService().notice(matchingVo.getServerId(), matchingVo.getRoleId(), new Daily5v5CancelMatchingEvent());
				continue;
			}
			//段位分范围检测
			checkIntegral = matchingVo.getFixIntegral();
			if(checkIntegral>=matchFloat[0]&&checkIntegral<=matchFloat[1]){
				memberList.add(matchingVo);
//				matchingVo.setStep((byte)2);
			}
			if(memberList.size()==Daily5v5Manager.TEAM_MEMBER_NUM){
				break;
			}
		}
		return memberList;
	}
	
	//获取匹配值区间
	private int[] getMatchFloat(List<MatchFloat> list, int integral, int time){
		int size = list.size();
		MatchFloat matchFloat = null;
		int[] range;
		for(int i=0;i<size;i++){
			matchFloat = list.get(i);
			range = matchFloat.getRange();
			if(integral>=range[0]&&integral<=range[1]){
				break;
			}
		}
		if(matchFloat==null) return null;
		int mat = -1;
		int tempMat = -1;
		int[] field = null;
		List<int[]> timeFloatList = matchFloat.getTimeFloatList();
		int fSize = timeFloatList.size();
		int[] info;
		for(int i=0;i<fSize;i++){
			info = timeFloatList.get(i);
			tempMat = Math.abs(time-info[0]);
			if(i==0){
				field = new int[]{integral-info[1], integral+info[1]};		
				mat = tempMat;
			}
			if(tempMat<mat){
				mat = tempMat;
				field = new int[]{integral-info[1], integral+info[1]};
			}
			if(tempMat==0) break;
		}
		return field;
	}
	
	/**
	 * 定时检测队伍匹配
	 */
	@Override
	public void checkTeamMatching(){
		try {
			List<MatchFloat> list = Daily5v5Manager.matchFloatMap.get((byte)2);
			LinkedHashSet<MatchingTeamVo> teamSet = new LinkedHashSet<>(teamMatchMap.values());
			Iterator<MatchingTeamVo> iterator = teamSet.iterator();
			int currentTime = DateUtil.getCurrentTimeInt();
			int passTime = 0;
			MatchingTeamVo vo = null;
			MatchingTeamVo checkVo = null;
			MatchingTeamVo enemyTeamVo = null;
			int integral = 0;
			int checkIntegral = 0;
			for(;iterator.hasNext();){
				vo = iterator.next();
				passTime = currentTime-vo.getStartTime();
				if(passTime>=Daily5v5Manager.TEAM_MATCHING_TIME){
					dismissTeam(vo);
					continue;
				}
				if(!teamMatchMap.containsKey(vo.getTeamId())) continue;
				enemyTeamVo = null;
				integral = vo.getIntegral();
				int[] matchFloat = getMatchFloat(list, integral, passTime);
				LogUtil.info("daily5v5,队伍匹配浮动值区间, teamId:"+vo.getTeamId()+" ,min:"+matchFloat[0]+" ,max:"+matchFloat[1]);
				Iterator<MatchingTeamVo> checkInterator = teamMatchMap.values().iterator();
				for(;checkInterator.hasNext();){
					checkVo = checkInterator.next();
					if(checkVo.getTeamId()==vo.getTeamId()) continue;
					passTime = currentTime-checkVo.getStartTime();
					if(passTime>=Daily5v5Manager.TEAM_MATCHING_TIME){
						checkInterator.remove();
						dismissTeam(checkVo);
						continue;
					}
					checkIntegral = checkVo.getIntegral();
					if(checkIntegral>=matchFloat[0]&&checkIntegral<=matchFloat[1]){
						enemyTeamVo = checkVo;
						checkInterator.remove();
						break;
					}
				}
				if(enemyTeamVo!=null){
					//从匹配集合移除
					teamMatchMap.remove(vo.getTeamId());
					//准备战斗处理
//					readyFight(enemyTeamVo);
//					readyFight(vo);
					try {						
						createFight(vo, enemyTeamVo);
					} catch (Exception e) {
						dismissTeam(vo);
						dismissTeam(enemyTeamVo);
						LogUtil.error("createFight fail", e);
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error("checkTeamMatching fail", e);
		}
	}
	
	private void readyFight(MatchingTeamVo vo){
		List<Daily5v5MatchingVo> memberList = vo.getMemberList();
		int size = memberList.size();
		Daily5v5MatchingVo matchingVo = null;
		//生成战斗id
		//通知游戏服切换战斗服   玩家进入战斗
		for(int i=0;i<size;i++){
			matchingVo = memberList.get(i);
//			matchingMap.remove(matchingVo.getRoleId());
//			Daily5v5RpcHelper.daily5v5Service.finishMatching(matchingVo.getServerId(), memberList);
		}
	}
	
	/**
	 * 解散队伍
	 */
	private void dismissTeam(MatchingTeamVo vo){
		teamMatchMap.remove(vo.getTeamId());
		List<Daily5v5MatchingVo> memberList = vo.getMemberList();
		int size = memberList.size();
		Daily5v5MatchingVo matchingVo = null;
		for(int i=0;i<size;i++){
			matchingVo = memberList.get(i);
			matchingVo.setStep((byte)1);
			matchingVo.setTeamId(0);
			LogUtil.info("daily5v5,解散队伍 roleId:"+matchingVo.getRoleId()+" , teamId:"+vo.getTeamId());
		}
	}
	
//	public void createFight(MatchingTeamVo vo, MatchingTeamVo enemyVo){
//		String newFightId = newFightId();
//		StageinfoVo stageVo = SceneManager.getStageVo(Daily5v5Manager.STAGEID);
//		
//		Map<Long, byte[]> enterPacketMap = new HashMap<>();
//		Map<Long, FighterEntity> entityMap = new HashMap<>();
//		//组装队伍1 数据
//		ClientEnterDaily5v5 camp1Packet = new ClientEnterDaily5v5();
//		camp1Packet.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT);
//		camp1Packet.setStageId(Daily5v5Manager.STAGEID);
//		camp1Packet.setLimitTime(Daily5v5Manager.FIGHT_TIME_LIMIT);
//		camp1Packet.setStartRemainderTime(Daily5v5Manager.START_REMIND_TIME);
//		List<FighterEntity> camp1EntityList = new ArrayList<>();
//		camp1Packet.setFighterEntityList(camp1EntityList);
//		camp1Packet.addMonsterVoMap(stageVo.getMonsterVoMap());
//		//修正fighterEntity数据
//		Daily5v5MatchingVo matchingVo = null;
//		FighterEntity entity = null;
//		List<Daily5v5MatchingVo> memberList = vo.getMemberList();
//		int size = memberList.size();
//		for(int i=0;i<size;i++){
//			matchingVo = memberList.get(i);			
//			entity = matchingVo.getEntity();
//			entity.setCamp((byte)1); // 阵营1
//			entity.setFighterType(FighterEntity.TYPE_PLAYER);
//			entity.setPosition(stageVo.getEnemyPos(0)); // 出生点
//			camp1EntityList.add(entity);
//			entityMap.put(matchingVo.getRoleId(), entity);
//		}
//		for(int i=0;i<size;i++){
//			matchingVo = memberList.get(i);	
//			enterPacketMap.put(matchingVo.getRoleId(), PacketUtil.packetToBytes(camp1Packet));
//		}
//		
//		//组装队伍2 数据
//		ClientEnterDaily5v5 camp2Packet = new ClientEnterDaily5v5();
//		camp2Packet.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT);
//		camp2Packet.setStageId(Daily5v5Manager.STAGEID);
//		camp2Packet.setLimitTime(Daily5v5Manager.FIGHT_TIME_LIMIT);
//		camp2Packet.setStartRemainderTime(Daily5v5Manager.START_REMIND_TIME);
//		List<FighterEntity> camp2EntityList = new ArrayList<>();
//		camp2Packet.setFighterEntityList(camp2EntityList);
//		camp2Packet.addMonsterVoMap(stageVo.getMonsterVoMap());
//		//修正fighterEntity数据
//		List<Daily5v5MatchingVo> member2List = enemyVo.getMemberList();
//		int sizeCamp2 = member2List.size();
//		for(int i=0;i<sizeCamp2;i++){
//			matchingVo = member2List.get(i);			
//			entity = matchingVo.getEntity();
//			entity.setCamp((byte)2); // 阵营2
//			entity.setFighterType(FighterEntity.TYPE_PLAYER);
//			entity.setPosition(stageVo.getEnemyPos(1)); // 出生点
//			camp2EntityList.add(entity);
//			entityMap.put(matchingVo.getRoleId(), entity);
//		}
//		for(int i=0;i<sizeCamp2;i++){
//			matchingVo = member2List.get(i);
//			enterPacketMap.put(matchingVo.getRoleId(), PacketUtil.packetToBytes(camp2Packet));
//		}
//		
//		//组装ClientEnterFight参数
//		ClientEnterDaily5v5 enterPacket = new ClientEnterDaily5v5();
//		enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT);
//		enterPacket.setStageId(Daily5v5Manager.STAGEID);
//		enterPacket.setLimitTime(Daily5v5Manager.FIGHT_TIME_LIMIT);
//		enterPacket.setStartRemainderTime(Daily5v5Manager.START_REMIND_TIME);
//		enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
//		
//		// 组装FightHandler参数
//		PhasesPkFightArgs args = new PhasesPkFightArgs();
//		args.setNumOfFighter(size+sizeCamp2);
//		args.setTimeLimitOfInitialPhase(Daily5v5Manager.timeLimitOfInitial);
//		args.setTimeLimitOfClientPreparationPhase(Daily5v5Manager.timeLimitOfPreparation);
//		args.setEntityMap(entityMap);
//		args.setEnterPacketMap(enterPacketMap);
//		Map<Long, FighterEntity> buddyEntityMap = new HashMap<>();
//		args.setBuddyEntityMap(buddyEntityMap);
//		
//		int fightServerId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
//		//组装fightdata
//		Daily5v5FightData fightDate = new Daily5v5FightData();
//		fightDate.setFightServerId(fightServerId);
//		fightDate.setTeam1(vo);
//		fightDate.setTeam2(enemyVo);
//		fightDate.setCreatTimestamp(DateUtil.getCurrentTimeLong());
//		fightMap.put(newFightId, battle);
//		
//		Daily5v5RpcHelper.fightBaseService().createFight(fightServerId, FightConst.T_DAILY_5V5, 
//				MultiServerHelper.getServerId(), newFightId, PacketUtil.packetToBytes(enterPacket), args);
//	}
	
	public void createFight(MatchingTeamVo vo, MatchingTeamVo enemyVo){
		String newFightId = newFightId();
		try {
			int fightServerId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
			vo.setFightId(newFightId);
			enemyVo.setFightId(newFightId);
			Daily5v5Battle battle = createDaily5v5Battle(vo, enemyVo);
			battle.setFightServerId(fightServerId);
			battle.setCreatTimestamp(DateUtil.getCurrentTimeLong());
			fightMap.put(newFightId, battle);
			battle.onInitFight();
		} catch (Exception e) {
			fightMap.remove(newFightId);
			dismissTeam(vo);
			dismissTeam(enemyVo);
			LogUtil.error("createFight fail", e);
		}
	}
	
	private String newFightId(){
		return "Daily5v5-" + MultiServerHelper.getServerId() + "-" + ++idGenerator;
	}
	
	private Daily5v5Battle createDaily5v5Battle(MatchingTeamVo vo, MatchingTeamVo enemyVo){
		Daily5v5Battle battle = new Daily5v5Battle();
		battle.setBattleInfoHandler(new Daily5v5BattleInfoHandler());
		BattleData battleData = new BattleData(vo, enemyVo);
		battle.setBattleData(battleData);
		battle.setTeam1(vo);
		battle.setTeam2(enemyVo);
		
		StageinfoVo stageVo = SceneManager.getStageVo(Daily5v5Manager.STAGEID);
		Map<Long, byte[]> enterPacketMap = new HashMap<>();
		Map<Long, FighterEntity> entityMap = new HashMap<>();
		Map<String, Integer> fighterSeverIdMap = new HashMap<>();
		/* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
		//组装队伍1 数据
		ClientEnterDaily5v5 camp1Packet = new ClientEnterDaily5v5();
		camp1Packet.setFightType(SceneManager.SCENETYPE_DAILY_5V5);
		camp1Packet.setStageId(Daily5v5Manager.STAGEID);
		camp1Packet.setLimitTime(Daily5v5Manager.FIGHT_TIME_LIMIT);
		camp1Packet.setStartRemainderTime(Daily5v5Manager.DYNAMIC_BLOCK_TIME);
		List<FighterEntity> camp1EntityList = new ArrayList<>();
		camp1Packet.setFighterEntityList(camp1EntityList);
		camp1Packet.addMonsterVoMap(stageVo.getMonsterVoMap());
		//修正fighterEntity数据
		Daily5v5MatchingVo matchingVo = null;
		FighterEntity entity = null;
		List<Daily5v5MatchingVo> memberList = vo.getMemberList();
		int size = memberList.size();
		long totalFight1 = 0;
		for(int i=0;i<size;i++){
			matchingVo = memberList.get(i);		
			totalFight1 += matchingVo.getFightValue();
			entity = matchingVo.getEntity();
//			entity.getAttribute().setHp(100);
//			entity.getAttribute().setMaxhp(100);
			entity.setCamp(Daily5v5Manager.CAMP1); // 阵营1
			entity.setFighterType(FighterEntity.TYPE_PLAYER);
			entity.setPosition(stageVo.getPosition()); // 出生点
			entity.setRotation(stageVo.getRotation());
			camp1EntityList.add(entity);
			entityMap.put(matchingVo.getRoleId(), entity);
			fighterSeverIdMap.put(String.valueOf(matchingVo.getRoleId()), matchingVo.getServerId());
			addToFightingMap(matchingVo.getRoleId(), vo.getFightId(), vo.getTeamId());
		}
		LogUtil.info("动态阻挡数据1Elites:{}", blockStatus);
		camp1Packet.setBlockMap(stageVo.getDynamicBlockMap());
		camp1Packet.addBlockStatusMap(blockStatus);
		for(int i=0;i<size;i++){
			matchingVo = memberList.get(i);	
			enterPacketMap.put(matchingVo.getRoleId(), PacketUtil.packetToBytes(camp1Packet));
		}
		
		//组装队伍2 数据
		ClientEnterDaily5v5 camp2Packet = new ClientEnterDaily5v5();
		camp2Packet.setFightType(SceneManager.SCENETYPE_DAILY_5V5);
		camp2Packet.setStageId(Daily5v5Manager.STAGEID);
		camp2Packet.setLimitTime(Daily5v5Manager.FIGHT_TIME_LIMIT);
		camp2Packet.setStartRemainderTime(Daily5v5Manager.DYNAMIC_BLOCK_TIME);
		List<FighterEntity> camp2EntityList = new ArrayList<>();
		camp2Packet.setFighterEntityList(camp2EntityList);
		camp2Packet.addMonsterVoMap(stageVo.getMonsterVoMap());
		//修正fighterEntity数据
		List<Daily5v5MatchingVo> member2List = enemyVo.getMemberList();
		int sizeCamp2 = member2List.size();
		long totalFight2 = 0;
		for(int i=0;i<sizeCamp2;i++){
			matchingVo = member2List.get(i);	
			totalFight2 += matchingVo.getFightValue();
			entity = matchingVo.getEntity();
//			entity.getAttribute().setHp(100);
//			entity.getAttribute().setMaxhp(100);
			entity.setCamp(Daily5v5Manager.CAMP2); // 阵营2
			entity.setFighterType(FighterEntity.TYPE_PLAYER);
			entity.setPosition(stageVo.getEnemyPos(0)); // 出生点
			entity.setRotation(stageVo.getEnemyRot(0));
			camp2EntityList.add(entity);
			entityMap.put(matchingVo.getRoleId(), entity);
			fighterSeverIdMap.put(String.valueOf(matchingVo.getRoleId()), matchingVo.getServerId());
			addToFightingMap(matchingVo.getRoleId(), enemyVo.getFightId(), enemyVo.getTeamId());
		}
		LogUtil.info("动态阻挡数据2Elites:{}", blockStatus);
		camp2Packet.setBlockMap(stageVo.getDynamicBlockMap());
		camp2Packet.addBlockStatusMap(blockStatus);
		for(int i=0;i<sizeCamp2;i++){
			matchingVo = member2List.get(i);
			enterPacketMap.put(matchingVo.getRoleId(), PacketUtil.packetToBytes(camp2Packet));
		}
		
		//组装ClientEnterFight参数
//		ClientEnterDaily5v5 enterPacket = new ClientEnterDaily5v5();
//		enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT);
//		enterPacket.setStageId(Daily5v5Manager.STAGEID);
//		enterPacket.setLimitTime(Daily5v5Manager.FIGHT_TIME_LIMIT);
//		enterPacket.setStartRemainderTime(Daily5v5Manager.START_REMIND_TIME);
//		enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
		
		// 组装FightHandler参数
		PhasesPkFightArgs args = new PhasesPkFightArgs();
		args.setNumOfFighter(size+sizeCamp2);
		args.setTimeLimitOfInitialPhase(Daily5v5Manager.START_REMIND_TIME*1000);
		args.setTimeLimitOfClientPreparationPhase(Daily5v5Manager.START_REMIND_TIME);
		args.setEntityMap(entityMap);
		args.setEnterPacketMap(enterPacketMap);
		Map<Long, FighterEntity> buddyEntityMap = new HashMap<>();
		args.setBuddyEntityMap(buddyEntityMap);
		Daily5v5FightArgs fightArgs = new Daily5v5FightArgs();
		fightArgs.setCreateTimestamp(System.currentTimeMillis());
		fightArgs.setFighterSeverIdMap(fighterSeverIdMap);
		args.setArgs0(fightArgs);
		battleData.setArgs(args);
		battleData.setFighterSeverIdMap(fighterSeverIdMap);
		battleData.setTotalFight1(totalFight1);
		battleData.setTotalFight2(totalFight2);
		return battle;
	}
	
	@Override
	public void rpcOnFightCreated(int mainServerId, String fightId, boolean isOk) {
		if(!fightMap.containsKey(fightId)) return;
		Daily5v5Battle battle = fightMap.get(fightId);
		int fightServerId = battle.getFightServerId();
		int currentTimeInt = DateUtil.getCurrentTimeInt();
		if(isOk){//创建成功   切换连接
			Daily5v5MatchingVo memberVo = null;
			MatchingTeamVo team1 = battle.getTeam1();
			List<Daily5v5MatchingVo> memberList = team1.getMemberList();
			int size = memberList.size();
			MatchingTeamVo team2 = battle.getTeam2();
			List<Daily5v5MatchingVo> memberList2 = team2.getMemberList();
			List<Daily5v5MatchingVo> sendMemberList1 = new ArrayList<>();
			List<Daily5v5MatchingVo> sendMemberList2 = new ArrayList<>();
			sendMemberList1.addAll(memberList);
			sendMemberList2.addAll(memberList2);
			long roleId = 0;
			for(int i=0;i<size;i++){
				memberVo = memberList.get(i);
				roleId = memberVo.getRoleId();
				Daily5v5RpcHelper.daily5v5Service().finishMatching(memberVo.getServerId(), roleId, 
						sendMemberList1, sendMemberList2, fightServerId, currentTimeInt);
				matchingMap.remove(memberVo.getRoleId());
				battle.handleFighterEnter(roleId);
			}
			int size2 = memberList2.size();
			for(int i=0;i<size2;i++){		
				memberVo = memberList2.get(i);
				roleId = memberVo.getRoleId();
				Daily5v5RpcHelper.daily5v5Service.finishMatching(memberVo.getServerId(), roleId, 
						sendMemberList2, sendMemberList1, fightServerId, currentTimeInt);
				matchingMap.remove(memberVo.getRoleId());
				battle.handleFighterEnter(roleId);
			}
		}else{//失败则解散
			fightMap.remove(fightId);
			dismissTeam(battle.getTeam1());
			dismissTeam(battle.getTeam2());
		}
	}
	
	@Override
	public void onFighterAddingSucceeded(int serverId, int fightServerId, String fightId, long roleId){
		Daily5v5Battle daily5v5Battle = fightMap.get(fightId);
		if(daily5v5Battle==null) return;
		if(daily5v5Battle.checkIsFighting(roleId)) return;//在战场中
		if(!continueFightEnter.contains(roleId)){
			return;
		}
		continueFightEnter.remove(roleId);
		daily5v5Battle.handleFighterEnter(roleId);
		byte camp = 0;
		MatchingTeamVo vo = null;
		BattleData battleData = daily5v5Battle.getBattleData();
		if(battleData.getCamp1FighterMap().containsKey(String.valueOf(roleId))){
			camp = Daily5v5Manager.CAMP1;
			vo = daily5v5Battle.getTeam1();
		}else{
			camp = Daily5v5Manager.CAMP2;
			vo = daily5v5Battle.getTeam2();
		}
		long creatTimestamp = daily5v5Battle.getCreatTimestamp();
		long currentTime = System.currentTimeMillis();
		int passTime =(int)((currentTime-creatTimestamp)/1000);
		int fightTimeLimit = Daily5v5Manager.FIGHT_TIME_LIMIT - passTime;
		if(fightTimeLimit<=0){
			return;
		}
		int startRemainderTime = Daily5v5Manager.DYNAMIC_BLOCK_TIME - passTime;
		if(startRemainderTime<0){
			startRemainderTime = 0;
		}
		StageinfoVo stageVo = SceneManager.getStageVo(Daily5v5Manager.STAGEID);
		ClientEnterDaily5v5 campPacket = new ClientEnterDaily5v5();
		campPacket.setFightType(SceneManager.SCENETYPE_DAILY_5V5);
		campPacket.setStageId(Daily5v5Manager.STAGEID);
		campPacket.setLimitTime(fightTimeLimit);
		campPacket.setStartRemainderTime(startRemainderTime);
		List<FighterEntity> campEntityList = new ArrayList<>();
		campPacket.setFighterEntityList(campEntityList);
		campPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
		//修正fighterEntity数据
		Daily5v5MatchingVo matchingVo = null;
		FighterEntity entity = null;
		List<Daily5v5MatchingVo> memberList = vo.getMemberList();
		int size = memberList.size();
		long totalFight1 = 0;
		for(int i=0;i<size;i++){
			matchingVo = memberList.get(i);		
			totalFight1 += matchingVo.getFightValue();
			entity = matchingVo.getEntity();
			entity.setCamp(camp); // 阵营1
			entity.setFighterType(FighterEntity.TYPE_PLAYER);
			entity.setPosition(stageVo.getPosition()); // 出生点
			entity.setRotation(stageVo.getRotation());
			campEntityList.add(entity);
			addToFightingMap(matchingVo.getRoleId(), vo.getFightId(), vo.getTeamId());
		}
		if(startRemainderTime>0){
			/* 动态阻挡数据 */			
			Map<String, Byte> blockStatus = new HashMap<>();
			for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
				blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
				if (dynamicBlock.getShowSpawnId() == 0) {
					blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
				}
			}
			LogUtil.info("动态阻挡数据Elites:{}", blockStatus);
			campPacket.setBlockMap(stageVo.getDynamicBlockMap());
			campPacket.addBlockStatusMap(blockStatus);
		}
		
		Daily5v5RpcHelper.roleService().send(battleData.getFighterSeverIdMap().get(String.valueOf(roleId)), roleId, campPacket);
		Daily5v5RpcHelper.roleService().notice(battleData.getFighterSeverIdMap().get(String.valueOf(roleId)), roleId, new Daily5v5MatchingSuccessEvent(fightServerId));
	}
	
	@Override
	public void handleClientPreloadFinished(int serverId, String fightId, long roleId) {
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null){
			battle.handleClientPreloadFinished(roleId);
		}
	}
	
	@Override
	public void handleUseBuff(int serverId, String fightId, long roleId, int effectId) {
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null){
			battle.handleUseBuff(roleId, effectId);
		}
	}
	
	@Override
	public void handleFightDamage(int serverId, String battleId, String fightId,
			Map<String, HashMap<String, Integer>> damageMap) {
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null&&damageMap!=null){
			Iterator<Entry<String, HashMap<String, Integer>>> iterator = damageMap.entrySet().iterator();
			Entry<String, HashMap<String, Integer>> entry = null;
			for(;iterator.hasNext();){
				entry = iterator.next();
				battle.handleDamage(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public void handleFightDead(int serverId, String battleId, String fightId, Map<String, String> deadMap) {
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null&&deadMap!=null){
			Iterator<Entry<String, String>> iterator = deadMap.entrySet().iterator();
			Entry<String, String> entry = null;
			for(;iterator.hasNext();){
				entry = iterator.next();
				battle.handleDead(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public void handleFighterQuit(int serverId, String fightId, long roleId) {
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null){
			battle.handleFighterQuit(roleId);		
		}
	}
	
	@Override
	public void handleRevive(int serverId, String battleId, String fightId, String fighterUid){
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null){
			battle.handleRevive(fighterUid);			
		}
	}
	
	@Override
	public void handChangeConn(int serverId, String battleId, String fightId, String fighterUid){
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null){			
			battle.handChangeConn(fighterUid);
		}
	}
	
	@Override
	public void handleTimeOut(int serverId, String fightId, HashMap<String, String> hpInfo) {
		Daily5v5Battle battle = fightMap.get(fightId);
		if(battle!=null){			
			battle.handleTimeOut();
		}
	}
	
	public void addToFightingMap(long roleId, String fightId, int teamId){
		this.playerFightingMap.put(roleId, new Daily5v5FightingInfo(fightId, teamId));
	}
	
	public void removeFromFightingMap(long roleId){
		this.playerFightingMap.remove(roleId);
	}
	
	@Override
	public void checkFighitEndTimeOut() {
		Set<String> tempSet = new HashSet<>(fightMap.keySet());
		Iterator<String> iterator = tempSet.iterator();
		String fightId = "";
		Daily5v5Battle daily5v5Battle = null;
		long currentTime = System.currentTimeMillis();
		long creatTimestamp = 0;
		for(;iterator.hasNext();){
			fightId = iterator.next();
			daily5v5Battle = fightMap.get(fightId);
			if(daily5v5Battle==null) continue;
			creatTimestamp = daily5v5Battle.getCreatTimestamp();
			if((currentTime-creatTimestamp)/1000>=(Daily5v5Manager.FIGHT_TIME_LIMIT+Daily5v5Manager.FIX_TIME)){
				daily5v5Battle.handleTimeOut();
				fightMap.remove(fightId);
			}
		}
	}
	
	@Override
	public void endRemoveFight(String fightId){
		fightMap.remove(fightId);
	}
	
	/**
	 * 活动开启
	 */
	@Override
	public void activityStart(){
		//活动开启时 启动匹配检测定时器
		
		SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.Daily5v5, new checkMatchingTask(), 1, 1, TimeUnit.SECONDS);
		
		//检测比赛超时
		SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.Daily5v5, new checkFightTimeOut(), 1, 1, TimeUnit.SECONDS);
	}
	
	/**
	 * 活动结束
	 */
	@Override
	public void activityEnd(){
		//活动结束 关闭定时器
		SchedulerManager.shutDownNow(ExcutorKey.Daily5v5);
	}

	@Override
	public void printState() {
		// TODO Auto-generated method stub
		
	}
	
	class checkMatchingTask implements Runnable{

		@Override
		public void run() {
			ServiceHelper.daily5v5MatchService().checkMatching();
			ServiceHelper.daily5v5MatchService().checkTeamMatching();
		}
		
	}
	
	class checkFightTimeOut implements Runnable{

		@Override
		public void run() {
			ServiceHelper.daily5v5MatchService().checkFighitEndTimeOut();
		}
		
	}
	
	public static void main(String[] args) {
		LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
		for(int i=1;i<=10;i++){
			map.put(i, i);
		}
		Set<Integer> keySet = new HashSet<>(map.keySet());
		Iterator<Integer> iterator = keySet.iterator();
		for(;iterator.hasNext();){
			Integer next = iterator.next();
			if(!map.containsKey(next)){
				System.err.println("移除 了55：：：："+next);
			}
			if(next>5){
				System.err.println("移除：：：："+next);
			}
			Iterator<Integer> iterator2 = map.keySet().iterator();
			for(;iterator2.hasNext();){
				Integer next2 = iterator2.next();
				if(next2>5){
					iterator2.remove();
					System.err.println("移除66：：：："+next2);
				}
			}
		}
	}

}
