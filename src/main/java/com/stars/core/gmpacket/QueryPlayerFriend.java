package com.stars.core.gmpacket;

import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.SummaryComponent;
import com.stars.services.summary.SummaryConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhoujin on 2017/3/23.
 */
public class QueryPlayerFriend extends GmPacketHandler {
    @Override   
    public String handle(HashMap args) {
    	GmPacketResponse response = null;
    	long roleId = 0;
    	int serverId = 0;
    	roleId = Long.parseLong((String) args.get("roleId"));
    	serverId = Integer.parseInt((String) args.get("serverId"));
    	if (roleId == 0 || serverId == 0) {
    		response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
    		return response.toString();
    	}
    	try {
    		List<Long> FriendIdList = ServiceHelper.friendService().getFriendList(roleId);
    		
    		List<Map> resultList = getResultMap(FriendIdList);
    		response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList));
    	} catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
    
    private List<Map> getResultMap(List<Long> FriendIdList) {
    	List<Map> resultList = new ArrayList<>();
        if(FriendIdList == null) return resultList; 
        for(Long FriendId : FriendIdList) {
			SummaryComponent Summary = ServiceHelper.summaryService().getSummaryComponent(FriendId, SummaryConst.C_ROLE);
			if (null == Summary)
				continue;
			RoleSummaryComponent RoleSummary = (RoleSummaryComponent)Summary;
			Map<String,Object> map = new HashMap<>();
			map.put("roleId", FriendId);
			map.put("roleName", RoleSummary.getRoleName());
			map.put("level", RoleSummary.getRoleLevel());
			map.put("job", RoleSummary.getRoleJob());
			resultList.add(map);
		}
        return resultList;
    }
}
