package com.stars.services.family.role;

import com.google.common.cache.*;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.family.FamilyManager;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.family.event.FamilyContributionEvent;
import com.stars.modules.family.packet.ClientFamilyContribution;
import com.stars.modules.family.packet.ClientFamilyRecommendation;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.main.FamilyMainServiceActor;
import com.stars.services.family.main.memdata.RecommendationFamily;
import com.stars.services.family.main.userdata.FamilyPo;
import com.stars.services.family.role.userdata.FamilyRoleApplicationPo;
import com.stars.services.family.role.userdata.FamilyRolePo;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class FamilyRoleServiceActor extends ServiceActor implements FamilyRoleService {

    private DbRowDao dao;
    private String serviceName;
    private Map<Long, FamilyRoleData> onlineDataMap;
    private LoadingCache<Long, FamilyRoleData> offlineDataMap;
    private Map<Long, FamilyRoleData> pendingSavingDataMap;

    public FamilyRoleServiceActor(String id) {
        this.serviceName = "family role service-" + id;
    }

    public FamilyRoleServiceActor(int id) {
        this(Integer.toString(id));
    }

    @Override
    public void init() throws Throwable {
        this.dao = new DbRowDao(serviceName);
        ServiceSystem.getOrAdd(serviceName, this);
        onlineDataMap = new HashMap<>();
        offlineDataMap = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(1800, TimeUnit.SECONDS)
                // todo: removalListener
                .removalListener(new RemovalListener<Long, FamilyRoleData>() {
                    @Override
                    public void onRemoval(RemovalNotification<Long, FamilyRoleData> notification) {
                        if (notification.wasEvicted()) {
                            Set<DbRow> set = new HashSet<DbRow>();
                            set.add(notification.getValue().getRolePo());
                            set.addAll(notification.getValue().getApplicationPoMap().values());
                            if (!dao.isSavingSucceeded(set)) {
                                pendingSavingDataMap.put(notification.getKey(), notification.getValue());
                                LogUtil.error("family - role缓存移除异常，roleId=" + notification.getKey());
                            }
                        }
                    }
                })
                .build(new FamilyRoleDataCacheLoader());
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
    public void online(long roleId) {
        FamilyRoleData data = onlineDataMap.get(roleId);
        if (data == null && pendingSavingDataMap.containsKey(roleId)) {
            data = pendingSavingDataMap.remove(roleId);
            onlineDataMap.put(roleId, data);
        }
        if (data == null && offlineDataMap.getIfPresent(roleId) != null) {
            data = offlineDataMap.getIfPresent(roleId);
            offlineDataMap.invalidate(roleId);
            onlineDataMap.put(roleId, data);
        }
        if (data == null) {
            try {
                FamilyRolePo rolePo = DBUtil.queryBean(DBUtil.DB_USER, FamilyRolePo.class,
                        "select * from `familyrole` where `roleid`=" + roleId);
                Map<Long, FamilyRoleApplicationPo> applicationPoMap = DBUtil.queryMap(DBUtil.DB_USER, "familyid", FamilyRoleApplicationPo.class,
                        "select * from `familyroleapplication` where `roleid`=" + roleId);
                if (rolePo != null) {
                    data = new FamilyRoleData(rolePo, applicationPoMap);
                } else {
                    data = new FamilyRoleData(new FamilyRolePo(roleId, 0, 0), applicationPoMap);
                    dao.insert(data.getRolePo());
                }
                onlineDataMap.put(roleId, data);
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }

        LogUtil.info("FamilyRoleServiceActor - roleId:{}, familyId:{}", data.getRolePo().getRoleId(), data.getRolePo().getFamilyId());

        PlayerUtil.send(roleId, new ClientFamilyContribution(data.getRolePo().getContribution()));
        if (data.getRolePo().getFamilyId() > 0) {
            ServiceHelper.familyMainService().online(data.getRolePo().getFamilyId(), roleId, true);
        } else {
            ServiceHelper.roleService().notice(
                    roleId, new FamilyAuthUpdatedEvent(
                            FamilyAuthUpdatedEvent.TYPE_LOGIN, roleId, 0, "", 0, FamilyPost.MASSES, 0));
        }
    }

    @Override
    public void offline(long roleId) {
        FamilyRoleData data = onlineDataMap.remove(roleId);
        if (data != null) {
            offlineDataMap.put(roleId, data);
            if (data.getRolePo().getFamilyId() > 0) {
                ServiceHelper.familyMainService().offline(data.getRolePo().getFamilyId(), roleId);
            }
        }
    }

    @Override
    public FamilyAuth getFamilyAuth(long roleId) {
        FamilyRoleData data = getData(roleId);
        if (data != null) {
            if (data.getRolePo().getFamilyId() <= 0) {
                getCurrentFuture().set(new FamilyAuth(0, "", 0, roleId, null, FamilyPost.MASSES));
            } else {
                ServiceHelper.familyMainService().getFamilyAuth(
                        data.getRolePo().getFamilyId(), roleId, getCurrentFuture());
            }
        }
        return null; // 因为异步委托，所以返回值是不会设置到InvocationFuture中的
    }

    @Override
    public long getFamilyId(long roleId) {
        FamilyRoleData data = getData(roleId);
        if (data != null) {
            return data.getRolePo().getFamilyId();
        }
        return 0;
    }

    @Override
    public void setFamilyId(long roleId, long familyId) {
        FamilyRoleData data = getData(roleId);
        if (data != null) {
            data.getRolePo().setFamilyId(familyId);
            dao.update(data.getRolePo());
            if (familyId > 0) {
                cancelOtherApplication(data);
            }
        }
    }

    @Override
    public boolean compareAndSetFamilyId(long roleId, long oldFamilyId, long newFamilyId) {
        FamilyRoleData data = getData(roleId);
        if (data != null) {
            if (data.getRolePo().getFamilyId() == oldFamilyId) {
                data.getRolePo().setFamilyId(newFamilyId);
                dao.update(data.getRolePo());
                if (newFamilyId > 0) {
                    cancelOtherApplication(data);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getContribution(long roleId) {
        FamilyRoleData data = getData(roleId);
        if (data != null) {
            return data.getRolePo().getContribution();
        }
        return 0;
    }

    @Override
    public boolean addAndSendContribution(long roleId, int delta) {
        FamilyRoleData data = getData(roleId);
        if (data != null) {
            int contribution = data.getRolePo().getContribution();
            if (contribution + delta >= 0) {
                data.getRolePo().setContribution(contribution + delta);
                dao.update(data.getRolePo());
                //红点逻辑
                ServiceHelper.roleService().notice(roleId, new FamilyContributionEvent());
                PlayerUtil.send(roleId, new ClientFamilyContribution(data.getRolePo().getContribution()));
                return true;
            } else {
                return false;
            }

        }
        return false;
    }

    @Override
    public void multiplyAndSendContribution(long roleId, double factor) {
        if (factor > 1.0) {
            LogUtil.error("帮贡扣除百分比大于100%");
            return;
        }
        FamilyRoleData data = getData(roleId);
        if (data != null) {
            int contribution = data.getRolePo().getContribution();
            data.getRolePo().setContribution((int) Math.floor(contribution * (1.0 - factor)));
            dao.update(data.getRolePo());
            //红点逻辑
            ServiceHelper.roleService().notice(roleId, new FamilyContributionEvent());
            PlayerUtil.send(roleId, new ClientFamilyContribution(data.getRolePo().getContribution()));
        }
    }

    @Override
    public void innerNotifyVerification(long applicantId, long verifierId, long familyId, boolean isOk) {
        // todo: 处理申请列表
        FamilyRoleData data = getData(applicantId);
        if (data == null) {
            ServiceHelper.familyMainService().innerAckVerification(familyId, verifierId, applicantId, false, "数据丢失了? 技术蛋蛋疼!");
            return;
        }
        FamilyRoleApplicationPo applicationPo = data.getApplicationPoMap().remove(familyId);
        if (applicationPo != null) {
            dao.delete(applicationPo);
        }
        if (isOk) {
            if (data.getRolePo().getFamilyId() > 0 && data.getRolePo().getFamilyId() != familyId) {
                ServiceHelper.familyMainService().innerAckVerification(familyId, verifierId, applicantId, false, "family_tips_havefamily");
                return;
            }
            data.getRolePo().setFamilyId(familyId);
            dao.update(data.getRolePo());
            ServiceHelper.familyMainService().innerAckVerification(familyId, verifierId, applicantId, true, null);
            cancelOtherApplication(data);
        }
    }

    @Override
    public void sendRecommendationList(long roleId) {
        FamilyRoleData data = getOnlineData(roleId);
        if (data != null) {
            ClientFamilyRecommendation packet = new ClientFamilyRecommendation(
                    FamilyMainServiceActor.recommList, data.getApplicationPoMap());
            PlayerUtil.send(roleId, packet);
        }

    }

    public void searchFamily(long roleId, String pattern) {
        // 检查pattern
        if (pattern == null || pattern.length() == 0 || pattern.length() > 6 || pattern.matches(".*[';%].*")) {
            ServiceUtil.sendText(roleId, I18n.get("family.management.search.illegalText"));
            return;
        }
        FamilyRoleData data = getOnlineData(roleId);
        if (data == null) {
            LogUtil.error("不存在玩家数据");
            return;
        }
        Map<Long, RecommendationFamily> recommMap = new HashMap<>();
        Iterator<RecommendationFamily> iterator = FamilyMainServiceActor.recommList.iterator();
        while (iterator.hasNext()) {
            RecommendationFamily recomm = iterator.next();
            if (recomm.getName().contains(pattern)) {
                recommMap.put(recomm.getFamilyId(), recomm);
            }
        }
        if (recommMap.size() < FamilyManager.familyRecomListLimit) {
            String sql = "select * from `family` where `name` like '%" + pattern + "%' " +
                    "and `membercount` > 0 " +
                    "and `mastername` is not null " +
                    "and `mastername` not like '' limit 20";
            try {
                List<FamilyPo> list = DBUtil.queryList(DBUtil.DB_USER, FamilyPo.class, sql);
                for (FamilyPo familyPo : list) {
                    if (!recommMap.containsKey(familyPo.getFamilyId())) {
                        recommMap.put(familyPo.getFamilyId(), FamilyMainServiceActor.newRecommendationFamily(familyPo));
                    }
                }
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
        List<RecommendationFamily> recommList = new ArrayList<>(recommMap.values());
        int size = Math.min(FamilyManager.familyRecomListLimit, recommList.size()); // 获取最小值
        recommList = size == 0 ? new ArrayList<RecommendationFamily>() : recommList.subList(0, size); // 判断为零的情况
        ClientFamilyRecommendation packet = new ClientFamilyRecommendation(
                recommList, data.getApplicationPoMap());
        PlayerUtil.send(roleId, packet);

    }

    @Override
    public void addAppliedFamilyId(long roleId, long familyId) {
        FamilyRoleData data = getData(roleId);
        if (data == null) {
            return;
        }
        FamilyRoleApplicationPo applicationPo = newApplicationPo(roleId, familyId);
        data.getApplicationPoMap().put(familyId, applicationPo);
        dao.insert(applicationPo);
    }

    @Override
    public Set<Long> getAppliedFamilyIdSet(long roleId) {
        FamilyRoleData data = getOnlineData(roleId);
        if (data == null) {
            return new HashSet<>();
        }
        return new HashSet<>(data.getApplicationPoMap().keySet());
    }

    private FamilyRoleData getData(long roleId) {
        if (onlineDataMap.containsKey(roleId)) {
            return onlineDataMap.get(roleId);
        }
        return offlineDataMap.getUnchecked(roleId);
    }

    public FamilyRoleData getOnlineData(long roleId) {
        return onlineDataMap.get(roleId);
    }

    private void cancelOtherApplication(FamilyRoleData data) {
        Iterator<FamilyRoleApplicationPo> iterator = data.getApplicationPoMap().values().iterator();
        while (iterator.hasNext()) {
            FamilyRoleApplicationPo applicationPo = iterator.next();
            iterator.remove();
            dao.delete(applicationPo);
            ServiceHelper.familyMainService().cancel(applicationPo.getFamilyId(), applicationPo.getRoleId());
        }
    }

    public FamilyRoleApplicationPo newApplicationPo(long roleId, long familyId) {
        FamilyRoleApplicationPo applicationPo = new FamilyRoleApplicationPo();
        applicationPo.setRoleId(roleId);
        applicationPo.setFamilyId(familyId);
        return applicationPo;
    }

    class FamilyRoleDataCacheLoader extends CacheLoader<Long, FamilyRoleData> {
        @Override
        public FamilyRoleData load(Long roleId) throws Exception {
            FamilyRoleData data = pendingSavingDataMap.get(roleId);
            if (data != null) {
                pendingSavingDataMap.remove(roleId);
                return data;
            }
            FamilyRolePo rolePo = DBUtil.queryBean(DBUtil.DB_USER, FamilyRolePo.class,
                    "select * from `familyrole` where `roleid`=" + roleId);
            Map<Long, FamilyRoleApplicationPo> applicationPoMap = DBUtil.queryMap(
                    DBUtil.DB_USER, "familyid", FamilyRoleApplicationPo.class,
                    "select * from `familyroleapplication` where `roleid`=" + roleId);
            data = new FamilyRoleData(rolePo, applicationPoMap);
            return data;
        }
    }

}
