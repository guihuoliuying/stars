package com.stars.core.gmpacket.specialaccount;

import com.stars.db.DBUtil;
import com.stars.util._HashMap;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by chenkeyu on 2017-03-24 10:19
 */
public class SpecialAccountManager {

    private static LinkedHashMap<String, Set<Long>> accountRoleMap = new LinkedHashMap<>();//白名单account--对应的roleid

    public static int getAccountSize() {
        return accountRoleMap.size();
    }

    public static String getAccountByIndex(int index) {
        List<String> accountList = new LinkedList<>(accountRoleMap.keySet());
        return accountList.get(index);
    }

    /**
     * 添加账号对应的roleId
     *
     * @param account
     * @param roleId
     */
    public static void addRoleToAccount(String account, long roleId) {
        try {
            Set<Long> roleList = accountRoleMap.get(account);
            if (roleList == null) {
                roleList = new HashSet<>();
                accountRoleMap.put(account, roleList);
                roleList.add(roleId);
                DBUtil.execSql(DBUtil.DB_USER, "update specialaccount set roleid = " + roleId + " where account = '" + account + "'");
            } else {
                if (roleList.contains(roleId)) {
                    return;
                }
                if (roleList.contains(0L)) {
                    roleList.remove(0L);
                    roleList.add(roleId);
                    DBUtil.execSql(DBUtil.DB_USER, "update specialaccount set roleid = " + roleId + " where account = '" + account + "' and roleid = " + 0L);
                    return;
                }
                roleList.add(roleId);
                DBUtil.execSql(DBUtil.DB_USER, "insert into specialaccount values ('" + account + "', " + roleId + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断账号是否在充值白名单内
     *
     * @param account 账号
     * @return
     */
    public static boolean isSpecialAccount(String account) {
        return accountRoleMap.containsKey(account);
    }

    /**
     * 判断账号是否在充值白名单内
     *
     * @param roleId 角色Id
     * @return
     */
    public static boolean isSpecialAccount(long roleId) {
        try {
            for (Set<Long> roleIds : accountRoleMap.values()) {
                if (roleIds != null && roleIds.contains(roleId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getAccountByRoleId(long roleId) {
        for (Map.Entry<String, Set<Long>> entry : accountRoleMap.entrySet()) {
            if (entry.getValue().contains(roleId)) {
                return entry.getKey();
            }
        }
        return "";
    }

    public static void loadSpecialAccount() throws SQLException {
        List<_HashMap> mapList = DBUtil.queryList(DBUtil.DB_USER, _HashMap.class, "select sa.account,sa.roleid from specialaccount sa");
        synchronized (accountRoleMap) {
            for (_HashMap map : mapList) {
                String account = map.getString("sa.account");
                long roleId = map.getLong("sa.roleid");
                Set<Long> roleIds = accountRoleMap.get(account);
                if (roleIds == null) {
                    roleIds = new HashSet<>();
                    accountRoleMap.put(account, roleIds);
                }
                roleIds.add(roleId);
            }
        }
    }

    public static void addSpecialAccount(String account) throws SQLException {
        synchronized (accountRoleMap) {
            if (accountRoleMap.containsKey(account)) {
                return;
            }
            DBUtil.execSql(DBUtil.DB_USER, "insert into specialaccount values ('" + account + "', 0)");
            if (!accountRoleMap.containsKey(account)) {
                accountRoleMap.put(account, null);
            }
        }
    }

    public static void delSpecialAccount(String account) throws SQLException {
        synchronized (accountRoleMap) {
            DBUtil.execSql(DBUtil.DB_USER, "delete from specialaccount where account = '" + account + "'");
            accountRoleMap.remove(account);
        }
    }
}
