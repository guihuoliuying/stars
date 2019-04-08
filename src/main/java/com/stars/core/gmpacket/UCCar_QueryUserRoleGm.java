package com.stars.core.gmpacket;

import com.stars.AccountRow;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.modules.role.userdata.Role;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/22.
 */
public class UCCar_QueryUserRoleGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        String accountId = (String) args.get("accountId");
        try {
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(accountId, null);
            if (accountRow != null) {
                List<AccountRole> relativeRoleList = DBUtil.queryList(DBUtil.DB_USER, AccountRole.class,
                        "select * from `accountrole` where `account`='" + accountId + "'");
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
                List<Map<String, Object>> list = new ArrayList<>();
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
                    Map<String, Object> data = new HashMap<>();
                    data.put("roleId", accountRole.getRoleId());
                    data.put("roleName", role.getName());
                    data.put("lastLoginTimestamp", loginRow.getLastDailyResetTimestamp());
                    data.put("roleLevel", role.getLevel());
                    list.add(data);
                }
                GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(list));
                return response.toString();
            }
        } catch (SQLException e) {
            GmPacketResponse response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 1, e.getMessage());
            return response.toString();
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 1, "");
        return response.toString();
    }
}
