package com.stars.services.fightingmaster;

import com.google.common.base.Preconditions;
import com.stars.bootstrap.ServerManager;
import com.stars.core.dao.DbRowDao;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.fightingmaster.FightingMasterManager;
import com.stars.modules.fightingmaster.event.FiveRewardStatusEvent;
import com.stars.modules.fightingmaster.event.GetFiveRewardEvent;
import com.stars.modules.fightingmaster.packet.ClientFightingMaster;
import com.stars.modules.fightingmaster.packet.ServerFightReady;
import com.stars.modules.fightingmaster.packet.ServerFightingMaseter;
import com.stars.modules.fightingmaster.prodata.PersonPKcoeVo;
import com.stars.modules.fightingmaster.prodata.PersonPaircoeVo;
import com.stars.modules.induct.event.InductEvent;
import com.stars.modules.induct.packet.ServerInduct;
import com.stars.modules.offlinepvp.OfflinePvpManager;
import com.stars.modules.offlinepvp.prodata.OPRobotVo;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.ServerEnterCity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.skyrank.packet.ServerSkyRankReq;
import com.stars.modules.skyrank.prodata.SkyRankScoreVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fightingmaster.*;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.connector.packet.FrontendClosedN2mPacket;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightingmaster.data.Fighting;
import com.stars.services.fightingmaster.data.RoleFightingMaster;
import com.stars.services.fightingmaster.event.BackMainServerEvent;
import com.stars.services.fightingmaster.event.EnterFightingMasterEvent;
import com.stars.services.fightingmaster.event.NoticeMainServerAddTool;
import com.stars.startup.FightingMasterStartup;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;
import io.netty.buffer.Unpooled;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.stars.services.SConst.fightingMasterService;

/**
 * Created by zhouyaohui on 2016/11/1.
 */
public class FightingMasterServiceActor extends ServiceActor implements FightingMasterService {

    public final static String prefix = "fightingmaster.";
    private int matchFightId = 0;
    private LinkedList<Fighter> personMatch = new LinkedList<>(); // 真人匹配排队,先到优先匹配
    private Matcher<Robot> robots = new Matcher<>(); // 机器人匹配器
    private Map<Long, Fighter> fighterMap = new HashMap<>();
    private Map<String, Fighting> fightingMap = new HashMap<>();
    private DbRowDao dao = new DbRowDao();
    private Map<Long, RoleFightingMaster> roleDataMap = new HashMap<>();
    private Map<Integer, Map<Integer, RoleFightingMaster>> rank = new HashMap<>();  // 排行榜,先用map存下来
    private ConcurrentMap<String, Long> timestampMap = new ConcurrentHashMap<>(); //上次战斗时间戳记录

    private Map<Integer, Map<Long, String>> rankAward = new HashMap<>();    // 排行榜发奖名单,按服务存放，方便回调确认和重发
    private int lastResend; // 最后一次重新发奖的时间

    public static int MaxFightingVal = 0;//当前所有服的最高战力，由游戏服那边主动刷新过来

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(fightingMasterService, this);
        loadRobot();
        loadUserData();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FightingMaster, new FightingMasterSchedule(), 5, 1, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},personMatch:{},rank:{},rankAward:{}", this.getClass().getSimpleName(), personMatch.size(),
                rank.size(), rankAward.size());
    }

    /**
     * 发奖
     */
    @Override
    public void rankAward() {
        Map<Integer, Map<Long, String>> award = new HashMap<>();
        int ymdh = getYMDH();
        for (Map<Integer, RoleFightingMaster> subRank : rank.values()) {
            for (int i = 1; i <= FightingMasterManager.rankSize; i++) {
                if (subRank.get(i) == null) {
                    break;
                }
                RoleFightingMaster rfm = subRank.get(i);
                Map<Long, String> oneServer = award.get(rfm.getServerId());
                if (oneServer == null) {
                    oneServer = new HashMap<>();
                    award.put(rfm.getServerId(), oneServer);
                }
                PersonPKcoeVo pkVo = FightingMasterManager.matchVo(rfm.getFightScore(), rfm.getDisScore());
                oneServer.put(rfm.getRoleId(), pkVo.getRankid() + "+" + i);
                rfm.setLastRankAwardYMD(ymdh);
                dao.update(rfm);
            }
            // 将排行榜中的上榜的候选名单移除，防止排行榜变更奖励错乱
            int size = subRank.size();
            for (int i = FightingMasterManager.rankSize + 1; i <= size; i++) {
                if (subRank.get(i) == null) {
                    break;
                }
                RoleFightingMaster rfm = subRank.get(i);
                subRank.remove(i);
                rfm.setRank(0);
                dao.update(rfm);
            }
        }

        rankAward = award;

        for (Map.Entry<Integer, Map<Long, String>> oneServer : award.entrySet()) {
            try {
                // 防止某个服务器rpc连不上，中断其他服务器的发奖
                FightingMasterRPC.localService().sendFightingMasterAward(oneServer.getKey(), oneServer.getValue());
            } catch (Exception e) {
                LogUtil.error("send award to server {} failed.", oneServer.getKey());
            }
        }

        // 重置最高积分段积分
        Map<Integer, RoleFightingMaster> maxRank = rank.get(FightingMasterManager.maxDisSegmentRankId);
        if (maxRank != null) {
            Map<Integer, RoleFightingMaster> newRank = new HashMap<>();
            for (RoleFightingMaster rfm : maxRank.values()) {
                rfm.setDisScore(FightingMasterManager.maxDisScore);
                rfm.setRank(0);
                updateRank(newRank, rfm);
            }
            rank.put(FightingMasterManager.maxDisSegmentRankId, newRank);
        }
    }

    /**
     * 玩家登陆，检查排行榜奖励，重置积分
     */
    @Override
    public void checkRankAward(int serverId, long roleId, int fromServer) {
        RoleFightingMaster rfm = roleDataMap.get(roleId);
        if (rfm == null) {
            rfm = loadRoleFightingMaster(roleId);
            if (rfm == null) {
                return;
            }
            roleDataMap.put(roleId, rfm);
        }
        rfm.setServerId(fromServer);
        int lastRankAward = rfm.getLastRankAwardYMD();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        int ymd = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(calendar.getTime())) * 100 + FightingMasterManager.rankReset;
        calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        int lastYmd = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(calendar.getTime())) * 100 + FightingMasterManager.rankReset;
        boolean needAward = false;
        if (lastRankAward < lastYmd) {
            // 上周都没有领，先领取
            needAward = true;
        } else {
            // 上周或者这周已经领取
            int today = getYMDH();
            if (today >= ymd && lastRankAward < ymd) {
                // 大于重置时间，并且没有领取这周的奖励
                needAward = true;
            }
        }
        // 发奖并重置积分
        if (needAward) {
            PersonPKcoeVo pkVo = FightingMasterManager.matchVo(rfm.getFightScore(), rfm.getDisScore());
            Map<Long, String> award = new HashMap<>();
            award.put(rfm.getRoleId(), pkVo.getRankid() + "+" + 101);   // 上线发的奖励全部是不上榜的奖励
            FightingMasterRPC.localService().sendFightingMasterAward(rfm.getServerId(), award);
//            FightingMasterRPC.localService().sendFightingMasterAward(fromServer, award);
            rfm.setLastRankAwardYMD(getYMDH());
            if (rfm.getDisScore() > FightingMasterManager.maxDisScore) {
                rfm.setDisScore(FightingMasterManager.maxDisScore);
            }
            dao.update(rfm);
        }

        checkFightingTime(rfm, fromServer);

        //发送五战奖励领取状态给主服
        byte fiveFightRewardStatus = FiveRewardStatusEvent.CAN_NOT_GET;
        if (rfm.getFightTimes() >= 5) {
            fiveFightRewardStatus = FiveRewardStatusEvent.CAN_GET;
        }
        if (rfm.getFiveAward() == 1) {//已领取
            fiveFightRewardStatus = FiveRewardStatusEvent.HAVE_GOT;
        }
        FightingMasterRPC.roleService().notice(fromServer, roleId, new FiveRewardStatusEvent(fiveFightRewardStatus));
    }

    /**
     * 排行榜发奖回调
     *
     * @param fightingMasterServer
     * @param serverId
     * @param awardSuccess
     */
    @Override
    public void rankAwardCallback(int fightingMasterServer, int serverId, Set<Long> awardSuccess) {
        Map<Long, String> award = rankAward.get(serverId);
        if (award != null) {
            for (Long roleId : awardSuccess) {
                award.remove(roleId);
            }
            if (award.size() == 0) {
                rankAward.remove(serverId);
            }
        }
    }

    /**
     * 加载用户数据
     *
     * @param roleId
     * @return
     */
    private RoleFightingMaster loadRoleFightingMaster(long roleId) {
        RoleFightingMaster roleFightingMaster = null;
        try {
            roleFightingMaster = DBUtil.queryBean(DBUtil.DB_USER, RoleFightingMaster.class, "select * from rolefightingmaster where roleid = " + roleId);
        } catch (SQLException e) {
            LogUtil.error("", e);
        }
        return roleFightingMaster;
    }

    public void save() {
        dao.flush();
    }

    /**
     * 排行榜奖励发送失败后重新发送
     */
    @Override
    public void reSendAward() {
        int now = DateUtil.getSecondTime();
        if (now - lastResend > 60 * 30) {
            // 半小时重发一次
            lastResend = now;
            for (Map.Entry<Integer, Map<Long, String>> oneServer : rankAward.entrySet()) {
                try {
                    // 防止某个服务器rpc连不上，中断其他服务器的发奖
                    FightingMasterRPC.localService().sendFightingMasterAward(oneServer.getKey(), oneServer.getValue());
                } catch (Exception e) {
                    LogUtil.error("send award to server {} failed.", oneServer.getKey());
                }
            }
        }
    }

    /**
     * 加载用户数据，初始化排行榜
     */
    private void loadUserData() throws SQLException {
        roleDataMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", RoleFightingMaster.class, "select * from rolefightingmaster where rank > 0");
        for (RoleFightingMaster value : roleDataMap.values()) {
            PersonPKcoeVo pkVo = FightingMasterManager.matchVo(value.getFightScore(), value.getDisScore());
            Map<Integer, RoleFightingMaster> subRank = rank.get(pkVo.getRankid());
            if (subRank == null) {
                rank.put(pkVo.getRankid(), new HashMap<Integer, RoleFightingMaster>());
                subRank = rank.get(pkVo.getRankid());
            }
            updateRank(subRank, value);
        }
    }

    /**
     * 加载机器人
     */
    private void loadRobot() throws Exception {
        for (OPRobotVo vo : OfflinePvpManager.robotVoMap.values()) {
            Map<String, FighterEntity> entities = FighterCreator.createRobot(FighterEntity.CAMP_ENEMY, vo);
            Robot robot = new Robot();
            for (FighterEntity entity : entities.values()) {
                if (entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                    robot.setCharactor(entity);
                    entity.setFighterType(FighterEntity.TYPE_ROBOT);
                    robots.add(robot);
                } else {
                    robot.addOtherEntity(entity);
                }
            }
        }
        robots.check();
    }

    @Override
    public void dispatch(Packet packet) {
        try {
            // 特殊处理天梯请求
            if (packet instanceof ServerSkyRankReq) {
                long roleId = packet.getRoleId();
                RoleFightingMaster po = roleDataMap.get(roleId);
                if (po != null) {
                    ((ServerSkyRankReq) packet).setPlayer(null);
                    packet.setSession(null);
                    FightingMasterRPC.roleService().exec(po.getServerId(), po.getRoleId(), packet);
                }
                return;
            }
            if (packet instanceof ServerFightingMaseter) {
                ServerFightingMaseter sm = (ServerFightingMaseter) packet;
                Fighter fighter = fighterMap.get(sm.getRoleId());
                if (SpecialAccountManager.isSpecialAccount(sm.getRoleId())) {
                    ServiceHelper.roleService().notice(sm.getRoleId(), new SpecialAccountEvent(sm.getRoleId(), "斗神殿相关ServerFightingMaseter", true));
                    return;
                }
                if (sm.getReqType() == ServerFightingMaseter.MATCH) {
                    match(fighter);
                }
                if (sm.getReqType() == ServerFightingMaseter.ENTERFIGHT) {
                    noticeEntryFightScene(sm.getFightId(), sm.getRoleId());
                }
                if (sm.getReqType() == ServerFightingMaseter.BACKCITY) {
                    MultiServerHelper.modifyConnectorRoute(packet.getRoleId(), RoleId2ServerIdManager.get(packet.getRoleId()));
                }
                if (sm.getReqType() == ServerFightingMaseter.RANK) {
                    getRank(sm.getRoleId(), sm.getRankId());
                }
                if (sm.getReqType() == ServerFightingMaseter.FIVEAWARD) {
                    getFiveAward(fighter);
                }
                if (sm.getReqType() == ServerFightingMaseter.RETRY_MATCH) {
                    matchPerson(fighter);
                }
                if (sm.getReqType() == ServerFightingMaseter.FORCE_MATCH) {
                    personMatch.remove(fighter);
                    fighter.setPersonMatchBegin(0);
                    fighter.setLastpersonMatch(0);
                    matchRobot(fighter);
                }
                if (sm.getReqType() == ServerFightingMaseter.CANCEL_MATCH) {
                    personMatch.remove(fighter);
                    fighter.setPersonMatchBegin(0);
                    fighter.setLastpersonMatch(0);
                }
                return;
            }
            if (packet instanceof ServerFightReady) {
                ServerFightReady ready = (ServerFightReady) packet;
                if (SpecialAccountManager.isSpecialAccount(ready.getRoleId())) {
                    ServiceHelper.roleService().notice(ready.getRoleId(), new SpecialAccountEvent(ready.getRoleId(), "斗神殿相关ServerFightReady", true));
                    return;
                }
                Fighter fighter = fighterMap.get(ready.getRoleId());
                Fighting fighting = fightingMap.get(fighter.getFightId());
                if (fighting == null) { // 另一方掉线或者什么其他原因导致战斗被清理掉了，就不切换链接了，直接通知客户端战斗准备失败了
                    ClientFightingMaster failed = new ClientFightingMaster();
                    failed.setResType(ClientFightingMaster.BATTLE_FAILED);
                } else {
                    // 切客户端连接
                    MultiServerHelper.modifyConnectorRoute(fighter.getRoleIdLong(), fighting.getFightServer());
                    fighting.ready();
                }
                return;
            }
            if (packet instanceof FrontendClosedN2mPacket) {
                // 客户端掉线
                Fighter fighter = fighterMap.get(packet.getRoleId());
                personMatch.remove(fighter);    // 移除匹配
                Fighting fighting = fightingMap.get(fighter.getFightId());
                if (fighting != null && fighting.isReady() == false) {
                    // 已经创建战斗但是还没有进入ready，已经进入ready的掉线由handleoffline处理
                    handleResult(fighting.getOther(fighter.getRoleId()), fighter);
                    destoryFighting(fighting.getFightId());
                }
                return;
            }

            if (packet instanceof ServerEnterCity) {
                /** 回城 */
                Fighter fighter = fighterMap.get(packet.getRoleId());
                personMatch.remove(fighter);
                MultiServerHelper.modifyConnectorRoute(packet.getRoleId(), packet.getSession().getServerId());
                FightingMasterRPC.roleService().notice(packet.getSession().getServerId(), packet.getRoleId(), new BackMainServerEvent());
                return;
            }

            if (packet instanceof ServerInduct) {//新手引导完成包
                ServerInduct si = (ServerInduct) packet;
                byte eventType = si.getReqType();
                int inductId = si.getInductId();
                FightingMasterRPC.roleService().notice(packet.getSession().getServerId(), packet.getRoleId(), new InductEvent(eventType, inductId));
            }

            LogUtil.error("unhandle packet: {}", packet.getType());
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    /**
     * 获取五战奖励
     *
     * @param fighter
     */
    private void getFiveAward(Fighter fighter) throws Exception {
        checkFightingTime(fighter.getRoleFightingMaster(), RoleId2ServerIdManager.get(Long.valueOf(fighter.getRoleId())));
        if (fighter.getRoleFightingMaster().getFightTimes() < 5) {
            PacketManager.send(Long.valueOf(fighter.getRoleId()), new ClientText("战斗次数为达成"));
        } else {
            if (fighter.getRoleFightingMaster().getFiveAward() == 0) {
                PersonPKcoeVo pkVo = FightingMasterManager.matchVo(fighter.getCharactor().getFightScore(), fighter.getRoleFightingMaster().getDisScore());
//                ClientFightingMaster res = new ClientFightingMaster();
//                res.setResType(ClientFightingMaster.VIEW_FIVEAWARD);
//                PacketManager.send(Long.valueOf(fighter.getRoleId()), res);
                //Map<Integer, Integer> itemMap = StringUtil.toMap(pkVo.getFiveaward(), Integer.class, Integer.class, '+', '|');
                //NoticeMainServerAddTool tool = new NoticeMainServerAddTool(itemMap, (byte) 1);
                //FightingMasterRPC.roleService().notice(RoleId2ServerIdManager.get(Long.valueOf(fighter.getRoleId())), Long.valueOf(fighter.getRoleId()), tool);
                GetFiveRewardEvent getFiveRewardEvent = new GetFiveRewardEvent(pkVo.getFiveawardGroupId());
                FightingMasterRPC.roleService().notice(RoleId2ServerIdManager.get(Long.valueOf(fighter.getRoleId())), Long.valueOf(fighter.getRoleId()), getFiveRewardEvent);
                fighter.getRoleFightingMaster().setFiveAward((byte) 1);

                //发送五战奖励领取状态给主服                 
                FightingMasterRPC.roleService().notice(RoleId2ServerIdManager.get(Long.valueOf(fighter.getRoleId())), Long.valueOf(fighter.getRoleId()), new FiveRewardStatusEvent(FiveRewardStatusEvent.HAVE_GOT));
            } else {
                PacketManager.send(Long.valueOf(fighter.getRoleId()), new ClientText("已经领取过"));
            }
        }
    }

    /**
     * 获取排行榜
     *
     * @param roleId
     * @param rankId
     */
    private void getRank(long roleId, int rankId) {
        Map<Integer, RoleFightingMaster> subRank = rank.get(rankId);
        List<RoleFightingMaster> temp = new ArrayList<>();
        for (int i = 1; i <= FightingMasterManager.rankSize; i++) {
            if (subRank == null || subRank.get(i) == null) {
                break;
            }
            temp.add(subRank.get(i));
        }
        ClientFightingMaster res = new ClientFightingMaster();
        res.setRankId(rankId);
        res.setResType(ClientFightingMaster.VIEW_RANK);
        res.setSubRank(temp);
        PacketManager.send(roleId, res);
    }

    /**
     * 添加角色到战斗中回调
     *
     * @param serverId
     * @param fightId
     * @param result
     * @param fighters
     */
    @Override
    public void addNewFighterCallback(int serverId, String fightId, boolean result, Set<Long> fighters) {
        if (result) {
            // 战斗服添加战斗实体成功，通知客户端进入匹配流程
            Fighting fighting = fightingMap.get(fightId);
            for (Fighter fighter : fighting.getFighterSet()) {
                ClientFightingMaster match = new ClientFightingMaster();
                match.setResType(ClientFightingMaster.VIEW_MATCH);
                match.setFightId(fighting.getFightId());
                match.setFighter(fighting.getOther(fighter.getRoleId()));
                if (!(fighter instanceof Robot)) {
                    //不是机器人，显示匹配画面
                    PacketManager.send(Long.valueOf(fighter.getRoleId()), match);
                }
            }
        } else {
            Fighting fighting = fightingMap.get(fightId);
            for (Fighter fighter : fighting.getFighterSet()) {
                if (!(fighter instanceof Robot)) {
                    PacketManager.send(fighter.getRoleIdLong(), new ClientText("匹配失败"));
                }
            }
            destoryFighting(fightId);
        }
    }

    /**
     * 处理战斗超时
     *
     * @param fromServerId
     * @param fightId
     * @param hpInfo
     */
    @Override
    public void handleTimeOut(int fromServerId, String fightId, HashMap<String, String> hpInfo) {
        Fighting fighting = fightingMap.get(fightId);
        if (fighting == null) {
            return;
        }

        for (Fighter fighter : fighting.getFighterSet()) {
            Fighter other = fighting.getOther(fighter.getRoleId());
            String[] otherInfo = hpInfo.get(other.getRoleId()).split("[+]");
            String[] myInfo = hpInfo.get(fighter.getRoleId()).split("[+]");
            if (otherInfo == null) {
                handleResult(fighter, other);
                destoryFighting(fightId);
                return;
            }
            if (myInfo == null) {
                handleResult(other, fighter);
                destoryFighting(fightId);
                return;
            }
            if (Float.valueOf(otherInfo[1]) / Float.valueOf(otherInfo[0]) < Float.valueOf(myInfo[1]) / Float.valueOf(myInfo[0])) {
                handleResult(fighter, other);
                destoryFighting(fightId);
                return;
            } else {
                handleResult(other, fighter);
                destoryFighting(fightId);
                return;
            }
        }
    }

    /**
     * 掉线处理
     *
     * @param fromServerId
     * @param fightId
     * @param roleId
     */
    @Override
    public void handleOffline(int fromServerId, String fightId, long roleId) {
        Fighting fighting = fightingMap.get(fightId);
        if (fighting == null) {
            return;
        }
        Fighter victor = null;
        Fighter loser = null;
        for (Fighter value : fighting.getFighterSet()) {
            if (value.getRoleId().equals(String.valueOf(roleId))) {
                loser = value;
            } else {
                victor = value;
            }
        }
        handleResult(victor, loser);
        destoryFighting(fightId);
    }

    /**
     * 进入斗神殿
     */
    @Override
    public void enterFightingMaster(int toServer, int fromServer, FighterEntity entry, FighterEntity buddy, int validMedalId, String familyName) {
        try {
            if (entry.getLevel() < DataManager.getCommConfig("personpk_enterlv", 20)) {
                FightingMasterRPC.roleService().send(fromServer, Long.valueOf(entry.getUniqueId()), new ClientText("等级不够"));
                return;
            }
            Fighter fighter = fighterMap.get(Long.valueOf(entry.getUniqueId()));
            if (fighter == null) {
                fighter = new Fighter();
                fighterMap.put(Long.valueOf(entry.getUniqueId()), fighter);
            }
            fighter.setServerId(fromServer);
            fighter.setCharactor(entry);
            fighter.setFamilyName(familyName);
            fighter.getOtherEntities().clear();
            if (buddy != null) {
                fighter.addOtherEntity(buddy);
            }
            entry.setFighterType(FighterEntity.TYPE_PLAYER);
            RoleFightingMaster roleFightingMaster = roleDataMap.get(Long.valueOf(entry.getUniqueId()));
            if (roleFightingMaster == null) {
                roleFightingMaster = DBUtil.queryBean(DBUtil.DB_USER, RoleFightingMaster.class, "select * from rolefightingmaster where roleid = " + entry.getUniqueId());
            }
            if (roleFightingMaster == null) {
                // 第一次进入斗神殿
                roleFightingMaster = new RoleFightingMaster();
                roleFightingMaster.setRoleId(Long.valueOf(fighter.getRoleId()));
                roleDataMap.put(roleFightingMaster.getRoleId(), roleFightingMaster);
                /** 初始积分 */
                int factor = DataManager.getCommConfig("personpk_defensescore", 1);
                roleFightingMaster.setDisScore((int) Math.floor(fighter.getCharactor().getFightScore() / factor));

                // 初始化匹配机器人的概率
                PersonPKcoeVo vo = FightingMasterManager.matchVo(fighter.getCharactor().getFightScore(), roleFightingMaster.getDisScore());
                String[] odds = vo.getMatchpairodds().split("[+]");
                roleFightingMaster.setMatchRobotPersent(Integer.valueOf(odds[0]));

                roleFightingMaster.setLastRankAwardYMD(getYMDH());  // 初始化排行榜奖励

                dao.insert(roleFightingMaster);
            }
            roleFightingMaster.setName(fighter.getCharactor().getName());
            roleFightingMaster.setServerId(fighter.getServerId());
            roleFightingMaster.setFightScore(fighter.getCharactor().getFightScore());
            roleFightingMaster.setLevel(fighter.getCharactor().getLevel());
            roleFightingMaster.setServerName(fighter.getCharactor().getServerName());
            roleFightingMaster.setValidMedalId(validMedalId);
            dao.update(roleFightingMaster);
            fighter.setRoleFightingMaster(roleFightingMaster);

            // 处理排行榜,刚开始没有用户数据的时候，在玩家进入时建立排行榜
            PersonPKcoeVo pkVo = FightingMasterManager.matchVo(fighter.getCharactor().getFightScore(), roleFightingMaster.getDisScore());
            Map<Integer, RoleFightingMaster> subRank = rank.get(pkVo.getRankid());
            if (subRank == null) {
                rank.put(pkVo.getRankid(), new HashMap<Integer, RoleFightingMaster>());
                subRank = rank.get(pkVo.getRankid());
            }
            updateRank(subRank, roleFightingMaster);

            // 关联 roleId 与 主服id
            RoleId2ServerIdManager.put(Long.valueOf(fighter.getRoleId()), fromServer);

            // 检查一下每日战斗次数重置
            checkFightingTime(fighter.getRoleFightingMaster(), fromServer);

            ClientFightingMaster view = new ClientFightingMaster();
            view.setResType(ClientFightingMaster.VIEW_MAIN);
            view.setDisScore(fighter.getRoleFightingMaster().getDisScore());
            view.setFightTimes(fighter.getRoleFightingMaster().getFightTimes());
            view.setFiveReward(fighter.getRoleFightingMaster().getFiveAward());
            view.setRank(fighter.getRoleFightingMaster().getRank());

            // 跨榜处理
            if (roleFightingMaster.isSequenceWin()) {
                // 赢才会有跨榜
                if (!roleFightingMaster.alreadyRankUp(pkVo.getRankid()) &&
                        !pkVo.getRankupaward().equals("") && !pkVo.getRankupaward().equals("0")) {
                    Map<Integer, Integer> toolMap = StringUtil.toMap(pkVo.getRankupaward(), Integer.class, Integer.class, '+', '|');
                    view.setAward(toolMap);
                    NoticeMainServerAddTool event = new NoticeMainServerAddTool(toolMap, (byte) 1);
                    FightingMasterRPC.roleService().notice(roleFightingMaster.getServerId(), roleFightingMaster.getRoleId(), event);
                }
            }

            FightingMasterRPC.roleService().notice(fromServer, Long.valueOf(fighter.getRoleId()), new EnterFightingMasterEvent(true, view));
        } catch (Exception e) {
            LogUtil.error("enter fighting master failed.", e);
            fighterMap.remove(Long.valueOf(entry.getUniqueId()));
            RoleId2ServerIdManager.remove(Long.valueOf(entry.getUniqueId()));
            FightingMasterRPC.roleService().notice(fromServer, Long.valueOf(entry.getUniqueId()), new EnterFightingMasterEvent(false, null));
        }
    }

    @Override
    public void getFightCount(int serverId, int fromServerId, long roleId) {
        Fighter fighter = fighterMap.get(roleId);
        LogUtil.info("请求战斗次数 roleId:{},fighter:{}", roleId, fighter);
        ClientFightingMaster fightCount = new ClientFightingMaster();
        fightCount.setResType(ClientFightingMaster.FIGHT_TIMES);
        fightCount.setFightTimes(fighter == null ? 0 : fighter.getRoleFightingMaster().getFightTimes());
        FightingMasterRPC.roleService().send(fromServerId, roleId, fightCount);
    }

    /**
     * 交换排名
     *
     * @param subRank
     * @param pre
     * @param next
     */
    private void swap(Map<Integer, RoleFightingMaster> subRank, int pre, int next) {
        RoleFightingMaster temp = subRank.get(pre);
        subRank.put(pre, subRank.get(next));
        subRank.put(next, temp);
        subRank.get(pre).setRank(pre);
        subRank.get(next).setRank(next);
    }

    /**
     * 比较规则
     *
     * @param c1
     * @param c2
     * @return
     */
    private int compare(RoleFightingMaster c1, RoleFightingMaster c2) {
        if (c1.getDisScore() < c2.getDisScore() ||
                (c1.getDisScore() == c2.getDisScore() && c1.getFightScore() < c2.getFightScore())) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 更新排行榜
     *
     * @param subRank
     * @param roleFightingMaster
     */
    private void updateRank(Map<Integer, RoleFightingMaster> subRank, RoleFightingMaster roleFightingMaster) {
        if (subRank.get(roleFightingMaster.getRank()) == null || subRank.get(roleFightingMaster.getRank()) != roleFightingMaster) {
            // 不在排行榜内，先加入到排行榜末尾
            subRank.put(subRank.size() + 1, roleFightingMaster);    // 排行榜排名从1开始
            roleFightingMaster.setRank(subRank.size());
            for (int i = subRank.size(); i > 1; i--) {
                if (compare(subRank.get(i - 1), subRank.get(i)) > 0) {
                    // 前一名大于后一名,排名有序
                    break;
                } else {
                    // 前一名小于后一名，交换排名
                    swap(subRank, i - 1, i);
                }
            }
            // 每一个子榜只保留200名，排行榜只显示100名，保留200名是为了处理跨榜后补上该子榜的排行榜空缺
            for (int i = FightingMasterManager.rankSize * 2 + 1; i <= subRank.size(); i++) {
                RoleFightingMaster rfm = subRank.get(i);
                rfm.setRank(0); // 不上榜
                subRank.remove(i);  // 移除200名开外的排名
            }
        } else {
            // 在排行榜内，积分变化，重新排名
            if (subRank.get(roleFightingMaster.getRank() - 1) != null &&
                    compare(roleFightingMaster, subRank.get(roleFightingMaster.getRank() - 1)) > 0) {
                // 有前一名，并且比前一名排名要高，判断排名上升
                for (int i = roleFightingMaster.getRank(); i > 1; i--) {
                    if (compare(subRank.get(i - 1), subRank.get(i)) > 0) {
                        break;
                    } else {
                        swap(subRank, i - 1, i);
                    }
                }
            } else if (subRank.get(roleFightingMaster.getRank() + 1) != null &&
                    compare(roleFightingMaster, subRank.get(roleFightingMaster.getRank() + 1)) < 0) {
                // 有后一名，并且比后一名排名要地，判断为排名下降
                for (int i = roleFightingMaster.getRank(); i < subRank.size(); i++) {
                    if (compare(subRank.get(i), subRank.get(i + 1)) > 0) {
                        break;
                    } else {
                        swap(subRank, i, i + 1);
                    }
                }
            }
        }

        // 排行榜更新保存一次
        for (RoleFightingMaster rfm : subRank.values()) {
            dao.update(rfm);
        }
    }

    /**
     * 最大可以表示 2147483647 -> 2147-12-31 23小时
     *
     * @return
     */
    private int getYMDH() {
        return Integer.valueOf(new SimpleDateFormat("yyyyMMddHH").format(new Date()));
    }

    /**
     * 检查战斗次数
     *
     * @param roleFightingMaster
     */
    private void checkFightingTime(RoleFightingMaster roleFightingMaster, int fromServer) {
        int now = getYMDH();
        int ymd = Integer.valueOf(DateUtil.getYMD_Str());
        int reset = ymd * 100 + FightingMasterManager.dailyReset; // 每天6点重置
        if (now >= reset && roleFightingMaster.getYmdH() < reset) {
            roleFightingMaster.setYmdH(now);
            roleFightingMaster.setFightTimes((short) 0);
            roleFightingMaster.setFiveAward((byte) 0);
            dao.update(roleFightingMaster);

            //发送五战奖励领取状态给主服
            FightingMasterRPC.roleService().notice(fromServer, Long.valueOf(roleFightingMaster.getRoleId()), new FiveRewardStatusEvent(FiveRewardStatusEvent.CAN_NOT_GET));
        }
    }

    /**
     * 匹配战斗
     *
     * @param fighter
     */
    private void match(Fighter fighter) {
        RoleFightingMaster fightingMaster = fighter.getRoleFightingMaster();

        if (fightingMaster.getFightCount() < FightingMasterManager.pairmatchcount) {
            // 前n次必须匹配机器人
            matchRobot(fighter, createRobotForMatch(fighter.getCharactor().getFightScore() / 2), 0, 500);
            return;
        }
        PersonPaircoeVo pairVo = FightingMasterManager.matchPairVo(fighter.getRoleFightingMaster().getDisScore());
        int matchScore = fighter.getMatchScore(pairVo.getFightFactor(), pairVo.getSeqFactor());
        int randInt = new Random().nextInt(100) + 1;
        PersonPKcoeVo pkVo = FightingMasterManager.matchVo(fighter.getRoleFightingMaster().getFightScore(), fighter.getRoleFightingMaster().getDisScore());
        LogUtil.info("match robot percent {} | randInt {} | match score {}", fightingMaster.getMatchRobotPersent(), randInt, matchScore);
        if (randInt < fightingMaster.getMatchRobotPersent()) {
            matchRobot(fighter);
        } else {
            int curSecond = DateUtil.getSecondTime();
            fighter.setPersonMatchBegin(curSecond);
            fighter.setLastpersonMatch(curSecond);
            personMatch.offer(fighter);
            matchPerson(fighter);
        }
    }

    /**
     * 匹配机器人
     *
     * @param fighter
     */
    private void matchRobot(Fighter fighter) {
        PersonPKcoeVo pkVo = FightingMasterManager.matchVo(fighter.getRoleFightingMaster().getFightScore(), fighter.getRoleFightingMaster().getDisScore());
        String paires = DataManager.getCommConfig("personpk_pairscorecoe", "1+0|2+5|3+10|4+20");
        Map<Integer, Integer> pairMap = new HashMap<>();
        try {
            pairMap = StringUtil.toMap(paires, Integer.class, Integer.class, '+', '|');
        } catch (Exception e) {
            LogUtil.error("", e);
        }
        int add = 0;
        int sequentCount = fighter.getRoleFightingMaster().sequenceCount();
        if (pairMap.containsKey(sequentCount)) {
            add = pairMap.get(sequentCount);
        } else {
            for (int i = sequentCount; i > 0; i--) {
                if (pairMap.containsKey(i)) {
                    add = pairMap.get(i);
                    break;
                }
            }
        }
        if (!fighter.getRoleFightingMaster().isSequenceWin()) {
            add *= -1;
        }
        int maxPower = pkVo.getMaxMatchPower();
        int minPower = pkVo.getMinMatchPower();
        LogUtil.info("MaxFightingVal=" + MaxFightingVal);
        if (MaxFightingVal > 0 && fighter.getCharactor().getFightScore() >= MaxFightingVal) {
            if (pkVo.getMaxHighMatchPower() > 0 && pkVo.getMinHighMatchPower() > 0) {
                maxPower = pkVo.getMaxHighMatchPower();
                minPower = pkVo.getMinHighMatchPower();
            }
        }
        int base = (int) (fighter.getRoleFightingMaster().getFightScore() * ((minPower + add) / 100.0f));
        int max = (int) (fighter.getRoleFightingMaster().getFightScore() * ((maxPower + add) / 100.0f));
        LogUtil.info("match robot min {}    |   max {}  | add {}    |   minpower {} |   maxpower {}", base, max, add, minPower, maxPower);
        matchRobot(fighter, createRobotForMatch(base), 0, max - base);
    }

    private Robot createRobotForMatch(int fightScore) {
        Robot robot = new Robot();
        FighterEntity characotr = new FighterEntity();
        characotr.setFightScore(fightScore);
        robot.setCharactor(characotr);
        return robot;
    }

    /**
     * 匹配真人
     */
    private void matchPerson(Fighter fighter) {
        if (fighter.getPersonMatchBegin() == 0) {
            // 已经匹配，或者并没有获得匹配真人的机会（外挂）
            return;
        }
        if (fighter.getLastpersonMatch() != fighter.getPersonMatchBegin() &&
                DateUtil.getSecondTime() - fighter.getLastpersonMatch() < 3) {
            // 每三秒只能匹配一次
            return;
        }
        // 日志
        for (Fighter f : personMatch) {
            LogUtil.info("roldId {}   |  score {}", f.getRoleId(), f.getRoleFightingMaster().getDisScore());
        }
        fighter.setLastpersonMatch(DateUtil.getSecondTime());
        RoleFightingMaster rfm = fighter.getRoleFightingMaster();
        PersonPKcoeVo pkVo = FightingMasterManager.matchVo(rfm.getFightScore(), rfm.getDisScore());
        PersonPaircoeVo myPair = FightingMasterManager.matchPairVo(rfm.getDisScore());
        int myMatchScore = fighter.getMatchScore(myPair.getFightFactor(), myPair.getSeqFactor());
        Iterator<Fighter> iter = personMatch.iterator();
        while (iter.hasNext()) {
            Fighter match = iter.next();
            if (match == fighter) {
                continue;
            }
            PersonPaircoeVo otherPair = FightingMasterManager.matchPairVo(match.getRoleFightingMaster().getDisScore());
            int otherMatchScore = match.getMatchScore(otherPair.getFightFactor(), otherPair.getSeqFactor());
            int myLevel = fighter.getRoleFightingMaster().getLevel();
            int otherLevel = match.getRoleFightingMaster().getLevel();
            LogUtil.info("cry match: mine match score{} | other {} match score {} | match level {}",
                    myMatchScore, match.getRoleId(), otherMatchScore, otherLevel);
            if ((Math.abs(myMatchScore - otherMatchScore) * 1.0) / Math.max(myMatchScore, otherMatchScore) < pkVo.getScoresub() / 100.0 &&
                    (Math.abs(myLevel - otherLevel) * 1.0) / Math.max(myLevel, otherLevel) < pkVo.getLevelsub() / 100.0) {
                // 匹配成功
                LogUtil.info("match success: mine match score{} | other {} match score {} | match level {}",
                        myMatchScore, match.getRoleId(), otherMatchScore, otherLevel);
                iter.remove();
                personMatch.remove(fighter);
                fighter.setPersonMatchBegin(0);
                fighter.setLastpersonMatch(0);
                match.setPersonMatchBegin(0);
                match.setLastpersonMatch(0);
                fight(fighter, match);
                return;
            }
        }
        // 告诉客户端需要再次发包
        ClientFightingMaster res = new ClientFightingMaster();
        res.setResType(ClientFightingMaster.RETRY_MATCH);
        res.setWaitTime(fighter.getLastpersonMatch() - fighter.getPersonMatchBegin());
        PacketManager.send(fighter.getRoleIdLong(), res);
    }

    /**
     * 打机器人
     */
    private void matchRobot(Fighter fighter, Robot expect, int floor, int ceil) {
        Fighter match = robots.match(expect, floor, ceil);
        int retry = 1;
        int retryceil = ceil;
        while (match == null) {
            retryceil += 500;
            match = robots.match(expect, floor, retryceil);
            retry++;
            if (retry >= 5) {
                break;
            }
        }
        if (match == null) {
            PacketManager.send(Long.valueOf(fighter.getRoleId()), new ClientText("匹配失败"));
            return;
        }
        fight(fighter, match);
    }

    private void fight(Fighter invotor, Fighter invotee) {
        String invotorFightId = invotor.getFightId();
        String invoteeFightId = invotee.getFightId();


        if (fightingMap.containsKey(invotorFightId) || fightingMap.containsKey(invoteeFightId)) { //如果有存在战斗
            Long invotorTimestamp = 0L;
            Long invoteeTimestamp = 0L;
            if (invotorFightId != null) {
                invotorTimestamp = timestampMap.get(invotorFightId);
            }
            if (invoteeFightId != null) {
                invoteeTimestamp = timestampMap.get(invoteeFightId);
            }
            //检查是不是有战斗超时还不移除，若超时，则销毁原来的战斗
            if (fightingMap.containsKey(invotorFightId) && (invotorTimestamp == null || System.currentTimeMillis() - invotorTimestamp > 900_000)) {
                LogUtil.info("fightingMaster|destoryFighting|invotor.roleid:{}", invotor.getRoleId());
                destoryFighting(invotorFightId);
            }
            if (fightingMap.containsKey(invoteeFightId) && (invoteeTimestamp == null || System.currentTimeMillis() - invoteeTimestamp > 900_000)) {
                LogUtil.info("fightingMaster|destoryFighting|invotee.roleid:{}", invotee.getRoleId());
                destoryFighting(invoteeFightId);
            }
        }

        if ((invotorFightId != null && fightingMap.containsKey(invotorFightId)) ||
                (invoteeFightId != null) && fightingMap.containsKey(invoteeFightId)) {
            return;
        }

        /** 创建一场战斗 */
        matchFightId++;
        int fightServer = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
        Fighting fighting = new Fighting(prefix + "-" + MultiServerHelper.getServerId() + "-" + matchFightId);
        List<Fighter> fighters = new ArrayList<>();
        fighters.add(invotee);
        fighters.add(invotor);
        fighting.setFightServer(fightServer);
        fightingMap.put(fighting.getFightId(), fighting);

        timestampMap.put(fighting.getFightId(), System.currentTimeMillis());


        // 到战斗服注册session
        MultiServerHelper.modifyConnectorRoute(invotor.getRoleIdLong(), fightServer);
        MultiServerHelper.modifyConnectorRoute(invotor.getRoleIdLong(), MultiServerHelper.getServerId());
        if (!(invotee instanceof Robot)) {
            MultiServerHelper.modifyConnectorRoute(invotee.getRoleIdLong(), fightServer);
            MultiServerHelper.modifyConnectorRoute(invotee.getRoleIdLong(), MultiServerHelper.getServerId());
        }

        StageinfoVo infoVo = SceneManager.getStageVo(FightingMasterManager.stageId);
        String[] position = infoVo.getMultiPosition().split("[|]");
        List<FighterEntity> entityList = new ArrayList<>();
        invotor.getCharactor().setCamp(FighterEntity.CAMP_SELF);
        invotor.getCharactor().setPosition(position[0]);
        invotee.getCharactor().setCamp(FighterEntity.CAMP_ENEMY);
        invotee.getCharactor().setPosition(position[1]);
        entityList.add(invotor.getCharactor());
        entityList.addAll(invotor.getOtherEntities());
        entityList.add(invotee.getCharactor());
        entityList.addAll(invotee.getOtherEntities());
        fighting.addFighter(invotor);
        fighting.addFighter(invotee);
        invotor.setFightId(fighting.getFightId());
        invotee.setFightId(fighting.getFightId());

        ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_FIGHTINGMASTER);
        enterPack.setStageId(FightingMasterManager.stageId);
        // 限制时间
        enterPack.setLimitTime(FightingMasterManager.fighttime);
        enterPack.setBlockMap(infoVo.getDynamicBlockMap());
        enterPack.setFighterEntityList(entityList);
        enterPack.getFighterEntityList().clear();

        int serverid = MultiServerHelper.getServerId();

        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        enterPack.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();

        FightingMasterRPC.fightBaseService().createFight(fightServer,
                FightConst.T_FIGHTING_MASTER, serverid, fighting.getFightId(), bytes, null);
    }

    /**
     * 通知客户端进入战斗场景
     *
     * @param fightId
     * @param roleId
     */
    private void noticeEntryFightScene(String fightId, long roleId) {
        Fighting fighting = fightingMap.get(fightId);
        if (fighting == null) {
            return;
        }
        /** 提取战斗需要的技能数据 */
        /*List<String> skillList = new ArrayList<>();
        for (Fighter fighter : fighting.getFighterSet()) {
            for (Map.Entry<Integer, Integer> entry : fighter.getCharactor().getSkills().entrySet()) {
                skillList.add(entry.getKey() + "_" + entry.getValue());
            }
            for (FighterEntity fn : fighter.getOtherEntities()) {
                for (Map.Entry<Integer, Integer> entry : fn.getSkills().entrySet()) {
                    skillList.add(entry.getKey() + "_" + entry.getValue());
                }
            }
        }
        NoticeClientEnter enter = new NoticeClientEnter();
        enter.setFightId(fighting.getFightId());
        enter.setSkillList(skillList);
        Fighter fighter = fighterMap.get(roleId);
        if (fighter != null) {
            fighter.noticClientEntry(enter);
        }*/
        List<FighterEntity> list = new ArrayList<>();
        List<FighterEntity> remove = new ArrayList<>();
        for (Fighter fighter : fighting.getFighterSet()) {
            FighterEntity copy = fighter.getCharactor().copy();
            copy.setFighterType(FighterEntity.TYPE_SELF);
            list.add(copy);
            if (fighter instanceof Robot || fighter.getRoleIdLong() != roleId) {
                remove.add(copy);
            }
            for (FighterEntity buddy : fighter.getOtherEntities()) {
                copy = buddy.copy();
                copy.setCamp(fighter.getCharactor().getCamp());
                list.add(copy);
                if (fighter instanceof Robot || fighter.getRoleIdLong() != roleId) {
                    remove.add(copy);
                }
            }
        }
        ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_FIGHTINGMASTER);
        enterPack.setStageId(FightingMasterManager.stageId);
        StageinfoVo info = SceneManager.getStageVo(FightingMasterManager.stageId);
        enterPack.setBlockMap(info.getDynamicBlockMap());
        // 限制时间
        enterPack.setLimitTime(FightingMasterManager.fighttime);
        enterPack.setFighterEntityList(list);
        enterPack.getFighterEntityList().removeAll(remove);
        PacketManager.send(roleId, enterPack);
    }

    @Override
    public void createFightingCallback(int serverId, String fightId, boolean success) {
        if (success) {
            Fighting fighting = fightingMap.get(fightId);
            List<FighterEntity> list = new ArrayList<>();
            for (Fighter key : fighting.getFighterSet()) {
                list.add(key.getCharactor());
                for (FighterEntity buddy : key.getOtherEntities()) {
                    buddy.setCamp(key.getCharactor().getCamp());
                    list.add(buddy);
                }
            }
            FightingMasterRPC.fightBaseService().addFighter(fighting.getFightServer(),
                    FightConst.T_FIGHTING_MASTER, MultiServerHelper.getServerId(), fighting.getFightId(), list);
        } else {
            Fighting fighting = fightingMap.get(fightId);
            for (Fighter fighter : fighting.getFighterSet()) {
                if (!(fighter instanceof Robot)) {
                    PacketManager.send(fighter.getRoleIdLong(), new ClientText("匹配失败"));
                }
            }
            destoryFighting(fightId);
        }
    }

    /**
     * 销毁战斗
     *
     * @param fightId
     */
    private void destoryFighting(String fightId) {
        Fighting fighting = fightingMap.remove(fightId);
        FightingMasterRPC.fightBaseService().stopFight(fighting.getFightServer(),
                FightConst.T_FIGHTING_MASTER, MultiServerHelper.getServerId(), fightId);
    }

    /***
     * 通知战斗服战斗准备完成回调
     * @param serverId
     * @param fightId
     * @param result
     */
    @Override
    public void noticeFightServerReadyCallback(int serverId, String fightId, boolean result) {
        if (!result) {
            Fighting fighting = fightingMap.get(fightId);
            // 因为连接已经切换到了战斗服，通知战斗服战斗准备失败后，需要将连接重新切回来,并通知客户端战斗创建失败
            for (Fighter fighter : fighting.getFighterSet()) {
                if (fighter instanceof Robot) {  // 机器人，跳过
                    continue;
                }
                MultiServerHelper.modifyConnectorRoute(Long.valueOf(fighter.getRoleId()), MultiServerHelper.getServerId());
                //通知客户端退出战斗场景
                ClientFightingMaster res = new ClientFightingMaster();
                res.setResType(ClientFightingMaster.BATTLE_FAILED);
                PacketManager.send(fighter.getRoleIdLong(), res);
            }
            destoryFighting(fightId);
        }
    }

    @Override
    public void handleDead(int serverId, String fightId, Map<String, String> deadMap) {
        Fighting fighting = fightingMap.get(fightId);
        if (fighting == null) {
            return;
        }
        // 处理死亡
        for (Map.Entry<String, String> entry : deadMap.entrySet()) {
            Fighter dead = fighting.getFighter(entry.getKey());
            if (dead == null) { // 宠物
                continue;
            }
            Fighter victor = null;
            for (Fighter fighter : fighting.getFighterSet()) {   // fighting 中只有两个人
                if (fighter == dead) {
                    continue;
                }
                victor = fighter;
            }
            handleResult(victor, dead);
            destoryFighting(fightId);
        }
    }

    /**
     * 处理战斗结果
     */
    private void handleResult(Fighter victor, Fighter loser) {
        Preconditions.checkArgument(victor != null);
        Preconditions.checkArgument(loser != null);
        // 处理胜利者
        if (!(victor instanceof Robot)) {
            try {
                // 处理战斗次数
                checkFightingTime(victor.getRoleFightingMaster(), RoleId2ServerIdManager.get(Long.valueOf(victor.getRoleId())));
                RoleFightingMaster roleFightingMaster = victor.getRoleFightingMaster();
                roleFightingMaster.setFightTimes((short) (victor.getRoleFightingMaster().getFightTimes() + 1));
                roleFightingMaster.setFightCount(roleFightingMaster.getFightCount() + 1);   // 总的战斗次数

                //发送五战奖励领取状态给主服
                byte fiveFightRewardStatus = FiveRewardStatusEvent.CAN_NOT_GET;
                if (roleFightingMaster.getFightTimes() >= 5) {
                    fiveFightRewardStatus = FiveRewardStatusEvent.CAN_GET;
                }
                if (roleFightingMaster.getFiveAward() == 1) {//已领取
                    fiveFightRewardStatus = FiveRewardStatusEvent.HAVE_GOT;
                }
                FightingMasterRPC.roleService().notice(victor.getServerId(), Long.valueOf(victor.getRoleId()), new FiveRewardStatusEvent(fiveFightRewardStatus));

                // 处理匹配机器人的概率,重置
                PersonPKcoeVo pkVo = FightingMasterManager.matchVo(victor.getCharactor().getFightScore(), roleFightingMaster.getDisScore());
                LogUtil.info("pkVo|fightScore:{}|displayScore:{}|matchScoreCoe:{}|displayScoreCoe:{}",
                        victor.getCharactor().getFightScore(), roleFightingMaster.getDisScore(), pkVo.getMatchscorecoe(), pkVo.getDisscorecoe());
                String[] odds = pkVo.getMatchpairodds().split("[+]");
                roleFightingMaster.setMatchRobotPersent(Integer.valueOf(odds[0]));

                // 处理连赢
                int seq = roleFightingMaster.getSeqWinOrFailed();
                if (seq / 1000 == 1) {
                    // 之前是赢了的
                    seq++;
                    roleFightingMaster.setSeqWinOrFailed(seq);
                } else {
                    roleFightingMaster.setSeqWinOrFailed(1001);
                }

                // 积分处理
                double factor;
                double changeScore;
                String[] matchScore = pkVo.getMatchscorecoe().split("[+]");
                String[] disScore = pkVo.getDisscorecoe().split("[+]");
                if (loser instanceof Robot) {
                    factor = 1.0 / 2.0;
                    changeScore = (1 - factor) * Integer.valueOf(matchScore[0]);
                } else {
                    double pow = Math.pow(10, -1 * (victor.getRoleFightingMaster().getDisScore() - loser.getRoleFightingMaster().getDisScore()) / Integer.valueOf(disScore[2]));
                    factor = 1.0 / (1 + pow);
                    changeScore = (1 - factor) * Integer.valueOf(disScore[0]);
                }
                if (changeScore < FightingMasterManager.winProtectScore) {  // 连赢保守积分
                    changeScore = FightingMasterManager.winProtectScore;
                }
                LogUtil.info("winer: factor {} | change score {}", factor, changeScore);
                int old = roleFightingMaster.getDisScore();
                roleFightingMaster.setDisScore(old + (int) Math.floor(changeScore));

                // 处理排行榜
                handleRankForResult(pkVo, roleFightingMaster);

                dao.update(roleFightingMaster);

                // 掉落奖励
                String[] award = pkVo.getEveryaward().split("[+]");
                Map<Integer, Integer> itemMap = new HashMap<Integer, Integer>();

                if (roleFightingMaster.getFightTimes() <= FightingMasterManager.awardCount) {
                    itemMap = DropUtil.executeDrop(Integer.valueOf(award[0]), 1);
                    //勋章产出加成
                    int validMedalId = roleFightingMaster.getValidMedalId();
                    //NewEquipmentManager.addProduce(NewEquipmentConstant.FightingMaster_AddProduce_TargetId, validMedalId, itemMap);

                    // 主服加物品
                    NoticeMainServerAddTool addToolEvent = new NoticeMainServerAddTool(itemMap, (byte) 1);
                    addToolEvent.setValidMedalId(validMedalId);
                    FightingMasterRPC.roleService().notice(victor.getServerId(), Long.valueOf(victor.getRoleId()), addToolEvent);
                }

                MultiServerHelper.modifyConnectorRoute(Long.valueOf(victor.getRoleId()), MultiServerHelper.getServerId());
                ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_FIGHTINGMASTER, ClientStageFinish.VICT);
                packet.setCurDisScore(roleFightingMaster.getDisScore());
                packet.setChangeScore((int) changeScore);
                packet.setItemMap(itemMap);
                PacketManager.send(Long.valueOf(victor.getRoleId()), packet);
            } finally {
                FightingMasterRPC.skyRankLocalService().handleScoreEvent(victor.getServerId(), victor.getRoleIdLong(), SkyRankScoreVo.TYPE_KFPVP, SceneManager.STAGE_VICTORY);
            }
        }

        // 处理失败者
        if (!(loser instanceof Robot)) {
            try {
                // 处理战斗次数
                checkFightingTime(loser.getRoleFightingMaster(), RoleId2ServerIdManager.get(Long.valueOf(loser.getRoleId())));
                RoleFightingMaster roleFightingMaster = loser.getRoleFightingMaster();
                roleFightingMaster.setFightTimes((short) (roleFightingMaster.getFightTimes() + 1));
                roleFightingMaster.setFightCount(roleFightingMaster.getFightCount() + 1);   // 总的战斗次数

                //发送五战奖励领取状态给主服
                byte fiveFightRewardStatus = FiveRewardStatusEvent.CAN_NOT_GET;
                if (roleFightingMaster.getFightTimes() >= 5) {
                    fiveFightRewardStatus = FiveRewardStatusEvent.CAN_GET;
                }
                if (roleFightingMaster.getFiveAward() == 1) {//已领取
                    fiveFightRewardStatus = FiveRewardStatusEvent.HAVE_GOT;
                }
                FightingMasterRPC.roleService().notice(loser.getServerId(), Long.valueOf(loser.getRoleId()), new FiveRewardStatusEvent(fiveFightRewardStatus));

                // 处理匹配机器人的概率
                PersonPKcoeVo pkVo = FightingMasterManager.matchVo(loser.getCharactor().getFightScore(), roleFightingMaster.getDisScore());
                String[] odds = pkVo.getMatchpairodds().split("[+]");
                roleFightingMaster.setMatchRobotPersent(roleFightingMaster.getMatchRobotPersent() + Integer.valueOf(odds[1]));

                // 处理连输
                int seq = roleFightingMaster.getSeqWinOrFailed();
                if (seq / 1000 == 2) {
                    // 之前是输了
                    seq++;
                    roleFightingMaster.setSeqWinOrFailed(seq);
                } else {
                    roleFightingMaster.setSeqWinOrFailed(2001);
                }

                // 积分处理
                String[] matchScore = pkVo.getMatchscorecoe().split("[+]");
                String[] disScore = pkVo.getDisscorecoe().split("[+]");
                double factor;
                double changeScore;
                if (victor instanceof Robot) {
                    factor = 1.0 / 2.0;
                    changeScore = (0 - factor) * Integer.valueOf(matchScore[1]);
                } else {
                    double pow = Math.pow(10, -1 * (loser.getRoleFightingMaster().getDisScore() - victor.getRoleFightingMaster().getDisScore()) / Integer.valueOf(disScore[2]));
                    factor = 1.0 / (1 + pow);
                    changeScore = (0 - factor) * Integer.valueOf(disScore[1]);
                }
                if (changeScore < FightingMasterManager.loseProtectScore) { // 失败保守积分
                    changeScore = FightingMasterManager.loseProtectScore;
                }
                LogUtil.info("loser: factor {} | change score {}", factor, changeScore);
                int old = roleFightingMaster.getDisScore();
                roleFightingMaster.setDisScore(old + (int) Math.floor(changeScore));

                // 排行榜处理
                handleRankForResult(pkVo, roleFightingMaster);

                dao.update(loser.getRoleFightingMaster());

                // 掉落奖励
                String[] award = pkVo.getEveryaward().split("[+]");
                Map<Integer, Integer> itemMap = new HashMap<Integer, Integer>();
                if (roleFightingMaster.getFightTimes() <= FightingMasterManager.awardCount) {
                    itemMap = DropUtil.executeDrop(Integer.valueOf(award[1]), 1);
                    // 主服加物品
                    FightingMasterRPC.roleService().notice(loser.getServerId(), Long.valueOf(loser.getRoleId()), new NoticeMainServerAddTool(itemMap, (byte) 0));
                }

                MultiServerHelper.modifyConnectorRoute(Long.valueOf(loser.getRoleId()), MultiServerHelper.getServerId());
                ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_FIGHTINGMASTER, ClientStageFinish.LOSE);
                packet.setCurDisScore(roleFightingMaster.getDisScore());
                packet.setChangeScore((int) changeScore);
                packet.setItemMap(itemMap);
                PacketManager.send(Long.valueOf(loser.getRoleId()), packet);
            } finally {
                FightingMasterRPC.skyRankLocalService().handleScoreEvent(loser.getServerId(), loser.getRoleIdLong(), SkyRankScoreVo.TYPE_KFPVP, SceneManager.STAGE_FAIL);
            }

        }
    }

    /**
     * 战斗结算后积分变化后排行榜的处理
     *
     * @param pkVo
     * @param roleFightingMaster
     */
    private void handleRankForResult(PersonPKcoeVo pkVo, RoleFightingMaster roleFightingMaster) {
        Map<Integer, RoleFightingMaster> subRank = rank.get(pkVo.getRankid());
        pkVo = FightingMasterManager.matchVo(roleFightingMaster.getFightScore(), roleFightingMaster.getDisScore());
        if (subRank != rank.get(pkVo.getRankid())) {
            deleteFromRank(subRank, roleFightingMaster);
            subRank = rank.get(pkVo.getRankid());
            if (subRank == null) {
                subRank = new HashMap<>();
                rank.put(pkVo.getRankid(), subRank);
            }
        }
        updateRank(subRank, roleFightingMaster);
    }

    /**
     * 从子榜中删除玩家
     *
     * @param subRank
     * @param roleFightingMaster
     */
    private void deleteFromRank(Map<Integer, RoleFightingMaster> subRank, RoleFightingMaster roleFightingMaster) {
        if (subRank.get(roleFightingMaster.getRank()) == roleFightingMaster) {
            for (int i = roleFightingMaster.getRank(); i < subRank.size(); i++) {
                RoleFightingMaster rfm = subRank.get(i + 1);
                rfm.setRank(i);
                subRank.put(i, rfm);
                dao.update(rfm);
            }
            subRank.remove(subRank.size());
            roleFightingMaster.setRank(0);
            dao.update(roleFightingMaster);
        }
    }

    @Override
    public void reloadProduct(int serverId) {
        try {
            FightingMasterStartup.loadProduct();
        } catch (Exception e) {
            LogUtil.info("斗神殿服重载产品数据失败,serverId={}", ServerManager.getServer().getConfig().getServerId());
            LogUtil.error("", e);
        }
    }

    @Override
    public void updateMaxFightingVal(int serverId, int fighting) {
        if (fighting > MaxFightingVal) {
            MaxFightingVal = fighting;
        }
    }

    @Override
    public void updateRoleName(int fightingMasterServer, long roleId, String newName) {
        RoleFightingMaster roleFightingMaster = roleDataMap.get(roleId);
        if (roleFightingMaster != null) {
            roleFightingMaster.setName(newName);
            dao.update(roleFightingMaster);
        }
    }
}
