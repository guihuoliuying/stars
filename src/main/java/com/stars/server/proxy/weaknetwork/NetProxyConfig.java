package com.stars.server.proxy.weaknetwork;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by zws on 2015/10/13.
 */
public class NetProxyConfig {
    private String upstreamIp;
    private int upstreamPort;
    private String proxyIp;
    private int proxyPort;
    private boolean isMock;

    public static NetProxyConfig load() throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream("./config/netproxy/config.properties"));
        NetProxyConfig config = new NetProxyConfig();
        config.upstreamIp = getString(prop, "upstream.ip", null);
        config.upstreamPort = getInt(prop, "upstream.port", -1);
        config.proxyIp = getString(prop, "proxy.ip", "127.0.0.1");
        config.proxyPort = getInt(prop, "proxy.port", 6888);
        config.isMock = config.upstreamIp == null;
        return config;
    }

    private static String getString(Properties prop, String key, String defaultValue) {
        String value = prop.getProperty(key);
        return "".equals(value.trim()) ? defaultValue : value;
    }

    private static int getInt(Properties prop, String key, int defaultValue) {
        String value = prop.getProperty(key);
        return (value == null || "".equals(value.trim()) || "default".equals(value.trim()))
                ? defaultValue : Integer.parseInt(value);
    }

    public String upstreamIp() {
        return upstreamIp;
    }

    public int upstreamPort() {
        return upstreamPort;
    }

    public String proxyIp() {
        return proxyIp;
    }

    public int proxyPort() {
        return proxyPort;
    }

    public boolean isMock() {
        return isMock;
    }
}
