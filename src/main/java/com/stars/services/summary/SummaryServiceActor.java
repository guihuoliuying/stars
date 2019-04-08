package com.stars.services.summary;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.stars.core.dao.DbRowDao;
import com.stars.db.DBUtil;
import com.stars.services.Service;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.summary.basecomponent.BaseSummaryComponent;
import com.stars.services.summary.basecomponent.BaseSummaryComponentImpl;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/8/11.
 */
public class SummaryServiceActor extends ServiceActor implements Service, SummaryService {

    private DbRowDao dao;
    private String serviceName; // 服务名
    private Map<Long, Summary> onlineDataMap; // 在线数据列表
    private LoadingCache<Long, Summary> offlineDataMap; // 离线数据列表
    private Map<Long, Summary> pendingSavingDataMap; // 保存失败数据列表

    public SummaryServiceActor(String id) {
        this.serviceName = "summary service-" + id;
    }

    public SummaryServiceActor(int id) {
        this(Integer.toString(id));
    }

    @Override
    public void init() throws Throwable {
        dao = new DbRowDao(serviceName);
        ServiceSystem.getOrAdd(serviceName, this);
        onlineDataMap = new HashMap<>();
        offlineDataMap = CacheBuilder.newBuilder()
                .maximumSize(2048)
                .expireAfterAccess(1800, TimeUnit.SECONDS) // 半小时
                .build(new RoleSummaryCacheLoader());
        pendingSavingDataMap = new HashMap<>();
        synchronized (Summary.class) {
            if (!Summary.componentClassMap.containsKey("base")) {
                Summary.regComponentClass("base", BaseSummaryComponentImpl.class);
            }
        }
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},onlineDataMap:{},offlineDataMap:{},pendingSavingDataMap:{}",this.getClass().getSimpleName(),onlineDataMap.size(), offlineDataMap.size(),  pendingSavingDataMap.size());
    }

    class RoleSummaryCacheLoader extends CacheLoader<Long, Summary> {
        @Override
        public Summary load(Long roleId) throws Exception {
            // 从待保存列表找
            Summary summary = pendingSavingDataMap.get(roleId);
            if (summary != null) {
                pendingSavingDataMap.remove(roleId);
                return summary;
            }
            // 从数据库找
            Map<String, SummaryComponentRow> rowMap = DBUtil.queryMap(
                    DBUtil.DB_USER, "componentname", SummaryComponentRow.class,
                    "select * from `rolesummary` where `roleid`=" + roleId);
            ConcurrentMap<String, SummaryComponent> componentMap = new ConcurrentHashMap<>();
            if (rowMap != null && rowMap.size() > 0) {
                for (String compName : Summary.componentClassMap.keySet()) {
                    SummaryComponentRow row = rowMap.get(compName);
                    if (row != null) {
                        SummaryComponent comp = fromRow(roleId, row);
                        if (comp != null) {
                            componentMap.put(comp.getName(), comp);
                        }
                    } else {
                        componentMap.put(compName, Summary.createDummyComponent(compName, Summary.componentClassMap.get(compName)));
                    }
                }
                summary = new Summary(roleId, componentMap);
            } else {
                throw new NoSuchElementException();
            }
            return summary;
        }
    }

    @Override
    public void save() {
        dao.flush(true); // 失败了就失败了（常用数据没那么重要）
    }

    @Override
    public void online(long roleId) {
        load(roleId);
    }

    private void load(long roleId) {
        if (onlineDataMap.containsKey(roleId)) {
            return;
        }
        // 从待保存列表中取
        Summary summary = pendingSavingDataMap.get(roleId);
        if (summary != null) {
            onlineDataMap.put(roleId, summary);
            pendingSavingDataMap.remove(roleId);
            summary.setOnline(true);
            return;
        }
        // 从离线缓存中取
        summary = offlineDataMap.getIfPresent(roleId);
        if (summary != null) {
            onlineDataMap.put(roleId, summary);
            offlineDataMap.invalidate(roleId);
            summary.setOnline(true);
            return;
        }
        // 从数据库中取
        try {
            Map<String, SummaryComponentRow> rowMap = DBUtil.queryMap(
                    DBUtil.DB_USER, "componentname", SummaryComponentRow.class,
                    "select * from `rolesummary` where `roleid`=" + roleId);
            ConcurrentMap<String, SummaryComponent> componentMap = new ConcurrentHashMap<>();
            if (rowMap != null && rowMap.size() > 0) {
                for (String compName : Summary.componentClassMap.keySet()) {
                    SummaryComponentRow row = rowMap.get(compName);
                    if (row != null) {
                        SummaryComponent comp = fromRow(roleId, row);
                        if (comp != null) {
                            componentMap.put(comp.getName(), comp);
                        }
                    } else {
                        componentMap.put(compName, Summary.createDummyComponent(compName, Summary.componentClassMap.get(compName)));
                    }
                }
                summary = new Summary(roleId, componentMap);

                summary.setOnline(true);
                onlineDataMap.put(roleId, summary);
            } else {
                summary = new Summary(roleId);
                summary.setOnline(true);
                onlineDataMap.put(roleId, summary);
                updateSummaryComponent(roleId, new BaseSummaryComponentImpl(ServiceUtil.now()));
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public void offline(long roleId) {
        Summary summary = onlineDataMap.get(roleId);
        if (summary != null) {
            offlineDataMap.put(roleId, summary);
            onlineDataMap.remove(roleId);
            summary.setOnline(false);
            BaseSummaryComponent baseComp = new BaseSummaryComponentImpl(ServiceUtil.now());
            if (summary.getComponent("base") == null) {
                dao.insert(toRow(roleId, baseComp));
            }
            summary.setComponent("base", new BaseSummaryComponentImpl(ServiceUtil.now()));
            for (SummaryComponent component : summary.getComponentMap().values()) {
                dao.update(toRow(roleId, component));
            }
        } else {
            LogUtil.error("没有相关常用数据");
        }
    }

    @Override
    public void updateSummaryComponent(long roleId, SummaryComponent component) {
        if (!Summary.componentClassMap.containsKey(component.getName())) {
            throw new IllegalStateException("没有注册SummaryComponent: " + component.getName());
        }
        Summary summary = getOnlineSummary(roleId);
        if (summary != null) {
            SummaryComponent prevComp = summary.getComponent(component.getName());
            if (prevComp != null && !prevComp.isDummy()) {
                summary.setComponent(component.getName(), component);
                dao.update(toRow(roleId, component));
            } else {
                summary.setComponent(component.getName(), component);
                dao.insert(toRow(roleId, component));
            }
        }
    }

    /**
     * 某些公共数据的常用数据更新时,对应角色可能不在线
     * 此方法为强制执行公共数据的常用数据的更新
     * 不在线时会参入更新语句，所以比较保证数据库内已经insert过
     */
    @Override
    public void updateOfflineSummaryComponent(long roleId, SummaryComponent component) {
        if (!Summary.componentClassMap.containsKey(component.getName())) {
            throw new IllegalStateException("没有注册SummaryComponent: " + component.getName());
        }
        Summary summary = getSummary(roleId);
        if (summary != null) {
            SummaryComponent prevComp = summary.getComponent(component.getName());
            if (prevComp != null && !prevComp.isDummy()) {
                summary.setComponent(component.getName(), component);
                dao.update(toRow(roleId, component));
            } else {
                summary.setComponent(component.getName(), component);
                dao.insert(toRow(roleId, component));
            }
        }
    }

    @Override
    public void updateSummaryComponent(long roleId, Map<String, SummaryComponent> componentMap) {
        for (SummaryComponent comp : componentMap.values()) {
            if (!Summary.componentClassMap.containsKey(comp.getName())) {
                throw new IllegalStateException("没有注册SummaryComponent: " + comp.getName());
            }
        }
        Summary summary = getOnlineSummary(roleId);
        if (summary != null) {
            for (Map.Entry<String, SummaryComponent> entry : componentMap.entrySet()) {
                SummaryComponent currComp = entry.getValue();
                SummaryComponent prevComp = summary.getComponent(entry.getKey());
                if (prevComp != null && !prevComp.isDummy()) {
                    summary.setComponent(entry.getKey(), currComp);
                    dao.update(toRow(roleId, currComp));
                } else {
                    summary.setComponent(entry.getKey(), currComp);
                    dao.insert(toRow(roleId, currComp));
                }
            }
        }
    }

    private SummaryComponentRow toRow(long roleId, SummaryComponent component) {
        SummaryComponentRow row = new SummaryComponentRow();
        row.setRoleId(roleId);
        row.setComponentName(component.getName());
        row.setComponentValue(component.makeString());
        row.setVersion(component.getLatestVersion());
        return row;
    }

    private SummaryComponent fromRow(long roleId, SummaryComponentRow row) {
        Class<? extends SummaryComponent> clazz = Summary.componentClassMap.get(row.getComponentName());
        if (clazz != null) {
            try {
                SummaryComponent component = clazz.newInstance();
                component.fromString(row.getVersion(), row.getComponentValue());
                return component;
            } catch (Exception e) {
                LogUtil.error("解析常用数据异常, key=" + row.getComponentName() + ", ver=" + row.getVersion() + ", val=" + row.getComponentValue(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public SummaryComponent getOnlineSummaryComponent(long roleId, String componentName) {
        if (!Summary.componentClassMap.containsKey(componentName)) {
            return null;
        }
        Summary summary = getOnlineSummary(roleId);
        if (summary == null) {
            return null;
        }
        SummaryComponent comp = summary.getComponent(componentName);
        return comp != null ? comp : Summary.createDummyComponent(componentName, Summary.componentClassMap.get(componentName));
    }

    @Override
    public SummaryComponent getSummaryComponent(long roleId, String componentName) {
        if (!Summary.componentClassMap.containsKey(componentName)) {
            return null;
        }
        Summary summary = getSummary(roleId);
        SummaryComponent comp = summary != null ? summary.getComponent(componentName) : null;
        return comp != null ? comp : Summary.createDummyComponent(componentName, Summary.componentClassMap.get(componentName));
    }

    @Override
    public Summary getOnlineSummary(long roleId) {
        return onlineDataMap.get(roleId);
    }

    @Override
    public Summary getSummary(long roleId) {
        Summary summary = onlineDataMap.get(roleId);
        if (summary == null) {
            summary = pendingSavingDataMap.get(roleId);
        }
        if (summary == null) {
            try {
                summary = offlineDataMap.get(roleId);
            } catch (Exception e) {
            }
        }
        return summary != null ? summary : Summary.newDummy(roleId);
    }

    @Override
    public List<Summary> getAllOnlineSummary() {
        return new ArrayList<>(onlineDataMap.values());
    }

    @Override
    public List<Summary> getAllOfflineSummary() {
        return new ArrayList<>(offlineDataMap.asMap().values());
    }

    @Override
    public List<Summary> getAllSummary(List<Long> roleIdList) {
        List<Summary> list = new ArrayList<>(roleIdList.size());
        for (long roleId : roleIdList) {
            list.add(getSummary(roleId));
        }
        return list;
    }

    @Override
    public List<Summary> getAllOnlineSummary(List<Long> roleIdList) {
        List<Summary> list = new ArrayList<>(roleIdList.size());
        for (long roleId : roleIdList) {
            Summary summary = getOnlineSummary(roleId);
            if (summary != null) {
                list.add(summary);
            }
        }
        return list;
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

}
