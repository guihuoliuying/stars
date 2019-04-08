package com.stars.core.gmpacket;


import com.stars.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.family.main.userdata.FamilyPo;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyRankPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhoujin on 2017/3/23.
 * GM家族排行榜信息查询
 */
public class QueryFamilyRankInfo extends GmPacketHandler {
    @Override   
    public String handle(HashMap args) {
    	GmPacketResponse response = null;
    	long teamId = 0;
    	String teamName = "";
    	String tmp_ids = (String) args.get("teamId");
    	if ( null != tmp_ids)
    	    teamId = Long.parseLong(tmp_ids);
    	String tmp_names = (String) args.get("teamName");
    	if (null != tmp_names)
    	    teamName = tmp_names;
    	if (teamId == 0 && ("".equals(teamName))) {
    		// 家族id和家族名都没有输入，输出家族排行前20名
    		
    			List<AbstractRankPo> RankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_FAMILYFIGHTSCORE,20);
    			List<Map> resultList = getResultMap(RankPoList);
        		response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList));
    		
    	} else {
    		// 取具体家族排名
    		if (teamId == 0) {
    			 String QuerySql = "select * from family where name =" + " '" + teamName + "'";
    			 try {
    				 FamilyPo familyPo = DBUtil.queryBean(DBUtil.DB_USER, FamilyPo.class, QuerySql);
    				 if (null != familyPo)
    				     teamId = familyPo.getFamilyId(); 			 
    			 }catch (Exception e) {
    				 e.printStackTrace();
    				 response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
    				 return response.toString();
    	         }			
    		}
    		if (teamId == 0) {
    			response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
    			return response.toString();
    		}
    		AbstractRankPo RankPo = ServiceHelper.rankService().getRank(RankConstant.RANKID_FAMILYFIGHTSCORE, teamId);
			if (null == RankPo) {
				 response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
				 return response.toString();
			}else {
				 List<AbstractRankPo> RankPoList = new ArrayList<>();
				 RankPoList.add(RankPo);
				 List<Map> resultList = getResultMap(RankPoList);
	        	 response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList));
			}  
    	}  	
    	return response.toString();
    }
    
    private List<Map> getResultMap(List<AbstractRankPo> RankPoList) {
    	List<Map> resultList = new ArrayList<>();
        if( null == RankPoList) return resultList;
        Map<String,Object> map;
        for (AbstractRankPo RankPo : RankPoList) {
        	FamilyRankPo FamilyRankPoInfo = (FamilyRankPo)RankPo;
        	map = new HashMap<>();
        	map.put("rank", FamilyRankPoInfo.getRank());
        	map.put("teamId", FamilyRankPoInfo.getFamilyId());
        	map.put("teamName", FamilyRankPoInfo.getName());
        	map.put("leaderName", FamilyRankPoInfo.getMasterName());
        	map.put("lev", FamilyRankPoInfo.getLevel());
        	map.put("fight", FamilyRankPoInfo.getFightScore());
        	resultList.add(map);
        }
        return resultList;
    }
}
