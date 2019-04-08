package com.stars.services.familyEscort;

import com.stars.core.persist.DbRowDao;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyEscort.FamilyEscortActivityFlow;
import com.stars.modules.familyEscort.FamilyEscortConst;
import com.stars.modules.familyEscort.FamilyEscortManager;
import com.stars.modules.familyEscort.FamilyEscrotRoleParameter;
import com.stars.modules.familyEscort.event.FamilyEscortEnterPKEvent;
import com.stars.modules.familyEscort.event.FamilyEscortFlowEvent;
import com.stars.modules.familyEscort.packet.ClientEscTimeTips;
import com.stars.modules.familyEscort.packet.ClientFamilyEscortList;
import com.stars.modules.familyEscort.packet.ClientFamilyEscortMainUI;
import com.stars.modules.familyEscort.packet.ClientFamilyEscortSceneInfo;
import com.stars.modules.familyEscort.prodata.FamilyEscortConfig;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightArgs;
import com.stars.network.PacketUtil;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.login.packet.ClientWarning;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyConst;
import com.stars.services.familyEscort.route.EscortCar;
import com.stars.services.familyEscort.userdata.RoleFamilyEscortData;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyRankPo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FamilyEscortServiceActor extends ServiceActor implements FamilyEscortService {

	public static volatile int flowState = FLOW_STATE_END; // 流程状态

	public static volatile long startTimestamp = 0; // 开始时间戳
	public static volatile long endTimestamp = 0; // 结束时间戳

	public static volatile long clearTimestamp = 0; // 清场时间戳

	public static int SHOW_MAX_LIST = 100;// 运镖列表显示上限

	/**
	 * 家族ID：家族信息
	 */
	private Map<Long, FamilyRankPo> familyFightScoreMap; // 家族战力排行榜
	private Map<String, FamilyEscortFightData> fightDataMap = new HashMap<>(); // 1v1战斗数据
	private long idGenerator = 0L; // fightId的自增序列生成器

	private Map<Long, FamilyEscortMap> familyEscorts = new HashMap<>();

	public static int runInterval = 1;// 检测间隔

	// 运镖次数列表
	private Map<Long, RoleFamilyEscortData> dailyEscortCountMap = new HashMap<>();

	private DbRowDao dao = new DbRowDao("rolefamilyescort", DBUtil.DB_COMMON);

	public FamilyEscortMap getFamilyEscortMap(long familyId) {
		return familyEscorts.get(familyId);
	}

	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd("familyEscort", this);

		SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyEscort, new SchedulerTask(), runInterval, runInterval,
				TimeUnit.SECONDS);

		loadUserData();
	}

	@Override
	public void printState() {

	}

	/**
	 * 每日重置
	 */
	public void dailyReset() {
		try {
			dao.flush();
			DBUtil.execSql(DBUtil.DB_COMMON, "delete from rolefamilyescort");
			dailyEscortCountMap = new HashMap<>();
			clearCache();
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}

	public void clearCache() {
		familyEscorts = new HashMap<>();
		fightDataMap = new HashMap<>();
		familyFightScoreMap = null;
	}

	/**
	 * 获取运镖次数
	 * 
	 * @param roleId
	 * @return
	 */
	public int getEscount(long roleId) {
		RoleFamilyEscortData fed = dailyEscortCountMap.get(roleId);
		if (fed != null) {
			return fed.getEscortTime();
		}
		return 0;
	}

	/**
	 * 获取劫镖次数
	 * 
	 * @param roleId
	 * @return
	 */
	public int getRobcount(long roleId) {
		RoleFamilyEscortData fed = dailyEscortCountMap.get(roleId);
		if (fed != null) {
			return fed.getRobTime();
		}
		return 0;
	}

	public void addCount(long roleId, int escortCount, int robCount) {
		RoleFamilyEscortData fed = dailyEscortCountMap.get(roleId);
		if (fed == null) {
			fed = new RoleFamilyEscortData();
			fed.setRoleId(roleId);
			fed.setEscortTime(escortCount);
			fed.setRobTime(robCount);
			fed.setInsertStatus();
			dao.insert(fed);
			dailyEscortCountMap.put(roleId, fed);
		} else {
			fed.setEscortTime(escortCount + fed.getEscortTime());
			fed.setRobTime(robCount + fed.getRobTime());
			fed.setUpdateStatus();
			dao.update(fed);
		}
	}

	long lastSaveTime = 0;
	public static long SAVE_INTERVAL = 1 * 60 * 1000;

	public void saveUserData() {
		long now = System.currentTimeMillis();
		if (now - lastSaveTime < SAVE_INTERVAL) {
			return;
		}
		lastSaveTime = now;
		dao.flush();
	}

	public void loadUserData() throws Throwable {
		Map<Long, RoleFamilyEscortData> tmpDailyEscortCountMap = new HashMap<>();
		tmpDailyEscortCountMap = DBUtil.queryMap(DBUtil.DB_COMMON, "roleid", RoleFamilyEscortData.class,
				"select * from rolefamilyescort");
		dailyEscortCountMap = tmpDailyEscortCountMap;
	}

	@Override
	public void offline(long enterFamilyId, long roleFamilyId, long roleId) {
		FamilyEscortMap em = familyEscorts.get(enterFamilyId);
		if (em == null)
			return;
		EscortCar ec = em.getEscortCar(roleId);
		long now = System.currentTimeMillis();
		if (ec != null) {
			ec.setOffLineTime(now);
		}
		FamilyEscortRoleData fd = em.getFightData(roleId);
		if (fd != null) {
			fd.setOffLineTime(now);
		}
	}

	public void reconnect(long enterFamilyId, long roleFamilyId, long roleId) {
		FamilyEscortMap em = familyEscorts.get(enterFamilyId);
		if (em == null)
			return;
		EscortCar ec = em.getEscortCar(roleId);
		if (ec != null) {
			ec.setOffLineTime(0);
		}
		FamilyEscortRoleData fd = em.getFightData(roleId);
		if (fd != null) {
			fd.setOffLineTime(0);
		}
	}

	public int getCarStar(long roleId, long familyId) {
		FamilyEscortMap em = familyEscorts.get(familyId);
		if (em == null)
			return 0;
		EscortCar ec = em.getEscortCar(roleId);
		if (ec == null)
			return 0;
		return ec.getStarLv();
	}

	public void extendExistScene(long enterFamilyId, long roleFamilyId, long roleId) {
		FamilyEscortMap fem = familyEscorts.get(enterFamilyId);
		if (fem == null)
			return;
		fem.exitEscortMap(roleId);
	}

	public int[] getEscortRandomBornPostion() {
		List<int[]> bornList = FamilyEscortConfig.config.getEscortBornPosList();
		if (bornList == null || bornList.size() <= 0) {
			return null;
		}
		Random random = new Random(System.currentTimeMillis());
		return bornList.get(random.nextInt(bornList.size()));
	}

	public int[] getLootRandomBornPostion() {
		List<int[]> bornList = FamilyEscortConfig.config.getRobBornPosList();
		if (bornList == null || bornList.size() <= 0) {
			return null;
		}
		Random random = new Random(System.currentTimeMillis());
		return bornList.get(random.nextInt(bornList.size()));
	}

	/**
	 * 进入运镖场景的额外处理
	 * 
	 * @param enterFamilyId
	 * @param roleFamilyId
	 * @param role
	 */
	public void extendEnterScene(FamilyEscrotRoleParameter erp) {

		if (erp.getMyFamlilyId() == erp.getEnterFamilyId()) {
			initFaimliyEscortMap(erp.getFamilyAuth());
		}

		long enterFamilyId = erp.getEnterFamilyId();
		long roleFamilyId = erp.getMyFamlilyId();
		FighterEntity role = erp.getFe();
		long roleId = role.getRoleId();
		int safeId = FamilyEscortConfig.config.getSafeStageId();
		SafeinfoVo curSafeinfoVo = SceneManager.getSafeVo(safeId);
		ClientFamilyEscortSceneInfo packet = new ClientFamilyEscortSceneInfo();
		packet.setStageId(curSafeinfoVo.getSafeId());
		packet.setStageType(curSafeinfoVo.getType());

		if (erp.getOldPostion() != null) {
			packet.setBornPos(StringUtil.toPositionByArray(erp.getOldPostion()));
		} else {
			// 进入场景出生点控制
			int[] randomPos = erp.getRandomPos();
			if (randomPos == null) {
				packet.setBornPos(curSafeinfoVo.getCharPosition());
			} else {
				packet.setBornPos(StringUtil.toPositionByArray(randomPos));
			}
		}
		packet.setTranferInfo(curSafeinfoVo.getTransfer());

		long remainTime = FamilyEscortServiceActor.endTimestamp - System.currentTimeMillis();

		remainTime = remainTime > 0 ? remainTime : 0;
		remainTime = remainTime / 1000;

		long clearTime = FamilyEscortServiceActor.clearTimestamp - System.currentTimeMillis();
		clearTime = clearTime > 0 ? clearTime : 0;
		clearTime = clearTime / 1000;

		int leftEsCount = FamilyEscortConfig.config.getEscortTime() - getEscount(roleId);
		leftEsCount = leftEsCount < 0 ? 0 : leftEsCount;

		int robCount = getRobcount(roleId);

		int leftRobCount = FamilyEscortConfig.config.getRobTime() - robCount;
		leftRobCount = leftRobCount < 0 ? 0 : leftRobCount;

		int leftRobBaseCount = FamilyEscortConfig.config.getRobBaseAwardMaxTime() + FamilyEscortConfig.config.getRobTime()
				- robCount;
		leftRobBaseCount = leftRobBaseCount < 0 ? 0 : leftRobBaseCount;

		packet.setSceneRemainTime((int) remainTime);
		if (roleFamilyId == enterFamilyId) {
			packet.setStartCount(getCarStar(roleId, enterFamilyId));
		} else {
			packet.setStartCount(10);
		}
		packet.setEnterFamilyId(enterFamilyId + "");
		packet.setLeftEsCount(leftEsCount);
		packet.setSceneClearTime((int) clearTime);
		packet.setLeftRobCount(leftRobCount);
		packet.setLeftRobBaseCount(leftRobBaseCount);
		PacketManager.send(roleId, packet);

		// 进入
		FamilyEscortMap fem = familyEscorts.get(enterFamilyId);
		if (fem == null)
			return;
		fem.enterEscortMap(erp.getServerId(), role.getRoleId(), roleFamilyId, role);

	}

	public void extendExistSceneFromFight(long enterFamily, long roleId) {
		FamilyEscortMap fem = familyEscorts.get(enterFamily);
		if (fem == null)
			return;
		FamilyEscortRoleData erd = fem.getFightData(roleId);
		if (erd == null)
			return;
		fem.onFireEndAfterEnterEscortScene(roleId, erd.getLastFightWin());
	}

	public void openEscortUI(FighterEntity role) {
		long roleId = role.getRoleId();
		ClientFamilyEscortMainUI cFamilyEscortCars = new ClientFamilyEscortMainUI();

		int leftEsCount = FamilyEscortConfig.config.getEscortTime() - getEscount(roleId);
		leftEsCount = leftEsCount < 0 ? 0 : leftEsCount;

		int robCount = getRobcount(roleId);
		int leftRobCount = FamilyEscortConfig.config.getRobTime() - robCount;
		leftRobCount = leftRobCount < 0 ? 0 : leftRobCount;

		int leftRobBaseCount = FamilyEscortConfig.config.getRobBaseAwardMaxTime() + FamilyEscortConfig.config.getRobTime()
				- robCount;
		leftRobBaseCount = leftRobBaseCount < 0 ? 0 : leftRobBaseCount;

		cFamilyEscortCars.setLeftRobBaseCount(leftRobBaseCount);
		cFamilyEscortCars.setEs_count(leftEsCount);
		cFamilyEscortCars.setRob_count(leftRobCount);

		cFamilyEscortCars.setEs_award(FamilyEscortConfig.config.getEscortShowAward());
		cFamilyEscortCars.setRob_award(FamilyEscortConfig.config.getRobShowAward());

		PacketManager.send(roleId, cFamilyEscortCars);
	}

	/**
	 * 开始运镖
	 */
	@Override
	public void actEscort(FamilyAuth fAuth, FighterEntity role) {
		long roleId = role.getRoleId();
		if (!isCanEscort(roleId)) {
			return;
		}
		int nowScortTime = getEscount(roleId);
		if (nowScortTime >= FamilyEscortConfig.config.getEscortTime()) {
			PacketManager.send(roleId, new ClientText("今天运镖次数已用完"));
			return;
		}
		FamilyEscortMap familyEscortMap = familyEscorts.get(fAuth.getFamilyId());

		// 当前是否还在运镖
		EscortCar car = familyEscortMap.getEscortCar(roleId);
		if (car != null) {
			if (car.isFinished()) {
				familyEscortMap.destroyACar(roleId);
			} else {
				PacketManager.send(roleId, new ClientText("当前运镖还未结束"));
				return;
			}
		}
		FamilyEscortRoleData roleData = familyEscortMap.getFightData(roleId);
		if (roleData.isFighting()) {
			PacketManager.send(roleId, new ClientText("正在战斗中，不能发起运镖"));
			return;
		}
		LogUtil.info("actEscort " + roleId + "|" + nowScortTime);
		addCount(roleId, 1, 0);
		familyEscortMap.initFightData(roleId, role);
		familyEscortMap.actEscort(role, nowScortTime + 1);
	}

	/**
	 * 初始化家族运镖场景（数据相关）
	 * 
	 * @param serverId
	 * @param fAuth
	 */
	public void initFaimliyEscortMap(FamilyAuth fAuth) {
		long familyId = fAuth.getFamilyId();
		FamilyEscortMap familyEscortMap = familyEscorts.get(familyId);
		if (familyEscortMap == null) {
			int rank = 101;
			if (familyFightScoreMap != null) {
				FamilyRankPo familyRank = familyFightScoreMap.get(familyId);
				if (familyRank != null) {
					rank = familyRank.getRank();
				}
			}
			EscortFamily familyInfo = new EscortFamily(fAuth.getFamilyName(), familyId, rank);
			familyEscortMap = new FamilyEscortMap(this, familyInfo);
			familyEscorts.put(familyId, familyEscortMap);
		}
	}

	/**
	 * 加入运镖地图
	 */
	@Override
	public void joinActEscort(int serverId, FamilyAuth fAuth, FighterEntity entity, FighterEntity buddyEntity) {
		if (!isCanJoin(entity.getRoleId())) {
			return;
		}
		FamilyEscortMap familyEscortMap = familyEscorts.get(fAuth.getFamilyId());
		familyEscortMap.joinActEscort(serverId, fAuth, entity, buddyEntity);
	}

	/**
	 * 加入劫镖地图
	 */
	@Override
	public void joinLootEscort(int serverId, String familyId, FighterEntity entity, FighterEntity buddyEntity) {
		if (!isCanJoin(entity.getRoleId())) {
			return;
		}
		FamilyEscortMap familyEscortMap = familyEscorts.get(Long.parseLong(familyId));
		if (familyEscortMap == null) {
			PacketManager.send(entity.getRoleId(), new ClientWarning("目标家族不存在"));
			return;
		}
		familyEscortMap.joinLootEscort(serverId, entity, buddyEntity);
	}

	/**
	 * 开始劫镖
	 */
	@Override
	public void lootEscort(String familyId, String self, String aim) {
		if (!isCanJoin(Long.parseLong(self))) {
			return;
		}
		FamilyEscortMap familyEscortMap = familyEscorts.get(Long.parseLong(familyId));
		if (familyEscortMap == null) {
			return;
		}
		familyEscortMap.lootEscort(self, aim);
	}

	@Override
	public void removeBarrier(long familyId, long roleId) {
		FamilyEscortMap familyEscortMap = familyEscorts.get(familyId);
		if (familyEscortMap == null) {
			return;
		}
		familyEscortMap.removeBarrier(roleId);
	}

	@Override
	public void prepare() {
		flowState = FLOW_STATE_PREPARE;
		startTimestamp = FamilyEscortManager.flow.nextValidTime(FamilyEscortActivityFlow.STEP_START);
		endTimestamp = FamilyEscortManager.flow.nextValidTime(FamilyEscortActivityFlow.STEP_END);
		clearTimestamp = FamilyEscortManager.flow.nextValidTime(FamilyEscortActivityFlow.STEP_CLEARUP);
		if (startTimestamp > endTimestamp) {
			startTimestamp = System.currentTimeMillis();
		}
		loadFamilyRank();
		ServiceHelper.roleService().noticeAll(new FamilyEscortFlowEvent()); // 主界面入口

	}

	@Override
	public void start() {
		flowState = FLOW_STATE_START;
		ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_ESORT,
				FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "");
		ServiceHelper.roleService().noticeAll(new FamilyEscortFlowEvent()); // 主界面入口
		// 单向锁家族
		long clearUpTimestamp = FamilyEscortManager.flow.nextValidTime(FamilyEscortActivityFlow.STEP_CLEARUP);
		long timeout = clearUpTimestamp - startTimestamp;
		if (timeout < 0) {
			timeout = clearUpTimestamp - System.currentTimeMillis();
		}
		ServiceHelper.familyMainService().lockGlobalUnidirect(timeout);
	}

	private void loadFamilyRank() {
		List<AbstractRankPo> rankList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_FAMILYFIGHTSCORE, 100);
		familyFightScoreMap = new HashMap<>();
		for (AbstractRankPo rankPo : rankList) {
			FamilyRankPo po = (FamilyRankPo) rankPo;
			familyFightScoreMap.put(po.getFamilyId(), po);
		}
	}

	/**
	 * 押镖结束，不能再发起押镖
	 */
	public void escortEnd() {
		flowState = FLOW_STATE_ESCORTEND;
		escortTimeTips();
		ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_ESORT, FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "");
		ServiceHelper.roleService().noticeAll(new FamilyEscortFlowEvent()); // 主界面入口
	}

	/**
	 * 运镖时间点刷新
	 */
	public void escortTimeTips() {
		try {
			long remainTime = FamilyEscortServiceActor.endTimestamp - System.currentTimeMillis();

			remainTime = remainTime > 0 ? remainTime : 0;
			remainTime = remainTime / 1000;

			long clearTime = FamilyEscortServiceActor.clearTimestamp - System.currentTimeMillis();
			clearTime = clearTime > 0 ? clearTime : 0;
			clearTime = clearTime / 1000;

			ClientEscTimeTips escTimeTips = new ClientEscTimeTips((int) remainTime, (int) clearTime);

			for (FamilyEscortMap fem : familyEscorts.values()) {
				fem.sendPacketToPlayerNoFight(escTimeTips);
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}

	/**
	 * 战斗结束，不能再发起战斗
	 */
	@Override
	public void end() {
		flowState = FLOW_STATE_END;
	}

	public boolean isStart() {
		return flowState == FLOW_STATE_START || flowState == FLOW_STATE_ESCORTEND;
	}

	public boolean isCanJoin(long roleId) {
		if (isStart()) {
			return true;
		}
		PacketManager.send(roleId, new ClientWarning("活动已结束"));
		return false;
	}

	public boolean isCanEscort(long roleId) {
		if (flowState == FLOW_STATE_START) {
			return true;
		}
		PacketManager.send(roleId, new ClientWarning("活动已结束"));
		return false;
	}

	@Override
	public void clearup() {
		// 清场处理
		try {
			if (familyEscorts != null) {
				for (FamilyEscortMap fMap : familyEscorts.values()) {
					try {
						fMap.clearUp();
					} catch (Throwable e) {
						LogUtil.error(e.getMessage(), e);
					}
				}
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
		try {
			clearCache();
			// 单向解锁家族
			ServiceHelper.familyMainService().unlockGlobalUnidirect();
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}

	public void updateFlushArroundPlayerList(long roleId, Object sceneMsg, List<Long> list) {
		if (sceneMsg == null)
			return;
		long familyId = (long) sceneMsg;
		if (familyId <= 0)
			return;
		FamilyEscortMap fem = getFamilyEscortMap(familyId);
		if (fem == null)
			return;
		fem.updateArroudPlayerList(roleId, list);
	}

	public void createFight(FamilyEscortRoleData attackerData, FamilyEscortRoleData defenderData, long attackerFamilyId,
			long defenderFamilyId, long enterFamilyId) {
		FighterEntity attacker = attackerData.getEntity();
		FighterEntity attackerBuddy = attackerData.getBuddyEntity();
		FighterEntity defender = defenderData.getEntity();
		FighterEntity defenderBuddy = defenderData.getBuddyEntity();
		String fightId = newFightId();
		int fightServerId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
		long attackerId = Long.parseLong(attacker.getUniqueId());
		long defenderId = Long.parseLong(defender.getUniqueId());

		// 修正fighterEntity数据
		StageinfoVo stageVo = SceneManager.getStageVo(FamilyEscortConfig.config.getStageId());
		attacker.setCamp((byte) 1); // 阵营1
		attacker.setFighterType(FighterEntity.TYPE_PLAYER);
		attacker.setPosition(stageVo.getEnemyPos(0)); // 出生点
		if (attackerBuddy != null) {
			attackerBuddy.setCamp((byte) 1); // 阵营1
			attackerBuddy.setFighterType(FighterEntity.TYPE_ROBOT);
			attackerBuddy.setPosition(stageVo.getEnemyPos(0)); // 出生点
		}
		defender.setCamp((byte) 2); // 阵营2
		defender.setFighterType(FighterEntity.TYPE_PLAYER);
		defender.setPosition(stageVo.getEnemyPos(1)); // 出生点
		if (defenderBuddy != null) {
			defenderBuddy.setCamp((byte) 2); // 阵营2
			defenderBuddy.setFighterType(FighterEntity.TYPE_PLAYER);
			defenderBuddy.setPosition(stageVo.getEnemyPos(1)); // 出生点
		}

		Map<Long, byte[]> enterPacketMap = new HashMap<>();
		StageinfoVo info = SceneManager.getStageVo(FamilyEscortConfig.config.getStageId());
		// 组装attacker的数据
		ClientFamilyEscortEnterPK attackerEnterPacket = new ClientFamilyEscortEnterPK();
		attackerEnterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_ESCORT_PVP_SCENE);
		attackerEnterPacket.setStageId(FamilyEscortConfig.config.getStageId());
		attackerEnterPacket.setBlockMap(info.getDynamicBlockMap());
		attackerEnterPacket.setLimitTime(FamilyEscortConst.timeLimitOf1v1);
		attackerEnterPacket.setIsAttacker((byte) 1);
		attackerEnterPacket.setCountdownOfBegin(FamilyEscortConst.timeLimitOf1v1Countdown);
		List<FighterEntity> attackerEntityList = new ArrayList<>();
		attackerEntityList.add(attacker);
		if (attackerBuddy != null) {
			attackerEntityList.add(attackerBuddy);
		}
		attackerEnterPacket.setFighterEntityList(attackerEntityList);
		// attackerEnterPacket.setSkillVoMap(FamilyWarUtil.getAllRoleSkillVoMap());
		enterPacketMap.put(Long.parseLong(attacker.getUniqueId()), PacketUtil.packetToBytes(attackerEnterPacket));
		// 组装defender的数据
		ClientFamilyEscortEnterPK defenderEnterPacket = new ClientFamilyEscortEnterPK();
		defenderEnterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_ESCORT_PVP_SCENE);
		defenderEnterPacket.setStageId(FamilyEscortConfig.config.getStageId());
		defenderEnterPacket.setBlockMap(info.getDynamicBlockMap());
		defenderEnterPacket.setLimitTime(FamilyEscortConst.timeLimitOf1v1);
		defenderEnterPacket.setIsAttacker((byte) 0);
		defenderEnterPacket.setCountdownOfBegin(FamilyEscortConst.timeLimitOf1v1Countdown);
		List<FighterEntity> defenderEntityList = new ArrayList<>();
		defenderEntityList.add(defender);
		if (defenderBuddy != null) {
			defenderEntityList.add(defenderBuddy);
		}
		defenderEnterPacket.setFighterEntityList(defenderEntityList);
		// defenderEnterPacket.setSkillVoMap(FamilyWarUtil.getAllRoleSkillVoMap());
		enterPacketMap.put(Long.parseLong(defender.getUniqueId()), PacketUtil.packetToBytes(defenderEnterPacket));
		// 组装ClientEnterFight参数（服务端
		ClientFamilyEscortEnterPK enterPacket = new ClientFamilyEscortEnterPK();
		enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_ESCORT_PVP_SCENE);
		enterPacket.setStageId(FamilyEscortConfig.config.getStageId());
		enterPacket.setBlockMap(info.getDynamicBlockMap());
		enterPacket.setLimitTime(FamilyEscortConst.timeLimitOf1v1);
		enterPacket.setCountdownOfBegin(FamilyEscortConst.timeLimitOf1v1Countdown);
		// enterPacket.setSkillVoMap(FamilyWarUtil.getAllRoleSkillVoMap());

		// 组装FightHandler参数
		PhasesPkFightArgs args = new PhasesPkFightArgs();
		args.setNumOfFighter(2);
		args.setTimeLimitOfInitialPhase(FamilyEscortConst.timeLimitOf1v1Initial);
		args.setTimeLimitOfClientPreparationPhase(FamilyEscortConst.timeLimitOf1v1Preparation);
		Map<Long, FighterEntity> entityMap = new HashMap<>();
		entityMap.put(Long.parseLong(attacker.getUniqueId()), attacker);
		entityMap.put(Long.parseLong(defender.getUniqueId()), defender);
		Map<Long, FighterEntity> buddyEntityMap = new HashMap<>();
		if (attackerBuddy != null) {
			buddyEntityMap.put(Long.parseLong(attacker.getUniqueId()), attackerBuddy);
		}
		if (defenderBuddy != null) {
			buddyEntityMap.put(Long.parseLong(defender.getUniqueId()), defenderBuddy);
		}
		args.setEntityMap(entityMap);
		args.setBuddyEntityMap(buddyEntityMap);
		args.setEnterPacketMap(enterPacketMap);
		// 创建战斗
		MainRpcHelper.fightBaseService().createFight(fightServerId, FightConst.T_FAMILY_ESCORT, MultiServerHelper.getServerId(),
				fightId, PacketUtil.packetToBytes(enterPacket), args);
		// 组装FamilyEscortFightData
		long now = System.currentTimeMillis();
		FamilyEscortFightData data = new FamilyEscortFightData();
		data.setFightId(fightId);
		data.setFightServerId(fightServerId);
		data.setAttackerId(attackerId);
		data.setAttackerFamilyId(attackerFamilyId);
		data.setDefenderId(defenderId);
		data.setDefenderFamilyId(defenderFamilyId);
		data.setEnterFamilyId(enterFamilyId);
		fightDataMap.put(fightId, data);
		// 设置相关状态
		attackerData.setFightIdAndStartTimestamp(fightId, now);
		defenderData.setFightIdAndStartTimestamp(fightId, now);

		LogUtil.info("createFight escort " + attackerId + "|" + defenderId + "|" + fightId);

		ServiceHelper.roleService().notice(attackerData.getServerId(), attackerData.getRoleId(),
				new FamilyEscortEnterPKEvent(defenderFamilyId));
		ServiceHelper.roleService().notice(defenderData.getServerId(), defenderData.getRoleId(),
				new FamilyEscortEnterPKEvent(defenderFamilyId));

	}

	private String newFightId() {
		return "FamilyEscort-" + MultiServerHelper.getServerId() + "-" + ++idGenerator;
	}

	@Override
	public void rpcOnFightCreated(int mainServerId, String fightId, boolean isOk) {
		LogUtil.info("家族运镖|战斗创建回调|fightId:{}|isOk:{}", fightId, isOk);
		FamilyEscortFightData data = fightDataMap.get(fightId);
		if (data != null) {
			if (isOk) { // 创建成功，切换连接
				MultiServerHelper.modifyConnectorRoute(data.getAttackerId(), data.getFightServerId());
				MultiServerHelper.modifyConnectorRoute(data.getDefenderId(), data.getFightServerId());
			} else { // 创建失败，移除战斗
				fightDataMap.remove(fightId);
				FamilyEscortMap fMap = familyEscorts.get(data.getEnterFamilyId());
				if (fMap == null)
					return;
				fMap.clearFightState(data.getAttackerId());
				fMap.clearFightState(data.getDefenderId());
				fMap.clearCarState(data.getAttackerId());
				fMap.clearCarState(data.getDefenderId());
			}
		}
	}

	@Override
	public void rpcOnFightEnd(int mainServerId, int fightServerId, String fightId, long loserId) {
		LogUtil.info("家族运镖|战斗结束|fightId:{}|loserId:{}", fightId, loserId);
		FamilyEscortFightData fightData = fightDataMap.remove(fightId);
		if (fightData == null) {
			return;
		}
		// 停止
		try {
			MainRpcHelper.fightBaseService().stopFight(fightServerId, FightConst.T_FAMILY_ESCORT, mainServerId, fightId);
		} catch (Exception e) {
			LogUtil.error("家族运镖战斗停止报错", e);
		}
		// 业务层处理
		// todo:
		FamilyEscortMap fEscortMap = familyEscorts.get(fightData.getEnterFamilyId());
		if (fEscortMap != null) {
			// 劫镖胜利则扣除劫镖次数

			// 已经劫镖成功的次数
			int robCount = getRobcount(fightData.getAttackerId());

			int leftRobCount = FamilyEscortConfig.config.getRobTime() - getRobcount(fightData.getAttackerId());
			leftRobCount = leftRobCount < 0 ? 0 : leftRobCount;

			boolean isRobWin = fightData.getDefenderId() == loserId;

			if (fightData.getDefenderId() == loserId && fEscortMap.getEscortCar(loserId) != null) {
				addCount(fightData.getAttackerId(), 0, 1);
			} else {
				// 代表没奖励
				robCount = -1;
			}

			fEscortMap.onFightEnd(fightData.getAttackerId(), fightData.getDefenderId(), robCount, isRobWin);
		}
	}

	@Override
	public void showEscortList(long famliyId, FighterEntity role) {
		if (!isCanJoin(role.getRoleId())) {
			return;
		}
		List<FamilyEscortMap> tempList = new LinkedList<>();
		if (this.familyEscorts != null && this.familyEscorts.size() > 0) {
			for (FamilyEscortMap fem : this.familyEscorts.values()) {
				if (fem.getFamilyInfo().getFamilyId() == famliyId)
					continue;
				tempList.add(fem);
			}
		}
		// 如果超过上限，则排序，下发时会按照最大限制处理
		if (tempList.size() > SHOW_MAX_LIST) {
			Collections.sort(tempList);
		}
		ClientFamilyEscortList fel = new ClientFamilyEscortList();
		fel.setFeList(tempList);
		PacketManager.send(Long.parseLong(role.getUniqueId()), fel);
	}

	public void runUpdate() {
		try {
			Collection<FamilyEscortMap> col = familyEscorts.values();
			for (FamilyEscortMap familyEscortMap : col) {
				familyEscortMap.checkEscortCarStatus();
			}
			// todo: 检查战斗的是否超时，如果超时就关掉
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
		try {
			saveUserData();
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}

	}

	@Override
	public void killRole(long familyId, long selfRoleId, long aimRoleId) {
		if (!isCanJoin(selfRoleId)) {
			return;
		}
		FamilyEscortMap fEscortMap = familyEscorts.get(familyId);
		if (fEscortMap == null) {
			PacketManager.send(selfRoleId, new ClientWarning("找不到对应家族"));
			return;
		}
		fEscortMap.killRole(selfRoleId, aimRoleId);
	}

	public Map<Long, RoleFamilyEscortData> getDailyEscortCountMap() {
		return dailyEscortCountMap;
	}

	public void setDailyEscortCountMap(Map<Long, RoleFamilyEscortData> dailyEscortCountMap) {
		this.dailyEscortCountMap = dailyEscortCountMap;
	}

	public DbRowDao getDao() {
		return dao;
	}

	public void setDao(DbRowDao dao) {
		this.dao = dao;
	}

	class SchedulerTask implements Runnable {
		@Override
		public void run() {
			ServiceHelper.familyEscortService().runUpdate();
		}
	}
}
