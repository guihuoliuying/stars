package com.stars.services.escort;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.escort.EscortActivityFlow;
import com.stars.modules.escort.EscortConstant;
import com.stars.modules.escort.EscortManager;
import com.stars.modules.escort.EscortModule;
import com.stars.modules.escort.event.*;
import com.stars.modules.escort.packet.ClientEscort;
import com.stars.modules.escort.packet.ClientEscortSafe;
import com.stars.modules.escort.packet.vo.CargoPo;
import com.stars.modules.escort.prodata.CargoAIVo;
import com.stars.modules.escort.prodata.CargoCarVo;
import com.stars.modules.escort.prodata.CargoMonsterVo;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.imp.fight.EscortRobScene;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.MonsterVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.data.Vector3;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.role.RoleNotification;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;
import io.netty.buffer.Unpooled;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyuxing on 2016/12/5.
 */
public class EscortServiceActor extends ServiceActor implements EscortService {

    private Map<String,EscortScene> PERSONAL_ESCORT_SCENES = new HashMap<>();  //个人运镖集合 <carId,<fightId,scene>>
    private Map<String,EscortScene> TEAM_ESCORT_SCENES = new HashMap<>();      //组队运镖集合 <carId,<fightId,scene>>
    private Map<String,EscortScene> SCENE_MAP = new HashMap<>();

    private Map<Long,EscortRobScene> ROB_SCENE_MAP = new HashMap<>();   //劫镖机器人关卡场景

    private Map<Long,Escorter> escorterMap = new HashMap<>();
    private Map<String, byte[]> dataMap = new HashMap<>();

    private Map<Long,CargoListCache> CACHE_MAP = new HashMap<>();
    private Map<String,RobTempCache> ROB_TEMP_CACHE = new HashMap<>();//劫镖者战斗后的临时缓存

    private long sceneId = 0;
    public final static String prefix = "escort.";

    private Set<Long> escortPermitSet = new HashSet<>();
    private Set<Long> robPermitSet = new HashSet<>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.EscortService, this);
    }

    @Override
    public void printState() {

    }

    /**
     * 创建单人押镖的战斗实体
     */
    private Escorter createSingleEscorter(String fightId, long roleId, Map<String, Module> moduleMap, byte camp, StageinfoVo stageVo){
        Escorter escorter = new Escorter(fightId,roleId,roleId);

        /* 玩家实体 */
        FighterEntity playerEntity = FighterCreator.createSelf(moduleMap,camp);
        escorter.setPlayerEntity(playerEntity);
        playerEntity.setFighterType(FighterEntity.TYPE_PLAYER);
        playerEntity.setPosition(stageVo.getPosition());
        playerEntity.setRotation(stageVo.getRotation());
        escorter.setCamp(camp);

        /* 出战伙伴 */
//        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
//        if (buddyModule.getFightBuddyId() != 0) {
//            FighterEntity buddyEntity = FighterCreator.create(FighterEntity.TYPE_BUDDY, camp,
//                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId()));
//            escorter.addOtherEntities(buddyEntity);
//        }
        return escorter;
    }

    /**
     * 创建单人押镖的战斗实体
     */
    private Escorter createSingleEscorter(String fightId,long roleId,Map<String, Module> moduleMap,byte camp,String pos){
        Escorter escorter = new Escorter(fightId,roleId,roleId);

        /* 玩家实体 */
        FighterEntity playerEntity = FighterCreator.createSelf(moduleMap,camp);
        escorter.setPlayerEntity(playerEntity);
        playerEntity.setFighterType(FighterEntity.TYPE_PLAYER);
        playerEntity.setPosition(pos);
        playerEntity.setRotation(0);
        escorter.setCamp(camp);

        /* 出战伙伴 */
//        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
//        if (buddyModule.getFightBuddyId() != 0) {
//            FighterEntity buddyEntity = FighterCreator.create(FighterEntity.TYPE_BUDDY, camp,
//                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId()));
//            escorter.addOtherEntities(buddyEntity);
//        }
        return escorter;
    }

    private String getSpawnUId(int spawnId) {
        return "" + spawnId;
    }

    private String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

    /**
     * 初始化场景怪物
     */
    private void initSceneMonster(EscortScene escortScene){
        CargoMonsterVo monsterVo = EscortManager.getCargoMonsterByFightScore(escortScene.getEscortTotalFighting());
        if(monsterVo == null || StringUtil.isEmpty(monsterVo.getMonsterSpawnIdList())) return;
        for (int monsterSpawnId : monsterVo.getMonsterSpawnIdList()) {
            MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
            if (monsterSpawnVo == null) {
                LogUtil.error("找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
                return;
            }
            int index = 0;
            for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
                String monsterUniqueId = getMonsterUId(escortScene.getStageId(), monsterSpawnId, monsterAttrVo.getStageMonsterId());
                FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                        getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                        monsterSpawnVo.getSpawnDelayByIndex(index++), null);
                monsterEntity.setCamp(EscortConstant.CAMP_MONSTER);
                escortScene.getEntityMap().put(monsterUniqueId, monsterEntity);
            }
        }
    }

    /**
     * 初始化镖车怪物
     */
    private void initCargoMonster(EscortScene escortScene,int carId){
        CargoCarVo carVo = EscortManager.getCargoCarVoById(carId);
        if(carVo == null) return;
        MonsterAttributeVo monsterAttrVo = SceneManager.getMonsterAttrVo(carVo.getStageMonsterId());;
//        String monsterUniqueId = getMonsterUId(escortScene.getSectionId(), 0, monsterAttrVo.getStageMonsterId());
        String monsterUniqueId = EscortConstant.CARGO_CAR_FIGHT_ID;
        FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                getSpawnUId(0), 0, monsterAttrVo, "0",0, null);
        monsterEntity.setCamp(EscortConstant.CAMP_ESCORT);
        escortScene.getEntityMap().put(monsterUniqueId, monsterEntity);
    }

    /**
     * 将包转为byte[]
     */
    private byte[] packetToBytes(Packet packet) {
        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        packet.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
        return bytes;
    }

    /**
     * 将运镖战斗场景加入缓存
     */
    private void addEscortSceneInCache(byte type,EscortScene escortScene){
        SCENE_MAP.put(escortScene.getFightId(),escortScene);
        if(type == EscortConstant.ESCORT_TYPE_SINGLE){
            PERSONAL_ESCORT_SCENES.put(escortScene.getFightId(), escortScene);
        }else if(type == EscortConstant.ESCORT_TYPE_TEAM){
            TEAM_ESCORT_SCENES.put(escortScene.getFightId(),escortScene);
        }
    }

    /**
     * 移除运镖战斗场景的缓存
     */
    private void removeEscortSceneInCache(String fightId){
        if(SCENE_MAP.containsKey(fightId)){
            SCENE_MAP.remove(fightId);
        }
        if(PERSONAL_ESCORT_SCENES.containsKey(fightId)){
            PERSONAL_ESCORT_SCENES.remove(fightId);
        }
        if(TEAM_ESCORT_SCENES.containsKey(fightId)){
            TEAM_ESCORT_SCENES.remove(fightId);
        }
        if(dataMap.containsKey(fightId)) {
            dataMap.remove(fightId);//移除缓存
        }
    }

    private void noticeDailyEvent(Collection<Escorter> set){
        DailyFuntionEvent event = new DailyFuntionEvent(DailyManager.DAILYID_ESCORT, 1);
//        for(Escorter escorter:set){
//            ServiceHelper.roleService().notice(escorter.getRoleId(),new RoleNotification(event));
//        }
    }

    /**
     * 开始个人押镖
     */
    @Override
    public void singleBeginEscort(long roleId, Map<String, Module> moduleMap,byte index,int carId,long familyId){
        sceneId++;
        String fightId = prefix + sceneId;
        int stageId = EscortManager.getCargocarStageid();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if (stageVo == null) return;
        CargoCarVo carVo = EscortManager.getCargoCarVoById(carId);
        if(carVo == null) return;
        
       
       
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        if(roleModule == null) return;
        int fightServer = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
        EscortScene escortScene = new EscortScene(fightId,stageId,carId,roleId,carVo.getCarType(),roleModule.getRoleRow().getName(),familyId);
        escortScene.setFightServer(fightServer);
        escortScene.setIndex(index);

        //初始化运镖玩家实体
        Escorter escorter = createSingleEscorter(fightId,roleId,moduleMap,EscortConstant.CAMP_ESCORT,stageVo);
        escortScene.addEscorter(escorter);
        escorterMap.put(escorter.getRoleId(), escorter);

        initSceneMonster(escortScene);//初始化场景怪物
        initCargoMonster(escortScene,carId);//初始化镖车怪物
        addEscortSceneInCache(EscortConstant.ESCORT_TYPE_SINGLE,escortScene);//将押镖场景加入缓存

        List<FighterEntity> entityList = new ArrayList<>();
        entityList.addAll(escortScene.getEntityMap().values());

        //构建进入场景包
        ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_ESCORT_FIGHT);
        enterPack.setStageId(EscortManager.getCargocarStageid());
        enterPack.setLimitTime(EscortManager.ESCORT_TIME);// 限制时间
        enterPack.setFighterEntityList(entityList);
        enterPack.addBuffData(getEnemyBuffIdMap());
        addAllJobSkill(enterPack);
        
        int serverid = MultiServerHelper.getServerId();

        //将进入场景包转为缓存数据
        byte[] bytes = packetToBytes(enterPack);
        dataMap.put(fightId,bytes);

        //请求创建战斗
        MainRpcHelper.fightBaseService().createFight(fightServer,
                FightConst.T_ESCORT_CARGO, serverid, escortScene.getFightId(), bytes, createFightInitData(roleId));

        noticeDailyEvent(escortScene.getEscorterMap().values());//日常活动参与事件
    }

    private void addAllJobSkill(ClientEnterPK client){
        Map<Integer, Job> jobMap = RoleManager.jobMap;
        Job job;
        Resource resource;
        SkillVo skillVo;
        List<Integer> skillVoList;
        Map<Integer, Integer> skillMap = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, Job> kvp : jobMap.entrySet()) {
            job = kvp.getValue();
            resource = RoleManager.getResourceById(job.getModelres());
            skillVoList = resource.getSkillList();
            for (int i = 0, len = skillVoList.size(); i < len; i++) {
                skillVo = SkillManager.getSkillVo(skillVoList.get(i));
                skillMap.put(skillVo.getSkillid(), 1);
            }
            for (Integer pSkillId : job.getPSkillList()) {
                skillMap.put(pSkillId, 1);
            }
        }
        client.addSkillData(skillMap);
    }

    private Map<Integer, Integer> getEnemyBuffIdMap(){
        Map<Integer, Integer> buffIdLevelMap = new HashMap<>();
        buffIdLevelMap.put(EscortManager.getCargocarEnemyBuffId(), EscortManager.getCargocarEnemyBuffLevel());
        return buffIdLevelMap;
    }

    private EscortFightInitData createFightInitData(long roleId){
        List<Long> list = new ArrayList<>();
        list.add(roleId);
        return new EscortFightInitData(list);
    }

    private EscortFightInitData createFightInitData(Collection<Long> idSet){
        List<Long> list = new ArrayList<>();
        list.addAll(idSet);
        return new EscortFightInitData(list);
    }

    //初始化队伍中玩家实体
    private void initTeamEscorter(EscortScene escortScene, byte camp, BaseTeam team, StageinfoVo stageVo){
        Escorter escorter;
        for(BaseTeamMember member:team.getMembers().values()){
            FighterEntity newEntity = member.getRoleEntity().copy();
            if (newEntity != null && newEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                newEntity.setPosition(stageVo.getPosition());
                newEntity.setRotation(stageVo.getRotation());
                newEntity.setCamp(camp);
                escorter = new Escorter(escortScene.getFightId(),member.getRoleId(),team.getCaptainId());
                escorter.setPlayerEntity(newEntity);
                escorter.setCamp(camp);
                escorter.setTeamId(team.getTeamId());
                escortScene.addEscorter(escorter);
                escorterMap.put(escorter.getRoleId(), escorter);
            }
        }
    }

    //初始化队伍中玩家实体
    private void initTeamRobber(EscortScene escortScene,byte camp,BaseTeam team,String pos){
        Escorter escorter;
        for(BaseTeamMember member:team.getMembers().values()){
            FighterEntity newEntity = member.getRoleEntity().copy();
            if (newEntity != null && newEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                newEntity.setPosition(pos);
                newEntity.setRotation(0);
                newEntity.setCamp(camp);
                escorter = new Escorter(escortScene.getFightId(),member.getRoleId(),team.getCaptainId());
                escorter.setPlayerEntity(newEntity);
                escorter.setCamp(camp);
                escorter.setTeamId(team.getTeamId());
                escortScene.addRobber(escorter);
                escorterMap.put(escorter.getRoleId(), escorter);
            }
        }
    }

    /**
     * 开始组队押镖
     */
    @Override
    public void teamBeginEscort(long leaderId, byte index,int carId,long familyId){
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(leaderId);
        if(team == null) return;

        if(team.getCaptainId() != leaderId) return;//不是队长

        sceneId++;
        String fightId = prefix + sceneId;
        int stageId = EscortManager.getCargocarStageid();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if (stageVo == null) return;
        
        CargoCarVo carVo = EscortManager.getCargoCarVoById(carId);
        if(carVo == null) return;
        int fightServer = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
        EscortScene escortScene = new EscortScene(fightId,stageId,carId,leaderId,carVo.getCarType(),team.getCaptain().getRoleEntity().getName(),familyId);
        escortScene.setFightServer(fightServer);
        escortScene.setIndex(index);

        initTeamEscorter(escortScene,EscortConstant.CAMP_ESCORT,team,stageVo);//初始化队伍中玩家实体
        initSceneMonster(escortScene);//初始化场景怪物
        initCargoMonster(escortScene,carId);//初始化镖车怪物
        addEscortSceneInCache(EscortConstant.ESCORT_TYPE_TEAM,escortScene);//将押镖场景加入缓存

        List<FighterEntity> entityList = new ArrayList<>();
        entityList.addAll(escortScene.getEntityMap().values());

        //构建进入场景包
        ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_ESCORT_FIGHT);
        enterPack.setStageId(EscortManager.getCargocarStageid());
        enterPack.setLimitTime(EscortManager.ESCORT_TIME);// 限制时间
        enterPack.setFighterEntityList(entityList);
        enterPack.addBuffData(getEnemyBuffIdMap());
        addAllJobSkill(enterPack);
        int serverid = MultiServerHelper.getServerId();

        //将进入场景包转为缓存数据
        byte[] bytes = packetToBytes(enterPack);
        dataMap.put(fightId,bytes);
        team.setFight(true);

        //请求创建战斗
        MainRpcHelper.fightBaseService().createFight(fightServer,
                FightConst.T_ESCORT_CARGO, serverid, escortScene.getFightId(), bytes, createFightInitData(team.getMembers().keySet()));

        noticeDailyEvent(escortScene.getEscorterMap().values());//日常活动参与事件
    }

    /**
     * 战斗场景创建成功
     */
    @Override
    public void onFightCreationSucceeded(int mainServerId, int fightServerId, String fightId,List<Long> escortIds){
        EscortScene scene = SCENE_MAP.get(fightId);
        List<FighterEntity> list;
        for(Escorter escorter:scene.getEscorterMap().values()) {
            list = new ArrayList<>();
            list.add(escorter.getPlayerEntity());
            if(StringUtil.isNotEmpty(escorter.getOtherEntities())){
                list.addAll(escorter.getOtherEntities());
            }
            MainRpcHelper.fightBaseService().addFighter(scene.getFightServer(),
                    FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), scene.getFightId(), list);
        }
    }

    @Override
    public void onFighterAddingSucceeded(int mainServerId, int fightServerId, String fightId, Set<Long> entitySet){
        EscortScene scene = SCENE_MAP.get(fightId);
        if(scene == null) return;

        Set<Long> onlineSet = new HashSet<>();
        // 切路由
        Escorter escorter = null;
        for(long roleId:entitySet) {
            escorter = scene.getEscorterMap().get(roleId);
            if(escorter==null){
                escorter = scene.getRobberMap().get(roleId);
            }
            if(escorter == null || escorter.isOffline()) continue;
            MultiServerHelper.modifyConnectorRoute(roleId, fightServerId);
            onlineSet.add(roleId);

            if(escorter.isRevive()){//重生的玩家,不需要重新发进入场景包
                escorter.setRevive(false);
            }else {
                noticeClientEnterScene(fightId, roleId);//通知客户端进入场景
            }
        }

        if(scene.getStatus() == EscortConstant.SCENE_STATUS_NOT_BEGIN) {
            scene.setStatus(EscortConstant.SCENE_STATUS_NORMAL);
            sendCargoCarBeginMoveOrder(scene);
        }
        if(escorter != null  && escorter.getCamp() == EscortConstant.CAMP_ESCORT){
            sendCargoCarBeginMoveOrder(scene);
        }
    }

    /**
     * 发送镖车开始移动指令
     */
    private void sendCargoCarBeginMoveOrder(EscortScene scene){
        String pos = scene.getCargoCurPosTargetPos();
        if(StringUtil.isEmpty(pos)) return;//已经到终点了

        //创建服务端lua命令
        ServerOrder serverOrder = new ServerOrder();
        serverOrder.setOrderType(ServerOrder.ORDER_TYPE_MOVE_CHARAC);
        serverOrder.setUniqueId(EscortConstant.CARGO_CAR_FIGHT_ID);
        serverOrder.setPosition(pos);

        sendServerOrder(scene.getFightId(), serverOrder);//发送服务端lua命令
    }

    /**
     * 发送重置战斗状态指令
     */
    private void sendChangeFightStateOrder(EscortScene scene,byte state,int duration){
        ServerOrder serverOrder = new ServerOrder();
        serverOrder.setOrderType(ServerOrder.ORDER_TYPE_CHANGE_FIGHTSTATE);
        serverOrder.setFightState(state);
        serverOrder.setDuration(duration);
        sendServerOrder(scene.getFightId(), serverOrder);//发送服务端lua命令
    }

    /**
     * 发送镖车停止移动指令
     */
    private void sendStopCargoCarOrder(EscortScene scene){
        ServerOrder serverOrder = new ServerOrder();
        serverOrder.setOrderType(ServerOrder.ORDER_TYPE_STOP_CHARAC);
        serverOrder.setUniqueId(EscortConstant.CARGO_CAR_FIGHT_ID);
        sendServerOrder(scene.getFightId(), serverOrder);//发送服务端lua命令
    }

    /**
     * 发送服务端lua命令
     */
    private void sendServerOrder(String fightId,ServerOrder serverOrder){
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.addOrder(serverOrder);
        byte[] bytes = packetToBytes(packet);
        
        MainRpcHelper.fightBaseService().addServerOrder(SCENE_MAP.get(fightId).getFightServer(),
                FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), fightId, bytes);
    }

    /**
     * 发送移除战斗实体lua命令
     */
    private void removePlayer(String fightId,List<String> removeFighter){
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.setRemoveFighter(removeFighter);
        byte[] bytes = packetToBytes(packet);

        MainRpcHelper.fightBaseService().addServerOrder(SCENE_MAP.get(fightId).getFightServer(),
                FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), fightId, bytes);
    }

    /**
     * 新增伙伴monsterVo产品数据指令
     */
    private void addMonsterVoServerOrder(String fightId,MonsterVo monsterVo){
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.addMonsterVo(monsterVo);
        byte[] bytes = packetToBytes(packet);

        MainRpcHelper.fightBaseService().addServerOrder(SCENE_MAP.get(fightId).getFightServer(),
                FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), fightId, bytes);
    }

    /**
     * 通知客户端进入场景
     */
    public void noticeClientEnterScene(String fightId,long roleId){
        byte[] initData = dataMap.get(fightId);
        if(initData == null) return;
        EnterEscortSceneEvent enterSceneEvent = new EnterEscortSceneEvent(initData);
        ServiceHelper.roleService().notice(roleId, new RoleNotification(enterSceneEvent));
    }

    /**
     * 镖车是否能被别人选中
     */
    private boolean canPick(EscortScene scene){
        if (scene.getStatus() == EscortConstant.SCENE_STATUS_NOT_BEGIN) return false;//还未开始
        if (scene.isLeaderOffline()) return false;//队长掉线,剔除
        if (scene.getRobbedTimes() >= 2) return false;//已经被抢2次了
        return true;
    }

    /**
     * 初始化镖车队列
     * 取车规则很操蛋，有事找策划，怼他
     */
    private void initCargoList(CargoListCache cache,Map<String,EscortScene> sceneMap,long familyId){
        Map<Byte,List<EscortScene>> carTypeMap = new HashMap<>();
        int totalSize = 0;
        List<EscortScene> list;
        List<EscortScene> enemyList = new ArrayList<>();
        //先筛选，剔除不能被选中的镖车，并选出仇人镖车,及镖车分类(用于计算取车数量)
        for(EscortScene scene:sceneMap.values()) {
            if(!canPick(scene)) continue;//镖车是否能被选中
            if(familyId !=0 && scene.getFamilyId() == familyId) continue;

            list = carTypeMap.get(scene.getCarType());
            if (list == null) {
                list = new ArrayList<>();
                carTypeMap.put(scene.getCarType(),list);
            }
            list.add(scene);
            totalSize++;

            //仇人列表
            if(cache.getEnemyList().size() > 0  && cache.getEnemyList().contains(scene.getRoleId())){
                enemyList.add(scene);
            }
        }

        //计算取镖车数量(得出每个carid对应的取车数)
        Map<Byte,Integer> carCountMap = new HashMap<>();
        int count;
        int coefficient = EscortManager.getCargocarCoefficient();//策划配置的取车系数
        for(byte carType=4;carType>=1;carType--){//默认carType为 1,2,3,4
            list = carTypeMap.get(carType);
            if(StringUtil.isEmpty(list)){
                carCountMap.put(carType,0);
            }else{
                if(carType >= 3) {//向上取整
                    count = (int) Math.ceil(1.0 * list.size() / totalSize * coefficient);
                }else{//向下取整
                    count = (int) Math.ceil(1.0 * list.size() / totalSize * coefficient);
                }
                carCountMap.put(carType,count);
            }
        }

        int initSize = EscortManager.getCargocarCarcount();//初始化场景需要的镖车数量
        int resultSize = 0;//实际已取车数,不区分carId
        Map<Byte,List<EscortScene>> resultMap = new HashMap<>();//实际取的镖车集合
        //优先填入仇人列表
        for(EscortScene scene:enemyList){
            count = carCountMap.get(scene.getCarType());//需要取车数量
            if(count <= 0) continue;
            list = resultMap.get(scene.getCarType());//已取的对应carType镖车列表
            if(list == null){
                list = new ArrayList<>();
                resultMap.put(scene.getCarType(),list);
            }
            if(list.size() >= count) continue;//对应carType已经取满了
            list.add(scene);
            resultSize++;
            if(resultSize >= initSize) break;//场景取车数已满
        }

        List<EscortScene> tmpList = new ArrayList<>();//备选镖车,不符合战力要求,但是符合类型的镖车列表
        List<EscortScene> carList;
        int missingValue = 0;//缺少的数量
        //正常填入可选镖车
        for(byte carType=4;carType>=1;carType--) {//默认carId为 1,2,3,4
            count = carCountMap.get(carType) + missingValue;//需要取车数量
            if(count <= 0) continue;
            list = resultMap.get(carType);//已取的对应carID镖车列表
            if(list == null){
                list = new ArrayList<>();
                resultMap.put(carType,list);
            }
            if(list.size() >= count) continue;//对应carid已经取满了
            if(resultSize >= initSize) break;//场景取车数已满

            tmpList.clear();
            carList = carTypeMap.get(carType);
            if(StringUtil.isNotEmpty(carList)) {//可选镖车集合不为空
                for (EscortScene scene : carList) {
                    if(enemyList.contains(scene)) continue;//跳过仇人,已经处理过了
                    if(isMatchFighting(carType,cache,scene)){
                        list.add(scene);
                        resultSize++;
                        if(list.size() >= count) break;
                        if(resultSize >= initSize) break;//场景取车数已满
                    }else{
                        tmpList.add(scene);
                    }
                }
            }

            //遍历一次后未满足取车数量，将不满足战力要求的对应carId的镖车加入序列
            if(list.size() < count){
                for (EscortScene scene : tmpList) {
                    if(resultSize >= initSize) break;//场景取车数已满
                    list.add(scene);
                    resultSize++;
                    if(list.size() >= count) break;
                }
            }

            missingValue = count - list.size();//缺少的取车数
        }

        Set<EscortScene> resultSet = new HashSet<>(initSize);//最终筛选到的镖车集合
        for(List<EscortScene> result : resultMap.values()){
            resultSet.addAll(result);
        }

        initCargoPoList(cache,resultSet);//将筛选后的镖车生成cargoPo,不足则用机器人补充
    }

    /**
     * 生成机器人镖车po
     */
    private CargoPo createRobotCargoPo(byte index,int fighting){
        CargoPo cargoPo = new CargoPo();
        cargoPo.setIndex(index);
        CargoAIVo aiVo = EscortManager.getCargoAiByFightScore(fighting);
        cargoPo.setCarId(aiVo.getCarId());
        cargoPo.setFighting(aiVo.getDisplayRandomPower());
        cargoPo.setIsRobot((byte)1);
        cargoPo.setSectionId(aiVo.getPowerSection());
        cargoPo.setAward(aiVo.getAwardMap());
        return cargoPo;
    }

    /**
     * 获得镖车当前状态  0正常  1战斗中  2保护中
     */
    private byte getCargoStatue(EscortScene scene){
        if(scene.isInPvp() || scene.isPause()){//pvp & 暂停中 都是在战斗中
            return EscortConstant.CARGO_STATUE_FIGHTING;
        }
        if(scene.isInProtect()){
            return EscortConstant.CARGO_STATUE_PROTECT;
        }
        return EscortConstant.CARGO_STATUE_NORMAL;
    }

    /**
     * 生成玩家镖车po
     */
    private CargoPo createPlayerCargoPo(byte index,EscortScene scene,CargoListCache cache){
        CargoPo cargoPo = new CargoPo();
        cargoPo.setIndex(index);
        cargoPo.setCarId(scene.getCarId());
        cargoPo.setFighting(scene.getEscortTotalFighting());
        cargoPo.setStatue(getCargoStatue(scene));
        cargoPo.setFightId(scene.getFightId());
        cargoPo.setEscortId(scene.getRoleId());
        cargoPo.setEscortName(scene.getRoleName());

        if(cache.getEnemyList().contains(scene.getRoleId())){
            cargoPo.setIsEnemy((byte)1);//仇人标识
        }
        return cargoPo;
    }

    /**
     * 将筛选后的镖车生成cargoPo,不足则用机器人补充
     */
    private void initCargoPoList(CargoListCache cache,Set<EscortScene> sceneSet){
        int initSize = EscortManager.getCargocarCarcount();//初始化场景需要的镖车数量
        byte index = 0;
        for(EscortScene scene:sceneSet){//玩家镖车处理
            index++;
            cache.getCargoList().put(index,createPlayerCargoPo(index,scene,cache));
        }

//        while(index < initSize){//数量不足，填充机器人镖车
//            index++;
//            cache.getCargoList().put(index, createRobotCargoPo(index, cache.getFighting()));
//        }
    }

    /**
     * 是否符合对应战力范围
     */
    private boolean isMatchFighting(int carId,CargoListCache cache,EscortScene scene){
        int minFighting,maxFighting;
        int[] fightingScope = EscortManager.getCargoGetCarCountCoef().get(carId);
        minFighting = (int)(1.0 * cache.getFighting() * fightingScope[0] / 100);
        if(fightingScope[1]==0){
            maxFighting = Integer.MAX_VALUE;
        }else{
            maxFighting = (int)(1.0 * cache.getFighting() * fightingScope[1] / 100);
        }
        return scene.getEscortTotalFighting() >= minFighting && scene.getEscortTotalFighting() <= maxFighting;
    }

    /**
     * 单人进入队列场景
     */
    @Override
    public void singleEnterCargoListScene(long roleId,List<Long> enemyList,int fighting){
        CargoListCache cache = new CargoListCache(roleId,enemyList,fighting);
        CACHE_MAP.put(roleId,cache);
        long familyId = ServiceHelper.familyRoleService().getFamilyId(roleId);
        initCargoList(cache, PERSONAL_ESCORT_SCENES, familyId);//初始化镖车队列

        ClientEscortSafe client = new ClientEscortSafe(ClientEscortSafe.RESP_ENTER_SENCE,cache.getCargoList().values());
        byte[] packetData = packetToBytes(client);
        EnterEscortSafeSceneEvent event = new EnterEscortSafeSceneEvent(packetData);
        ServiceHelper.roleService().notice(roleId, new RoleNotification(event));
    }

    /**
     * 组队进入队列场景
     */
    @Override
    public void teamEnterCargoListScene(long leaderId,List<Long> enemyList){
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(leaderId);
        if(team == null || team.getCaptainId() != leaderId) return;//不是队长
        CargoListCache cache = new CargoListCache(leaderId,enemyList,team.getPlayerTotalFighting());

        CACHE_MAP.put(leaderId, cache);
        long familyId = ServiceHelper.familyRoleService().getFamilyId(leaderId);
        initCargoList(cache,TEAM_ESCORT_SCENES,familyId);//初始化镖车队列

        ClientEscortSafe client = new ClientEscortSafe(ClientEscortSafe.RESP_ENTER_SENCE,cache.getCargoList().values());
        byte[] packetData = packetToBytes(client);
        EnterEscortSafeSceneEvent event = new EnterEscortSafeSceneEvent(packetData);

        team.setFight(true);

        for(BaseTeamMember member:team.getMembers().values()){
            ServiceHelper.roleService().notice(member.getRoleId(), new RoleNotification(event));
        }
    }

    /**
     * 刷新镖车列表场景
     * @param roleId
     * @param refreshIndex 必须刷新的序号(该镖车已走到尽头消失)
     */
    public void updateCargoListScene(long roleId,byte refreshIndex){
        CargoListCache cache = CACHE_MAP.get(roleId);
        if(cache == null) return;

        List<Byte> resetList = new ArrayList<>();       //需要重置的镖车序号
        List<CargoPo> updateList = new ArrayList<>();   //实际刷新的镖车(重置 & 状态改变)

        if(refreshIndex != 0){//需要重置走到尽头的镖车
            resetList.add(refreshIndex);
        }

        long familyId = ServiceHelper.familyRoleService().getFamilyId(roleId);

        EscortScene scene;
        byte curStatue;
        Set<Byte> curList = new HashSet<>();
        for(CargoPo po:cache.getCargoList().values()){
            if(po == null) continue;
            curList.add(po.getIndex());
            if(po.getIndex() == refreshIndex) continue;//必须要重置的镖车,直接跳过
            scene = SCENE_MAP.get(po.getFightId());
            //镖车已不存在 || 镖车已被抢2次 需要重置
            if (scene == null || scene.getRobbedTimes() >= 2 || scene.isLeaderOffline()){
                resetList.add(po.getIndex());
                continue;
            }

            curStatue = getCargoStatue(scene);//镖车当前状态
            if(curStatue != po.getStatue()){
                po.setStatue(curStatue);
                updateList.add(po);
            }
        }
        byte initSize = (byte)EscortManager.getCargocarCarcount();//初始化场景需要的镖车数量
        for(byte i=1;i<=initSize;i++){//更新时补充空位
            if(curList.contains(i)) continue;
            resetList.add(i);
        }

        boolean hasTeam = ServiceHelper.baseTeamService().hasTeam(roleId);
        if(hasTeam){
            rePickCargoCar(EscortConstant.ESCORT_TYPE_TEAM,cache,resetList,updateList,familyId);

            //给全部队员下发刷新包
            BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
            if(team!=null){
                ClientEscortSafe client = new ClientEscortSafe(ClientEscortSafe.RESP_UPDATE_CARGO,updateList);
                for(BaseTeamMember member:team.getMembers().values()){
                    PlayerUtil.send(member.getRoleId(),client);
                }
            }
        }else{
            rePickCargoCar(EscortConstant.ESCORT_TYPE_SINGLE,cache,resetList,updateList,familyId);

            //下发刷新包
            ClientEscortSafe client = new ClientEscortSafe(ClientEscortSafe.RESP_UPDATE_CARGO,updateList);
            PlayerUtil.send(roleId,client);
        }
    }

    /**
     * 根据列表重置镖车(在缓存中刷新新的镖车)
     */
    private void rePickCargoCar(byte type,CargoListCache cache,List<Byte> resetList,List<CargoPo> updateList,long familyId){
        if(StringUtil.isEmpty(resetList)) return;

        Map<String,EscortScene> sceneMap;
        if(type == EscortConstant.ESCORT_TYPE_TEAM){
            sceneMap = TEAM_ESCORT_SCENES;
        }else{
            sceneMap = PERSONAL_ESCORT_SCENES;
        }

        List<EscortScene> enemyList = new ArrayList<>();    //仇人列表
        List<EscortScene> playList = new ArrayList<>();     //玩家列表
        //先筛选，剔除不能被选中的镖车，并选出仇人镖车,及镖车分类(用于计算取车数量)
        for(EscortScene scene:sceneMap.values()) {
            if(!canPick(scene)) continue;//镖车是否能被选中
            if(familyId != 0 && scene.getFamilyId() == familyId) continue;

            //仇人列表
            if(cache.getEnemyList().size() > 0  && cache.getEnemyList().contains(scene.getRoleId())){
                enemyList.add(scene);
            }else{
                playList.add(scene);
            }
        }

        boolean success;
        CargoPo cargoPo;
        for(byte index:resetList){//需要替换的镖车
            success = false;
            //优先替换仇人到队列中
            for(EscortScene scene:enemyList){
                if(checkIsReadyInList(scene,cache)) continue;//已经在队列中
                cargoPo = createPlayerCargoPo(index, scene, cache);
                cache.getCargoList().put(index,cargoPo);
                updateList.add(cargoPo);
                success = true;
                break;
            }
            if(success) continue;//成功替换

            //替换玩家镖车到队列中
            for(EscortScene scene:playList){
                if(checkIsReadyInList(scene,cache)) continue;//已经在队列中
                cargoPo = createPlayerCargoPo(index, scene, cache);
                cache.getCargoList().put(index,cargoPo);
                updateList.add(cargoPo);
                success = true;
                break;
            }
            if(success) continue;//成功替换

//            //填充机器人
//            cargoPo = createRobotCargoPo(index, cache.getFighting());
//            cache.getCargoList().put(index,cargoPo);
//            updateList.add(cargoPo);
        }
    }

    /**
     * 是否已经在队列中
     */
    private boolean checkIsReadyInList(EscortScene scene,CargoListCache cache){
        if(scene == null || cache == null || cache.getCargoList() == null) return false;
        for(CargoPo po:cache.getCargoList().values()){
            if(po == null || po.getFightId() == null) continue;
            if(po.getFightId().equals(scene.getFightId())){
                return true;
            }
        }
        return false;
    }

    /**
     * 进入运镖场景进行劫镖
     */
    @Override
    public void robCargo(long roleId, byte index,byte escortType,Map<String, Module> moduleMap,boolean useMask,int remainRobTimes){
        CargoListCache cache = CACHE_MAP.get(roleId);
        if(cache == null || cache.getCargoList() == null) return;
        CargoPo po = cache.getCargoList().get(index);
        if(po == null) {//镖车已不存在
            updateCargoListScene(roleId,index);
            PlayerUtil.send(roleId, new ClientText(I18n.get("escort.cargoNotExist")));
            return;
        }
        EscortScene scene = SCENE_MAP.get(po.getFightId());
        //镖车已不存在 || 镖车已被抢2次 需要重置
        if (scene == null || scene.getRobbedTimes() >= 2){
            updateCargoListScene(roleId,index);
            PlayerUtil.send(roleId, new ClientText(I18n.get("escort.cargoNotExist")));
            return;
        }
        byte curStatue = getCargoStatue(scene);
        if(curStatue == EscortConstant.CARGO_STATUE_FIGHTING){//战斗中
            updateCargoListScene(roleId,(byte)0);
            PlayerUtil.send(roleId, new ClientText(I18n.get("escort.inFighting")));
            return;
        }else if(curStatue == EscortConstant.CARGO_STATUE_PROTECT){//被保护中
            updateCargoListScene(roleId,(byte)0);
            PlayerUtil.send(roleId, new ClientText(I18n.get("escort.inProtect")));
            return;
        }

        BaseTeam team = null;
        if(escortType == EscortConstant.ESCORT_TYPE_TEAM){//组队劫镖需要进行队伍判断
            team = ServiceHelper.baseTeamService().getTeam(roleId);
            if(team == null){//队伍不存在
                PlayerUtil.send(roleId,new ClientText("team_teamNotExist"));
                return;
            }
        }

        if(useMask){//使用了面具，发送消耗面具事件
            NoticeConsumeMaskEvent event = new NoticeConsumeMaskEvent();
            ServiceHelper.roleService().notice(roleId, event);
        }


        scene.addRobbedTimes(); //增加被抢次数
        scene.setStatus(EscortConstant.SCENE_STATUS_IN_PVP);
        sendStopCargoCarOrder(scene);//发送镖车停止移动指令
        //发送改变战斗状态指令
        sendChangeFightStateOrder(scene, ServerOrder.FIGHT_STATE_FIGHTING,(int)(EscortManager.getCargocarFightTime() * DateUtil.SECOND));

        //押镖方播放强敌来袭特效
        ClientEscort client = new ClientEscort(ClientEscort.RESP_PLAY_ENEMY_COME_AMJ);
        scene.sendPacketToAllEscorter(client);

        //仇人buff
        boolean isEnemy = po.getIsEnemy() == 1;
        StringBuilder sb = new StringBuilder();
        sb.append("defaultbuff=").append(EscortManager.getCargocarEnemyBuffId()).append("+")
                .append(EscortManager.getCargocarEnemyBuffLevel()).append("+").append("-1;");

        List<FighterEntity> list;
        if(escortType == EscortConstant.ESCORT_TYPE_TEAM){//组队
            initTeamRobber(scene,EscortConstant.CAMP_ROB,team,scene.getCargoNextTargetPos(2));

            //加入战斗实体
            for(Escorter escorter:scene.getRobberMap().values()){
                list = new ArrayList<>();
                if(escorter.getRoleId() == roleId){//队长标识使用面具
                    escorter.setUseMask(useMask);
                    escorter.setRemainRobTimes(remainRobTimes);
                }
                if(isEnemy){//增加仇人Buff
                    escorter.getPlayerEntity().addExtraValue(sb.toString());
                }
                list.add(escorter.getPlayerEntity());
                //将劫镖战斗实体加入押镖场景
                MainRpcHelper.fightBaseService().addFighter(scene.getFightServer(),
                        FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), scene.getFightId(), list);
            }

            team.setFight(true);
        }else{//个人
            list = new ArrayList<>();
            Escorter escorter = createSingleEscorter(scene.getFightId(),roleId,moduleMap,EscortConstant.CAMP_ROB,scene.getCargoNextTargetPos(2));
            escorter.setUseMask(useMask);
            escorter.setRemainRobTimes(remainRobTimes);
            scene.addRobber(escorter);
            escorterMap.put(escorter.getRoleId(), escorter);

            if(isEnemy){//增加仇人Buff
                escorter.getPlayerEntity().addExtraValue(sb.toString());
            }
            list.add(escorter.getPlayerEntity());   //玩家战斗实体
            if(StringUtil.isNotEmpty(escorter.getOtherEntities())){//伙伴实体
                list.addAll(escorter.getOtherEntities());

                //发送新增伙伴monster产品数据指令
                addMonsterVoServerOrder(scene.getFightId(),
                        SceneManager.getMonsterVo(escorter.getOtherEntities().get(0).getModelId()));
            }
            //将劫镖战斗实体加入押镖场景
            MainRpcHelper.fightBaseService().addFighter(scene.getFightServer(),
                    FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), scene.getFightId(), list);
        }

        //构建进入场景包并进行缓存
        List<FighterEntity> entityList = new ArrayList<>();
        entityList.addAll(scene.getEntityMap().values());
        ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_ESCORT_FIGHT);
        enterPack.setStageId(EscortManager.getCargocarStageid());
        enterPack.setLimitTime(EscortManager.ESCORT_TIME);// 限制时间
        enterPack.setFighterEntityList(entityList);
        enterPack.addBuffData(getEnemyBuffIdMap());
        addAllJobSkill(enterPack);
        byte[] bytes = packetToBytes(enterPack);
        dataMap.put(scene.getFightId(),bytes);

        CACHE_MAP.remove(roleId);

        noticeDailyEvent(scene.getRobberMap().values());//日常活动参与事件
    }

    @Override
    public void robRobotCargo(long roleId, int sectionId, byte escortType, Map<String, Module> moduleMap) {
        if(true){
            //暂时屏蔽抢劫机器人镖车
            return;
        }
        CargoAIVo aiVo = EscortManager.getCargoAiVoByPowerSection(sectionId);
        if(aiVo == null) return;
        StageinfoVo stageVo = SceneManager.getStageVo(aiVo.getStageId());
        if (stageVo == null) return;
        EscortModule escortModule = (EscortModule) moduleMap.get(MConst.Escort);
        if(escortModule == null) return;

        EscortRobScene scene = (EscortRobScene) SceneManager.newScene(SceneManager.SCENETYPE_ROB_ROBOT);
        scene.setEscortType(escortType);
        scene.setSectionId(sectionId);
        scene.stageId = stageVo.getStageId();
        scene.setLeaderRemainRobTimes(escortModule.getRoleEscort().getRemainRobTime());
        scene.setLeaderId(roleId);

        BaseTeam team = null;
        if(escortType == EscortConstant.ESCORT_TYPE_TEAM){
            team = ServiceHelper.baseTeamService().getTeam(roleId);
            if(team == null) return;
            scene.setTeamId(team.getTeamId());
            scene.addTeamMemberFighter(team.getMembers().values());

            team.setFight(true);

//            DailyFuntionEvent event = new DailyFuntionEvent(DailyManager.DAILYID_ESCORT, 1);
            for(BaseTeamMember member:team.getMembers().values()) {
//                ServiceHelper.roleService().notice(member.getRoleId(), new RoleNotification(event));

                ROB_SCENE_MAP.put(member.getRoleId(),scene);
            }
        }else{
            scene.addSinglePlayerFighter(roleId,moduleMap,stageVo);

//            DailyFuntionEvent event = new DailyFuntionEvent(DailyManager.DAILYID_ESCORT, 1);
//            ServiceHelper.roleService().notice(roleId, new RoleNotification(event));

            ROB_SCENE_MAP.put(roleId,scene);
        }
        if(!scene.canEnter(null,sectionId)) return;
        scene.enter(null,null);

        if(team!=null) {
            team.setFight(Boolean.TRUE);
        }
        CACHE_MAP.remove(roleId);
    }

    /**
     * 战斗数据处理
     */
    @Override
    public void doFightFramData(int serverId, String fightId, LuaFrameData lData){
        EscortScene scene = SCENE_MAP.get(fightId);
        if(scene == null) return;

        recordDeadPos(scene,lData);     //记录死亡位置
        synAndCheckCarPos(scene,lData); //同步镖车位置 & 检测是否结束
    }

    /**
     * 记录死亡位置
     */
    private void recordDeadPos(EscortScene scene,LuaFrameData lData){
        if(lData == null|| StringUtil.isEmpty(lData.getDeadPos())) return;

        FighterEntity fighterEntity;
        Escorter escorter;
        for(Map.Entry<String, String> entry:lData.getDeadPos().entrySet()) {
            fighterEntity = scene.getEntityMap().get(entry.getKey());
            if(fighterEntity.getFighterType() == FighterEntity.TYPE_PLAYER){
                escorter = escorterMap.get(Long.parseLong(fighterEntity.getUniqueId()));
                if(escorter == null) continue;
                escorter.setDeadPos(entry.getValue());
            }
        }
    }

    /**
     * 同步镖车位置 & 检测是否结束
     */
    private void synAndCheckCarPos(EscortScene scene,LuaFrameData lData){
        if(lData == null || StringUtil.isEmpty(lData.getCargoPosition())) return;
        if(scene == null || scene.isInPvp() || scene.isPause()) return;//pvp中不同步镖车位置
        EscortCargoPosTarget posTarget = scene.getCargoCurPosTarget();
        if(StringUtil.isEmpty(posTarget.getPosition())) return;

        scene.setCarCurPos(lData.getCargoPosition());
        Vector3 curVec = new Vector3(lData.getCargoPosition());     //客户端单位是分米
        Vector3 curTarget = new Vector3(posTarget.getPosition());   //服务端配置的是分米

        Vector3 vec = Vector3.sub(curTarget, curVec);
        double dis = vec.sqrMagnitude();
        if(dis <= 100){
            posTarget.setFinish(true);

            posTarget = scene.getCargoCurPosTarget();//存在下一目标点,改变镖车位置目标
            if(posTarget!=null) {
                sendCargoCarBeginMoveOrder(scene);
            }else{
                handlerEscortSuccess(scene);//到达终点,完成押镖
            }
        }
    }

    /**
     * 完成押镖处理
     */
    private void handlerEscortSuccess(EscortScene scene){
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(escorter == null||escorter.isOffline()) continue;
            handlerFinish(scene,escorter);//结算处理
        }

        Escorter leader = scene.getEscorterMap().get(scene.getRoleId());
        if(!leader.isOffline()){
            ServiceHelper.baseTeamService().disbandTeam(scene.getRoleId());//解散队伍
        }
        destoryFightScene(scene,true);//删除运镖战斗场景相关缓存
    }

    /**
     * 获取运镖结束奖励
     */
    private Map<Integer,Integer> getFinishAward(Escorter escorter,EscortScene scene){
        CargoCarVo carVo = EscortManager.getCargoCarVoById(scene.getCarId());
        if(carVo == null) return null;

        Map<Integer,Integer> award= new HashMap<>();
        if(escorter.getRoleId() == scene.getRoleId()){//队长得到运镖奖励
            award.putAll(getAwardAfterLose(carVo.getCarAwardMap(),scene));
        }else{//队员获得护卫奖励
            award.putAll(carVo.getEscortsAwardMap());
        }

        if(EscortActivityFlow.isStarted()){//活动时间内翻倍
            award = getDoubleMap(award);
        }
        return award;
    }

    /**
     * 获取护镖成功奖励
     */
    private Map<Integer,Integer> getEscortsAward(int carId){
        CargoCarVo carVo = EscortManager.getCargoCarVoById(carId);
        if(carVo == null) return null;
        Map<Integer,Integer> award = new HashMap<>();
        award.putAll(carVo.getEscortsAwardMap());
        if(EscortActivityFlow.isStarted()){//活动时间内翻倍
            award = getDoubleMap(award);
        }
        return award;
    }

    /**
     * 获取劫镖成功奖励
     */
    private Map<Integer,Integer> getRobAward(int carId){
        CargoCarVo carVo = EscortManager.getCargoCarVoById(carId);
        if(carVo == null) return null;
        Map<Integer,Integer> award = new HashMap<>();
        award.putAll(carVo.getRobAwardMap());
        if(EscortActivityFlow.isStarted()){//活动时间内翻倍
            award = getDoubleMap(award);
        }
        return award;
    }

    /**
     * 获得损失后的运镖奖励
     */
    private Map<Integer,Integer> getAwardAfterLose(Map<Integer,Integer> award,EscortScene scene){
        if(scene.getBeenRobbedSuccess() <= 0) return award;
        int lostPercent = EscortManager.getLoseCargo(scene.getBeenRobbedSuccess());
        if(lostPercent <= 0) return award;

        Map<Integer, Integer> tmpMap = new HashMap<>();
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : award.entrySet()) {
            count = entry.getValue() * (100 - lostPercent) / 100;
            if(count <= 0) count = 1;
            tmpMap.put(entry.getKey(), count);
        }
        return tmpMap;
    }

    /**
     * 获得双倍奖励
     */
    private Map<Integer,Integer> getDoubleMap(Map<Integer,Integer> award){
        Map<Integer,Integer> doubleMap = new HashMap<>();
        for(Map.Entry<Integer,Integer> entry:award.entrySet()){
            doubleMap.put(entry.getKey(),entry.getValue() * EscortManager.getCargocarDouble() / 100);
        }
        return doubleMap;
    }

    /**
     * 运镖结算处理
     */
    private void handlerFinish(EscortScene scene,Escorter escorter){
        Map<Integer,Integer> finishAward = getFinishAward(escorter,scene);//运镖结束奖励
        NoticeServerAddEscortAwardEvent event;
        if(escorter.getRoleId() == scene.getRoleId()) {//队长
            event = new NoticeServerAddEscortAwardEvent(EscortConstant.SUB_TYPE_ESCORT_SUCCESS,finishAward,scene.getCarId(),scene.getIndex());
        }else{
            event = new NoticeServerAddEscortAwardEvent(EscortConstant.SUB_TYPE_GUIDE_SUCCESS,finishAward);
        }
        ServiceHelper.roleService().notice(escorter.getRoleId(), event);//处理运镖相关奖励增加 & 活动参与次数增加 & 设置镖车冷却时间

        ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_ESCORT_FINISH);
        packet.setItemMap(finishAward);
        packet.setRobTimes(scene.getBeenRobbedSuccess());
        packet.setLosePercent((byte) EscortManager.getLoseCargo(scene.getBeenRobbedSuccess()));
        packet.setDoubleAward(EscortActivityFlow.isStarted()?(byte)1:(byte)0);
        PlayerUtil.send(escorter.getRoleId(), packet);//下发运镖结束界面

        //路由切回本服
        MultiServerHelper.modifyConnectorRoute(Long.valueOf(escorter.getRoleId()), MultiServerHelper.getServerId());
    }

    /**
     * 删除运镖战斗场景相关缓存
     */
    private void destoryFightScene(EscortScene scene,boolean force){
        removeEscortSceneInCache(scene.getFightId());//移除运镖缓存

        for(Escorter escorter:scene.getEscorterMap().values()){
            if(escorter == null) continue;
            if(escorterMap.containsKey(escorter.getRoleId())) {
                escorterMap.remove(escorter.getRoleId());
            }
        }

        if(force) {
            for (Escorter escorter : scene.getRobberMap().values()) {
                if (escorter == null) continue;
                if (escorterMap.containsKey(escorter.getRoleId())) {
                    escorterMap.remove(escorter.getRoleId());
                }
            }
        }

        //关闭战斗
        MainRpcHelper.fightBaseService().stopFight(scene.getFightServer(),
                FightConst.T_FIGHTING_MASTER, MultiServerHelper.getServerId(), scene.getFightId());
    }

    /**
     * 劫镖超时处理
     */
    @Override
    public void handleTimeOut(int fromServerId, String fightId, HashMap<String, String> hpInfo){
        EscortScene scene = SCENE_MAP.get(fightId);
        if(scene == null) return;
        if(!scene.isInPvp()) return;    //不在pvp中,运镖不进行超时处理
        if(scene.isPause()) return;
        if(scene.isInProtect()) return; //被保护中

        //超时 劫镖方失败,运镖方防守胜利处理
        handlePvpProtectSuccess(scene);
    }

    /**
     * 死亡处理
     */
    @Override
    public void handleDead(int serverId, String fightId, Map<String, String> deadMap){
        EscortScene scene = SCENE_MAP.get(fightId);
        if(scene == null) return;

        boolean isEnd;
        isEnd = checkAndHandleCargoCarDead(scene,deadMap);//检测镖车是否死亡
        if(isEnd) return;

        // 处理死亡标识
        for (Map.Entry<String, String> entry : deadMap.entrySet()) {
            FighterEntity fighterEntity = scene.getEntityMap().get(entry.getKey());
            if(fighterEntity.getFighterType() != FighterEntity.TYPE_PLAYER) continue;

            Escorter escorter = escorterMap.get(Long.parseLong(entry.getKey()));
            if(escorter == null) continue;
            escorter.setDead(true);
        }

        if(scene.isPause()) return;
        if(scene.isInProtect()) return; //被保护中

        isEnd = checkAndHandleEscorterDead(scene);//检测运镖方死亡
        if(isEnd) return;

        checkAndHandleRobberDead(scene);//检测劫镖方死亡
    }

    //检测运镖方死亡
    private boolean checkAndHandleEscorterDead(EscortScene scene){
        boolean isAllDead = true;
        //判断整队死亡
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(!escorter.isDead()) isAllDead = false;
        }
        if(isAllDead){//运镖方全部阵亡
            handleProtectFail(scene);//运镖方失败处理
            return true;
        }
        return false;
    }

    //检测劫镖方死亡
    private boolean checkAndHandleRobberDead(EscortScene scene){
        if(!scene.isInPvp()) return false;
        boolean isAllDead = true;
        //判断整队死亡
        for(Escorter escorter:scene.getRobberMap().values()){
            if(!escorter.isDead()) isAllDead = false;
        }
        if(isAllDead){//劫镖方全部阵亡
            handlePvpProtectSuccess(scene);
            return true;
        }
        return false;
    }

    //运镖方失败处理
    private void handleProtectFail(EscortScene scene){
        if(scene.isInPvp()){//押镖失败(pvp劫镖成功)
            handlePvpProtectFail(scene);
        }else{//运镖失败(pve失败,直接结束)
            handlePveFail(scene);
        }
    }

    //镖车死亡判断
    private boolean checkAndHandleCargoCarDead(EscortScene scene,Map<String, String> deadMap){
        if(deadMap.keySet().contains(EscortConstant.CARGO_CAR_FIGHT_ID)){//镖车死亡,劫镖队伍胜利
            scene.setCargoCarDead(true);
            if(scene.isPause()) return true;
            if(scene.isInProtect()) return true; //被保护中
            handleProtectFail(scene);//运镖方失败处理
            return true;
        }
        return false;
    }

    //运镖方失败,劫镖方胜利处理
    private void handlePvpProtectFail(EscortScene scene){
        scene.setStatus(EscortConstant.SCENE_STATUS_PAUSE);//场景状态改为状态
        scene.setProtectTimes();

        scene.addBeenRobbedSuccess();   //增加被劫成功次数
        //暂停战斗
        sendChangeFightStateOrder(scene, ServerOrder.FIGHT_STATE_PAUSE, 99999);

        //发送运镖方护镖失败界面
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(escorter == null || escorter.isOffline()) continue;
            ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_ESCORT_FAIL);
            packet.setLosePercent((byte) EscortManager.getLoseCargo(scene.getBeenRobbedSuccess()));
            PlayerUtil.send(escorter.getRoleId(),packet);

            //路由切暂时切回本服
            MultiServerHelper.modifyConnectorRoute(Long.valueOf(escorter.getRoleId()), MultiServerHelper.getServerId());
        }

        //移除劫镖方战斗实体
        List<Long> enemyList = new ArrayList<>();//仇人列表
        Map<Integer,Integer> robAward = getRobAward(scene.getCarId());
        List<Long> removeFighterId = new ArrayList<>();
        //发送劫镖方胜利界面
        for(Escorter escorter:scene.getRobberMap().values()){
            if(escorter == null || escorter.isOffline()) continue;
            NoticeServerAddEscortAwardEvent event = new NoticeServerAddEscortAwardEvent(EscortConstant.SUB_TYPE_ROB_SUCCESS,robAward);
            ServiceHelper.roleService().notice(escorter.getRoleId(), event);//发送增加奖励事件

            ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_ROB_SUCCESS);
            packet.setItemMap(robAward);
            packet.setDoubleAward(EscortActivityFlow.isStarted()?(byte)1:(byte)0);
            PlayerUtil.send(escorter.getRoleId(), packet);

            //路由切回本服
            MultiServerHelper.modifyConnectorRoute(Long.valueOf(escorter.getRoleId()), MultiServerHelper.getServerId());
            if(!escorter.isUseMask()) {//没有使用面具,列为仇人
                enemyList.add(escorter.getRoleId());
            }
            removeFighterId.add(escorter.getRoleId());
        }

        //给运镖队长新增仇人
        Escorter leader = scene.getEscorterMap().get(scene.getRoleId());
        if(leader != null && !leader.isOffline()) {//没离线，则增加仇人
            NoticeServerAddEnemyRecordEvent event = new NoticeServerAddEnemyRecordEvent(enemyList);
            ServiceHelper.roleService().notice(scene.getRoleId(), event);//发送增加奖励事件
        }

        //将劫镖者信息移到临时缓存区
        ROB_TEMP_CACHE.put(scene.getFightId(),new RobTempCache(scene.getRobberMap(),scene.getFightId()));

        //将劫镖方从fightActor移除
        MainRpcHelper.fightBaseService().removeFromFightActor(scene.getFightServer(),
                FightConst.T_FIGHTING_MASTER, MultiServerHelper.getServerId(), scene.getFightId(),removeFighterId);

        escorterOfflineCheck(scene);//运镖方掉线检测
    }

    //劫镖方失败,运镖方防守胜利处理
    private void handlePvpProtectSuccess(EscortScene scene){
        scene.setStatus(EscortConstant.SCENE_STATUS_PAUSE);//场景状态改为状态
        scene.setProtectTimes();

        //暂停战斗
        sendChangeFightStateOrder(scene, ServerOrder.FIGHT_STATE_PAUSE, 99999);

        Map<Integer,Integer> escortAward = getEscortsAward(scene.getCarId());
        //发送运镖方护镖成功界面
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(escorter == null||escorter.isOffline()) continue;
            NoticeServerAddEscortAwardEvent event = new NoticeServerAddEscortAwardEvent(EscortConstant.SUB_TYPE_NONE,escortAward);
            ServiceHelper.roleService().notice(escorter.getRoleId(), event);//发送增加奖励事件

            ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_ESCORT_SUCCESS);
            packet.setItemMap(escortAward);
            packet.setDoubleAward(EscortActivityFlow.isStarted()?(byte)1:(byte)0);
            PlayerUtil.send(escorter.getRoleId(),packet);

            //路由切暂时切回本服
            MultiServerHelper.modifyConnectorRoute(Long.valueOf(escorter.getRoleId()), MultiServerHelper.getServerId());
        }

        //发送劫镖方失败界面
        List<Long> removeFighterId = new ArrayList<>();
        for(Escorter escorter:scene.getRobberMap().values()){
            if(escorter == null||escorter.isOffline()) continue;
            ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_ROB_FAIL);
            PlayerUtil.send(escorter.getRoleId(),packet);

            //路由切回本服
            MultiServerHelper.modifyConnectorRoute(Long.valueOf(escorter.getRoleId()), MultiServerHelper.getServerId());
            removeFighterId.add(escorter.getRoleId());
        }
        //将劫镖者信息移到临时缓存区
        ROB_TEMP_CACHE.put(scene.getFightId(),new RobTempCache(scene.getRobberMap(),scene.getFightId()));

        //将劫镖方从fightActor移除
        MainRpcHelper.fightBaseService().removeFromFightActor(scene.getFightServer(),
                FightConst.T_FIGHTING_MASTER, MultiServerHelper.getServerId(), scene.getFightId(),removeFighterId);

        escorterOfflineCheck(scene);//运镖方掉线检测
    }

    /**
     * pve运镖失败，直接结束
     */
    private void handlePveFail(EscortScene scene){
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(escorter == null||escorter.isOffline()) continue;
            ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_PVE_ESCORT_FAIL);
            PlayerUtil.send(escorter.getRoleId(),packet);

            //路由切回本服
            MultiServerHelper.modifyConnectorRoute(Long.valueOf(escorter.getRoleId()), MultiServerHelper.getServerId());
        }
        destoryFightScene(scene, true);//删除运镖战斗场景相关缓存

        Escorter leader = scene.getEscorterMap().get(scene.getRoleId());
        if(!leader.isOffline()){
            ServiceHelper.baseTeamService().disbandTeam(leader.getRoleId());
        }
    }

    /**
     * 继续运镖
     */
    @Override
    public void escortContinue(long roleId){
        Escorter player = escorterMap.get(roleId);
        if(player == null) return;

        EscortScene scene = SCENE_MAP.get(player.getFightId());
        if(scene == null || !scene.isPause()) return;//不在暂停中

        for(Escorter escort:scene.getEscorterMap().values()) {
            if(escort.isOffline()) continue;
            MultiServerHelper.modifyConnectorRoute(escort.getRoleId(), scene.getFightServer());
        }

        //移除劫镖方战斗实体
        List<String> removeFighter = new ArrayList<>();
        //发送劫镖方失败界面
        for(Escorter escorter:scene.getRobberMap().values()){
            if(escorter == null || escorter.isDead()) continue;//死亡跳过
            removeFighter.add(String.valueOf(escorter.getRoleId()));
        }
        scene.setRobberMap(null);
        if(StringUtil.isNotEmpty(removeFighter)) {
            removePlayer(scene.getFightId(), removeFighter);
        }

        resetEcorterStatue(scene);//重置运镖者 & 镖车状态
        sendChangeFightStateOrder(scene, ServerOrder.FIGHT_STATE_FIGHTING, 0);//改变战斗状态,继续运镖
        sendCargoCarBeginMoveOrder(scene);//发送镖车开始移动指令

        //发送关闭界面，继续战斗包
        ClientEscort client = new ClientEscort(ClientEscort.RESP_ESCORT_CONTINUES);
        scene.sendPacketToAllEscorter(client);

        scene.setStatus(EscortConstant.SCENE_STATUS_NORMAL);//解除暂停状态
    }

    @Override
    public void receiveFightPacket(PlayerPacket packet) {
        long roleId = packet.getRoleId();
        EscortRobScene scene = ROB_SCENE_MAP.get(roleId);
        if(scene == null) return;
        scene.receivePacket(null, packet);
    }

    /**
     * 战斗中的掉线处理
     */
    @Override
    public void handleOffline(int fromServerId, String fightId, long roleId) {
        handleOfflineInEscortScene(roleId);//运镖场景掉线处理
    }

    /**
     * pve劫镖关卡掉线处理
     */
    private void handleOfflineInPveRobScene(long roleId){
        EscortRobScene scene = ROB_SCENE_MAP.get(roleId);
        if(scene == null) return;

        scene.exit(roleId);
        ROB_SCENE_MAP.remove(roleId);
    }

    /**
     * 非战斗场景掉线处理
     */
    @Override
    public void handleOffline(long roleId){
        handleOfflineInEscortScene(roleId);//运镖场景掉线处理
//        handleOfflineInPveRobScene(roleId);//pve劫镖关卡掉线处理
        handleOfflineInSafeScene(roleId);// 镖车队列场景/组队界面  的掉线/回城处理
        removeFromEscortPermitSet(roleId);  //运镖组队权限掉线处理
        removeFromRobPermitSet(roleId);     //押镖组队权限掉线处理
    }

    /**
     * 镖车队列场景的掉线/回城处理
     */
    public void handleOfflineInSafeScene(long roleId){
        CargoListCache cache = CACHE_MAP.get(roleId);
        if(cache!=null){//队长
            BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
            if(team!=null){
                for (BaseTeamMember teamMember : team.getMembers().values()) {
                    if (teamMember.getRoleId() == roleId) continue;
                    if (!teamMember.isPlayer()) continue;
                    ServiceHelper.roleService().notice(teamMember.getRoleId(), new CheckRobTimesBackCityEvent());
                }
            }

            //解散队伍，如果存在
            ServiceHelper.baseTeamService().disbandTeam(roleId);
            CACHE_MAP.remove(roleId);
        }else{//队员
            BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
            if(team == null) return;
            if(team.getTeamType() == BaseTeamManager.TEAM_TYPE_CARGO_ROB) {
                ServiceHelper.baseTeamService().leaveTeam(roleId);//离开劫镖队伍
            }else if(team.getTeamType() == BaseTeamManager.TEAM_TYPE_ESCORT){
                if(team.getCaptainId() == roleId){
                    ServiceHelper.baseTeamService().disbandTeam(roleId);
                }else{
                    ServiceHelper.baseTeamService().leaveTeam(roleId);
                }
            }
        }
    }

    /**
     * 是否在pve劫镖关卡
     */
    private boolean isInPveRobScene(long roleId){
        return ROB_SCENE_MAP.containsKey(roleId);
    }

    /**
     * 组队pve关卡结束离场处理
     */
    private void handleLeavePveRobSence(long roleId, byte success){
        EscortRobScene scene = ROB_SCENE_MAP.get(roleId);
        if(scene == null) return;
        boolean leaderOnline = scene.getMemberRoleIds().contains(scene.getLeaderId());//队长是否在线
        if(!leaderOnline || (scene.getLeaderRemainRobTimes() <= 1 && success == 1)){//队长掉线 || 队长劫镖成功并次数用完
            if(scene.getTeamId() != 0) {
                ServiceHelper.baseTeamService().disbandTeam(scene.getTeamId());//解散队伍
            }

            //回城并检测次数打开劫镖组队界面
            for(long id:scene.getMemberRoleIds()) {
                ServiceHelper.roleService().notice(id, new CheckRobTimesBackCityEvent());
            }
            return;
        }

        //发事件给队长，请求进入队列场景
        ServiceHelper.roleService().notice(scene.getLeaderId(), new EnterCargoListSceneEvent());

        for(long id:scene.getMemberRoleIds()) {
            ROB_SCENE_MAP.remove(id);
        }
    }

    /**
     * 劫镖后回城处理
     */
    @Override
    public void leaveSceneAfterRob(long roleId, byte success) {
//        //pve关卡判断
//        if(isInPveRobScene(roleId)){
//            handleLeavePveRobSence(roleId, success);//pve劫镖关卡结束离场处理
//            return;
//        }

        //pvp劫镖离场处理
        Escorter escorter = escorterMap.get(roleId);
        if(escorter == null || escorter.isOffline()) return;

        RobTempCache robTempCache = ROB_TEMP_CACHE.get(escorter.getFightId());
        if(robTempCache == null) return;

        Escorter leader = robTempCache.getRobberMap().get(escorter.getLeaderId());
        if(leader == null) {
            ServiceHelper.baseTeamService().disbandTeam(escorter.getTeamId());//解散队伍

            //回城并打开劫镖组队界面
            for(Escorter robber:robTempCache.getRobberMap().values()){
                if(robber == null || robber.isOffline()) continue;
                ServiceHelper.roleService().notice(robber.getRoleId(), new CheckRobTimesBackCityEvent());
            }
            return;
        }
        if(leader.isOffline() || (leader.getRemainRobTimes() <= 1 && success == 1)){//队长掉线或者次数不足,全部回城
            ServiceHelper.baseTeamService().disbandTeam(leader.getTeamId());//解散队伍

            //回城并打开劫镖组队界面
            for(Escorter robber:robTempCache.getRobberMap().values()){
                if(robber == null || robber.isOffline()) continue;
                ServiceHelper.roleService().notice(robber.getRoleId(), new CheckRobTimesBackCityEvent());
            }
            return;
        }

        //发事件给队长，请求进入队列场景
        ServiceHelper.roleService().notice(leader.getRoleId(), new EnterCargoListSceneEvent());

        ROB_TEMP_CACHE.remove(escorter.getFightId());
        for(Escorter robber:robTempCache.getRobberMap().values()){
            if(robber==null) continue;
            if(escorterMap.containsKey(robber.getRoleId())){
                escorterMap.remove(robber.getRoleId());
            }
        }
        checkOutTimeCache();//检测并移除过时缓存
    }

    //检测并移除过时缓存
    private void checkOutTimeCache(){
        List<RobTempCache> removeList = new ArrayList<>();
        for(RobTempCache cache:ROB_TEMP_CACHE.values()){
            if(cache==null) continue;
            if(cache.isTimeOut()){
                removeList.add(cache);
            }
        }

        for(RobTempCache cache:removeList){
            ROB_TEMP_CACHE.remove(cache.getFightId());
        }
    }

    private void handleOfflineInEscortScene(long roleId){
        Escorter escorter = escorterMap.get(roleId);
        if(escorter == null || escorter.isOffline()) return;
        escorter.setOffline(true);

        //先从总缓存中剔除,防止再次押镖的冲突
        if(escorterMap.containsKey(escorter.getRoleId())) {
            escorterMap.remove(escorter.getRoleId());
        }

        EscortScene scene = SCENE_MAP.get(escorter.getFightId());
        if(scene == null) return;
        if(scene.getRoleId() == escorter.getRoleId() || escorter.getRoleId() == escorter.getLeaderId()){//如果是队长,需要解散队伍
            scene.setLeaderOffline(true);
            ServiceHelper.baseTeamService().disbandTeam(escorter.getRoleId());
        }else{//不是队长,离开队伍即可
            ServiceHelper.baseTeamService().leaveTeam(escorter.getRoleId());
        }

        offlineCheck(scene);//掉线检测
    }

    /**
     * 运镖方掉线检测
     */
    private void escorterOfflineCheck(EscortScene scene){
        if(scene == null) return;
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(!escorter.isOffline()) return;
        }
        //全部玩家掉线，关闭战斗场景
        destoryFightScene(scene,false);//删除运镖战斗场景相关缓存
    }

    /**
     * 掉线检测
     */
    private void offlineCheck(EscortScene scene){
        if(scene == null) return;
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(!escorter.isOffline()) return;
        }
        for(Escorter escorter:scene.getRobberMap().values()){
            if(!escorter.isOffline()) return;
        }
        //全部玩家掉线，关闭战斗场景
        destoryFightScene(scene,true);//删除运镖战斗场景相关缓存
    }

    /**
     * 重置运镖者 & 镖车状态(复活/满血满状态)
     */
    private void resetEcorterStatue(EscortScene scene){
        ArrayList<String> resetStateList = new ArrayList<>();
        if(scene.isCargoCarDead()){//镖车死亡,需要重新添加新镖车
            List<FighterEntity> carlist = new ArrayList<>();
            FighterEntity carEntity = scene.getCargoCarEntity();
            carEntity.setPosition(scene.getCarCurPos());
            carlist.add(carEntity);
            MainRpcHelper.fightBaseService().addMonster(scene.getFightServer(),
                    FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), scene.getFightId(), carlist);
        }else{
            resetStateList.add(EscortConstant.CARGO_CAR_FIGHT_ID);//重置为满血满状态
        }

        List<FighterEntity> list = new ArrayList<>();
        for(Escorter escorter:scene.getEscorterMap().values()){
            if(escorter == null) continue;
            if(escorter.isDead()){
                escorter.getPlayerEntity().setPosition(escorter.getDeadPos());
                escorter.getPlayerEntity().setState((byte)1);
                list.add(escorter.getPlayerEntity());
                escorter.setDead(false);
                escorter.setDeadPos("");
                escorter.setRevive(true);//重生
            }else{
                resetStateList.add(String.valueOf(escorter.getRoleId()));//重置为满血满状态
            }
        }
        //复活玩家实体
        MainRpcHelper.fightBaseService().addFighter(scene.getFightServer(),
                FightConst.T_ESCORT_CARGO, MultiServerHelper.getServerId(), scene.getFightId(), list);

        //发送满血满状态指令
        ServerOrder serverOrder = new ServerOrder();
        serverOrder.setOrderType(ServerOrder.ORDER_TYPE_RESET_CHARACS);
        serverOrder.setUniqueIDs(resetStateList);
        sendServerOrder(scene.getFightId(),serverOrder);//发送服务端lua命令
    }

    public void addToEscortPermitSet(long roleId){
        escortPermitSet.add(roleId);
    }

    public void removeFromEscortPermitSet(long roleId){
        escortPermitSet.remove(roleId);
    }

    public void addToRobPermitSet(long roleId){
        robPermitSet.add(roleId);
    }

    public void removeFromRobPermitSet(long roleId){
        robPermitSet.remove(roleId);
    }

    public boolean hasJoinEscortTeamPermit(long roleId){
        return escortPermitSet.contains(roleId);
    }

    public boolean hasJoinRobTeamPermit(long roleId){
        return robPermitSet.contains(roleId);
    }

}
