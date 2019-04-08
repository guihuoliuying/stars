package com.stars.core.gmpacket;

import com.stars.core.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.TimeUtil;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by chenkeyu on 2017/1/6 10:07
 */
public class PublishOrEditLoginGameboardGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        if (args.containsKey("title") && args.containsKey("content") && args.containsKey("startTime") && args.containsKey("endTime")) {
            try {
                doPublish(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        return response.toString();
    }

    /**
     * 发布公告
     *
     * @param args
     * @throws SQLException
     */
    private void doPublish(HashMap args) throws SQLException {
        DBUtil.execSql(DBUtil.DB_LOGIN, "delete from gameboard");
        String title = ((String) args.get("title")).replaceAll("\\^p", "\\\n");
        String content = ((String) args.get("content")).replaceAll("\\^p", "\\\n");
        String startTime = TimeUtil.toDateString(Long.parseLong(args.get("startTime") + "000"));
        String endTime = TimeUtil.toDateString(Long.parseLong(args.get("endTime") + "000"));
        String channel = "0";
        if (args.containsKey("channelIds")) {
            channel = (String) args.get("channelIds");
        }
        DBUtil.execSql(DBUtil.DB_LOGIN,
                "insert into gameboard (title,text,date,plateform) values('" + title + "','" + content + "','" +
                        startTime + "&" + endTime + "','" + channel + "')");
    }
}
