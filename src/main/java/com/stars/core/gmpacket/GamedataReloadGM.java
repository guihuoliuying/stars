package com.stars.core.gmpacket;

import com.yinhan.AfterUpdate;
import com.stars.core.module.ModuleManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.ServerLogConst;

import java.util.HashMap;

public class GamedataReloadGM extends GmPacketHandler {

    @Override
    public String handle(HashMap paramHashMap) {
        String[] tables = new String[10];
        String param = (String) paramHashMap.get("value");
        String result = "";
        int statu = GmPacketResponse.SUC;
        if (param.contains("&")) {
            tables = param.split("&");
        } else {
            tables[0] = param;
        }
        ServerLogConst.console.info("gamedataReload begin table size=" + tables.length);
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < tables.length; i++) {
                if (tables[i] == null) {
                    continue;
                }
                if (tables[i].equalsIgnoreCase("all")) {
                    ModuleManager.loadProductData();
                    sb.append(tables[i]).append(",");
                    ServerLogConst.console.info(tables[i] + " had reload");
                } else {
                    ModuleManager.loadProductData(tables[i]);
                    sb.append(tables[i]).append(",");
                    ServerLogConst.console.info(tables[i] + " had reload");
                }
                result = tables[i] + "reload scuess";
            }
            AfterUpdate.writeToFile(sb.toString());
        } catch (Exception e) {
            ServerLogConst.exception.info(e.getMessage(), e);
            statu = GmPacketResponse.TIMEOUT;
            result = param + " reload fail";
        }

        GmPacketResponse response = new GmPacketResponse(statu, 1, resultToJson(result));
        return response.toString();
    }

}
