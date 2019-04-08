package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.db.DBUtil;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.event.ModifyRoleLevelEvent;
import com.stars.modules.role.userdata.Role;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by liuyuheng on 2017/2/16.
 */
public class ModifyRoleLevelGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        try {
            long roleId = Long.parseLong((String) args.get("roleId"));
            int serverId = Integer.parseInt((String) args.get("serverId"));
            // 值,自带符号,负数为减少,正数为增加
            long value = Long.parseLong((String) args.get("value"));
            Player player = PlayerSystem.get(roleId);
            if (player != null) {
                ServiceHelper.roleService().notice(roleId, new ModifyRoleLevelEvent((int) value));
            } else {
                String sql = "select * from `role` where `roleid`=" + roleId;
                Role role = DBUtil.queryBean(DBUtil.DB_USER, Role.class, sql);
                int result = (int) Math.min(RoleManager.getMaxlvlByJobId(role.getJobId()), Math.max(0, role.getLevel() + value));
                sql = "update `role` set `level`=" + result + " where `roleid`=" + roleId;
                DBUtil.execSql(DBUtil.DB_USER, sql);
            }
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
