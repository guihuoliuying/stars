package com.stars.modules.scene;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.familyactivities.invade.FamilyInvadeModule;
import com.stars.modules.gamecave.GameCaveManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.event.BackCityEvent;
import com.stars.modules.scene.event.EnterSceneEvent;
import com.stars.modules.scene.event.RoleReviveEvent;
import com.stars.modules.scene.event.TalkWithNpcEvent;
import com.stars.modules.scene.packet.ClientCampVo;
import com.stars.modules.scene.packet.ClientDrama;
import com.stars.modules.scene.packet.ClientRoleRevive;
import com.stars.modules.scene.packet.ServerAreaSpawnMonster;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.prodata.*;
import com.stars.modules.scene.userdata.RoleDrama;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.tool.ToolModule;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;


/**
 * Created by liuyuheng on 2016/7/18.
 */
public class SceneModule extends AbstractModule {
    /**
     * 最多持有一个Scene实例
     */
    private Scene scene = null;
    private byte lastSceneType;
    private int lastSceneId;
    private boolean repeat = true;  //  防止重复进入场景，提供特殊需求开小灶开关

    // 产品数据下发记录缓存
    private Set<Integer> monsterSendCache = new HashSet<>();
    private Set<Integer> skillSendCache = new HashSet<>();
    private Set<Integer> buffSendCache = new HashSet<>();
    // 播放剧情记录, <类型, <功能Id, po>>
    private Map<String, Map<String, RoleDrama>> dramaPlayedMap = new HashMap<>();
    // 收到死亡怪物UId
    private Set<String> receiveDeadUId;

    private int gmDungeonId;

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `roledrama` where `roleid`=" + id();
        List<RoleDrama> list = DBUtil.queryList(DBUtil.DB_USER, RoleDrama.class, sql);
        Map<String, Map<String, RoleDrama>> map = new HashMap<>();
        for (RoleDrama roleDrama : list) {
            Map<String, RoleDrama> poMap = map.get(roleDrama.getType());
            if (poMap == null) {
                poMap = new HashMap<>();
                map.put(roleDrama.getType(), poMap);
            }
            poMap.put(roleDrama.getParamId(), roleDrama);
        }
        this.dramaPlayedMap = map;
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        lastSceneId = -1;
        lastSceneType = -1;
        repeat = false;
        //不能初始化旧场景，还需要对旧场景做处理
//        scene = null;
    }

    @Override
    public void onReconnect() throws Throwable {
        lastSceneId = -1;
        lastSceneType = -1;
    }

    @Override
    public void onSyncData() throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (this.scene != null) {
            try {
                //登陆时，对旧场景的处理
                this.scene.login(moduleMap(), null);
            } catch (Throwable e) {
                com.stars.util.LogUtil.error(e.getMessage(), e);
            }
        }
        LoginModule lm = module(MConst.Login);
        do {
            if (hasNewGuideDungeon() && roleModule.getLevel() < 2) {
                String isPassNewGuideDungeonValue = roleModule.context().recordMap().getString("isPassNewGuideDungeon");
                if (isPassNewGuideDungeonValue == null) {
                    enterScene(SceneManager.SCENETYPE_NEWGUIDE, 0, "");
                    break;
                }
            }
            SafeinfoVo safeinfoVo = SceneManager.getSafeVo(roleModule.getSafeStageId());
            /**当退出游戏前所在场景为游园场景时，需特殊处理，直接把玩家放到指定主城*/
            if ((byte) safeinfoVo.getType() == SceneManager.SCENETYPE_GAMECAVE) {
                roleModule.updateSafeStageId(GameCaveManager.defaultCityId);
            }

            enterScene(SceneManager.SCENETYPE_CITY, roleModule.getSafeStageId(), "");
        } while (false);
//        if (lm.isCreating()) {
//        } else {
//        }
        lazySendCampVo();
        // 下发剧情播放记录
        sendPlayedDrama(SceneManager.DRAMA_TASK, "all", Boolean.TRUE);
    }

    @Override
    public void onTimingExecute() {
        if (scene instanceof FightScene) {
            ((FightScene) scene).updateTimeExecute(moduleMap());
        }
    }

    public SceneModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("场景", id, self, eventDispatcher, moduleMap);
    }


    public void lazySendCampVo() {
        ClientCampVo packet = new ClientCampVo(SceneManager.campVoMap);
        lazySend(packet);
    }

    /**
     * 城镇场景——>城镇场景(传送)
     * 战斗场景——>城镇场景(回城)
     * 坐标为空时,进入当前角色所在城镇场景
     *
     * @param transferPos
     */
    public void transferSafeStage(String transferPos) {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int curSafeId = roleModule.getSafeStageId();
        if (StringUtil.isEmpty(transferPos)) {// 坐标为空,进入当前角色所在城镇场景
            enterScene(SceneManager.SCENETYPE_CITY, curSafeId, "");
        } else {
            SafeinfoVo safeinfoVo = SceneManager.getSafeVo(curSafeId);
            String[] targetInfo = safeinfoVo.getTransTargetInfo(transferPos);
            if (targetInfo == null) {
                com.stars.util.LogUtil.info("安全区场景safeid={}入口坐标:[{}]获得传送目标配置失败", curSafeId, transferPos);
                warn(I18n.get("scene.transferError"));
                return;
            }
            enterScene(SceneManager.SCENETYPE_CITY, Integer.parseInt(targetInfo[0]), transferPos);
        }
        eventDispatcher().fire(new BackCityEvent());

    }

    /**
     * 根据场景Id获得显示npcList
     *
     * @param sceneId
     * @return
     */
    public List<Integer> displayNpcId(int sceneId) {
        for (NpcInfoVo npcVo : SceneManager.getNpcVoBySceneId(sceneId)) {
            npcVo.getDisplay();
        }
        return null;
    }

    /**
     * 查看是否存在新手副本
     */
    public boolean hasNewGuideDungeon() {
        Map<String, String> defineMap = SceneManager.getFcdMap();
        if (defineMap != null && defineMap.containsKey("newguidedungeon") && defineMap.get("newguidedungeon").equals("0") == false) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 执行npc功能
     *
     * @param npcId
     */
    public void executeNpcFunc(int npcId) {

    }

    /**
     * npc对话,直接抛出事件
     *
     * @param npcId
     */
    public void talkWithNpc(int npcId) {
        eventDispatcher().fire(new TalkWithNpcEvent(npcId));
    }

    /**
     * 怪物死亡处理
     *
     * @param monsterUIdList
     */
    public void monsterDead(List<String> monsterUIdList) {
        // 遍历只处理没接收过的
        List<String> unDeal = new LinkedList<>();
        for (String deadUId : monsterUIdList) {
            if (receiveDeadUId.contains(deadUId))
                continue;
            unDeal.add(deadUId);
        }
        receiveDeadUId.addAll(monsterUIdList);
        if (unDeal.isEmpty())
            return;
        if (scene instanceof FightScene) {
            ((FightScene) scene).enemyDead(moduleMap(), unDeal);
        } else {
            com.stars.util.LogUtil.info("monster=" + monsterUIdList.toString() + " dead but scence is not FightScene,userid=" + id());
        }
    }

    /**
     * 区域触发刷怪
     *
     * @param
     */
    public void areaSpawnMonster(byte sceneType, PlayerPacket packet) {
        if (sceneType == SceneManager.SCENETYPE_TEAMDUNGEON) {
            ServiceHelper.teamDungeonService().receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_ELITEDUNGEON) {
            ServiceHelper.eliteDungeonService().receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_FAMILY_INVADE) {
            FamilyInvadeModule familyInvadeModule = module("family.act.invade");
            familyInvadeModule.receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_ROB_ROBOT) {
            ServiceHelper.escortService().receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_MARRY_DUNGEON) {
            ServiceHelper.teamDungeonService().receiveMarryFightPacket(packet);
        } else {
            if (scene instanceof FightScene) {
                ServerAreaSpawnMonster serverAreaSpawnMonster = (ServerAreaSpawnMonster) packet;
                ((FightScene) scene).areaSpawnMonster(moduleMap(), serverAreaSpawnMonster.getSpawnId());
            }
        }
    }

    /**
     * 玩家角色死亡
     * todo:temp code,服务端计算伤害后死亡状态会由服务控制
     */
    public void roleDead() {
        if (scene instanceof FightScene) {
            ((FightScene) scene).selfDead(moduleMap());
        }
    }

    public void dispatchEvent(Event event) {
        eventDispatcher().fire(event);
    }

    /**
     * 暂停战斗场景计时(剧情/引导需要)
     */
    public void pauseFightTime() {
        if (scene instanceof FightScene) {
            ((FightScene) scene).pauseFightTime();
        }
    }

    /**
     * 开始/继续战斗场景计时
     */
    public void startFightTime(byte sceneType) {
        if (sceneType == SceneManager.SCENETYPE_ELITEDUNGEON) {
            ServiceHelper.eliteDungeonService().startFightTime(id());
        } else if (sceneType == SceneManager.SCENETYPE_CAMP_CITY_FIGHT) {
            ServiceHelper.campCityFightService().startFightTime(id());
        } else {
            if (scene instanceof FightScene) {
                com.stars.network.server.packet.Packet packet = ((FightScene) scene).startFightTime();
                if (packet != null)
                    send(packet);
            }
        }
    }

    public Scene createNewScene(Scene newScene, byte newSceneType, int newSceneId) {
        if (newScene != null)
            return newScene;
        if (scene != null) {
            newScene = scene.createNewScene(newScene, newSceneType, newSceneId);
        }
        if (newScene == null) {
            newScene = SceneManager.newScene(newSceneType);
        }
        return newScene;
    }

    /**
     * 检查新的场景类型
     */
    public byte checkNewSceneType(byte newSceneType, Object extend, Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        if (newSceneType == SceneManager.SCENETYPE_CITY && "".equals(extend)) {
            /** 进入安全区，对类型进行修正，策划希望回城可以到上一次退出的安全区 */
            SafeinfoVo safeinfoVo = SceneManager.getSafeVo(roleModule.getSafeStageId());
            newSceneType = (byte) safeinfoVo.getType();
        }
        return newSceneType;
    }


    public void enterScene(Scene newScene, byte sceneType, int sceneId, Object extend) {
        Scene lastScene = scene;
        byte newSceneType = sceneType;
        // 重复进入安全区场景判断,战斗场景暂时不判断
        try {
            try {
                //上一个场景的退出处理，PS:这里不管是否能进入
                if (lastScene != null) {
                    //临时预知新进的场景
                    Scene tmpNewScene = createNewScene(newScene, sceneType, sceneId);
                    lastScene.extendExit(moduleMap(), tmpNewScene, extend);
                }
            } catch (Throwable e) {
                com.stars.util.LogUtil.error(e.getMessage(), e);
            }
            //是否能重复进入场景
            if (lastScene != null && !lastScene.isCanRepeatEnter(newScene, sceneType, sceneId, extend) && repeat) {
                return;
            }
            repeat = true;
            //退出上一个场景的处理
            if (lastScene != null) {
                lastScene.exit(moduleMap());
            }
            RoleModule roleModule = module(MConst.Role);
            String lastSceneIdStr = roleModule.getJoinSceneStr();
            //回主城特殊处理
            sceneType = checkNewSceneType(newSceneType, extend, moduleMap());


            //创建新的场景
            newScene = createNewScene(newScene, sceneType, sceneId);
            this.scene = newScene;
            newScene.setSceneId(sceneId);
            newScene.setSceneType(sceneType);

            //是否能进入新的场景
            if (!newScene.canEnter(moduleMap(), extend)) {
                //如果不能，要怎么处理
                newScene.cannotEnterNewSceneDo(moduleMap(), extend);
                return;
            }
            //进入场景处理
            newScene.enter(moduleMap(), extend);
            //刷新玩家位置
            newScene.enterAndUpdatePosition(roleModule, sceneId, moduleMap());

            String sceneIdStr = roleModule.getJoinSceneStr();
            this.receiveDeadUId = new HashSet<>();
            this.dispatchEvent(new EnterSceneEvent(sceneType, sceneIdStr, lastSceneType, lastSceneIdStr));
            setLastSceneType(sceneType);
            setLastSceneId(sceneId);
        } finally {
            //新场景的进入处理
            this.scene.extendEnter(moduleMap(), extend);
        }
    }

    public void enterScene(byte sceneType, int sceneId, Object extend) {
        enterScene(null, sceneType, sceneId, extend);
    }

//	public void enterScene(byte sceneType, int sceneId, Object extend) {
//		Scene lastScene = scene;
//		Scene newScene = null;
//		// 重复进入安全区场景判断,战斗场景暂时不判断
//		try {
//			if (!(scene instanceof FightScene) && sceneType == lastSceneType && sceneId == lastSceneId && repeat) {
//				return;
//			}
//			repeat = true;
//			if (scene != null) {
//				scene.exit(moduleMap());
//			}
//			RoleModule roleModule = module(MConst.Role);
//			String lastSceneIdStr = roleModule.getJoinSceneStr();
//			if (sceneType == SceneManager.SCENETYPE_CITY && "".equals(extend)) {
//				/** 进入安全区，对类型进行修正，策划希望回城可以到上一次退出的安全区 */
//				SafeinfoVo safeinfoVo = SceneManager.getSafeVo(roleModule.getSafeStageId());
//				sceneType = (byte) safeinfoVo.getType();
//			}
//			if (scene != null && sceneType != SceneManager.SCENETYPE_CITY && scene instanceof FightScene) {
//				FightScene fs = (FightScene) scene;
//				if (fs.stageStatus == SceneManager.STAGE_PROCEEDING && fs.getSceneId() == sceneId) {
//					return;
//				}
//				newScene = SceneManager.newScene(sceneType);
//				// 再次进入
//				if (fs.getSceneId() == sceneId) {
//					((FightScene) scene).setIsAgain((byte) 1);
//				}
//			} else {
//				newScene = SceneManager.newScene(sceneType);
//			}
//			this.scene = newScene;
//			newScene.setSceneId(sceneId);
//
//			newScene.setSceneType(sceneType);
//
//			if (!newScene.canEnter(moduleMap(), extend)) {
//				if (newScene instanceof ArroundScene) {
//					/**
//					 * 不能进入安全区，可能是带有进入条件的安全区 比如被踢出了家族了就不能进入 初始化角色安全区位置，重新进入
//					 */
//					roleModule.initSafeStage();
//					enterScene(SceneManager.SCENETYPE_CITY, roleModule.getSafeStageId(), "");
//				}
//				return;
//			}
//			newScene.enter(moduleMap(), extend);
//
//			if (newScene instanceof ArroundScene) {
//				ArroundScene arroundScene = (ArroundScene) newScene;
//				roleModule.getRoleRow().setPositionStr(arroundScene.getPosition());
//				if (!(newScene instanceof FamilyEscortScene)) {
//					roleModule.updateSafeStageId(sceneId);
//				}
//				roleModule.updateArroundId(arroundScene.getArroundId(moduleMap()));
//				ArroundPlayerModule am = module(MConst.ArroundPlayer);
//				am.setPosition(arroundScene.getPosition());
//			}
//			String sceneIdStr = roleModule.getJoinSceneStr();
//			this.receiveDeadUId = new HashSet<>();
//			this.dispatchEvent(new EnterSceneEvent(sceneType, sceneIdStr, lastSceneType, lastSceneIdStr));
//			setLastSceneType(sceneType);
//			setLastSceneId(sceneId);
//		} finally {
//			try{
//				//上一个场景的退出处理
//				if(lastScene != null){
//					lastScene.extendExit(moduleMap(), extend);
//				}
//			}catch(Throwable e){
//				LogUtil.error(e.getMessage(),e);
//			}
//			//新场景的进入处理
//			this.scene.extendEnter(moduleMap(), extend);
//		}
//	}

    public void sendPlayedDrama(Scene scene, int stageId) {
        // 下发剧情播放记录
        if (scene instanceof FightScene) {
            sendPlayedDrama(SceneManager.DRAMA_STAGE, String.valueOf(stageId), Boolean.FALSE);
        } else {
            sendPlayedDrama(SceneManager.DRAMA_SAFE, String.valueOf(stageId), Boolean.FALSE);
        }
    }

    /**
     * 某些战斗交互包,交给FightScene子类处理
     *
     * @param packet
     */
    public void receiveFightPacket(byte sceneType, PlayerPacket packet) {
        if (sceneType == SceneManager.SCENETYPE_TEAMDUNGEON) {
            ServiceHelper.teamDungeonService().receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_ELITEDUNGEON) {
            ServiceHelper.eliteDungeonService().receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_FAMILY_INVADE) {
            FamilyInvadeModule familyInvadeModule = module("family.act.invade");
            familyInvadeModule.receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_ROB_ROBOT) {
            ServiceHelper.escortService().receiveFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_MARRY_DUNGEON) {
            ServiceHelper.teamDungeonService().receiveMarryFightPacket(packet);
        } else if (sceneType == SceneManager.SCENETYPE_CAMP_CITY_FIGHT) {
            ServiceHelper.campCityFightService().receiveFightPacket(packet);
        } else {
            if (scene instanceof FightScene) {
                ((FightScene) scene).receivePacket(moduleMap(), packet);
            }
        }
    }


    /**
     * 返回安全区场景
     */
    public void backToCity() {
        transferSafeStage("");
    }

    /**
     * 返回安全区场景
     * repeat=false,使重复进入场景检查失效一次,强制返回安全区场景
     *
     * @param repeat
     */
    public void backToCity(boolean repeat) {
        this.repeat = repeat;
        transferSafeStage("");
    }

    /**
     * 复活
     *
     * @param stageType
     */
    public void revive(byte stageType) {
        ReviveConfig reviveConfig = SceneManager.getReviveConfig(stageType);
        // 没有复活配置
        if (reviveConfig == null) {
            send(new ClientRoleRevive(id(), false));
            warn(I18n.get("scene.noReviveConfig"));
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        byte revivedNum = roleModule.getReviveNum(stageType);// 已复活次数
        com.stars.util.LogUtil.info("玩家复活次数| {} ,revivedNum:{}", id(), revivedNum);
        // 超过限制次数
        if (revivedNum >= reviveConfig.getLimitNum()) {
            send(new ClientRoleRevive(id(), false));
            warn(I18n.get("scene.reviveLimit"));
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        Map<Integer, Integer> costMap = new HashMap<>();
        // 免费次数已用完
        if (revivedNum >= reviveConfig.getFreeNum()) {
            costMap = reviveConfig.getCost((byte) (revivedNum - reviveConfig.getFreeNum() + 1));
            if (!toolModule.contains(costMap)) {
                send(new ClientRoleRevive(id(), false));
                warn(I18n.get("scene.reviveCostNotEnough"));
                return;
            }
        }
        boolean result = false;
        if (stageType == SceneManager.SCENETYPE_TEAMDUNGEON || stageType == SceneManager.SCENETYPE_MARRY_DUNGEON) {
            result = ServiceHelper.teamDungeonService().checkResurgence(id());
        } else if (stageType == SceneManager.SCENETYPE_ELITEDUNGEON) {
            result = ServiceHelper.eliteDungeonService().checkResurgence(id());
        } else {
            if (scene instanceof FightScene) {
                result = ((FightScene) scene).checkRevive(String.valueOf(id()));
            }
        }
        LogUtil.info("复活|result:{}", result);
        if (result) {
            toolModule.deleteAndSend(costMap, EventType.REVIVE.getCode());
            roleModule.updateReviveNum(stageType);
            send(new ClientRoleRevive(id(), true));
            eventDispatcher().fire(new RoleReviveEvent());
        } else {
//            warn(I18n.get("scene.statusError"));
        }
    }

    /**
     * 复活
     *
     * @param stageType
     * @param buddyId
     */
    public void reviveBuddy(byte stageType, String buddyId) {
        ClientRoleRevive clientRoleRevive = new ClientRoleRevive();
        clientRoleRevive.setSubType(ClientRoleRevive.BUDDY);
        clientRoleRevive.setBuddyId(buddyId);
        clientRoleRevive.setSuc(true);
        send(clientRoleRevive);
    }

    /**
     * 缓存下发
     *
     * @param packet
     */
    public void cacheSend(Packet packet) {
        // 缓存进入战斗包的monsterVo,skillVo,buffVo状态
        if (packet instanceof ClientEnterFight) {
            ClientEnterFight enterFight = (ClientEnterFight) packet;
            Map<Integer, MonsterVo> monsterVoMap = enterFight.getMonsterVoMap();
            // monsterVo
            for (int monsterId : monsterSendCache) {
                monsterVoMap.remove(monsterId);
            }
            for (int monsterId : monsterVoMap.keySet()) {
                monsterSendCache.add(monsterId);
            }
            // SkillVo
            Map<Integer, SkillVo> skillVoMap = enterFight.getSkillMap();
            for (int skillId : skillSendCache) {
                skillVoMap.remove(skillId);
            }
            for (int skillId : skillVoMap.keySet()) {
                skillSendCache.add(skillId);
            }
            // buffVo
            Set<BuffVo> buffVos = enterFight.getBuff();
            for (int buffId : buffSendCache) {
                buffVos.remove(buffId);
            }
            for (BuffVo buffVo : buffVos) {
                buffSendCache.add(buffVo.getBuffId());
            }
        }
        send(packet);
    }

    /**
     * 更新已播放剧情记录
     *
     * @param type
     * @param paramId
     * @param dramaId
     */
    public void updatePlayedDrama(String type, String paramId, String dramaId) {
        if (!type.equals(SceneManager.DRAMA_SAFE) && !type.equals(SceneManager.DRAMA_STAGE)
                && !type.equals(SceneManager.DRAMA_TASK)) {
            return;
        }
        if (!dramaPlayedMap.containsKey(type)) {
            dramaPlayedMap.put(type, new HashMap<String, RoleDrama>());
        }
        RoleDrama roleDrama = dramaPlayedMap.get(type).get(paramId);
        if (roleDrama == null) {
            roleDrama = new RoleDrama(id(), type, paramId);
            dramaPlayedMap.get(type).put(roleDrama.getParamId(), roleDrama);
            context().insert(roleDrama);
        }
        roleDrama.addPlayedDrama(dramaId);
        context().update(roleDrama);
    }

    /**
     * 下发播放过的剧情记录
     *
     * @param type
     * @param paramId
     * @param lazySend
     */
    private void sendPlayedDrama(String type, String paramId, boolean lazySend) {
        Map<String, RoleDrama> roleDramaMap = dramaPlayedMap.get(type);
        ClientDrama clientDrama = new ClientDrama(ClientDrama.PLAYED_DRAMA);
        List<RoleDrama> sendList = new LinkedList<>();
        if (roleDramaMap != null) {
            if ("all".equals(paramId)) {
                sendList.addAll(roleDramaMap.values());
            } else {
                RoleDrama roleDrama = roleDramaMap.get(paramId);
                if (roleDrama != null) {
                    sendList.add(roleDrama);
                }
            }
        }
        clientDrama.setType(type);
        clientDrama.setRoleDramas(sendList);
        if (lazySend) {
            lazySend(clientDrama);
        } else {
            send(clientDrama);
        }
    }

    public byte getLastSceneType() {
        return lastSceneType;
    }

    public void setLastSceneType(byte lastSceneType) {
        this.lastSceneType = lastSceneType;
    }

    public int getLastSceneId() {
        return lastSceneId;
    }

    public void setLastSceneId(int lastSceneId) {
        this.lastSceneId = lastSceneId;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public int getGmDungeonId() {
        return gmDungeonId;
    }

    public void setGmDungeonId(int gmDungeonId) {
        this.gmDungeonId = gmDungeonId;
    }
}
