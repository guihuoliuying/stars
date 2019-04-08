package com.stars.db;

import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import org.logicalcobwebs.proxool.ConnectionInfoIF;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by zhaowenshuo on 2016/7/19.
 */
public class DbDetectionUtil {

    public static String SEMICOLON_REPLACE_STR = "@^";//分号替换字符
    public static String SQL_SPLIT = ";";

    /**
     * 获得所有的连接的情况
     *
     * @return
     */
    public static String getConnectionInfos() {
        StringBuffer bufStr = new StringBuffer();
        String[] aliases = ProxoolFacade.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                bufStr.append(getConnectionInfos(alias));
            }
        }
        return bufStr.toString();
    }

    /**
     * 获取当前数据库连接情况
     *
     * @param alias
     * @return
     */
    public static String getConnectionInfos(String alias) {
        StringBuffer bufStr = new StringBuffer();
        ConnectionInfoIF[] connectionInfos = null;
        try {
            connectionInfos = ProxoolFacade.getSnapshot(alias, true).getConnectionInfos();
            bufStr.append("*************数据库").append(alias).append("的连接情况******************\n");
            bufStr.append("连接为空的数量：").append(getStatusCount(connectionInfos, ConnectionInfoIF.STATUS_NULL)).append("\n");
            bufStr.append("活动的连接数量：").append(getStatusCount(connectionInfos, ConnectionInfoIF.STATUS_ACTIVE)).append("\n");
            bufStr.append("可利用连接数量：").append(getStatusCount(connectionInfos, ConnectionInfoIF.STATUS_AVAILABLE)).append("\n");
            bufStr.append("断开的连接数量：").append(getStatusCount(connectionInfos, ConnectionInfoIF.STATUS_OFFLINE)).append("\n");
            for (int i = 0; i < connectionInfos.length; i++) {
                ConnectionInfoIF connectionInfo = connectionInfos[i];
                bufStr.append("*********id:").append(connectionInfo.getId()).append(";").
                        append("*****status:").append(convertStatus(connectionInfo.getStatus())).append(";").
                        append("********age:").append(connectionInfo.getAge()).append(";").
                        append("**birthDate:").append(connectionInfo.getBirthDate()).append(";").
                        append("**BirthTime:").append(connectionInfo.getBirthTime()).append(";").
                        append("DelegateUrl:").append(connectionInfo.getDelegateUrl()).append(";").
                        append("*******Mark:").append(connectionInfo.getMark()).append(";").
                        append("TimeLastStartActive:").append(connectionInfo.getTimeLastStartActive()).append(";").
                        append("TimeLastStopActive:").append(connectionInfo.getTimeLastStopActive()).append(";").append("\n");
            }
        } catch (ProxoolException e) {
            e.printStackTrace();
        }
        return bufStr.toString();
    }

    /**
     * 获取连接的信息
     *
     * @param alias
     * @param connectionId
     * @return
     */
    public static ConnectionInfoIF getConnectionInfoIF(String alias, long connectionId) {
        try {
            SnapshotIF snapshotIF = ProxoolFacade.getSnapshot(alias, true);
            if (snapshotIF == null) return null;
            return snapshotIF.getConnectionInfo(connectionId);
        } catch (ProxoolException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获得当前连接池中相应状态连接的数量
     *
     * @param connectionInfos
     * @param status
     * @return
     */
    private static int getStatusCount(ConnectionInfoIF[] connectionInfos, int status) {
        int count = 0;
        for (int i = 0; i < connectionInfos.length; i++) {
            ConnectionInfoIF connectionInfo = connectionInfos[i];
            if (connectionInfo.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    /**
     * 转换状态
     *
     * @param status
     * @return
     */
    private static String convertStatus(int status) {
        String returnStr = null;
        switch (status) {
            case 0:
                returnStr = "NULL";
                break;
            case 1:
                returnStr = "AVAILABLE";
                break;
            case 2:
                returnStr = "ACTIVE";
                break;
            case 3:
                returnStr = "OFFLINE";
                break;
        }
        return returnStr;
    }

    /**
     * 获取字符串的值
     *
     * @param filedValue
     * @return
     */
    private static Object getQueryStringValue(Object filedValue) {
        if (filedValue == null) return filedValue;
        String value = filedValue.toString();
        if (value.contains(SEMICOLON_REPLACE_STR)) {
            value = value.replace(SEMICOLON_REPLACE_STR, SQL_SPLIT);
            filedValue = value;
        }
        return filedValue;
    }

    private static String getQueryStringValue(String filedValue) {
        if (filedValue == null) return filedValue;
        if (filedValue.contains(SEMICOLON_REPLACE_STR)) {
            filedValue = filedValue.replace(SEMICOLON_REPLACE_STR, SQL_SPLIT);
        }
        return filedValue;
    }


    /**
     * 检查用户库表字段是否有大写
     */
    public static void checkTableIsUpcase() throws Exception {
        ArrayList<String> list = (ArrayList<String>) DBUtil.queryList(DBUtil.DB_USER, String.class, "show tables");
        StringBuffer buf = new StringBuffer();
        for (String tableName : list) {
            if (StringUtil.containsUpperCase(tableName)) {
                buf.append(tableName).append("&");
            }
        }
        if (buf.length() > 0) {
            com.stars.util.LogUtil.info("表字段中有大写|" + buf.toString() + "", new Exception());
            System.exit(-1);
        }
    }


    /**
     * 获取导出的命令
     *
     * @return
     */
    public static String getExportCommand(String tableNames, String sqlName) {
        StringBuffer cmdBuf = null;
        try {
            ConnectionPoolDefinitionIF poolDefinitionIF = ProxoolFacade.getConnectionPoolDefinition("user");
            String temps[] = poolDefinitionIF.getUrl().split("/");
            String[] ipPort = temps[2].trim().split(":");
            String host = ipPort[0];
            String port = ipPort[1];
            String dbName = temps[3].trim();
            cmdBuf = new StringBuffer().append("mysqldump -u").append(poolDefinitionIF.getUser())
                    .append(" -p").append(poolDefinitionIF.getPassword())
                    .append(" -h").append(host)
                    .append(" -P").append(port)
                    .append(" -t ").append(dbName)
                    .append(" ").append(tableNames)
                    .append(" >").append("dbbackupuser/").append(sqlName);
        } catch (ProxoolException e) {
            com.stars.util.LogUtil.error("getImportCommand", e.getMessage(), e);
        }
        return cmdBuf.toString();
    }


    /**
     * 获取导入的命令
     * 当天sql
     *
     * @return
     */
    public static String getImportCommand(String sqlName) {
        StringBuffer cmdBuf = null;
        try {
            ConnectionPoolDefinitionIF poolDefinitionIF = ProxoolFacade.getConnectionPoolDefinition("user");
            String temps[] = poolDefinitionIF.getUrl().split("/");
            String[] ipPort = temps[2].trim().split(":");
            String host = ipPort[0];
            String port = ipPort[1];
            String dbName = temps[3].trim();
            cmdBuf = new StringBuffer().append("mysql -u").append(poolDefinitionIF.getUser())
                    .append(" -p").append(poolDefinitionIF.getPassword())
                    .append(" -h").append(host)
                    .append(" -P").append(port)
                    .append(" -f ").append(dbName)
                    .append(" <").append("dbbackupuser/").append(sqlName);
        } catch (ProxoolException e) {
            com.stars.util.LogUtil.error("getImportCommand", e.getMessage(), e);
        }
        return cmdBuf.toString();
    }


    /**
     * 执行cmd
     *
     * @param cmd
     */
    public static void execCmd(String cmd) {
        File file = new File("dbbackupuser");
        if (!file.exists()) {
            file.mkdir();
        }
        String[] cmds = {"/bin/sh", "-c", cmd};
        Runtime rt = Runtime.getRuntime();
        BufferedReader br = null;
        StringBuffer buf = new StringBuffer(Arrays.toString(cmds));
        try {
            Process p = rt.exec(cmds);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                buf.append(line).append("\r\n");
            }
        } catch (IOException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    com.stars.util.LogUtil.error(e.getMessage(), e);
                }
            }
            if (buf.length() > 0) {
                LogUtil.info(buf.toString());
            }
        }
    }

}
