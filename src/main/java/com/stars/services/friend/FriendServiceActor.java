package com.stars.services.friend;

import com.google.common.cache.*;
import com.stars.ExcutorKey;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.friend.event.*;
import com.stars.modules.friend.packet.ClientBlacker;
import com.stars.modules.friend.packet.ClientContacts;
import com.stars.modules.friend.packet.ClientFriend;
import com.stars.modules.friend.packet.ClientRecommendation;
import com.stars.modules.role.event.FriendGetVigorEvent;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.func.impl.FriendFlowerFunc;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.friend.memdata.FriendData;
import com.stars.services.friend.memdata.RecommendationFriend;
import com.stars.services.friend.summary.FriendFlowerSummaryComponent;
import com.stars.services.friend.summary.FriendFlowerSummaryComponentImpl;
import com.stars.services.friend.userdata.*;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.stars.services.ServiceUtil.sendText;

/**
 * Created by zhaowenshuo on 2016/7/22.
 */
public class FriendServiceActor extends ServiceActor implements FriendService {

    static volatile Map<Integer, List<RecommendationFriend>> onlineCandidateMap;
    static volatile Map<Integer, List<RecommendationFriend>> offlineCandidateMap;
    static volatile boolean isLoadData = false;

    public static int recommLevelRange = 0; // 推荐好友的等级范围
    public static int recommMinLevel = 1; // 推荐好友的最小等级（小于这个值的好友不在推荐列表中）
    public static int recommMaxLevel = 150; // 推荐好友的最小等级（用于推荐好友做边界判断的）
    public static int recommTryTimes = 20; // 随机的尝试次数
    public static int recommListSize = 10; // 推荐列表大小

    public static int[] friendSizeArray;

    public static int blackerMaxSize = 30;

    public static int contactsListSize = 5;

    public static int MAX_RECORD_SIZE = 100;
    public static int FIRST_SEND_FLOWER_MAIL_ID = 19001;
    public static Map<Integer, Integer> FIRST_SEND_FLOWER_AWARD;
    public static Map<Integer, Integer> sendVigorAward;

    private DbRowDao dao;
    private String serviceName;
    private Map<Long, FriendData> onlineDataMap;
    private LoadingCache<Long, FriendData> offlineDataMap; // 离线数据列表
    private Map<Long, FriendData> pendingSavingDataMap; // 保存失败数据列表

    public FriendServiceActor(String id) {
        this.serviceName = "friend service-" + id;
    }

    public FriendServiceActor(int id) {
        this(Integer.toString(id));
    }

    @Override
    public void init() throws Throwable {
        // 加载产品数据
        synchronized (FriendServiceActor.class) {
            if (!isLoadData) {
                SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FriendRecommendation, new RecommendationFriendListUpdateTask(), 0, 5, TimeUnit.SECONDS);
                isLoadData = true;
                LogUtil.info("玩家等级对应好友数量: {}", Arrays.toString(friendSizeArray));
            }
        }
        dao = new DbRowDao(serviceName);
        // 初始化Actor
        ServiceSystem.getOrAdd(serviceName, this);
        onlineDataMap = new HashMap<>();
        offlineDataMap = CacheBuilder.newBuilder()
                .expireAfterAccess(1200, TimeUnit.SECONDS) // 保留20分钟
                .removalListener(new FriendCacheRemovalListener())
                .build(new RoleFriendDataCacheLoader());
        pendingSavingDataMap = new HashMap<>();

    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},onlineCandidateMap.size:{},offlineCandidateMap.size:{},FIRST_SEND_FLOWER_AWARD.size:{}" +
                        ",onlineDataMap.size:{},offlineDataMap.size:{},pendingSavingDataMap.size:{}",
                this.getClass().getSimpleName(),
                onlineCandidateMap == null ? 0 : onlineCandidateMap.size(),
                offlineCandidateMap == null ? 0 : offlineCandidateMap.size(),
                FIRST_SEND_FLOWER_AWARD == null ? 0 : FIRST_SEND_FLOWER_AWARD.size(),
                onlineDataMap == null ? 0 : onlineDataMap.size(),
                offlineDataMap == null ? 0 : offlineDataMap.size(),
                pendingSavingDataMap == null ? 0 : pendingSavingDataMap.size());
    }

    class FriendCacheRemovalListener implements RemovalListener<Long, FriendData> {
        @Override
        public void onRemoval(RemovalNotification<Long, FriendData> notification) {
            if (notification.wasEvicted()) {
                long roleId = notification.getKey();
                FriendData data = notification.getValue();
                Set<DbRow> set = new HashSet<>();
                set.add(data.getRolePo());
                set.add(data.getFriendVigor());
                set.addAll(data.getFriendMap().values());
                set.addAll(data.getContactsMap().values());
                set.addAll(data.getBlackerMap().values());
                set.addAll(data.getApplicationMap().values());
                set.addAll(data.getSendFlowerList());
                set.addAll(data.getReceiveFlowerList());
                if (!dao.isSavingSucceeded(set)) {
                    pendingSavingDataMap.put(roleId, data);
                    LogUtil.info("好友|移除缓存|保存数据失败|roleId:{}|actor:{}", roleId, serviceName);
                } else {
                    LogUtil.info("好友|移除缓存|保存数据成功|roleId:{}|actor:{}", roleId, serviceName);
                }
            }
        }
    }

    class RoleFriendDataCacheLoader extends CacheLoader<Long, FriendData> {
        @Override
        public FriendData load(Long roleId) throws Exception {
            // 从待保存列表找
            FriendData data = pendingSavingDataMap.get(roleId);
            if (data != null) {
                pendingSavingDataMap.remove(roleId);
                return data;
            }
            // 从数据库找
            FriendRolePo rolePo = DBUtil.queryBean(DBUtil.DB_USER, FriendRolePo.class,
                    "select * from `friendrole` where `roleid`=" + roleId);
            if (rolePo == null) {
                return null;
            }
            Map<Long, FriendPo> friendPoMap = DBUtil.queryMap(DBUtil.DB_USER, "friendid", FriendPo.class,
                    "select * from `friend` where `roleid`=" + roleId);
            Map<Long, ContactsPo> contactsPoMap = DBUtil.queryMap(DBUtil.DB_USER, "contactsid", ContactsPo.class,
                    "select * from `friendcontacts` where `roleid`=" + roleId);
            Map<Long, BlackerPo> blackerPoMap = DBUtil.queryMap(DBUtil.DB_USER, "blackerid", BlackerPo.class,
                    "select * from `friendblacker` where `roleid`=" + roleId);
            Map<Long, FriendApplicationPo> applicationMap = DBUtil.queryMap(DBUtil.DB_USER, "applicantid", FriendApplicationPo.class,
                    "select * from `friendapplication` where `objectid`=" + roleId);

            FriendVigorPo friendVigor = DBUtil.queryBean(DBUtil.DB_USER, FriendVigorPo.class,
                    "select * from `friendvigor` where `roleid`=" + roleId);
            List<SendFlowerRecordPo> sendFlowerList = DBUtil.queryList(DBUtil.DB_USER, SendFlowerRecordPo.class,
                    "select * from `sendflowerrecord` where `roleid`=" + roleId + " order by occurtimestamp");
            List<ReceiveFlowerRecordPo> receiveFlowerList = DBUtil.queryList(DBUtil.DB_USER, ReceiveFlowerRecordPo.class,
                    "select * from `receiveflowerrecord` where `roleid`=" + roleId + " order by occurtimestamp");

            if (friendVigor == null) {
                friendVigor = new FriendVigorPo(roleId);
                dao.insert(friendVigor);
            }

            data = new FriendData();
            data.setRolePo(rolePo);
            data.setFriendMap(friendPoMap);
            data.setContactsMap(contactsPoMap);
            data.setBlackerMap(blackerPoMap);
            data.setApplicationMap(applicationMap);
            data.setFriendVigor(friendVigor);
            data.setSendFlowerList(sendFlowerList);
            data.setReceiveFlowerList(receiveFlowerList);

            dailyResetFriendData(data);//日重置检测
            return data;
        }
    }

    @Override
    public void save() {
        dao.flush();
        Iterator<FriendData> it = pendingSavingDataMap.values().iterator();
        while (it.hasNext()) {
            long roleId = 0;
            try {
                FriendData data = it.next();
                roleId = data.getRolePo().getRoleId();
                Set<DbRow> set = new HashSet<>(64);
                set.add(data.getRolePo());
                set.add(data.getFriendVigor());
                set.addAll(data.getFriendMap().values());
                set.addAll(data.getContactsMap().values());
                set.addAll(data.getBlackerMap().values());
                set.addAll(data.getApplicationMap().values());
                set.addAll(data.getSendFlowerList());
                set.addAll(data.getReceiveFlowerList());
                if (dao.isSavingSucceeded(set)) {
                    it.remove();
                    LogUtil.info("好友|pendingSavingData|保存数据成功，roleId={}, actor={}", roleId, serviceName);
                }
                roleId = 0;
            } catch (Throwable t) {
                LogUtil.error("好友|pendingSavingData|保存数据失败，roleId=" + roleId, t);
            }
        }
    }

    @Override
    public void online(long roleId, String roleName, int jobId, int level, int fightScore) {
        // 加载数据
        // todo: 通知其他在线玩家（尽最大努力）
        if (onlineDataMap.containsKey(roleId)) {
            notifyFriendInit(onlineDataMap.get(roleId));
            notifyOnlineOrOffline(onlineDataMap.get(roleId), true);
            return;
        }
        FriendData data = pendingSavingDataMap.get(roleId);
        if (data != null) {
            onlineDataMap.put(roleId, data);
            pendingSavingDataMap.remove(roleId);
            data.setOnline(true);
            notifyFriendInit(data);
            notifyOnlineOrOffline(data, true);
            return;
        }
        data = offlineDataMap.getIfPresent(roleId);
        if (data != null) {
            onlineDataMap.put(roleId, data);
            offlineDataMap.invalidate(roleId);
            data.setOnline(true);
            notifyFriendInit(data);
            notifyOnlineOrOffline(data, true);
            return;
        }
        try {
            FriendRolePo rolePo = DBUtil.queryBean(DBUtil.DB_USER, FriendRolePo.class,
                    "select * from `friendrole` where `roleid`=" + roleId);
            Map<Long, FriendPo> friendPoMap = DBUtil.queryMap(DBUtil.DB_USER, "friendid", FriendPo.class,
                    "select * from `friend` where `roleid`=" + roleId);
            Map<Long, ContactsPo> contactsPoMap = DBUtil.queryMap(DBUtil.DB_USER, "contactsid", ContactsPo.class,
                    "select * from `friendcontacts` where `roleid`=" + roleId);
            Map<Long, BlackerPo> blackerPoMap = DBUtil.queryMap(DBUtil.DB_USER, "blackerid", BlackerPo.class,
                    "select * from `friendblacker` where `roleid`=" + roleId);
            Map<Long, FriendApplicationPo> applicationMap = DBUtil.queryMap(DBUtil.DB_USER, "applicantid", FriendApplicationPo.class,
                    "select * from `friendapplication` where `objectid`=" + roleId);

            FriendVigorPo friendVigor = DBUtil.queryBean(DBUtil.DB_USER, FriendVigorPo.class,
                    "select * from `friendvigor` where `roleid`=" + roleId);
            List<SendFlowerRecordPo> sendFlowerList = DBUtil.queryList(DBUtil.DB_USER, SendFlowerRecordPo.class,
                    "select * from `sendflowerrecord` where `roleid`=" + roleId + " order by occurtimestamp");
            List<ReceiveFlowerRecordPo> receiveFlowerList = DBUtil.queryList(DBUtil.DB_USER, ReceiveFlowerRecordPo.class,
                    "select * from `receiveflowerrecord` where `roleid`=" + roleId + " order by occurtimestamp");

            data = new FriendData();
            if (rolePo == null) {
                rolePo = new FriendRolePo(roleId, roleName, jobId, level, fightScore);
                dao.insert(rolePo);
            }
            if (friendVigor == null) {
                friendVigor = new FriendVigorPo(roleId);
                dao.insert(friendVigor);
            }
            data.setRolePo(rolePo);
            data.setFriendMap(friendPoMap);
            data.setContactsMap(contactsPoMap);
            data.setBlackerMap(blackerPoMap);
            data.setApplicationMap(applicationMap);
            data.setFriendVigor(friendVigor);
            data.setSendFlowerList(sendFlowerList);
            data.setReceiveFlowerList(receiveFlowerList);
            data.setOnline(true);
            onlineDataMap.put(roleId, data);
            notifyFriendInit(data);
            notifyOnlineOrOffline(data, true);

            dailyResetFriendData(data);//日重置检测
            friendRedPoints(roleId);
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public void notifyOnline(List<Long> otherIdList, long roleId) {
        notifyOnlineOrOffline0(otherIdList, roleId, true);
    }

    @Override
    public void offline(long roleId) {
        FriendData data = onlineDataMap.get(roleId);
        if (data != null) {
            data.setOnline(false);
            offlineDataMap.put(roleId, data);
            onlineDataMap.remove(roleId);
            notifyOnlineOrOffline(data, false);
        } else {
            LogUtil.error("没有对应好友信息");
        }
    }

    @Override
    public void notifyOffline(List<Long> otherIdList, long roleId) {
        notifyOnlineOrOffline0(otherIdList, roleId, false);
    }

    @Override
    public void updateRoleLevel(long roleId, int level) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        FriendRolePo rolePo = data.getRolePo();
        if (rolePo == null) {
            return;
        }
        if (rolePo.getLevel() != level) {
            rolePo.setLevel(level);
            dao.update(rolePo);
        } else {
            LogUtil.error("updateRoleLevel(), 没有对应在线好友");
        }
    }

    @Override
    public void updateRoleName(long roleId, String newName) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        FriendRolePo rolePo = data.getRolePo();
        if (rolePo == null) {
            return;
        }
        if (!rolePo.getName().equals(newName)) {
            rolePo.setName(newName);
            dao.update(rolePo);
        } else {
            LogUtil.error("updateRoleLevel(), 没有对应在线好友");
        }
        FriendData friendData = onlineDataMap.get(roleId);
        Map<Long, FriendPo> friendMap = friendData.getFriendMap();
        /**
         * 通知朋友
         */
        for (Long otherRoleId : friendMap.keySet()) {
            ServiceHelper.friendService().notifyFriendChangeName(otherRoleId, roleId, newName);
        }
        /**
         * 通知联系人
         */
        Map<Long, ContactsPo> contactsMap = friendData.getContactsMap();
        for (Long contactId : contactsMap.keySet()) {
            ServiceHelper.friendService().notifyContactChangeName(contactId, roleId, newName);
        }
    }

    @Override
    public void notifyFriendChangeName(Long targetRoleId, Long changeRoleId, String newName) {
        boolean isOnline = true;
        FriendData otherFriendData = onlineDataMap.get(targetRoleId);
        if (otherFriendData == null) {
            isOnline = false;
            otherFriendData = pendingSavingDataMap.get(targetRoleId);
            if (otherFriendData == null) {
                try {
                    otherFriendData = offlineDataMap.get(targetRoleId);
                } catch (ExecutionException e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
        }
        /**
         * 好友列表
         */
        Map<Long, FriendPo> map = new HashMap<>();
        Map<Long, FriendPo> otherFriendMap = otherFriendData.getFriendMap();
        FriendPo friendPo = otherFriendMap.get(changeRoleId);
        if (friendPo != null) {
            friendPo.setFriendName(newName);
            dao.update(friendPo);
            map.put(changeRoleId, friendPo);
        }
        if (isOnline) {
            updateFriendList(targetRoleId, map, otherFriendData.getFriendVigor());
        }

    }

    @Override
    public void notifyContactChangeName(Long targetRoleId, Long changeRoleId, String newName) {
        boolean isOnline = true;
        FriendData otherFriendData = onlineDataMap.get(targetRoleId);
        if (otherFriendData == null) {
            isOnline = false;
            otherFriendData = pendingSavingDataMap.get(targetRoleId);
            if (otherFriendData == null) {
                try {
                    otherFriendData = offlineDataMap.get(targetRoleId);
                } catch (ExecutionException e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
        }
        /**
         * 好友列表
         */
        Map<Long, ContactsPo> otherFriendMap = otherFriendData.getContactsMap();
        ContactsPo contactsPo = otherFriendMap.get(changeRoleId);
        if (contactsPo != null) {
            contactsPo.setContactsName(newName);
            dao.update(contactsPo);
        }
        if (isOnline) {
            ClientContacts clientContacts = new ClientContacts(ClientContacts.SUBTYPE_CONTACTS_NOTIFY_ADD);
            clientContacts.setContactsPo(contactsPo);
            ServiceHelper.roleService().send(targetRoleId, clientContacts);
        }

    }

    @Override
    public void updateRoleFightScore(long roleId, int fightScore) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        FriendRolePo rolePo = data.getRolePo();
        if (rolePo == null) {
            return;
        }
        if (rolePo.getFightScore() != fightScore) {
            rolePo.setLevel(fightScore);
            dao.update(rolePo);
        } else {
            LogUtil.error("updateRoleLevel(), 没有对应在线好友");
        }
    }

    @Override
    public void updateRoleJob(long roleId, int jobId) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        FriendRolePo rolePo = data.getRolePo();
        if (rolePo == null) {
            return;
        }
        if (rolePo.getJobId() != jobId) {
            rolePo.setJobId(jobId);
            dao.update(rolePo);
        } else {
            LogUtil.error("updateRoleJob(), 没有对应在线好友");
        }
    }

    @Override
    public void sendFriendList(long roleId) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_FRIEND_LIST);
        packet.setFriendPoMap(data.getFriendMap());
        packet.setFriendVigorPo(data.getFriendVigor());
        PlayerUtil.send(roleId, packet);
        fireSpecialAccountEvent(roleId, roleId, "发送好友列表", true);
    }

    public FriendPo getFriendPo(long roleId, long friendId) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (null == data || null == data.getFriendMap()) return null;
        if (null == data.getFriendMap().get(friendId)) return null;
        FriendPo po = new FriendPo();
        po.setRoleId(data.getFriendMap().get(friendId).getRoleId());
        po.setFriendId(data.getFriendMap().get(friendId).getFriendId());
        po.setFriendName(data.getFriendMap().get(friendId).getFriendName());
        po.setIntimacy(data.getFriendMap().get(friendId).getIntimacy());
        po.setGetFlowerTimes(data.getFriendMap().get(friendId).getGetFlowerTimes());
        return po;
    }

    private void friendRedPoints(long roleId) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) return;
        FriendVigorPo friendVigor = data.getFriendVigor();
        if (friendVigor != null) {
            //每日最大赠送体力次数
            int dailyReceiveVigorMaxTimes = Integer.parseInt(DataManager.getCommConfig("friend_receivevigor_maxnum"));
            if (friendVigor.getDailyReceiveVigorTimes() < dailyReceiveVigorMaxTimes) {
                ServiceHelper.roleService().notice(roleId, new FriendCanReceiveVigorEvent(true));
            } else {
                ServiceHelper.roleService().notice(roleId, new FriendCanReceiveVigorEvent(false));
            }
        }
        for (Map.Entry<Long, FriendPo> entry : data.getFriendMap().entrySet()) {
            if (entry.getValue().getDailyGetVigorType() == 1) {
                ServiceHelper.roleService().notice(roleId, new FriendAddVigorEvent(entry.getValue().getFriendId()));
            }
        }
        for (Map.Entry<Long, FriendApplicationPo> entry : data.getApplicationMap().entrySet()) {
            ServiceHelper.roleService().notice(roleId, new FriendApplyAddEvent(entry.getValue().getApplicantId()));
        }
    }

    /**
     * 刷新部分好友列表
     * addFlowerCount 送花时特殊处理，更新单个好友时加上送花数据
     */
    private void updateFriendList(long roleId, Map<Long, FriendPo> friendPoMap, FriendVigorPo friendVigorPo, int addFlowerCount) {
        ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_UPDATE_FRIEND_LIST);
        packet.setFriendPoMap(friendPoMap);
        packet.setFriendVigorPo(friendVigorPo);
        packet.setAddFlowerCount(addFlowerCount);
        PlayerUtil.send(roleId, packet);
    }

    /**
     * 刷新部分好友列表
     */
    private void updateFriendList(long roleId, Map<Long, FriendPo> friendPoMap, FriendVigorPo friendVigorPo) {
        updateFriendList(roleId, friendPoMap, friendVigorPo, 0);
    }

    @Override
    public List<Long> getFriendList(long roleId) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(data.getFriendMap().keySet());
    }

    @Override
    public void sendReceivedApplicationList(long roleId) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_APPLICATION_LIST);
        packet.setApplicationPoMap(data.getApplicationMap());
        PlayerUtil.send(roleId, packet);
        fireSpecialAccountEvent(roleId, roleId, "好友申请列表", true);
    }

    /*
     * 申请好友的整体流程
     */
    @Override
    public void applyFriend(long applicantId, long objectId, FriendApplicationPo applicationPo) {
        // 1. 申请方等级
        // 2. 申请方好友数量
        // 3. 申请方是否好友
        // 4. 申请方黑名单
        // 5. 写入已申请列表
        // 6. 通知目标方处理申请
        if (applicantId == objectId) {
            ServiceUtil.sendText(applicantId, I18n.get("friend.apply.forbidToMakeFriendWithSelf"));
            return;
        }
        FriendData applicantData = onlineDataMap.get(applicantId);
        if (applicantData == null) {
            return;
        }
        if (applicantData.getRolePo().getLevel() < 0) {
            ServiceUtil.sendText(applicantId, I18n.get("friend.apply.notEnoughLevel"));
            return;
        }
        int friendSize = getFriendMaxSize(applicantData.getRolePo().getLevel());
        if (applicantData.getFriendMap().size() >= friendSize) {
            ServiceUtil.sendText(applicantId, "friend_applytips_maxnum_self");
            return;
        }
        if (applicantData.getFriendMap().containsKey(objectId)) {
            ServiceUtil.sendText(applicantId, "friend_applytips_added_self");
            return;
        }
        if (applicantData.getBlackerMap().containsKey(objectId)) {
            ServiceUtil.sendText(applicantId, "friend_applytips_blacklist_self");
            return;
        }
        if (applicantData.getAppliedObjectMap().containsKey(objectId)) {
            ServiceUtil.sendText(applicantId, I18n.get("friend.apply.alreadyDone"));
            return;
        }
        ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(objectId, MConst.ForeShow);
        if (fsSummary.isOpen(ForeShowConst.FRIEND)) {
            applicantData.getAppliedObjectMap().put(objectId, objectId);
            ServiceHelper.friendService().innerNotifyApplication(objectId, applicantId, applicationPo);
        } else {
            ServiceUtil.sendText(applicantId, "friend_familyinvite_notopen");
        }
        fireSpecialAccountEvent(applicantId, applicantId, "申请" + objectId + "为好友", true);
        //申请好友 日志
        FriendLogEvent event = new FriendLogEvent(FriendLogEvent.APPLY);
        event.setFriendId(objectId);
        ServiceHelper.roleService().notice(applicantId, event);
    }

    @Override
    public void innerNotifyApplication(long objectId, long applicantId, FriendApplicationPo applicationPo) {
        // 1. 目标方等级
        // 2. 目标方是否好友（直接返回同意）
        // 3. 目标方好友数量
        // 4. 目标方黑名单
        // 5. 目标方的收到的申请列表中
        // 6. 写入好友申请列表
        FriendData objectData = getRoleFriendDataPo(objectId);
        if (objectData.getRolePo().getLevel() < 0) { // 判断目标方等级
            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, "level");
            return;
        }
        if (objectData.getFriendMap().containsKey(applicantId)) { // 判断是否已成为好友（直接同意）
            String friendName = objectData.getFriendMap().get(applicantId).getFriendName();
            ServiceHelper.friendService().innerNotifyAgreement(applicantId, objectId, objectData.getRolePo().getName());
            return;
        }
        int friendSize = getFriendMaxSize(objectData.getRolePo().getLevel());
        if (objectData.getFriendMap().size() >= friendSize) { // 判断目标放的好友数据
            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, "friend_applytips_maxnum_other");
            return;
        }
        if (objectData.getBlackerMap().containsKey(applicantId)) { // 判断是否为目标方黑名单
            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, "friend_applytips_blacklist_other");
            return;
        }
        if (objectData.getApplicationMap().containsKey(applicantId)) { // 判断是否在目标方的收到的申请列表中
            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, I18n.get("friend.apply.alreadyDone"));
            return;
        }
        // 写入申请列表
        objectData.getApplicationMap().put(applicantId, applicationPo);

        /**
         *拷贝一份申请列表数据到module那边
         */
        ServiceHelper.roleService().notice(objectId, new FriendApplyAddEvent(applicantId));

        dao.insert(applicationPo);
        // 如果目标方在线，还要通知他
        if (objectData.isOnline()) {
//            ServiceUtil.sendText(objectId, applicationPo.getApplicantName() + "申请好友");
            ServiceUtil.sendText(objectId, I18n.get("friend.apply.notifyObject", applicationPo.getApplicantName()));
            ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_APPLICATION_NOTIFY_ADD);
            packet.setApplicationPo(applicationPo);
            PlayerUtil.send(objectId, packet);
        }
        // 通知申请方添加成功（假设申请方在线，一般来说都在线）
        ServiceUtil.sendText(applicantId, "friend_addfriend_applysended");
//        ServiceHelper.roleService().notice(objectId, new FriendApplyNewFriendEvent(applicantId));
    }

    private int getFriendMaxSize(int level) {
        int friendSize = friendSizeArray[friendSizeArray.length - 1];
        if (level < friendSizeArray.length) {
            friendSize = friendSizeArray[level];
        }
        return friendSize;
    }

    @Override
    public void agreeApplication(long objectId, long applicantId) {
        // 1. 目标方等级
        // 2. 目标方是否好友（直接返回同意）
        // 3. 目标方好友数量
        // 4. 目标方黑名单
        // 5. 从收到的申请列表中移除，添加到好友列表中
        if (objectId == applicantId) {
            sendText(objectId, I18n.get("friend.agree.forbidToMakeFriendWithSelf"));
            return;
        }
        FriendData objectData = onlineDataMap.get(objectId);
        if (objectData == null) {
            return;
        }
        if (objectData.getApplicationMap().get(applicantId) == null) {
            ServiceUtil.sendText(objectId, I18n.get("friend.agree.noSuchApplication"));
            return;
        }
        if (objectData.getRolePo().getLevel() < 0) {
//            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, "level");
            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, null);
            ServiceUtil.sendText(objectId, "level");
            return;
        }
//        if (objectData.getFriendMap().containsKey(applicantId)) {
//            ServiceHelper.friendService().innerNotifyAgreement(applicantId, objectId);
//            return;
//        }
        int friendSize = getFriendMaxSize(objectData.getRolePo().getLevel());
        if (objectData.getFriendMap().size() >= friendSize) {
//            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, "friend_applytips_maxnum_self");
            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, null);
            ServiceUtil.sendText(objectId, "friend_applytips_maxnum_self");
            return;
        }
        if (objectData.getBlackerMap().containsKey(applicantId)) {
//            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, "friend_applytips_blacklist_self");
            ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, null);
            ServiceUtil.sendText(objectId, "friend_applytips_blacklist_self");
            return;
        }
        // 从收到的申请列表中移除，添加到好友列表中
        FriendApplicationPo applicationPo = objectData.getApplicationMap().remove(applicantId);
        dao.delete(applicationPo);
        if (!objectData.getFriendMap().containsKey(applicantId)) {
            FriendPo friendPo = newFriendPo(objectId, applicantId, applicationPo.getApplicantName());
            objectData.getFriendMap().put(applicantId, friendPo);
            LogUtil.info("同意申请--新增好友----->roleId:" + objectId + "|friendId:" + applicantId);
            dao.insert(friendPo);
        } else {
            ServiceUtil.sendText(objectId, "friend_applytips_added_self");
        }
        // 通知申请方同意添加
        ServiceHelper.friendService().innerNotifyAgreement(applicantId, objectId, objectData.getRolePo().getName());
        /**
         * 拷贝一份申请列表数据到module那边
         */
        ServiceHelper.roleService().notice(objectId, new FriendRemoveApplyEvent(applicantId));
        // 通知客户端（删除申请）
        ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_APPLICATION_NOTIFY_DEL);
        packet.setApplicantId(applicationPo.getApplicantId());
        PlayerUtil.send(objectId, packet);
        fireSpecialAccountEvent(objectId, objectId, "同意" + applicantId + "加为好友", true);
        //添加好友 日志      	
        FriendLogEvent event = new FriendLogEvent(FriendLogEvent.ACCEPT);
        event.setFriendId(applicantId);
        event.setState((byte) 1);
        ServiceHelper.roleService().notice(objectId, event);
    }

    @Override
    public void agreeAllApplication(long objectId) {
        FriendData objectData = onlineDataMap.get(objectId);
        if (objectData == null) {
            return;
        }
        if (objectData.getApplicationMap() == null || objectData.getApplicationMap().size() == 0) {
            ServiceUtil.sendText(objectId, "friend_apply_agree_none");
        } else {
            List<Long> applicantIdList = new ArrayList<>(objectData.getApplicationMap().keySet());
            for (long applicantId : applicantIdList) {
                agreeApplication(objectId, applicantId);
            }
        }
        fireSpecialAccountEvent(objectId, objectId, "同意全部申请列表的加为好友", true);
    }

    @Override
    public void innerNotifyAgreement(long applicantId, long objectId, String objectName) {
        //
        FriendData applicantData = getRoleFriendDataPo(applicantId);
        if (applicantData.getRolePo().getLevel() < 0) {
            ServiceHelper.friendService().innerNotifyAck(objectId, applicantId, false, "level"); // 失败
            return;
        }
//        if (applicantData.getFriendMap().containsKey(objectId)) { // 成功
//            ServiceHelper.friendService().innerNotifyAck(objectId, applicantId, true, null);
//            return;
//        }
        int friendSize = getFriendMaxSize(applicantData.getRolePo().getLevel());
        if (applicantData.getFriendMap().size() >= friendSize) { // 失败
            ServiceHelper.friendService().innerNotifyAck(objectId, applicantId, false, "friend_applytips_maxnum_other");
            return;
        }
        if (applicantData.getBlackerMap().containsKey(objectId)) { // 失败
            ServiceHelper.friendService().innerNotifyAck(objectId, applicantId, false, "friend_applytips_blacklist_other");
            return;
        }
        // 从已申请列表中移除
        applicantData.getAppliedObjectMap().remove(objectId);
        // 入库
        FriendPo friendPo = null;
        if (!applicantData.getFriendMap().containsKey(objectId)) {
            friendPo = newFriendPo(applicantId, objectId, objectName);
            applicantData.getFriendMap().put(objectId, friendPo);
            LogUtil.info("被通知新增好友成功----->roleId:" + applicantId + "|friendId:" + objectId);
            dao.insert(friendPo);
            ServiceHelper.friendService().innerNotifyAck(objectId, applicantId, true, null);

            // 在线，则通知
            if (applicantData.isOnline()) {
                ServiceHelper.roleService().notice(applicantId, new FriendNewFriendEvent(objectId));

                ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_FRIEND_NOTIFY_ADD);
                packet.setFriendPo(friendPo);
                packet.setFriendVigorPo(applicantData.getFriendVigor());
                PlayerUtil.send(applicantId, packet);
            }
        }
//        else {
//            friendPo = applicantData.getFriendMap().get(objectId);
//        }


    }

    @Override
    public void innerNotifyAck(long objectId, long applicantId, boolean isSuccess, String cause) { // 最终一致（过程可能看到不一致的情况）
        FriendData objectData = getRoleFriendDataPo(objectId);
        FriendPo friendPo = objectData.getFriendMap().get(applicantId);
        if (isSuccess) {
            ServiceHelper.roleService().notice(objectId, new FriendNewFriendEvent(applicantId));
            ServiceUtil.sendText(objectId, "friend_addfriend_addsucess", friendPo.getFriendName());
            if (objectData.isOnline() && friendPo != null) {
                ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_FRIEND_NOTIFY_ADD);
                packet.setFriendPo(friendPo);
                packet.setFriendVigorPo(objectData.getFriendVigor());
                PlayerUtil.send(objectId, packet);
            }
        } else {
            if (friendPo != null) {
                objectData.getFriendMap().remove(applicantId);
                LogUtil.info("删除好友---(添加好友失败)--->roleId:" + objectId + "|friendId:" + applicantId);
                dao.delete(friendPo);
            }
            // 如果在线，则通知客户端
            if (objectData.isOnline() && cause != null) {
                ServiceUtil.sendText(objectId, cause);
            }
        }
    }

    @Override
    public void rejectApplication(long objectId, long applicantId) {
        FriendData dataPo = onlineDataMap.get(objectId);
        if (dataPo == null) {
            return;
        }
        // 删除申请列表
        FriendApplicationPo applicationPo = dataPo.getApplicationMap().remove(applicantId);
        if (applicationPo != null) {
            dao.delete(applicationPo);
        }
        // 通知申请方删除
        ServiceHelper.friendService().innerNotifyRejection(applicantId, objectId, null);
        // 通知目标方删除
        if (!dataPo.getFriendMap().containsKey(applicantId)) {
            ServiceUtil.sendText(objectId, "friend_apply_refuse", applicationPo.getApplicantName());
        } else {
            ServiceUtil.sendText(objectId, "friend_applytips_added_self");
        }
        /**
         * 拷贝一份申请列表数据到module那边
         */
        ServiceHelper.roleService().notice(objectId, new FriendRemoveApplyEvent(applicantId));
        //
        ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_APPLICATION_NOTIFY_DEL);
        packet.setApplicantId(applicationPo.getApplicantId());
        PlayerUtil.send(objectId, packet);
        fireSpecialAccountEvent(objectId, objectId, "拒绝" + applicantId + "加为好友", true);
        //拒绝好友 日志
        FriendLogEvent event = new FriendLogEvent(FriendLogEvent.ACCEPT);
        event.setFriendId(applicantId);
        event.setState((byte) 0);
        ServiceHelper.roleService().notice(objectId, event);
    }

    @Override
    public void rejectAllApplication(long objectId) {
        FriendData objectData = onlineDataMap.get(objectId);
        if (objectData == null) {
            return;
        }
        if (objectData.getApplicationMap() == null || objectData.getApplicationMap().size() == 0) {
            ServiceUtil.sendText(objectId, "friend_apply_refuse_none");
        } else {
            List<Long> applicantIdList = new ArrayList<>(objectData.getApplicationMap().keySet());
            for (long applicantId : applicantIdList) {
                rejectApplication(objectId, applicantId);
            }
        }
        fireSpecialAccountEvent(objectId, objectId, "拒绝全部申请列表的加为好友", true);
    }

    @Override
    public void innerNotifyRejection(long applicantId, long objectId, String cause) {
        FriendData applicantData = onlineDataMap.get(applicantId);
        if (applicantData == null) { // 离线不处理
            return;
        }
        // 在线，从已申请的列表移除，通知客户端
        applicantData.getAppliedObjectMap().remove(objectId);
        if (cause != null) {
            ServiceUtil.sendText(applicantId, cause);
        }

    }


    /*
     * 删除
     */
    @Override
    public void deleteFriend(long applicantId, long objectId, boolean needTips) {
        FriendData applicantData = onlineDataMap.get(applicantId);
        if (applicantData == null) {
            return;
        }
        FriendPo friendPo = applicantData.getFriendMap().remove(objectId);
        if (friendPo == null) {
            if (needTips) {
                ServiceUtil.sendText(applicantId, "friend_delete_errortips");
            }
            return;
        }
        LogUtil.info("删除好友----->roleId:" + applicantId + "|friendId:" + objectId);
        dao.delete(friendPo);
        if (needTips) {
            ServiceUtil.sendText(applicantId, "friend_delfriend_tipsui_tips", friendPo.getFriendName());
        }
        ServiceHelper.friendService().innerNotifyDelete(objectId, applicantId);
        if (applicantData.isOnline()) {
            ServiceHelper.roleService().notice(applicantId, new FriendDelFriendEvent(objectId));
            ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_FRIEND_NOTIFY_DEL);
            packet.setFriendId(objectId);
            PlayerUtil.send(applicantId, packet);
        }
        fireSpecialAccountEvent(objectId, applicantId, "同意" + objectId + "加为好友", true);
    }

    /**
     * 赠送体力
     */
    @Override
    public void sendVigor(long roleId, long friendId) {
        /**
         * 不可赠送自己
         */
        if (roleId == friendId) {
            return;
        }
        FriendData applicantData = onlineDataMap.get(roleId);
        if (applicantData == null) return;
        FriendVigorPo friendVigor = applicantData.getFriendVigor();
        if (friendVigor == null) return;

        //每日最大赠送体力次数
        int dailySendVigorMaxTimes = Integer.parseInt(DataManager.getCommConfig("friend_sendvigor_maxnum"));
        if (friendVigor.getDailySendVigorTimes() >= dailySendVigorMaxTimes) {
            ServiceUtil.sendText(roleId, I18n.get("friend.vigor.dailySendMax"));
            return;
        }

        //每日只能给每个玩家赠送一次体力
        if (friendVigor.getDailySendVigorList().contains(friendId)) {
            ServiceUtil.sendText(roleId, I18n.get("friend.vigor.dailyOneTimes"));
            return;
        }

        friendVigor.addDailySendVigorTimes();   //次数+1
        friendVigor.getDailySendVigorList().add(friendId);  //记录赠送名单
        dao.update(friendVigor);

        ServiceUtil.sendText(roleId, I18n.get("friend.vigor.sendSuccess"));
        ;

        // 通知好友改变领取体力状态
        ServiceHelper.friendService().innerNotifySendVigor(friendId, roleId);
        //体力红点
        ServiceHelper.roleService().notice(friendId, new FriendAddVigorEvent(roleId));
        //刷新好友界面
        FriendPo friendPo = applicantData.getFriendMap().get(friendId);
        Map<Long, FriendPo> updateMap = new HashMap<>();
        updateMap.put(friendId, friendPo);
        updateFriendList(roleId, updateMap, friendVigor);
        fireSpecialAccountEvent(roleId, roleId, "赠送体力给" + friendId, true);
        //赠送体力日志
        FriendLogEvent event = new FriendLogEvent(FriendLogEvent.PHYSICAL);
        event.setNum(1);
        event.setFriendId(friendId);
        ServiceHelper.roleService().notice(roleId, event);
    }

    @Override
    public void innerNotifySendVigor(long roleId, long friendId) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) return;
        FriendPo friendPo = data.getFriendMap().get(friendId);
        if (friendPo == null) return;
        friendPo.setDailyGetVigorType((byte) 1);//设置为体力待接收状态
        dao.update(friendPo);

        if (data.isOnline()) {
            //刷新好友界面
            Map<Long, FriendPo> updateMap = new HashMap<>();
            updateMap.put(friendId, friendPo);
            FriendVigorPo friendVigor = data.getFriendVigor();
            updateFriendList(roleId, updateMap, friendVigor);
        }
    }

    /**
     * 一键赠送体力
     */
    @Override
    public void sendAllVigor(long roleId) {
        FriendData applicantData = onlineDataMap.get(roleId);
        if (applicantData == null) return;
        FriendVigorPo friendVigor = applicantData.getFriendVigor();
        if (friendVigor == null) return;

        //每日最大赠送体力次数
        int dailySendVigorMaxTimes = Integer.parseInt(DataManager.getCommConfig("friend_sendvigor_maxnum"));
        if (friendVigor.getDailySendVigorTimes() >= dailySendVigorMaxTimes) {
            ServiceUtil.sendText(roleId, I18n.get("friend.vigor.dailySendMax"));
            return;
        }
        if (StringUtil.isEmpty(applicantData.getFriendMap())) return;

        Map<Long, FriendPo> updateMap = new HashMap<>();//刷新列表
        int sendNum = 0;
        for (FriendPo friendPo : applicantData.getFriendMap().values()) {
            if (friendPo == null) continue;
            if (friendVigor.getDailySendVigorList().contains(friendPo.getFriendId())) continue;

            friendVigor.addDailySendVigorTimes();   //次数+1
            friendVigor.getDailySendVigorList().add(friendPo.getFriendId());  //记录赠送名单
            dao.update(friendVigor);

            updateMap.put(friendPo.getFriendId(), friendPo);//加入刷新列表
            //体力红点
            ServiceHelper.roleService().notice(friendPo.getFriendId(), new FriendAddVigorEvent(roleId));
            // 通知好友改变领取体力状态
            ServiceHelper.friendService().innerNotifySendVigor(friendPo.getFriendId(), roleId);
            sendNum += 1;

            if (friendVigor.getDailySendVigorTimes() >= dailySendVigorMaxTimes) break;//今日次数已满
        }

        ServiceUtil.sendText(roleId, I18n.get("friend.vigor.sendSuccess"));
        fireSpecialAccountEvent(roleId, roleId, "给全部好友赠送体力", true);
        //刷新好友界面
        updateFriendList(roleId, updateMap, friendVigor);
        // 发送奖励给自己

        //赠送体力日志
        FriendLogEvent event = new FriendLogEvent(FriendLogEvent.PHYSICAL);
        event.setNum(sendNum);
        event.setFriendId(0);
        ServiceHelper.roleService().notice(roleId, event);
    }

    /**
     * 接收体力
     */
    @Override
    public void receiveVigor(long roleId, long friendId) {
        FriendData applicantData = onlineDataMap.get(roleId);
        if (applicantData == null) return;
        FriendVigorPo friendVigor = applicantData.getFriendVigor();
        if (friendVigor == null) return;

        //每日最大赠送体力次数
        int dailyReceiveVigorMaxTimes = Integer.parseInt(DataManager.getCommConfig("friend_receivevigor_maxnum"));
        if (friendVigor.getDailyReceiveVigorTimes() >= dailyReceiveVigorMaxTimes) {
            ServiceUtil.sendText(roleId, I18n.get("friend.vigor.dailyReceiveMax"));
            return;
        }

        FriendPo friendPo = applicantData.getFriendMap().get(friendId);
        if (friendPo == null) return;

        if (friendPo.getDailyGetVigorType() != 1) return;

        friendPo.setDailyGetVigorType((byte) 2);//标识为已接收
        friendVigor.addDailyReceiveVigorTimes();//增加接收体力次数


        dao.update(friendPo);
        dao.update(friendVigor);

        int vigor = Integer.parseInt(DataManager.getCommConfig("friend_sendvigor_singlenum"));
        ServiceHelper.roleService().notice(roleId, new FriendGetVigorEvent(vigor));

        //刷新好友界面
        Map<Long, FriendPo> updateMap = new HashMap<>();
        updateMap.put(friendId, friendPo);
        updateFriendList(roleId, updateMap, friendVigor);
        ServiceHelper.roleService().notice(roleId, new FriendRemoveVigorEvent(friendId));//体力红点
        fireSpecialAccountEvent(roleId, roleId, "接收" + friendId + "的体力", true);
    }

    /**
     * 一键接收体力
     */
    @Override
    public void receiveAllVigor(long roleId) {
        FriendData applicantData = onlineDataMap.get(roleId);
        if (applicantData == null) return;
        FriendVigorPo friendVigor = applicantData.getFriendVigor();
        if (friendVigor == null) return;

        //每日最大赠送体力次数
        int dailyReceiveVigorMaxTimes = Integer.parseInt(DataManager.getCommConfig("friend_receivevigor_maxnum"));
        if (friendVigor.getDailyReceiveVigorTimes() >= dailyReceiveVigorMaxTimes) {
            ServiceUtil.sendText(roleId, I18n.get("friend.vigor.dailyReceiveMax"));
            return;
        }
        int basicVigor = Integer.parseInt(DataManager.getCommConfig("friend_sendvigor_singlenum"));
        int vigor = 0;

        Map<Long, FriendPo> updateMap = new HashMap<>();//刷新列表

        //遍历好友列表
        for (FriendPo friendPo : applicantData.getFriendMap().values()) {
            if (friendPo == null || friendPo.getDailyGetVigorType() != 1) continue;
            friendPo.setDailyGetVigorType((byte) 2);//标识为已接收
            friendVigor.addDailyReceiveVigorTimes();//增加接收体力次数
            dao.update(friendPo);
            dao.update(friendVigor);

            vigor += basicVigor;
            updateMap.put(friendPo.getFriendId(), friendPo);
            ServiceHelper.roleService().notice(roleId, new FriendRemoveVigorEvent(friendPo.getFriendId()));//体力红点
            if (friendVigor.getDailyReceiveVigorTimes() >= dailyReceiveVigorMaxTimes) break;//达到最大领取次数
        }
        if (vigor > 0) {
            ServiceHelper.roleService().notice(roleId, new FriendGetVigorEvent(vigor));

        } else {
            PlayerUtil.send(roleId, new ClientText(I18n.get("friend.vigor.haveNone")));
        }
        fireSpecialAccountEvent(roleId, roleId, "接收所有体力", true);
        //刷新好友界面
        updateFriendList(roleId, updateMap, friendVigor);
    }

    @Override
    public void innerNotifyDelete(long objectId, long applicantId) {
        FriendData objectData = getRoleFriendDataPo(objectId);
        FriendPo friendPo = objectData.getFriendMap().remove(applicantId);
        if (friendPo == null) {
            return;
        }
        LogUtil.info("被通知删除好友----->roleId:" + applicantId + "|friendId:" + objectId);
        dao.delete(friendPo);
        if (objectData.isOnline()) {
            ServiceHelper.roleService().notice(objectId, new FriendDelFriendEvent(applicantId));
            ClientFriend packet = new ClientFriend(ClientFriend.SUBTYPE_FRIEND_NOTIFY_DEL);
            packet.setFriendId(applicantId);
            PlayerUtil.send(objectId, packet);
        }
    }

    @Override
    public void sendContactsList(long roleId) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        ClientContacts packet = new ClientContacts(ClientContacts.SUBTYPE_CONTACTS_LIST);
        packet.setContactsMap(data.getContactsMap());
        PlayerUtil.send(roleId, packet);
        fireSpecialAccountEvent(roleId, roleId, "请求好友列表", true);
    }

    @Override
    public void updateContacts(long roleId, long contactsId) {
        if (roleId == contactsId) {
            return;
        }
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        Map<Long, ContactsPo> contactsMap = data.getContactsMap();
        ContactsPo contactsPo = null;
        if (contactsMap.containsKey(contactsId)) { // 存在就更新一下最后联系时间戳
            contactsPo = contactsMap.get(contactsId);
            contactsPo.setLastContactsTimestamp(now());
            dao.update(contactsPo);

        } else { // 不存在就加一个
            if (data.getBlackerMap().containsKey(contactsId)) { // 黑名单忽略
                return;
            }
            if (contactsMap.size() >= contactsListSize) { // 列表太大就淘汰一个
                long minContactsId = 0;
                int minLastContactTimestamp = Integer.MAX_VALUE;
                for (ContactsPo c : contactsMap.values()) {
                    if (c.getLastContactsTimestamp() < minLastContactTimestamp) {
                        minContactsId = c.getContactsId();
                        minLastContactTimestamp = c.getLastContactsTimestamp();
                    }
                }
                dao.delete(contactsMap.remove(minContactsId));
                // 通知客户端
                ClientContacts packet = new ClientContacts(ClientContacts.SUBTYPE_CONTACTS_NOTIFY_DEL);
                packet.setContactsId(minContactsId);
                PlayerUtil.send(roleId, packet);
            }
            contactsPo = newContactsPo(roleId, contactsId, now());
            contactsMap.put(contactsPo.getContactsId(), contactsPo);
            dao.insert(contactsPo);
            // 通知客户端
            ClientContacts packet = new ClientContacts(ClientContacts.SUBTYPE_CONTACTS_NOTIFY_ADD);
            packet.setContactsPo(contactsPo);
            PlayerUtil.send(roleId, packet);
        }
    }

    @Override
    public void sendRecommendationList(long roleId, int level) {
        // 判断等级是否满足条件
        Map<Long, RecommendationFriend> recommendationMap = new HashMap<>();
        FriendData dataPo = onlineDataMap.get(roleId);
        if (dataPo == null || dataPo.getRolePo().getLevel() < recommMinLevel) {
            ServiceUtil.sendText(roleId, I18n.get("friend.recomm.notEnoughLevel"));
            return;
        }
        selectRecommendationFrom(onlineCandidateMap, dataPo, recommendationMap);
        if (recommendationMap.size() < recommListSize) {
            selectRecommendationFrom(offlineCandidateMap, dataPo, recommendationMap);
        }
        for (Long aLong : recommendationMap.keySet()) {
            fireSpecialAccountEvent(roleId, aLong, "出现在好友推荐列表中", false);
        }
        ClientRecommendation packet = new ClientRecommendation();
        packet.setRecommendationMap(recommendationMap);
        packet.setIsRecommend((byte) 1);
        PlayerUtil.send(roleId, packet);
        fireSpecialAccountEvent(roleId, roleId, "请求好友推荐列表", true);
    }

    @Override
    public void searchRole(long roleId, String pattern) {
        // fixme: 先不考虑搜索效率考虑，以及数据独立性的问题（不够时间）
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            ServiceUtil.sendText(roleId, I18n.get("friend.search.noSuchRole"));
            return;
        }
        // 检查pattern
        if (pattern == null || pattern.length() == 0 || pattern.length() > 6 || pattern.matches(".*[';%].*")) {
            ServiceUtil.sendText(roleId, I18n.get("friend.search.illegalText"));
            return;
        }
        Map<Long, RecommendationFriend> recomMap = new HashMap<>();
        // 先查在线
        Iterator<List<RecommendationFriend>> iterator = onlineCandidateMap.values().iterator();
        while (iterator.hasNext()) {
            List<RecommendationFriend> onlineList = iterator.next();
            for (RecommendationFriend recom : onlineList) {
                if (recom.getName().contains(pattern)
                        && recom.getRoleId() != roleId
                        && !data.getBlackerMap().containsKey(recom.getRoleId())
                        && !SpecialAccountManager.isSpecialAccount(recom.getRoleId())) {//2017-03-25 特殊账号不允许被搜到到
                    recomMap.put(recom.getRoleId(), recom);
                }
            }
        }
        if (recomMap.size() < recommListSize) {
            // 再查库
            String sql = "select `roleid`, `name`, `jobid`, `level` from `role` " +
                    "where `name` like '%" + pattern + "%' limit 20";
            try {
                List<_HashMap> offlineList = DBUtil.queryList(DBUtil.DB_USER, _HashMap.class, sql);
                for (_HashMap map : offlineList) {
                    if (roleId != map.getLong("role.roleid")
                            && !data.getBlackerMap().containsKey(map.getLong("role.roleid"))
                            && !SpecialAccountManager.isSpecialAccount(map.getLong("role.roleid"))) {
                        RecommendationFriend recom = new RecommendationFriend();
                        recom.setRoleId(map.getLong("role.roleid"));
                        recom.setName(map.getString("role.name"));
                        recom.setJobId(map.getInt("role.jobid"));
                        recom.setLevel(map.getInt("role.level"));
                        RoleSummaryComponent comp = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                                map.getLong("role.roleid"), SummaryConst.C_ROLE); // 唉
                        recom.setFightScore(comp.getFightScore());
                        recom.setOfflineTimestamp(-1);
                        recomMap.put(recom.getRoleId(), recom);
                    }
                    if (recomMap.size() >= recommListSize) {
                        break;
                    }
                }
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
        for (Long aLong : recomMap.keySet()) {
            fireSpecialAccountEvent(roleId, aLong, "出现在好友搜索中", false);
        }
        PlayerUtil.send(roleId, new ClientRecommendation(recomMap));
        fireSpecialAccountEvent(roleId, roleId, "请求好友搜索", true);
    }

    @Override
    public void sendBlackList(long roleId) {
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        ClientBlacker packet = new ClientBlacker(ClientBlacker.SUBTYPE_BLACKER_LIST);
        packet.setBlackerPoMap(data.getBlackerMap());
        PlayerUtil.send(roleId, packet);
        fireSpecialAccountEvent(roleId, roleId, "请求好友黑名单列表", true);
    }

    private void fireSpecialAccountEvent(long selfId, long roleId, String content, boolean self) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(selfId, new SpecialAccountEvent(roleId, content, self));
        }
    }

    @Override
    public void addToBlackList(long roleId, long blackerId) {
        if (roleId == blackerId) {
            sendText(roleId, I18n.get("friend.blacklist.forbidToAddSelf"));
            return;
        }
        FriendData data = onlineDataMap.get(roleId);
        if (data == null) {
            return;
        }
        if (data.getBlackerMap().size() >= blackerMaxSize) {
            PlayerUtil.send(roleId, new ClientText("friend_blacklist_nummax"));
            return;
        }
        Summary summary = ServiceHelper.summaryService().getSummary(blackerId);
        if (summary == null) return;

        BlackerPo blackerPo = null;
        if (data.getBlackerMap().containsKey(blackerId)) {
            blackerPo = data.getBlackerMap().get(blackerId);
            ServiceUtil.sendText(roleId, "friend_blacklist_tipsui_intips", blackerPo.getBlackerName());
            return;
        }
        blackerPo = newBlackerPo(roleId, blackerId);
        data.getBlackerMap().put(blackerId, blackerPo);
        dao.insert(blackerPo);
        deleteFriend(roleId, blackerId, false);
        deleteContacts(roleId, blackerId, data);
        data.getAppliedObjectMap().remove(blackerId);
        ServiceHelper.roleService().notice(roleId, new FriendNewBlackerEvent(blackerId));
        // 通知客户端
        ClientBlacker packet = new ClientBlacker(ClientBlacker.SUBTYPE_BLACKER_NOTIFY_ADD);
        packet.setBlackerPo(blackerPo);
        PlayerUtil.send(roleId, packet);
        // 提示
        ServiceUtil.sendText(roleId, "friend_blacklist_tipsui_intips", blackerPo.getBlackerName());
        fireSpecialAccountEvent(roleId, roleId, "将" + blackerId + "加入黑名单", true);
        //黑名单 日志
        FriendLogEvent event = new FriendLogEvent(FriendLogEvent.BLACKLIST);
        event.setFriendId(blackerId);
        event.setNum(data.getBlackerMap().size());
        ServiceHelper.roleService().notice(roleId, event);
    }

    @Override
    public void removeFromBlackList(long roleId, long blackerId) {
        FriendData applicantData = onlineDataMap.get(roleId);
        if (applicantData == null) {
            return;
        }
        BlackerPo blackerPo = applicantData.getBlackerMap().remove(blackerId);
        if (blackerPo != null) {
            dao.delete(blackerPo);
            ServiceHelper.roleService().notice(roleId, new FriendDelBlackerEvent(blackerId));
            // 通知客户端
            ClientBlacker blacker = new ClientBlacker(ClientBlacker.SUBTYPE_BLACKER_NOTIFY_DEL);
            blacker.setBlackerId(blackerId);
            PlayerUtil.send(roleId, blacker);
            // 提示
            ServiceUtil.sendText(roleId, "friend_blacklist_tipsui_outtips", blackerPo.getBlackerName());
        }
        fireSpecialAccountEvent(roleId, roleId, "将" + blackerId + "移出黑名单", true);
    }

    private void selectRecommendationFrom(Map<Integer, List<RecommendationFriend>> candidateMap, FriendData data, Map<Long, RecommendationFriend> recommendationMap) {
        long roleId = data.getRolePo().getRoleId();
        int level = data.getRolePo().getLevel();
        // 求出推荐等级范围段的最小值和最大值
        int rangeMinLevel = level - recommLevelRange < recommMinLevel ? recommMinLevel
                : level - recommLevelRange;
        int rangeMaxLevel = level + recommLevelRange;
        if (rangeMinLevel > rangeMaxLevel) {
            return;
        }
        int size = 0;
        for (int i = rangeMinLevel; i <= rangeMaxLevel; i++) {
            List<RecommendationFriend> tempList = candidateMap.get(i);
            if (tempList != null) {
                size += tempList.size();
            }
        }
        List<RecommendationFriend> list = new ArrayList<>(size);
        for (int i = rangeMinLevel; i <= rangeMaxLevel; i++) {
            List<RecommendationFriend> tempList = candidateMap.get(i);
            if (tempList != null) {
                list.addAll(tempList);
            } else {
            }
        }

        Random random = new Random();
        if (list.size() > 0) {
            for (int i = 0; i < recommTryTimes && recommendationMap.size() < recommListSize; i++) {
                int index = random.nextInt(list.size());
                addCandidate(data, recommendationMap, list.get(index));
            }
        }

        if (recommendationMap.size() >= recommListSize) {
            return;
        }


        while (recommendationMap.size() < recommListSize
                && (rangeMinLevel > recommMinLevel || rangeMaxLevel < recommMaxLevel)) {

            if (rangeMinLevel - 1 >= recommMinLevel) {
                rangeMinLevel--;
                List<RecommendationFriend> tempList = candidateMap.get(rangeMinLevel);
                if (tempList != null) {
                    for (int i = 0; i < tempList.size() && recommendationMap.size() < recommListSize; i++) {
                        addCandidate(data, recommendationMap, tempList.get(i));
                    }
                } else {
                }
            }
            if (recommendationMap.size() >= recommListSize) {
                break;
            }
            if (rangeMaxLevel + 1 <= recommMaxLevel) {
                rangeMaxLevel++;
                List<RecommendationFriend> tempList = candidateMap.get(rangeMaxLevel);
                if (tempList != null) {
                    for (int i = 0; i < tempList.size() && recommendationMap.size() < recommListSize; i++) {
                        addCandidate(data, recommendationMap, tempList.get(i));
                    }
                } else {
                }
            }
        }

    }

    private void addCandidate(FriendData dataPo, Map<Long, RecommendationFriend> candidateMap, RecommendationFriend roleMo) {
        if (dataPo.getBlackerMap().containsKey(roleMo.getRoleId())
                || dataPo.getFriendMap().containsKey(roleMo.getRoleId())
                || candidateMap.containsKey(roleMo.getRoleId())
                || dataPo.getRolePo().getRoleId() == roleMo.getRoleId()
                || dataPo.getApplicationMap().containsKey(roleMo.getRoleId())
                || SpecialAccountManager.isSpecialAccount(roleMo.getRoleId())) {//2017-03-25 特殊账号不允许被好友推荐
            return;
        }
        candidateMap.put(roleMo.getRoleId(), roleMo);
    }

    private FriendData getRoleFriendDataPo(long roleId) {
        FriendData friendData = onlineDataMap.get(roleId);
        if (friendData == null) {
            friendData = pendingSavingDataMap.get(roleId);
        }
        if (friendData == null) {
            friendData = offlineDataMap.getUnchecked(roleId);
        }
        return friendData;
    }

    private FriendPo newFriendPo(long roleId, long friendId, String friendName) {
        FriendPo friendPo = new FriendPo();
        friendPo.setRoleId(roleId);
        friendPo.setFriendId(friendId);
//        try {
//            RoleSummaryComponent component = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(friendId, MConst.Role);
//            friendPo.setFriendName(component.getRoleName());
//        } catch (Exception e) {
//            LogUtil.error("", e);
//        } // fixme: 如果处理异常问题
        friendPo.setFriendName(friendName);
        return friendPo;
    }

    private BlackerPo newBlackerPo(long roleId, long blackerId) {
        BlackerPo blackerPo = new BlackerPo();
        blackerPo.setRoleId(roleId);
        blackerPo.setBlackerId(blackerId);
        RoleSummaryComponent component = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(blackerId, MConst.Role);
        blackerPo.setBlackerName(component.getRoleName());
        return blackerPo;
    }

    private ContactsPo newContactsPo(long roleId, long contactsId, int lastContactTimestamp) {
        ContactsPo contactsPo = new ContactsPo();
        contactsPo.setRoleId(roleId);
        contactsPo.setContactsId(contactsId);
        contactsPo.setLastContactsTimestamp(lastContactTimestamp);
        RoleSummaryComponent component = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(contactsId, MConst.Role);
        contactsPo.setContactsName(component.getRoleName());
        return contactsPo;
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private void notifyOnlineOrOffline(FriendData data, boolean isOnline) {
        Set<Long> otherIdSet = new HashSet<>(data.getFriendMap().size() + data.getContactsMap().size());
        otherIdSet.addAll(data.getFriendMap().keySet());
        otherIdSet.addAll(data.getContactsMap().keySet());
        List<Long> otherIdList = new ArrayList<>(otherIdSet);
        if (isOnline) {
            ServiceHelper.friendService().notifyOnline(otherIdList, data.getRolePo().getRoleId());
        } else {
            ServiceHelper.friendService().notifyOffline(otherIdList, data.getRolePo().getRoleId());
        }
    }

    private void notifyOnlineOrOffline0(List<Long> otherIdList, long roleId, boolean isOnline) {
        if (otherIdList == null || otherIdList.size() == 0) {
            return;
        }
        ClientFriend friendPacket = new ClientFriend(
                isOnline ? ClientFriend.SUBTYPE_FRIEND_NOTIFY_ONLINE : ClientFriend.SUBTYPE_FRIEND_NOTIFY_OFFLINE);
        friendPacket.setFriendId(roleId);
        ClientContacts contactsPacket = new ClientContacts(
                isOnline ? ClientContacts.SUBTYPE_CONTACTS_NOTIFY_ONLINE : ClientContacts.SUBTYPE_CONTACTS_NOTIFY_OFFLINE);
        contactsPacket.setContactsId(roleId);

        for (long otherId : otherIdList) {
            FriendData otherData = onlineDataMap.get(otherId);
            if (otherData != null) {
                if (otherData.getFriendMap().containsKey(roleId)) {
                    PlayerUtil.send(otherId, friendPacket);
                } else if (otherData.getContactsMap().containsKey(roleId)) {
                    PlayerUtil.send(otherId, contactsPacket);
                }
            }
        }
    }

    private void notifyFriendInit(FriendData data) {
        ServiceHelper.roleService().notice(data.getRolePo().getRoleId(), new FriendInitEvent(
                new HashSet<Long>(data.getFriendMap().keySet()), new HashSet<Long>(data.getBlackerMap().keySet()), new HashSet<Long>(data.getApplicationMap().keySet())));
    }

    private void deleteContacts(long roleId, long contactsId, FriendData data) {
        ContactsPo contactsPo = data.getContactsMap().remove(contactsId);
        if (contactsPo == null) {
            return;
        }
        dao.delete(contactsPo);
        // 通知客户端
        ClientContacts packet = new ClientContacts(ClientContacts.SUBTYPE_CONTACTS_NOTIFY_DEL);
        packet.setContactsId(contactsId);
        PlayerUtil.send(roleId, packet);
    }

    /**
     * 赠送鲜花
     */
    @Override
    public void sendFlower(long roleId, long friendId, int itemId, int count) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) return;

        if (count <= 0) return;
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) return;

        FriendFlowerFunc func = (FriendFlowerFunc) itemVo.getToolFunc();
        FriendRolePo rolePo = data.getRolePo();
        int addFlowerCount = func.getCount() * count;
        int addIntimacy = func.getIntimacy() * count;
        rolePo.addSendFlower(addFlowerCount);//更新送花数量
        dao.update(rolePo);
        updateSummary(rolePo);//更新常用数据

        RoleSummaryComponent component = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(friendId, MConst.Role);
        if (component != null) {
            SendFlowerRecordPo recordPo = recordSendFlower(data.getSendFlowerList(), roleId, friendId, component.getRoleName(), component.getRoleJob(), component.getRoleLevel(), addFlowerCount);
            //同步客户端
            syncSendFlowerRecord(rolePo, recordPo);

            //下发送花成功展示ui
            sendFlowerSuccessUI(roleId, friendId, component.getRoleName(), component.getRoleJob(), component.getRoleLevel(), component.getFightScore(), addFlowerCount);
        }

        if (rolePo.getDailyFirstSendFlower() == 0) {//今日首次送花,有额外奖励
            rolePo.addDailyFirstSendFlower();

            //发送奖励邮件
            ServiceHelper.emailService().sendToSingle(rolePo.getRoleId(), FIRST_SEND_FLOWER_MAIL_ID, null, "送花管理员", FIRST_SEND_FLOWER_AWARD);
        }

        FriendPo friendPo = data.getFriendMap().get(friendId);
        if (friendPo != null) {//互为好友,增加亲密度
            friendPo.addIntimacy(addIntimacy);
            dao.update(friendPo);

            //推送刷新
            Map<Long, FriendPo> updateMap = new HashMap<>();
            updateMap.put(friendId, friendPo);
            updateFriendList(roleId, updateMap, data.getFriendVigor(), addFlowerCount);
            //送花 日志
            FriendLogEvent event = new FriendLogEvent(FriendLogEvent.FLOWER);
            event.setFriendId(friendId);
            event.setNum(count);
            byte state = 1;
            int nomalCount = func.getCount();
            if (nomalCount == 99) {
                state = 3;
            } else if (func.getCount() == 9) {
                state = 2;
            }
            event.setState(state);
            event.setFriendShip(friendPo.getIntimacy());
            ServiceHelper.roleService().notice(roleId, event);
        }
        //通知收花
        ServiceHelper.friendService().innerNotifyReceiveFlower(friendId, rolePo.getRoleId(), rolePo.getName(), rolePo.getJobId(), rolePo.getLevel(), addFlowerCount, addIntimacy);
    }

    //下发送花成功展示ui
    private void sendFlowerSuccessUI(long roleId, long friendId, String friendName, int jobId, int level, int fighting, int addFlowerCount) {
        ClientFriend client = new ClientFriend(ClientFriend.SUBTYPE_SEND_FLOWER_SUCCESS_UI);
        client.setAddCount(addFlowerCount);
        client.setName(friendName);
        client.setFighting(fighting);
        client.setLevel(level);
        client.setJob(jobId);

        //历史收花数量
        int flowerCount = 0;
        FriendFlowerSummaryComponent flowerSummary = (FriendFlowerSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(friendId, SummaryConst.C_FRIEND_FLOWER);
        if (flowerSummary != null) {
            flowerCount = flowerSummary.getReceiveFlowerCount();
        }
        client.setFlowerCount(flowerCount);

        //公会名称
        String familyName = "";
        FamilySummaryComponent comp = (FamilySummaryComponent) ServiceHelper.summaryService().getSummaryComponent(friendId, SummaryConst.C_FAMILY);
        if (comp != null) {
            familyName = comp.getFamilyName();
        }

        client.setFamilyName(familyName);
        PlayerUtil.send(roleId, client);
    }

    public void innerNotifyReceiveFlower(long roleId, long friendId, String friendName, int jobId, int level, int addFlowerCount, int addIntimacy) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) return;

        FriendRolePo rolePo = data.getRolePo();
        rolePo.addReceiveFlower(addFlowerCount);
        dao.update(rolePo);
        updateSummary(rolePo);//更新常用数据

        FriendPo friendPo = data.getFriendMap().get(friendId);
        if (friendPo != null) {//互为好友,增加亲密度
            friendPo.addIntimacy(addIntimacy);
            friendPo.addGetFlower(addFlowerCount);
            dao.update(friendPo);
        }
        //生成收花记录
        ReceiveFlowerRecordPo receiveRecord = recordReceiveFlower(data.getReceiveFlowerList(), roleId, friendId, friendName, jobId, level, addFlowerCount);
        if (data.isOnline()) {
            //刷新好友信息
            if (friendPo != null) {
                //推送刷新
                Map<Long, FriendPo> updateMap = new HashMap<>();
                updateMap.put(friendId, friendPo);
                updateFriendList(roleId, updateMap, data.getFriendVigor());
            }
            ServiceHelper.roleService().notice(roleId, new FriendReceiveFlowerEvent(rolePo, receiveRecord));
        }
    }

    private Set<Long> getReceiveTimeSet(List<ReceiveFlowerRecordPo> list) {
        Set<Long> set = new HashSet<>();
        if (StringUtil.isNotEmpty(list)) {
            for (ReceiveFlowerRecordPo po : list) {
                set.add(po.getOccurTimestamp());
            }
        }
        return set;
    }

    private ReceiveFlowerRecordPo recordReceiveFlower(List<ReceiveFlowerRecordPo> list, long roleId, long friendId, String friendName, int jobId, int level, int addFlowerCount) {
        ReceiveFlowerRecordPo recordPo = new ReceiveFlowerRecordPo(roleId, friendId, friendName, addFlowerCount, jobId, level);
        Set<Long> timeSet = getReceiveTimeSet(list);
        while (timeSet.contains(recordPo.getOccurTimestamp())) {
            recordPo.addOccurTimestamp();
        }
        list.add(recordPo);
        if (list.size() > MAX_RECORD_SIZE) {//记录只保留100条
            Collections.sort(list);
            ReceiveFlowerRecordPo deletePo = list.remove(list.size() - 1);
            dao.delete(deletePo);
        }
        dao.insert(recordPo);
        return recordPo;
    }

    private Set<Long> getSendTimeSet(List<SendFlowerRecordPo> list) {
        Set<Long> set = new HashSet<>();
        if (StringUtil.isNotEmpty(list)) {
            for (SendFlowerRecordPo po : list) {
                set.add(po.getOccurTimestamp());
            }
        }
        return set;
    }

    private SendFlowerRecordPo recordSendFlower(List<SendFlowerRecordPo> list, long roleId, long friendId, String friendName, int jobId, int level, int addFlowerCount) {
        SendFlowerRecordPo recordPo = new SendFlowerRecordPo(roleId, friendId, friendName, addFlowerCount, jobId, level);
        Set<Long> timeSet = getSendTimeSet(list);
        while (timeSet.contains(recordPo.getOccurTimestamp())) {
            recordPo.addOccurTimestamp();
        }
        list.add(recordPo);
        if (list.size() > MAX_RECORD_SIZE) {//记录只保留100条
            Collections.sort(list);
            SendFlowerRecordPo deletePo = list.remove(list.size() - 1);
            dao.delete(deletePo);
        }
        dao.insert(recordPo);
        return recordPo;
    }

    /**
     * 查看收/送花记录界面
     */
    public void viewFriendFlowerUI(long roleId) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) return;

        FriendRolePo rolePo = data.getRolePo();
        if (rolePo == null) return;

        ClientFriend client = new ClientFriend(ClientFriend.SUBTYPE_VIEW_FLOWER_RECORD);
        client.setSendFlower(rolePo.getSendFlower());
        client.setReceiveFlower(rolePo.getReceiveFlower());
        client.setReceiveFlowerList(data.getReceiveFlowerList());
        client.setSendFlowerList(data.getSendFlowerList());
        PlayerUtil.send(roleId, client);
        fireSpecialAccountEvent(roleId, roleId, "查看收/送花记录界面", true);
    }

    public void syncSendFlowerRecord(FriendRolePo rolePo, SendFlowerRecordPo recordPo) {
        ClientFriend client = new ClientFriend(ClientFriend.SUBTYPE_SYN_SEND_FLOWER_RECORD);
        client.setSendFlower(rolePo.getSendFlower());
        client.setReceiveFlower(rolePo.getReceiveFlower());
        client.setSendFlowerRecord(recordPo);
        PlayerUtil.send(rolePo.getRoleId(), client);
    }

    public void dailyReset() {
        long now = System.currentTimeMillis();
        for (FriendData friendData : onlineDataMap.values()) {
            dailyResetFriendData(now, friendData);
        }

        for (FriendData friendData : offlineDataMap.asMap().values()) {
            dailyResetFriendData(now, friendData);
        }

        for (FriendData friendData : pendingSavingDataMap.values()) {
            dailyResetFriendData(now, friendData);
        }
    }

    private void dailyResetFriendData(long now, FriendData friendData) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        int nowDay = calendar.get(Calendar.DAY_OF_YEAR);


        calendar.setTimeInMillis(friendData.getRolePo().getLastDailyResetTime());
        int lastResetDay = calendar.get(Calendar.DAY_OF_YEAR);

        if (nowDay != lastResetDay) {//不是同一天,执行日重置
            friendData.getRolePo().setLastDailyResetTime(now);
            friendData.getRolePo().resetDailyFirstSendFlower();
            dao.update(friendData.getRolePo());

            if (friendData.getFriendVigor().dailyReset()) {
                dao.update(friendData.getFriendVigor());
            }

            Map<Long, FriendPo> updateMap = new HashMap<>();
            for (FriendPo friendPo : friendData.getFriendMap().values()) {
                if (friendPo.dailyReset()) {
                    dao.update(friendPo);
                }
                updateMap.put(friendPo.getFriendId(), friendPo);
            }

            if (friendData.isOnline()) {//玩家在线,推送重置后信息
                updateFriendList(friendData.getRolePo().getRoleId(), updateMap, friendData.getFriendVigor());
                ServiceHelper.roleService().notice(friendData.getRolePo().getRoleId(), new FriendRemoveAllVigorEvent());
            }
        }
    }

    private void dailyResetFriendData(FriendData friendData) {
        long now = System.currentTimeMillis();
        dailyResetFriendData(now, friendData);
    }

    /**
     * 送花界面(选择鲜花)
     */
    public void openSendFlowerUI(long roleId) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) return;

        FriendRolePo rolePo = data.getRolePo();
        if (rolePo == null) return;

        ClientFriend client = new ClientFriend(ClientFriend.SUBTYPE_SEND_FLOWER_UI);
        client.setDailyFirstSendFlower(rolePo.getDailyFirstSendFlower());
        PlayerUtil.send(roleId, client);
        fireSpecialAccountEvent(roleId, roleId, "送花界面(选择鲜花)", true);
    }

    private void updateSummary(FriendRolePo friendRolePo) {
        if (friendRolePo == null) return;
        try {
            ServiceHelper.summaryService().updateOfflineSummaryComponent(friendRolePo.getRoleId(), new FriendFlowerSummaryComponentImpl(friendRolePo.getSendFlower(), friendRolePo.getReceiveFlower()));
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    /**
     * 上线登陆更新送鲜花常用数据
     */
    public void onUpdateSummary(long roleId) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) return;
        FriendRolePo rolePo = data.getRolePo();
        if (rolePo == null) return;
        updateSummary(rolePo);//更新
    }

    /**
     * 检测
     */
    public boolean checkIsFriend(long roleId, long friendId) {
        FriendData data = getRoleFriendDataPo(roleId);
        if (data == null) return false;
        if (data.getFriendMap().containsKey(friendId)) {
            return true;
        }
        return false;
    }
}
