package com.stars.db;

import com.stars.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
* Created with IntelliJ IDEA.
* User: yushan
* Date: 12-11-8
* Time: 下午8:43
* 处理对象并获取相应的sql语句
* 对象必须是
* 对象对应的类
*/
public class SqlUtil {
    public static String SQL_SPLIT = ";";
    public static String SEMICOLON_REPLACE_STR="@^";//分号替换字符
    public static String QUOTATION_MARK="'";
    public static String NULLSTR="";

    public static String getSql(DbRow dbRow, String alias, String tableName, String condition) {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(tableName);
        if (dbRow == null) {
            return null;
        }
        tableName = tableName.toLowerCase();
        String sql = "";
        try {
            if (dbRow.isSave()) {
                return sql;
            } else if (dbRow.isInsert()) {
                sql = getInsertSql(alias, dbRow, tableName);
            } else if (dbRow.isUpdate()) {
                sql = getUpdateSql(alias, dbRow, tableName, condition);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (sql.equals("null"))
            sql = "";
        return sql;
    }

    public static String getDeleteSql(String tableName, String condition) {
        Objects.requireNonNull(tableName);
        String whereStmt = StringUtil.isNotEmpty(condition) ? " where " + condition : "";
        return "delete from `" + tableName.toLowerCase() + "`" + whereStmt;
    }

    public static String getInsertSql(String alias, Object obj, String tableName) throws SQLException {
        ObjectMetaData metaData = DBUtil.getObjectMetaData(obj.getClass());
        if (metaData == null) {
            Connection conn = DBUtil.getConnection(alias);
            try {
                metaData = DBUtil.getObjectMetaData(conn, obj.getClass(), tableName);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBUtil.closeConnection(conn);
            }
        }
        return metaData.getInsertSql(obj, tableName);
    }

    public static String getUpdateSql(String alias, Object obj, String tableName, String condition) throws SQLException {
        ObjectMetaData metaData = DBUtil.getObjectMetaData(obj.getClass());
        if (metaData == null) {
            Connection conn = DBUtil.getConnection(alias);
            try {
                metaData = DBUtil.getObjectMetaData(conn, obj.getClass(), tableName);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBUtil.closeConnection(conn);
            }
        }
        return metaData.getUpdateSql(obj, tableName, condition);
    }

}
