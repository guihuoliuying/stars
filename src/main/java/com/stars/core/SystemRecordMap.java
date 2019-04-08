package com.stars.core;

import com.stars.db.DBUtil;
import com.stars.util.LogUtil;
import com.stars.util._HashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

/**
 * 提供系统的数据存储操作（针对非频繁更新操作的，因为操作是互斥的，并且实时入库的）
 * <p>
 * Created by zhaowenshuo on 2016/7/27.
 */
public class SystemRecordMap {

    public static volatile long dailyResetTimestamp;
    public static volatile long fiveOClockResetTimestamp;   //每日凌晨五点重置时间戳
    public static volatile int dateVersion;
    public static volatile long weeklyResetTimestamp;       //每周凌晨五点重置时间戳
    public static volatile long monthlyResetTimestamp;
    public static volatile long familyTreasureResetTimestamp;//家族探宝重置时间戳
    public static volatile long campReputationResetTimestamp;//阵营声望重置时间戳
    public static volatile long openServerTime; // yyyyMMddHHmmss;
//    public static volatile long opActSceondKillResetTimestamp;//限时秒杀重置时间戳


    public static volatile int lastLockedSkyRankSeasonId; //上次锁定的赛季id
    public static volatile int lastAwardedSkyRankSeasonId;//上次奖励的赛季id
    public static volatile int lastResetSkyRankSeasonId; //上次重置的赛季id

    public static volatile String forbidCollectPhoneChannels;   //收集手机号码禁止渠道列表

    public static synchronized void load() throws Throwable {
        _HashMap map = new _HashMap();
        map.putAll(DBUtil.queryMap(DBUtil.DB_USER, "recordkey", String.class, "select * from `systemrecords`"));

        Iterator<Map.Entry> itor = map.entrySet().iterator();
        while (itor.hasNext()) {
            Map.Entry entry = itor.next();
            String key = (String) entry.getKey();
            int index = key.lastIndexOf('.') + 1;
            Field field = SystemRecordMap.class.getDeclaredField(key.substring(index));
            if (!Modifier.isVolatile(field.getModifiers())) {
                throw new RuntimeException("属性" + field.getName() + "缺少修饰符volatile");
            }
            Class clazz = field.getType();
            if (clazz == String.class) {
                field.set(SystemRecordMap.class, map.getString(key));
            } else if (clazz == Byte.class || clazz == byte.class) {
                field.set(SystemRecordMap.class, map.getByte(key));
            } else if (clazz == Short.class || clazz == short.class) {
                field.set(SystemRecordMap.class, map.getShort(key));
            } else if (clazz == Integer.class || clazz == int.class) {
                field.set(SystemRecordMap.class, map.getInt(key));
            } else if (clazz == Long.class || clazz == long.class) {
                field.set(SystemRecordMap.class, map.getLong(key));
            } else if (clazz == Float.class || clazz == float.class) {
                field.set(SystemRecordMap.class, map.getFloat(key));
            } else if (clazz == Double.class || clazz == double.class) {
                field.set(SystemRecordMap.class, map.getDouble(key));
            }
        }
    }

    private static synchronized void setValue(String key, Object value) {
        try {
            // 设置变量的值
            int index = key.lastIndexOf('.') + 1;
            Field field = SystemRecordMap.class.getField(key.substring(index));
            field.set(SystemRecordMap.class, value);
        } catch (Throwable t) {
            LogUtil.error("", t);
        }
    }

    private static synchronized void saveToDatabase(String key, String value) {
        Connection connection = null;
        Statement st = null;
        try {
            // 入库
            String sql = "update `systemrecords` set `recordval`='" + value + "' where `recordkey`='" + key + "'";
//            DBUtil.execUserSql(sql);
            connection = DBUtil.getConnection(DBUtil.DB_USER);
            st = connection.createStatement();
            int rowEffect = st.executeUpdate(sql);
            if (rowEffect == 0) {
                sql = "insert into `systemrecords` values ('" + key + "','" + value + "')";
                st.execute(sql);
            }
        } catch (Throwable t) {
            LogUtil.error("", t);
        } finally {
            DBUtil.closeStatement(st);
            DBUtil.closeConnection(connection);
        }
    }

    public static synchronized void update(String key, String value) {
        setValue(key, value);
        saveToDatabase(key, value);
    }

    public static synchronized void update(String key, byte value) {
        setValue(key, value);
        saveToDatabase(key, Byte.toString(value));
    }

    public static synchronized void update(String key, short value) {
        setValue(key, value);
        saveToDatabase(key, Short.toString(value));
    }

    public static synchronized void update(String key, int value) {
        setValue(key, value);
        saveToDatabase(key, Integer.toString(value));
    }

    public static synchronized void update(String key, long value) {
        setValue(key, value);
        saveToDatabase(key, Long.toString(value));
    }

    public static synchronized void update(String key, float value) {
        setValue(key, value);
        saveToDatabase(key, Float.toString(value));
    }

    public static synchronized void update(String key, double value) {
        setValue(key, value);
        saveToDatabase(key, Double.toString(value));
    }


}
