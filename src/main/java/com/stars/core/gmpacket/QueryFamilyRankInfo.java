package com.stars.core.gmpacket;


import com.stars.core.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.family.main.userdata.FamilyPo;

import java.util.HashMap;


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
        }
    	return response.toString();
    }
    
}
