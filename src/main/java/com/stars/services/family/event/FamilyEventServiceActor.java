package com.stars.services.family.event;

import com.google.common.cache.*;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.family.packet.ClientFamilyEvent;
import com.stars.modules.family.packet.ServerFamilyEvent;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.event.userdata.FamilyEventPo;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class FamilyEventServiceActor extends ServiceActor implements FamilyEventService {

    private DbRowDao dao;

    private String serviceName;
    private Map<Long, FamilyEventData> onlineDataMap;
    private LoadingCache<Long, FamilyEventData> offlineDataMap;
    private Map<Long, FamilyEventData> pendingSavingDataMap;

    public FamilyEventServiceActor(String id) {
        this.serviceName = "family event service-" + id;
    }

    public FamilyEventServiceActor(int id) {
        this(Integer.toString(id));
    }

    @Override
    public void init() throws Throwable {
        dao = new DbRowDao(serviceName);
        ServiceSystem.getOrAdd(serviceName, this);
        onlineDataMap = new HashMap<>();
        offlineDataMap = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(1800, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<Long, FamilyEventData>() {
                    @Override
                    public void onRemoval(RemovalNotification<Long, FamilyEventData> notification) {
                        if (notification.wasEvicted()) {
                            Set<DbRow> set = new HashSet<DbRow>();
                            set.addAll(notification.getValue().getEventList());
                            if (!dao.isSavingSucceeded(set)) {
                                pendingSavingDataMap.put(notification.getKey(), notification.getValue());
                                LogUtil.error("family - event缓存移除异常，roleId=" + notification.getKey());
                            }
                        }
                    }
                })
                .build(new FamilyEventDataCacheLoader());
        pendingSavingDataMap = new HashMap<>();
    }

    @Override
    public void printState() {

    }

    @Override
    public void online(long familyId) {
        FamilyEventData data = onlineDataMap.get(familyId);
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
                List<FamilyEventPo> donateList = new LinkedList<>(DBUtil.queryList(DBUtil.DB_USER, FamilyEventPo.class,
                        "select * from `familyevent` where `familyid`=" + familyId + " and `event`=0 order by `timestamp`"));
                List<FamilyEventPo> eventList = new LinkedList<>(DBUtil.queryList(DBUtil.DB_USER, FamilyEventPo.class,
                        "select * from `familyevent` where `familyid`=" + familyId + " and `event`!=0 order by `timestamp`"));
                data = new FamilyEventData(donateList, eventList);
                onlineDataMap.put(familyId, data);
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
    }

    @Override
    public void offline(long familyId) {
        FamilyEventData data = onlineDataMap.remove(familyId);
        if (data != null) {
            offlineDataMap.put(familyId, data);
        }
    }

    @Override
    public void save() {
        dao.flush();
    }

    @Override
    public void logEvent(long familyId, int event, String... params) {
        FamilyEventData data = getOnlineData(familyId);
        if (data == null) {
            LogUtil.error("不存在数据");
            return;
        }
        FamilyEventPo eventPo = newFamilyEventPo(familyId, event, params);
        data.addEvent(eventPo, dao);
        dao.insert(eventPo);
        LogUtil.info("家族Id：{}，事件：{}，参数：{}", familyId, event, params);
    }

    @Override
    public void sendEvent(FamilyAuth auth, byte subtype) {
        if (auth.getFamilyId() <= 0) {
            ServiceUtil.sendText(auth.getRoleId(), "common_tips_loading");
            return;
        }
        FamilyEventData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            LogUtil.error("不存在数据");
            return;
        }
        if (subtype == ServerFamilyEvent.SUBTYPE_DONATE) {
            PlayerUtil.send(auth.getRoleId(), new ClientFamilyEvent(subtype, data.getDonateList()));
        } else {
            PlayerUtil.send(auth.getRoleId(), new ClientFamilyEvent(subtype, data.getEventList()));
        }
    }

    private FamilyEventData getOnlineData(long familyId) {
        return onlineDataMap.get(familyId);
    }

    private FamilyEventData getData(long familyId) {
        if (onlineDataMap.containsKey(familyId)) {
            return onlineDataMap.get(familyId);
        }
        return offlineDataMap.getUnchecked(familyId);
    }

    private FamilyEventPo newFamilyEventPo(long familyId, int event, String[] params) {
        FamilyEventPo eventPo = new FamilyEventPo();
        eventPo.setFamilyId(familyId);
        eventPo.setEvent(event);
        eventPo.setParamArray(params);
        eventPo.setTimestamp(ServiceUtil.now());
        return eventPo;
    }

    class FamilyEventDataCacheLoader extends CacheLoader<Long, FamilyEventData> {
        @Override
        public FamilyEventData load(Long familyId) throws Exception {
            FamilyEventData data = pendingSavingDataMap.get(familyId);
            if (data != null) {
                pendingSavingDataMap.remove(familyId);
                return data;
            }
            List<FamilyEventPo> donateList = new LinkedList<>(DBUtil.queryList(DBUtil.DB_USER, FamilyEventPo.class,
                    "select * from `familyevent` where `familyid`=" + familyId + " and `event`=0 order by `timestamp`"));
            List<FamilyEventPo> eventList = new LinkedList<>(DBUtil.queryList(DBUtil.DB_USER, FamilyEventPo.class,
                    "select * from `familyevent` where `familyid`=" + familyId + " and `event`!=0 order by `timestamp`"));
            data = new FamilyEventData(donateList, eventList);
            return data;
        }
    }

}
