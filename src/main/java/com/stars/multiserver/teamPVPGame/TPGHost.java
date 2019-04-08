package com.stars.multiserver.teamPVPGame;

import com.stars.bootstrap.ServerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.teampvpgame.packet.ClientTPGData;
import com.stars.modules.teampvpgame.packet.ClientTPGScoreRank;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.teamPVPGame.helper.TPGGroup;
import com.stars.multiserver.teamPVPGame.stepIns.AbstractTPGStep;
import com.stars.multiserver.teamPVPGame.stepIns.SignupTPGStep;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.chat.cache.MyLinkedList;
import com.stars.services.chat.cache.MyLinkedListNode;
import com.stars.services.fightbase.FightBaseService;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;

import java.util.*;

/**
 * @author dengzhou
 *         主办方，整个赛事的流程控制
 */
public class TPGHost {

    /**
     * 活动ID
     */
    private String tpgId;
    private int tpgIdCreator;// 活动Id创建
    private byte tpgType;// 类型;1=本服/2=跨服
    private MyLinkedList<String> flows;

    private MyLinkedListNode<String> step;

    private AbstractTPGStep tpgStep;

    private String dbAlias;
    // 各阶段对阵图 <step, groupMap>
    private Map<String, Map<Integer, TPGGroup>> faceMap;
    // 积分赛排行榜
    private List<TPGTeam> scoreRank;
    // 所有队伍信息缓存
    private Map<Integer, TPGTeam> teamInfoMap;
    // roleId-teamId
    private Map<Long, Integer> roleId2Team;

    public TPGHost(String tpgId) {
        this.tpgId = tpgId;
    }

    /**
     * @param tpgId
     * @param flow  流程
     */
    public TPGHost(String tpgId, byte tpgType, List<String> flow, String dbAlias) {
        this.tpgId = tpgId;
        this.tpgType = tpgType;
        this.dbAlias = dbAlias;
        this.faceMap = new HashMap<>();
        this.scoreRank = new LinkedList<>();
        this.teamInfoMap = new HashMap<>();
        this.roleId2Team = new HashMap<>();
        String stepT = loadStepFromDB();
        flows = new MyLinkedList<String>();
        for (String string : flow) {
            MyLinkedListNode<String> node = flows.addLast(string);
            if (stepT != null && string.equals(stepT)) {
                this.step = node;
                continue;
            }
        }
        for (String string : flow) {
            if (stepT != null && string.equals(stepT)) {
                break;
            } else {// 已经结束的阶段要加载一次,有需要用到以前的数据(积分赛排行榜,小组赛对阵图等)
                try {
                    AbstractTPGStep finishedStep = TPGUtil.tpgStepInsClassMap.get(string).newInstance();
                    finishedStep.initFromDb(this);
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                    System.exit(-1);
                }
            }
        }
        boolean initFromDB = true;
        if (this.step == null) {
            //新开启的
            this.tpgId = String.valueOf(newTPGId());
            this.step = flows.getFirst();
            initFromDB = false;
            //入库
            StringBuilder builder = new StringBuilder("insert into teampvpgame values('");
            builder.append(this.tpgId).append("','");
            builder.append(DateUtil.formatDateTime(System.currentTimeMillis()));
            builder.append("','").append(getStep()).append("')");
            try {
                DBUtil.execSql(dbAlias, builder.toString());
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                System.exit(-1);
            }

        }
        try {
            tpgStep = TPGUtil.tpgStepInsClassMap.get(getStep()).newInstance();
            if (!initFromDB) {
                tpgStep.init(this, null);
            } else {
                tpgStep.initFromDb(this);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            System.exit(-1);
        }

    }

    /**
     * @param orders 晋级名单
     *               晋级到下一阶段
     */
    public void promotion(Collection<TPGTeam> orders) {
        MyLinkedListNode<String> next = this.step.next;
        if (next == null) {
            return;
        }
        String stepStr = next.getObject();
        try {
            DBUtil.execSql(dbAlias, "update teampvpgame set step='" + stepStr + "'");
            LogUtil.info("promotion update step={},curStep={}", stepStr, step.getObject());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            LogUtil.info("init next step:" + stepStr + " failed0,current step:" + (String) this.step.next.getObject());
            return;
        }
        this.step = next;
        try {
            tpgStep = TPGUtil.tpgStepInsClassMap.get(getStep()).newInstance();
            tpgStep.init(this, orders);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            LogUtil.info("init next step:" + stepStr + " failed1");
            return;
        }

    }

    public String getTpgId() {
        return tpgId;
    }

    /**
     * 是否在开启时间内
     *
     * @return
     */
    public boolean isStepTimeActive() {
        return tpgStep.isBetweenTime();
    }

    /**
     * 是否处于某个阶段
     *
     * @param stepType
     * @return
     */
    public boolean isInStep(String stepType) {
        return stepType.equals(getStep());
    }

    /**
     * 能否报名
     *
     * @param inititor
     * @return
     */
    public boolean canSignUp(long inititor) {
        // 不在报名阶段 || 不在开启时间
        if (!isInStep(TPGUtil.TPGSTEP_SIGNUP) || !isStepTimeActive()) {
            return false;
        }
        return !tpgStep.isInTeam(inititor);
    }

    /**
     * 报名
     *
     * @param teamId
     */
    public void signUp(int teamId) {
        // 不在报名阶段 || 不在开启时间
        if (!isInStep(TPGUtil.TPGSTEP_SIGNUP) || !isStepTimeActive()) {
            return;
        }
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
        if (team == null)
            return;
        SignupTPGStep signupTPGStep = (SignupTPGStep) tpgStep;
        signupTPGStep.addTeam(team);
        // 解散队伍
        ServiceHelper.baseTeamService().disbandTeam(team.getTeamId());
    }

    /**
     * 先从当前step拿,拿不到从host缓存中拿
     *
     * @param inititor
     * @return
     */
    public TPGTeam getTeam(long inititor) {
        TPGTeam tpgTeam = tpgStep.getTeam(inititor);
        if (tpgTeam != null)
            return tpgTeam;
        if (roleId2Team.containsKey(inititor)) {
            return teamInfoMap.get(roleId2Team.get(inititor));
        }
        return null;
    }

    /**
     * 下发数据
     *
     * @param initiator
     * @param tpgType
     */
    public void sendTPGData(long initiator, byte tpgType) {
        ClientTPGData clientTPGData = new ClientTPGData(ClientTPGData.ALL_DATA);
        clientTPGData.setTpgType(tpgType);
        clientTPGData.setStep(getStep());
        clientTPGData.setStatus((byte) (isStepTimeActive() ? 1 : 0));
        // 队伍信息
        TPGTeam tpgTeam = getTeam(initiator);
        if (tpgTeam != null) {
            tpgTeam.setScoreRanking(getScoreRanking(tpgTeam.getTeamId()));
        }
        clientTPGData.setMyTeam(tpgTeam);
        clientTPGData.setGroupFaceMap(faceMap.get(TPGUtil.TPGSTEP_GROUP));
        clientTPGData.setQuarterFaceMap(faceMap.get(TPGUtil.TPGSTEP_QUARTER));
        PacketManager.send(initiator, clientTPGData);
    }

    public void reqScoreMatchResult(long initiator) {
        tpgStep.sendMatchResult(initiator);
    }

    /**
     * 进入战斗
     *
     * @param initiator
     */
    public void enterFight(long initiator) {
        // 不在阶段开启时间
        if (!isStepTimeActive()) {
            return;
        }
        tpgStep.enterFight(initiator);
    }

    public void maintenance(){
        tpgStep.maintenance();
    }

    public void doFightLuaFram(String fightSceneId, LuaFrameData luaFrameData) {
        tpgStep.doLuaFram(fightSceneId, luaFrameData);
    }

    /**
     * 请求积分排行榜
     *
     * @param initiator
     */
    public void reqScoreRank(long initiator) {
        ClientTPGScoreRank clientTPGScoreRank = new ClientTPGScoreRank(ClientTPGScoreRank.SCORE_RANK);
        clientTPGScoreRank.setScoreRank(scoreRank);
        TPGTeam tpgTeam = getTeam(initiator);
        clientTPGScoreRank.setMyRank(tpgTeam == null ? -1 : getScoreRanking(tpgTeam.getTeamId()));
        PacketManager.send(initiator, clientTPGScoreRank);
    }

    /**
     * 获得队伍的积分赛排名
     *
     * @param teamId
     * @return -1=未上榜
     */
    public int getScoreRanking(int teamId) {
        int ranking = 1;
        for (TPGTeam tpgTeam : scoreRank) {
            if (tpgTeam.getTeamId() == teamId) {
                return ranking;
            }
            ranking++;
        }
        return -1;
    }

    public void updateTPGTeamMember(BaseTeamMember teamMember) {
        tpgStep.updateTPGTeamMember(teamMember);
    }

    private void registerTPGStep(String step, Class<AbstractTPGStep> stepInsClass) {
        TPGUtil.tpgStepInsClassMap.put(step, stepInsClass);
    }

    private String loadStepFromDB() {
        try {
            _HashMap result = DBUtil.querySingleMap(dbAlias, "select * from teampvpgame where tpgid=" + tpgId);
            return result.getString("step");
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return null;
        }
    }

    public void onReceived(Object message) {

    }

    public String getStep() {
        return (String) (step.getObject());
    }

    public void saveToDb() {
        this.tpgStep.saveToDb();
    }

    public String getDbAlias() {
        return dbAlias;
    }

    public void setDbAlias(String dbAlias) {
        this.dbAlias = dbAlias;
    }

    public int getFightServerId() {
        return Integer.valueOf(ServerManager.getServer().getConfig().getProps().get("fightServer").getProperty("serverId"));
    }

    public int getServerId() {
        return ServerManager.getServer().getConfig().getServerId();
    }

    public FightBaseService getFightBaseService() {
        return MainRpcHelper.fightBaseService();
    }

    public void putFaceMap(Map<Integer, TPGGroup> groupMap) {
        faceMap.put(getStep(), groupMap);
    }

    /**
     * 更新积分赛排行榜
     *
     * @param scoreRank
     */
    public void updateScoreRank(List<TPGTeam> scoreRank) {
        this.scoreRank = scoreRank;
    }

    public void updateTeamInfo(Map<Integer, TPGTeam> teamMap) {
        if (StringUtil.isEmpty(teamMap)) {
            return;
        }
        teamInfoMap.putAll(teamMap);
        for (TPGTeam tpgTeam : teamMap.values()) {
            for (long roleId : tpgTeam.getMembers().keySet()) {
                roleId2Team.put(roleId, tpgTeam.getTeamId());
            }
        }
    }

    public byte getTpgType() {
        return tpgType;
    }

    /**
     * 组队pvp一次活动Id
     *
     * @return
     */
    private int newTPGId() {
        return ++tpgIdCreator;
    }
}
