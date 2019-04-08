package com.stars.core.gmpacket;

import com.stars.core.persist.DbRowDao;
import com.stars.core.db.DBUtil;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.userdata.RoleUCGiftRecord;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/22.
 */
public class UCCar_sendGift extends GmPacketHandler {
    DbRowDao dbRowDao = new DbRowDao("allEmailSendGm");

    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        Map<String, Object> data = new HashMap<>();
        try {
            String accountId = (String) args.get("accountId");
            String gameId = (String) args.get("gameId");
            String kaIdStr = (String) args.get("kaId");
            String serverId = (String) args.get("serverId");
            String roleIdStr = (String) args.get("roleId");
            String getDate = (String) args.get("getDate");//格式为“yyyy-MM-dd”
            int kaId = Integer.parseInt(kaIdStr);
            long roleId = Long.parseLong(roleIdStr);
            String sql = "select * from roleucgiftrecord where roleId=%s and getdate='%s';";
            RoleUCGiftRecord roleUCGiftRecord = DBUtil.queryBean(DBUtil.DB_USER, RoleUCGiftRecord.class, String.format(sql, roleId, getDate));
            if (roleUCGiftRecord == null) {
                roleUCGiftRecord = new RoleUCGiftRecord(roleId, getDate);
            }
            if (roleUCGiftRecord.canReceiveKaId(kaId)) {
                Map<Integer, Integer> affix = ToolManager.UC_GIFT_MAP.get(kaId);
                Integer emailTemplateId = ToolManager.UC_GIFT_EMAIL_MAP.get(kaId);
                if (affix == null) {
                    data.put("repeat", -1);
                    data.put("success", 0);
                    response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(data));
                } else {
                    roleUCGiftRecord.addKaId(kaId);
                    if (roleUCGiftRecord.getKaIdSet().size() == 1) {
                        dbRowDao.insert(roleUCGiftRecord);
                    } else {
                        dbRowDao.update(roleUCGiftRecord);
                    }
                    dbRowDao.flush(true,true);
                    ServiceHelper.emailService().sendToSingle(roleId, emailTemplateId, 0L, "系统", affix);
                    data.put("repeat", 0);
                    data.put("success", 1);
                    response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(data));
                }
            } else {
                data.put("repeat", 1);
                data.put("success", 0);
                response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(data));
            }
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 1, "");
        }


        return response.toString();
    }
}
