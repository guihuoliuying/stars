package com.stars.core.gmpacket;

import com.google.gson.Gson;
import com.stars.modules.MConst;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2017/1/9.
 */
public class QueryPlayerDetailInfoGm extends GmPacketHandler {

    @Override
    public String handle(HashMap args) {
        QueryPlayerBasicInfoGm basicInfoGm = new QueryPlayerBasicInfoGm();
        String basic = basicInfoGm.handle(args);
        Gson json = new Gson();
        Map<String, Object> basicMap = json.fromJson(basic, HashMap.class);
        if (((Double) basicMap.get("count")).intValue() == 0) {
            GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            return response.toString();
        }
        List<Object> basicResult = (List<Object>) basicMap.get("result");

        Map<String, Object> detail = (Map<String, Object>) basicResult.get(0);
        long roleId = Long.valueOf((String) args.get("roleId"));
        detail.remove("blockStatus");
        detail.remove("talkStatus");
        detail.remove("blockTime");
        detail.remove("banTime");
        detail.remove("onlineStatus");
        detail.put("roleCreateTime", (long) ((double) detail.get("roleCreateTime")));

        RoleSummaryComponent role = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(roleId, MConst.Role);
        detail.put("fight", String.valueOf(role.getFightScore()));
        detail.put("job", String.valueOf(role.getRoleJob()));
        FamilySummaryComponent family = (FamilySummaryComponent) ServiceHelper.summaryService().getSummaryComponent(roleId, MConst.Family);
        if (family == null) {
            detail.put("faction_id", "0");
        } else {
            detail.put("faction_id", String.valueOf(family.getFamilyId()));
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(detail));
        return response.toString();
    }
}
