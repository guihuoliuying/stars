package com.stars.core.module;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.exception.LogicException;
import com.stars.core.player.Player;
import com.stars.core.redpoint.RedPoints;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.services.summary.SummaryComponent;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zws on 2015/11/30.
 */
public class AbstractModule implements Module {

    private final String name;
    private long id;
    private Player self;
    private EventDispatcher eventDispatcher;
    private RedPoints redPoints;
    private ModuleContext context;
    private Map<String, Module> moduleMap;
    private Map<String, Object> hotUpdateCache;  //热更临时缓存数据

    public AbstractModule(String name, long id, Player self, EventDispatcher eventDispatcher,  Map<String, Module> moduleMap) {
        this.name = name;
        this.self = self;
        this.id = id;
        this.eventDispatcher = eventDispatcher;
        this.moduleMap = moduleMap;
    }

    protected Player self() {
        return self;
    }

    protected EventDispatcher eventDispatcher() {
        return eventDispatcher;
    }

    protected RedPoints redPoints() {
        return redPoints;
    }

    public ModuleContext context() {
        return context;
    }

    protected Map<String, Module> moduleMap() {
        return moduleMap;
    }

    protected <T extends Module> T module(String moduleName) {
        return (T) moduleMap.get(moduleName);
    }

    // 便利方法
    public final void send(Packet packet) {
        self.send(packet);
    }

    public final void send(GameSession session, Packet packet) {
        PacketManager.send(session, packet);
    }

    public final void warn(String message) {
        self.send(new ClientText(message));
    }

    public final void warn(String message, String... params) {
        self.send(new ClientText(message, params));
    }

    public final void warn(GameSession session, String message) {
        PacketManager.send(session, new ClientText(message));
    }

    public final void warn(GameSession session, String message, String... params) {
        PacketManager.send(session, new ClientText(message, params));
    }

    public final void check(boolean condition, String message, String... params) {
        if (condition) {
            throw new LogicException(message, params);
        }
    }

    public final void fire(Event event) {
        eventDispatcher().fire(event);
    }

    public final long now() {
        return System.currentTimeMillis();
    }

    protected final void lazySend(Packet packet) {
        self.lazySend(packet);
    }

    protected final void clearLazyQueue() {
        self.clearLazyQueue();
    }

    // 模块相关的流程方法
    @Override
    public final String name() {
        return this.name;
    }

    @Override
    public final long id() {
        return id;
    }

    @Override
    public void onDataReq() throws Throwable {

    }

    @Override
    public void onCreation(String name, String account) throws Throwable {

    }


    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {

    }

    /**
     * 每天凌晨五点重置
     */
    @Override
    public void onFiveOClockReset(Calendar now) throws Throwable {

    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {

    }

    @Override
    public void onMonthlyReset() throws Throwable {

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }

    @Override
    public void onReconnect() throws Throwable{
        onInit(false);
    }

    @Override
    public void onSyncData() throws Throwable {

    }

    @Override
    public void onOffline() throws Throwable {

    }

    @Override
    public void onExit() throws Throwable {

    }

    @Override
    public void onTimingExecute() {

    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {

    }

    public void signCalRedPoint(String moduelName, int redPointId) {
        self.getRedPoints().addSign(moduelName, redPointId);
    }

    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {

    }

    // string
    public String getString(String key) {
        return context().recordMap().getString(key);
    }

    public String getString(String key, String defaultValue) {
        return context().recordMap().getString(key, defaultValue);
    }


    public void setString(String key, String value) {
        context().recordMap().setString(key, value);
    }

    // string from/to map
    public String getStringFromMap(String key, String field) {
        return context().recordMap().getStringFromMap(key, field);
    }

    public String getStringFromMap(String key, String field, String defaultValue) {
        return context().recordMap().getStringFromMap(key, field, defaultValue);
    }

    public void setStringToMap(String key, String field, String value) {
        context().recordMap().setStringToMap(key, field, value);
    }

    // byte
    public byte getByte(String key) {
        return context().recordMap().getByte(key);
    }

    public byte getByte(String key, byte defaultValue) {
        return context().recordMap().getByte(key, defaultValue);
    }

    public void setByte(String key, byte value) {
        context().recordMap().setByte(key, value);
    }

    // byte from/to map
    public byte getByteFromMap(String key, String field) {
        return context().recordMap().getByteFromMap(key, field);
    }

    public byte getByteFromMap(String key, String field, byte defaultValue) {
        return context().recordMap().getByteFromMap(key, field, defaultValue);
    }

    public void setByteToMap(String key, String field, byte value) {
        context().recordMap().setByteToMap(key, field, value);
    }

    // short
    public short getShort(String key) {
        return context().recordMap().getShort(key);
    }

    public short getShort(String key, short defaultValue) {
        return context().recordMap().getShort(key, defaultValue);
    }

    public void setShort(String key, short value) {
        context().recordMap().setShort(key, value);
    }

    // short from/to map
    public short getShortFromMap(String key, String field) {
        return context().recordMap().getShortFromMap(key, field);
    }

    public short getShortFromMap(String key, String field, short defaultValue) {
        return context().recordMap().getShortFromMap(key, field, defaultValue);
    }

    public void setShortToMap(String key, String field, short value) {
        context().recordMap().setShortToMap(key, field, value);
    }

    // int
    public int getInt(String key) {
        return context().recordMap().getInt(key);
    }

    public int getInt(String key, int defaultValue) {
        return context().recordMap().getInt(key, defaultValue);
    }

    public void setInt(String key, int value) {
        context().recordMap().setInt(key, value);
    }

    // int from/to map
    public int getIntFromMap(String key, String field) {
        return context().recordMap().getIntFromMap(key, field);
    }

    public int getIntFromMap(String key, String field, int defaultValue) {
        return context().recordMap().getIntFromMap(key, field, defaultValue);
    }

    public int getIntFromMap(String key, int field) {
        return context().recordMap().getIntFromMap(key, field);
    }

    public int getIntFromMap(String key, int field, int defaultValue) {
        return context().recordMap().getIntFromMap(key, field, defaultValue);
    }

    public void setIntToMap(String key, String field, int value) {
        context().recordMap().setIntToMap(key, field, value);
    }

    public void setIntToMap(String key, int field, int value) {
        context().recordMap().setIntToMap(key, field, value);
    }

    // long
    public long getLong(String key) {
        return context().recordMap().getLong(key);
    }

    public long getLong(String key, long defaultValue) {
        return context().recordMap().getLong(key, defaultValue);
    }

    public void setLong(String key, long value) {
        context().recordMap().setLong(key, value);
    }

    // long from/to map
    public long getLongFromMap(String key, String field) {
        return context().recordMap().getLongFromMap(key, field);
    }

    public long getLongFromMap(String key, String field, long defaultValue) {
        return context().recordMap().getLongFromMap(key, field, defaultValue);
    }

    public void setLongToMap(String key, String field, long value) {
        context().recordMap().setLongToMap(key, field, value);
    }

    // float
    public float getFloat(String key) {
        return context().recordMap().getFloat(key);
    }

    public float getFloat(String key, float defaultValue) {
        return context().recordMap().getFloat(key, defaultValue);
    }

    public void setFloat(String key, float value) {
        context().recordMap().setFloat(key, value);
    }

    // float from/to map
    public float getFloatFromMap(String key, String field) {
        return context().recordMap().getFloatFromMap(key, field);
    }

    public float getFloatFromMap(String key, String field, float defaultValue) {
        return context().recordMap().getFloatFromMap(key, field, defaultValue);
    }

    public void setFloatToMap(String key, String field, float value) {
        context().recordMap().setFloatToMap(key, field, value);
    }

    // double
    public double getDouble(String key) {
        return context().recordMap().getDouble(key);
    }

    public double getDouble(String key, double defaultValue) {
        return context().recordMap().getDouble(key, defaultValue);
    }

    public void setDouble(String key, double value) {
        context().recordMap().setDouble(key, value);
    }

    // double from/to map
    public double getDoubleFromMap(String key, String field) {
        return context().recordMap().getDoubleFromMap(key, field);
    }

    public double getDoubleFromMap(String key, String field, double defaultValue) {
        return context().recordMap().getDoubleFromMap(key, field, defaultValue);
    }

    public void setDoubleToMap(String key, String field, double value) {
        context().recordMap().setDoubleToMap(key, field, value);
    }

    public void clearValueMap(String key) {
        context().recordMap().clearValueMap(key);
    }

    public void delete(String key) {
        context().recordMap().delete(key);
    }

    protected String toString(boolean b) {
        return Boolean.toString(b);
    }

    protected String toString(byte b) {
        return Byte.toString(b);
    }

    protected String toString(short s) {
        return Short.toString(s);
    }

    protected String toString(int i) {
        return Integer.toString(i);
    }

    protected String toString(Long l) {
        return Long.toString(l);
    }

    protected String toString(float f) {
        return Float.toString(f);
    }

    protected String toString(double d) {
        return Double.toString(d);
    }

    /*
     * 热更相关
     */
    public void selfCache(String key, Object obj){
        if (hotUpdateCache == null) {
            hotUpdateCache = new HashMap<>();
        }
        hotUpdateCache.put(key, obj);
    }

    public <T> T selfCache(String key) {
        if (hotUpdateCache == null) {
            return null;
        }
        return (T) hotUpdateCache.get(key);
    }

    public void removeSelfCache(String key) {
        if (hotUpdateCache == null) {
            return;
        }
        hotUpdateCache.remove(key);
    }

    @Override
	public void onLog() {
		// TODO Auto-generated method stub
		
	}

}
