package com.stars.core.gmpacket;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.ServerLogConst;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zhouyaohui on 2016/12/22.
 */
public class WhiteListAddOrDelGm extends GmPacketHandler {

    @Override
    public String handle(HashMap args) {
        int result = 1;
        if (args.get("add")!=null) {
        	ArrayList<String> accounts = (ArrayList<String>)args.get("add");
            result = 0;
            for (int i = 0; i < accounts.size(); i++) {
                try {
                    WhiteListOpenOrCloseGm.addWhiteList(accounts.get(i));
                } catch (SQLException e) {
                    result = 1;
                    ServerLogConst.console.error("添加白名单错误", e);
                }
            }
        }
        if (args.get("del")!=null) {
            result = 0;
            ArrayList<String> accounts = (ArrayList<String>)args.get("del");
            for (int i = 0; i < accounts.size(); i++) {
                try {
                    WhiteListOpenOrCloseGm.delWhiteList(accounts.get(i));
                } catch (SQLException e) {
                    result = 1;
                    ServerLogConst.console.error("删除白名单错误", e);
                }
            }
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(result));
        return response.toString();
    }
}
