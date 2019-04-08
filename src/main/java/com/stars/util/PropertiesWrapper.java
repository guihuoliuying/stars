package com.stars.util;

import java.util.Properties;

/**
 * Created by zhaowenshuo on 2016/3/2.
 */
public class PropertiesWrapper {

    private Properties props;

    public PropertiesWrapper(Properties props) {
        this.props = props;
    }

    public byte getByte(String key, byte defaultValue) {
        String val = props.getProperty(key);
        return val != null ? Byte.parseByte(val) : defaultValue;
    }

    public short getShort(String key, short defaultValue) {
        String val = props.getProperty(key);
        return val != null ? Short.parseShort(val) : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String val = props.getProperty(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        String val = props.getProperty(key);
        return val != null ? Long.parseLong(val) : defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        String val = props.getProperty(key);
        return val != null ? Float.parseFloat(val) : defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        String val = props.getProperty(key);
        return val != null ? Double.parseDouble(val) : defaultValue;
    }

    public String getString(String key, String defaultValue) {
        String val = props.getProperty(key);
        return val != null ? val : defaultValue;
    }
}
