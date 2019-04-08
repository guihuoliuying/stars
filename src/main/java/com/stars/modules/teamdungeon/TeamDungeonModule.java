package com.stars.modules.teamdungeon;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamModule;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.ClientMonsterDrop;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.teamdungeon.event.DeadInTeamDungeonEvent;
import com.stars.modules.teamdungeon.event.TeamDungeonEnterEvent;
import com.stars.modules.teamdungeon.event.TeamDungeonExitEvent;
import com.stars.modules.teamdungeon.event.TeamDungeonFinishEvent;
import com.stars.modules.teamdungeon.packet.ClientTeamDungeonPacket;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;

import java.util.*;

/**
 * Created by liuyuheng on 2016/11/11.
 */
public class TeamDungeonModule extends AbstractModule {
    private Map<Integer, Integer> monsterDrop = new HashMap<>();// 组队副本怪物掉落
    private long actStartTime = 0L;

    public TeamDungeonModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("日常组队副本", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onOffline() throws Throwable {
        // 离线做退出活动处理
        if (SpecialAccountManager.isSpecialAccount(id())) return;
        for (int teamDungeonId : TeamDungeonManager.getTeamDungeonVoMap().keySet()) {
            ServiceHelper.teamDungeonService().removeMemberId(id(), teamDungeonId);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            sendTeamDungeon();
        }
    }

    @Override
    public void onTimingExecute() {
    }

    /**
     * 发送组队副本信息
     */
    public void sendTeamDungeon() {
        ClientTeamDungeonPacket ctd = new ClientTeamDungeonPacket(ClientTeamDungeonPacket.TEAM_DUNGEON_INFO);
        RoleModule rm = module(MConst.Role);
        DailyModule dm = module(MConst.Daily);
        Map<Integer, TeamDungeonVo> map = TeamDungeonManager.getTeamDungeonVoMap();
        Map<Integer, Integer> remainCountMap = new HashMap<>();
        Collection<TeamDungeonVo> coll = map.values();
        for (TeamDungeonVo teamDungeon : coll) {
            if (rm.getLevel() < teamDungeon.getLevellimit()) {
                continue;
            }
            if (teamDungeon.getEntrance() != TeamDungeonManager.ENTRANCE_DAILY) {
                continue;
            }
            ctd.addTeamDungeon(teamDungeon);
            remainCountMap.put(teamDungeon.getTeamdungeonid(), dm.getDailyRemain(teamDungeon.getDailyid()));
            if (dm.getDailyRemain(teamDungeon.getDailyid()) > 0) {
                ServiceHelper.teamDungeonService().addMemberId(id(), teamDungeon.getTeamdungeonid());
            }
        }
        ctd.setRemainCountMap(remainCountMap);
        send(ctd);
        this.actStartTime = System.currentTimeMillis();
    }

    /**
     * 进入队伍副本
     */
    public void enterDegeno() {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        //判断是否有剩余次数可挑战
        TeamDungeonVo teamDungeon = TeamDungeonManager.getTeamDungeonVo(team.getTarget());
        if (teamDungeon == null) {
            warn(I18n.get("team.dataError"));
            return;
        }
        short dailyId = teamDungeon.getDailyid();
        DailyModule dailyModule = module(MConst.Daily);
        int dailyRemainCount = dailyModule.getDailyRemain(dailyId);
        if (dailyRemainCount <= 0) {
            warn("playerteam_enterchance_empty", teamDungeon.getName());
            return;
        }
        ServiceHelper.teamDungeonService().enterFight(id());
    }

    /**
     * 组队副本回城
     *
     * @param teamDungeonId
     */
    public void backToCityFromTeamDungeon(int teamDungeonId) {
        DailyModule dailyModule = module(MConst.Daily);
        TeamDungeonVo teamDungeon = TeamDungeonManager.getTeamDungeonVo(teamDungeonId);
        if (teamDungeon != null) {
            short dailyId = teamDungeon.getDailyid();
            int remainCount = dailyModule.getDailyRemain(dailyId);
            if (remainCount <= 0) {
                ServiceHelper.teamDungeonService().removeMemberId(id(), teamDungeonId);
            }
        }
        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.backToCity(Boolean.FALSE);
    }

    /**
     * 组队副本死亡没复活次数了，点击离开回城
     */
    public void backToCity() {
        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.backToCity(Boolean.FALSE);
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
                com.stars.util.MapUtil.add(monsterDrop, map);
            }
            sendDrop.put(entry.getKey(), mapList);
        }
        // send to client
        ClientMonsterDrop packet = new ClientMonsterDrop(sendDrop);
        send(packet);
    }

    /**
     * 组队副本奖励结算
     *
     * @param event
     */
    public void finishReward(Event event) {
        TeamDungeonFinishEvent finishEvent = (TeamDungeonFinishEvent) event;
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(finishEvent.getTeamDungeonId());
        Map<Integer, Integer> rewards = new HashMap<>();
        // 胜利失败奖励
        if (finishEvent.getResult() == SceneManager.STAGE_VICTORY) {
            com.stars.util.MapUtil.add(rewards, teamDungeonVo.getVictoryRewards());
        } else {
            com.stars.util.MapUtil.add(rewards, teamDungeonVo.getDefeatRewards());
        }
        // 守护奖励
        byte protectHpRemain = finishEvent.getTargetHpRemain();
        if (protectHpRemain > 0) {
            Map<Integer, Integer> protectRewards = new HashMap<>();
            byte flag = 0;
            for (Map.Entry<Byte, Map<Integer, Integer>> entry : teamDungeonVo.getProtectRewards().entrySet()) {
                if (protectHpRemain > flag && protectHpRemain <= entry.getKey()) {
                    break;
                }
                protectRewards = entry.getValue();
                flag = entry.getKey();
            }
            com.stars.util.MapUtil.add(rewards, protectRewards);
        }
        // 伤害奖励
        Map<String, Integer> damageMap = finishEvent.getDamageMap();
        String myUniqueId = Long.toString(id());
        int damage = 0;
        if (damageMap.containsKey(myUniqueId)) {
            damage = damageMap.get(myUniqueId);
        }

        if (!teamDungeonVo.getDamageRewards().isEmpty() && damage > 0) {
            Map<Integer, Integer> damageRewards = new HashMap<>();
            int flag = 0;
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : teamDungeonVo.getDamageRewards().entrySet()) {
                damageRewards = entry.getValue();
                if (damage > flag && damage <= entry.getKey()) {
                    break;
                }
                flag = entry.getKey();
            }
            com.stars.util.MapUtil.add(rewards, damageRewards);
        }
        // 怪物掉落
        com.stars.util.MapUtil.add(rewards, monsterDrop);
        ToolModule toolModule = module(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(rewards, EventType.TEAMDUNGEON.getCode());
        monsterDrop.clear();
        ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_TEAMDUNGEON, finishEvent.getResult());
        // 组队关系加成
        Map<Integer, Integer> addReward = calAddReward(map, teamDungeonVo, packet);
        toolModule.addAndSend(addReward, EventType.TEAMDUNGEON.getCode());
        com.stars.util.MapUtil.add(map, addReward);
        packet.setDamage(damage);
        packet.setItemMap(map);
        send(packet);
        // 结束日志
        byte logType = finishEvent.getResult() == SceneManager.STAGE_VICTORY ?
                ServerLogConst.ACTIVITY_WIN : ServerLogConst.ACTIVITY_FAIL;
        doLog(teamDungeonVo.getType(), logType, finishEvent.getStageId(), finishEvent.getSpendTime());
    }

    private Map<Integer, Integer> calAddReward(Map<Integer, Integer> reward, TeamDungeonVo teamDungeonVo,
                                               ClientStageFinish packet) {
        // 关系加成奖励
        Map<Integer, Integer> addReward = new HashMap<>();
        // 下发关系加成类型
        StringBuilder addRewardType = new StringBuilder("");
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) {
            packet.setAddRewardType(addRewardType.toString());
            return addReward;
        }
        int addPercent = 0;// 总加成系数
        // 关系加成只累加一次
        boolean addFriend = false, addCouple = false, addFamily = false;
        FriendModule friendModule = module(MConst.Friend);
        MarryModule marryModule = module(MConst.Marry);
        FamilyModule familyModule = module(MConst.Family);
        long myFamilyId = familyModule.getAuth().getFamilyId();
        for (long memberId : team.getPlayerMembers().keySet()) {
            if (memberId == id())
                continue;
            if (!addFriend && friendModule.isFriend(memberId)) {
                addPercent = addPercent + teamDungeonVo.getAddRewardPercent(TeamDungeonManager.ADDREWARD_TYPE_FRIEND);
                addRewardType.append(TeamDungeonManager.ADDREWARD_TYPE_FRIEND).append(",");
                addFriend = Boolean.TRUE;
            }
            if (!addCouple && marryModule.isMarried(memberId)) {
                addPercent = addPercent + teamDungeonVo.getAddRewardPercent(TeamDungeonManager.ADDREWARD_TYPE_COUPLE);
                addRewardType.append(TeamDungeonManager.ADDREWARD_TYPE_COUPLE).append(",");
                addCouple = Boolean.TRUE;
            }
            if (!addFamily && myFamilyId != 0) {
                FamilySummaryComponent fsc = (FamilySummaryComponent) ServiceHelper.summaryService().
                        getSummaryComponent(memberId, "family");
                if (fsc != null && myFamilyId == fsc.getFamilyId()) {
                    addPercent = addPercent + teamDungeonVo.getAddRewardPercent(TeamDungeonManager.ADDREWARD_TYPE_FAMILY);
                    addRewardType.append(TeamDungeonManager.ADDREWARD_TYPE_FAMILY).append(",");
                    addFamily = Boolean.TRUE;
                }
            }
        }
        if (addRewardType.length() > 0) {
            addRewardType.deleteCharAt(addRewardType.lastIndexOf(","));
        }
        packet.setAddRewardType(addRewardType.toString());
        for (Map.Entry<Integer, Integer> entry : reward.entrySet()) {
            if (teamDungeonVo.containAddReward(entry.getKey())) {
                addReward.put(entry.getKey(), (int) Math.ceil(entry.getValue() * addPercent / 100.0));
            }
        }
        return addReward;
    }

    /**
     * 中途死亡退出失败奖励结算
     */
    public void failReward(Event event) {
        DeadInTeamDungeonEvent deadEvent = (DeadInTeamDungeonEvent) event;
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(deadEvent.getTeamDungeonId());
        if (teamDungeonVo == null) {
            LogUtil.info("TeamModule.failReward teamDungeonVo is null");
            return;
        }

        Map<Integer, Integer> rewards = new HashMap<>();
        //失败奖励
        com.stars.util.MapUtil.add(rewards, teamDungeonVo.getDefeatRewards());
        //伤害奖励
        int damage = deadEvent.getDamage();
        if (!teamDungeonVo.getDamageRewards().isEmpty() && damage > 0) {
            Map<Integer, Integer> damageRewards = new HashMap<>();
            int flag = 0;
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : teamDungeonVo.getDamageRewards().entrySet()) {
                damageRewards = entry.getValue();
                if (damage > flag && damage <= entry.getKey()) {
                    break;
                }
                flag = entry.getKey();
            }
            MapUtil.add(rewards, damageRewards);
        }
        ToolModule toolModule = module(MConst.Tool);
        rewards = toolModule.addAndSend(rewards, EventType.TEAMDUNGEON.getCode());
        //发送消息给客户端
        ClientTeamDungeonPacket packet = new ClientTeamDungeonPacket(ClientTeamDungeonPacket.BACK_TO_CITY);
        packet.setDamage(damage);
        packet.setItemMap(rewards);
        send(packet);
        // 结束日志
        doLog(teamDungeonVo.getType(), ServerLogConst.ACTIVITY_FAIL, deadEvent.getStageId(), deadEvent.getSpendTime());
    }

    /**
     * 退出组队界面
     */
    public void quitFromTeamPage() {
        for (int teamDungeonId : TeamDungeonManager.getTeamDungeonVoMap().keySet()) {
            ServiceHelper.teamDungeonService().removeMemberId(id(), teamDungeonId);
        }
        BaseTeamModule teamModule = module(MConst.Team);
        teamModule.reqCancelMatchTeam(false);
    }

    /**
     * 进入副本处理
     *
     * @param event
     */
    public void enterHandler(Event event) {
        TeamDungeonEnterEvent enterEvent = (TeamDungeonEnterEvent) event;
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(enterEvent.getTeamDungeonId());
        // 开始日志
        doLog(teamDungeonVo.getType(), ServerLogConst.ACTIVITY_START, enterEvent.getStageId(), 0);
    }

    /**
     * 退出副本处理
     *
     * @param event
     */
    public void exitHandler(Event event) {
        TeamDungeonExitEvent exitEvent = (TeamDungeonExitEvent) event;
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(exitEvent.getTeamDungeonId());
        // 结束日志
        doLog(teamDungeonVo.getType(), ServerLogConst.ACTIVITY_FAIL, exitEvent.getStageId(), exitEvent.getSpendTime());
    }

    /**
     * 日志
     *
     * @param teamDungeonType
     * @param logType
     * @param stageId
     * @param spendTime
     */
    private void doLog(byte teamDungeonType, byte logType, int stageId, int spendTime) {
        ServerLogModule logModule = module(MConst.ServerLog);
        ThemeType themeType = getLogThemeType(teamDungeonType);
        if (themeType != null) {
            logModule.Log_core_activity(logType, themeType.getThemeId(), logModule.makeJuci(),
                    themeType.getThemeId(), stageId, spendTime);
        }
    }

    /**
     * 根据副本类型获得日志类型
     *
     * @param teamDungeonType
     * @return
     */
    private ThemeType getLogThemeType(byte teamDungeonType) {
        ThemeType themeType = null;
        switch (teamDungeonType) {
            case 1:
                themeType = ThemeType.ACTIVITY_12;
                break;
            case 2:
                themeType = ThemeType.ACTIVITY_13;
                break;
        }
        return themeType;
    }
}
