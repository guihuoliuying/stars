package com.stars.core.db;

import com.stars.util.LogUtil;
import com.stars.util._HashMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: 西游online
 * Date: 13-5-23
 * Time: 上午9:48
 * 结果结合转换
 */
public class ResultSetHandler {
    public static String SEMICOLON_REPLACE_STR="@^";//分号替换字符
    public static String SQL_SPLIT = ";";

    public static <T> List<T> toList(ResultSet rs, Class<T> clazz, ObjectMetaData objectMetaData) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        List<T> list = new ArrayList<T>();
        ValueGetter valGetter = getValueGetter(clazz);
        if (clazz == com.stars.util._HashMap.class) {
            return (List<T>) toMapList(rs);

        } else if (valGetter instanceof ObjectValueGetter) {
            while (rs.next()) {
                list.add(toObject(rs, clazz, objectMetaData));
            }

        } else {
            while (rs.next()) {
                list.add((T) valGetter.get(rs, null));
            }
        }
        return list;
    }

    public static Map toMap(ResultSet rs, Class valClass, ObjectMetaData objectMetaData, String keyField, Class<? extends Map> mapClass) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Map map = mapClass.newInstance();
        //
        ResultSetMetaData rsmd = rs.getMetaData();
        int keyFieldIndex = 0;
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (rsmd.getColumnName(i).equals(keyField)) {
                keyFieldIndex = i;
                break;
            }
        }
        String valField = null;
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (!rsmd.getColumnName(i).equals(keyField)) {
                valField = rsmd.getColumnName(i);
                break;
            }
        }
        //
        ValueGetter keyGetter = getValueGetter(rsmd.getColumnTypeName(keyFieldIndex));
        ValueGetter valGetter = getValueGetter(valClass);
        if (valGetter instanceof ObjectValueGetter) {
            while (rs.next()) {
                map.put(keyGetter.get(rs, keyField), toObject(rs, valClass, objectMetaData));
            }
        } else {
            while (rs.next()) {
                map.put(keyGetter.get(rs, keyField), valGetter.get(rs, valField));
            }
        }
        return map;
    }

    public static <T> T toObject(ResultSet rs, Class<T> clazz, ObjectMetaData objectMetaData) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        T bean = clazz.newInstance();
        Iterator<Map.Entry<String, Method>> iter = objectMetaData.setterMethodIterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String fieldName = entry.getKey().toString();
            Method method = (Method) entry.getValue();

            String parameterType = method.getParameterTypes()[0].getName();
            Object tempObj = getFieldValue(rs, fieldName, parameterType);

            if (tempObj == null) continue;
            try {
                method.invoke(bean, tempObj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                String methodString = method.toString();
//                LogUtil.error("toObject() Getter/Setter参数不对应: " + clazz.getSimpleName() + "." + methodString.substring(methodString.lastIndexOf(".")+1)
//                        + ", 实参类型:" + tempObj.getClass().getSimpleName() + ", 实参值:" + tempObj.toString());
                com.stars.util.LogUtil.error("toObject() Getter/Setter参数不对应: " + clazz.getSimpleName() + "." + method.getName()
                        + ", 实参类型:" + tempObj.getClass().getSimpleName() + ", 实参值:" + tempObj.toString());
                throw new SQLException(e);
            }
        }
        return bean;
    }

    /**
     * 将结果集转换成list集合
     *
     * @param rst
     * @return
     * @throws java.sql.SQLException
     */
    public static List<com.stars.util._HashMap> toMapList(ResultSet rst) throws SQLException {
        List<com.stars.util._HashMap> list = new ArrayList<>();
        ResultSetMetaData rsmd = rst.getMetaData();
        while (rst.next()) {
            com.stars.util._HashMap dataMap = new _HashMap();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                //由于考虑到多表连接查询的时候，有相同的字段，所以将数据存入map集合的时候key值= 表的别名+"."+字段名
                //没有取别名那么默认别名和表名一致
                dataMap.put(rsmd.getTableName(i) + "." + rsmd.getColumnName(i),
                        rst.getObject(i) == null ? "" : rst.getObject(i));
            }
            list.add(dataMap);
        }
        return list;
    }

    private static Object getFieldValue(ResultSet rst, String fieldName, String paramType) throws SQLException {
        Object value;
        // todo: 为什么要对short和byte做特殊处理
        try {
            if (paramType.equals("short")) {
                value = rst.getShort(fieldName);
            } else if (paramType.equals("byte")) {
                value = rst.getByte(fieldName);
            } else if (paramType.equals("java.lang.String")) {
                String temp = rst.getString(fieldName);
                if (temp != null && temp.contains(SEMICOLON_REPLACE_STR)) {
                    temp = temp.replace(SEMICOLON_REPLACE_STR, SQL_SPLIT);
                }
                value = temp;
            } else {
                value = rst.getObject(fieldName);
            }
        } catch (Throwable t) {
            LogUtil.error("getFieldValue|fieldName:" + fieldName + "|paramType:" + paramType, t);
            throw t;
        }
        return value;
    }

    public static ValueGetter getValueGetter(String typeName) {
        switch (typeName.toLowerCase()) {
            case "tinyint":
                return new ByteValueGetter();
            case "smallint":
                return new ShortValueGetter();
            case "bigint":
                return new LongValueGetter();
            default:
                return new ObjectValueGetter();
        }
    }

    public static ValueGetter getValueGetter(Class clazz) {
        if (clazz == Byte.class) {
            return new ByteValueGetter();
        } else if (clazz == Short.class) {
            return new ShortValueGetter();
        } else if (clazz == Integer.class) {
            return new IntValueGetter();
        } else if (clazz == Long.class) {
            return new LongValueGetter();
        } else if (clazz == Float.class) {
            return new FloatValueGetter();
        } else if (clazz == Double.class) {
            return new DoubleValueGetter();
        } else if (clazz == String.class) {
            return new StringValueGetter();
        } else {
            return new ObjectValueGetter();
        }
    }

}

interface ValueGetter {
    Object get(ResultSet rs, String fieldName) throws SQLException;
}

class ByteValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getByte(1) : rs.getByte(fieldName);
    }
}

class ShortValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getShort(1) : rs.getShort(fieldName);
    }
}

class IntValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getInt(1) : rs.getInt(fieldName);
    }
}

class LongValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getLong(1) : rs.getLong(fieldName);
    }
}

class FloatValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getFloat(1) : rs.getFloat(fieldName);
    }
}

class DoubleValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getDouble(1) : rs.getDouble(fieldName);
    }
}

class StringValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getString(1) : rs.getString(fieldName);
    }
}

class ObjectValueGetter implements ValueGetter {
    @Override
    public Object get(ResultSet rs, String fieldName) throws SQLException {
        return fieldName == null ? rs.getObject(1) : rs.getObject(fieldName);
    }
}
