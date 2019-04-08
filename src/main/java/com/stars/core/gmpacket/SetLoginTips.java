package com.stars.core.gmpacket;

import com.stars.modules.demologin.LoginManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.HashMap;

public class SetLoginTips extends GmPacketHandler{

	@Override
	public String handle(HashMap paramHashMap) {
		int statu = GmPacketResponse.SUC;
		int result = 0;
		try {
			String tips = (String) paramHashMap.get("value");
			LoginManager.loginTips = tips;			
		} catch (Exception e) {
			statu = GmPacketResponse.TIMEOUT;
			result = 1;
		}		
		GmPacketResponse response = new GmPacketResponse(statu, 1, resultToJson(result));
	    return response.toString();
	}

}
