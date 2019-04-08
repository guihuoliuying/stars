package com.stars.core.gmpacket;

import com.stars.ServerVersion;
import com.stars.bootstrap.ServerManager;
import com.stars.core.SystemRecordMap;
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
 * 这个是给运维的
 * Created by chenkeyu on 2016/12/19.
 */
public class QueryServerStatusGm extends GmPacketHandler {
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
        map.put("entrance", LoginModuleHelper.serverState == SwitchEntranceGm.OPEN ? "true" : "false");
        map.put("type", ServerManager.getServer().getConfig().getServerType());
        map.put("online", PlayerSystem.system().getActors().size());
        map.put("version", ServerVersion.getBigVersion() + "." + ServerVersion.getSmallVersion());
        String dateStr = String.valueOf(SystemRecordMap.openServerTime / 1000000);
        int year = Integer.parseInt(dateStr.substring(0, 4));
        int month = Integer.parseInt(dateStr.substring(4, 6));
        int day = Integer.parseInt(dateStr.substring(6, 8));
        map.put("openDate", year + "-" + month + "-" + day);
        map.put("limitTotal", PlayerSystem.getActorCount());
        map.put("whiteListEntrance", WhiteListOpenOrCloseGm.isOpen() ? "true" : "false");
        list.add(map);
        return list;
    }
}
