package com.stars.core.dao;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.util.LogUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * 非线程安全
 * Created by zhaowenshuo on 2016/8/2.
 */
public class DbRowDao {

    // 标识
    private String name;
    private String alias;
    // 用户数据相关
    private Map<DbRow, DbRow> deleteDbRowSet = new LinkedHashMap<>();
    private Map<DbRow, DbRow> changeDbRowSet = new LinkedHashMap<>();
    private boolean isSavingSucceeded = false; // 初始化为false，强制必须保存一次后才能入库

    public DbRowDao() {
        this.name = "";
        this.alias = DBUtil.DB_USER;
    }

    public DbRowDao(String name) {
        this.name = name;
        this.alias = DBUtil.DB_USER;
    }

    public DbRowDao(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public boolean isSavingSucceeded() {
        return isSavingSucceeded;
    }

    public boolean isSavingSucceeded(Set<DbRow> set) {
        LogUtil.debug("deleteDbRowSet.size()=" + deleteDbRowSet.size() + ", changeDbRowSet.size()=" + changeDbRowSet.size()
                + ", set.size()=" + set.size()); // 可能影响效率
        for (DbRow row : set) {
            if (deleteDbRowSet.containsKey(row) || changeDbRowSet.containsKey(row)) {
                return false;
            }
        }
        return true;
    }

    public void insert(DbRow row) {
        Objects.requireNonNull(row);
        row.setInsertStatus();
        changeDbRowSet.put(row, row);
    }

    public void insert(DbRow... rows) {
        Objects.requireNonNull(rows);
        for (DbRow r : rows) {
            insert(r);
        }
    }

    public void update(DbRow row) {
        Objects.requireNonNull(row);
        row.setUpdateStatus();
        if (changeDbRowSet.containsKey(row)) {
            row.setDbStatus(changeDbRowSet.get(row).getDbStatus());
        }
        changeDbRowSet.put(row, row);
    }

    public void update(DbRow... rows) {
        Objects.requireNonNull(rows);
        for (DbRow r : rows) {
            update(r);
        }
    }

    public void delete(DbRow row) {
        Objects.requireNonNull(row);
        deleteDbRowSet.put(row, row);
        changeDbRowSet.remove(row);
    }

    public void delete(DbRow... rows) {
        Objects.requireNonNull(rows);
        for (DbRow r : rows) {
            delete(r);
        }
    }

    public void flush() {
        try {
            flush(false, false);
        } catch (Throwable cause) {
            LogUtil.error("DbRowDao.flush()", cause);
        }
    }

    public void flush(boolean removeOnFailure) {
        try {
            flush(false, removeOnFailure);
        } catch (Throwable cause) {
            LogUtil.error("DbRowDao.flush(boolean)", cause);
        }
    }

    public void flush(boolean throwExceptionOnFailure, boolean removeOnFailure) {
        if (deleteDbRowSet.size() == 0 && changeDbRowSet.size() == 0) {
            return;
        }
        long s = System.currentTimeMillis();
        // 1. 生成dbRow的列表
        int deleteRowSize = deleteDbRowSet.size();
        List<DbRow> rowList = new ArrayList<>(deleteDbRowSet.size() + changeDbRowSet.size());
        rowList.addAll(deleteDbRowSet.values());
        rowList.addAll(changeDbRowSet.values());
        // 2. 执行
        List<Boolean> resultList = null;
        try {
            resultList = execBatch(rowList, deleteRowSize, throwExceptionOnFailure);
        } catch (SQLException e) { // 在这一层的异常是数据库的异常（即所有SQL都会失败）
            isSavingSucceeded = false;
            if (throwExceptionOnFailure) {
                throw new RuntimeException(e);
            }
            LogUtil.error("flush()", e); // 不抛出异常的情况才打印异常，避免异常过多
            return;
        }
        // 3. 修改状态
        isSavingSucceeded = true;
        if (resultList != null) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i)) {
                    remove(rowList, deleteRowSize, i);
                } else {
                    isSavingSucceeded = false;
                    if (removeOnFailure) {
                        remove(rowList, deleteRowSize, i);
                    }
                }
            }
        }
        long e = System.currentTimeMillis();
//        LogUtil.info("自动保存[{}]: 是否成功={}, 待删除对象={}, 待插入/更新对象={}, 删除对象={}, 插入/更新对象={}, changeDbRowSet={}, deleteDbRowSet={}, 耗时={}s",
//                name, isSavingSucceeded, deleteRowSize, rowList.size() - deleteRowSize, deleteRowSize - deleteDbRowSet.size(), rowList.size() - deleteRowSize - changeDbRowSet.size(),
//                changeDbRowSet.size(), deleteDbRowSet.size(), (e - s) / 1000.0);
    }

    public List<String> getSqlList() {
        List<String> sqlList = new LinkedList<>();
        // delete
        for (DbRow row : deleteDbRowSet.values()) {
            String sql = null;
            try {
                sql = row.getDeleteSql();
            } catch (Throwable cause) {
                LogUtil.error("getSqlList|生成DeleteSql异常", cause);
            }
            if (sql != null && !sql.trim().equals("")) {
                sqlList.add(sql);
            }
        }
        // insert / update
        for (DbRow row : changeDbRowSet.values()) {
            String sql = null;
            try {
                sql = row.getChangeSql();
            } catch (Throwable cause) {
                LogUtil.error("getSqlList|生成ChangeSql异常", cause);
            }
            if (sql != null && !sql.trim().equals("")) {
                sqlList.add(sql);
            }
        }
        return sqlList;
    }

    private void remove(List<DbRow> rowList, int deleteRowSize, int index) {
        if (index < deleteRowSize) {
            deleteDbRowSet.remove(rowList.get(index));
        } else {
            DbRow row = rowList.get(index);
            row.setSaveStatus();
            changeDbRowSet.remove(row);
        }
    }

    private List<Boolean> execBatch(List<DbRow> rowList, int deleteRowSize, boolean throwExceptionOnFailure) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        List<Boolean> resultList = new ArrayList<>(rowList.size() + deleteRowSize);
        int oldIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
        try {
            conn = DBUtil.getConnection(alias);
            /* 因为可重复读的隔离级别下（RR），能因为Gap Lock导致死锁的问题。
             * 所以将隔离级别调到提交读（RC）。
             */
            oldIsolationLevel = conn.getTransactionIsolation();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            for (int i = 0; i < rowList.size(); i++) {
                if (i < deleteRowSize) {
                    stmt.addBatch(rowList.get(i).getDeleteSql());
                } else {
                    stmt.addBatch(rowList.get(i).getChangeSql());
                }
            }
            stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            fillList(resultList, Boolean.TRUE, rowList.size());
            return resultList;
        } catch (Exception e) {
            // 回滚
            try {
                if (conn != null) {
                    conn.rollback();
//                    conn.setAutoCommit(false);
                }
            } catch (SQLException ex) {
                LogUtil.error("execBatch():回滚异常", ex);
            }
            // 1. 判断错误类型
            if (DBUtil.isDbError(e) || throwExceptionOnFailure) {
                throw new SQLException(e);
            } else {
                DBUtil.closeStatement(stmt);
                return execOneByOne(conn, rowList, deleteRowSize);
            }
        } finally {
            DBUtil.closeStatement(stmt);
            try {
                conn.setTransactionIsolation(oldIsolationLevel);
            } catch (Exception e) {
            }
            try {
                conn.setAutoCommit(true);
            } catch (Exception e) {
            }
            DBUtil.closeConnection(conn);
        }
    }

    private List<Boolean> execOneByOne(Connection conn, List<DbRow> rowList, int deleteRowSize) {
        Statement stmt = null;
        List<Boolean> resultList = new LinkedList<>();
        try {
            conn.setAutoCommit(true);
            stmt = conn.createStatement();
            for (int i = 0; i < rowList.size(); i++) {
                String sql = null;
                try {
                    if (i < deleteRowSize) {
                        sql = rowList.get(i).getDeleteSql();
                    } else {
                        sql = rowList.get(i).getChangeSql();
                    }
                    if (sql != null && sql.trim().length() > 1) {
                        stmt.executeUpdate(sql);
                        resultList.add(Boolean.TRUE);
                    } else {
                        if (rowList.get(i).printErrorLog) {
                            LogUtil.error("execOneByOne(): dbRow生成" + ((i < deleteRowSize) ? "删除" : "插入/更新") + "SQL为Null, " +
                                    "Class=" + rowList.get(i).getClass().getName() + ", Object=" + rowList.get(i).toString());
                            rowList.get(i).printErrorLog = Boolean.FALSE;
                        }
                        resultList.add(Boolean.FALSE);
                    }
                } catch (Throwable t) {
                    if (rowList.get(i).printErrorLog) {
                        LogUtil.error("execOneByOne(), sql=" + sql, t);
                        rowList.get(i).printErrorLog = Boolean.FALSE;
                    }
                    resultList.add(Boolean.FALSE);
                }
            }
            return resultList;
        } catch (SQLException e) {
            LogUtil.error("execOneByOne()", e);
            fillList(resultList, Boolean.FALSE, rowList.size());
            return resultList;
        } finally {
            DBUtil.closeStatement(stmt);
        }
    }

    private void fillList(List<Boolean> resultList, Boolean flag, int size) {
        for (int i = 0; i < size; i++) {
            resultList.add(flag);
        }
    }

}
