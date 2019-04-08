package com.stars.core.gmpacket.specialaccount;

import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.multiserver.MultiServerHelper;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.util._HashMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-24 10:56
 */
public class QuerySpecialAccountDetailGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        List<Map<String, Object>> roleMaps = new ArrayList<>();
        try {
            if (args.get("account") != null && SpecialAccountManager.isSpecialAccount((String) args.get("account"))) {
                String accountSql = getQueryAccountSql((String) args.get("account"));
                String accountRoleSql = getQueryRoleSql((String) args.get("account"));
                _HashMap account = DBUtil.queryBean(DBUtil.DB_USER, _HashMap.class, accountSql);
                List<Long> roleList = DBUtil.queryList(DBUtil.DB_USER, Long.class, accountRoleSql);
                for (long roleId : roleList) {
                    Map<String, Object> roleMap = getRoleMap(account, roleId);
                    if (roleMap != null) {
                        roleMaps.add(roleMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, roleMaps.size(), resultToJson(roleMaps));
        return response.toString();
    }

    private Map<String, Object> getRoleMap(_HashMap account, long roleId) throws SQLException {
        String tmpAccountStr = account.getString("a.channel");
        int tmeAccount = Integer.parseInt(tmpAccountStr.split("@")[0]);
        Map<String, Object> map = new HashMap<>();
        map.put("serverId", MultiServerHelper.getServerId());
        map.put("account", account.getString("a.name"));
        map.put("channelId", tmeAccount);
        map.put("vipLev", account.getInt("a.viplevel"));
        map.put("totalMoney", account.getInt("a.chargesum"));
        String roleSql = getQueryRoleSql(roleId);
        String paymentSql = getQueryPaymentSql(roleId);
        _HashMap role = DBUtil.queryBean(DBUtil.DB_USER, _HashMap.class, roleSql);
        String payTime = DBUtil.queryBean(DBUtil.DB_USER, String.class, paymentSql);
        RoleSummaryComponent roleSummary = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(roleId, MConst.Role);
        map.put("roleId", roleId);
        map.put("roleName", role.getString("r.name"));
        map.put("roleLev", role.getInt("r.level"));
        map.put("fight", roleSummary.getFightScore());
        map.put("gold", role.getLong("r.gold"));
        map.put("bindgold", role.getLong("r.bandgold"));
        map.put("lastRechargeDate", payTime);
        return map;
    }

    /**
     * 这里能拿到    渠道，vip等级，vip经验，vip充值金额
     *
     * @param account
     * @return
     */
    private String getQueryAccountSql(String account) {
        StringBuilder sb = new StringBuilder();
        sb.append("select a.name, a.channel, a.viplevel, a.chargesum from account a where ");
        sb.append("a.name ='").append(account).append("'");
        return sb.toString();
    }

    /**
     * 这里拿账号对应的roleId
     *
     * @param account
     * @return
     */
    private String getQueryRoleSql(String account) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ar.roleid from accountrole ar where ");
        sb.append(" ar.account ='").append(account).append("'");
        return sb.toString();
    }

    /**
     * 这里拿玩家名字，等级，金币，绑金
     *
     * @param roleId
     * @return
     */
    private String getQueryRoleSql(long roleId) {
        StringBuilder sb = new StringBuilder();
        sb.append("select r.roleid, r.name, r.level, r.gold, r.bandgold from role r where ");
        sb.append(" r.roleid ='").append(roleId).append("'");
        return sb.toString();
    }

    /**
     * 角色最后充值时间
     *
     * @param roleId
     * @return
     */
    private String getQueryPaymentSql(long roleId) {
        StringBuilder sb = new StringBuilder();
        sb.append("select pm.time from payment pm where ");
        sb.append(" pm.roleid = '").append(roleId).append("' ");
        sb.append(" order by pm.time desc limit 1");
        return sb.toString();
    }
}
