package com.stars.core.gmpacket;

import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.flow.FamilyWarFlow;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by chenkeyu on 2017-05-15 10:48
 */
public class FamilywarTriggerGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        int warType = 0;
        if (args.containsKey("value")) {
            warType = Integer.parseInt((String) args.get("value"));
        }
        try {
            if (warType == FamilyWarConst.W_TYPE_LOCAL) {
                FamilyWarFlow.isLocalRunning = true;
                ServiceHelper.familyWarService().createFamilyInfo();
                ServiceHelper.familyWarLocalService().startByDisaster(MultiServerHelper.getServerId());
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
                ServiceHelper.familyWarService().startQualifyByDisaster(MultiServerHelper.getServerId());
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
                ServiceHelper.familyWarService().startRemoteByDisaster(MultiServerHelper.getServerId());
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            } else {
                response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
            }
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        }
        return response.toString();
    }
}
