package com.stars.core.gmpacket;


import com.stars.modules.family.FamilyManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.family.main.FamilyData;
import com.stars.services.family.main.userdata.FamilyPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhoujin on 2017/3/23.
 * GM家族信息查询
 */
public class QueryFamilyInfo extends GmPacketHandler {
    @Override   
    public String handle(HashMap args) {
    	GmPacketResponse response = null;
    	long teamId = 0;
    	teamId = Long.parseLong((String) args.get("teamId"));
    	if (0 == teamId)
    		return null;
    	try {
    		FamilyData data = ServiceHelper.familyMainService().getFamilyDataClone(teamId);
    		List<Map> resultList = getResultMap(data);
    		response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList));
    	}catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
    
    private List<Map> getResultMap(FamilyData data) {
    	List<Map> resultList = new ArrayList<>();
        if( null == data) return resultList;
        FamilyPo DataPro = data.getFamilyPo();
        if (null == DataPro) return resultList;
        Map<String,Object> map = new HashMap<>();
        map.put("teamId", DataPro.getFamilyId());
        map.put("teamName", DataPro.getName());
        map.put("lev", DataPro.getLevel());
        map.put("teamEquip", DataPro.getMoney());
        map.put("teamFight", DataPro.getTotalFightScore());
        map.put("teamNotice", DataPro.getNotice());
        int memberCount = DataPro.getMemberCount();
        int memberMax = FamilyManager.levelVoMap.get(DataPro.getLevel()).getMemberLimit();
        String memberStr = String.format("%d/%d",memberCount,memberMax);
        map.put("teamNum", memberStr);
        resultList.add(map);
        return resultList;
    }
}
