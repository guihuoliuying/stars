package com.stars.server.connector;

import com.stars.util.LogUtil;

import java.io.IOException;
import java.util.*;

/**
 * Created by zws on 2015/9/14.
 */
public class ConnectorConfig {

    public static ConnectorConfig load(Properties prop) throws IOException {
        ConnectorConfig config = new ConnectorConfig();

        config.ip = prop.getProperty("serverIp");
        config.port = Integer.parseInt(prop.getProperty("serverPort"));
        config.threadNumber = getInt(prop, "connector-threadNumber", Runtime.getRuntime().availableProcessors() * 2);
        config.frontendTimeout = Integer.parseInt(prop.getProperty("connector-frontend-timeout"));
        config.backendTimeout = Integer.parseInt(prop.getProperty("connector-backend-timeout"));
        config.frontendSendBuf = getInt(prop, "connector-frontend-sendBuf", 128 * 1024);
        config.frontendRecvBuf = getInt(prop, "connector-frontend-recvBuf", 128 * 1024);

        /* 后端连接高低水位 */
        config.hwm = getInt(prop, "connector-backend-hwm", 128 * 1024);
        config.lwm = getInt(prop, "connector-backend-lwm", 32 * 1024);

        /* 测试配置 */
        config.isOpenBackdoor = getBoolean(prop, "connector-test-isOpenBackdoor", false);
        config.isTestOn = getBoolean(prop, "connector-test-isOn", false);
        config.needRelayTestPacket = getBoolean(prop, "connector-test-needRelay", false);

        /* 访问控制 */
        config.allowsSet = new HashSet<>(getStringList(prop, "connector-allows", ";", ""));
        config.needAccessControl = config.allowsSet.size() > 0;

        /* 过载保护配置 */
        config.thresholdOfPayload = getInt(prop, "connector-thresholdOfPayload", 7500);

        return config;
    }

    private static String getString(Properties prop, String key, String defaultValue) {
        String value = prop.getProperty(key);
        return value == null ? defaultValue : value;
    }

    private static int getInt(Properties prop, String key, int defaultValue) {
        String value = prop.getProperty(key);
        return (value == null || "".equals(value.trim()) || "default".equals(value.trim()))
                ? defaultValue : Integer.parseInt(value);
    }

    private static boolean getBoolean(Properties prop, String key, boolean defaultValue) {
        String value = prop.getProperty(key);
        return (value != null && "true".equals(value.trim())) || defaultValue;
    }

    private static List<String> getStringList(Properties prop, String key, String delimiter, String defaultValue) {
        String value = prop.getProperty(key);
        value = value == null ? defaultValue : value;
        if ("".equals(value.trim())) {
            return new ArrayList<>();
        }

        List<String> list = new ArrayList<>();
        String[] array = value.split(delimiter);
        for (String ip : array) {
            list.add(ip.replace("*", ""));
        }
        LogUtil.info("访问控制允许集合: {}", list);
        return list;
    }

    private String ip;
    private int port;
    private int threadNumber;

    private int frontendTimeout;
    private int backendTimeout;

    private int thresholdOfPayload; // 负载保护阈值

    // 后端连接高低水位
    private int hwm;
    private int lwm;

    private int frontendSendBuf;
    private int frontendRecvBuf;

    // 测试相关
    private boolean isOpenBackdoor; // 是否开启后门程序
    private boolean isTestOn; // 测试开关
    private boolean needRelayTestPacket; // 是否转发测试数据包（false:连接服立刻返回;true:转发到游戏服）
    private boolean needAccessControl;
    private Set<String> allowsSet; // 允许IP列表，暂时使用


    private int statLevel; // 连接，线程，进程
//    private int statConnectionSample; //
//    private int statThreadSubmitInterval;
//    private int statProcessPrintInterval;

    public String ip() {
        return this.ip;
    }

    public int port() {
        return this.port;
    }

    public int threadNumber() {
        return this.threadNumber;
    }

    public int frontendTimeout() {
        return this.frontendTimeout;
    }

    public int backendTimeout() {
        return this.backendTimeout;
    }

    public int thresholdOfPayload() {
        return this.thresholdOfPayload;
    }

    public int hwm() {
        return this.hwm;
    }

    public int lwm() {
        return this.lwm;
    }

    public boolean isTestOn() {
        return this.isTestOn;
    }

    public void isTestOn(boolean flag) {
        this.isTestOn = flag;
    }

    public boolean needRelayTestPacket() {
        return this.needRelayTestPacket;
    }

    public void needRelayTestPacket(boolean flag) {
        this.needRelayTestPacket = flag;
    }

    public int getFrontendRecvBuf() {
        return frontendRecvBuf;
    }

    public int getFrontendSendBuf() {
        return frontendSendBuf;
    }

    public boolean isOpenBackdoor() {
        return this.isOpenBackdoor;
    }

    public void isOpenBackdoor(boolean flag) {
        this.isOpenBackdoor = flag;
    }

    public boolean needAccessControl() {
        return this.needAccessControl;
    }

    public void needAccessControl(boolean needAccessControl) {
        this.needAccessControl = needAccessControl;
    }

    public boolean inAllowsSet(String ip) {
//        return allowsSet.contains(ip);
        for (String allowsIp : allowsSet) {
            if (ip.startsWith(allowsIp)) {
                return true;
            }
        }
        return false;
    }
}
