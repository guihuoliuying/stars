package com.stars.core.gmpacket;


import com.stars.modules.MConst;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.RoleRankPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhoujin on 2017/3/24.
 * GM玩家排行榜信息查询
 */
public class QueryPlayerRankInfo extends GmPacketHandler {
    @Override   
    public String handle(HashMap args) {
    	GmPacketResponse response = null;
    	int type = 0;  	
    	type = Integer.parseInt((String) args.get("type"));
    	Integer RankType = 0;
    	
    	RankType = RankConstant.gmRankTypeMap.get((byte)type);
    	
    	
    	if (null == RankType || 0 == RankType) {
    		response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
    		return response.toString();
    	}   	
        List<AbstractRankPo> RankPoList = ServiceHelper.rankService().getFrontRank(RankType,50);
        List<Map> resultlist = getResultMap(RankPoList,RankType);
        response = new GmPacketResponse(GmPacketResponse.SUC, resultlist.size(), resultToJson(resultlist)); 	
    	return response.toString();
    }
    
    private List<Map> getResultMap(List<AbstractRankPo> RankPoList,int RankType) {
    	List<Map> resultList = new ArrayList<>();
        if( null == RankPoList) return resultList;
        Map<String,Object> map;
        for (AbstractRankPo Po : RankPoList) {
        	RoleRankPo RoleRankInfo = (RoleRankPo)Po;
        	map = new HashMap<>();
        	map.put("rank",RoleRankInfo.getRank());
        	map.put("roleId",RoleRankInfo.getRoleId());
        	//map.put("roleName",RoleRankInfo.getRoleName());
        	int compareValue = 0;
        	switch (RankType) {
        	case RankConstant.RANKID_FIGHTSCORE:
        		compareValue = RoleRankInfo.getRoleLevel();
        	    break;
        	case RankConstant.RANKID_ROLELEVEL:
        	case RankConstant.RANKID_TOTAL_ROLELEVEL:
        		compareValue = RoleRankInfo.getRoleLevel();
        	    break;
        	default:
        		break;
        	}
        	map.put("levOrPoint",compareValue);
        	map.put("fight",RoleRankInfo.getFightScore());
        	String RoleName = RoleRankInfo.getRoleName();
        	if (null == RoleName || "".equals(RoleName)) {
        		RoleSummaryComponent roleSummary = (RoleSummaryComponent)
                        ServiceHelper.summaryService().getSummaryComponent(RoleRankInfo.getRoleId(), MConst.Role);
        		RoleName = roleSummary.getRoleName();
        	}
        	map.put("roleName",RoleName);
        	resultList.add(map);
        }
        return resultList;
    }
}
