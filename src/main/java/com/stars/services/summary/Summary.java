package com.stars.services.summary;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.services.summary.basecomponent.BaseSummaryComponent;
import com.stars.util.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 可能需要拷贝，暂不考虑
 * Created by zhaowenshuo on 2016/8/11.
 */
public class Summary extends DbRow {

    static Map<String, Class<? extends SummaryComponent>> componentClassMap = new HashMap<>();
    static Map<String, Map<String, Field>> componentFieldMap = new HashMap<>();

    public static void regComponentClass(String name, Class<? extends SummaryComponent> componentClass) {
        if (componentClassMap.containsKey(name)) {
            throw new IllegalArgumentException("组件已存在: " + name);
        }
        componentClassMap.put(name, componentClass);
        componentFieldMap.put(name, new HashMap<String, Field>());
        for (Field field : componentClass.getDeclaredFields()) {
            field.setAccessible(true);
            componentFieldMap.get(name).put(field.getName(), field);
        }
    }

    public static Summary newDummy(long roleId) {
        return new Summary(roleId, true);
    }

    public static Field getField(String componentName, String fieldName) {
        if (componentFieldMap.containsKey(componentName)) {
            return componentFieldMap.get(componentName).get(fieldName);
        }
        return null;
    }

    static SummaryComponent createDummyComponent(String componentName, Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            try {
                return (SummaryComponent) Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        interfaces, new DummySummaryComponentInvocationHandler());
            } catch (Exception e) {
                LogUtil.error("生成常用数据Dummy异常, key=" + componentName, e);
            }
        }
        return null;
    }

    private long roleId;
    private boolean isOnline = false;
    private ConcurrentMap<String, SummaryComponent> componentMap = new ConcurrentHashMap<>();

    public Summary() {

    }

    public Summary(long roleId) {
        this.roleId = roleId;
        this.componentMap = new ConcurrentHashMap<>();
    }

    public Summary(long roleId, ConcurrentMap<String, SummaryComponent> componentMap) {
        this.roleId = roleId;
        this.componentMap = componentMap;
    }

    public Summary(long roleId, boolean isDummy) {
        this.roleId = roleId;
        if (isDummy) {
            for (Map.Entry<String, Class<? extends SummaryComponent>> entry : componentClassMap.entrySet()) {
                createAndPutDummy(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public String getChangeSql() {
        String json = SqlUtil.getSql(this, DBUtil.DB_USER, "rolesummary", "`roleid`=" + roleId);
        return json;
    }

    @Override
    public String getDeleteSql() {
        return null;
    }

    public <T extends SummaryComponent> T getComponent(String componentName) {
        return (T) componentMap.get(componentName);
    }

    public void setComponent(String componentName, SummaryComponent component) {
        componentMap.put(componentName, component);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public ConcurrentMap<String, SummaryComponent> getComponentMap() {
        return componentMap;
    }

    public void setComponentMap(ConcurrentMap<String, SummaryComponent> componentMap) {
        this.componentMap = componentMap;
    }

    public int getOfflineTimestamp() {
        return isOnline() ? 0 : ((BaseSummaryComponent) getComponent("base")).getOfflineTimestamp();
    }

    public boolean isDummy() {
        for (SummaryComponent comp : componentMap.values()) {
            if (comp.isDummy()) {
                return true;
            }
        }
        return false;
    }

    private void createAndPutDummy(String componentName, Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            try {
                componentMap.put(componentName, (SummaryComponent) Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        interfaces, new DummySummaryComponentInvocationHandler()));
            } catch (Exception e) {
                LogUtil.error("生成常用数据Dummy异常, key=" + componentName, e);
            }
        }
    }

}
