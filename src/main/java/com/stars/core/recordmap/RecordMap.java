package com.stars.core.recordmap;

import java.util.List;
import java.util.Map;

/**
 * 提供统一的小规模数据的存储接口（不需要单独建表，适合日常活动这样的小需求）
 *
 * 1. 调用setXXX()方法会自动进行保存（根据记录的状态调用context().insert()或context().update()）
 * 2. 调用delete()方法会自动进行删除。
 * 3. key的命名规则推荐格式：moduleName[.submoduleName].XXXX，比如：
 *    login.firstTimestamp
 *    login.lastTimestamp
 *    kfpvp.pk.mapId
 *    kfpvp.pk.position
 *    kfpvp.pk.time
 * Created by zhaowenshuo on 2016/7/26.
 */
public interface RecordMap {

    public <K, V> Map<K, V> getMap(String key, boolean buildNotExist, Class<K> keyClass, Class<V> valClass);
    public <E> List<E> getList(String key, boolean buildNotExist, Class<E> elemClass);

    // string
    public String getString(String key);
    public String getString(String key, String defaultValue);
    public void setString(String key, String value);

    // string from/to map
    public String getStringFromMap(String key, String field);
    public String getStringFromMap(String key, String field, String defaultValue);
    public void setStringToMap(String key, String field, String value);

    // byte
    public byte getByte(String key);
    public byte getByte(String key, byte defaultValue);
    public void setByte(String key, byte value);

    // byte from/to map
    public byte getByteFromMap(String key, String field);
    public byte getByteFromMap(String key, String field, byte defaultValue);
    public void setByteToMap(String key, String field, byte value);

    // short
    public short getShort(String key);
    public short getShort(String key, short defaultValue);
    public void setShort(String key, short value);

    // short from/to map
    public short getShortFromMap(String key, String field);
    public short getShortFromMap(String key, String field, short defaultValue);
    public void setShortToMap(String key, String field, short value);

    // int
    public int getInt(String key);
    public int getInt(String key, int defaultValue);
    public void setInt(String key, int value);

    // int from/to map
    public int getIntFromMap(String key, String field);
    public int getIntFromMap(String key, String field, int defaultValue);
    public int getIntFromMap(String key, int field);
    public int getIntFromMap(String key, int field, int defaultValue);
    public void setIntToMap(String key, String field, int value);
    public void setIntToMap(String key, int field, int value);

    // long
    public long getLong(String key);
    public long getLong(String key, long defaultValue);
    public void setLong(String key, long value);

    // long from/to map
    public long getLongFromMap(String key, String field);
    public long getLongFromMap(String key, String field, long defaultValue);
    public void setLongToMap(String key, String field, long value);

    // float
    public float getFloat(String key);
    public float getFloat(String key, float defaultValue);
    public void setFloat(String key, float value);

    // float from/to map
    public float getFloatFromMap(String key, String field);
    public float getFloatFromMap(String key, String field, float defaultValue);
    public void setFloatToMap(String key, String field, float value);

    // double
    public double getDouble(String key);
    public double getDouble(String key, double defaultValue);
    public void setDouble(String key, double value);

    // double from/to map
    public double getDoubleFromMap(String key, String field);
    public double getDoubleFromMap(String key, String field, double defaultValue);
    public void setDoubleToMap(String key, String field, double value);

    public void clearValueMap(String key);
    public void delete(String key);

}
