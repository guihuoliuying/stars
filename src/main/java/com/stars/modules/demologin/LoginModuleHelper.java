package com.stars.modules.demologin;

import com.stars.AccountRow;
import com.stars.SwitchEntranceGm;
import com.stars.core.SystemRecordMap;
import com.stars.core.actor.Actor;
import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.module.ModuleManager;
import com.stars.core.persist.SaveDBManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.redpoint.RedPoints;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.message.DailyResetMsg;
import com.stars.modules.demologin.message.FiveOClockResetMsg;
import com.stars.modules.demologin.message.MonthlyResetMsg;
import com.stars.modules.demologin.message.WeeklyResetMsg;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.modules.role.userdata.Role;
import com.stars.startup.MainStartup;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by zhaowenshuo on 2016/7/26.
 */
public class LoginModuleHelper {

    public static volatile byte serverState = SwitchEntranceGm.CLOSE;    // 0 入口开放 1 入口关闭

    public static AccountRow getOrLoadAccount(String name, String md5Password) throws SQLException {
        /**
         * 获取账号转移后的账号（如果存在的话，不存在则用之前账号）
         */
        name = getTransferAccount(name);
        AccountRow accountRow = MainStartup.accountMap.get(name);
        if (accountRow != null) {
            if (md5Password == null) {
                return accountRow;
            }
            if (accountRow.getPassword().equals(md5Password)) {
                com.stars.util.LogUtil.info("获取账号|有密码|内存|验证成功|account:{}|password:{}", name, md5Password);
                return accountRow;
            } else {
                com.stars.util.LogUtil.info("获取账号|有密码|内存|验证失败|account:{}|password:{}", name, md5Password);
                return null;
            }
        }
        StringBuffer bf = new StringBuffer();
        bf.append("select * from `account` where `name`='").append(name).append("'");
        if (md5Password != null) {
            bf.append(" and `password`='").append(md5Password).append("'");
        }
        //同时验证账号和密码
        accountRow = DBUtil.queryBean(DBUtil.DB_USER, AccountRow.class, bf.toString());
        com.stars.util.LogUtil.info("获取账号|有密码|内存|account:{}|password:{}|accountRow:{}", name, md5Password, accountRow);
        if (accountRow != null) {
            List<AccountRole> relativeRoleList = DBUtil.queryList(DBUtil.DB_USER, AccountRole.class,
                    "select * from `accountrole` where `account`='" + name + "'");


            List<Long> roleIdList = new ArrayList<>();
            for (AccountRole accountRole : relativeRoleList) {
                roleIdList.add(Long.parseLong(accountRole.getRoleId()));
            }
            String roleIdListStr = StringUtil.makeString(roleIdList, ',');

            Map<Long, Role> roleMap = new HashMap<>();
            Map<Long, LoginRow> loginRowMap = new HashMap<>();
            if (roleIdListStr != null && roleIdListStr.length() > 0) {
                String queryRoleSql = "select * from `role` where roleid in(" + roleIdListStr + ")";
                String queryLoginSql = "select * from `login` where roleid in(" + roleIdListStr + ")";

                roleMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", Role.class, queryRoleSql);
                loginRowMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", LoginRow.class, queryLoginSql);
            }
            accountRow.setLoginRowMap(loginRowMap);

            for (AccountRole accountRole : relativeRoleList) {
                long roleId = Long.parseLong(accountRole.getRoleId());
                Role role = roleMap.get(roleId);
                LoginRow loginRow = loginRowMap.get(roleId);
                if (role == null || loginRow == null) {
                    continue;
                }
                accountRole.roleName = role.getName();
                accountRole.roleLevel = role.getLevel();
                accountRole.roleJobId = role.getJobId();
                accountRole.lastLoginTimestamp = loginRow.getLastLoginTimestamp();
                accountRole.fightScore = role.getFightScore();
            }
            /**
             * 账号职业激活列表
             */
            String sql = "select jobid from accountjobactive where account='%s'";
            List<Integer> activedJobs = DBUtil.queryList(DBUtil.DB_USER, Integer.class, String.format(sql, accountRow.getName()));
            accountRow.setActivedJobs(activedJobs);

            accountRow.setRelativeRoleList(relativeRoleList);
            AccountRow oldValue = MainStartup.accountMap.putIfAbsent(name, accountRow);
            return oldValue == null ? accountRow : oldValue;
        }
        return null;
    }

    /**
     * 查询此账户是否存在账号转移
     * 如果存在转移号，用转移的 没有则用现在的
     *
     * @param name
     * @return
     * @throws SQLException
     */
    public static String getTransferAccount(String name) {
        return name;
    }

    /**
     * 每日重置
     * 玩家登录检查和系统发送重置消息应该是满足HB关系的
     * 1. 玩家登录检查读到旧的resetTimestamp，必定收到重置消息
     *
     * @param isInitializing 是否正在初始化，如果正在初始化则不需要向玩家发送消息
     * @throws Exception
     */
    public static void resetDaily(boolean isInitializing) throws Exception {
        synchronized (LoginModule.class) {
            if (isInitializing) {
                com.stars.util.LogUtil.info("登录初始化重置时间");
            } else {
                com.stars.util.LogUtil.info("每日重置时间");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            SystemRecordMap.update("dailyResetTimestamp", calendar.getTimeInMillis());

            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            SystemRecordMap.update("dateVersion", (int) (calendar.getTimeInMillis() / 1000));

            if (!isInitializing) {
                for (com.stars.core.actor.Actor actor : PlayerSystem.system().getActors().values()) {
                    try {
                        if (actor instanceof Player) {
                            actor.tell(new DailyResetMsg(), com.stars.core.actor.Actor.noSender);
                        }
                    } catch (Throwable cause) {
                        com.stars.util.LogUtil.error("每日重置异常:发送消息异常", cause);
                    }
                }
            }
        }
    }

    public static void FiveOClockResetDaily(boolean isInitializing) throws Exception {
        synchronized (LoginModule.class) {
            if (isInitializing) {
                com.stars.util.LogUtil.info("登录初始化五点重置时间");
            } else {
                com.stars.util.LogUtil.info("每日凌晨五点重置时间");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            SystemRecordMap.update("fiveOClockResetTimestamp", calendar.getTimeInMillis());

            if (!isInitializing) {
                for (com.stars.core.actor.Actor actor : PlayerSystem.system().getActors().values()) {
                    try {
                        if (actor instanceof Player) {
                            actor.tell(new FiveOClockResetMsg(), com.stars.core.actor.Actor.noSender);
                        }
                    } catch (Throwable cause) {
                        com.stars.util.LogUtil.error("每日凌晨五点重置异常:发送消息异常", cause);
                    }
                }
            }
        }
    }

    public static void resetWeekly(boolean isInitializing) throws Exception {
        synchronized (LoginModule.class) {
            if (isInitializing) {
                com.stars.util.LogUtil.info("登录初始化重置时间");
            } else {
                com.stars.util.LogUtil.info("每周重置时间");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            SystemRecordMap.update("weeklyResetTimestamp", calendar.getTimeInMillis());

            if (!isInitializing) {
                for (com.stars.core.actor.Actor actor : PlayerSystem.system().getActors().values()) {
                    try {
                        if (actor instanceof Player) {
                            actor.tell(new WeeklyResetMsg(), com.stars.core.actor.Actor.noSender);
                        }
                    } catch (Throwable cause) {
                        com.stars.util.LogUtil.error("每周重置异常:发送消息异常", cause);
                    }
                }
            }
        }
    }

    public static void resetMonthly(boolean isInitializing) throws Exception {
        synchronized (LoginModule.class) {
            if (isInitializing) {
                com.stars.util.LogUtil.info("登录初始化重置时间");
            } else {
                com.stars.util.LogUtil.info("每月重置时间");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            SystemRecordMap.update("monthlyResetTimestamp", calendar.getTimeInMillis());

            if (!isInitializing) {
                for (com.stars.core.actor.Actor actor : PlayerSystem.system().getActors().values()) {
                    try {
                        if (actor instanceof Player) {
                            actor.tell(new MonthlyResetMsg(), Actor.noSender);
                        }
                    } catch (Throwable cause) {
                        com.stars.util.LogUtil.error("每月重置异常:发送消息异常", cause);
                    }
                }
            }
        }
    }


    public static long getCreateRoleRemainCd(AccountRow accountRow_) {
        long lastCreateRoleTimestamp = accountRow_.getLastCreateRoleTimestamp();
        long currentTimestamp = System.currentTimeMillis();
        long customCreateRoleCd = Long.parseLong(DataManager.getCommConfig("createroletime")) * 1000;
        long diffTime = 0;
        int limitAtCount = Integer.parseInt(DataManager.getCommConfig("createrolelimit"));
        //要判断是否达到判断cd的条件;
        LogUtil.info("已创角个数：{}", accountRow_.getCreatedRoleCount());
        if (accountRow_.getCreatedRoleCount() >= limitAtCount) {
            diffTime = customCreateRoleCd - (currentTimestamp - lastCreateRoleTimestamp);
        }
        return diffTime;
    }

    public static void inject(Map<String, Module> moduleMap, ModuleContext context, RedPoints redPoints) {
        try {
            Field contextField = AbstractModule.class.getDeclaredField("context");
            Field redPointsField = AbstractModule.class.getDeclaredField("redPoints");
            for (Module module : moduleMap.values()) {
                // 注入上下文
                contextField.setAccessible(true);
                contextField.set(module, context);
                contextField.setAccessible(false);
                // 注入红点系统
                redPointsField.setAccessible(true);
                redPointsField.set(module, redPoints);
                redPointsField.setAccessible(false);
            }
        } catch (Exception e) {
            // fixme: log it
            e.printStackTrace();
        }
    }

    public static Player loadPlayerFromDB(long roleId, AccountRow accountRow) throws Throwable {
        Player player = new Player(roleId);
        player.setFrom((byte) 1);
        EventDispatcher eventDispatcher = new EventDispatcher();
        ModuleContext context = new ModuleContext(roleId);
        RedPoints redPoints = new RedPoints(player);
        Map<String, Module> moduleMap = ModuleManager.newModuleList(roleId, player, eventDispatcher);
        inject(moduleMap, context, redPoints);
        for (Module module : moduleMap.values()) {
            moduleAccountInject(module, accountRow);
            module.onDataReq();
        }
        player.init(moduleMap, eventDispatcher, context, redPoints);
        Player oldPlayer = PlayerSystem.getOrAdd(roleId, player);
        if (oldPlayer == player) {
            SaveDBManager.addRole2Save(roleId);
        } else {
            player = oldPlayer;
        }
        return player;
    }

    /**
     * 实现AccountRowAware接口的模块，账户信息会自动注入模块内
     *
     * @param module
     * @param accountRow
     */
    public static void moduleAccountInject(Module module, AccountRow accountRow) {
        if (module instanceof AccountRowAware) {
            AccountRowAware accountRowModule = (AccountRowAware) module;
            accountRowModule.setAccountRow(accountRow);
        }
    }
}
