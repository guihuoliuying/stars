package com.stars.core.gmpacket;


import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceUtil;
import com.stars.services.family.main.userdata.FamilyMemberPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhoujin on 2017/3/23.
 * GM家族成员信息查询
 */
public class QueryFamilyMemberInfo extends GmPacketHandler {
    @Override   
    public String handle(HashMap args) {
    	GmPacketResponse response = null;
    	long teamId = 0;
    	teamId = Long.parseLong((String) args.get("teamId"));
    	if (0 == teamId)
    		return null;
    	try {
    		List<FamilyMemberPo> FamilyMembers = ServiceHelper.familyMainService().getMemberList(teamId, false);
    		List<Map> resultList = getResultMap(FamilyMembers);
    		response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList));
    	}catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
    
    private List<Map> getResultMap(List<FamilyMemberPo> FamilyMembers) {
    	List<Map> resultList = new ArrayList<>();
        if( null == FamilyMembers) return resultList;
        Map<String,Object> map;
        for(FamilyMemberPo member:FamilyMembers) {
        	map = new HashMap<>();
        	map.put("roleId", member.getRoleId());
        	map.put("roleName", member.getRoleName());
        	map.put("lev", member.getRoleLevel());
        	map.put("fight", member.getRoleFightScore());
        	map.put("jobName", member.getPostId());
        	map.put("goldContribute", member.getRmbDonation());
        	map.put("allContribute", member.getHistoricalContribution());
        	long tmp_time = member.getOfflineTimestamp();
        	if (member.isOnline())
        		tmp_time = ServiceUtil.now();
        	map.put("lastLoginTime", tmp_time);
        	resultList.add(map);
        }
        return resultList;
    }
}
