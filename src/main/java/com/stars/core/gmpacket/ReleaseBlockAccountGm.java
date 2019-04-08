package com.stars.core.gmpacket;

import com.stars.db.DBUtil;
import com.stars.modules.demologin.userdata.BlockAccount;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.ServerLogConst;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by liuyuheng on 2017/1/16.
 */
public class ReleaseBlockAccountGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        int serverId = 0;
        String account = null;
        if (args.containsKey("serverId")) {
            serverId = Integer.parseInt((String) args.get("serverId"));
        }
        if (args.containsKey("blockAccount")) {
            account = (String) args.get("blockAccount");
        }
        BlockAccount blockAccount = BlockAccountGm.getBlockAccount(account);
        if (blockAccount == null) {
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            return response.toString();
        }
        blockAccount.setDeleteStatus();
        try {
            DBUtil.execSql(DBUtil.DB_USER, blockAccount.getDeleteSql());
            BlockAccountGm.removeBlockAccount(account);
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (SQLException e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
            ServerLogConst.console.error("解封账号错误", e);
        } finally {
            return response.toString();
        }
    }
}
