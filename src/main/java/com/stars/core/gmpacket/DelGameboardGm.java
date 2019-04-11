package com.stars.core.gmpacket;

import com.stars.core.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by chenkeyu on 2017/1/6 11:39
 */
public class DelGameboardGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        if (args.containsKey("noticeId")) {
            try {
                delGameboard(Integer.parseInt(args.get("noticeId").toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        return response.toString();
    }

    private void delGameboard(int boardId) {
        try {
            DBUtil.execSql(DBUtil.DB_USER, "delete from gameboard where boardid = '" + boardId + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
