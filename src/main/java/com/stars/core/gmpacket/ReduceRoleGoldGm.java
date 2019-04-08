package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.db.DBUtil;
import com.stars.modules.role.event.ReduceRoleResourceEvent;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.tool.ToolManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by liuyuheng on 2017/2/14.
 */
public class ReduceRoleGoldGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        try {
            long roleId = Long.parseLong((String) args.get("roleId"));
            int type = Integer.parseInt((String) args.get("type"));
            int serverId = Integer.parseInt((String) args.get("serverId"));
            // 运营传过来的自带负号
            long value = Long.parseLong((String) args.get("value"));
            Player player = PlayerSystem.get(roleId);
            if (player != null) {
                ServiceHelper.roleService().notice(roleId, new ReduceRoleResourceEvent(type, value));
            } else {
                String selectSql = "select * from `role` where `roleid`=" + roleId;
                Role role = DBUtil.queryBean(DBUtil.DB_USER, Role.class, selectSql);
                long result;
                String updateSql = null;
                if (type == ToolManager.GOLD) {
                    result = Math.max(0, role.getGold() + value);
                    updateSql = "update `role` set `gold`=" + result + " where `roleid`=" + roleId;
                } else if (type == ToolManager.BANDGOLD) {
                    result = Math.max(0, role.getBandGold() + value);
                    updateSql = "update `role` set `bandgold`=" + result + " where `roleid`=" + roleId;
                }
                if (updateSql != null) {
                    DBUtil.execSql(DBUtil.DB_USER, updateSql);
                }
            }
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
