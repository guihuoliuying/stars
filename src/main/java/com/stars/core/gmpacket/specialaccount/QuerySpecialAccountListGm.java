package com.stars.core.gmpacket.specialaccount;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-24 10:55
 */
public class QuerySpecialAccountListGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        List<Map> mapList = new ArrayList<>();
        try {
            int start;
            int limit;
            if (args.get("start") != null && args.get("limit") != null) {
                start = Integer.parseInt((String) args.get("start"));
                limit = Integer.parseInt((String) args.get("limit"));
                for (int i = start; i < SpecialAccountManager.getAccountSize() && i < limit; i++) {
                    Map<String, Object> accountMap = new HashMap<>();
                    accountMap.put("account", SpecialAccountManager.getAccountByIndex(i));
                    mapList.add(accountMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, mapList.size(), resultToJson(mapList));
        return response.toString();
    }
}
