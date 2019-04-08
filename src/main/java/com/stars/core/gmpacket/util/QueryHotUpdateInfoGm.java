package com.stars.core.gmpacket.util;

import com.yinhan.hotupdate.HotUpdateManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.HashMap;

/**
 * Created by wuyuxing on 2017/3/31.
 */
public class QueryHotUpdateInfoGm extends GmPacketHandler {

    @Override
    public String handle(HashMap args) {
        StringBuffer sb = new StringBuffer();
        sb.append("已热更的类文件:").append(HotUpdateManager.getUpdateClassBuff().toString());
        sb.append("|上次热更成功的类文件：").append(HotUpdateManager.getLastUpdateSucClass());
        sb.append("|上次热更失败的类文件：").append(HotUpdateManager.getLastUpdateFailClass());
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(sb.toString()));
        return response.toString();
    }
}
