package com.stars.modules.scene;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.drop.prodata.DropVo;
import com.stars.modules.scene.cache.ResendPacketCache;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientFightTime;
import com.stars.modules.scene.packet.ServerSpawnMonsterConfirm;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.func.impl.BoxToolFunc;
import com.stars.modules.tool.func.impl.BoxToolFunc2;
import com.stars.modules.tool.func.impl.JobBoxToolFunc;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.network.server.packet.Packet;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.*;

/**
 * pve战斗场景父类
 * Created by liuyuheng on 2016/7/8.
 */
public abstract class FightScene extends Scene {
    public byte isAgain;// 是否重复进入
    public int stageId;// 场景Id
    public long startTimestamp;// 开始时间戳
    public long endTimestamp;// 结束时间戳
    public long spendTime;// 已度过时间,用于剧情/引导暂停时间计算
    public byte stageStatus;// 状态 0失败;1进行中;2胜利;3暂停(剧情/引导需要)
    protected int spawnSeq = 0;// 刷怪组自增序列
    protected String monsterUniqueId;//怪物唯一Id;
    //    public int marryBattleScore; //获得的情义副本积分
    // 召唤战斗实体缓存Map(怪物/玩家/伙伴) <uId,FighterEntity>
    protected Map<String, FighterEntity> entityMap = new HashMap<>();
    // 动态阻挡状态 <uId, 1开启/0不开启>
    protected Map<String, Byte> blockStatus = new HashMap<>();
    // 刷怪组UId-怪物UIdList,用于检测刷出一波怪物是否死完
    protected Map<String, List<String>> spawnMapping = new HashMap<>();
    // 刷怪组UId-怪物UIdList,用于统计是否满足killall条件，不算陷阱怪
    protected Map<String, List<String>> notTrapMonsterMap = new HashMap<>();
    // 区域触发刷怪状态 spawnId-true/false
    protected Map<Integer, Boolean> areaSpawnStateMap = new HashMap<>();
    // 已获得怪物掉落集合
    protected Map<Integer, Integer> totalDropMap = new HashMap<>();
    // 战斗实体死亡时间戳 <uId,timestamp>
    protected Map<String, Long> deadTimeMap = new HashMap<>();
    // 战场上陷阱怪的集合 <uId , monsterAttributeId>
    protected Map<String, Integer> trapMonsterMap = new HashMap<>();

    /* 缓存下发包,<刷怪组唯一Id, packet> */
    private Map<Integer, ResendPacketCache> resendPacketCache = new HashMap<>();

    // 重发间隔
    private static long resendInterval = 5 * 1000L;
    // 超时时间
    private static long resendTimeout = 50 * 1000L;

    public FightScene() {

    }

    public Scene createNewScene(Scene newScene, byte newSceneType, int newSceneId) {
        if (newScene != null)
            return newScene;
        if (newSceneType == SceneManager.SCENETYPE_CITY) return newScene = SceneManager.newScene(newSceneType);
        if (this.stageStatus == SceneManager.STAGE_PROCEEDING && this.getSceneId() == newSceneId) {
            return null;
        }
        newScene = SceneManager.newScene(newSceneType);
        // 再次进入
        if (this.getSceneId() == newSceneId) {
            setIsAgain((byte) 1);
        }
        return newScene;
    }


    @Override
    public abstract boolean canEnter(Map<String, Module> moduleMap, Object obj);

    @Override
    public abstract void enter(Map<String, Module> moduleMap, Object obj);

    /**
     * 2016.09.08策划需求主动退出战斗场景需进行怪物掉落结算
     */
    @Override
    public abstract void exit(Map<String, Module> moduleMap);

    @Override
    public abstract boolean isEnd();

    public void checkFinish(List<String> uIdList) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        // 先检测胜利
        for (Map.Entry<Byte, Integer> entry : stageVo.getVictoryConMap().entrySet()) {
            switch (entry.getKey()) {
                case SceneManager.VICTORY_CONDITION_KILL_ALL:// 全部击杀
                    killAll();
                    break;
                case SceneManager.VICTORY_CONDITION_KILL_APPOINT:// 击杀指定
                    killAppoint(SceneManager.STAGE_VICTORY, entry.getValue(), uIdList);
                    break;
                case SceneManager.VICTORY_CONDITION_TIME:// 指定时间后
                    victoryCheckTime(entry.getValue());
                    break;
                case SceneManager.VICTORY_CONDITION_KILL_BOSS:// 击杀boss类型怪物
                    killBoss(uIdList);
                    break;
                default:
                    break;
            }
        }
        // 失败
        for (Map.Entry<Byte, Integer> entry : stageVo.getFailConMap().entrySet()) {
            switch (entry.getKey()) {
                case SceneManager.FAIL_CONDITION_SELFDEAD:
                    break;
                case SceneManager.FAIL_CONDITION_TIME:// 指定时间后
                    defeatCheckTime(entry.getValue());
                    break;
                case SceneManager.FAIL_CONDITION_KILL_APPOINT:// 击杀指定 失败
                    killAppoint(SceneManager.STAGE_FAIL, entry.getValue(), uIdList);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 达成条件处理
     * 达成胜利/失败条件后调用
     *
     * @param finish
     */
    public abstract void finishDeal(Map<String, Module> moduleMap, byte finish);

    /**
     * 敌方死亡
     *
     * @param uIdList 敌方唯一Id
     */
    public abstract void enemyDead(Map<String, Module> moduleMap, List<String> uIdList);

    /**
     * 我方死亡
     */
    public void selfDead(Map<String, Module> moduleMap) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if (!stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_SELFDEAD)) {
            return;
        }
        stageStatus = SceneManager.STAGE_FAIL;
        finishDeal(moduleMap, SceneManager.STAGE_FAIL);
    }

    /**
     * 区域触发刷怪
     *
     * @param spawnId
     */
    public abstract void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId);

    protected String getSpawnUId(int spawnId) {
        return "" + spawnId + spawnSeq;
    }

    protected String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

    /**
     * 初始化刷怪数据
     * 包括预加载所有怪物数据,区域刷怪配置,初始刷怪数据
     *
     * @param enterFightPacket
     * @param obj
     */
    protected void initMonsterData(Map<String, Module> moduleMap, ClientEnterDungeon enterFightPacket, Object obj) {
        StageinfoVo stageVo = (StageinfoVo) obj;
        // 区域刷怪配置
        List<MonsterSpawnVo> areaSpawnList = new LinkedList<>();
        // 怪物所有掉落物品IdSet,用于下发客户端预加载掉落模型
        Set<Integer> itemIdSet = new HashSet<>();
        /* 怪物数据 */
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
            // 区域刷怪配置
            if (monsterSpawnVo.getSpawnType() == SceneManager.SPAWNTYPE_AREA) {
                areaSpawnList.add(monsterSpawnVo);
                areaSpawnStateMap.put(monsterSpawnId, false);
            }
            if (monsterSpawnVo.getSpawnType() == SceneManager.SPAWNTYPE_INIT) {
                spawnMonster(moduleMap, monsterSpawnId);
                enterFightPacket.addBlockStatusMap(openBlock(monsterSpawnId));
            }
            // 遍历 怪物掉落模型预加载
            for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
                DropVo dropVo = DropManager.getDropVo(monsterAttrVo.getDropId());
                if (dropVo == null) {
                    continue;
                }
                itemIdSet.addAll(dropVo.getReawardItemIdSet());
            }
        }
        enterFightPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        enterFightPacket.setAreaSpawnList(areaSpawnList);
        enterFightPacket.setItemIdSet(itemIdSet);
    }

    /**
     * 执行刷怪
     *
     * @param monsterSpawnId
     * @return
     */
    public Map<String, FighterEntity> spawnMonster(Map<String, Module> moduleMap, int monsterSpawnId) {
        DropModule dropModule = (DropModule) moduleMap.get(MConst.Drop);
        Map<String, FighterEntity> resultMap = new HashMap<>();
        spawnSeq++;
        spawnMapping.put(getSpawnUId(monsterSpawnId), new LinkedList<String>());
        notTrapMonsterMap.put(getSpawnUId(monsterSpawnId), new LinkedList<String>());
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            com.stars.util.LogUtil.error("找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            this.monsterUniqueId = monsterUniqueId;
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), dropModule.executeDropNotCombine(monsterAttrVo.getDropId(), 1, false));
            entityMap.put(monsterUniqueId, monsterEntity);
            resultMap.put(monsterUniqueId, monsterEntity);

            //本来陷阱怪不算入怪物组的，然而策划要求算入，然而陷阱怪可能是无敌的 影响刷下一波怪
            //策划已约定配陷阱怪时要么非无敌 要么没下一波怪，若配错了出问题，砍他
            spawnMapping.get(getSpawnUId(monsterSpawnId)).add(monsterUniqueId);
            if (monsterAttrVo.getIsTrap() == 0) {
                //添加到统计非陷阱怪物的集合
                notTrapMonsterMap.get(getSpawnUId(monsterSpawnId)).add(monsterUniqueId);
            } else if (monsterAttrVo.getIsTrap() == 1) {
                //添加到陷阱的怪的集合
                trapMonsterMap.put(monsterUniqueId, monsterAttrVo.getStageMonsterId());
            }
        }
        return resultMap;
    }

    /**
     * 执行销毁陷阱怪
     *
     * @param monsterSpawnId
     * @return
     */
    public List<String> destroyTrapMonster(int monsterSpawnId) {
        List<String> destroyMonsterUids = new ArrayList<String>();

        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            return destroyMonsterUids;
        }

        List<Integer> destroyIdList = monsterSpawnVo.getDestroyMonsterIdList();

        Iterator<Map.Entry<String, Integer>> it = trapMonsterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            int attributeId = entry.getValue();
            MonsterAttributeVo monsterAttributeVo = SceneManager.getMonsterAttrVo(attributeId);
            if (monsterAttributeVo != null && monsterAttributeVo.getIsTrap() == 1) {//只有陷阱怪才能销毁
                for (Integer monsterAttributeId : destroyIdList) {
                    if (attributeId == monsterAttributeId) {
                        //若是配置中的陷阱怪，执行销毁
                        String uid = entry.getKey();
                        destroyMonsterUids.add(uid);
                        it.remove();
                        break;
                    }
                }
            }
        }

        return destroyMonsterUids;
    }

    /**
     * 初始化动态阻挡数据
     *
     * @param enterFightPacket
     * @param stageVo
     */
    protected void initDynamicBlockData(ClientEnterFight enterFightPacket, StageinfoVo stageVo) {
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        enterFightPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterFightPacket.addBlockStatusMap(blockStatus);
    }

    /**
     * 激活动态阻挡
     *
     * @param monsterSpawnId
     * @return
     */
    protected Map<String, Byte> openBlock(int monsterSpawnId) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        Map<String, Byte> resultMap = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            if (dynamicBlock.getShowSpawnId() == monsterSpawnId) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
                resultMap.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        return resultMap;
    }

    /**
     * 关闭动态阻挡
     *
     * @param monsterSpawnId
     * @return
     */
    protected Map<String, Byte> closeBlock(int monsterSpawnId) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        Map<String, Byte> resultMap = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            if (dynamicBlock.getHideSpawnId() == monsterSpawnId) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
                resultMap.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            }
        }
        return resultMap;
    }

    public void setIsAgain(byte isAgain) {
        this.isAgain = isAgain;
    }

//    /**
//     * 加入剧情用的怪物、技能数据
//     *
//     * @param clientEnterFight
//     * @param dramaId
//     */
//    protected void initDramaData(ClientEnterDungeon clientEnterFight, String dramaId) {
//        DramaConfig dramaConfig = SceneManager.getDramaConfig(dramaId);
//        if (dramaConfig == null) {
//            return;
//        }
//        clientEnterFight.addMonsterVoMap(dramaConfig.getMonsterVoMap());
//        clientEnterFight.getSkillMap().putAll(dramaConfig.getSkillVoMap());
//    }

    /**
     * 暂停战斗场景计时(剧情/引导需要)
     */
    public void pauseFightTime() {
        if (stageStatus == SceneManager.STAGE_PROCEEDING) {
            stageStatus = SceneManager.STAGE_PAUSE;
        }
        spendTime = System.currentTimeMillis() - startTimestamp;
        com.stars.util.LogUtil.info("暂停战斗:{}", spendTime);
    }

    /**
     * 开始/继续战斗场景计时
     */
    public Packet startFightTime() {
        if (stageStatus == SceneManager.STAGE_PAUSE) {
            stageStatus = SceneManager.STAGE_PROCEEDING;
        }
        com.stars.util.LogUtil.info("开始战斗|暂停间隔:{},startTimestamp:{}", spendTime, startTimestamp);
        if (spendTime != 0) {
            startTimestamp = System.currentTimeMillis() - spendTime;
        }
        spendTime = 0;
        LogUtil.info("剩余时间间隔:{}", System.currentTimeMillis() - startTimestamp);
        //这里改为了Math.ceil，原为floor, 因为计时是按>=0来结算的;
        ClientFightTime packet = new ClientFightTime((int) Math.ceil((System.currentTimeMillis() - startTimestamp) / 1000.0));
        return packet;
    }

    public void updateTimeExecute(Map<String, Module> moduleMap) {
        StageinfoVo stageVo = SceneManager.getStageVo(this.stageId);
        if (this.stageStatus == SceneManager.STAGE_PROCEEDING && stageVo.containTimeCondition()) {
            // 胜利失败检测
            checkFinish(null);
            if (stageStatus != SceneManager.STAGE_PROCEEDING) {
                finishDeal(moduleMap, stageStatus);
            }
        }
        resendPacket(moduleMap);
    }

    /**
     * 服务器主动击杀怪物;
     */
    public void serverKillMonster(Map<String, Module> moduleMap, byte type) {

    }

    /**
     * 用于处理某些战斗场景中的交互
     *
     * @param packet
     */
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        if (packet instanceof ServerSpawnMonsterConfirm) {
            resendPacketCache.remove(((ServerSpawnMonsterConfirm) packet).getSpawnUniqueId());
        }
    }

    /**
     * 提供默认复活流程：只检测自己的复活,只检测stageinfo中配置复活cd条件
     * 其他复活需求则需要重写此方法
     *
     * @param roleId
     */
    public boolean checkRevive(String roleId) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if (!deadTimeMap.containsKey(roleId))
            return false;
        if (System.currentTimeMillis() - deadTimeMap.get(roleId) < stageVo.getRebornTime())
            return false;
        // 移除死亡时间戳
        deadTimeMap.remove(roleId);
        entityMap.get(roleId).getAttribute().setHp(entityMap.get(roleId).getAttribute().getMaxhp());
        return true;
    }

    // 胜利条件

    /**
     * 击杀所有
     *
     * @return
     */
    public void killAll() {
        boolean allDead = true;
        for (boolean isSpawn : areaSpawnStateMap.values()) {
            if (!isSpawn) {// 区域刷怪还没触发
                allDead = false;
                break;
            }
        }
        for (Map.Entry<String, List<String>> entry : notTrapMonsterMap.entrySet()) {
            if (allDead == false) {
                break;
            }
            String spawnUId = entry.getKey();
            List<String> monsterUIdList = spawnMapping.get(spawnUId);
            for (String monsterid : entry.getValue()) {
                if (monsterUIdList.contains(monsterid)) {// 刷出的非陷阱怪物没死完
                    allDead = false;
                    break;
                }
            }
        }
        if (allDead) {
            stageStatus = SceneManager.STAGE_VICTORY;
            return;
        }
    }

    /**
     * 击杀指定 胜利/失败
     *
     * @param result
     * @param conditonParam
     * @param uIdList
     * @return
     */
    public void killAppoint(byte result, int conditonParam, List<String> uIdList) {
        if (uIdList == null) {
            return;
        }

        for (String monsterUId : uIdList) {
            FighterEntity entity = entityMap.get(monsterUId);
            if (entity.getMonsterAttrId() == conditonParam) {
                stageStatus = result;
                return;
            }
        }
        return;
    }

    /**
     * 指定时间之后
     *
     * @param conditonParam
     * @return
     */
    public void victoryCheckTime(int conditonParam) {
        if (System.currentTimeMillis() - startTimestamp >= conditonParam) {
            stageStatus = SceneManager.STAGE_VICTORY;
            return;
        }
        return;
    }

    /**
     * 击杀boss类型怪物
     *
     * @param uIdList
     * @return
     */
    public void killBoss(List<String> uIdList) {
        if (uIdList == null) {
            return;
        }

        for (String monsterUId : uIdList) {
            FighterEntity entity = entityMap.get(monsterUId);
            if (SceneManager.getMonsterAttrVo(entity.getMonsterAttrId()).getMonsterVo().getType() == 1) {
                stageStatus = SceneManager.STAGE_VICTORY;
                return;
            }
        }
        return;
    }

    // 失败条件

    /**
     * 指定时间之后
     *
     * @param conditonParam
     */
    public void defeatCheckTime(int conditonParam) {
        if (System.currentTimeMillis() - startTimestamp >= conditonParam) {
            stageStatus = SceneManager.STAGE_FAIL;
            return;
        }
    }


    /**
     * 转换自动使用宝箱类物品显示
     *
     * @param rewardMap
     */
    protected void switchBoxTool(Map<Integer, Integer> rewardMap, int jobId) {
        List<Integer> removeIds = new LinkedList<>();
        Map<Integer, Integer> rewardCopy = new HashMap<>();
        rewardCopy.putAll(rewardMap);
        for (Map.Entry<Integer, Integer> entry : rewardCopy.entrySet()) {
            ItemVo itemVo = ToolManager.getItemVo(entry.getKey());
            if (itemVo.getAutoUse() != 1) continue;
            Map<Integer, Integer> boxRewardItem;

            if (itemVo.getFuncType() == ToolManager.FUNC_TYPE_BOX) {//先走宝箱逻辑,宝箱可能掉落职业装备宝箱
                BoxToolFunc boxToolFunc = (BoxToolFunc) itemVo.getToolFunc();
                boxRewardItem = new HashMap<>();
                for (Map.Entry<Integer, Integer> itemEntry : boxToolFunc.getTools().entrySet()) {
                    boxRewardItem.put(itemEntry.getKey(), itemEntry.getValue() * entry.getValue());
                }
                com.stars.util.MapUtil.add(rewardMap, boxRewardItem);
            }

            if (itemVo.getFuncType() == ToolManager.FUNC_TYPE_BOX_NO_TIPS) {//先走宝箱逻辑,宝箱可能掉落职业装备宝箱
                BoxToolFunc2 boxToolFunc = (BoxToolFunc2) itemVo.getToolFunc();
                boxRewardItem = new HashMap<>();
                for (Map.Entry<Integer, Integer> itemEntry : boxToolFunc.getTools().entrySet()) {
                    boxRewardItem.put(itemEntry.getKey(), itemEntry.getValue() * entry.getValue());
                }
                com.stars.util.MapUtil.add(rewardMap, boxRewardItem);
            }

            if (itemVo.getFuncType() == ToolManager.FUNC_TYPE_JOB_BOX) {//职业宝箱,用于掉落对应职业装备
                JobBoxToolFunc jobBoxToolFunc = (JobBoxToolFunc) itemVo.getToolFunc();
                boxRewardItem = new HashMap<>();
                Map<Integer, Integer> toolMap = jobBoxToolFunc.getJobToolMap().get(jobId);
                for (Map.Entry<Integer, Integer> itemEntry : toolMap.entrySet()) {
                    boxRewardItem.put(itemEntry.getKey(), itemEntry.getValue() * entry.getValue());
                }
                MapUtil.add(rewardMap, boxRewardItem);
            }

            removeIds.add(entry.getKey());
        }
        for (int removeId : removeIds) {
            rewardMap.remove(removeId);
        }
    }

    protected void addResendPacket(int key, PlayerPacket packet) {
        resendPacketCache.put(key, new ResendPacketCache(System.currentTimeMillis(), packet));
    }

    protected void resendPacket(Map<String, Module> moduleMap) {
        if (resendPacketCache.isEmpty())
            return;
        List<Integer> removeCache = new LinkedList<>();
        for (Map.Entry<Integer, ResendPacketCache> entry : resendPacketCache.entrySet()) {
            ResendPacketCache cache = entry.getValue();
            if (System.currentTimeMillis() - cache.getTimestamp() >= resendInterval) {
                SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
                sceneModule.send(cache.getPacket());
            }
            // 超时移除
            if (System.currentTimeMillis() - cache.getTimestamp() >= resendTimeout) {
                removeCache.add(entry.getKey());
            }
        }
        if (!removeCache.isEmpty()) {
            for (int key : removeCache) {
                resendPacketCache.remove(key);
            }
        }
    }
}
