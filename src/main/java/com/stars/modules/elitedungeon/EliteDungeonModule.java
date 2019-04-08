package com.stars.modules.elitedungeon;

import com.stars.core.attr.Attribute;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamModule;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.summary.DungeonSummaryComponent;
import com.stars.modules.elitedungeon.event.BackToCityFromEliteDungeonEvent;
import com.stars.modules.elitedungeon.event.EliteDungeonEnterFightEvent;
import com.stars.modules.elitedungeon.event.EliteDungeonFinishEvent;
import com.stars.modules.elitedungeon.event.EliteDungonAchieveEvent;
import com.stars.modules.elitedungeon.packet.ClientEliteData;
import com.stars.modules.elitedungeon.packet.ClientEliteDungeonPacket;
import com.stars.modules.elitedungeon.prodata.EliteDungeonVo;
import com.stars.modules.elitedungeon.recordmap.RecordMapEliteDungeon;
import com.stars.modules.elitedungeon.summary.EliteDungeonSummaryComponent;
import com.stars.modules.elitedungeon.summary.EliteDungeonSummaryComponentImpl;
import com.stars.modules.elitedungeon.userdata.ElitePlayerImagePo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.scene.packet.ClientMonsterDrop;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.MapUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by gaopeidian on 2017/3/8.
 */
public class EliteDungeonModule extends AbstractModule {
    RecordMapEliteDungeon record = null;
    private Map<Integer, Integer> monsterDrop = new HashMap<>();// 组队副本怪物掉落

    public EliteDungeonModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("精英副本", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name_, String account_) throws Throwable {
        initRecordMap();
    }

    @Override
    public void onDataReq() throws Exception {
        initRecordMap();
    }

    @Override
    public void onInit(boolean isCreation) {
        //检查精英副本的记录
        checkEliteDungeonRecord();

        //更新精英副本常用数据
        updateEliteDungeonSummaryComp();
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        record.reset();

        sendEliteData();

        //更新精英副本常用数据
        updateEliteDungeonSummaryComp();
    }

    @Override
    public void onOffline() throws Throwable {
        // 离线做退出活动处理
        for (int eliteDungeonId : EliteDungeonManager.getEliteDungeonVoMap().keySet()) {
            ServiceHelper.eliteDungeonService().removeMemberId(id(), eliteDungeonId);
        }
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (record != null) {
            componentMap.put(MConst.EliteDungeon, new EliteDungeonSummaryComponentImpl(record.getPlayCount(),
                    record.getRewardTimes(), record.getHelpTimes()));
        }
    }

    private void initRecordMap() throws SQLException {
        record = new RecordMapEliteDungeon(moduleMap(), context());
    }

    private void updateEliteDungeonSummaryComp() {
        context().markUpdatedSummaryComponent(MConst.EliteDungeon);
    }

    private void checkEliteDungeonRecord() {
        //由于更改激活条件而原本激活变成未激活的精英副本，清空记录
        if (record == null) return;
        Set<Integer> enterEliteDungeons = record.getEnterEliteDungeons();
        Set<Integer> passedEliteDungeons = record.getPassedEliteDungeons();

        Map<Integer, EliteDungeonVo> eliteDungeonVos = EliteDungeonManager.getEliteDungeonVoMap();
        for (EliteDungeonVo eliteDungeonVo : eliteDungeonVos.values()) {
            int eliteId = eliteDungeonVo.getEliteId();
            if (!isEliteDungeonActive(eliteId)) {//未激活的，清空记录
                if (enterEliteDungeons.contains(eliteId)) enterEliteDungeons.remove(eliteId);
                if (passedEliteDungeons.contains(eliteId)) passedEliteDungeons.remove(eliteId);
            }
        }
    }

    public boolean isEliteDungeonActive(int eliteId) {
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) return false;
        int activeDungeonId = eliteDungeonVo.getActiveDungeon();
        DungeonModule dungeonModule = (DungeonModule) module(MConst.Dungeon);
        return dungeonModule.isPassDungeon(activeDungeonId);
    }

    public void handleEnterFightEvent(EliteDungeonEnterFightEvent event) {
        int eliteDungeonId = event.getEliteDungeonId();
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteDungeonId);
        if (eliteDungeonVo == null) return;

        //判断是否可进
        int playCount = record.getPlayCount();
        int rewardTimes = record.getRewardTimes();
        int helpTimes = record.getHelpTimes();

        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        ServerLogModule serverLogModule = module(MConst.ServerLog);

        if (playCount < rewardTimes) {//判断是否可收益进入    
            //判断体力是否足够
            //扣除体力
            int costVigor = eliteDungeonVo.getVigorCost();
            ToolModule toolModule = (ToolModule) module(MConst.Tool);
            if (!toolModule.deleteAndSend(ToolManager.VIGOR, costVigor, EventType.ELITE_DUNGEON.getCode())) {
                ServiceHelper.eliteDungeonService().removeMemberId(id(), eliteDungeonId);
                return;
            }

            serverLogModule.logTeamBegin(team.getMemberCount(), rewardTimes - playCount - 1, helpTimes);
            serverLogModule.logBaseTeamBegin(playCount, eliteDungeonVo.getEliteId());
            ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_ELITE_DUNGEON, 1));

        } else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
            //无需消耗，可进入

            serverLogModule.logTeamBegin(team.getMemberCount(), 0, rewardTimes + helpTimes - playCount - 1);
            serverLogModule.logBaseTeamBegin(playCount, eliteDungeonVo.getEliteId());
            ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_ELITE_DUNGEON, 1));

        } else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
            ServiceHelper.eliteDungeonService().removeMemberId(id(), eliteDungeonId);
            return;
        }

        //添加进入次数
        record.setPlayCount(playCount + 1);

        //更新精英副本常用数据
        updateEliteDungeonSummaryComp();

        //记录玩家进入过该副本
        Set<Integer> enterEliteDungeons = record.getEnterEliteDungeons();
        if (!enterEliteDungeons.contains(eliteDungeonId)) {
            enterEliteDungeons.add(eliteDungeonId);
            record.setEnterEliteDungeons(enterEliteDungeons);
        }
        fireEliteDungonAchieveEvent(eliteDungeonId);
        //下发最新的精英副本数据(更新进入次数)
        sendEliteData();
    }

    private void fireEliteDungonAchieveEvent(int eliteDungonId){
        EliteDungonAchieveEvent event = new EliteDungonAchieveEvent(eliteDungonId);
        eventDispatcher().fire(event);
    }

    public void handleBackToCityEvent(BackToCityFromEliteDungeonEvent event) {
        int eliteDungeonId = event.getEliteDungeonId();

        //检查体力是否足够，不足则踢出队伍
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteDungeonId);
        if (eliteDungeonVo != null) {
            int playCount = record.getPlayCount();
            int rewardTimes = record.getRewardTimes();
            int helpTimes = record.getHelpTimes();

            if (playCount < rewardTimes) {//判断是否可收益进入    
                //判断体力是否足够
                int costVigour = eliteDungeonVo.getVigorCost();
                RoleModule roleModule = (RoleModule) module(MConst.Role);
                int myVigour = roleModule.getRoleRow().getVigor();
                if (myVigour < costVigour) {
                    ServiceHelper.baseTeamService().leaveTeam(id());
                }
            } else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
                //无需消耗，可进入
            } else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
                ServiceHelper.baseTeamService().leaveTeam(id());
            }
        }

        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.backToCity(Boolean.FALSE);
    }

    public void sendEliteData() {
        Map<Integer, Byte> eliteDatas = new HashMap<Integer, Byte>();
        Set<Integer> enterEliteDungeons = record.getEnterEliteDungeons();
        Set<Integer> passedEliteDungeons = record.getPassedEliteDungeons();

        Map<Integer, EliteDungeonVo> eliteDungeonVos = EliteDungeonManager.getEliteDungeonVoMap();
        for (EliteDungeonVo eliteDungeonVo : eliteDungeonVos.values()) {
            int eliteId = eliteDungeonVo.getEliteId();
            byte state = EliteDungeonConstant.ELITE_STATE_NOT_ACTIVE;
            if (passedEliteDungeons.contains(eliteId)) {
                state = EliteDungeonConstant.ELITE_STATE_PASSED;
            } else if (isEliteDungeonActive(eliteId) && !enterEliteDungeons.contains(eliteId)) {
                state = EliteDungeonConstant.ELITE_STATE_NEW_ACTIVE;
            } else if (isEliteDungeonActive(eliteId) && enterEliteDungeons.contains(eliteId)) {
                state = EliteDungeonConstant.ELITE_STATE_ALREADY_ACTIVE;
            }
            eliteDatas.put(eliteId, state);

            RoleModule roleModule = (RoleModule) module(MConst.Role);
            int myVigour = roleModule.getRoleRow().getVigor();
            int needVigour = eliteDungeonVo.getVigorCost();
            if (myVigour >= needVigour && (state != EliteDungeonConstant.ELITE_STATE_NOT_ACTIVE)) {
                ServiceHelper.eliteDungeonService().addMemberId(id(), eliteId);
            }
        }

        int playCount = record.getPlayCount();
        int rewardTimes = record.getRewardTimes();
        int helpTimes = record.getHelpTimes();
//    	int leftRewardTimes = 0;
//    	int leftHelpTimes = 0;    	
//    	if (playCount < rewardTimes) {    
//        	leftRewardTimes = rewardTimes - playCount;
//        	leftHelpTimes = helpTimes;
//		}else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {
//			leftRewardTimes = 0;
//			leftHelpTimes = rewardTimes + helpTimes - playCount;
//		}else if (playCount >= rewardTimes + helpTimes) {
//			leftRewardTimes = 0;
//			leftHelpTimes = 0;
//		}  

        //发消息到客户端
        ClientEliteData clientEliteData = new ClientEliteData();
        clientEliteData.setEliteDatas(eliteDatas);
        clientEliteData.setPlayCount(playCount);
        clientEliteData.setRewardTimes(rewardTimes);
        clientEliteData.setHelpTimes(helpTimes);
        send(clientEliteData);
    }

    /**
     * 进入队伍副本
     */
    public void enterDegeno(boolean isForce) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) return;
        EliteDungeonVo eliteDungeon = EliteDungeonManager.getEliteDungeonVo(team.getTarget());
        if (eliteDungeon == null) {
            warn(I18n.get("no product data"));
            return;
        }

        int playCount = record.getPlayCount();
        int rewardTimes = record.getRewardTimes();
        int helpTimes = record.getHelpTimes();

        if (playCount < rewardTimes) {//判断是否可收益进入    
            //判断体力是否足够
            int costVigour = eliteDungeon.getVigorCost();
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            int myVigour = roleModule.getRoleRow().getVigor();
            if (myVigour < costVigour) {
                warn("体力不足");
                return;
            }
        } else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
            //无需消耗，可进入
        } else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
            warn("elitedungeon_createteam_timesshort");
            return;
        }

        if (!isForce && !canAllChangeTeamTarget(id(), team.getTarget())) {
            return;
        }

        removeUnSatifyMember(id(), team.getTarget());

        ServiceHelper.eliteDungeonService().enterFight(id());

    }

    //移除不满足进入条件的成员
    public void removeUnSatifyMember(long initiator, int target) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        if (team == null) {
            return;
        }
        int eliteId = target;
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) {
            return;
        }

        for (BaseTeamMember teamMember : team.getMembers().values()) {
            if (teamMember.getType() == (byte) 1) {//机器人无需判断
                continue;
            }

            long roleId = teamMember.getRoleId();
            DungeonSummaryComponent dsc = (DungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleId, "dungeon");
            RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleId, MConst.Role);
            EliteDungeonSummaryComponent esc = (EliteDungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleId, "elitedungeon");
            //判断是否激活了该精英副本
            Map<Integer, Byte> dStatusMap = dsc.getDungeonStatusMap();
            if (dStatusMap == null
                    || !dStatusMap.containsKey(eliteDungeonVo.getActiveDungeon())
                    || dStatusMap.get(eliteDungeonVo.getActiveDungeon()) != DungeonManager.STAGE_PASSED) {
                ServiceHelper.baseTeamService().leaveTeam(roleId);
                continue;
            }

            //判断体力和次数
            int myVigour = rsc.getVigour();
            int playCount = esc.getPlayCount();
            int rewardTimes = esc.getRewardTimes();
            int helpTimes = esc.getHelpTimes();

            if (playCount < rewardTimes) {//判断是否可收益进入
                //判断体力是否足够
                int costVigour = eliteDungeonVo.getVigorCost();
                if (myVigour < costVigour) {
                    ServiceHelper.baseTeamService().leaveTeam(roleId);
                    continue;
                }
            } else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
                //无需消耗，可进入
            } else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
                ServiceHelper.baseTeamService().leaveTeam(roleId);
                continue;
            }
        }
    }

    //判断是否所有队员都能进入该副本
    public boolean canAllChangeTeamTarget(long initiator, int target) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        if (team == null) {
            PlayerUtil.send(initiator, new ClientText("team_noTeam"));
            return false;
        }

        int eliteId = target;

        //检查是否有配置
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) {
            PlayerUtil.send(initiator, new ClientText("获取不到精英副本产品数据"));
            return false;
        }

        Map<Long, String> memberNameMap = new HashMap<Long, String>();
        Map<Long, Byte> tipsTypeMap = new HashMap<Long, Byte>();

        for (BaseTeamMember teamMember : team.getMembers().values()) {
            if (teamMember.getType() == (byte) 1) {//机器人无需判断
                continue;
            }

            long roleId = teamMember.getRoleId();
            DungeonSummaryComponent dsc = (DungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleId, "dungeon");
            RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleId, MConst.Role);
            EliteDungeonSummaryComponent esc = (EliteDungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleId, "elitedungeon");
            //判断是否激活了该精英副本
            Map<Integer, Byte> dStatusMap = dsc.getDungeonStatusMap();
            if (dStatusMap == null
                    || !dStatusMap.containsKey(eliteDungeonVo.getActiveDungeon())
                    || dStatusMap.get(eliteDungeonVo.getActiveDungeon()) != DungeonManager.STAGE_PASSED) {
                memberNameMap.put(roleId, rsc.getRoleName());
                tipsTypeMap.put(roleId, ClientEliteDungeonPacket.TIPS_NOT_ACTIVE);
                continue;
            }

            //判断体力和次数
            int myVigour = rsc.getVigour();
            int playCount = esc.getPlayCount();
            int rewardTimes = esc.getRewardTimes();
            int helpTimes = esc.getHelpTimes();

            if (playCount < rewardTimes) {//判断是否可收益进入
                //判断体力是否足够
                int costVigour = eliteDungeonVo.getVigorCost();
                if (myVigour < costVigour) {
                    memberNameMap.put(roleId, rsc.getRoleName());
                    tipsTypeMap.put(roleId, ClientEliteDungeonPacket.TIPS_NOT_ENOUGH_VIGOUR);
                    continue;
                }
            } else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
                //无需消耗，可进入
            } else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
                memberNameMap.put(roleId, rsc.getRoleName());
                tipsTypeMap.put(roleId, ClientEliteDungeonPacket.TIPS_NO_TIMES);
                continue;
            }
        }

        if (memberNameMap.size() > 0) {
            ClientEliteDungeonPacket clientEliteDungeonPacket = new ClientEliteDungeonPacket(ClientEliteDungeonPacket.ENTER_FIGHT_TIPS);
            clientEliteDungeonPacket.setMemberNameMap(memberNameMap);
            clientEliteDungeonPacket.setTipsTypeMap(tipsTypeMap);
            clientEliteDungeonPacket.setTargetEliteId(eliteId);
            PlayerUtil.send(initiator, clientEliteDungeonPacket);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 退出组队界面
     */
    public void quitFromTeamPage() {
        for (int eliteDungeonId : EliteDungeonManager.getEliteDungeonVoMap().keySet()) {
            ServiceHelper.eliteDungeonService().removeMemberId(id(), eliteDungeonId);
        }
        BaseTeamModule teamModule = module(MConst.Team);
        teamModule.reqCancelMatchTeam(false);
    }

    /**
     * 组队副本死亡没复活次数了，点击离开回城
     */
    public void backToCity() {
        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.backToCity(Boolean.FALSE);
    }

    /**
     * 组队副本奖励结算
     *
     * @param event
     */
    public void finishReward(Event event) {
        EliteDungeonFinishEvent finishEvent = (EliteDungeonFinishEvent) event;
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(finishEvent.getEliteDungeonId());
        if (eliteDungeonVo == null) return;
        Map<Integer, Integer> firstRewardsMap = new HashMap<>();
        Map<Integer, Integer> rewards = new HashMap<>();
        DropModule dropModule = (DropModule) module(MConst.Drop);
        ToolModule toolModule = module(MConst.Tool);
        // 胜利奖励
        if (finishEvent.getResult() == SceneManager.STAGE_VICTORY) {
            //首通奖励
            Set<Integer> passedEliteDungeons = record.getPassedEliteDungeons();
            int eliteDungeonId = finishEvent.getEliteDungeonId();
            if (!passedEliteDungeons.contains(eliteDungeonId)) {
                passedEliteDungeons.add(eliteDungeonId);
                record.setPassedEliteDungeons(passedEliteDungeons);

                Map<Integer, Integer> firstRewards = dropModule.executeDrop(eliteDungeonVo.getFirstreward(), 1, false);
                firstRewardsMap = toolModule.addAndSend(firstRewards, EventType.ELITE_DUNGEON.getCode());
            }

            //收益奖励或助战奖励
            int playCount = record.getPlayCount();
            int rewardTimes = record.getRewardTimes();
            int helpTimes = record.getHelpTimes();
            if (playCount <= rewardTimes) {//发收益奖励
                Map<Integer, Integer> rewardMap = dropModule.executeDrop(eliteDungeonVo.getReward(), 1, false);
                com.stars.util.MapUtil.add(rewards, rewardMap);
                ServerLogModule serverLogModule = module(MConst.ServerLog);
                serverLogModule.logTeamFinish(rewardMap, 1);
                serverLogModule.logBaseTeamFinish(playCount, finishEvent.getSpendTime(), 1, eliteDungeonVo.getEliteId());
            } else if (playCount > rewardTimes && playCount <= rewardTimes + helpTimes) {//发助战奖励
                Map<Integer, Integer> rewardMap = dropModule.executeDrop(eliteDungeonVo.getHelpreward(), 1, false);
                com.stars.util.MapUtil.add(rewards, rewardMap);
                ServerLogModule serverLogModule = module(MConst.ServerLog);
                serverLogModule.logTeamFinish(rewardMap, 1);
                serverLogModule.logBaseTeamFinish(playCount, finishEvent.getSpendTime(), 1, eliteDungeonVo.getEliteId());
            }

        } else if (finishEvent.getResult() == SceneManager.STAGE_FAIL) {
            ServerLogModule serverLogModule = module(MConst.ServerLog);
            serverLogModule.logTeamFinish(null, 0);
            serverLogModule.logBaseTeamFinish(record.getPlayCount(), finishEvent.getSpendTime(), 0, eliteDungeonVo.getEliteId());
        }
        // 怪物掉落
        com.stars.util.MapUtil.add(rewards, monsterDrop);
        Map<Integer, Integer> map = toolModule.addAndSend(rewards, EventType.ELITE_DUNGEON.getCode());
        monsterDrop.clear();
        ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ELITEDUNGEON, finishEvent.getResult());
        packet.setEliteFirstReward(firstRewardsMap);
        packet.setItemMap(map);
        send(packet);


        // 结束日志
//        byte logType = finishEvent.getResult() == SceneManager.STAGE_VICTORY ?
//                ServerLogConst.ACTIVITY_WIN : ServerLogConst.ACTIVITY_FAIL;
//        doLog(teamDungeonVo.getType(), logType, finishEvent.getStageId(), finishEvent.getSpendTime());
    }

    public RecordMapEliteDungeon getRecord() {
        return record;
    }

    /**
     * 怪物掉落缓存并发送客户端
     *
     * @param dropIds
     */
    public void addMonsterDrop(Map<String, Integer> dropIds) {
        Map<String, List<Map<Integer, Integer>>> sendDrop = new HashMap<>();
        DropModule dropModule = module(MConst.Drop);
        for (Map.Entry<String, Integer> entry : dropIds.entrySet()) {
            List<Map<Integer, Integer>> mapList = dropModule.executeDropNotCombine(entry.getValue(), 1, false);
            for (Map<Integer, Integer> map : mapList) {
                MapUtil.add(monsterDrop, map);
            }
            sendDrop.put(entry.getKey(), mapList);
        }
        // send to client
        ClientMonsterDrop packet = new ClientMonsterDrop(sendDrop);
        send(packet);
    }

    /**
     * 处理副本通关时间
     */
    public void handlePassStageEvent(PassStageEvent event) {
        int passDungeonId = event.getStageId();
        Map<Integer, EliteDungeonVo> eliteDungeonVos = EliteDungeonManager.getEliteDungeonVoMap();
        for (EliteDungeonVo eliteDungeonVo : eliteDungeonVos.values()) {
            if (eliteDungeonVo.getActiveDungeon() == passDungeonId) {
                sendEliteData();
                return;
            }
        }
    }
    
    public void addPlayerImageDate(int stageId){
    	RoleModule module = module(MConst.Role);
    	ElitePlayerImagePo po = new ElitePlayerImagePo();
    	Role roleRow = module.getRoleRow();
    	po.setCreateTime(DateUtil.getCurrentTimeInt());
    	po.setRoleid(roleRow.getRoleId());
    	po.setName(EliteDungeonManager.randomName());
    	po.setJob(roleRow.getJobId());
    	po.setLevel(roleRow.getLevel());
    	po.setStageid(stageId);
    	po.setFightScore(module.getFightScore());
    	po.setAttribute(new Attribute(roleRow.getTotalAttr()));
    	SkillModule skillModule = module(MConst.Skill);
    	po.setSkillMap(skillModule.getUseSkill());
    	po.setRobotSkillDamage(skillModule.getSkillDamageMap());
    	BuddyModule buddyModule = module(MConst.Buddy);
    	po.setBuddyId(buddyModule.getFightBuddyId());
    	RoleBuddy roleBuddy = buddyModule.getRoleBuddy(buddyModule.getFightBuddyId());
    	if(roleBuddy!=null){
    		po.setBuddyLevel(roleBuddy.getLevel());
    		po.setBuddyStageLevel(roleBuddy.getStageLevel());
    	}
    	ServiceHelper.eliteDungeonService().addPlayerImageData(po);
    }
    
    public void gmHandler(String[] args){
    	byte opType = Byte.parseByte(args[0]);
    	if(opType==1){
    		EliteDungeonManager.KEEP_MAX_NUM = Integer.parseInt(args[1]);
    	}
    }
}
