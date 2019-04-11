package com.stars.core.gmpacket;

import com.stars.core.SystemRecordMap;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SetServerOpenTImeGM extends GmPacketHandler{

	@Override
	public String handle(HashMap args) {
		 if (args.get("value")==null) return new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("")).toString();
		long ymd = parseYMD(args.get("value").toString());
		SystemRecordMap.update("openServerTime", ymd);
		long time = SystemRecordMap.openServerTime;
		List resultList = new ArrayList<>();
		resultList.add(parseYMD(time));
		GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(resultList));
		//等级加速条件更新处理（开服时间修改）
		return response.toString();
	}

	private long parseYMD(String timeStr) {
		long time = Long.parseLong(timeStr.replace("-", ""));
		return time * 1000000;
	}

	private String parseYMD(long time) {
		String timeStr = (time / 1000000) + "";
		StringBuilder sb = new StringBuilder();
		sb.append(timeStr.substring(0, 4)).append("-").append(timeStr.substring(4, 6)).append("-").append(timeStr.substring(6, 8));
		return sb.toString();
	}
}
