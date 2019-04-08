package com.stars.core.gmpacket;

import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhoujin on 2017/3/22.
 * GM包裹信息查询
 */
public class QueryPlayerBag extends GmPacketHandler {
    @Override   
    public String handle(HashMap args) {
    	GmPacketResponse response = null;
    	try {
    		long roleId = 0;
    		int serverId = 0;
    		roleId = Long.parseLong((String) args.get("roleId"));
            serverId = Integer.parseInt((String) args.get("serverId"));
            if (roleId == 0 || serverId == 0) {
            	response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            	return response.toString();
            }
            int type = Integer.parseInt((String) args.get("type"));
            String sql = new StringBuffer().append("select * from roletool").append(roleId % 10).append(" where roleid=").append(roleId).toString();
            List<RoleToolRow> ls = DBUtil.queryList(DBUtil.DB_USER, RoleToolRow.class, sql);
            List<Map> resultList = getResultMap(ls,type);
            response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList));
    	}catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
    
    private List<Map> getResultMap(List<RoleToolRow> list, int type) {
    	List<Map> resultList = new ArrayList<>();
        if(list == null) return resultList;        
        Map<String,Object> map;
        for(RoleToolRow tool : list) {
            if(tool == null) continue;
            if (type != 0) {
            	int bagType = ToolManager.getBagType(tool.getItemId());
            	if (bagType != type)
            		continue;
            }
            ItemVo itemVo = ToolManager.getItemVo(tool.getItemId());
            if (null == itemVo)
            	continue;
            map = new HashMap<>();
            map.put("itemId", tool.getItemId());
            String toolName = DataManager.getGametext(itemVo.getName());
            map.put("itemName", toolName);
            map.put("uniCode", tool.getToolId());
            map.put("amount", tool.getCount());
            map.put("others", tool.getExtraAttrStr());
            resultList.add(map);           
        }
        return resultList;
    }
}
