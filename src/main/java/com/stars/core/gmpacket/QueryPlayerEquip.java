package com.stars.core.gmpacket;

import com.stars.modules.data.DataManager;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.newequipment.summary.NewEquipmentSummaryComponentImpl;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
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
 * GM装备信息查询
 */
public class QueryPlayerEquip extends GmPacketHandler {
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
    		List<Map> resultList = null;    		
    		SummaryComponent EquipSummary = ServiceHelper.summaryService().getSummaryComponent(roleId, SummaryConst.C_NEW_EQUIPMENT);
    		resultList = getResultMap(EquipSummary);
    		response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList));
    	} catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
    private List<Map> getResultMap(SummaryComponent Summary) {
    	List<Map> resultList = new ArrayList<>();
        if(Summary == null) return resultList;        
        NewEquipmentSummaryComponentImpl EquipSummary = (NewEquipmentSummaryComponentImpl)Summary; 
        Map<Byte,RoleEquipment> EquipMap = EquipSummary.getEquipmentMap();
        Map<String,Object> map;
        for(Map.Entry<Byte,RoleEquipment> entry:EquipMap.entrySet()){
        	map = new HashMap<>();
        	ItemVo itemVo = ToolManager.getItemVo(entry.getValue().getEquipId());
        	if (null == itemVo)
        		continue;
        	EquipmentVo equipVo = NewEquipmentManager.getEquipmentVo(entry.getValue().getEquipId());
        	if (null == equipVo)
        		continue;
        	map.put("itemId",entry.getValue().getEquipId());
        	String toolName = DataManager.getGametext(itemVo.getName());
        	map.put("itemName",toolName);
        	map.put("uniCode",equipVo.getType());
        	String str = "starlevel=" + entry.getValue().getStarLevel() + "|strengthlevel=" + 
        	         entry.getValue().getStrengthLevel() + "|extraattrstr=" + entry.getValue().getExtraAttrStr();
        	map.put("attrs",str);
        	resultList.add(map);
        }
        return resultList;
    }
}
