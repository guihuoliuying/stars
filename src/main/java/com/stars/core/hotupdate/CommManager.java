package com.stars.core.hotupdate;

import com.stars.AccountRow;
import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.core.gmpacket.CommandGm;
import com.stars.db.DBUtil;
import com.stars.db.SqlUtil;
import com.stars.modules.collectphone.CollectPhoneUtil;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.modules.gm.gmhandler.AccessControlGmHandler;
import com.stars.modules.role.userdata.Role;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;
import com.stars.util.JsonUtil;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public class CommManager implements CommManagerInterface, HotUpdateInterface {

    /**
     * 执行comm命令
     */
    public String comm(List<String> paramerList) {
        if (paramerList == null) return "gm命令参数为null,执行失败";
        CommandGm.gmLock.lock();
        String result = "command命令执行成功";
        try {
            String operateName = paramerList.get(0);
            Method m = CommManager.class.getDeclaredMethod(operateName.trim(), List.class);
            result = (String) m.invoke(null, paramerList);
            return result;
        } catch (NoSuchMethodException e) {
            result = "没有该命令,请检查";
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            result = "command命令执行失败," + e.getMessage();
        } finally {
            CommandGm.gmLock.unlock();
        }
        return result;
    }

    public static String gmTest(List<String> paramerList) {
        LogUtil.info("gmTest  执行成功:" + StringUtil.makeString(paramerList, ','));
        return "gmTest  执行成功:" + StringUtil.makeString(paramerList, ',');
    }

    public static String modAppointField(List<String> paramterList)
            throws IllegalArgumentException, IllegalAccessException,
            InstantiationException {

        if (paramterList != null && paramterList.size() == 3) {

            // 获取类名,必须是完整的类名,包含包名
            String className = paramterList.get(0).toString();
            Class c;
            try {
                c = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                return "没有这个类,类名必须是全名!";
            }
            // 获取要修改的属性
            String filedName = paramterList.get(1).toString();
            // 遍历类属性
            Field[] fields = c.getFields();
            Field modField = null;
            for (Field f : fields) {
                if (f.getName().equals(filedName)) {
                    modField = f;
                    break;
                }
            }
            if (modField == null) {
                return className + "中没有这个属性";
            }

            // 给指定的属性赋值
            Class fieldType = modField.getType();
            // 获取指定类的对象
            Object object = c.newInstance();
            // 获取要修改为的值
            String value = paramterList.get(2).toString();
            if (value == null || value.equals("")) {
                return "值不能为空";
            }
            if (fieldType == byte.class) {
                try {
                    byte bType = Byte.parseByte(value);
                    modField.setByte(object, bType);
                } catch (NumberFormatException e) {
                    return "byte类型转型错误";
                }
            } else if (fieldType == char.class) {
                try {
                    char cType = value.charAt(0);
                    modField.setChar(object, cType);
                } catch (NumberFormatException e) {
                    return "char类型转型错误";
                }

            } else if (fieldType == short.class) {
                try {
                    short sType = Short.parseShort(value);
                    modField.setShort(object, sType);
                } catch (NumberFormatException e) {
                    return "short类型转型错误";
                }

            } else if (fieldType == int.class) {
                try {
                    int iType = Integer.parseInt(value);
                    modField.setInt(object, iType);
                } catch (NumberFormatException e) {
                    return "int类型转型错误";
                }

            } else if (fieldType == long.class) {
                try {
                    long lType = Long.parseLong(value);
                    modField.setLong(object, lType);
                } catch (NumberFormatException e) {
                    return "long类型转型错误";
                }
            } else if (fieldType == float.class) {
                try {
                    float fType = Float.parseFloat(value);
                    modField.setFloat(object, fType);
                } catch (NumberFormatException e) {
                    return "float类型转型错误";
                }
            } else if (fieldType == double.class) {
                try {
                    double dType = Double.parseDouble(value);
                    modField.setDouble(object, dType);
                } catch (NumberFormatException e) {
                    return "double类型转型错误";
                }
            } else if (fieldType == boolean.class) {
                try {
                    boolean bType = Boolean.parseBoolean(value);
                    modField.setBoolean(object, bType);
                } catch (NumberFormatException e) {
                    return "boolean类型转型错误";
                }
            } else if (fieldType == String.class) {
                modField.set(object, value);
            }

            String info = className.substring(className.lastIndexOf('.')) + "."
                    + filedName + "=" + value;
            return "执行结果:" + info;
        }
        return "参数个数错误";

    }

    public static String ac(List<String> paramterList) {
        boolean isOpen = false;
        switch (paramterList.get(0)) {
            case "open":
                isOpen = true;
                break;
            case "close":
                isOpen = false;
                break;
            default:
                return "参数异常";
        }
        List<String> list = new LinkedList<>();
        for (int i = 1; i < paramterList.size(); i++) {
            list.add(paramterList.get(i));
        }
        try {
            AccessControlGmHandler.openOrClose(isOpen, list);
            return "执行成功";
        } catch (Exception e) {
            return "执行失败";
        }
    }

    public static String reloadFightServer(List<String> paramterList) {
        // 战斗服
        BootstrapConfig config = ServerManager.getServer().getConfig();
        ConcurrentHashMap<String, Properties> map = config.getPubProps();
        boolean flag = true;
        for (ConcurrentHashMap.Entry<String, Properties> entry : map.entrySet()) {
            if (entry.getKey() == "fightServer") {
                try {
                    MainRpcHelper.fightBaseService().reloadProduct(Integer.parseInt(entry.getKey()));
                } catch (Exception e) {
                    ServerLogConst.exception.info(e.getMessage(), e);
                    flag = false;
                }
            }
        }
        return flag ? "战斗服重载成功" : "战斗服重载失败";
    }

    public static String reloadFightingMaster(List<String> paramterList) {
        // 斗神殿服
        boolean flag = true;
        try {
            BootstrapConfig config = ServerManager.getServer().getConfig();
            MainRpcHelper.fightingMasterService().reloadProduct(MultiServerHelper.getFightingMasterServer());
        } catch (Exception e) {
            ServerLogConst.exception.info(e.getMessage(), e);
            flag = false;
        }
        return flag ? "巅峰对决服重载成功" : "巅峰对决服重载失败";
    }

    public static String reloadLootTreasure(List<String> paramterList) {
        // 夺宝服
        boolean flag = true;
        BootstrapConfig config = ServerManager.getServer().getConfig();
        Properties loottreasure = config.getProps().get("loottreasure");
        try {
            MainRpcHelper.rmltService().reloadProduct(Integer.parseInt(loottreasure.getProperty("serverId")));
        } catch (Exception e) {
            ServerLogConst.exception.info(e.getMessage(), e);
            flag = false;
        }
        return flag ? "夺宝服重载成功" : "夺宝服重载失败";
    }

    public static String bindRole(List<String> paramterList) {
        String result = "绑号失败";
        try {
            if (paramterList.size() == 4) {
                String account = paramterList.get(1) + "#" + paramterList.get(2);
                long roleId = Long.parseLong(paramterList.get(3));
                AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
                if (accountRow == null) {
                    result = "绑号失败,不存在账号,account:" + account;
                } else if (accountRow.getAccountRole(roleId) != null) {
                    result = "绑号失败,已有该角色,roleId:" + roleId;
                } else {
                    AccountRole accountRole = new AccountRole(account, Long.toString(roleId), new Timestamp(System.currentTimeMillis()));
                    Role role = DBUtil.queryBean(DBUtil.DB_USER, Role.class, "select * from role where roleid=" + roleId);
                    accountRole.roleName = role.getName();
                    accountRole.roleLevel = role.getLevel();
                    accountRole.roleJobId = role.getJobId();
                    accountRole.lastLoginTimestamp = System.currentTimeMillis();
                    DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, accountRole, "accountrole"));
                    List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
                    if (relativeRoleList == null) {
                        relativeRoleList = new ArrayList<>();
                    }
                    relativeRoleList.add(accountRole);
                    accountRow.setRelativeRoleList(relativeRoleList);
                    result = "绑号成功,account:" + account + ",roleId:" + roleId;
                }
            }
        } catch (Exception e) {
            result = "绑号失败,发生异常:" + e.getMessage();
            LogUtil.error("GM绑号", e);
        }
        return result;
    }

    public static String bindRole2(List<String> paramterList) {
        String result = "绑号失败";
        try {
            if (paramterList.size() == 3) {
                long selfRoleId = Long.parseLong(paramterList.get(1));
                long otherRoleId = Long.parseLong(paramterList.get(2));
                AccountRole selfAccountRole = DBUtil.queryBean(
                        DBUtil.DB_USER, AccountRole.class, "select * from accountrole where roleid='" + selfRoleId + "'");
                AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(selfAccountRole.getAccount(), null);
                if (accountRow == null) {
                    result = "绑号失败,不存在账号,account:" + selfAccountRole.getAccount();
                } else if (accountRow.getAccountRole(otherRoleId) != null) {
                    result = "绑号失败,已有该角色,roleId:" + otherRoleId;
                } else {
                    AccountRole accountRole = new AccountRole(
                            selfAccountRole.getAccount(), Long.toString(otherRoleId), new Timestamp(System.currentTimeMillis()));
                    Role role = DBUtil.queryBean(DBUtil.DB_USER, Role.class, "select * from role where roleid=" + otherRoleId);
                    accountRole.roleName = role.getName();
                    accountRole.roleLevel = role.getLevel();
                    accountRole.roleJobId = role.getJobId();
                    accountRole.lastLoginTimestamp = System.currentTimeMillis();
                    DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, accountRole, "accountrole"));
                    String queryLoginSql = "select * from `login` where roleid in(" + otherRoleId + ")";
                    LoginRow loginRow = DBUtil.queryBean(DBUtil.DB_USER, LoginRow.class, queryLoginSql);
                    List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
                    Map<Long, LoginRow> loginRowMap = accountRow.getLoginRowMap();
                    loginRowMap.put(otherRoleId, loginRow);
                    if (relativeRoleList == null) {
                        relativeRoleList = new ArrayList<>();
                    }
                    relativeRoleList.add(accountRole);
                    accountRow.setRelativeRoleList(relativeRoleList);
                    result = "绑号成功,account:" + selfAccountRole.getAccount() + ",roleId:" + otherRoleId;
                }
            }
        } catch (Exception e) {
            result = "绑号失败,发生异常:" + e.getMessage();
            LogUtil.error("GM绑号", e);
        }
        return result;
    }

    public static String unbindRole2(List<String> paramterList) {
        String result = "解绑失败";
        try {
            if (paramterList.size() == 3) {
                long selfRoleId = Long.parseLong(paramterList.get(1));
                long otherRoleId = Long.parseLong(paramterList.get(2));
                AccountRole selfAccountRole = DBUtil.queryBean(
                        DBUtil.DB_USER, AccountRole.class, "select * from accountrole where roleid='" + selfRoleId + "'");
                AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(selfAccountRole.getAccount(), null);
                if (accountRow == null) {
                    result = "解绑失败,不存在账号,account:" + selfAccountRole.getAccount();
                } else if (accountRow.getAccountRole(otherRoleId) == null) {
                    result = "解绑失败,不存在角色,roleId:" + otherRoleId;
                } else {
                    AccountRole otherRole = accountRow.getAccountRole(otherRoleId);
                    accountRow.getRelativeRoleList().remove(otherRole);
                    DBUtil.execSql(DBUtil.DB_USER, otherRole.getDeleteSql());
                    Map<Long, LoginRow> loginRowMap = accountRow.getLoginRowMap();
                    loginRowMap.remove(otherRoleId);
                    result = "解绑成功,account:" + selfAccountRole.getAccount() + ",roleId:" + otherRoleId;
                }
            }
        } catch (Exception e) {
            result = "解绑失败,发生异常:" + e.getMessage();
            LogUtil.error("GM解绑", e);
        }
        return result;
    }

    /**
     * 添加禁止渠道列表
     *
     * @param paramterList
     * @return
     */
    public static String addForbidChannel4CollectPhone(List<String> paramterList) {
        String result = "操作成功";
        try {
            Set<Integer> channelIds = new HashSet<>();
            for (int index = 1; index < paramterList.size(); index++) {
                channelIds.add(Integer.parseInt(paramterList.get(index)));
            }
            CollectPhoneUtil.addForbidCollectPhoneChannels(channelIds);
        } catch (Exception e) {
            result = "操作失败,发生异常:" + e.getMessage();
            LogUtil.error("添加收集号码渠道设定错误", e);
        }
        return result;
    }

    /**
     * 删除禁止渠道列表
     *
     * @param paramterList
     * @return
     */
    public static String delForbidChannel4CollectPhone(List<String> paramterList) {
        String result = "操作成功";
        try {
            Set<Integer> channelIds = new HashSet<>();
            for (int index = 1; index < paramterList.size(); index++) {
                channelIds.add(Integer.parseInt(paramterList.get(index)));
            }
            CollectPhoneUtil.delForbidCollectPhoneChannels(channelIds);
        } catch (Exception e) {
            result = "操作失败,发生异常:" + e.getMessage();
            LogUtil.error("删除收集号码渠道设定错误", e);
        }
        return result;
    }

    /**
     * 删除禁止渠道列表
     *
     * @param paramterList
     * @return
     */
    public static String queryTableCount(List<String> paramterList) {
        String result = "";
        try {
            Map<String, Integer> tableCountMap = new HashMap<>();
            for (int index = 1; index < paramterList.size(); index++) {
                String tableName = paramterList.get(index);
                String sql = "select count(*) from %s;";
                try {
                    long count = DBUtil.queryCount(DBUtil.DB_USER, String.format(sql, tableName));
                    tableCountMap.put(tableName, (int) count);
                } catch (Exception e) {
                    LogUtil.error("查询数据库数据量错误:" + tableName, e);
                    tableCountMap.put(tableName, -1);
                }
            }
            result = JsonUtil.toJson(tableCountMap);
        } catch (Exception e) {
            result = "操作失败,发生异常:" + e.getMessage();
        }
        return result;
    }

    /**
     * 活动重置（备用）
     * @param paramterList
     * @return
     */
    public static String actLoopReset(List<String> paramterList) {
        String result = "";
        try {
            ServiceHelper.actLoopResetService().resetAndLoop();
            result = "操作成功";
        } catch (Exception e) {
            LogUtil.error("活动循环重置失败:"+e.getMessage(),e);
            result = "操作失败,发生异常:" + e.getMessage();
        }
        return result;
    }
}
