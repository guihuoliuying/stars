package com.stars.server.login2.asyncdb;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class AsyncDbTask implements Runnable {

    private int dbId;
    private String sql;
    private long callbackId;

    public AsyncDbTask(int dbId, String sql, long callbackId) {
        this.dbId = dbId;
        this.sql = sql;
        this.callbackId = callbackId;
    }

    @Override
    public void run() {
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//        try {
//            conn = DbUtil.getConn(dbId);
//            stmt = conn.createStatement();
//            AsyncDbResult result = new AsyncDbResult();
//            Map<String, Object> map = new HashMap<>();
//            if (stmt.execute(sql)) { // has result set
//                rs = stmt.getResultSet();
//                if (rs.next()) {
//                    ResultSetMetaData rsmd = rs.getMetaData();
//                    int colCount = rsmd.getColumnCount();
//                    for (int i = 1; i <= colCount; i++) {
//                        String key = rsmd.getColumnName(i);
//                        Object val = null;
//                        switch (rsmd.getColumnType(i)) {
//                            case Types.VARCHAR:
//                                val = rs.getString(i);
//                                break;
//                            case Types.INTEGER:
//                                val = rs.getInt(i);
//                                break;
//                            case Types.TIMESTAMP:
//                                val = rs.getTimestamp(i);
//                                break;
//                        }
//                        map.put(key, val);
//                    }
//                }
//                result.setResultSet(map);
//            } else {
//                result.setUpdatedCount(stmt.getUpdateCount());
//            }
//            result.setSuccess(true);
//            AsyncDbManager.finish(callbackId, result);
//
//        } catch (Exception e) {
//            AsyncDbResult result = new AsyncDbResult();
//            result.setSuccess(false);
//            result.setCause(e);
//            AsyncDbManager.finish(callbackId, result);
//
//        } finally {
//            if (rs != null) try { rs.close(); } catch (Exception ex) {}
//            if (stmt != null) try { stmt.close(); } catch (Exception ex) {}
//            if (conn != null) try { conn.close(); } catch (Exception ex) {}
//
//        }
    }

}
