package com.stars.modules.dungeon;

import com.stars.core.db.DBUtil;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.drop.DropModule;
import com.stars.modules.dungeon.event.ChapterStarAchieveEvent;
import com.stars.modules.dungeon.packet.ClientDungeon;
import com.stars.modules.dungeon.packet.ClientProduceDungeon;
import com.stars.modules.dungeon.packet.ClientWorld;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.dungeon.prodata.WorldinfoVo;
import com.stars.modules.dungeon.summary.DungeonSummaryComponentImpl;
import com.stars.modules.dungeon.userdata.RoleChapter;
import com.stars.modules.dungeon.userdata.RoleDungeon;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.fight.DungeonScene;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.I18n;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * 关卡模块
 * 处理关卡相关外围业务(章节/关卡状态等),不包含关卡场景战斗
 * Created by liuyuheng on 2016/6/21.
 */
public class DungeonModule extends AbstractModule {
    private Map<Integer, RoleChapter> roleChapterMap = new HashMap<>();
    private Map<Integer, RoleDungeon> roleDungeonMap = new HashMap<>();
    private Set<Integer> chapterIds = new HashSet<>();

    public DungeonModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("关卡", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Exception {
        String chapterSql = "select * from `rolechapter` where `roleid`=" + id();
        String dungeonSql = "select * from `roledungeon` where `roleid`=" + id();
        roleChapterMap = DBUtil.queryMap(DBUtil.DB_USER, "chapterid", RoleChapter.class, chapterSql);
        roleDungeonMap = DBUtil.queryMap(DBUtil.DB_USER, "dungeonid", RoleDungeon.class, dungeonSql);
    }

    @Override
    public void onCreation(String name, String account) {

    }

    @Override
    public void onInit(boolean isCreation) {
        // 玩家章节列表为空,则激活第一章
        if (roleChapterMap.isEmpty()) {
            activeChapter(DungeonManager.INIT_CHAPTERID);
        }
        // 关卡列表为空,则激活第一关
        if (roleDungeonMap.isEmpty()) {
            DungeoninfoVo dungeoninfoVo = DungeonManager.getDungeonVoByStep(DungeonManager.INIT_CHAPTERID,
                    DungeonManager.INIT_STAGESTEP);
            activeDungeon(dungeoninfoVo);
        }

        //检查并激活需要激活的关卡
        activeDungeonHandler();

        canGetStarReward();

        //更新summary数据
        context().markUpdatedSummaryComponent(MConst.Dungeon);
    }

    @Override
    public void onSyncData() {

        //检测玩家是否完成关卡章节成就
        if (StringUtil.isEmpty(roleDungeonMap))
            return;
        Set<Integer> worldIdSet = new HashSet<>();
        for (RoleDungeon roleDungeon : roleDungeonMap.values()) {
            DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(roleDungeon.getDungeonId());
            if (dungeonVo == null)
                continue;
            worldIdSet.add(dungeonVo.getWorldId());
        }
        if (StringUtil.isEmpty(worldIdSet))
            return;
        for (int wordId : worldIdSet) {
            int currStar = calChapterStar(wordId);
            // 本章节星星数,抛出事件
            eventDispatcher().fire(new ChapterStarAchieveEvent(wordId, currStar, currStar));
        }
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (foreShowModule.isOpen(DungeonManager.HERO_STAGE_OPEN_TAG)) {
            activeChapter(DungeonManager.INIT_HERO_CHAPTERID);
            DungeoninfoVo dungeoninfoVo = DungeonManager.getDungeonVoByStep(DungeonManager.INIT_HERO_CHAPTERID,
                    DungeonManager.INIT_STAGESTEP);
            activeDungeon(dungeoninfoVo);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) {
        List<Integer> list = new LinkedList<>();
        for (RoleDungeon roleDungeon : roleDungeonMap.values()) {
            if (roleDungeon.getNumber() != 0) {
                roleDungeon.setNumber((byte) 0);
                context().update(roleDungeon);
                list.add(roleDungeon.getDungeonId());
            }
        }
        updateDungeonToClient(list);
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        Map<Integer, Byte> dungeonStatusMap = new HashMap<Integer, Byte>();
        for (RoleDungeon roleDungeon : roleDungeonMap.values()) {
            int dungeonId = roleDungeon.getDungeonId();
            byte status = roleDungeon.getStatus();
            dungeonStatusMap.put(dungeonId, status);
        }
        componentMap.put(MConst.Dungeon, new DungeonSummaryComponentImpl(dungeonStatusMap));
    }

    @Override
    public void onReconnect() throws Throwable {
        SceneModule sceneModule = module(MConst.Scene);
        Scene scene = sceneModule.getScene();
        if (scene != null && scene.getClass() == DungeonScene.class) {
            // log it
            DungeonScene dungeonScene = (DungeonScene) scene;
        }
    }


    /**
     * 下发所有章节数据,产品数据+玩家数据
     * 所有关卡数据,产品数据+玩家数据
     */
    public void sendAllChapterData() {
        ClientWorld clientWorld = new ClientWorld(ClientWorld.SEND_ALL_CHAPTER);
        clientWorld.setChapterVoMap(DungeonManager.chapterVoMap);
        clientWorld.setRoleChapterMap(roleChapterMap);
        send(clientWorld);
        ClientDungeon clientDungeon = new ClientDungeon(ClientDungeon.SEND_ALL_DUNGEON);
        clientDungeon.setDungeonVoMap(DungeonManager.dungeonVoMap);
        clientDungeon.setRoleDungeonMap(roleDungeonMap);
        send(clientDungeon);
    }

    /**
     * 更新客户端章节状态
     *
     * @param list
     */
    public void updateChapterToClient(List<Integer> list) {
        Map<Integer, RoleChapter> sendMap = new HashMap<>();
        RoleChapter roleChapter;
        for (int chapterId : list) {
            roleChapter = roleChapterMap.get(chapterId);
            sendMap.put(chapterId, roleChapter);
        }
        ClientWorld clientWorld = new ClientWorld(ClientWorld.UPDATE_CHAPTER);
        clientWorld.setRoleChapterMap(sendMap);
        send(clientWorld);
    }

    /**
     * 更新客户端关卡状态
     *
     * @param list
     */
    public void updateDungeonToClient(List<Integer> list) {
        Map<Integer, RoleDungeon> sendMap = new HashMap<>();
        RoleDungeon roleDungeon;
        for (int dungeonId : list) {
            roleDungeon = roleDungeonMap.get(dungeonId);
            sendMap.put(dungeonId, roleDungeon);
        }
        ClientDungeon clientDungeon = new ClientDungeon(ClientDungeon.UPDATE_DUNGEON);
        clientDungeon.setRoleDungeonMap(sendMap);
        send(clientDungeon);
    }

    /**
     * 计算经验奖励
     * 2016.09.02修改经验计算不再乘以角色等级
     *
     * @param rewardMap
     * @param coef
     */
    public void calExpReward(Map<Integer, Integer> rewardMap, int coef) {
        Map<Integer, Integer> map = new HashMap<>();
        map.putAll(rewardMap);
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getKey() == ToolManager.EXP) {
                int calExp = entry.getValue() * coef / 100;
                rewardMap.put(entry.getKey(), calExp);
            }
        }
    }

    /**
     * 获得通关奖励+首通奖励
     *
     * @param dungeonId
     * @return
     */
    public Map<Integer, Integer> getPassReward(int dungeonId) {
        if (!roleDungeonMap.containsKey(dungeonId)) {// 未解锁
            return null;
        }
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
        DropModule dropModule = (DropModule) module(MConst.Drop);
        Map<Integer, Integer> reward = dropModule.executeDrop(dungeonVo.getPassDropId(), 1, true);
        // 经验计算
        calExpReward(reward, dungeonVo.getPassExpCoef());
        // 没通关过增加首通奖励
        if (roleDungeonMap.get(dungeonId).getStatus() != DungeonManager.STAGE_PASSED) {
//            StringUtil.combineIntegerMap(reward, dungeonVo.getFirstPasssReward());
            com.stars.util.MapUtil.add(reward, dungeonVo.getFirstPasssReward());
        }
        return reward;
    }

    /**
     * 是否首次通关
     *
     * @param dungeonId
     * @return
     */
    public boolean isFirstPass(int dungeonId) {
        RoleDungeon roleDungeon = roleDungeonMap.get(dungeonId);
        if (roleDungeon != null && roleDungeon.getStatus() != DungeonManager.STAGE_PASSED) {
            return true;
        } else {
            return false;
        }
    }


    public int getRoleMaxDungeonId(byte type) {//参数type标示关卡的类型,0为普通关卡,1为精英关卡
        int worldstep = 0;
        for (int id : roleDungeonMap.keySet()) {
            if (roleDungeonMap.get(id) != null && roleDungeonMap.get(id).getStatus() == (byte) 2) {
                DungeoninfoVo dungeonInfo = DungeonManager.dungeonVoMap.get(id);
                if (dungeonInfo == null) {
                    continue;
                }
                switch (type) {
                    case (byte) 0://普通关卡
                        if (dungeonInfo.getBossIcon() == (byte) 1) {
                            continue;
                        } else if (dungeonInfo.getWorldId() * 1000 + dungeonInfo.getStep() > worldstep) {
                            worldstep = dungeonInfo.getWorldId() * 1000 + dungeonInfo.getStep();
                        }
                        break;
                    case (byte) 1://精英关卡
                        if (dungeonInfo.getBossIcon() == (byte) 0) {
                            continue;
                        } else if (dungeonInfo.getWorldId() * 1000 + dungeonInfo.getStep() > worldstep) {
                            worldstep = dungeonInfo.getWorldId() * 1000 + dungeonInfo.getStep();
                        }
                        break;
                }
            }
        }
        if (type == 0) {
            if (!DungeonManager.dungeonSepttoMap.containsKey(worldstep)) {
                return 0;
            }
            return DungeonManager.dungeonSepttoMap.get(worldstep);
        } else if (type == 1) {
            if (!DungeonManager.shigeDungeonSepettoMap.containsKey(worldstep)) {
                return 0;
            }
            return DungeonManager.shigeDungeonSepettoMap.get(worldstep);
        } else {
            return 0;
        }
    }

    /**
     * 通关关卡
     *
     * @param dungeonId
     * @param star
     */
    public void passDungeon(int dungeonId, byte star) {
        List<Integer> updateDungeonList = new LinkedList<>();// 更新列表
        DungeoninfoVo curDungeonVo = DungeonManager.getDungeonVo(dungeonId);
        RoleDungeon curRoleDungeon = roleDungeonMap.get(dungeonId);
        if (curRoleDungeon == null) return;
        if (star > curRoleDungeon.getStar()) {
            int preStar = calChapterStar(curDungeonVo.getWorldId());
            int addStar = star - curRoleDungeon.getStar();
            curRoleDungeon.setStar(star);
            // 本章节星星数增加,抛出事件
            eventDispatcher().fire(new ChapterStarAchieveEvent(curDungeonVo.getWorldId(), preStar, preStar + addStar));
        }
        curRoleDungeon.setStatus(DungeonManager.STAGE_PASSED);
//        curRoleDungeon.setUpdateStatus();
        context().update(curRoleDungeon);
        updateDungeonList.add(dungeonId);

        //更新summary数据
        context().markUpdatedSummaryComponent(MConst.Dungeon);

        // 下一关卡解锁
        DungeoninfoVo nextDungeonVo = DungeonManager.getDungeonVoByStep(curDungeonVo.getWorldId(), (byte) (curDungeonVo.getStep() + 1));
        // 本章节找不到下一个关卡配置,开启下一章第一关
        if (nextDungeonVo == null) {
            int nextChapterId = curDungeonVo.getWorldId() + 1;
            nextDungeonVo = DungeonManager.getDungeonVoByStep(nextChapterId, DungeonManager.INIT_STAGESTEP);
            if (nextDungeonVo == null)
                return;
            if (activeDungeon(nextDungeonVo)) {
                updateDungeonList.add(nextDungeonVo.getDungeonId());
                activeChapter(nextChapterId);
            }
        } else {
            if (activeDungeon(nextDungeonVo)) {
                updateDungeonList.add(nextDungeonVo.getDungeonId());
            }
        }
        updateDungeonToClient(updateDungeonList);
        canGetStarReward();
    }


    /**
     * 激活章节
     *
     * @param chapterId
     */
    public boolean activeChapter(int chapterId) {
        WorldinfoVo chapterVo = DungeonManager.getChapterVo(chapterId);
        if (chapterVo == null) {
            return false;
        }
        RoleChapter roleChapter = roleChapterMap.get(chapterId);
        if (roleChapter != null) {
            return false;
        }
        roleChapter = new RoleChapter(id(), chapterId);
        roleChapterMap.put(roleChapter.getChapterId(), roleChapter);
        context().insert(roleChapter);
        return true;
    }

    /**
     * 关卡是否激活
     *
     * @param dungeonId
     * @return
     */
    boolean isActiveDungeion(int dungeonId) {
        RoleDungeon roleDungeon = roleDungeonMap.get(dungeonId);
        if (roleDungeon != null) {
            return true;
        }
        return false;
    }

    /**
     * 章节是否激活
     *
     * @param chapterId
     * @return
     */
    boolean isActiveChapter(int chapterId) {
        RoleChapter roleChapter = roleChapterMap.get(chapterId);
        if (roleChapter != null) {
            return true;
        }
        return false;
    }

    /**
     * 激活关卡
     *
     * @param dungeonVo
     */
    public boolean activeDungeon(DungeoninfoVo dungeonVo) {
        int dungeonId = dungeonVo.getDungeonId();
        RoleDungeon roleDungeon = roleDungeonMap.get(dungeonId);
        if (roleDungeon != null) {
            return false;
        }
        // 角色等级条件
        if (dungeonVo.getReqRoleLevel() != 0) {
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            if (roleModule.getLevel() < dungeonVo.getReqRoleLevel()) {
                return false;
            }
        }
        // 通关前置关卡条件
        if (dungeonVo.getReqDungeonId() != 0) {
            RoleDungeon reqRoleDungeon = roleDungeonMap.get(dungeonVo.getReqDungeonId());
            if (reqRoleDungeon == null || reqRoleDungeon.getStatus() != DungeonManager.STAGE_PASSED) {
                return false;
            }
        }
        roleDungeon = new RoleDungeon(id(), dungeonId);
        roleDungeonMap.put(roleDungeon.getDungeonId(), roleDungeon);
        context().insert(roleDungeon);

        //更新summary数据
        context().markUpdatedSummaryComponent(MConst.Dungeon);

        return true;
    }

    /* 计算章节总星星数 */
    private int calChapterStar(int chapterId) {
        int totalStar = 0;
        int dungeonStar;
        Map<Byte, DungeoninfoVo> voMap = DungeonManager.getDungeonVoByWorldId(chapterId);
        if (voMap == null)
            return totalStar;
        for (DungeoninfoVo dungeonVo : voMap.values()) {
            if (dungeonVo.getBossIcon() == (byte) 1) continue;//诗歌关卡(boss关卡)不计入总星星数
            dungeonStar = roleDungeonMap.get(dungeonVo.getDungeonId()) == null ? 0 :
                    roleDungeonMap.get(dungeonVo.getDungeonId()).getStar();
            totalStar = totalStar + dungeonStar;
        }
        return totalStar;
    }

    /**
     * 领取章节集星奖励
     *
     * @param chapterId
     */
    public void chapterStarReward(int chapterId) {
        WorldinfoVo chapterVo = DungeonManager.getChapterVo(chapterId);
        if (chapterVo == null) {
            return;
        }
        RoleChapter roleChapter = roleChapterMap.get(chapterId);
        if (roleChapter == null) {
            warn(I18n.get("dungeon.chapterLock"));
            return;
        }
        if (roleChapter.getIsReward() == DungeonManager.CHAPTER_STAR_REWARDED) {
            warn(I18n.get("dungeon.rewarded"));
            return;
        }
        if (calChapterStar(chapterId) < chapterVo.getReqStar()) {
            warn(I18n.get("dungeon.starNotEnough"));
            return;
        }
        int groupId = chapterVo.getStarGroupId();
        DropModule dropModule = (DropModule) module(MConst.Drop);
        Map<Integer, Integer> dropMap = dropModule.executeDrop(groupId, 1, true);
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        Map<Integer, Integer> getReward = toolModule.addAndSend(dropMap, EventType.GETSART.getCode());
        roleChapter.setIsReward(DungeonManager.CHAPTER_STAR_REWARDED);
        //roleChapter.setUpdateStatus();
        context().update(roleChapter);

        //发获奖提示到客户端
        ClientAward clientAward = new ClientAward(getReward);
        send(clientAward);

        List<Integer> list = new LinkedList<>();
        list.add(chapterId);
        updateChapterToClient(list);
        canGetStarReward();
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, chapterIds, RedPointConst.DUNGEON_AWARD);
    }

    public void canGetStarReward() {
        RoleChapter roleChapter;
        for (Map.Entry<Integer, RoleChapter> entry : roleChapterMap.entrySet()) {
            roleChapter = entry.getValue();
            int chapterId = roleChapter.getChapterId();
            WorldinfoVo chapterVo = DungeonManager.getChapterVo(chapterId);
            if (roleChapter != null && chapterVo != null) {
                if (roleChapter.getIsReward() == DungeonManager.CHAPTER_STAR_NOT_REWARD &&
                        calChapterStar(chapterId) >= chapterVo.getReqStar()) {
                    chapterIds.add(chapterId);
                } else {
                    if (chapterIds.contains(chapterId)) {
                        chapterIds.remove(chapterId);
                    }
                }
            }
        }
        signCalRedPoint(MConst.Dungeon, RedPointConst.DUNGEON_AWARD);
    }

    private void checkRedPoint(Map<Integer, String> redPointMap, Set<Integer> list, int redPointConst) {
        StringBuilder builder = new StringBuilder("");
        if (!list.isEmpty()) {
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(redPointConst, null);
        }
    }


    /**
     * 挑战次数检查
     *
     * @param dungeonId
     * @return
     */
    public boolean countCheck(int dungeonId) {
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
        if (dungeonVo == null) {
            return false;
        }
        RoleDungeon roleDungeon = roleDungeonMap.get(dungeonId);
        if (roleDungeon == null) {
            return false;
        }
        int calEnterCount = dungeonVo.getEnterCount();
        /**
         * 英雄关卡不要添加vip次数
         */
        if (!DungeonManager.isHeroStage(dungeonId)) {

        }
        return roleDungeon.getNumber() < calEnterCount;
    }

    /**
     * 增加挑战次数记录
     *
     * @param dungeonId
     */
    public void addEnterCount(int dungeonId) {
        RoleDungeon roleDungeon = roleDungeonMap.get(dungeonId);
        roleDungeon.setNumber((byte) (roleDungeon.getNumber() + 1));
//        roleDungeon.setUpdateStatus();
        context().update(roleDungeon);
        List<Integer> list = new LinkedList<>();
        list.add(dungeonId);
        updateDungeonToClient(list);
    }


    /**
     * 获得挑战次数
     *
     * @param dungeonId
     * @return
     */
    public int getDungeonCount(int dungeonId) {
        RoleDungeon roleDungeon = roleDungeonMap.get(dungeonId);
        if (roleDungeon == null) return 0;
        return roleDungeon.getNumber();
    }

    /**
     * 重置挑战次数
     */
    public void resetAllEnterCount() {
        for (RoleDungeon roleDungeon : roleDungeonMap.values()) {
            roleDungeon.setNumber((byte) 0);
//            roleDungeon.setUpdateStatus();
            context().update(roleDungeon);
        }
    }

    /**
     * 关卡扫荡
     *
     * @param dungeonId
     * @param times
     */
    public void sweepDungeon(int dungeonId, byte times) {
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
        if (dungeonVo == null) {
            return;
        }
        RoleDungeon roleDungeon = roleDungeonMap.get(dungeonId);
        if (roleDungeon == null) {
            return;
        }
        // 没到3星
        if (roleDungeon.getStar() < DungeonManager.STARMAX) {
            warn(I18n.get("dungeon.notFullStarPass"));
            return;
        }
        // 开始日志
        long startTimestamp = System.currentTimeMillis();
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        List<Map<Integer, Integer>> rewardList = new LinkedList<>();
        Map<Integer, Integer> rewardPer;
        // vip增加进入次数
        int calEnterCount = dungeonVo.getEnterCount();
        Map<Integer, Integer> map;
        Map<Integer, Integer> totalMap = new HashMap<>();
        /**
         * 英雄关卡不要添加vip次数
         */
        if (!DungeonManager.isHeroStage(dungeonId)) {
        }
        for (byte i = 0; i < times; i++) {
            // 进入次数
            if (calEnterCount - roleDungeon.getNumber() < 1) {
                break;
            }
            // 体力不足
            if (!toolModule.deleteAndSend(dungeonVo.getEnterCostMap(), EventType.SWEEPDUNGEON.getCode())) {
                break;
            }
            DropModule dropModule = (DropModule) module(MConst.Drop);
            rewardPer = dropModule.executeDrop(dungeonVo.getSweepDropId(), 1, true);
            // 经验计算
            calExpReward(rewardPer, dungeonVo.getSweepExpCoef());
            //实际获得
            map = toolModule.addNotSend(rewardPer, EventType.SWEEPDUNGEON.getCode());
            // 扫荡奖励
            rewardList.add(map);
            MapUtil.add(totalMap, map);
            roleDungeon.setNumber((byte) (roleDungeon.getNumber() + 1));
//            roleDungeon.setUpdateStatus();
            context().update(roleDungeon);
        }
        // 刷新客户端背包
        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);
        toolModule.fireAddItemEvent(totalMap);
        List<Integer> dungeonIdlist = new LinkedList<>();
        dungeonIdlist.add(roleDungeon.getDungeonId());
        // 刷新关卡数据
        updateDungeonToClient(dungeonIdlist);
        // 下发扫荡结果
        ClientDungeon clientDungeon = new ClientDungeon(ClientDungeon.SWEEP_RESULT);
        clientDungeon.setSweepResult(rewardList);
        send(clientDungeon);
        // 抛出日常活动事件
        eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_SWEEPDUNGEON, times));
        // 结束日志
        long endTimestamp = System.currentTimeMillis();
        int juci = getDungeonCount(dungeonId);
        StringBuffer info = new StringBuffer();
        info.append("fight_time:");
        info.append("0").append("#sp_case:");
        info.append(getRoleMaxDungeonId((byte) 0)).append("#nm_case:").append(getRoleMaxDungeonId((byte) 1));

    }

    /**
     * 激活关卡处理
     */
    public void activeDungeonHandler() {
        List<Integer> changeList = new LinkedList<>();
        for (DungeoninfoVo dungeoninfoVo : DungeonManager.lockDungeonVoMap.values()) {
            /**
             * 关卡，章节容错
             */
            if (isActiveDungeion(dungeoninfoVo.getDungeonId()) && !isActiveChapter(dungeoninfoVo.getWorldId())) {
                activeChapter(dungeoninfoVo.getWorldId());
            }
            if (!activeDungeon(dungeoninfoVo)) {
                continue;
            }
            changeList.add(dungeoninfoVo.getDungeonId());
            activeChapter(dungeoninfoVo.getWorldId());
        }
        updateDungeonToClient(changeList);
    }

    /**
     * @param dungeonId
     * @return 是否已通关
     */
    public boolean isPassDungeon(int dungeonId) {
        RoleDungeon rd = roleDungeonMap.get(dungeonId);
        if (rd == null) {
            return false;
        }
        return rd.getStatus() == 2 ? true : false;
    }

    /**
     * @param dungeonId
     * @return 关卡是否激活
     */
    public boolean isDungeonActive(int dungeonId) {
        return roleDungeonMap.get(dungeonId) == null ? false : true;
    }

    /**
     * @param chapterId
     * @return 章节是否激活
     */
    public boolean isChapterActive(int chapterId) {
        return roleChapterMap.get(chapterId) == null ? false : true;
    }

    public void putRoleDungeonMap(RoleDungeon roleDungeon) {
        roleDungeonMap.put(roleDungeon.getDungeonId(), roleDungeon);
    }

    public void putRoleChapterMap(RoleChapter roleChapter) {
        roleChapterMap.put(roleChapter.getChapterId(), roleChapter);
    }

    /**
     * 根据角色等级获得对应类型产出副本vo
     *
     * @return 找不到对应返回null
     */
    public ProduceDungeonVo getEnterProduceDungeonVo(byte produceDungeonType) {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int roleLv = roleModule.getLevel();
        if (!DungeonManager.produceDungeonVoMap.containsKey(produceDungeonType))
            return null;
        for (ProduceDungeonVo produceDungeonVo : DungeonManager.produceDungeonVoMap.get(produceDungeonType).values()) {
            if (produceDungeonVo.getLevelRangeArray()[0] <= roleLv && roleLv <= produceDungeonVo.getLevelRangeArray()[1])
                return produceDungeonVo;
        }
        return null;
    }

    /**
     * 下发对应等级类型产出副本数据
     *
     * @param produceDungeonType
     */
    public void sendProduceDungeonVo(byte produceDungeonType) {
        ProduceDungeonVo vo = getEnterProduceDungeonVo(produceDungeonType);
        if (vo == null) {
            warn(I18n.get("dungeon.produce.canNotFind"));
            return;
        }
        ClientProduceDungeon packet = new ClientProduceDungeon(vo);
        send(packet);
    }


    /**
     * 获取玩家所有的通关副本
     *
     * @return
     */
    public Map<Integer, RoleDungeon> getRolePassDungeonMap() {
        Map<Integer, RoleDungeon> passDungeonMap = new HashMap<>();
        for (Map.Entry<Integer, RoleDungeon> entry : roleDungeonMap.entrySet()) {
            if (entry.getValue().getStatus() == 2) {
                passDungeonMap.put(entry.getKey(), entry.getValue());
            }
        }
        return passDungeonMap;
    }

    public void onEvent(Event event) {
        if (event instanceof ForeShowChangeEvent) {
            ForeShowChangeEvent foreShowChangeEvent = (ForeShowChangeEvent) event;
            if (foreShowChangeEvent.getMap().containsKey(DungeonManager.HERO_STAGE_OPEN_TAG)) {
                ForeShowModule foreShowModule = module(MConst.ForeShow);
                if (foreShowModule.isOpen(DungeonManager.HERO_STAGE_OPEN_TAG)) {
                    activeChapter(DungeonManager.INIT_HERO_CHAPTERID);
                    DungeoninfoVo dungeoninfoVo = DungeonManager.getDungeonVoByStep(DungeonManager.INIT_HERO_CHAPTERID,
                            DungeonManager.INIT_STAGESTEP);
                    activeDungeon(dungeoninfoVo);
                    sendAllChapterData();
                }
            }
        }
    }
}
