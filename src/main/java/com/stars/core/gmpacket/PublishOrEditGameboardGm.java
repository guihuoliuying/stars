package com.stars.core.gmpacket;

import com.stars.core.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.TimeUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenkeyu on 2017/1/6 10:07
 */
public class PublishOrEditGameboardGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        List<String> lack = new ArrayList<>();
        boolean isPublish = true;
        if (args.containsKey("noticeId")) {
            isPublish = false;
        }
        if (args.containsKey("title") && args.containsKey("content") && args.containsKey("startTime") && args.containsKey("endTime")) {
            try {
                if (isPublish) {
                    doPublish(args);
                } else {
                    doEdit(args);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (!args.containsKey("title"))
                lack.add("title");
            if (!args.containsKey("content"))
                lack.add("content");
            if (!args.containsKey("startTime"))
                lack.add("startTime");
            if (!args.containsKey("endTime"))
                lack.add("endTime");
            if (!lack.isEmpty()) {
                lack.add(0, "Error:lack of args,");
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
        String title = ((String) args.get("title")).replaceAll("\\^p", "\\\n");
        String content = ((String) args.get("content")).replaceAll("\\^p", "\\\n");
        String startTime = TimeUtil.toDateString(Long.parseLong(args.get("startTime") + "000"));
        String endTime = TimeUtil.toDateString(Long.parseLong(args.get("endTime") + "000"));
        String channel = "0";
        if (args.containsKey("channelIds")) {
            channel = (String) args.get("channelIds");
        }
        DBUtil.execSql(DBUtil.DB_USER,
                "insert into gameboard (title,text,date,plateform) values('" + title + "','" + content + "','" + startTime +
                        "&" + endTime + "','" + channel + "')");
    }

    /**
     * 编辑公告
     *
     * @param args
     * @throws SQLException
     */
    private void doEdit(HashMap args) throws SQLException {
        int boardId = Integer.parseInt(args.get("noticeId").toString());
        String title = ((String) args.get("title")).replaceAll("\\^p", "\\\n");
        String content = ((String) args.get("content")).replaceAll("\\^p", "\\\n");
        String startTime = TimeUtil.toDateString(Long.parseLong(args.get("startTime") + "000"));
        String endTime = TimeUtil.toDateString(Long.parseLong(args.get("endTime") + "000"));
        String channel = "0";
        if (args.containsKey("channelIds")) {
            channel = (String) args.get("channelIds");
        }
        DBUtil.execSql(DBUtil.DB_USER,
                "update gameboard set title = '" + title + "', text = '" + content + "', date = '" + startTime + "&"
                        + endTime + "', plateform = '" + channel + "' where boardid =" + boardId + "");
    }
}
