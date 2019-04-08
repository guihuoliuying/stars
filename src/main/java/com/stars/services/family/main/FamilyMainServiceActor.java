package com.stars.services.family.main;

import com.google.common.cache.*;
import com.stars.core.dao.DbRowDao;
import com.stars.core.event.Event;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.family.FamilyManager;
import com.stars.modules.family.event.*;
import com.stars.modules.family.packet.ClientFamilyManagement;
import com.stars.modules.familyactivities.treasure.event.LeaveOrKickOutFamilyEvent;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.friend.event.FriendLogEvent;
import com.stars.modules.newserverrank.NewServerRankConstant;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.event.FamilyEvent;
import com.stars.services.family.main.memdata.FamilyPlaceholder;
import com.stars.services.family.main.memdata.RecommendationFamily;
import com.stars.services.family.main.prodata.FamilyLevelVo;
import com.stars.services.family.main.userdata.FamilyApplicationPo;
import com.stars.services.family.main.userdata.FamilyLogData;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.family.main.userdata.FamilyPo;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyRankPo;
import com.stars.services.rank.userdata.FamilyTreasureRankPo;
import com.stars.services.role.RoleNotification;
import com.stars.services.summary.Summary;
import com.stars.util.*;
import com.stars.util.actlock.ActSimpleLock;
import com.stars.core.actor.invocation.InvocationFuture;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.stars.modules.family.FamilyManager.*;

/**
 * fixme: 自动禅让逻辑（自动触发逻辑处理）
 * Created by zhaowenshuo on 2016/8/24.
 */
public class FamilyMainServiceActor extends ServiceActor implements FamilyMainService {

    public static volatile boolean isLoadData = false;
    public static volatile List<RecommendationFamily> recommList;

    private ActSimpleLock globalUnidirectLock = new ActSimpleLock(); // 单向锁
    private DbRowDao dao;
    private String serviceName;
    private Map<Long, FamilyData> onlineDataMap; // 在线列表
    private LoadingCache<Long, FamilyData> offlineDataMap;
    private Map<Long, FamilyData> pendingSavingDataMap;


    public FamilyMainServiceActor(String id) {
        this.serviceName = "family main service-" + id;
    }

    public FamilyMainServiceActor(int id) {
        this(Integer.toString(id));
    }

    @Override
    public void init() throws Throwable {
        synchronized (FamilyMainServiceActor.class) {
            if (!isLoadData) {
                isLoadData = true;
                SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyRecommendation, new RecommendationFamilyListUpdateTask(), 0, 5, TimeUnit.SECONDS);
            }
        }
        dao = new DbRowDao(serviceName);
        ServiceSystem.getOrAdd(serviceName, this);
        onlineDataMap = new HashMap<>();
        offlineDataMap = CacheBuilder.newBuilder()
//                .maximumSize(500)
                .expireAfterAccess(7, TimeUnit.DAYS)
                .removalListener(new RemovalListener<Long, FamilyData>() {
                    @Override
                    public void onRemoval(RemovalNotification<Long, FamilyData> notification) {
                        long familyId = notification.getKey();
                        FamilyData data = notification.getValue();
                        if (data.getPlaceholderMap().size() > 0) {
                            pendingSavingDataMap.put(familyId, data);
                            LogUtil.error("family - main缓存移除异常（挖人），roleId=" + notification.getKey());
                        }
                        Set<DbRow> set = new HashSet<DbRow>();
                        set.add(data.getFamilyPo());
                        set.addAll(data.getMemberPoMap().values());
                        set.addAll(data.getApplicationPoMap().values());
                        if (!dao.isSavingSucceeded(set)) {
                            pendingSavingDataMap.put(familyId, data);
                            LogUtil.error("family - main缓存移除异常（保存），roleId=" + notification.getKey());
                        }
                    }
                })
                .build(new FamilyDataCacheLoader());
        pendingSavingDataMap = new HashMap<>();
    }

    @Override
    public void printState() {

    }

    @Override
    public void save() {
        dao.flush();
    }

    @Override
    public List<RecommendationFamily> getOnlineRecommendationList() {
        List<RecommendationFamily> list = new LinkedList<>();
        for (FamilyData data : onlineDataMap.values()) {
            list.add(newRecommendationFamily(data.getFamilyPo()));
        }
        return list;
    }

    @Override
    public void online(long familyId, long roleId, boolean needRespAuth) {
        // 加载家族数据
        byte postId = FamilyPost.ERROR_ID;
        FamilyData data = onlineDataMap.get(familyId);
        if (data == null && pendingSavingDataMap.containsKey(familyId)) {
            data = pendingSavingDataMap.remove(familyId);
            onlineDataMap.put(familyId, data);
        }
        if (data == null && offlineDataMap.getIfPresent(familyId) != null) {
            data = offlineDataMap.getIfPresent(familyId);
            offlineDataMap.invalidate(familyId);
            onlineDataMap.put(familyId, data);
        }
        if (data == null) {
            try {
                FamilyPo familyPo = DBUtil.queryBean(
                        DBUtil.DB_USER, FamilyPo.class, "select * from `family` where `familyid`=" + familyId);
                Map<Long, FamilyMemberPo> memberPoMap = DBUtil.queryMap(
                        DBUtil.DB_USER, "roleid", FamilyMemberPo.class, "select * from `familymember` where `familyid`=" + familyId);
                Map<Long, FamilyApplicationPo> applicationPoMap = DBUtil.queryMap(
                        DBUtil.DB_USER, "roleid", FamilyApplicationPo.class, "select * from `familyapplication` where `familyid`=" + familyId);

                if (familyPo != null) {
                    data = new FamilyData(familyPo, memberPoMap, applicationPoMap);
                    onlineDataMap.put(familyId, data);
//                    autoAbdicate(data); // 自动禅让
                    recalc(data); // 重新计算
                } else { // 存在错误的数据
                    // fixme: fuck!
                    LogUtil.error("不存在familyId", new Exception());
                    return;
                }
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
        // 更新在线数据
        FamilyMemberPo memberPo = null;
        if (data != null && data.getMemberPoMap().containsKey(roleId)) {
            memberPo = data.getMemberPoMap().get(roleId);
            if (!memberPo.isOnline()) {
                memberPo.setOnline(true);
                data.increaseOnlineCount();
            }
            memberPo.setOfflineTimestamp(now());
        } else {
            LogUtil.error("不存在familyId或roleId", new Exception());
            return;
        }
        if (roleId == 4206886947L) {
            for (FamilyMemberPo mpo : data.getMemberPoMap().values()) {
                LogUtil.info("FamilyMainServiceActor - mpo, roleId:{}, familyId:{}, postId:{}",
                        mpo.getRoleId(), mpo.getFamilyId(), mpo.getPostId());
            }
        }
        autoAbdicate(data); // 执行自动禅让
        // 通知用户更新公会权限
        if (needRespAuth) {
            FamilyPo familyPo = data.getFamilyPo();
            postId = memberPo.getPostId();
            FamilyPost post = FamilyPost.postMap.containsKey(postId) ? FamilyPost.postMap.get(postId) : FamilyPost.ERROR;
            ServiceHelper.roleService().notice(roleId, new FamilyAuthUpdatedEvent(
                    FamilyAuthUpdatedEvent.TYPE_LOGIN, roleId, familyId, familyPo.getName(), familyPo.getLevel(), post, familyId));
            ServiceHelper.roleService().notice(roleId, new FamilyLockUpdatedEvent(familyId, familyPo.getLockState()));
        }

        FamilyPost post = FamilyPost.postMap.containsKey(postId) ? FamilyPost.postMap.get(postId) : FamilyPost.ERROR;
        if (post.canVerify()) {
            Set<Long> applyIds = new HashSet<>();
            for (Map.Entry<Long, FamilyApplicationPo> entry : data.getApplicationPoMap().entrySet()) {
                if (entry.getValue().getType() == FamilyApplicationPo.TYPE_APPLYING) {
                    applyIds.add(entry.getValue().getRoleId());
                }
            }
            ServiceHelper.roleService().notice(roleId, new FamilyAddApplyEvent(applyIds));
        }


        // 通知其他在线成员
        ClientFamilyManagement notifiedPacket = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_ONLINE);
        notifiedPacket.setMemberId(roleId);
        notifiedPacket.setMemberName(memberPo.getRoleName());
        sendToAllMember(data, notifiedPacket, roleId);
        // 通知其他服务进行登录
        ServiceHelper.familyRedPacketService().online(familyId, roleId);
        ServiceHelper.familyEventService().online(familyId);
        ServiceHelper.familyTreasureService().online(roleId, familyId);
//        for (Long aLong : onlineDataMap.keySet()) {
//            LogUtil.info("玩家:{} 上线--在线家族id:{}", roleId, aLong);
//        }
//        for (Long aLong : offlineDataMap.asMap().keySet()) {
//            LogUtil.info("玩家:{} 上线--离线家族id:{}", roleId, aLong);
//        }
    }

    @Override
    public void offline(long familyId, long roleId) {
        FamilyData data = getOnlineData(familyId);
        if (data == null) {
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(roleId);
        if (memberPo == null) {
            return;
        }
        memberPo.setOnline(false);
        memberPo.setOfflineTimestamp(now());
        dao.update(memberPo);
        data.decreaseOnlineCount();
        if (data.getOnlineCount() <= 0) { // 家族的在线人数为0时，移到离线容器中
            data.getFamilyPo().setLastActiveTimestamp(now());
            dao.update(data.getFamilyPo());
            onlineDataMap.remove(familyId);
            offlineDataMap.put(familyId, data);
            // 通知其他服务进行离线
            ServiceHelper.familyRedPacketService().offline(familyId);
            ServiceHelper.familyEventService().offline(familyId);
//            ServiceHelper.familyTreasureService().offline(familyId);
        }
        // 通知其他在线成员
        ClientFamilyManagement notifiedPacket = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_OFFLINE);
        notifiedPacket.setMemberId(roleId);
        notifiedPacket.setMemberName(memberPo.getRoleName());
        sendToAllMember(data, notifiedPacket, roleId);

        ServiceHelper.chatService().delFamilyMemberId(familyId, roleId);
    }

    @Override
    public void getFamilyAuth(long familyId, long roleId, InvocationFuture future) {
        FamilyData data = getData(familyId);
        FamilyPo familyPo = data.getFamilyPo();
        FamilyMemberPo memberPo = data.getMemberPoMap().get(roleId);
        if (data == null) {
            LogUtil.error("不存在家族数据, familyId=" + familyId + ", roleId=" + roleId);
            future.setThrowable(new IllegalStateException("不存在家族数据, familyId=" + familyId + ", roleId=" + roleId));
            return;
        }
        if (memberPo == null) {
            LogUtil.error("不是家族成员, familyId=" + familyId + ", roleId=" + roleId);
            future.setThrowable(new IllegalStateException("不是家族成员, familyId=" + familyId + ", roleId=" + roleId));
            return;
        }
        future.set(new FamilyAuth(familyId, familyPo.getName(), familyPo.getLevel(),
                roleId, "", FamilyPost.postMap.get(memberPo.getPostId())));
        return;
    }

    @Override
    public void sendFamilyInfo(FamilyAuth auth) {
        if (auth.getFamilyId() == 0) {
            sendText(auth.getRoleId(), I18n.get("family.management.notJoinFamily"));
            return;
        }
        FamilyData data = getData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_INFO);
        packet.setFamilyData(data);
        PlayerUtil.send(auth.getRoleId(), packet);
        packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_OPTIONS);
        packet.setFamilyData(data);
        PlayerUtil.send(auth.getRoleId(), packet);
    }

    @Override
    public void sendMemberList(FamilyAuth auth) {
        if (auth.getFamilyId() == 0) {
            sendText(auth.getRoleId(), I18n.get("family.management.notJoinFamily"));
            return;
        }
        FamilyData data = getData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_LIST);
        packet.setFamilyData(data);
        PlayerUtil.send(auth.getRoleId(), packet);
    }

    @Override
    public List<Long> getMemberIdList(long familyId, long roleId) {
        if (familyId == 0) {
            return new ArrayList<>();
        }
        FamilyData data = getData(familyId);
        if (data == null) {
            return new ArrayList<>();
        }
        if (!data.getMemberPoMap().containsKey(roleId)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(data.getMemberPoMap().keySet());
    }

    @Override
    public List<FamilyMemberPo> getMemberList(long familyId, boolean isOnline) {
        if (familyId == 0) {
            return new ArrayList<>();
        }
        FamilyData data = getData(familyId);
        if (data == null) {
            return new ArrayList<>();
        }
        List<FamilyMemberPo> list = new ArrayList<>();
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (isOnline && !memberPo.isOnline()) {
                continue;
            }
            FamilyMemberPo clone = newMemberPo(familyId, memberPo.getRoleId(), memberPo.getJobId(),
                    memberPo.getPostId(), memberPo.getRoleName(), memberPo.getRoleLevel(), memberPo.getRoleFightScore());
            clone.setHistoricalContribution(memberPo.getHistoricalContribution());
            clone.setRmbDonation(memberPo.getRmbDonation());
            clone.setOfflineTimestamp(memberPo.getOfflineTimestamp());
            clone.setOnline(memberPo.isOnline());
            list.add(clone);
        }
        return list;
    }

    @Override
    public void sendApplicationList(FamilyAuth auth) {
        if (!auth.getPost().canVerify()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_APPLICATION_LIST);
        packet.setFamilyData(data);
        PlayerUtil.send(auth.getRoleId(), packet);
    }

    @Override
    public void updateMemberLevel(long familyId, long memberId, int currentLevel) {
        FamilyData data = getData(familyId);
        if (data == null) {
            LogUtil.error("updateMemberLevel 不存在对应家族数据");
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(memberId);
        if (memberPo == null) {
            LogUtil.error("updateMemberLevel 不存在对应成员数据");
            return;
        }
        memberPo.setRoleLevel(currentLevel);
        dao.update(memberPo);
    }

    @Override
    public void updateFalimyName(long familyId, String newName) {
        FamilyData data = getData(familyId);
        FamilyPo familyPo = data.getFamilyPo();
        familyPo.setName(newName);
        dao.update(familyPo);
        FamilyRankPo rank = (FamilyRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_FAMILYFIGHTSCORE, familyId);
        rank.setName(newName);
        dao.update(rank);
        FamilyTreasureRankPo familyTreasureRankPo = (FamilyTreasureRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_FAMILYTREASURE, familyId);
        familyTreasureRankPo.setName(newName);
        dao.update(familyTreasureRankPo);
    }

    @Override
    public void updateMemberFightScore(long familyId, long memberId, int currentFightScore) {
        FamilyData data = getData(familyId);
        if (data == null) {
            LogUtil.error("updateMemberLevel 不存在对应家族数据");
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(memberId);
        if (memberPo == null) {
            LogUtil.error("updateMemberLevel 不存在对应成员数据");
            return;
        }
        memberPo.setRoleFightScore(currentFightScore);
        recalc(data);
        dao.update(data.getFamilyPo(), memberPo);
    }

    @Override
    public void updateMemberJob(long familyId, long roleId, Integer newJobId) {
        FamilyData data = getData(familyId);
        if (data == null) {
            LogUtil.error("updateMemberLevel 不存在对应家族数据");
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(roleId);
        if (memberPo == null) {
            LogUtil.error("updateMemberLevel 不存在对应成员数据");
            return;
        }
        if (memberPo.getJobId() != newJobId) {
            memberPo.setJobId(newJobId);
            dao.update(memberPo);
        }
    }

    @Override
    public void updateFalimyMasterName(long familyId, String name) {
        FamilyData data = getData(familyId);
        FamilyPo familyPo = data.getFamilyPo();
        familyPo.setMasterName(name);
        dao.update(familyPo);
    }

    @Override
    public void updateMemberName(long familyId, long memberId, String newName) {
        FamilyData data = getData(familyId);
        if (data == null) {
            LogUtil.error("updateMemberLevel 不存在对应家族数据");
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(memberId);
        if (memberPo == null) {
            LogUtil.error("updateMemberLevel 不存在对应成员数据");
            return;
        }
        FamilyMemberPo familyMemberPo = data.getMemberPoMap().get(memberId);
        familyMemberPo.setRoleName(newName);
        dao.update(familyMemberPo);
    }

    @Override
    public void updateMemberContribution(long familyId, long memberId, int contributionDelta) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateMemberRmbDonation(long familyId, long memberId, int donationDelta) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FamilyAuth create(long roleId, FamilyPost post, String familyName, String familyNotice,
                             int roleJobId, String roleName, int roleLevel, int roleFightScore) {
        // 判断权限
        if (!post.canCreate()) {
            sendText(roleId, "family_tips_nopost");
            return null;
        }
        // fixme: 生成一个新的familyId
        long familyId = ServiceHelper.idService().newFamilyId();
        ServiceHelper.familyMainService().innerCreate(familyId, roleId, post, familyName, familyNotice,
                roleJobId, roleName, roleLevel, roleFightScore, getCurrentFuture());
        return null;
    }

    @Override
    public void innerCreate(long familyId, long roleId, FamilyPost post, String familyName, String familyNotice,
                            int roleJobId, String roleName, int roleLevel, int roleFightScore, InvocationFuture future) {
        // 初始化
        FamilyPo familyPo = newFamilyPo(familyId, familyName, roleName, familyNotice);
        familyPo.setInsertStatus();
        FamilyMemberPo memberPo = newMemberPo(familyId, roleId, roleJobId, FamilyPost.MASTER_ID, roleName, roleLevel, roleFightScore);
        memberPo.setInsertStatus();
        try {
//            DBUtil.execUserSql(familyPo.getChangeSql());
            if (!insertFamilyPo(familyPo.getChangeSql())) {
                future.set(null);
                sendText(roleId, "family_tips_samename"); // 多数都是重名
                return;
            }
            DBUtil.execUserSql(memberPo.getChangeSql());
        } catch (Exception e) {
            future.set(null);
            LogUtil.error("创建家族异常", e);
            return;
        }
        familyPo.saveStatus();
        memberPo.saveStatus();
        Map<Long, FamilyMemberPo> memberPoMap = new HashMap<>();
        memberPoMap.put(roleId, memberPo);
        FamilyData data = new FamilyData(familyPo, memberPoMap, new HashMap<Long, FamilyApplicationPo>());
        onlineDataMap.put(familyId, data);
        recalc(data);
        dao.update(data.getFamilyPo());
        // 通知对应的更新
        boolean flag = ServiceHelper.familyRoleService().compareAndSetFamilyId(roleId, 0, familyId);
        if (flag) {
            LogUtil.info("家族|创建成功|roleId:{}|familyId:{}|familyName:{}", roleId, familyId, familyName);
            future.set(new FamilyAuth(familyId, familyName, familyPo.getLevel(), roleId, roleName, FamilyPost.MASTER));
            online(familyId, roleId, false);
        } else {
            future.set(null);
            LogUtil.error("");
        }
    }

    private boolean insertFamilyPo(String insertSql) {
        Objects.requireNonNull(insertSql);
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBUtil.getConnection(DBUtil.DB_USER);
            stmt = conn.createStatement();
            int updatedCount = stmt.executeUpdate(insertSql);
            return updatedCount > 0 ? true : false;
        } catch (SQLException e) {
            LogUtil.error("SQL异常：" + insertSql, e);
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LogUtil.error("DBUtil.execBatch()回滚异常", ex);
            }
            return false;
        } finally {
            DBUtil.closeStatement(stmt);
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public FamilyAuth dissolve(FamilyAuth auth) {
        if (!auth.getPost().canDissolve()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return null;
        }
        //
        long familyId = auth.getFamilyId();
        FamilyData data = getOnlineData(auth.getFamilyId());
        String familyName = "";
        int familyLevel = 0;
        if (data != null) {
            if (data.getMemberPoMap().size() != 1) {
                sendText(auth.getRoleId(), "family_tips_nodissolve");
                return null;
            }
            if (globalUnidirectLock.isLock() || data.getFamilyPo().isLock()) {
                sendText(auth.getRoleId(), "family_tips_lock");
                return null;
            }
            FamilyPo familyPo = data.getFamilyPo();
            familyPo.setMasterName(""); // 设置族长名为空字符串
            familyPo.setMemberCount(0); // 设置成员数为0
            familyPo.setAllowApplication((byte) 0); // 不让申请
            dao.update(data.getFamilyPo());
            dao.delete(data.getMemberPoMap().get(auth.getRoleId()));
            for (FamilyApplicationPo applicationPo : data.getApplicationPoMap().values()) {
                dao.delete(applicationPo);
            }
            onlineDataMap.remove(auth.getFamilyId());
            offlineDataMap.put(familyId, data);
        }
        ServiceHelper.roleService().notice(auth.getRoleId(), new LeaveOrKickOutFamilyEvent(true));
        ServiceHelper.familyTreasureService().dissolve(familyId);
        ServiceHelper.rankService().removeRank(RankConstant.RANKID_FAMILYFIGHTSCORE, auth.getFamilyId(), new FamilyRankPo(auth.getFamilyId()));
        MainRpcHelper.familywarRankService().delete(FamilyWarUtil.getFamilyWarServerId(), auth.getFamilyId());
        LogUtil.info("家族|解散家族|roleId:{}|familyId:{}|familyName:{}",
                auth.getRoleId(), auth.getFamilyId(), auth.getFamilyName());
        if (ServiceHelper.familyRoleService().compareAndSetFamilyId(auth.getRoleId(), auth.getFamilyId(), 0)) {
            ServiceHelper.familyRoleService().multiplyAndSendContribution(auth.getRoleId(), contributionPenaltyRatio);
            return new FamilyAuth(0, "", 0, auth.getRoleId(), auth.getRoleName(), FamilyPost.MASSES);
        } else {
            return null;
        }
    }

    @Override
    public void editNotice(FamilyAuth auth, String notice) {
        if (notice == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.reqNotEmptyNotice"));
            return;
        }
        if (!auth.getPost().canEditNotice()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        data.getFamilyPo().setNotice(notice);
        dao.update(data.getFamilyPo());
        // 更新家族显示信息
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_INFO);
        packet.setFamilyData(data);
        sendToAllMember(data, packet);
    }

    @Override
    public void apply(long familyId, boolean needSendText, long applicantId, FamilyPost post, int roleJobId, String applicantName, int applicantLevel, int applicantFightScore) {
        if (post == null || !post.canApply()) {
            sendText(applicantId, needSendText, "family_tips_nopost");
            return;
        }
        FamilyData data = getData(familyId);
        if (data == null) {
            sendText(applicantId, needSendText, I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyPo familyPo = data.getFamilyPo();
        if (!familyPo.isAllowedApplication()) {
            sendText(applicantId, needSendText, "family_tips_noapply");
            return;
        }
        FamilyLevelVo levelVo = levelVoMap.get(data.getFamilyPo().getLevel());
        if (levelVo == null) {
            LogUtil.error("傻缺策划配错数据了");
            return;
        }
        // 判断是否达到申请列表的最大值，以及是否存在
        if (data.getMemberPoMap().size() + data.getPlaceholderMap().size() >= levelVo.getMemberLimit()) {
            sendText(applicantId, needSendText, "family_tips_appfullmember");
            return;
        }
        // 判断是否已发送申请
        if (data.getApplicationPoMap().containsKey(applicantId)) {
            sendText(applicantId, needSendText, I18n.get("family.management.apply.alreadySent"));
            return;
        }
        // 判断等级
        if (familyPo.getQualificationMinLevel() != 0 && applicantLevel < familyPo.getQualificationMinLevel()) {
            sendText(applicantId, needSendText, "family_tips_inconformity");
            return;
        }
        // 战力判断
        if (familyPo.getQualificationMinFightScore() != 0 && applicantFightScore < familyPo.getQualificationMinFightScore()) {
            sendText(applicantId, needSendText, "family_tips_inconformity");
            return;
        }
        FamilyApplicationPo applicationPo = newApplicationPo(familyId, applicantId, roleJobId, applicantName, applicantLevel, applicantFightScore);
        data.getApplicationPoMap().put(applicantId, applicationPo);
        dao.insert(applicationPo);


        // 是否自动审核
        if (!familyPo.isAutoVerified() || data.getFamilyPo().isAllLock()) {
            // 非自动审核/锁定家族
            // 设置
            ServiceHelper.familyRoleService().addAppliedFamilyId(applicantId, familyId);
            for (Map.Entry<Long, FamilyMemberPo> entry : data.getMasterPoMap().entrySet()) {
                //家族申请红点
                ServiceHelper.roleService().notice(entry.getValue().getRoleId(), new FamilyAddApplyEvent(applicantId));
            }

            // 通知
            sendText(applicantId, needSendText, I18n.get("family.management.apply.alreadySent"));
            // 通知
            ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_APPLY);
            packet.setFamilyData(data);
            PlayerUtil.send(applicantId, packet);
        } else {
            // 自动审核
            verify0(familyId, 0, applicantId, data, true, false);
        }
    }

    @Override
    public void verify(FamilyAuth auth, long applicantId, boolean isOk) {
        if (!auth.getPost().canVerify()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            return;
        }
        if (data.getFamilyPo().isAllLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        // todo: 判断成员数量是否足够
        FamilyLevelVo levelVo = levelVoMap.get(data.getFamilyPo().getLevel());
        if (levelVo == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.incorrectProductData"));
            return;
        }
        if (isOk && data.getMemberPoMap().size() >= levelVo.getMemberLimit()) { // 同意时才进行判断列表大小
            sendText(auth.getRoleId(), "family_tips_fullmember");
            return;
        }
        verify0(auth.getFamilyId(), auth.getRoleId(), applicantId, data, isOk, true);
        for (Map.Entry<Long, FamilyMemberPo> entry : data.getMasterPoMap().entrySet()) {
            //家族申请红点
            ServiceHelper.roleService().notice(entry.getValue().getRoleId(), new FamilyRemoveApplyEvent(applicantId));
        }
        // 同步申请列表（收到后删除）
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_APPLICATION_VERIFY);
        packet.setApplicantId(applicantId);
        PlayerUtil.send(auth.getRoleId(), packet);
    }

    private void verify0(long familyId, long verifierId, long applicantId, FamilyData data, boolean isOk, boolean needTips) {
        FamilyApplicationPo applicationPo = data.getApplicationPoMap().remove(applicantId);
        if (applicationPo == null) {
            if (needTips) {
                sendText(verifierId, I18n.get("family.management.apply.noSuchApplicant"));
            }
            return;
        }
        dao.delete(applicationPo);
        // todo: 判断成员数量是否足够
        if (isOk) {
            FamilyMemberPo memberPo = newMemberPo(applicationPo);
            data.getMemberPoMap().put(memberPo.getRoleId(), memberPo);
            dao.insert(memberPo);
            ServiceHelper.familyRoleService().innerNotifyVerification(applicantId, verifierId, familyId, true);
        } else {
            ServiceHelper.familyRoleService().innerNotifyVerification(applicantId, verifierId, familyId, false);
        }
    }

    @Override
    public void innerAckVerification(long familyId, long verifierId, long applicantId, boolean isOk, String cause) {
        FamilyData data = getData(familyId);
        if (data == null) {
            LogUtil.error(I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(applicantId);
        if (memberPo == null) {
            LogUtil.error(I18n.get("family.management.noSuchMember"));
            return;
        }
        if (isOk) {
            recalcForNewMember(data, memberPo);
            FamilyPo familyPo = data.getFamilyPo();
            familyPo.setMemberCount(data.getMemberPoMap().size());
            dao.update(familyPo);
            ServiceHelper.roleService().notice(applicantId, new RoleNotification(
                    new FamilyAuthUpdatedEvent(FamilyAuthUpdatedEvent.TYPE_NEW, applicantId, familyId, familyPo.getName(), familyPo.getLevel(), FamilyPost.MEMBER, 0)));
            ServiceHelper.roleService().notice(applicantId, new RoleNotification(
                    new FamilyAuthAchieveEvent(FamilyAuthAchieveEvent.TYPE_NEW, applicantId, familyId, familyPo.getName(), familyPo.getLevel(), FamilyPost.MEMBER, 0)));
            ServiceHelper.emailService().sendToSingle(applicantId, FamilyManager.joinEmailTemplateId, familyId, "系统", null,
                    familyPo.getName());
            sendText(verifierId, I18n.get("family.management.verify.succeedAdding"));
            // 通知其他服务进行登录
            ServiceHelper.familyRedPacketService().addMember(familyId, applicantId);
            ServiceHelper.familyEventService().logEvent(familyId, FamilyEvent.M_JOIN, memberPo.getRoleName());
            // 通知客户端
            ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_ADD);
            packet.setMemberPo(memberPo);
            sendToAllMember(data, packet);
            doAddMember(familyId, memberPo.copy());
            // 日志
            LogUtil.info("家族|加入成功|roleId:{}|familyId:{}|familyName:{}", applicantId, familyId, familyPo.getName());
        } else {
            data.getMemberPoMap().remove(memberPo.getRoleId());
            dao.delete(memberPo);
            sendText(verifierId, cause);
            // 日志
            LogUtil.info("家族|加入失败|roleId:{}|familyId:{}|familyName:{}", applicantId, familyId, data.getFamilyPo().getName());
        }
    }

    @Override
    public void cancel(long familyId, long applicantId) {
        FamilyData data = getData(familyId);
        if (data == null) {
            LogUtil.error(I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyApplicationPo applicationPo = data.getApplicationPoMap().remove(applicantId);
        if (applicationPo != null) {
            dao.delete(applicationPo);
            for (Map.Entry<Long, FamilyMemberPo> entry : data.getMasterPoMap().entrySet()) {
                //家族申请红点
                ServiceHelper.roleService().notice(entry.getValue().getRoleId(), new FamilyRemoveApplyEvent(applicantId));
            }
            ServiceHelper.familyRoleService().innerNotifyVerification(applicantId, 0, familyId, false);
            // 通知
            ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_APPLICATION_CANCEL);
            packet.setFamilyData(data);
            PlayerUtil.send(applicantId, packet);
        }
    }

    @Override
    public void kickOut(FamilyAuth auth, long memberId) {
        if (auth == null || !auth.getPost().canKickOut()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (globalUnidirectLock.isLock() || data.getFamilyPo().isAllLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        if (!data.getMemberPoMap().containsKey(memberId)) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(memberId);
        // 判断强踢的对象是不是副组长或长老
        if (memberPo.getPostId() < FamilyPost.MEMBER_ID) {
            sendText(auth.getRoleId(), "family_tips_getoutprotect");
            return;
        }
        // 对元宝贡献大于指定阈值的成员进行保护
        // 对int要强制提升成long
        if (memberPo.getRmbDonation() > protectionContributionThreshold
                && System.currentTimeMillis() - memberPo.getOfflineTimestamp() * 1000L < protectionTimeLimit) {
            sendText(auth.getRoleId(), "family_tips_getoutprotect");
            return;
        }
        boolean canVerify = false;
        FamilyPost post = FamilyPost.postMap.containsKey(memberPo.getPostId()) ? FamilyPost.postMap.get(memberPo.getPostId()) : FamilyPost.ERROR;
        if (post.canVerify()) {
            canVerify = true;
        }
        data.getMemberPoMap().remove(memberId);
        dao.delete(memberPo);
        FamilyPo familyPo = data.getFamilyPo();
        familyPo.setMemberCount(data.getMemberPoMap().size());
        dao.update(familyPo);
        ServiceHelper.familyRoleService().setFamilyId(memberId, 0L); // delete it
        ServiceHelper.familyRoleService().multiplyAndSendContribution(memberId, contributionPenaltyRatio);
        if (memberPo.isOnline()) {
            ServiceHelper.roleService().notice(memberId, new RoleNotification(
                    new FamilyAuthUpdatedEvent(memberId, 0L, "", 0, FamilyPost.MASSES, auth.getFamilyId())));
            ServiceHelper.roleService().notice(memberId, new LeaveOrKickOutFamilyEvent(canVerify));
            sendText(memberPo.getRoleId(), "family_tips_getoutother");
        }
        recalcForDelMember(data, memberPo);
        // fixme: senderType and senderName
        ServiceHelper.emailService().sendToSingle(memberId, kickOutEmailTemplateId, 0L, familyPo.getName(), null,
                familyPo.getName(), (int) (contributionPenaltyRatio * 100) + "%");

        // 通知其他服务进行登录
        doDelMember(auth.getFamilyId(), memberId);
        ServiceHelper.familyRedPacketService().delMember(auth.getFamilyId(), memberId);
        ServiceHelper.familyEventService().logEvent(auth.getFamilyId(), FamilyEvent.M_KICKOUT, auth.getRoleName(), memberPo.getRoleName());
        ServiceHelper.chatService().delFamilyMemberId(auth.getFamilyId(), memberId);
        // 通知客户端
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_DEL);
        packet.setMemberId(memberPo.getRoleId());
        sendToAllMember(data, packet);
        FamilyLeaveEvent event = new FamilyLeaveEvent(auth.getFamilyId());
        ServiceHelper.familyTaskService().leaveFamilyHandle(auth.getFamilyId(), memberId);
        ServiceHelper.roleService().notice(memberId, event);
        // 日志
        LogUtil.info("家族|提出家族|roleId:{}|familyId:{}|familyName:{}", memberId, familyPo.getFamilyId(), familyPo.getName());
        // 退出家族特性日志
        FamilyLogEvent logEvent = new FamilyLogEvent(FamilyLogEvent.FAMILY_QUIT);
        logEvent.setFamilyId(auth.getFamilyId());
        logEvent.setRoleId(memberId);
        ServiceHelper.roleService().notice(auth.getRoleId(), logEvent);
    }

    @Override
    public void leave(FamilyAuth auth) {
        if (auth == null || !auth.getPost().canLeave()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            LogUtil.error("不存在对应的家族数据");
            return;
        }
        if (globalUnidirectLock.isLock() || data.getFamilyPo().isAllLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        if (!data.getMemberPoMap().containsKey(auth.getRoleId())) {
            LogUtil.error("不存在对应的家族成员数据");
            return;
        }
        FamilyPo familyPo = data.getFamilyPo();
        FamilyMemberPo memberPo = data.getMemberPoMap().remove(auth.getRoleId());
        dao.delete(memberPo);
        familyPo.setMemberCount(data.getMemberPoMap().size());
        dao.update(familyPo);
        boolean canVerify = false;
        FamilyPost post = FamilyPost.postMap.containsKey(memberPo.getPostId()) ? FamilyPost.postMap.get(memberPo.getPostId()) : FamilyPost.ERROR;
        if (post.canVerify()) {
            canVerify = true;
        }
        // 如果有职位则更新相关计数
        if (memberPo.getPostId() == FamilyPost.ASSISTANT_ID) {
            data.decreaseCurrentAssistantCount();
        }
        if (memberPo.getPostId() == FamilyPost.ELDER_ID) {
            data.decreaseCurrentElderCount();
        }
        recalcForDelMember(data, memberPo);
        ServiceHelper.familyRoleService().setFamilyId(auth.getRoleId(), 0L); // delete it
        ServiceHelper.familyRoleService().multiplyAndSendContribution(auth.getRoleId(), contributionPenaltyRatio); // 贡献惩罚
        ServiceHelper.roleService().notice(auth.getRoleId(), new RoleNotification(
                new FamilyAuthUpdatedEvent(auth.getRoleId(), 0L, "", 0, FamilyPost.MASSES, auth.getFamilyId())));
        ServiceHelper.roleService().notice(auth.getRoleId(), new LeaveOrKickOutFamilyEvent(canVerify));
        sendText(auth.getRoleId(), "family_tips_getoutself");
        doDelMember(auth.getFamilyId(), auth.getRoleId());
        // 通知其他服务进行登录
        ServiceHelper.familyRedPacketService().delMember(auth.getFamilyId(), auth.getRoleId());
        ServiceHelper.familyEventService().logEvent(auth.getFamilyId(), FamilyEvent.M_LEAVE, memberPo.getRoleName());
        ServiceHelper.chatService().delFamilyMemberId(auth.getFamilyId(), auth.getRoleId());
        // 通知客户端
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_DEL);
        packet.setMemberId(memberPo.getRoleId());
        sendToAllMember(data, packet);
        FamilyLeaveEvent event = new FamilyLeaveEvent(auth.getFamilyId());
        ServiceHelper.roleService().notice(auth.getRoleId(), event);
        // 日志
        LogUtil.info("家族|退出家族|roleId:{}|familyId:{}|familyName:{}", auth.getRoleId(), auth.getFamilyId(), familyPo.getName());
        // 退出家族特性日志
        FamilyLogEvent logEvent = new FamilyLogEvent(FamilyLogEvent.FAMILY_QUIT);
        logEvent.setFamilyId(auth.getFamilyId());
        logEvent.setRoleId(auth.getRoleId());
        ServiceHelper.roleService().notice(auth.getRoleId(), logEvent);
    }

    @Override
    public void appoint(FamilyAuth auth, long memberId, byte postId) {
        if (auth == null || !auth.getPost().canAppoint()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        if (auth.getRoleId() == memberId) {
            sendText(auth.getRoleId(), I18n.get("family.management.appoint.forbidToAppointSelf"));
            return;
        }
        // 对职位有效性进行验证
        if (!FamilyPost.postMap.containsKey(postId)) {
            sendText(auth.getRoleId(), I18n.get("family.management.appoint.noSuchPost"));
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (globalUnidirectLock.isLock() /*|| data.getFamilyPo().isAllLock()*/) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        if (postId == FamilyPost.MASTER_ID) {
            sendText(auth.getRoleId(), I18n.get("family.management.appoint.forbidToAppointToMaster"));
            return;
        }
        if (postId == FamilyPost.ASSISTANT_ID && data.getCurrentAssistantCount() >= assistantLimit) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_fullpost");
            return;
        }
        if (postId == FamilyPost.ELDER_ID && data.getCurrentElderCount() >= elderLimit) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_fullpost");
            return;
        }
        if (!data.getMemberPoMap().containsKey(memberId)) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
            return;
        }
        FamilyPo familyPo = data.getFamilyPo();
        FamilyMemberPo memberPo = data.getMemberPoMap().get(memberId);
        byte originalPostId = memberPo.getPostId();
        if (originalPostId == postId) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.appoint.forbidToAppointToSamePost"));
            return;
        }
        memberPo.setPostId(postId);
        dao.update(memberPo);
        if (originalPostId == FamilyPost.ASSISTANT_ID) {
            data.decreaseCurrentAssistantCount();
        }
        if (originalPostId == FamilyPost.ELDER_ID) {
            data.decreaseCurrentElderCount();
        }
        if (postId == FamilyPost.ASSISTANT_ID) {
            data.increaseCurrentAssistantCount();
        }
        if (postId == FamilyPost.ELDER_ID) {
            data.increaseCurrentElderCount();
        }
        if (memberPo.isOnline()) {
            ServiceHelper.roleService().notice(memberId, new RoleNotification(
                    new FamilyAuthUpdatedEvent(memberId, auth.getFamilyId(), familyPo.getName(), familyPo.getLevel(), FamilyPost.postMap.get(postId), auth.getFamilyId())));
        }
        if (postId == FamilyPost.ASSISTANT_ID || postId == FamilyPost.ELDER_ID) {
            ServiceHelper.familyEventService().logEvent(auth.getFamilyId(), FamilyEvent.M_APPOINT,
                    auth.getRoleName(), memberPo.getRoleName(), Byte.toString(postId));
        }
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_POST_CHANGED);
        packet.setMemberId(memberId);
        packet.setMemberPostId(postId);
        sendToAllMember(data, packet);
    }

    @Override
    public void abdicate(FamilyAuth auth, long memberId) {
        if (auth == null || !auth.getPost().canAbdicate()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (globalUnidirectLock.isLock() || data.getFamilyPo().isLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        if (!data.getMemberPoMap().containsKey(auth.getRoleId())) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchMaster"));
            return;
        }
        if (!data.getMemberPoMap().containsKey(memberId)) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
            return;
        }
        FamilyPo familyPo = data.getFamilyPo();
        FamilyMemberPo selfPo = data.getMemberPoMap().get(auth.getRoleId());
        FamilyMemberPo memberPo = data.getMemberPoMap().get(memberId);
        // 对成员职位进行判断（禅让对象的职位判断，职位改变时的相关处理）
        byte originalMemberPostId = memberPo.getPostId();
        if (originalMemberPostId == FamilyPost.ASSISTANT_ID) {
            data.decreaseCurrentAssistantCount();
        }
        if (originalMemberPostId == FamilyPost.MEMBER_ID) {
            data.decreaseCurrentElderCount();
        }
        familyPo.setMasterName(memberPo.getRoleName());
        selfPo.setPostId(FamilyPost.MEMBER_ID);
        memberPo.setPostId(FamilyPost.MASTER_ID);
        dao.update(familyPo, selfPo, memberPo);
        ServiceHelper.roleService().notice(auth.getRoleId(), new RoleNotification(
                new FamilyAuthUpdatedEvent(
                        auth.getRoleId(), auth.getFamilyId(), familyPo.getName(), familyPo.getLevel(), FamilyPost.MEMBER, auth.getFamilyId())));
        ServiceHelper.roleService().notice(memberId, new RoleNotification(
                new FamilyAuthUpdatedEvent(
                        memberId, auth.getFamilyId(), familyPo.getName(), familyPo.getLevel(), FamilyPost.MASTER, auth.getFamilyId())));
        // todo: 更新客户端
        // 更新家族显示信息
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_INFO);
        packet.setFamilyData(data);
        sendToAllMember(data, packet);
        // 原族长职位
        packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_POST_CHANGED);
        packet.setMemberId(auth.getRoleId());
        packet.setMemberPostId(FamilyPost.MEMBER_ID);
        sendToAllMember(data, packet);
        // 新族长职位
        packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MEMBER_POST_CHANGED);
        packet.setMemberId(memberId);
        packet.setMemberPostId(FamilyPost.MASTER_ID);
        sendToAllMember(data, packet);

        //可发邮件数
        packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_EMAIL_COUNT);
        packet.setCount((byte) (FamilyManager.emailCount - data.getFamilyPo().getEmailCount()));
        PlayerUtil.send(memberId, packet);
    }

    public void autoAbdicate() {
        for (FamilyData data : onlineDataMap.values()) {
            autoAbdicate(data);
        }
        for (FamilyData data : offlineDataMap.asMap().values()) {
            autoAbdicate(data);
        }
        for (FamilyData data : pendingSavingDataMap.values()) {
            autoAbdicate(data);
        }
    }

    private void autoAbdicate(FamilyData data) {
        if (data.getFamilyPo().isLock()) {
            LogUtil.info("家族[{}]锁定中，不能够禅让", data.getFamilyPo().getFamilyId());
            return;
        }

        long masterId = 0L;
        FamilyMemberPo masterPo = null;
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (memberPo.getPostId() == FamilyPost.MASTER_ID) {
                masterId = memberPo.getRoleId();
                masterPo = memberPo;
                break;
            }
        }

        if (masterPo == null || masterPo.isOnline() || (now() - masterPo.getOfflineTimestamp()) * 1000L < autoAbdicationTimeLimit) {
            return;
        }
        /* 自动禅让（先筛选，再排序，后禅让） */
        Map<Long, FamilyMemberPo> memberPoMap = new HashMap<>(data.getMemberPoMap());
        memberPoMap.remove(masterId); // 提出族长
        // 筛选离线时间
        int now = now();
        Iterator<FamilyMemberPo> it = memberPoMap.values().iterator();
        while (it.hasNext()) {
            FamilyMemberPo memberPo = it.next();
            if (!memberPo.isOnline()
                    && (now - memberPo.getOfflineTimestamp()) * 1000L >= autoAbdicationTimeLimit) {
                it.remove();
            }
        }
        if (memberPoMap.size() > 0) {
            // 排序
            List<FamilyMemberPo> memberPoList = new ArrayList<>(memberPoMap.values());
            Collections.sort(memberPoList, new PostAndOfflineTimestampComparator()); // 排序
            // 选取排名最高的做族长
            FamilyMemberPo memberPo = memberPoList.get(0);
            long familyId = data.getFamilyPo().getFamilyId();
            String familyName = data.getFamilyPo().getName();
            int familyLevel = data.getFamilyPo().getLevel();
            abdicate(new FamilyAuth(familyId, familyName, familyLevel, masterId, masterPo.getRoleName(), FamilyPost.MASTER), memberPo.getRoleId());

            LogUtil.info("触发自动禅让, {} -> {}", masterId, memberPo.getRoleId());
            LogUtil.info("家族|自动禅让|fromRoleId:{}|toRoleId:{}|familyId:{}|familyName:{}", masterId, memberPo.getRoleId(), familyId, familyName);
        }
    }

    @Override
    public void sendUpgradeInfo(FamilyAuth auth) {
        if (!auth.getPost().canUpgrade()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        FamilyPo familyPo = data.getFamilyPo();
        FamilyLevelVo currentLevelVo = levelVoMap.get(familyPo.getLevel());
        if (currentLevelVo == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamilyLevel"));
            return;
        }
        FamilyLevelVo nextLevelVo = levelVoMap.get(currentLevelVo.getLevel() + 1);
        if (nextLevelVo == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.alreadyMaxFamilyLevel"));
            return;
        }
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_UPGRADE_INFO);
        packet.setCurrentLevelVo(currentLevelVo);
        packet.setNextLevelVo(nextLevelVo);
        PlayerUtil.send(auth.getRoleId(), packet);
    }

    @Override
    public void upgrade(FamilyAuth auth) {
        if (!auth.getPost().canUpgrade()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        FamilyPo familyPo = data.getFamilyPo();
        FamilyLevelVo currentLevelVo = levelVoMap.get(familyPo.getLevel());
        if (currentLevelVo == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamilyLevel"));
            return;
        }
        FamilyLevelVo nextLevelVo = levelVoMap.get(currentLevelVo.getLevel() + 1);
        if (nextLevelVo == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.alreadyMaxFamilyLevel"));
            return;
        }
        if (familyPo.getMoney() < nextLevelVo.getRequiredMoney()) {
            sendText(auth.getRoleId(), "common_tips_noreqitem");
            return;
        }
        familyPo.setLevel(nextLevelVo.getLevel());
        familyPo.setMoney(familyPo.getMoney() - nextLevelVo.getRequiredMoney());
        dao.update(familyPo);
        // 发送升级结果
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_UPGRADE, true, null);
        packet.setMaxLevel(!levelVoMap.containsKey(nextLevelVo.getLevel() + 1));
        PlayerUtil.send(auth.getRoleId(), packet);
        // 发送升级产品数据
        if (levelVoMap.containsKey(nextLevelVo.getLevel() + 1)) {
            sendUpgradeInfo(auth);
        }
        // 更新授权信息
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (memberPo.isOnline()) {
                ServiceHelper.roleService().notice(memberPo.getRoleId(), new RoleNotification(
                        new FamilyAuthUpdatedEvent(memberPo.getRoleId(), familyPo.getFamilyId(), familyPo.getName(), familyPo.getLevel(), FamilyPost.postMap.get(memberPo.getPostId()), familyPo.getFamilyId())));
            }
        }
        // 更新家族显示信息
        packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_INFO);
        packet.setFamilyData(data);
        sendToAllMember(data, packet);
    }

    @Override
    public void setApplicationAllowance(FamilyAuth auth, boolean isAllowed) {
        if (auth == null || !auth.getPost().canSetApplicationAllowance()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (data.getFamilyPo().isAllLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        data.getFamilyPo().setAllowApplication((byte) (isAllowed ? 1 : 0));
        dao.update(data.getFamilyPo());

        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_OPTIONS);
        packet.setFamilyData(data);
        PlayerUtil.send(auth.getRoleId(), packet);
    }

    @Override
    public void setApplicationQualification(FamilyAuth auth, int minLevel, int minFightScore, boolean isAutoVerified) {
        if (auth == null || !auth.getPost().canSetApplicationQualification()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (data.getFamilyPo().isAllLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        int maxLevel = Integer.parseInt(DataManager.getCommConfig("family_set_maxlevel"));
        int maxFightScore = Integer.parseInt(DataManager.getCommConfig("family_set_maxpower")) * 10000;
        if (minLevel < 0 || minLevel > maxLevel) {
            sendText(auth.getRoleId(), "family_tips_setoverlv");
            return;
        }
        if (minFightScore < 0 || minFightScore > maxFightScore) {
            sendText(auth.getRoleId(), "family_tips_setoverpower");
            return;
        }
        FamilyPo familyPo = data.getFamilyPo();
        familyPo.setQualificationMinLevel(minLevel);
        familyPo.setQualificationMinFightScore(minFightScore);
        familyPo.setAutoVerified((byte) (isAutoVerified ? 1 : 0));
        dao.update(familyPo);

        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_OPTIONS);
        packet.setFamilyData(data);
        for (Map.Entry<Long, FamilyMemberPo> entry : data.getMasterPoMap().entrySet()) {
            PlayerUtil.send(entry.getKey(), packet);
        }
    }

    @Override
    public void addMoneyAndUpdateContribution(FamilyAuth auth, long roleId, int moneyDelta,
                                              int contributionDelta, int contributionVersion, int rmbDonationDelta) {

        FamilyData data = getData(auth.getFamilyId());
        if (data == null) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        // 更新家族资金
        FamilyPo familyPo = data.getFamilyPo();
        if (familyPo != null) {
            if (familyPo.getMoney() > 0 && familyPo.getMoney() + moneyDelta < 0) {
                familyPo.setMoney(Integer.MAX_VALUE);
            } else {
                familyPo.setMoney(familyPo.getMoney() + moneyDelta);
            }
            dao.update(familyPo);
            // 给所有成员同步家族资金
            ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_MONEY);
            packet.setFamilyData(data);
            sendToAllMember(data, packet);
        } else {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
        }
        // 更新成员贡献记录
        FamilyMemberPo memberPo = data.getMemberPoMap().get(auth.getRoleId());
        if (memberPo != null) {
            memberPo.addContribution(contributionVersion, contributionDelta);
            if (rmbDonationDelta > 0) {
                int oldRmbDonation = memberPo.getRmbDonation();
                int newRmbDonation = oldRmbDonation + rmbDonationDelta;
                int redPacketNumber = newRmbDonation / rpRmbDonationPerRedPacket - oldRmbDonation / rpRmbDonationPerRedPacket;
                memberPo.setRmbDonation(newRmbDonation);
                if (redPacketNumber > 0) {
                    ServiceHelper.familyRedPacketService().addRedPacket(auth, redPacketNumber);
                }
            }
            dao.update(memberPo);
            // todo: sync data
        } else {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
        }

    }

    @Override
    public void poach(FamilyAuth auth, long inviteeId) {
        if (auth == null && !auth.getPost().canInvite()) {
            sendText(inviteeId, "family_tips_nopost");
            return;
        }
        RoleSummaryComponent component = (RoleSummaryComponent) ServiceHelper.summaryService().getOnlineSummaryComponent(inviteeId, MConst.Role);
        if (component == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.reqOtherOnline"));
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (globalUnidirectLock.isLock() || data.getFamilyPo().isAllLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        FamilyLevelVo levelVo = levelVoMap.get(data.getFamilyPo().getLevel());
        if (levelVo == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamilyLevel"));
            return;
        }
        // 判断是否达到申请列表的最大值，以及是否存在
        if (data.getMemberPoMap().size() + data.getPlaceholderMap().size() >= levelVo.getMemberLimit()) {
            sendText(auth.getRoleId(), I18n.get("family.management.alreadyFull"));
            return;
        }
        //
        long oldFamilyId = ServiceHelper.familyRoleService().getFamilyId(inviteeId);
        if (oldFamilyId == 0) {
            sendText(auth.getRoleId(), I18n.get("family.management.reqOtherJoinFamily"));
            return;
        }
        ServiceHelper.familyMainService().innerNotifyPoaching(oldFamilyId, auth.getFamilyId(), auth.getRoleId(), inviteeId);
    }

    @Override
    public void innerNotifyPoaching(long oldFamilyId, long newFamilyId, long inviterId, long inviteeId) {
        FamilyData oldData = getOnlineData(oldFamilyId);
        if (oldData == null) {
            sendText(inviterId, I18n.get("family.management.reqOtherOnline"));
            return;
        }
        FamilyMemberPo inviteeMemberPo = oldData.getMemberPoMap().get(inviteeId);
        if (inviteeMemberPo == null) {
            sendText(inviterId, I18n.get("family.management.noSuchInvitee"));
            return;
        }
        if (!inviteeMemberPo.isOnline()) {
            sendText(inviterId, I18n.get("family.management.reqOtherOnline"));
            return;
        }
        byte inviteePostId = inviteeMemberPo.getPostId();
        if (inviteePostId == FamilyPost.MASTER_ID) {
            sendText(inviterId, I18n.get("family.management.reqOtherIsNotMaster"));
            return;
        }
        ServiceHelper.familyMainService().innerAckPoaching(newFamilyId, inviterId, inviteeId,
                inviteeMemberPo.getJobId(), inviteeMemberPo.getRoleName(), inviteeMemberPo.getRoleLevel(), inviteeMemberPo.getRoleFightScore());
    }

    @Override
    public void innerAckPoaching(long newFamilyId, long inviterId, long inviteeId, int inviteeJobId, String inviteeName, int inviteeLevel, int inviteeFightScore) {
        FamilyData newData = getOnlineData(newFamilyId);
        if (newData == null) {
            sendText(inviterId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyMemberPo inviterPo = newData.getMemberPoMap().get(inviterId);
        if (inviterPo == null) {
            sendText(inviterId, I18n.get("family.management.noSuchInviter"));
            return;
        }
        FamilyApplicationPo applicationPo = newData.getApplicationPoMap().get(inviteeId);
        if (applicationPo == null) {
            applicationPo = newApplicationPo(newFamilyId, inviteeId, inviteeJobId, inviteeName, inviteeLevel, inviteeFightScore);
            applicationPo.setType(FamilyApplicationPo.TYPE_POACHING);
            newData.getApplicationPoMap().put(inviteeId, applicationPo);
            dao.insert(applicationPo);
        } else {
            applicationPo.setType(FamilyApplicationPo.TYPE_POACHING);
            dao.update(applicationPo);
        }
        // 通知受邀者弹框
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_POACH);
        packet.setInvitationFamilyId(newFamilyId);
        packet.setInvitationFamilyName(newData.getFamilyPo().getName());
        packet.setInviterName(inviterPo.getRoleName());
        PlayerUtil.send(inviteeId, packet);
    }

    /*
         * 1. 占坑
         * 2. 删除
         * 3. 新增
         *
         * fixme: 考虑移除出内存的情况
         */
    @Override
    public void acceptPoaching(long newFamilyId, long inviteeId) {
        // 判断是否申请
        FamilyData newData = getData(newFamilyId);
        if (newData == null) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyApplicationPo applicationPo = newData.getApplicationPoMap().get(inviteeId);
        if (applicationPo == null || applicationPo.getType() != FamilyApplicationPo.TYPE_POACHING) {
            sendText(inviteeId, I18n.get("family.management.noSuchApplication"));
            return;
        }
        newData.getApplicationPoMap().remove(inviteeId);
        dao.delete(applicationPo);

        long oldFamilyId = ServiceHelper.familyRoleService().getFamilyId(inviteeId);
        if (oldFamilyId == 0) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        // 占坑
        FamilyPlaceholder placeholder = new FamilyPlaceholder(inviteeId, applicationPo);
        newData.getPlaceholderMap().put(inviteeId, placeholder);

        ServiceHelper.familyMainService().innerNotifyAcceptingPoaching(oldFamilyId, newFamilyId, inviteeId);
    }

    @Override
    public void innerNotifyAcceptingPoaching(long oldFamilyId, long newFamilyId, long inviteeId) {
        // 判断是否申请
        FamilyData oldData = getData(oldFamilyId);
        if (oldData == null) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        //
        FamilyMemberPo inviteeMemberPo = oldData.getMemberPoMap().get(inviteeId);
        if (inviteeMemberPo == null || inviteeMemberPo.getPostId() == FamilyPost.MASTER_ID) {
            sendText(inviteeId, I18n.get("family.management.alreadChangePost"));
            return;
        }
        FamilyMemberPo memberPo = oldData.getMemberPoMap().remove(inviteeId);
        dao.delete(memberPo);
        ServiceHelper.familyRoleService().setFamilyId(inviteeId, 0L); // delete it
        ServiceHelper.roleService().notice(inviteeId, new RoleNotification(
                new FamilyAuthUpdatedEvent(inviteeId, 0L, "", 0, FamilyPost.MASSES, oldFamilyId)));

        ServiceHelper.familyMainService().innerAckAcceptingPoaching(newFamilyId, inviteeId);
    }

    @Override
    public void innerAckAcceptingPoaching(long newFamilyId, long inviteeId) {
        FamilyData newData = getData(newFamilyId);
        if (newData == null) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (!newData.getPlaceholderMap().containsKey(inviteeId)) {
            sendText(inviteeId, I18n.get("family.management.noSuchPlaceholder"));
            return;
        }
        FamilyPlaceholder placeholder = newData.getPlaceholderMap().remove(inviteeId);
        FamilyApplicationPo applicationPo = placeholder.getApplicationPo();
        // 新增家族成员流程
        FamilyMemberPo memberPo = newMemberPo(applicationPo);
        newData.getMemberPoMap().put(memberPo.getRoleId(), memberPo);
        dao.insert(memberPo);
        ServiceHelper.familyRoleService().innerNotifyVerification(inviteeId, applicationPo.getInviterId(), newFamilyId, true);
    }

    @Override
    public void refusePoaching(long newFamilyId, long inviteeId) {
        FamilyData newData = getData(newFamilyId);
        if (newData == null) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyApplicationPo applicationPo = newData.getApplicationPoMap().get(inviteeId);
        if (applicationPo == null || applicationPo.getType() != FamilyApplicationPo.TYPE_POACHING) {
            sendText(inviteeId, I18n.get("family.management.noSuchApplication"));
            return;
        }
        newData.getApplicationPoMap().remove(inviteeId);
        dao.delete(applicationPo);
    }

    @Override
    public void invite(FamilyAuth auth, long inviteeId) {
        ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(inviteeId, MConst.ForeShow);
        if (!fsSummary.isOpen(ForeShowConst.FAMILY)) {
            sendText(auth.getRoleId(), I18n.get("family.management.otherunopen"));
            return;
        }
        if (auth == null && !auth.getPost().canInvite()) {
            sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        RoleSummaryComponent comp = (RoleSummaryComponent) ServiceHelper.summaryService().getOnlineSummaryComponent(inviteeId, MConst.Role);
        if (comp == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.reqOtherOnline"));
            return;
        }
        FamilyData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamily"));
            return;
        }
        if (data.getFamilyPo().isAllLock()) {
            sendText(auth.getRoleId(), "family_tips_lock");
            return;
        }
        FamilyLevelVo levelVo = levelVoMap.get(data.getFamilyPo().getLevel());
        if (levelVo == null) {
            sendText(auth.getRoleId(), I18n.get("family.management.noSuchFamilyLevel"));
            return;
        }
        // 判断是否达到申请列表的最大值，以及是否存在
        if (data.getMemberPoMap().size() + data.getPlaceholderMap().size() >= levelVo.getMemberLimit()) {
            sendText(auth.getRoleId(), I18n.get("family.management.alreadyFull"));
            return;
        }
        FamilyMemberPo memberPo = data.getMemberPoMap().get(auth.getRoleId());
        switch (auth.getPost().getId()) {
            case FamilyPost.MASTER_ID:
                inviteByMaster(auth.getFamilyId(), auth.getRoleId(), inviteeId, data, comp, memberPo.getRoleName());
                break;
            case FamilyPost.ASSISTANT_ID:
            case FamilyPost.ELDER_ID:
                inviteByAssistantOrElder(auth.getFamilyId(), auth.getRoleId(), inviteeId, data, comp, memberPo.getRoleName());
                break;
            case FamilyPost.MEMBER_ID:
                inviteByMember(auth.getFamilyId(), inviteeId, data, memberPo.getRoleName());
                break;
        }
    }

    @Override
    public void acceptInvitation(long familyId, long inviteeId) {
        FamilyData data = getOnlineData(familyId);
        if (data == null) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyApplicationPo applicationPo = data.getApplicationPoMap().get(inviteeId);
        if (applicationPo == null || applicationPo.getType() != FamilyApplicationPo.TYPE_INVITING) {
            sendText(inviteeId, I18n.get("family.management.noSuchApplication"));
            return;
        }
        FamilyLevelVo levelVo = levelVoMap.get(data.getFamilyPo().getLevel());
        if (levelVo == null) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamilyLevel"));
            return;
        }
        if (data.getMemberPoMap().size() + data.getPlaceholderMap().size() >= levelVo.getMemberLimit()) {
            sendText(inviteeId, I18n.get("family.management.alreadyFull"));
            return;
        }
        data.getApplicationPoMap().remove(inviteeId); // 从内存中移除申请数据（处理重复邀请的问题）
        dao.delete(applicationPo);
        FamilyMemberPo memberPo = newMemberPo(applicationPo);
        data.getMemberPoMap().put(memberPo.getRoleId(), memberPo);
        dao.insert(memberPo);
        ServiceHelper.familyRoleService().innerNotifyVerification(inviteeId, applicationPo.getInviterId(), familyId, true);
        doAddMember(familyId, memberPo.copy());
        for (Map.Entry<Long, FamilyMemberPo> entry : data.getMasterPoMap().entrySet()) {
            //家族申请红点
            ServiceHelper.roleService().notice(entry.getValue().getRoleId(), new FamilyRemoveApplyEvent(inviteeId));
        }
        if (ServiceHelper.friendService().checkIsFriend(inviteeId, applicationPo.getInviterId())) {
            FriendLogEvent event = new FriendLogEvent(FriendLogEvent.FAMILY_INVITE);
            event.setFriendId(applicationPo.getInviterId());
            event.setState((byte) 1);
            ServiceHelper.roleService().notice(inviteeId, event);
        }
    }

    @Override
    public void refuseInvitation(long familyId, long inviteeId) {
        FamilyData data = getOnlineData(familyId);
        if (data == null) {
            sendText(inviteeId, I18n.get("family.management.noSuchFamily"));
            return;
        }
        FamilyApplicationPo applicationPo = data.getApplicationPoMap().get(inviteeId);
        if (applicationPo == null || applicationPo.getType() != FamilyApplicationPo.TYPE_INVITING) {
            sendText(inviteeId, I18n.get("family.management.noSuchApplication"));
            return;
        }
        data.getApplicationPoMap().remove(inviteeId);
        dao.delete(applicationPo);
        // todo: 通知客户端

        for (Map.Entry<Long, FamilyMemberPo> entry : data.getMasterPoMap().entrySet()) {
            //家族申请红点
            ServiceHelper.roleService().notice(entry.getValue().getRoleId(), new FamilyRemoveApplyEvent(inviteeId));
        }
        if (ServiceHelper.friendService().checkIsFriend(inviteeId, applicationPo.getInviterId())) {
            FriendLogEvent event = new FriendLogEvent(FriendLogEvent.FAMILY_INVITE);
            event.setFriendId(applicationPo.getInviterId());
            event.setState((byte) 2);
            ServiceHelper.roleService().notice(inviteeId, event);
        }
    }

    @Override
    public void lockFamily(long familyId) {
        if (familyId == 0) {
            LogUtil.info("家族id为0，不能进行锁定");
            return;
        }
        FamilyData data = getData(familyId);
        if (data == null) {
            return;
        }
        data.getFamilyPo().lock();
        // 同步数据
        ServiceHelper.roleService().notice(
                getOnlineMemberIdList(data), new FamilyLockUpdatedEvent(familyId, data.getFamilyPo().getLockState()));
    }

    @Override
    public void halfLockFamily(long familyId) {
        if (familyId == 0) {
            LogUtil.info("家族id为0，不能进行锁定");
            return;
        }
        FamilyData data = getData(familyId);
        if (data == null) {
            return;
        }
        data.getFamilyPo().halfLock();
        // 同步数据
        ServiceHelper.roleService().notice(
                getOnlineMemberIdList(data), new FamilyLockUpdatedEvent(familyId, data.getFamilyPo().getLockState()));
    }

    @Override
    public void unlockFamily(long familyId) {
        if (familyId == 0) {
            LogUtil.info("家族id为0，不能解除锁定");
            return;
        }
        FamilyData data = getData(familyId);
        if (data == null) {
            return;
        }
        data.getFamilyPo().unlock();
        // 同步数据
        ServiceHelper.roleService().notice(
                getOnlineMemberIdList(data), new FamilyLockUpdatedEvent(familyId, data.getFamilyPo().getLockState()));
    }

    @Override
    public void lockGlobalUnidirect(long timeout) {
        globalUnidirectLock.lock(timeout);
//        ServiceHelper.roleService().noticeAll(new FamilyLockUpdatedEvent(true));
    }

    @Override
    public void unlockGlobalUnidirect() {
        globalUnidirectLock.unlock();
//        ServiceHelper.roleService().noticeAll(new FamilyLockUpdatedEvent(false));
    }

    @Override
    public void resetDaily() {
        ClientFamilyManagement cfm = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_EMAIL_COUNT);
        for (FamilyData data : onlineDataMap.values()) {
            for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
                memberPo.expireAndRecalcWeekContribution(System.currentTimeMillis());
            }
            data.getFamilyPo().setEmailCount(0);
            dao.update(data.getFamilyPo());
            long master = 0;
            for (FamilyMemberPo memberPo : data.getMasterPoMap().values()) {
                if (memberPo.getPostId() == FamilyPost.MASTER_ID && memberPo.isOnline()) {
                    master = memberPo.getRoleId();
                }
            }
            cfm.setCount((byte) (FamilyManager.emailCount - data.getFamilyPo().getEmailCount()));
            PlayerUtil.send(master, cfm);
        }
        for (FamilyData data : offlineDataMap.asMap().values()) {
            for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
                memberPo.expireAndRecalcWeekContribution(System.currentTimeMillis());
            }
            data.getFamilyPo().setEmailCount(0);
            dao.update(data.getFamilyPo());
            long master = 0;
            for (FamilyMemberPo memberPo : data.getMasterPoMap().values()) {
                if (memberPo.getPostId() == FamilyPost.MASTER_ID && memberPo.isOnline()) {
                    master = memberPo.getRoleId();
                }
            }
            cfm.setCount((byte) (FamilyManager.emailCount - data.getFamilyPo().getEmailCount()));
            PlayerUtil.send(master, cfm);
        }
        for (FamilyData data : pendingSavingDataMap.values()) {
            for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
                memberPo.expireAndRecalcWeekContribution(System.currentTimeMillis());
            }
            data.getFamilyPo().setEmailCount(0);
            dao.update(data.getFamilyPo());
            long master = 0;
            for (FamilyMemberPo memberPo : data.getMasterPoMap().values()) {
                if (memberPo.getPostId() == FamilyPost.MASTER_ID && memberPo.isOnline()) {
                    master = memberPo.getRoleId();
                }
            }
            cfm.setCount((byte) (FamilyManager.emailCount - data.getFamilyPo().getEmailCount()));
            PlayerUtil.send(master, cfm);
        }

    }

    @Override
    public void sendToOnlineMember(long familyId, Packet packet) {
        FamilyData data = getOnlineData(familyId);
        if (data == null) {
            return;
        }
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (memberPo.isOnline()) {
                PlayerUtil.send(memberPo.getRoleId(), packet);
            }
        }
    }

    @Override
    public void sendEventToOnlineMember(long familyId, Event event) {
        FamilyData data = getOnlineData(familyId);
        if (data == null) {
            return;
        }
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (memberPo.isOnline()) {
                ServiceHelper.roleService().notice(memberPo.getRoleId(), event);
            }
        }
    }

    @Override
    public void sendToOnlineManager(long familyId, Packet packet) {
        FamilyData data = getOnlineData(familyId);
        if (data == null) {
            return;
        }
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            byte postId = memberPo.getPostId();
            if (memberPo.isOnline()
                    && (postId == FamilyPost.MASTER_ID
                    || postId == FamilyPost.ASSISTANT_ID
                    || postId == FamilyPost.ELDER_ID)) {
                PlayerUtil.send(memberPo.getRoleId(), packet);
            }
        }
    }

    @Override
    public void sendEmailToMember(long familyId, long roleId, String title, String text) {
        FamilyData data = getData(familyId);
        if (data == null) {
            sendText(roleId, "家族数据错误");
            return;
        }
        if (text.length() > FamilyManager.maxNumber) {
            sendText(roleId, "family_tips_longinput");
            return;
        }
        if (data.getFamilyPo().getEmailCount() >= FamilyManager.emailCount) {
            sendText(roleId, "family_tips_noallemail");
            return;
        }
        if (!StringUtil.isValidString(text)) {
            sendText(roleId, "不支持字符");
            return;
        }
        List<Long> roleList = new ArrayList<>();
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            roleList.add(memberPo.getRoleId());
        }
        ServiceHelper.emailService().sendTo(roleList, (byte) 0, roleId, data.getFamilyPo().getMasterName(), title, DirtyWords.normalizeChatMessage(text), null);
        sendText(roleId, "family_tips_sendsuccess");
        data.getFamilyPo().setEmailCount(data.getFamilyPo().getEmailCount() + 1);
        dao.update(data.getFamilyPo());
        ClientFamilyManagement clientFamilyManagement = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_EMAIL_COUNT);
        clientFamilyManagement.setCount((byte) (FamilyManager.emailCount - data.getFamilyPo().getEmailCount()));
        PlayerUtil.send(roleId, clientFamilyManagement);
    }

    @Override
    public void sendFamilySocreOpActAwardEmail(long familyId, Map<Byte, Integer> dropMap, int rank) {
        FamilyData data = getData(familyId);
        if (data == null) {
            LogUtil.error("家族战力冲榜开始发奖--没有家族数据");
            return;
        }
        for (Map.Entry<Long, FamilyMemberPo> memberPoEntry : data.getMemberPoMap().entrySet()) {
            FamilyMemberPo member = memberPoEntry.getValue();
            sendFamilySocreOpActAwardEmail(member, dropMap, rank);
        }
    }

    /**
     * 家族战力冲榜开始发奖
     *
     * @param member
     * @param dropMap
     * @param rank
     */
    private void sendFamilySocreOpActAwardEmail(FamilyMemberPo member, Map<Byte, Integer> dropMap, int rank) {
        if (member.getPostId() == FamilyPost.MASSES_ID) {
            LogUtil.error("围观群众直接退出");
            return;
        }
        Map<Integer, Integer> itemMap = getDropMap(dropMap, member.getPostId());
        String postStr = "";
        if (member.getPostId() == FamilyPost.MASTER_ID) {
            postStr = "族长";
        }
        if (member.getPostId() == FamilyPost.ASSISTANT_ID) {
            postStr = "副族长";
        }
        if (member.getPostId() == FamilyPost.ELDER_ID) {
            postStr = "长老";
        }
        if (member.getPostId() == FamilyPost.MEMBER_ID) {
            postStr = "成员";
        }
        ServiceHelper.emailService().sendToSingle(member.getRoleId(), NewServerRankConstant.TYPE_EMAIL_FAMILY_FIGHT_SCORE,
                0L, "系统", itemMap, String.valueOf(rank), postStr);
    }

    private Map<Integer, Integer> getDropMap(Map<Byte, Integer> dropMap, byte post) {
        return DropUtil.executeDrop(dropMap.get(post), 1);
    }

    @Override
    public void resetEmailCount(long familyId) {
        FamilyData data = getData(familyId);
        if (data != null) {
            data.getFamilyPo().setEmailCount(0);
            dao.update(data.getFamilyPo());
        }
    }

    @Override
    public void askMemberCount(long familyId) {
        FamilyData data = getOnlineData(familyId);
        if (data != null) {
            ServiceHelper.familyRedPacketService().updateMemberCount(familyId, data.getMemberPoMap().size());
        }
    }

    @Override
    public List<Long> getOnlineFamilyIdList() {
        return new ArrayList<>(onlineDataMap.keySet());
    }

    @Override
    public FamilyData getFamilyDataClone(long familyId) {
        FamilyData data = getData(familyId);
        if (data != null) {
            try {
                return (FamilyData) data.clone();
            } catch (Exception e) {
                LogUtil.error("", e);
                return null;
            }
        }
        return null;
    }

    private void doAddMember(long familyId, FamilyMemberPo memberPo) {
        Summary summary = ServiceHelper.summaryService().getSummary(memberPo.getRoleId());
        FighterEntity entity = FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(memberPo.getRoleId()));
        if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
            ServiceHelper.familyWarLocalService().addMember(familyId, memberPo, entity);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            MainRpcHelper.familyWarQualifyingService().addMember(FamilyWarUtil.getFamilyWarServerId(), familyId, memberPo, entity);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
            MainRpcHelper.familyWarRemoteService().addMember(FamilyWarUtil.getFamilyWarServerId(), familyId, memberPo, entity);
        }
    }

    private void doDelMember(long familyId, long roleId) {
        if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
            ServiceHelper.familyWarLocalService().delMember(familyId, roleId);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            MainRpcHelper.familyWarQualifyingService().delMember(FamilyWarUtil.getFamilyWarServerId(), familyId, roleId);
        } else if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
            MainRpcHelper.familyWarRemoteService().delMember(FamilyWarUtil.getFamilyWarServerId(), familyId, roleId);
        }
    }

    private FamilyData getData(long familyId) {
        FamilyData data = onlineDataMap.get(familyId);
        if (data != null) {
            return data;
        }
        try {
            data = offlineDataMap.get(familyId);
        } catch (Exception e) {

        }
        return data;
    }

    public FamilyData getOnlineData(long familyId) {
        return onlineDataMap.get(familyId);
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private void sendText(long roleId, String text, String... params) {
        if (roleId > 0) {
            PlayerUtil.send(roleId, new ClientText(text, params));
        }
    }

    private void sendText(long roleId, boolean needSendText, String text, String... params) {
        if (roleId > 0 && needSendText) {
            PlayerUtil.send(roleId, new ClientText(text, params));
        }
    }

    private void sendCommonResp(long roleId, byte subtype, boolean isSuccess, String cause) {
        ClientFamilyManagement packet = new ClientFamilyManagement(subtype, isSuccess, cause);
        PlayerUtil.send(roleId, packet);
    }

    private void sendToAllMember(FamilyData data, Packet packet) {
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (memberPo.isOnline()) {
                PlayerUtil.send(memberPo.getRoleId(), packet);
            }
        }
    }

    private void sendToAllMember(FamilyData data, Packet packet, long omittedId) {
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (memberPo.isOnline() && memberPo.getRoleId() != omittedId) {
                PlayerUtil.send(memberPo.getRoleId(), packet);
            }
        }
    }

    public List<Long> getOnlineMemberIdList(FamilyData data) {
        List<Long> list = new ArrayList<>(data.getMemberPoMap().size());
        for (FamilyMemberPo memberPo : data.getMemberPoMap().values()) {
            if (memberPo.isOnline()) {
                list.add(memberPo.getRoleId());
            }
        }
        return list;
    }

    private FamilyPo newFamilyPo(long familyId, String name, String masterName, String notice) {
        FamilyPo familyPo = new FamilyPo();
        familyPo.setFamilyId(familyId);
        familyPo.setName(name);
        familyPo.setMasterName(masterName);
        familyPo.setLevel(1);
        familyPo.setMoney(0);
        familyPo.setAllowApplication((byte) 1);
        familyPo.setQualificationMinLevel(0);
        familyPo.setQualificationMinFightScore(0);
        familyPo.setAutoVerified((byte) 1);
        familyPo.setCreationTimestamp(now());
        familyPo.setLastActiveTimestamp(now());
        familyPo.setNotice(notice);
        return familyPo;
    }

    private FamilyMemberPo newMemberPo(long familyId, long roleId, int jobId, byte postId, String roleName, int roleLevel, int roleFightScore) {
        FamilyMemberPo memberPo = new FamilyMemberPo();
        memberPo.setFamilyId(familyId);
        memberPo.setRoleId(roleId);
        memberPo.setJobId(jobId);
        memberPo.setPostId(postId);
        memberPo.setRoleName(roleName);
        memberPo.setRoleLevel(roleLevel);
        memberPo.setRoleFightScore(roleFightScore);
        memberPo.setHistoricalContribution(0);
//        memberPo.setCurrentContribution(0);
        memberPo.setWeekContribution("");
        memberPo.setRmbDonation(0);
        memberPo.setJoinTimestamp(now());
        memberPo.setOfflineTimestamp(now());
        return memberPo;
    }

    private FamilyMemberPo newMemberPo(FamilyApplicationPo applicationPo) {
        FamilyMemberPo memberPo = new FamilyMemberPo();
        memberPo.setFamilyId(applicationPo.getFamilyId());
        memberPo.setRoleId(applicationPo.getRoleId());
        memberPo.setJobId(applicationPo.getJobId());
        memberPo.setPostId(FamilyPost.MEMBER_ID);
        memberPo.setRoleName(applicationPo.getRoleName());
        memberPo.setRoleLevel(applicationPo.getRoleLevel());
        memberPo.setRoleFightScore(applicationPo.getRoleFightScore());
        memberPo.setHistoricalContribution(0);
//        memberPo.setCurrentContribution(0);
        memberPo.setWeekContribution("");
        memberPo.setRmbDonation(0);
        memberPo.setJoinTimestamp(now());
        memberPo.setOfflineTimestamp(now());
        return memberPo;
    }

    private FamilyApplicationPo newApplicationPo(long familyId, long roleId, int roleJobId, String roleName, int roleLevel, int roleFightScore) {
        FamilyApplicationPo applicationPo = new FamilyApplicationPo();
        applicationPo.setFamilyId(familyId);
        applicationPo.setRoleId(roleId);
        applicationPo.setJobId(roleJobId);
        applicationPo.setRoleName(roleName);
        applicationPo.setRoleLevel(roleLevel);
        applicationPo.setRoleFightScore(roleFightScore);
        applicationPo.setAppliedTimestamp(now());
        return applicationPo;
    }

    public static RecommendationFamily newRecommendationFamily(FamilyPo familyPo) {
        FamilyLevelVo levelVo = levelVoMap.get(familyPo.getLevel());
        RecommendationFamily recommendation = new RecommendationFamily();
        recommendation.setFamilyId(familyPo.getFamilyId());
        recommendation.setName(familyPo.getName());
        recommendation.setMasterName(familyPo.getMasterName());
        recommendation.setLevel(familyPo.getLevel());
        recommendation.setMemberCount(familyPo.getMemberCount());
        recommendation.setMemberLimit(levelVo != null ? levelVo.getMemberLimit() : 50);
        recommendation.setAllowApplication(familyPo.getAllowApplication());
        recommendation.setTotalFightScore(familyPo.getTotalFightScore());
        recommendation.setQualificationMinLevel(familyPo.getQualificationMinLevel());
        recommendation.setQualificationMinFightScore(familyPo.getQualificationMinFightScore());
        recommendation.setNotice(familyPo.getNotice());
        return recommendation;
    }

    private void recalc(FamilyData data) {
        FamilyPo familyPo = data.getFamilyPo();
        Map<Long, FamilyMemberPo> memberPoMap = data.getMemberPoMap();
        familyPo.setMemberCount(memberPoMap.size()); // 成员人数

        data.setOnlineCount(0);
        data.setCurrentAssistantCount(0);
        data.setCurrentElderCount(0);
        long tmpTotalFightScore = 0L;
        for (FamilyMemberPo memberPo : memberPoMap.values()) {
            tmpTotalFightScore += memberPo.getRoleFightScore();
            if (memberPo.isOnline()) {
                data.increaseOnlineCount();
            }
            if (memberPo.getPostId() == FamilyPost.ASSISTANT_ID) {
                data.increaseCurrentAssistantCount();
            }
            if (memberPo.getPostId() == FamilyPost.ELDER_ID) {
                data.increaseCurrentElderCount();
            }
        }
        familyPo.setTotalFightScore(tmpTotalFightScore);

        ServiceHelper.rankService().updateRank(
                RankConstant.RANK_TYPE_FAMILY, new FamilyRankPo(
                        familyPo.getFamilyId(), familyPo.getName(), familyPo.getMasterName(),
                        familyPo.getLevel(), familyPo.getTotalFightScore()));
    }

    private void recalcForNewMember(FamilyData data, FamilyMemberPo memberPo) {
        FamilyPo familyPo = data.getFamilyPo();
        familyPo.setMemberCount(data.getMemberPoMap().size());
        familyPo.setTotalFightScore(familyPo.getTotalFightScore() + memberPo.getRoleFightScore());

        ServiceHelper.rankService().updateRank(
                RankConstant.RANK_TYPE_FAMILY, new FamilyRankPo(
                        familyPo.getFamilyId(), familyPo.getName(), familyPo.getMasterName(),
                        familyPo.getLevel(), familyPo.getTotalFightScore()));
    }

    private void recalcForDelMember(FamilyData data, FamilyMemberPo memberPo) {
        FamilyPo familyPo = data.getFamilyPo();
        familyPo.setMemberCount(data.getMemberPoMap().size());
        familyPo.setTotalFightScore(familyPo.getTotalFightScore() - memberPo.getRoleFightScore());

        ServiceHelper.rankService().updateRank(
                RankConstant.RANK_TYPE_FAMILY, new FamilyRankPo(
                        familyPo.getFamilyId(), familyPo.getName(), familyPo.getMasterName(),
                        familyPo.getLevel(), familyPo.getTotalFightScore()));
    }

    private void inviteByMaster(long familyId, long inviterId, long inviteeId, FamilyData data, RoleSummaryComponent comp, String inviterName) { // 免资格，免审查
        FamilyApplicationPo applicationPo = data.getApplicationPoMap().get(inviteeId);
        if (applicationPo == null) {
            applicationPo = newApplicationPo(familyId, inviteeId,
                    comp.getRoleJob(), comp.getRoleName(), comp.getRoleLevel(), comp.getFightScore());
            applicationPo.setType(FamilyApplicationPo.TYPE_INVITING);
            applicationPo.setInviterId(inviterId);
            data.getApplicationPoMap().put(inviteeId, applicationPo);
            dao.insert(applicationPo);
        } else {
            applicationPo.setType(FamilyApplicationPo.TYPE_INVITING);
            applicationPo.setInviterId(inviterId);
            dao.update(applicationPo);
        }
        // 通知客户端
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_INVITE);
        packet.setInvitationType(ClientFamilyManagement.INVITATION_BY_MASTER);
        packet.setInvitationFamilyId(familyId);
        packet.setInvitationFamilyName(data.getFamilyPo().getName());
        packet.setInviterName(inviterName); // todo: 邀请者名字
        PlayerUtil.send(inviteeId, packet);
    }

    private void inviteByAssistantOrElder(long familyId, long inviterId, long inviteeId, FamilyData data, RoleSummaryComponent comp, String inviterName) {
        FamilyPo familyPo = data.getFamilyPo();
        // 等级判断
        if (familyPo.getQualificationMinLevel() != 0 && comp.getRoleLevel() < familyPo.getQualificationMinLevel()) {
            sendText(inviterId, I18n.get("family.management.reqOtherMinLevel"));
            return;
        }
        // 战力判断
        if (familyPo.getTotalFightScore() != 0 && comp.getFightScore() < familyPo.getQualificationMinFightScore()) {
            sendText(inviterId, I18n.get("family.management.reqOtherMinFightScore"));
            return;
        }
        FamilyApplicationPo applicationPo = data.getApplicationPoMap().get(inviteeId);
        if (applicationPo == null) {
            applicationPo = newApplicationPo(familyId, inviteeId,
                    comp.getRoleJob(), comp.getRoleName(), comp.getRoleLevel(), comp.getFightScore());
            applicationPo.setType(FamilyApplicationPo.TYPE_INVITING);
            applicationPo.setOptions((byte) FamilyApplicationPo.OPTION_NO_VERIFICATION);
            applicationPo.setInviterId(inviterId);
            data.getApplicationPoMap().put(inviteeId, applicationPo);
            dao.insert(applicationPo);
        } else {
            applicationPo.setType(FamilyApplicationPo.TYPE_INVITING);
            applicationPo.setOptions((byte) FamilyApplicationPo.OPTION_NO_VERIFICATION);
            applicationPo.setInviterId(inviterId);
            dao.update(applicationPo);
        }
        // 通知客户端
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_INVITE);
        packet.setInvitationType(ClientFamilyManagement.INVITATION_BY_ASSISTANT_ELDER);
        packet.setInvitationFamilyId(familyId);
        packet.setInvitationFamilyName(data.getFamilyPo().getName());
        packet.setInviterName(inviterName); // todo: 邀请者名字
        PlayerUtil.send(inviteeId, packet);
    }

    private void inviteByMember(long familyId, long inviteeId, FamilyData data, String inviterName) {
        // 通知客户端
        ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_INVITE);
        packet.setInvitationType(ClientFamilyManagement.INVITATION_BY_MEMBER);
        packet.setInvitationFamilyId(familyId);
        packet.setInvitationFamilyName(data.getFamilyPo().getName());
        packet.setInviterName(inviterName); // todo: 邀请者名字
        PlayerUtil.send(inviteeId, packet);
    }

    class FamilyDataCacheLoader extends CacheLoader<Long, FamilyData> {
        @Override
        public FamilyData load(Long familyId) throws Exception {
            FamilyData data = pendingSavingDataMap.get(familyId);
            if (data != null) {
                pendingSavingDataMap.remove(familyId);
                return data;
            }
            FamilyPo familyPo = DBUtil.queryBean(
                    DBUtil.DB_USER, FamilyPo.class, "select * from `family` where `familyid`=" + familyId);
            Map<Long, FamilyMemberPo> memberPoMap = DBUtil.queryMap(
                    DBUtil.DB_USER, "roleid", FamilyMemberPo.class, "select * from `familymember` where `familyid`=" + familyId);
            Map<Long, FamilyApplicationPo> applicationPoMap = DBUtil.queryMap(
                    DBUtil.DB_USER, "roleid", FamilyApplicationPo.class, "select * from `familyapplication` where `familyid`=" + familyId);

            if (familyPo != null) {
                data = new FamilyData(familyPo, memberPoMap, applicationPoMap);
//                autoAbdicate(data);
                recalc(data);
                return data;
            } else { // 存在错误的数据
                LogUtil.error("不存在familyId, faimlyId=" + familyId, new Exception());
            }
            throw new Exception();
        }
    }

    class PostAndOfflineTimestampComparator implements Comparator<FamilyMemberPo> {
        @Override
        public int compare(FamilyMemberPo m1, FamilyMemberPo m2) {
            // 职位（副组长，长老，成员）
            if (m1.getPostId() != m2.getPostId()) {
                return m1.getPostId() - m2.getPostId();
            }
            // 金币捐献
            if (m1.getRmbDonation() != m2.getRmbDonation()) {
                return -(m1.getRmbDonation() - m2.getRmbDonation());
            }
            // 战力
            return -(m1.getRoleFightScore() - m2.getRoleFightScore());
        }
    }

    @Override
    public void log_family() {
        try {
            Map<Long, FamilyPo> familyMap = DBUtil.queryMap(DBUtil.DB_USER, "familyid",
                    FamilyPo.class, "select * from `family`");
            Iterator<FamilyPo> iterator = familyMap.values().iterator();

            Map<Long, FamilyMemberPo> memberPoMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid",
                    FamilyMemberPo.class, "select * from `familymember`");
            Iterator<FamilyMemberPo> handleIterator = memberPoMap.values().iterator();
            FamilyMemberPo memberPo = null;
            Map<Long, List<FamilyMemberPo>> familyMemberMap = new HashMap<Long, List<FamilyMemberPo>>();
            List<FamilyMemberPo> list = null;
            for (; handleIterator.hasNext(); ) {
                memberPo = handleIterator.next();
                long familyId = memberPo.getFamilyId();
                if (!familyMap.keySet().contains(familyId)) {
                    continue;
                }
                list = familyMemberMap.get(familyId);
                if (list == null) {
                    list = new ArrayList<>();
                    familyMemberMap.put(familyId, list);
                }
                list.add(memberPo);
            }

            FamilyPo familyPo = null;
            int size = 0;
            FamilyData familyData;
            Map<Long, FamilyMemberPo> memberMap;
            Calendar calendar = Calendar.getInstance();
            int todayZeroTime = (int) (DateUtil.getZeroTime(calendar) / 1000);
            for (; iterator.hasNext(); ) {
                familyPo = iterator.next();
                long familyId = familyPo.getFamilyId();
                if (onlineDataMap.containsKey(familyId)) {
                    familyData = onlineDataMap.get(familyId);
                    memberMap = familyData.getMemberPoMap();
                    list = new ArrayList<>(memberMap.values());
                } else {
                    list = familyMemberMap.get(familyId);
                }
                if (list == null) continue;
                //排行数据
                List<AbstractRankPo> rankingList = ServiceHelper.rankService().getRankingList(RankConstant.RANKID_FAMILYFIGHTSCORE);
                Map<Long, Integer> rankMap = new HashMap<Long, Integer>();
                int rankSize = rankingList.size();
                int ranking = 0;
                FamilyRankPo rankPo = null;
                for (int i = 0; i < rankSize; i++) {
                    rankPo = (FamilyRankPo) rankingList.get(i);
                    ranking = i + 1;
                    rankMap.put(rankPo.getFamilyId(), ranking);
                }

                int activeNum = 0;//活跃人数
                long master = 0;
                long roleId = 0;
                Player player = null;
                StringBuffer assistantStr = new StringBuffer();//副族长
                StringBuffer elderStr = new StringBuffer();//元老
                StringBuffer memberStr = new StringBuffer();//普通成员
                size = list.size();
                for (int i = 0; i < size; i++) {
                    memberPo = list.get(i);
                    roleId = memberPo.getRoleId();
                    if (memberPo.getPostId() == FamilyPost.MASTER_ID) {
                        master = roleId;
                    } else if (memberPo.getPostId() == FamilyPost.ASSISTANT_ID) {
                        if (assistantStr.length() == 0) {
                            assistantStr.append(roleId);
                        } else {
                            assistantStr.append("@").append(roleId);
                        }
                    } else if (memberPo.getPostId() == FamilyPost.ELDER_ID) {
                        if (elderStr.length() == 0) {
                            elderStr.append(roleId);
                        } else {
                            elderStr.append("@").append(roleId);
                        }
                    } else if (memberPo.getPostId() == FamilyPost.MEMBER_ID) {
                        if (memberStr.length() == 0) {
                            memberStr.append(roleId);
                        } else {
                            memberStr.append("@").append(roleId);
                        }
                    }
                    player = PlayerSystem.get(roleId);
                    if (player != null || memberPo.getOfflineTimestamp() > todayZeroTime) {
                        activeNum += 1;
                    }
                }
                FamilyLogData data = new FamilyLogData(familyPo, master, assistantStr.toString(), elderStr.toString(),
                        memberStr.toString(), activeNum, rankMap.get(familyId));
                ServerLogModule.log_family(data);
            }
        } catch (SQLException e) {
            LogUtil.error("log_family fail", e);
        }
    }


}
