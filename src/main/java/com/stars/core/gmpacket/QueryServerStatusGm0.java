package com.stars.core.gmpacket;

import com.stars.ServerVersion;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这个是运营平台的
 * Created by chenkeyu on 2017/1/16 18:55
 */
public class QueryServerStatusGm0 extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(results()));
        return response.toString();
    }

    private List<Map> results() {
        List<Map> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("serverId", MultiServerHelper.getServerId());
        map.put("serverName", MultiServerHelper.getServerName());
        map.put("onlineTotal", PlayerSystem.system().getActors().size());
        map.put("limitTotal", PlayerSystem.getActorCount());
        map.put("version", ServerVersion.getBigVersion() + "." + ServerVersion.getSmallVersion());
        map.put("type", LoginModuleHelper.serverState == SwitchEntranceGm.OPEN?2:1);
        list.add(map);
        return list;
    }
}
