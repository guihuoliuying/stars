package com.stars.db;


import com.stars.util.LogUtil;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by zhaowenshuo on 2016/7/18.
 */
public class ObjectMetaData {

    private Class clazz;
    private Map<String, Method> setterMethodMap = new HashMap<>();
    private Map<String, Method> getterMethodMap = new HashMap<>();
    private List<String> fieldNameList = new ArrayList<>();
    private List<String> insertSqlSegments = new ArrayList<>();
    private List<String> updateSqlSegments = new ArrayList<>();

    public ObjectMetaData(Class clazz) {
        this.clazz = clazz;
    }

    public void addField(String fieldName) {
        fieldNameList.add(fieldName);
    }

    public Method getSetterMethod(String fieldName) {
        return setterMethodMap.get(fieldName);
    }

    public void setSetterMethod(String fieldName, Method method) {
        setterMethodMap.put(fieldName, method);
    }

    public Method getGetterMethod(String fieldName) {
        return getterMethodMap.get(fieldName);
    }

    public void setGetterMethod(String fieldName, Method method) {
        getterMethodMap.put(fieldName, method);
    }

    public Iterator<Map.Entry<String, Method>> setterMethodIterator() {
        return setterMethodMap.entrySet().iterator();
    }

    public void finish() {
        generateInsertSqlSegments();
        generateUpdateSqlSegments();
    }

    private void generateInsertSqlSegments() {
        String sql = "(";
        int size = fieldNameList.size();
        for (int i = 0; i < size; i++) {
            sql += "`" + fieldNameList.get(i) + "`,";
            if (i == size - 1) {
                sql = sql.substring(0, sql.length()-1);
            }
        }
        sql += ")values(";
        insertSqlSegments.add(sql);
        for (int i = 0; i < size; i++) {
            sql = ",";
            if (i == size - 1) {
                sql = ")";
            }
            insertSqlSegments.add(sql);
        }
    }

    private void generateUpdateSqlSegments() {
        String sql = "set `" + fieldNameList.get(0) + "`=";
        updateSqlSegments.add(sql);
        int size = fieldNameList.size();
        for (int i = 1; i < size; i++) {
            sql = ",`" + fieldNameList.get(i) + "`=";
            updateSqlSegments.add(sql);
        }
    }

    public String getInsertSql(Object obj, String tableName) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("insert into ");
        boolean needBackQuote = !tableName.startsWith("`");
        if (needBackQuote) {
            sb.append("`");
        }
        sb.append(tableName);
        if (needBackQuote) {
            sb.append("` ");
        }
        int i = 0;
        for (; i < fieldNameList.size(); i++) {
            String fieldName = fieldNameList.get(i);
            Object value = null;
            sb.append(insertSqlSegments.get(i));
            try {
            	value = getterMethodMap.get(fieldName).invoke(obj); 
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            } 
            if (value == null) {
                sb.append("null");
            } else {
                sb.append("'").append(value.toString()).append("'");
            }
        }
        for (; i < insertSqlSegments.size(); i++) {
            sb.append(insertSqlSegments.get(i));
        }
        return sb.toString();
    }

    public String getUpdateSql(Object obj, String tableName, String condition) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("update ");
        boolean needBackQuote = !tableName.startsWith("`");
        if (needBackQuote) {
            sb.append("`");
        }
        sb.append(tableName);
        if (needBackQuote) {
            sb.append("` ");
        }
        int i = 0;
        for (; i < fieldNameList.size(); i++) {
            String fieldName = fieldNameList.get(i);
            Object value = null;
            sb.append(updateSqlSegments.get(i));
            try {
                value = getterMethodMap.get(fieldName).invoke(obj);
            } catch (Exception e) {
                // fixme:
                e.printStackTrace();
            }
            if (value == null) {
                sb.append("null");
            } else {
                sb.append("'").append(value.toString()).append("'");
            }
        }
        for (; i < updateSqlSegments.size(); i++) {
            sb.append(updateSqlSegments.get(i));
        }
        if (condition != null && !"".equals(condition.trim())) {
            sb.append(" where ").append(condition);
        }
        return sb.toString();
    }

}
