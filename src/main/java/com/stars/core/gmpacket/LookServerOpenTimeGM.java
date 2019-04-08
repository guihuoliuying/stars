package com.stars.core.gmpacket;

import com.stars.core.SystemRecordMap;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LookServerOpenTimeGM extends GmPacketHandler {

    @Override
    public String handle(HashMap paramHashMap) {
        long time = SystemRecordMap.openServerTime;
        List resultList = new ArrayList<>();
        resultList.add(parseYMD(time));
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(resultList));
        return response.toString();
    }

    private String parseYMD(long time) {
        String timeStr = (time / 1000000) + "";
        StringBuilder sb = new StringBuilder();
        sb.append(timeStr.substring(0, 4)).append("-").append(timeStr.substring(4, 6)).append("-").append(timeStr.substring(6, 8));
        return sb.toString();
    }
}
