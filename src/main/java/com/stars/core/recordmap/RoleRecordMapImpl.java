package com.stars.core.recordmap;

import com.stars.core.module.ModuleContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.stars.core.recordmap.RoleRecord.*;

/**
 * byte, short, int, long
 * float, double
 * String
 * Created by zhaowenshuo on 2016/7/26.
 */
public class RoleRecordMapImpl implements RecordMap {

    private long roleId;
    private Map<String, RoleRecord> recordMap;
    private ModuleContext context;

    public RoleRecordMapImpl(long roleId, ModuleContext context, Map<String, RoleRecord> recordMap) {
        this.roleId = roleId;
        this.context = context;
        this.recordMap = recordMap;
    }

    private RoleRecord getRecordAsMap(String key, boolean buildNotExist) {
        RoleRecord record = recordMap.get(key);
        if (record == null) {
            if (buildNotExist) {
                record = new RoleRecord(TYPE_MAP, roleId, key, null);
                recordMap.put(key, record);
                context.insert(record);
            }
        }
        return null;
    }

    private RoleRecord getRecordAsList(String key, boolean buildNotExist) {
        RoleRecord record = recordMap.get(key);
        if (record == null) {
            if (buildNotExist) {
                record = new RoleRecord(TYPE_LIST, roleId, key, null);
                recordMap.put(key, record);
                context.insert(record);
            }
        }
        return null;
    }

    public <K, V> Map<K, V> getMap(String key, boolean buildNotExist, Class<K> keyClass, Class<V> valClass) {
        RoleRecord record = getRecordAsMap(key, buildNotExist);
        return record == null ? null
                : new RecordValMap<>(keyClass, valClass, record.getValMap(), record, context);
    }

    public <E> List<E> getList(String key, boolean buildNotExist, Class<E> elemClass) {
        RoleRecord record = getRecordAsList(key, buildNotExist);
        return record == null ? null
                : new RecordValList<>(elemClass, record.getValList(), record, context);
    }

    /*
     * string
     */
    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        String valueString = null;
        if (recordMap.get(key) != null) {
            valueString = recordMap.get(key).getRecordVal();
        }
        return valueString != null ? valueString : defaultValue;
    }

    public void setString(String key, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        RoleRecord record = recordMap.get(key);
        if (record == null) {
            record = new RoleRecord(TYPE_STR, roleId, key, value);
            recordMap.put(key, record);
            context.insert(record);
        } else {
            record.setRecordVal(value);
            context.update(record);
        }
    }

    /*
     * string from/to map
     */
    @Override
    public String getStringFromMap(String key, String field) {
        return getStringFromMap(key, field, null);
    }

    @Override
    public String getStringFromMap(String key, String field, String defaultValue) {
        RoleRecord record = recordMap.get(key);
        if (record != null) {
            String valueString = record.getValFromMap(field);
            return valueString != null ? valueString : defaultValue;
        }
        return defaultValue;
    }

    @Override
    public void setStringToMap(String key, String field, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(field);
        Objects.requireNonNull(value);
        RoleRecord record = recordMap.get(key);
        if (record == null) {
            record = new RoleRecord(TYPE_MAP, roleId, key, null);
            record.setValToMap(field, value);
            recordMap.put(key, record);
            context.insert(record);
        } else {
            record.setValToMap(field, value);
            context.update(record);
        }
    }

    /*
     * byte
     */
    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    public byte getByte(String key, byte defaultValue) {
        String valueString = getString(key);
        return valueString != null ? Byte.parseByte(valueString) : defaultValue;
    }

    public void setByte(String key, byte value) {
        setString(key, Byte.toString(value));
    }

    /*
     * byte from/to map
     */

    @Override
    public byte getByteFromMap(String key, String field) {
        return getByteFromMap(key, field, (byte) 0);
    }

    @Override
    public byte getByteFromMap(String key, String field, byte defaultValue) {
        String valueString = getStringFromMap(key, field);
        return valueString != null ? Byte.parseByte(valueString) : defaultValue;
    }

    @Override
    public void setByteToMap(String key, String field, byte value) {
        setStringToMap(key, field, Byte.toString(value));
    }

    /*
     * short
     */
    public short getShort(String key) {
        return getShort(key, (byte) 0);
    }

    public short getShort(String key, short defaultValue) {
        String valueString = getString(key);
        return valueString != null ? Short.parseShort(valueString) : defaultValue;
    }

    public void setShort(String key, short value) {
        setString(key, Short.toString(value));
    }

    /*
     * short from/to map
     */

    @Override
    public short getShortFromMap(String key, String field) {
        return getByteFromMap(key, field);
    }

    @Override
    public short getShortFromMap(String key, String field, short defaultValue) {
        String valueString = getStringFromMap(key, field);
        return valueString != null ? Short.parseShort(valueString) : defaultValue;
    }

    @Override
    public void setShortToMap(String key, String field, short value) {
        setStringToMap(key, field, Short.toString(value));
    }

    /*
     * int
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        String valueString = getString(key);
        return valueString != null ? Integer.parseInt(valueString) : defaultValue;
    }

    public void setInt(String key, int value) {
        setString(key, Integer.toString(value));
    }

    /*
     * int from/to map
     */

    @Override
    public int getIntFromMap(String key, String field) {
        return getIntFromMap(key, field, 0);
    }

    @Override
    public int getIntFromMap(String key, String field, int defaultValue) {
        String valueString = getStringFromMap(key, field);
        return valueString != null ? Integer.parseInt(valueString) : defaultValue;
    }

    @Override
    public int getIntFromMap(String key, int field) {
        return getIntFromMap(key, Integer.toString(field));
    }

    @Override
    public int getIntFromMap(String key, int field, int defaultValue) {
        return getIntFromMap(key, Integer.toString(field), defaultValue);
    }

    @Override
    public void setIntToMap(String key, String field, int value) {
        setStringToMap(key, field, Integer.toString(value));
    }

    @Override
    public void setIntToMap(String key, int field, int value) {
        setIntToMap(key, Integer.toString(field), value);
    }

    /*
         * long
         */
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(String key, long defaultValue) {
        String valueString = getString(key);
        return valueString != null ? Long.parseLong(valueString) : defaultValue;
    }

    public void setLong(String key, long value) {
        setString(key, Long.toString(value));
    }

    /*
     * long from/to map
     */

    @Override
    public long getLongFromMap(String key, String field) {
        return getLongFromMap(key, field, 0);
    }

    @Override
    public long getLongFromMap(String key, String field, long defaultValue) {
        String valueString = getStringFromMap(key, field);
        return valueString != null ? Long.parseLong(valueString) : defaultValue;
    }

    @Override
    public void setLongToMap(String key, String field, long value) {
        setStringToMap(key, field, Long.toString(value));
    }

    /*
     * float
     */
    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public float getFloat(String key, float defaultValue) {
        String valueString = getString(key);
        return valueString != null ? Float.parseFloat(valueString) : defaultValue;
    }

    public void setFloat(String key, float value) {
        setString(key, Float.toString(value));
    }

    /*
     * float from/to map
     */

    @Override
    public float getFloatFromMap(String key, String field) {
        return getFloatFromMap(key, field, 0.0F);
    }

    @Override
    public float getFloatFromMap(String key, String field, float defaultValue) {
        String valueString = getStringFromMap(key, field);
        return valueString != null ? Float.parseFloat(valueString) : defaultValue;
    }

    @Override
    public void setFloatToMap(String key, String field, float value) {
        setStringToMap(key, field, Float.toString(value));
    }

    /*
     * double
     */
    public double getDouble(String key) {
        return getDouble(key, 0.0d);
    }

    public double getDouble(String key, double defaultValue) {
        String valueString = getString(key);
        return valueString != null ? Double.parseDouble(valueString) : defaultValue;
    }

    public void setDouble(String key, double value) {
        setString(key, Double.toString(value));
    }

    /*
     * double from/to map
     */

    @Override
    public double getDoubleFromMap(String key, String field) {
        return getDoubleFromMap(key, field, 0.0D);
    }

    @Override
    public double getDoubleFromMap(String key, String field, double defaultValue) {
        String valueString = getStringFromMap(key, field);
        return valueString != null ? Double.parseDouble(valueString) : defaultValue;
    }

    @Override
    public void setDoubleToMap(String key, String field, double value) {
        setStringToMap(key, field, Double.toString(value));
    }

    public void clearValueMap(String key) {
        RoleRecord record = recordMap.get(key);
        if (record != null) {
            record.clearValueMap();
            context.update(record);
        }
    }

    public void delete(String key) {
        RoleRecord record = recordMap.get(key);
        if (record != null) {
            recordMap.remove(key);
            context.delete(record);
        }
    }

    @Override
    public String toString() {
        return recordMap.toString();
    }
}
