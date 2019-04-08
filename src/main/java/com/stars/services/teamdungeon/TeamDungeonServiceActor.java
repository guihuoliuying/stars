package com.stars.services.teamdungeon;

import com.stars.core.event.Event;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.JoinActivityEvent;
import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.imp.fight.MarryDungeonScene;
import com.stars.modules.scene.imp.fight.TeamDungeonScene;
import com.stars.modules.scene.packet.ClientRoleRevive;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teamdungeon.event.BackToCityFromTeamDungeonEvent;
import com.stars.modules.teamdungeon.event.TeamDungeonEnterEvent;
import com.stars.modules.teamdungeon.event.TeamDungeonExitEvent;
import com.stars.modules.teamdungeon.packet.ClientTeamDungeonPacket;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2016/11/14.
 */
public class TeamDungeonServiceActor extends ServiceActor implements TeamDungeonService {
    // 参与的玩家数据池(有次数且打开组队副本界面)
    private Map<Integer, Set<Long>> memberIdPool;
    // 战斗场景,<teamId, FightScene>
    private Map<Integer, TeamDungeonScene> fightSceneMap;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("teamDungeonService", this);
        memberIdPool = new HashMap<>();
        fightSceneMap = new HashMap<>();

        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.TeamDungeon, new SchedulerTask(), 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},memberIdPool:{},fightSceneMap:{}", this.getClass().getSimpleName(), memberIdPool.size()
                , fightSceneMap.size());
    }

    @Override
    public boolean addMemberId(long roleId, int teamDungeonId) {
        if (!memberIdPool.containsKey(teamDungeonId)) {
            memberIdPool.put(teamDungeonId, new HashSet<Long>());
        }
        memberIdPool.get(teamDungeonId).add(roleId);
        fireSpecialAccountEvent(roleId, roleId, "加入组队:" + teamDungeonId, true);
        return true;
    }

    private void fireSpecialAccountEvent(long selfId, long roleId, String content, boolean self) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(selfId, new SpecialAccountEvent(roleId, content, self));
        }
    }

    @Override
    public boolean removeMemberId(long roleId, int teamDungeonId) {
        if (!memberIdPool.containsKey(teamDungeonId)) {
            return false;
        }
        // 有队伍,先退队
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team != null) {
            TeamDungeonScene teamDungeonScene = fightSceneMap.get(team.getTeamId());
            // 队伍正在战斗
            if (teamDungeonScene != null && teamDungeonScene.stageStatus == SceneManager.STAGE_PROCEEDING) {
                teamDungeonScene.exit(roleId);
                int spendTime = (int) ((System.currentTimeMillis() - teamDungeonScene.startTimestamp) / 1000);
                ServiceHelper.roleService().notice(roleId, new TeamDungeonExitEvent(team.getTarget(), spendTime,
                        teamDungeonScene.stageId));
                if (teamDungeonScene.hasNoPlayer()) {
                    teamDungeonScene.stageStatus = SceneManager.STAGE_FAIL;
                    ServiceHelper.teamDungeonService().removeFightScene(teamDungeonScene.teamId);
                }
            }
            if (team.getTeamType() == BaseTeamManager.TEAM_TYPE_DAILYDUNGEON) {
                ServiceHelper.baseTeamService().leaveTeam(roleId);
            }
        }
        fireSpecialAccountEvent(roleId, roleId, "移除组队:" + teamDungeonId, true);
        return memberIdPool.get(teamDungeonId).remove(roleId);
    }

    @Override
    public void removeFightScene(long roleId, BaseTeam team) {
        TeamDungeonScene teamDungeonScene = fightSceneMap.get(team.getTeamId());
        // 队伍正在战斗
        if (teamDungeonScene != null && teamDungeonScene.stageStatus == SceneManager.STAGE_PROCEEDING) {
            teamDungeonScene.exit(roleId);
            int spendTime = (int) ((System.currentTimeMillis() - teamDungeonScene.startTimestamp) / 1000);
            ServiceHelper.roleService().notice(roleId, new TeamDungeonExitEvent(team.getTarget(), spendTime,
                    teamDungeonScene.stageId));
            if (teamDungeonScene.hasNoPlayer()) {
                teamDungeonScene.stageStatus = SceneManager.STAGE_FAIL;
                ServiceHelper.teamDungeonService().removeFightScene(teamDungeonScene.teamId);
            }
        }
    }

    @Override
    public boolean isMemberIn(long roleId, int teamDungeonId) {
        fireSpecialAccountEvent(roleId, roleId, "判断是否可加入:" + teamDungeonId, true);
        if (!memberIdPool.containsKey(teamDungeonId))
            return false;
        return memberIdPool.get(teamDungeonId).contains(roleId);
    }

    /**
     * 下发可邀请列表
     *
     * @param initiator
     * @param target
     * @param scene
     */
    @Override
    public void sendCanInviteList(long initiator, int target, String scene) {
        int teamDungeonId = target;
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(teamDungeonId);
        reqArroundPlayerCanInviteList(initiator, scene, teamDungeonVo.getLevellimit(), target);
        sendCandidateInviteeList(initiator, scene, teamDungeonVo.getLevellimit());
        fireSpecialAccountEvent(initiator, initiator, "下发可邀请列表:" + teamDungeonId, true);
    }

    /**
     * 进入副本战斗
     *
     * @param initiator
     */
    @Override
    public void enterFight(long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        int teamDungeonId = team.getTarget();
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(teamDungeonId);
        //判断队伍人数是否足够进入副本战斗
        if (teamDungeonVo == null || team.getMemberCount() < team.getMinMemberCount()) {
            PlayerUtil.send(initiator, new ClientText("playerteam_dungeon_minnumber", Integer.toString(team.getMinMemberCount())));
            return;
        }
        TeamDungeonScene teamDungeonScene = (TeamDungeonScene) SceneManager.newScene(SceneManager.SCENETYPE_TEAMDUNGEON);
        // 业务根据平均等级找到注入stageId
        int stageId = teamDungeonVo.getStageIdByLevel(team.getAverageLevel());
        StageinfoVo stageinfoVo = SceneManager.getStageVo(stageId);
        if (stageinfoVo == null) {
            return;
        }
        teamDungeonScene.stageId = stageId;
        //设置成员ids
        teamDungeonScene.addTeamMemberFighter(team.getMembers().values());
        if (!teamDungeonScene.canEnter(null, teamDungeonId)) {
            return;
        }
        teamDungeonScene.enter(null, teamDungeonId);
        teamDungeonScene.teamId = team.getTeamId();
        team.setFight(Boolean.TRUE);
        fightSceneMap.put(team.getTeamId(), teamDungeonScene);
        sendEvent(team.getPlayerMembers().keySet(), new TeamDungeonEnterEvent(stageId, teamDungeonVo.getTeamdungeonid()));
        sendEvent(team.getPlayerMembers().keySet(), new DailyFuntionEvent(teamDungeonVo.getDailyid(), 1));
        sendEvent(team.getPlayerMembers().keySet(), new JoinActivityEvent(getAchievementId(teamDungeonVo.getType())));
        fireSpecialAccountEvent(initiator, initiator, "进入副本战斗:" + teamDungeonId, true);
    }

    @Override
    public void enterMarryFight(long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        int teamDungeonId = team.getTarget();
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(teamDungeonId);
        //判断队伍人数是否足够进入副本战斗
        if (teamDungeonVo == null || team.getMemberCount() < team.getMinMemberCount()) {
            PlayerUtil.send(initiator, new ClientText("playerteam_dungeon_minnumber", Integer.toString(team.getMinMemberCount())));
            return;
        }
        MarryDungeonScene marryDungeonScene = (MarryDungeonScene) SceneManager.newScene(SceneManager.SCENETYPE_MARRY_DUNGEON);
        // 业务根据平均等级找到注入stageId
        int stageId = teamDungeonVo.getStageIdByLevel(team.getAverageLevel());
        StageinfoVo stageinfoVo = SceneManager.getStageVo(stageId);
        if (stageinfoVo == null) {
            return;
        }
        marryDungeonScene.stageId = stageId;
        //设置成员ids
        marryDungeonScene.addTeamMemberFighter(team.getMembers().values());
        if (!marryDungeonScene.canEnter(null, teamDungeonId)) {
            return;
        }
        marryDungeonScene.enter(null, teamDungeonVo);
        marryDungeonScene.teamId = team.getTeamId();
        team.setFight(Boolean.TRUE);
        fightSceneMap.put(team.getTeamId(), marryDungeonScene);
        LogUtil.info("结婚组队|roleIds：{} 进入结婚组队副本", team.getMembers().keySet());
        sendEvent(team.getPlayerMembers().keySet(), new TeamDungeonEnterEvent(stageId, teamDungeonVo.getTeamdungeonid()));
//        sendEvent(team.getPlayerMembers().keySet(), new DailyFuntionEvent(teamDungeonVo.getDailyid(), 1));
        sendEvent(team.getPlayerMembers().keySet(), new JoinActivityEvent(getAchievementId(teamDungeonVo.getType())));
        fireSpecialAccountEvent(initiator, initiator, "进入副本战斗:" + teamDungeonId, true);
    }

    @Override
    public void backToCity(long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        // 只有队长可以发起回城
        if (team == null || team.getCaptainId() != initiator) {
            PlayerUtil.send(initiator, new ClientText("team_notCaptain"));
            return;
        }
        //发回城消息给所有队员
        sendEvent(team.getPlayerMembers().keySet(), new BackToCityFromTeamDungeonEvent(team.getTarget()));
        team.setFight(Boolean.FALSE);
        // 销毁scene
        fightSceneMap.remove(team.getTeamId());
        fireSpecialAccountEvent(initiator, initiator, "组队副本回城", true);
    }

    public void deadInDungeon(long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        TeamDungeonScene teamDungeonScene = (TeamDungeonScene) fightSceneMap.get(team.getTeamId());
        if (teamDungeonScene == null) {
            LogUtil.info("TeamServiceActor.deadInDungeon teamDungeonScene is null");
            return;
        }
        teamDungeonScene.dead(initiator, team.getTarget());
        //退出队伍
        ServiceHelper.baseTeamService().leaveTeam(initiator);
        //发送消息给客户端
        ClientTeamDungeonPacket packet = new ClientTeamDungeonPacket(ClientTeamDungeonPacket.BACK_TO_CITY);
        PlayerUtil.send(initiator, packet);
        fireSpecialAccountEvent(initiator, initiator, "组队副本死掉无法复活回城", true);
    }

    @Override
    public void receiveFightPacket(PlayerPacket packet) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(packet.getRoleId());
        if (team == null) {
            return;
        }
        TeamDungeonScene teamDungeonScene = (TeamDungeonScene) fightSceneMap.get(team.getTeamId());
        if (teamDungeonScene == null) {
            return;
        }
        teamDungeonScene.receivePacket(null, packet);
        fireSpecialAccountEvent(packet.getRoleId(), packet.getRoleId(), "组队副本收到战斗的包", true);
    }

    @Override
    public void receiveMarryFightPacket(PlayerPacket packet) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(packet.getRoleId());
        if (team == null) {
            return;
        }
        MarryDungeonScene marryDungeonScene = (MarryDungeonScene) fightSceneMap.get(team.getTeamId());
        if (marryDungeonScene == null) {
            return;
        }
        marryDungeonScene.receivePacket(null, packet);
        fireSpecialAccountEvent(packet.getRoleId(), packet.getRoleId(), "组队副本收到战斗的包", true);
    }

    @Override
    public boolean checkResurgence(long roleId) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        fireSpecialAccountEvent(roleId, roleId, "组队副本复活检查", true);
        if (team == null)
            return false;
        TeamDungeonScene teamDungeonScene = (TeamDungeonScene) fightSceneMap.get(team.getTeamId());
        if (teamDungeonScene != null) {
            boolean result = teamDungeonScene.checkRevive(String.valueOf(roleId));
            if (result) {
                sendPacket(team.getPlayerMembers().keySet(), new ClientRoleRevive(roleId, result));
            }
            return result;
        }
        return false;
    }

    @Override
    public void removeFightScene(int teamId) {
        LogUtil.info("removeFightScene|teamId:{},fightSceneMap:{}", teamId, fightSceneMap.keySet());
        fightSceneMap.remove(teamId);
    }

    /**
     * 给指定roleId通知事件
     *
     * @param memberIds
     * @param event
     */
    private void sendEvent(Collection<Long> memberIds, Event event) {
        for (long roleId : memberIds) {
            ServiceHelper.roleService().notice(roleId, event);
        }
    }

    /**
     * 给指定roleId发包
     *
     * @param memberIds
     * @param packet
     */
    private void sendPacket(Collection<Long> memberIds, Packet packet) {
        for (long roleId : memberIds) {
            PlayerUtil.send(roleId, packet);
        }
    }

    /**
     * 根据玩法类型获得成就Id
     *
     * @param teamDungeonType
     * @return
     */
    private short getAchievementId(byte teamDungeonType) {
        short achievementId = 0;
        switch (teamDungeonType) {
            case 1:// 守护类
                achievementId = JoinActivityEvent.TEAMDUNGEON_DEFEND;
                break;
            case 2:// 挑战类
                achievementId = JoinActivityEvent.TEAMDUNGEON_CHALLENGE;
                break;
        }
        return achievementId;
    }

    /**
     * 可邀请列表-周围玩家
     *
     * @param initiator
     * @param scene
     * @param levelLimit
     * @param target
     */
    private void reqArroundPlayerCanInviteList(long initiator, String scene, int levelLimit, int target) {
        ClientBaseTeamInvite cti = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        cti.setInviteeType((byte) 0);
        Map<Long, ArroundPlayer> players = ServiceHelper.arroundPlayerService().getArroundPlayersBySceneId(scene);
        List<ArroundPlayer> ls = new ArrayList<>(players.values());
        if (ls != null) {
            TeamInvitee tInvitee;
            for (ArroundPlayer arroundPlayer : ls) {
                if (arroundPlayer.getRoleId() == initiator || arroundPlayer.getLevel() < levelLimit) {
                    continue;
                }
                if (ServiceHelper.baseTeamService().hasTeam(arroundPlayer.getRoleId())) {
                    continue;
                }
                tInvitee = new TeamInvitee();
                TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(target);
                short dailyid = teamDungeonVo.getDailyid();
                ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(arroundPlayer.getRoleId(), MConst.ForeShow);
                if (ForeShowConst.DAILY_TEAM_DEFEND.endsWith(dailyid + "")) {
                    if (!fsSummary.isOpen(ForeShowConst.DAILY_TEAM_DEFEND)) {
                        continue;
                    }
                }
                if (ForeShowConst.DAILY_TEAM_CHALLENGE.endsWith(dailyid + "")) {
                    if (!fsSummary.isOpen(ForeShowConst.DAILY_TEAM_CHALLENGE)) {
                        continue;
                    }
                }
                tInvitee.setId(arroundPlayer.getRoleId());
                tInvitee.setName(arroundPlayer.getName());
                tInvitee.setLevel(arroundPlayer.getLevel());
                tInvitee.setJob((byte) arroundPlayer.getJob());
                RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                        arroundPlayer.getRoleId(), "role");
                tInvitee.setFightScore(rsc.getFightScore());
                cti.addInvitee(tInvitee);
            }
        }
        PlayerUtil.send(initiator, cti);
    }

    /**
     * 可邀请列表-好友&家族
     *
     * @param initiator
     * @param scene
     * @param levelLimit
     */
    private void sendCandidateInviteeList(long initiator, String scene, int levelLimit) {
        Map<Long, ArroundPlayer> players = ServiceHelper.arroundPlayerService().getArroundPlayersBySceneId(scene);
        // friend
        List<Long> friendIdList = ServiceHelper.friendService().getFriendList(initiator); // 拿好友列表
        List<Summary> onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(friendIdList); // 获取

        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType(TeamDungeonManager.INVOTEE_TYPE_FRIEND);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (comp.getRoleLevel() < levelLimit
                    || ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == initiator) {
                continue;
            }
            TeamInvitee invitee = new TeamInvitee();
            invitee.setId(summary.getRoleId());
            invitee.setName(comp.getRoleName());
            invitee.setJob((byte) comp.getRoleJob());
            invitee.setLevel((short) comp.getRoleLevel());
            invitee.setFightScore(comp.getFightScore());
            packet.addInvitee(invitee);
        }
        PlayerUtil.send(initiator, packet);
        // family
        long familyId = ServiceHelper.familyRoleService().getFamilyId(initiator); // 拿家族id
        List<Long> familyMemberIdList = ServiceHelper.familyMainService().getMemberIdList(familyId, initiator); // 拿家族成员列表
        onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(familyMemberIdList); // 获取
        packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType(TeamDungeonManager.INVOTEE_TYPE_FAMILY);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (comp.getRoleLevel() < levelLimit
                    || ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == initiator) {
                continue;
            }
            TeamInvitee invitee = new TeamInvitee();
            invitee.setId(summary.getRoleId());
            invitee.setName(comp.getRoleName());
            invitee.setJob((byte) comp.getRoleJob());
            invitee.setLevel((short) comp.getRoleLevel());
            invitee.setFightScore(comp.getFightScore());
            packet.addInvitee(invitee);
        }
        PlayerUtil.send(initiator, packet);
    }

    class SchedulerTask implements Runnable {
        @Override
        public void run() {
            ServiceHelper.teamDungeonService().executeTask();
        }
    }

    @Override
    public void executeTask() {
        if (fightSceneMap != null) {
            for (TeamDungeonScene teamDungeonScene : fightSceneMap.values()) {
                teamDungeonScene.onTime();
            }
        }
    }
}
