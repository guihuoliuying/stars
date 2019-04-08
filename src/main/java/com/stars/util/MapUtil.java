package com.stars.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zhaowenshuo on 2016/7/18.
 */
public class MapUtil {

    public static byte getByte(Map<String, String> map, String key, byte defaultValue) {
        String val = map.get(key);
        return val == null || val.trim().equals("") ? defaultValue : Byte.parseByte(val);
    }

    public static byte getByte(Map<String, String> map, String key) {
        return getByte(map, key, (byte) 0);
    }

    public static byte getByte(Map<String, String> map, String key, String delimiter, int index, byte defaultValue) {
        String val = map.get(key).split(delimiter)[index];
        return val == null || val.trim().equals("") ? defaultValue : Byte.parseByte(val);
    }

    public static byte getByteKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index, byte defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[0];
        return val == null || val.trim().equals("") ? defaultValue : Byte.parseByte(val);
    }

    public static byte getByteKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index) {
        return getByteKey(map, key, delimiter, mappingSign, index, (byte) 0);
    }

    public static byte getByteVal(Map<String, String> map, String key, String delimiter, String mappingSign, int index, byte defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[1];
        return val == null || val.trim().equals("") ? defaultValue : Byte.parseByte(val);
    }

    public static short getShort(Map<String, String> map, String key, short defaultValue) {
        String val = map.get(key);
        return val == null || val.trim().equals("") ? defaultValue : Short.parseShort(val);
    }

    public static short getShort(Map<String, String> map, String key) {
        return getShort(map, key, (short) 0);
    }

    public static short getShort(Map<String, String> map, String key, String delimiter, int index, short defaultValue) {
        String val = map.get(key).split(delimiter)[index];
        return val == null || val.trim().equals("") ? defaultValue : Short.parseShort(val);
    }

    public static short getShortKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index, short defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[0];
        return val == null || val.trim().equals("") ? defaultValue : Short.parseShort(val);
    }

    public static short getShortVal(Map<String, String> map, String key, String delimiter, String mappingSign, int index, short defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[1];
        return val == null || val.trim().equals("") ? defaultValue : Short.parseShort(val);
    }

    public static int getInt(Map<String, String> map, String key, int defaultValue) {
        String val = map.get(key);
        return val == null || val.trim().equals("") ? defaultValue : Integer.parseInt(val);
    }

    public static int getInt(Map<String, String> map, String key) {
        return getInt(map, key, 0);
    }

    public static int getInt(Map<String, String> map, String key, String delimiter, int index, int defaultValue) {
        String val = map.get(key).split(delimiter)[index];
        return val == null || val.trim().equals("") ? defaultValue : Integer.parseInt(val);
    }

    public static int getIntKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index, int defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[0];
        return val == null || val.trim().equals("") ? defaultValue : Integer.parseInt(val);
    }

    public static int getIntVal(Map<String, String> map, String key, String delimiter, String mappingSign, int index, int defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[1];
        return val == null || val.trim().equals("") ? defaultValue : Integer.parseInt(val);
    }

    public static long getLong(Map<String, String> map, String key, long defaultValue) {
        String val = map.get(key);
        return val == null || val.trim().equals("") ? defaultValue : Long.parseLong(val);
    }

    public static long getLong(Map<String, String> map, String key) {
        return getLong(map, key, 0L);
    }

    public static long getLong(Map<String, String> map, String key, String delimiter, int index, long defaultValue) {
        String val = map.get(key).split(delimiter)[index];
        return val == null || val.trim().equals("") ? defaultValue : Long.parseLong(val);
    }

    public static long getLongKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index, long defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[0];
        return val == null || val.trim().equals("") ? defaultValue : Long.parseLong(val);
    }

    public static long getLongVal(Map<String, String> map, String key, String delimiter, String mappingSign, int index, long defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[1];
        return val == null || val.trim().equals("") ? defaultValue : Long.parseLong(val);
    }

    public static float getFloat(Map<String, String> map, String key, float defaultValue) {
        String val = map.get(key);
        return val == null || val.trim().equals("") ? defaultValue : Float.parseFloat(val);
    }

    public static float getFloat(Map<String, String> map, String key) {
        return getFloat(map, key, 0.0F);
    }

    public static float getFloat(Map<String, String> map, String key, String delimiter, int index, float defaultValue) {
        String val = map.get(key).split(delimiter)[index];
        return val == null || val.trim().equals("") ? defaultValue : Float.parseFloat(val);
    }

    public static float getFloatKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index, float defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[0];
        return val == null || val.trim().equals("") ? defaultValue : Float.parseFloat(val);
    }

    public static float getFloatVal(Map<String, String> map, String key, String delimiter, String mappingSign, int index, float defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[1];
        return val == null || val.trim().equals("") ? defaultValue : Float.parseFloat(val);
    }

    public static double getDouble(Map<String, String> map, String key, double defaultValue) {
        String val = map.get(key);
        return val == null || val.trim().equals("") ? defaultValue : Double.parseDouble(val);
    }

    public static double getDouble(Map<String, String> map, String key) {
        return getDouble(map, key, 0.0D);
    }

    public static double getDouble(Map<String, String> map, String key, String delimiter, int index, double defaultValue) {
        String val = map.get(key).split(delimiter)[index];
        return val == null || val.trim().equals("") ? defaultValue : Double.parseDouble(val);
    }

    public static double getDoubleKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index, double defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[0];
        return val == null || val.trim().equals("") ? defaultValue : Double.parseDouble(val);
    }

    public static double getDoubleVal(Map<String, String> map, String key, String delimiter, String mappingSign, int index, double defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[1];
        return val == null || val.trim().equals("") ? defaultValue : Double.parseDouble(val);
    }

    public static String getString(Map<String, String> map, String key, String defaultValue) {
        String val = map.get(key);
        return val == null || val.trim().equals("") ? defaultValue : val;
    }

    public static String getString(Map<String, String> map, String key) {
        return getString(map, key, "");
    }

    public static String getString(Map<String, String> map, String key, String delimiter, int index, String defaultValue) {
        String val = map.get(key).split(delimiter)[index];
        return val == null || val.trim().equals("") ? defaultValue : val;
    }

    public static String getStringKey(Map<String, String> map, String key, String delimiter, String mappingSign, int index, String defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[0];
        return val == null || val.trim().equals("") ? defaultValue : val;
    }

    public static String getStringVal(Map<String, String> map, String key, String delimiter, String mappingSign, int index, String defaultValue) {
        String val = map.get(key).split(delimiter)[index].split(mappingSign)[1];
        return val == null || val.trim().equals("") ? defaultValue : val;
    }


    /* setter */
    public static void setByte(Map<String, String> map, String key, byte value) {
        map.put(key, Byte.toString(value));
    }

    public static void setShort(Map<String, String> map, String key, short value) {
        map.put(key, Short.toString(value));
    }

    public static void setInt(Map<String, String> map, String key, int value) {
        map.put(key, Integer.toString(value));
    }

    public static void setLong(Map<String, String> map, String key, long value) {
        map.put(key, Long.toString(value));
    }

    public static void setFloat(Map<String, String> map, String key, float value) {
        map.put(key, Float.toString(value));
    }

    public static void setDouble(Map<String, String> map, String key, double value) {
        map.put(key, Double.toString(value));
    }

    public static void setString(Map<String, String> map, String key, String value) {
        map.put(key, value == null ? "" : value);
    }

    /* 集合类操作 */
    public static <K, V extends Number> V sum(Map<K, V> targetMap, V defaultValue) {
        Objects.requireNonNull(targetMap);
        V ret = defaultValue;
        if (targetMap.size() == 0) {
            return ret;
        }
        Class<V> oprandClass = findClass(targetMap);
        Operator<V> op = getOperator(oprandClass);
        if (op == null) {
            throw new IllegalArgumentException("Unsupport Class: " + oprandClass);
        }

        for (Map.Entry<K, V> entry : targetMap.entrySet()) {
            K key = entry.getKey();
            V leftVal = targetMap.get(key);
            if (ret == null) {
                ret = leftVal;
            } else {
                ret = op.add(leftVal, ret);
            }
        }
        return ret;
    }

    public static <K, V extends Number> void add(Map<K, V> targetMap, Map<K, V> deltaMap) {
        Objects.requireNonNull(targetMap);
        if (deltaMap == null || deltaMap.size() == 0) {
            return;
        }
        Class<V> oprandClass = findClass(deltaMap);
        Operator<V> op = getOperator(oprandClass);
        if (op == null) {
            throw new IllegalArgumentException("Unsupport Class: " + oprandClass);
        }
        for (Map.Entry<K, V> entry : deltaMap.entrySet()) {
            K key = entry.getKey();
            V leftVal = targetMap.get(key);
            V rightVal = deltaMap.get(key);
            if (leftVal != null) {
                targetMap.put(key, op.add(leftVal, rightVal));
            } else {
                targetMap.put(key, rightVal);
            }
        }
    }

    public static <K, V extends Number> void add(Map<K, V> targetMap, V rightVal) {
        Objects.requireNonNull(targetMap);

        Class<V> oprandClass = (Class<V>) rightVal.getClass();
        Operator<V> op = getOperator(oprandClass);
        if (op == null) {
            throw new IllegalArgumentException("Unsupport Class: " + oprandClass);
        }
        Map<K, V> tmpMap = new HashMap<>(targetMap);
        for (Map.Entry<K, V> entry : tmpMap.entrySet()) {
            K key = entry.getKey();
            V leftVal = targetMap.get(key);
            if (leftVal != null) {
                targetMap.put(key, op.add(leftVal, rightVal));
            } else {
                targetMap.put(key, rightVal);
            }
        }
    }

    public static <K, V extends Number> void subtract(Map<K, V> targetMap, Map<K, V> deltaMap) {
        Objects.requireNonNull(targetMap);
        if (deltaMap == null || deltaMap.size() == 0) {
            return;
        }
        Class<V> oprandClass = findClass(deltaMap);
        Operator<V> op = getOperator(oprandClass);
        if (op == null) {
            throw new IllegalArgumentException("Unsupport Class: " + oprandClass);
        }
        for (Map.Entry<K, V> entry : deltaMap.entrySet()) {
            K key = entry.getKey();
            V leftVal = targetMap.get(key);
            V rightVal = deltaMap.get(key);
            if (leftVal != null) {
                targetMap.put(key, op.subtract(leftVal, rightVal));
            } else {
                targetMap.put(key, op.subtract(0, rightVal));
            }
        }
    }

    public static <K, V extends Number> void subtract(Map<K, V> targetMap, V rightVal) {
        Objects.requireNonNull(targetMap);

        Class<V> oprandClass = (Class<V>) rightVal.getClass();
        Operator<V> op = getOperator(oprandClass);
        if (op == null) {
            throw new IllegalArgumentException("Unsupport Class: " + oprandClass);
        }
        Map<K, V> tmpMap = new HashMap<>(targetMap);
        for (Map.Entry<K, V> entry : tmpMap.entrySet()) {
            K key = entry.getKey();
            V leftVal = targetMap.get(key);
            if (leftVal != null) {
                targetMap.put(key, op.subtract(leftVal, rightVal));
            } else {
                targetMap.put(key, op.subtract(0, rightVal));
            }
        }
    }

    public static <K, V extends Number> void multiply(Map<K, V> targetMap, Map<K, V> deltaMap) {
        Objects.requireNonNull(targetMap);
        if (deltaMap == null || deltaMap.size() == 0) {
            return;
        }
        Class<V> oprandClass = findClass(deltaMap);
        Operator<V> op = getOperator(oprandClass);
        if (op == null) {
            throw new IllegalArgumentException("Unsupport Class: " + oprandClass);
        }
        for (Map.Entry<K, V> entry : deltaMap.entrySet()) {
            K key = entry.getKey();
            V leftVal = targetMap.get(key);
            V rightVal = deltaMap.get(key);
            if (leftVal != null) {
                targetMap.put(key, op.multiply(leftVal, rightVal));
            }
        }
    }

    public static <K, V extends Number> void multiply(Map<K, V> targetMap, V rightVal) {
        Objects.requireNonNull(targetMap);

        Class<V> oprandClass = (Class<V>) rightVal.getClass();
        Operator<V> op = getOperator(oprandClass);
        if (op == null) {
            throw new IllegalArgumentException("Unsupport Class: " + oprandClass);
        }
        Map<K, V> tmpMap = new HashMap<>(targetMap);
        for (Map.Entry<K, V> entry : tmpMap.entrySet()) {
            K key = entry.getKey();
            V leftVal = targetMap.get(key);
            if (leftVal != null) {
                targetMap.put(key, op.multiply(leftVal, rightVal));
            }
        }
    }

    private static <K, V> Class<V> findClass(Map<K, V> map) {
        Iterator<V> iterator = map.values().iterator();
        if (iterator.hasNext()) {
            return (Class<V>) iterator.next().getClass();
        }
        return null;
    }

    private static <T extends Number> Operator<T> getOperator(Class<T> clazz) {
        if (clazz == Byte.class) {
            return (Operator<T>) new ByteOperator();
        } else if (clazz == Short.class) {
            return (Operator<T>) new ShortOperator();
        } else if (clazz == Integer.class) {
            return (Operator<T>) new IntegerOperator();
        } else if (clazz == Long.class) {
            return (Operator<T>) new LongOperator();
        } else if (clazz == Float.class) {
            return (Operator<T>) new FloatOperator();
        } else if (clazz == Double.class) {
            return (Operator<T>) new DoubleOperator();
        }
        return null;
    }

    private interface Operator<T extends Number> {
        T add(Number left, Number right);
        T subtract(Number left, Number right);
        T multiply(Number left, Number right);
    }

    private static class ByteOperator implements Operator<Byte> {
        @Override
        public Byte add(Number left, Number right) {
            return (byte) (left.byteValue() + right.byteValue());
        }

        @Override
        public Byte subtract(Number left, Number right) {
            return (byte) (left.byteValue() - right.byteValue());
        }

        @Override
        public Byte multiply(Number left, Number right) {
            return (byte) (left.byteValue() * right.byteValue());
        }
    }

    private static class ShortOperator implements Operator<Short> {
        @Override
        public Short add(Number left, Number right) {
            return (short) (left.shortValue() + right.shortValue());
        }

        @Override
        public Short subtract(Number left, Number right) {
            return (short) (left.shortValue() - right.shortValue());
        }

        @Override
        public Short multiply(Number left, Number right) {
            return (short) (left.shortValue() * right.shortValue());
        }
    }

    private static class IntegerOperator implements Operator<Integer> {
        @Override
        public Integer add(Number left, Number right) {
            return left.intValue() + right.intValue();
        }

        @Override
        public Integer subtract(Number left, Number right) {
            return left.intValue() - right.intValue();
        }

        @Override
        public Integer multiply(Number left, Number right) {
            return left.intValue() * right.intValue();
        }
    }

    private static class LongOperator implements Operator<Long> {
        @Override
        public Long add(Number left, Number right) {
            return left.longValue() + right.longValue();
        }

        @Override
        public Long subtract(Number left, Number right) {
            return left.longValue() - right.longValue();
        }

        @Override
        public Long multiply(Number left, Number right) {
            return (long) (left.longValue() * right.longValue());
        }
    }

    private static class FloatOperator implements Operator<Float> {
        @Override
        public Float add(Number left, Number right) {
            return left.floatValue() + right.floatValue();
        }

        @Override
        public Float subtract(Number left, Number right) {
            return left.floatValue() - right.floatValue();
        }

        @Override
        public Float multiply(Number left, Number right) {
            return left.floatValue() * right.floatValue();
        }
    }

    private static class DoubleOperator implements Operator<Double> {
        @Override
        public Double add(Number left, Number right) {
            return left.doubleValue() + right.doubleValue();
        }

        @Override
        public Double subtract(Number left, Number right) {
            return left.doubleValue() - right.doubleValue();
        }

        @Override
        public Double multiply(Number left, Number right) {
            return left.doubleValue() * right.doubleValue();
        }
    }

}
