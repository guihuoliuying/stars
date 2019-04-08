package com.stars.core.db;

import com.stars.util.LogUtil;
import com.stars.util._HashMap;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-11-18 Time:
 * 下午8:02
 * 数据库操作类
 */
public class DBUtil {
    public static String SEMICOLON_REPLACE_STR = "@^";//分号替换字符
    public static String SQL_SPLIT = ";";

    private static final String PROXOOL = "proxool.";
    public static final String DB_CONFIG = PROXOOL + "config"; // 配置信息数据库别名
    public static final String DB_PRODUCT = PROXOOL + "product"; //产品数据库
    public static final String DB_USER = PROXOOL + "user"; //人物数据库
    public static final String DB_LOGIN = PROXOOL + "login";
    public static final String DB_RM = PROXOOL + "rm";//跨服数据
    public static final String DB_COMMON = PROXOOL + "common";//公共库

    private static ConcurrentMap<Class, ObjectMetaData> objectMetaDataMap = new ConcurrentHashMap<>();
    private static Set<String> dbErrorSqlStateSet = new HashSet<>();
    private static Set<Integer> dbErrorCodeSet = new HashSet<>();

    static {
//        dbErrorSqlStateSet.add("HY000");

//        dbErrorSqlStateSet.add("HY001");
//        dbErrorSqlStateSet.add("S1001");
//        dbErrorSqlStateSet.add("08004");
//        dbErrorSqlStateSet.add("08S01");
//        dbErrorSqlStateSet.add("28000");

        dbErrorCodeSet.add(1037); // ER_OUTOFMEMORY
        dbErrorCodeSet.add(1038); // ER_OUT_OF_SORTMEMORY
        dbErrorCodeSet.add(1040); // ER_CON_COUNT_ERROR
        dbErrorCodeSet.add(1042); // ER_BAD_HOST_ERROR
        dbErrorCodeSet.add(1043); // ER_HANDSHAKE_ERROR
        dbErrorCodeSet.add(1044); // ER_DBACCESS_DENIED_ERROR
        dbErrorCodeSet.add(1045); // ER_ACCESS_DENIED_ERROR
        dbErrorCodeSet.add(1046); // ER_NO_DB_ERROR
        dbErrorCodeSet.add(1047); // ER_UNKNOWN_COM_ERROR
        dbErrorCodeSet.add(1049); // ER_BAD_DB_ERROR
        dbErrorCodeSet.add(1080); // ER_FORCING_CLOSE
        dbErrorCodeSet.add(1081); // ER_IPSOCK_ERROR

        dbErrorCodeSet.add(1129); // ER_HOST_IS_BLOCKED
        dbErrorCodeSet.add(1130); // ER_HOST_NOT_PRIVILEGED
        dbErrorCodeSet.add(1131); // ER_PASSWORD_ANONYMOUS_USER
        dbErrorCodeSet.add(1132); // ER_PASSWORD_NOT_ALLOWED
        dbErrorCodeSet.add(1133); // ER_PASSWORD_NOT_MATCH

        dbErrorCodeSet.add(1152); // ER_ABORTING_CONNECTION
        dbErrorCodeSet.add(1153); // ER_NET_PACKET_TOO_LARGE
        dbErrorCodeSet.add(1154); // ER_NET_READ_ERROR_FROM_PIPE
        dbErrorCodeSet.add(1155); // ER_NET_FCNT_ERROR
        dbErrorCodeSet.add(1156); // ER_NET_PACKET_OUT_OF_ORDER
        dbErrorCodeSet.add(1157); // ER_NET_UNCOMPRESS_ERROR
        dbErrorCodeSet.add(1158); // ER_NET_READ_ERRO
        dbErrorCodeSet.add(1159); // ER_NET_READ_INTERRUPTED
        dbErrorCodeSet.add(1160); // ER_NET_ERROR_ON_WRITE
        dbErrorCodeSet.add(1161); // ER_NET_WRITE_INTERRUPTED

        dbErrorCodeSet.add(1184); // ER_NEW_ABORTING_CONNECTION
        dbErrorCodeSet.add(1203); // ER_TOO_MANY_USER_CONNECTIONS
        dbErrorCodeSet.add(1698); // ER_ACCESS_DENIED_NO_PASSWORD_ERROR
        dbErrorCodeSet.add(1873); // ER_ACCESS_CHANGE_USER_ERROR

    }

    public static void init() {
        try {
            com.stars.util.LogUtil.info("初始化数据库连接池……");
            // 读取配置文件并创建数据库连接池
            JAXPConfigurator.configure("config/db/proxool.xml", true);
        } catch (ProxoolException e) {
            com.stars.util.LogUtil.error("读取数据库配置文件出错", e.getMessage(), e);
            System.err.println("读取数据库配置文件出错");
            System.exit(-1);
        }
    }


    /**
     * 获得连接
     *
     * @param alias
     * @return
     */
    public static Connection getConnection(String alias) throws SQLException {
        return DriverManager.getConnection(alias);
    }

    /**
     * 关闭连接
     *
     * @param conn
     */
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            com.stars.util.LogUtil.error("closeConnection", e.getMessage(), e);
        }
    }

    /**
     * 关闭statement
     *
     * @param stmt
     */
    public static void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            com.stars.util.LogUtil.error("closeStatement", e.getMessage(), e);
        }
    }

    /**
     * 关闭resultSet
     *
     * @param rst
     */
    public static void closeResultSet(ResultSet rst) {
        try {
            if (rst != null) {
                rst.close();
            }
        } catch (SQLException e) {
            com.stars.util.LogUtil.error("closeResultSet", e.getMessage(), e);
        }
    }

    /**
     * 查询数据，并反序列化成对象，返回对象列表; 没有超时时间
     * @param alias 数据库别名
     * @param clazz 类
     * @param sql 查询SQL
     * @return 对象列表（ArrayList）
     * @throws SQLException
     */
    public static <T> List<T> queryList(String alias, Class<T> clazz, String sql) throws SQLException {
        return queryList(alias, clazz, 0, sql);
    }

    /**
     * 查询数据，并反序列化成对象，返回对象列表
     * @param alias 数据库别名
     * @param clazz 类
     * @param timeout 超时时间
     * @param sql 查询SQL
     * @return 对象列表（ArrayList）
     * @throws SQLException
     */
    public static <T> List<T> queryList(String alias, Class<T> clazz, int timeout, String sql) throws SQLException {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(sql);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection(alias);
            ObjectMetaData objectMetaData = getObjectMetaDataBySql(conn, clazz, sql);
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeout);
            rs = stmt.executeQuery(sql);
            return ResultSetHandler.toList(rs, clazz, objectMetaData);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            com.stars.util.LogUtil.error("DBUtil.queryMap()", e.getMessage(), e); // fixme: log it
            return new ArrayList<T>();
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * 查询数据，并反序列化成对象，返回对象哈希表; 没有超时时间
     * @param alias 数据库别名
     * @param keyField 作为键的字段名
     * @param clazz 类
     * @param sql SQL语句
     * @return 对象哈希表（ConcurrentHashMap）
     * @throws SQLException
     */
    public static ConcurrentMap queryConcurrentMap(String alias, String keyField, Class clazz, String sql) throws SQLException {
        return (ConcurrentMap) queryConcurrentMap(alias, keyField, clazz, 0, sql);
    }

    /**
     * 查询数据，并反序列化成对象，返回对象哈希表
     * @param alias 数据库别名
     * @param keyField 作为键的字段名
     * @param clazz 类
     * @param timeout 超时时间
     * @param sql SQL语句
     * @return 对象哈希表（ConcurrentHashMap）
     * @throws SQLException
     */
    public static ConcurrentMap queryConcurrentMap(String alias, String keyField, Class clazz, int timeout, String sql) throws SQLException {
        return (ConcurrentMap) queryMap(alias, keyField, clazz, timeout, sql, ConcurrentHashMap.class);
    }

    /**
     * 查询数据，并反序列化成对象，返回对象哈希表; 没有超时时间
     * @param alias 数据库别名
     * @param keyField 作为键的字段名
     * @param clazz 类
     * @param sql SQL语句
     * @return 对象哈希表（HashMap）
     * @throws SQLException
     */
    public static Map queryMap(String alias, String keyField, Class clazz, String sql) throws SQLException {
        return queryMap(alias, keyField, clazz, 0, sql);
    }

    /**
     * 查询数据，并反序列化成对象，返回对象哈希表
     * @param alias 数据库别名
     * @param keyField 作为键的字段名
     * @param clazz 类
     * @param timeout 超时时间
     * @param sql SQL语句
     * @return 对象哈希表（HashMap）
     * @throws SQLException
     */
    public static Map queryMap(String alias, String keyField, Class clazz, int timeout, String sql) throws SQLException {
        return queryMap(alias, keyField, clazz, timeout, sql, HashMap.class);
    }

    private static Map queryMap(String alias, String keyField, Class clazz, int timeout, String sql, Class<? extends Map> mapClass) throws SQLException {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(keyField);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(sql);
        Objects.requireNonNull(mapClass);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection(alias);
            ObjectMetaData objectMetaData = getObjectMetaDataBySql(conn, clazz, sql);
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeout);
            rs = stmt.executeQuery(sql);
            return ResultSetHandler.toMap(rs, clazz, objectMetaData, keyField, mapClass);
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
            com.stars.util.LogUtil.error("DBUtil.queryMap()", e.getMessage(), e); // fixme: log it
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return new HashMap();
    }

    public static <T> T queryBean(String alias, Class<T> clazz, String sql) throws SQLException {
        List<T> list = queryList(alias, clazz, sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public static com.stars.util._HashMap querySingleMap(String alias, String sql) throws SQLException {
        List<com.stars.util._HashMap> list = queryList(alias, com.stars.util._HashMap.class, sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public static long queryCount(String alias, String sql) throws SQLException {
        List<Long> list = queryList(alias, Long.class, sql);
        return list.size() > 0 ? list.get(0) : 0;
    }

    public static boolean execUserSqlNoEx(String sql) {
        return execSqlNoEx(DB_USER, sql);
    }

    public static void execUserSql(String sql) throws SQLException {
        execSql(DB_USER, sql);
        return;
    }

    public static boolean execSqlNoEx(String alias, String sql) {
        try {
            execSql(alias, sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void execSql(String alias, String sql) throws SQLException {
        execBatch(alias, true, sql);
        return;
    }

    public static DbExecResult execBatch(String alias, boolean rollbackOnFailure, String... sqls) throws SQLException {
        Objects.requireNonNull(sqls);
        return execBatch(alias, rollbackOnFailure, Arrays.asList(sqls));
    }

    /**
     *
     * @param alias
     * @param sqls
     * @return
     * @throws SQLException -- 1. 因数据库引起的异常应中断执行（如数据库不可用等）；2. 因业务代码引起的异常应忽略
     */
    public static DbExecResult execBatch(String alias, boolean rollbackOnFailure, List<String> sqls) throws SQLException {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(sqls);
        Connection conn = null;
        Statement stmt = null;
        StringBuilder sb = new StringBuilder();
        List<String> failedSqlList = new LinkedList<>();
        try {
            conn = getConnection(alias);
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            for (String sql : sqls) {
                if (sql.trim().length() > 1) {
                    stmt.addBatch(sql);
                    sb.append(sql).append(SQL_SPLIT);
                }
            }
            stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            return new DbExecResult(true);
        } catch (SQLException e) {
            com.stars.util.LogUtil.error("SQL异常：" + sb.toString(), e.getMessage(), e);
            try {
                conn.rollback();
            } catch (SQLException ex) {
                com.stars.util.LogUtil.error("DBUtil.execBatch()回滚异常", ex);
            }
            if (rollbackOnFailure || isDbError(e)) {
                throw e;
            } else {
                try {
                    closeStatement(stmt);
                    failedSqlList = execSqlOneByOne(conn, sqls);
                    return new DbExecResult(failedSqlList.isEmpty(), failedSqlList);
                } catch (SQLException ex) {
                    com.stars.util.LogUtil.error("回滚出错", ex.getMessage(), ex);
                    throw ex;
                } finally {
                    closeConnection(conn);
                }
            }
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    private static List<String> execSqlOneByOne(Connection conn, List<String> sqls) throws SQLException {
        Statement stmt = null;
        List<String> failedSqlList = new LinkedList<>();
        StringBuilder succeededSqlBuilder = new StringBuilder();
        try {
            stmt = conn.createStatement();
            conn.setAutoCommit(true);
            for (String sql : sqls) {
                if (sql != null && sql.trim().length() > 1) {
                    try {
                        stmt.executeUpdate(sql);
                        if (com.stars.util.LogUtil.needDebugLog) {
                            succeededSqlBuilder.append(sql).append(";");
                        }
                    } catch (SQLException e) {
                        failedSqlList.add(sql);
                        com.stars.util.LogUtil.error("DBUtil.execSqlOneByOne()", e.getMessage(), e);
                    } finally {
                        com.stars.util.LogUtil.debug(succeededSqlBuilder.toString());
                    }
                }
            }
            com.stars.util.LogUtil.error("DBUtil.execSqlOneByOne(), 错误sql语句:" + failedSqlList);
        } finally {
            closeStatement(stmt);
        }
        return failedSqlList;
    }

    static ObjectMetaData getObjectMetaData(Class clazz) {
        return objectMetaDataMap.get(clazz);
    }

    static ObjectMetaData getObjectMetaData(Connection conn, Class clazz, String tableName) throws SQLException {
        if (!objectMetaDataMap.containsKey(clazz)) {
            queryObjectMetaData(conn, clazz, tableName);
        }
        return objectMetaDataMap.get(clazz);
    }

    static ObjectMetaData getObjectMetaDataBySql(Connection conn, Class clazz, String sql) throws SQLException {
        if (clazz.isPrimitive()
                || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class
                || clazz == Float.class || clazz == Double.class
                || clazz == String.class || clazz == _HashMap.class) {
            return null;
        }
        if (!objectMetaDataMap.containsKey(clazz)) {
            queryObjectMetaData(conn, clazz, getTableName(sql));
        }
        return objectMetaDataMap.get(clazz);
    }

    private static void queryObjectMetaData(Connection conn, Class clazz, String tableName) throws SQLException {
        ObjectMetaData objectMetaData = new ObjectMetaData(clazz);
        Map<String, String> fieldTypeMap = queryFieldTypeMap(conn, tableName);
        Method[] methods = clazz.getMethods();
        for (String fieldName : fieldTypeMap.keySet()) {
            Method setterMethod = null;
            Method getterMethod = null;
            String setterName = "set" + fieldName;
            String getterName = "get" + fieldName;
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(setterName)
                        && method.getParameterTypes().length == 1) { // 预防方法重载的情况（不是很好搞）
                    setterMethod = method;
                } else if (method.getName().equalsIgnoreCase(getterName)
                        && method.getParameterTypes().length == 0) { // 预防方法重载的情况
                    getterMethod = method;
                }
            }
            if (setterMethod == null || getterMethod == null) {
                com.stars.util.LogUtil.error("DBUtil获取类型信息，表" + tableName + "字段" + fieldName + "缺少对应的Getter/Setter方法");
            } else {
                if (getterMethod.getReturnType() != setterMethod.getParameterTypes()[0]) {
                    com.stars.util.LogUtil.error("DBUtil获取类型信息，表" + tableName + "字段" + fieldName + "的Getter/Setter方法的参数类型不对应，" +
                            "Getter的返回类型为" + getterMethod.getReturnType().getSimpleName() + "，Setter的参数类型为" + setterMethod.getParameterTypes()[0].getSimpleName());
                }
                objectMetaData.addField(fieldName);
                objectMetaData.setSetterMethod(fieldName, setterMethod);
                objectMetaData.setGetterMethod(fieldName, getterMethod);
            }
        }
        objectMetaData.finish();
        objectMetaDataMap.putIfAbsent(clazz, objectMetaData);
    }

    private static String getTableName(String sql) {
        String temp = sql.toLowerCase().trim();
        if (temp.startsWith("select ")) {
            String[] array = sql.split(" ");
            for (int i = 0; i < array.length; i++) {
                if ("from".equalsIgnoreCase(array[i])) {
                    if (array[i+1].endsWith(";")) {
                        return array[i+1].substring(0, array[i+1].length()-1);
                    } else {
                        return array[i+1];
                    }
                }
            }
            return ""; // fixme:
        } else if (temp.startsWith("insert into ")) {
            return sql.split(" ")[2];
        } else if (temp.startsWith("update ")) {
            return sql.split(" ")[1];
        }
        return null;
    }

    private static Map<String, String> queryFieldTypeMap(Connection conn, String tableName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        Map<String, String> fieldTypeMap = new HashMap<>();
        try {
            stmt = conn.createStatement();
            tableName = tableName.startsWith("`") ? tableName : "`" + tableName + "`";
            rs = stmt.executeQuery("desc " + tableName.toLowerCase());
            while (rs.next()) {
                fieldTypeMap.put(rs.getString(1), rs.getString(2)); // key=fieldName, val=fieldType
            }
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }
        return fieldTypeMap;
    }

    public static boolean isDbError(Exception e) {
        if (e instanceof SQLException) {
            SQLException ex = (SQLException) e;
//            if (dbErrorSqlStateSet.contains(ex.getSQLState())
//                    || ex.getMessage().toLowerCase().startsWith("couldn't get connection because we are at maximum connection count")) {
//                return true;
//            }
            LogUtil.error("DBUtil.isDbError(), errorCode=" + ex.getErrorCode() + ", sqlState=" + ex.getSQLState());
            if (dbErrorCodeSet.contains(ex.getErrorCode())
                    || ex.getMessage().toLowerCase().startsWith("couldn't get connection because we are at maximum connection count")) {
                return true;
            }
        }
        return false;
    }

}
