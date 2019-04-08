package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.db.DBUtil;
import com.stars.modules.tool.event.GMDelToolEvent;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by zhoujin on 2017/3/22.
 * GM包裹删除
 */
public class DelPlayerBag extends GmPacketHandler {
    
    @Override
    public String handle(HashMap args) {       
    	GmPacketResponse response = null;
        try {
            long roleId = Long.parseLong((String) args.get("roleId"));
            int serverId = Integer.parseInt((String) args.get("serverId"));
            long uniCode = Long.parseLong((String) args.get("uniCode"));
            int amount = Integer.parseInt((String) args.get("amount"));
            if (amount <= 0) {
            	return null;
            }
            Player player = PlayerSystem.get(roleId);
            if (player != null) {
            	ServiceHelper.roleService().notice(roleId, new GMDelToolEvent(uniCode, amount));
            } else {
                String selectSql = new StringBuffer().append("select * from roletool").append(roleId % 10).append(" where roleid=").append(roleId).append(" and toolid =").append(uniCode).toString();
                RoleToolRow tool = DBUtil.queryBean(DBUtil.DB_USER, RoleToolRow.class, selectSql);
                if (null == tool) {
                	response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
                	return response.toString();
                }
                int result = Math.max(0,(tool.getCount() - amount));
                String updateSql = null;
                if (result == 0) {
                	updateSql = "delete from roletool" + (roleId % 10) + " where roleid=" + roleId +" and toolid =" + uniCode;
                }else {
                	updateSql = "update roletool" + (roleId % 10) + " set count =" + result + " where roleid=" + roleId +" and toolid =" + uniCode;
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
