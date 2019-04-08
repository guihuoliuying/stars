package com.stars.core.gmpacket;

import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by zhouyaohui on 2016/12/22.
 */
public class WhiteListOpenOrCloseGm extends GmPacketHandler {

    private volatile static boolean isOpen = false;
    private static Set<String> accountSet = new HashSet<>();
    private static Set<Long> WhiteRoleaccountSet = new HashSet<>();
    private static Object accountLock = new Object();

    public static void loadWhiteList() throws SQLException {
        List<String> accounts = DBUtil.queryList(DBUtil.DB_USER, String.class, "select * from whitelist");
        synchronized (accountLock) {
            accountSet.addAll(accounts);
        }
        /**
         * 白名单角色初始化
         */
        WhiteRoleaccountSet.addAll(handleRoleInfoByAccounts());
        LogUtil.info("WhiteList|{}", accounts);

    }

    public static boolean isWhiteList(String account) {
        ServerLogConst.console.info("whiteList open=" + isOpen + "|account=" + account + "|" + accountSet.size());
        if (isOpen) {
            synchronized (accountLock) {
                if (accountSet.contains(account)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static void addWhiteList(String account) throws SQLException {
        synchronized (accountLock) {
            if (accountSet.contains(account)) {
                return;
            }
            DBUtil.execSql(DBUtil.DB_USER, "insert into whitelist values ('" + account + "')");
            accountSet.add(account);
        }
    }

    public static void delWhiteList(String account) throws SQLException {
        synchronized (accountLock) {
            DBUtil.execSql(DBUtil.DB_USER, "delete from whitelist where account = '" + account + "'");
            accountSet.remove(account);
        }
    }

    @Override
    public String handle(HashMap args) {
        int opt = Integer.valueOf((String) args.get("value"));
        if (opt == 0) {
            // 开启白名单
            isOpen = true;
        } else {
            // 关闭白名单
            isOpen = false;
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(0));
        return response.toString();
    }

    public static boolean isOpen() {
        return isOpen;
    }

    public static Set<String> getAccountSet() {
        return accountSet;
    }

    public static Set<Long> getWhiteRoleaccountSet() {
        WhiteRoleaccountSet.clear();
        WhiteRoleaccountSet.addAll(handleRoleInfoByAccounts());
        return WhiteRoleaccountSet;
    }

    /**
     * 加载账号的所有角色信息
     *
     * @return
     */
    public static Set<Long> handleRoleInfoByAccounts() {
        Set<Long> roleIds = new HashSet<>();
        StringBuilder condition = new StringBuilder();
        for (String account : accountSet) {
            condition.append("'" + account + "'" + ",");
        }
        String conditionStr = null;
        if (condition.toString().endsWith(",")) {
            conditionStr = condition.substring(0, condition.length() - 1);
        }
        String sql = "select * from accountrole where SUBSTRING_INDEX(account,'#',1) in (" + conditionStr + ")";
        try {
            Map<String, AccountRole> roleAccountMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", AccountRole.class, sql);
            for (String roleIdStr : roleAccountMap.keySet()) {
                long roleId = Long.parseLong(roleIdStr);
                roleIds.add(roleId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roleIds;
    }
}
