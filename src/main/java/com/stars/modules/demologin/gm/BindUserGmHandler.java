package com.stars.modules.demologin.gm;

import com.stars.AccountRow;
import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.role.userdata.Role;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-04-27 9:58
 */
public class BindUserGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args.length == 2) {
            String bindRoleId = args[0];
            String account = args[1];
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            if (accountRow == null) {
                PlayerUtil.send(roleId, new ClientText("account非法"));
            } else {
                if (accountRow.getAccountRole(roleId) != null) {
                    PlayerUtil.send(roleId, new ClientText("该账号下已有相同角色"));
                    return;
                }
                AccountRole accountRole = new AccountRole(account, bindRoleId, new Timestamp(System.currentTimeMillis()));
                Role role = DBUtil.queryBean(DBUtil.DB_USER, Role.class, "select * from role where roleid=" + bindRoleId);
                accountRole.roleName = role.getName();
                accountRole.roleLevel = role.getLevel();
                accountRole.roleJobId = role.getJobId();
                accountRole.lastLoginTimestamp = System.currentTimeMillis();
                DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, accountRole, "accountrole"));
                String queryLoginSql = "select * from `login` where roleid in(" + bindRoleId + ")";
                LoginRow loginRow = DBUtil.queryBean(DBUtil.DB_USER, LoginRow.class, queryLoginSql);
                List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
                Map<Long, LoginRow> loginRowMap = accountRow.getLoginRowMap();
                loginRowMap.put(Long.parseLong(bindRoleId), loginRow);
                if (relativeRoleList == null) {
                    relativeRoleList = new ArrayList<>();
                }
                relativeRoleList.add(accountRole);
                accountRow.setRelativeRoleList(relativeRoleList);
                PlayerUtil.send(roleId, new ClientText("绑号成功"));
            }
        }
    }
}
