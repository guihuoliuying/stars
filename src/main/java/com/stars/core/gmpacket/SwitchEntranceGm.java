package com.stars.core.gmpacket;

import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.ServerLogConst;

import java.util.HashMap;

/**
 * Created by zhouyaohui on 2016/12/22.
 */
public class SwitchEntranceGm extends GmPacketHandler {
    public static final byte OPEN = 0;
    public static final byte CLOSE = 1;

    @Override
    public String handle(HashMap args) {
        int opt = Integer.valueOf((String)args.get("value"));
        int result = 1;
        if (opt == 0) {
            // 开入口
            result = 0;
            LoginModuleHelper.serverState = OPEN;
        }
        if (opt == 1) {
            // 关入口
            result = 0;
            LoginModuleHelper.serverState = CLOSE;
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(result));
        ServerLogConst.console.info("result|"+response.toString());
        return response.toString();
    }
}
