package com.stars.core.gmpacket.email;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/1/10.
 */
public class EmailViewGm extends GmPacketHandler {

    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        long roleId = Long.parseLong((String) args.get("roleId"));
        try {
            List<Map<String, Object>> emailList = ServiceHelper.emailService().gmView(roleId);
            response = new GmPacketResponse(GmPacketResponse.SUC, emailList.size(), resultToJson(emailList));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }

}
