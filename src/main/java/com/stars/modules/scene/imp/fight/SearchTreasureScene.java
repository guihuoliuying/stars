package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.drop.DropManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.drop.prodata.DropVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.searchtreasure.SearchTreasureConstant;
import com.stars.modules.searchtreasure.SearchTreasureManager;
import com.stars.modules.searchtreasure.SearchTreasureModule;
import com.stars.modules.searchtreasure.packet.ClientSearchTreasureInfo;
import com.stars.modules.searchtreasure.prodata.SearchContentVo;
import com.stars.modules.searchtreasure.prodata.SearchMapVo;
import com.stars.modules.searchtreasure.prodata.SearchStageVo;
import com.stars.modules.searchtreasure.recordmap.RecordMapSearchTreasure;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.I18n;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * 仙山夺宝场景;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchTreasureScene extends FightScene {

    private ServerLogModule serverLogModule;
    private SearchTreasureModule searchTreasureModule;
    private SearchStageVo searchStageVo = null;

    /**
     * 能否进入
     *
     * @return
     */
    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        int requestMapId = (int) obj;
        boolean isCanEnter = false;
        do {
            int recordMapId = recordMapSearchTreasure.getMapId();
            //判断下当前是否已经有记录了;
            if (recordMapId > 0) {
                if (recordMapId != requestMapId) {
                    if (recordMapSearchTreasure.getMapSearchState() != SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_GETTED) {
                        searchTreasureModule.warn(recordMapId + "地图还没有探索完成并领取奖励,不能进入其他地图 " + requestMapId);
                        break;
                    }
                }
            }
            //判断还有没有次数进入;
            int remainInCount = recordMapSearchTreasure.getDailyRemainInCount();
            if (remainInCount <= 0 && recordMapId != requestMapId) {
                searchTreasureModule.warn(I18n.get("searchtreasure.noenoughtimes"));
                break;
            }
            //判断请求进入的地图是否满足等级需求;
            SearchMapVo requestSearchMapVo = SearchTreasureManager.getSearchMapVo(requestMapId);
            RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
            if (roleModule.getLevel() < requestSearchMapVo.getLevelLimit()) {
                searchTreasureModule.warn("请求进入的地图" + requestMapId + " 不满足等级! 需求等级" + requestSearchMapVo.getLevelLimit());
                break;
            }
            isCanEnter = true;
        } while (false);
        return isCanEnter;
    }

    /**
     * 进入场景
     * 调用之前必须经过判断
     */
    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        entityMap.clear();
        int requestMapId = (int) obj;
        searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        do {
            //判断设置新地图;
            if (recordMapSearchTreasure.getMapId() == requestMapId) {
                //判断是否是完成并领取奖励了;
                if (recordMapSearchTreasure.getMapSearchState() != SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_GETTED) {
                    break;
                }
            }
            recordMapSearchTreasure.setNewSearchMapId(requestMapId, false);
            Role role = ((RoleModule) moduleMap.get(MConst.Role)).getRoleRow();
            recordMapSearchTreasure.setRemainHp(role.getTotalAttr().getHp());
            recordMapSearchTreasure.setMapState(recordMapSearchTreasure.getMapId(), SearchTreasureConstant.SEARCH_PROCESS_ING, true);
            searchTreasureModule.dispatchDailyEvent();
            recordMapSearchTreasure.syncToClient(ClientSearchTreasureInfo.TYPE_INFO);
        } while (false);
        //判断当前的层次状态;
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        searchStageVo = recordMapSearchTreasure.getCurSearchStageVo();
        this.stageId = searchStageVo.getStageinfoid();
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        requestSendClientEnterFight(moduleMap, enterFight, recordMapSearchTreasure);
        //判断是否上次还有怪没杀死;
        Map<String, FighterEntity> lastHasnotKillMonsterMap = checkGetLastProducedMonsters(moduleMap);
        ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
        clientSpawnMonster.setSpawnMonsterMap(lastHasnotKillMonsterMap);
        // 先发剧情播放记录
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.sendPlayedDrama(this, stageId);
        searchTreasureModule.send(clientSpawnMonster);
        //下发当前索引点信息;
        recordMapSearchTreasure.syncToClient(ClientSearchTreasureInfo.TYPE_SYNC_SEARCHPOINT);
        //打印日志;
        serverLogModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_19.getThemeId(), this.stageId);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }

    /**
     * 能否重复进入
     *
     * @return
     */
    @Override
    public boolean isCanRepeatEnter(Scene newScene, byte newSceneType, int newSceneId, Object extend){
        return true;
    }

    /**
     * 请求发送响应请求战斗协议;
     *
     * @param recordMapSearchTreasure
     */
    protected void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterDungeon enterFight, RecordMapSearchTreasure recordMapSearchTreasure) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        enterFight.setStageId(stageVo.getStageId());
        enterFight.setFightType(stageVo.getStageType());
        List<FighterEntity> fighterList = new LinkedList<>();
        /* 出战角色 */
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        FighterEntity selfEntity = FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation());
        entityMap.put(String.valueOf(roleModule.id()), selfEntity);
        selfEntity.getAttribute().setHp(recordMapSearchTreasure.getRemainHp());
        fighterList.add(selfEntity);
        /* 出战伙伴 */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        // 有出战伙伴
        if (buddyModule.getFightBuddyId() != 0) {
            fighterList.add(FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId())));
        }
        /* 预加载怪物 */
        initMonsterData(enterFight, recordMapSearchTreasure);
        enterFight.setFighterEntityList(fighterList);
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
        //获取道具的buff信息;
        Map<Integer, Integer> buffIdLevelMap = getSearchStageBuffIdLevelMap(moduleMap);
        enterFight.addBuffData(buffIdLevelMap);
        //发送到客户端;
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.send(enterFight);
    }

    private void judgeBuffToPreLoad(Map<Integer, Integer> buffIdLevelMap, int itemId) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        String itemFunction = null;
        int buffId = 0;
        int buffLevel = 0;
        if (itemVo != null && itemVo.getType() == ToolManager.TYPE_SEARCHTREASURE) {
            itemFunction = itemVo.getFunction();
            //格式11|buffId+buffLevel|文本索引; (注意:这里的类型为保留字段, 暂时没用到)
            if (StringUtil.isNotEmpty(itemFunction) && !itemFunction.equals("0")) {
                String[] itemArr = itemFunction.split(",");
                String[] args = itemArr[0].split("\\|");
                if (Integer.parseInt(args[0]) == ToolManager.FUNC_TYPE_ADD_BUFF) {
                    String[] valueArr = args[1].split("\\+");
                    buffId = Integer.parseInt(valueArr[0]);
                    if (valueArr.length > 1) {
                        buffLevel = Integer.parseInt(valueArr[1]);
                    }
                    buffIdLevelMap.put(buffId, buffLevel);
                }
            }
        }
    }

    private Map<Integer, Integer> getSearchStageBuffIdLevelMap(Map<String, Module> moduleMap) {
        List<Integer> searchContentIdList = searchStageVo.getAllSearchContentIds();
        Map<Integer, Integer> buffIdLevelMap = new HashMap<>();
        SearchContentVo searchContentVo = null;
        DropVo dropVo = null;
        Set<Integer> dropRewardItemSet = null;
        Map<Integer, Integer> normalAward = null;
        for (int i = 0, len = searchContentIdList.size(); i < len; i++) {
            searchContentVo = SearchTreasureManager.getSearchContentVo(searchContentIdList.get(i));
            if (searchContentVo != null) {
                switch (searchContentVo.getType()) {
                    case SearchTreasureConstant.CONTENTTYPE_REWARD:
                        dropVo = DropManager.getDropVo(Integer.parseInt(searchContentVo.getParam()));
                        dropRewardItemSet = dropVo.getReawardItemIdSet();
                        normalAward = new HashMap<>();
                        for (Integer rewardItemId : dropRewardItemSet) {
                            normalAward.put(rewardItemId, 1);
                        }
                        normalAward = SearchTreasureManager.filterSearchTreasureItemIds(normalAward);
                        if (normalAward != null) {
                            for (Map.Entry<Integer, Integer> kvp : normalAward.entrySet()) {
                                judgeBuffToPreLoad(buffIdLevelMap, kvp.getKey());
                            }
                        }
                        break;
                    case SearchTreasureConstant.CONTENTTYPE_MONSTER:
                        String[] monsterAttrIdCountArr = searchContentVo.getParam().split("\\|");
                        int monsterAttrId = 0;
                        MonsterAttributeVo monsterAttributeVo = null;
                        for (int k = 0, klen = monsterAttrIdCountArr.length; k < klen; k++) {
                            monsterAttrId = Integer.parseInt(monsterAttrIdCountArr[k].split("\\+")[0]);
                            monsterAttributeVo = SceneManager.getMonsterAttrVo(monsterAttrId);
                            if (monsterAttributeVo == null) {
                                searchTreasureModule.warn("找不到monsterAttribute数据: " + monsterAttrId);
                            } else {
                                dropVo = DropManager.getDropVo(monsterAttributeVo.getDropId());
                                if (dropVo != null) {
                                    dropRewardItemSet = dropVo.getReawardItemIdSet();
                                    normalAward = new HashMap<>();
                                    for (Integer rewardItemId : dropRewardItemSet) {
                                        normalAward.put(rewardItemId, 1);
                                    }
                                    normalAward = SearchTreasureManager.filterSearchTreasureItemIds(normalAward);
                                    if (normalAward != null) {
                                        for (Map.Entry<Integer, Integer> kvp : normalAward.entrySet()) {
                                            judgeBuffToPreLoad(buffIdLevelMap, kvp.getKey());
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }
            }
        }
        //获取玩家当前所有的buff信息;
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        List<String> recordSelfItemList = recordMapSearchTreasure.getItemlist();
        String[] tmpArr = null;
        for (int i = 0, len = recordSelfItemList.size(); i < len; i++) {
            tmpArr = recordSelfItemList.get(i).split("\\+");
            if (tmpArr.length > 1) {
                judgeBuffToPreLoad(buffIdLevelMap, Integer.parseInt(tmpArr[0]));
            }
        }
        return buffIdLevelMap;
    }

    /**
     * 初始化刷怪数据
     *
     * @param enterFightPacket
     * @param obj
     */
    protected void initMonsterData(ClientEnterDungeon enterFightPacket, Object obj) {
        RecordMapSearchTreasure recordMapSearchTreasure = (RecordMapSearchTreasure) obj;
        //获取当前的探索节点的信息内容;
        String[] tmpArr = recordMapSearchTreasure.getSearchStagePPRateInfo().split("\\+");
        int contentId = 0;
        SearchContentVo searchContentVo = null;
        HashMap<Integer, MonsterVo> monsterVoMap = new HashMap<>();
        Set<Integer> itemIdSet = new HashSet<>();
        String[] monsterAttrIdCountArr = null;
        MonsterAttributeVo monsterAttributeVo;
        DropVo dropVo = null;
        int monsterAttrId = 0;
//        List<FighterEntity> fighterList = new LinkedList<>();
        for (int i = 0, len = tmpArr.length; i < len; i += 2) {
            contentId = Integer.parseInt(tmpArr[i + 1]);
            searchContentVo = SearchTreasureManager.getSearchContentVo(contentId);
            //判断是否内容为怪物类型;
            if (searchContentVo.getType() == SearchTreasureConstant.CONTENTTYPE_MONSTER) {
                monsterAttrIdCountArr = searchContentVo.getParam().split("\\|");
                for (int k = 0, klen = monsterAttrIdCountArr.length; k < klen; k++) {
                    monsterAttrId = Integer.parseInt(monsterAttrIdCountArr[k].split("\\+")[0]);
                    monsterAttributeVo = SceneManager.getMonsterAttrVo(monsterAttrId);
                    if (monsterAttributeVo == null) {
                        searchTreasureModule.warn("找不到monsterAttribute数据: " + monsterAttrId);
                    } else {
                        if (!monsterVoMap.containsKey(monsterAttributeVo.getMonsterId())) {
                            monsterVoMap.put(monsterAttributeVo.getMonsterId(), monsterAttributeVo.getMonsterVo());
                        }
                        //预加载怪物列表;
//                        String monsterUniqueId = getMonsterUId(stageId, recordMapSearchTreasure.getPathPointIndex(), monsterAttributeVo.getStageMonsterId());
//                    fighterList.add(FighterCreator.create(FighterEntity.TYPE_MONSTER, FighterEntity.CAMP_ENEMY, monsterUniqueId, monsterAttributeVo));
                        //获取怪物掉落模型预加载;
                        dropVo = DropManager.getDropVo(monsterAttributeVo.getDropId());
                        if (dropVo != null) {
                            itemIdSet.addAll(dropVo.getReawardItemIdSet());
                        }
                    }
                }
            }
        }
//        enterFightPacket.setFighterEntityList(fighterList);
        enterFightPacket.addMonsterVoMap(monsterVoMap);
        enterFightPacket.setItemIdSet(itemIdSet);
    }

    @Override
    public void checkFinish(List<String> uIdList) {
        //判断下当前层是否完成;
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        if (recordMapSearchTreasure.getStageSearchState() == SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_GETTED) {
            this.stageStatus = SceneManager.STAGE_VICTORY;
            //打印日志;
            if (serverLogModule != null) {
                serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_19.getThemeId(), searchStageVo.getStageinfoid());
            }
        }
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {

    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        String monsterUId;
        Map<Integer, Integer> deadAwardMap = new HashMap<>();
        for (int i = uIdList.size() - 1; i >= 0; i--) {
            monsterUId = uIdList.get(i);
            enemyDead(moduleMap, monsterUId, false, deadAwardMap);
        }
        //有奖励的话立即发放;
        if (deadAwardMap.size() > 0) {
            ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
            //要区别存放道具奖励;
            Map<Integer, Integer> normalAward = deadAwardMap;
            Map<Integer, Integer> searchTreasureAward = SearchTreasureManager.filterSearchTreasureItemIds(normalAward);
            searchTreasureModule.getRecordMapSearchTreasure().addItem(searchTreasureAward);
            //告诉客户端用于提示;
//            ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo();
//            clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_ITEM);
//            clientSearchTreasureInfo.setItemIdCount(normalAward);
//            searchTreasureModule.send(clientSearchTreasureInfo);
            toolModule.addAndSend(normalAward, MConst.CCSearchTreasure, EventType.SEARCHTRESURE.getCode());
        }
    }

    private boolean enemyDead(Map<String, Module> moduleMap, String monsterUid, boolean isSendRewardRightNow, Map<Integer, Integer> deadAwardMap) {
        FighterEntity monsterEntity = entityMap.get(monsterUid);
        if (monsterEntity == null) {
            return false;
        }
        Map<Integer, Integer> normalAward = monsterEntity.getDropMap();
        spawnMapping.get(monsterEntity.getSpawnUId()).remove(monsterUid);
        
        if (isSendRewardRightNow) {
            if (!StringUtil.isEmpty(normalAward)) {
                ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
                Map<Integer, Integer> searchTreasureAward = SearchTreasureManager.filterSearchTreasureItemIds(normalAward);
                searchTreasureModule.getRecordMapSearchTreasure().addItem(searchTreasureAward);
                toolModule.addAndSend(normalAward, EventType.SEARCHTRESURE.getCode());
            }
        } else {
            MapUtil.add(deadAwardMap, normalAward);
        }
        //判断当前怪物是否击杀完毕,如果完成了，就标明当前探索点完成了;
        if (spawnMapping.get(monsterEntity.getSpawnUId()).size() <= 0) {
            searchTreasureModule.getRecordMapSearchTreasure().completeCurPathPointIndex();
        }
        //从记录的怪中删除指定怪物;
        searchTreasureModule.removeCurProducedMonster(monsterUid);
        return true;
    }

    @Override
    public void selfDead(Map<String, Module> moduleMap) {
        this.deadTimeMap.put(String.valueOf(searchTreasureModule.id()), System.currentTimeMillis());
//        finishDeal(moduleMap, SceneManager.STAGE_FAIL);
        searchTreasureModule.selfDead();
        //打印日志;
        if (serverLogModule != null) {
            serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_19.getThemeId(), searchStageVo.getStageinfoid());
        }
    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {

    }

    @Override
    public void updateTimeExecute(Map<String, Module> moduleMap) {

    }

    /**
     * 生成怪物并下发到客户端;
     */
    public void spawnMonsterAndSendToClient(Map<String, Module> moduleMap, float posX, float posY, float posZ) {
        //获取探宝模块数据;
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
        Map<String, FighterEntity> monsterMap = spawnMonster(moduleMap, recordMapSearchTreasure.getPathPointIndex(), posX, posY, posZ);
        clientSpawnMonster.setSpawnMonsterMap(monsterMap);
        sceneModule.send(clientSpawnMonster);
        //存储当前生成的怪物,用于中途离开，但是没杀死,下次进来的话，继续击杀;
        Map<String, FighterEntity> recordMonsterMap = new HashMap<>();
        Set<String> monsterUidArr = monsterMap.keySet();
        FighterEntity monsterEntity = null;
        for (String monsterUidKey : monsterUidArr) {
            monsterEntity = monsterMap.get(monsterUidKey);
            recordMonsterMap.put(monsterUidKey, monsterEntity);
        }
        searchTreasureModule.setCurProducedMonsters(recordMonsterMap);
    }

    /**
     * monsterSpawnId 在探宝里是没有的,那么这里传入探索点索引即可,即pathPointIndex;
     */
    public Map<String, FighterEntity> spawnMonster(Map<String, Module> moduleMap, int pathPointIndex, float posX, float posY, float posZ) {
        int spawnUidInt = pathPointIndex;
        String spawnUid = Integer.toString(pathPointIndex);
        DropModule dropModule = (DropModule) moduleMap.get(MConst.Drop);
        //获取探宝模块数据;
        SearchTreasureModule searchTreasureModule = (SearchTreasureModule) moduleMap.get(MConst.SearchTreasure);
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        Map<String, FighterEntity> resultMap = new HashMap<>();
        //将当前的探索点的内容判断;
        spawnMapping.put(spawnUid, new LinkedList<String>());
        int contentId = recordMapSearchTreasure.getCurSearchStageContentId(recordMapSearchTreasure.getPathPointIndex());
        SearchContentVo searchContentVo = SearchTreasureManager.getSearchContentVo(contentId);
        String[] monsterStageIdPosArr = searchContentVo.getParam().split("\\|");
        String[] monsterInfoArr = null;
        for (int k = 0, klen = monsterStageIdPosArr.length; k < klen; k++) {
            monsterInfoArr = monsterStageIdPosArr[k].split("\\+");
            MonsterAttributeVo monsterAttrVo = SceneManager.getMonsterAttrVo(Integer.parseInt(monsterInfoArr[0]));
            String monsterUniqueId = getMonsterUId(stageId, spawnUidInt, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId, spawnUid, spawnUidInt,
                    monsterAttrVo, "", 0, dropModule.executeDropNotCombine(monsterAttrVo.getDropId(), 1,false));
            StringBuilder position = new StringBuilder("");
            position.append(posX + Integer.parseInt(monsterInfoArr[1]))
                    .append("+")
                    .append(posY + Integer.parseInt(monsterInfoArr[2]))
                    .append("+")
                    .append(posZ + Integer.parseInt(monsterInfoArr[3]));
            monsterEntity.setPosition(position.toString());
            resultMap.put(monsterUniqueId, monsterEntity);
            entityMap.put(monsterUniqueId, monsterEntity);
            
            // 不是陷阱怪物
            if (monsterAttrVo.getIsTrap() == 0) {
                spawnMapping.get(spawnUid).add(monsterUniqueId);
            }else if (monsterAttrVo.getIsTrap() == 1) {//是陷阱怪
            	//添加到陷阱的怪的集合
                trapMonsterMap.put(monsterUniqueId, monsterAttrVo.getStageMonsterId());
			}
        }
        return resultMap;
    }

    //检查获取上次生成的怪物;
    public Map<String, FighterEntity> checkGetLastProducedMonsters(Map<String, Module> moduleMap) {
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        Map<String, String> lastProducedMap = recordMapSearchTreasure.getCurProducedMonsters();
        if (lastProducedMap.size() > 0) {
            Map<String, Integer> lastProducedMapRemainHp = recordMapSearchTreasure.getCurProducedMonsterRemainHp();
            String spawnUid = Integer.toString(recordMapSearchTreasure.getPathPointIndex());
            spawnMapping.put(spawnUid, new LinkedList<String>());
            DropModule dropModule = (DropModule) moduleMap.get(MConst.Drop);
            Map<String, FighterEntity> rtnMap = new HashMap<>();
            FighterEntity monsterEntity = null;
            String[] idArr = null;
            String[] infoArr = null;
            String monsterUid = null;
            for (Map.Entry<String, String> kvp : lastProducedMap.entrySet()) {
                //根据uid解析出monsterStageId;
                monsterUid = kvp.getKey();
                idArr = monsterUid.split("_");
                infoArr = kvp.getValue().split("\\+");
                MonsterAttributeVo monsterAttrVo = SceneManager.getMonsterAttrVo(Integer.parseInt(idArr[2]));
                monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUid, spawnUid, 0, monsterAttrVo,
                        "", 0, dropModule.executeDropNotCombine(monsterAttrVo.getDropId(), 1,false));
                monsterEntity.getAttribute().setHp(Integer.parseInt(infoArr[0]));
                monsterEntity.setPosition(infoArr[1] + "+" + infoArr[2] + "+" + infoArr[3]);
                if(lastProducedMapRemainHp.containsKey(monsterUid)){
                    monsterEntity.getAttribute().setHp(lastProducedMapRemainHp.get(monsterUid));
                }
                rtnMap.put(monsterUid, monsterEntity);
                entityMap.put(monsterUid, monsterEntity);
                
                // 不是陷阱怪物
                if (SceneManager.getMonsterAttrVo(monsterEntity.getMonsterAttrId()).getIsTrap() == 0) {
                    spawnMapping.get(spawnUid).add(monsterUid);
                }else if (monsterAttrVo.getIsTrap() == 1) {//是陷阱怪
                	//添加到陷阱的怪的集合
                    trapMonsterMap.put(monsterUid, monsterAttrVo.getStageMonsterId());
    			}
            }
            return rtnMap;
        }
        return null;
    }

    @Override
    protected String getMonsterUId(int stageId, int spawnId, int monsterStageId) {
        StringBuffer sb = new StringBuffer();
        sb.append(stageId);
        sb.append("_");
        sb.append(spawnId);
        sb.append("_");
        sb.append(monsterStageId);
        return sb.toString();
    }

    /**
     * 服务器主动击杀怪物;
     *
     * @param type 保留字段;
     */
    @Override
    public void serverKillMonster(Map<String, Module> moduleMap, byte type) {
        RecordMapSearchTreasure recordMapSearchTreasure = searchTreasureModule.getRecordMapSearchTreasure();
        List<String> monsterUidList = null;
        ClientSyncAttr clientSyncAttr = new ClientSyncAttr();
        boolean isHasData = false;
        for (Map.Entry<String, List<String>> kvp : spawnMapping.entrySet()) {
            monsterUidList = kvp.getValue();
            //判断当前是否有怪物先;
            if (monsterUidList.size() > 0) {
                for (int i = 0, len = monsterUidList.size(); i < len; i++) {
                    clientSyncAttr.addSyncCurHp(monsterUidList.get(i), 0);
                    isHasData = true;
                }
            }
        }
        if (isHasData) {
            searchTreasureModule.send(clientSyncAttr);
        }
    }

}
