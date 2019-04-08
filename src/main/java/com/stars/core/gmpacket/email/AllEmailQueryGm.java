package com.stars.core.gmpacket.email;

import com.google.gson.Gson;
import com.stars.core.gmpacket.email.vo.AllEmailGmPo;
import com.stars.core.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全服邮件Gm 20040
 * Created by huwenjun on 2017/3/24.
 */
public class AllEmailQueryGm extends GmPacketHandler {

    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        String taskIdStr = (String) args.get("taskId");
        String serverIdStr = (String) args.get("serverId");
        Long taskId = null;

        int serverId = Integer.parseInt(serverIdStr);
        String sql;
        List<AllEmailGmPo> allEmailGmPos = new ArrayList<>();
        List<Map> allEmailGmPosMap = new ArrayList<>();
        try {
            if (taskIdStr != null) {
                taskId = Long.parseLong(taskIdStr);
                sql = "select * from allemailgm where taskid=" + taskId + " and serverid=" + serverId;
                AllEmailGmPo allEmailGmPo = DBUtil.queryBean(DBUtil.DB_USER, AllEmailGmPo.class, sql);
                if (allEmailGmPo != null) {
                    allEmailGmPos.add(allEmailGmPo);
                }
            } else {
                sql = "select * from allemailgm where  serverid=" + serverId;
                allEmailGmPos = DBUtil.queryList(DBUtil.DB_USER, AllEmailGmPo.class, sql);
            }
            String result;
            if (allEmailGmPos.size() == 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("serverId:");
                stringBuilder.append(serverIdStr);
                stringBuilder.append("服务器下不存在");
                if (taskId != null) {
                    stringBuilder.append("taskId：");
                    stringBuilder.append(taskId);
                    stringBuilder.append("的");
                }
                stringBuilder.append("全服邮件任务");
                result = stringBuilder.toString();
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(result));
            } else {
                for (AllEmailGmPo allEmailGmPo : allEmailGmPos) {
                    Map<String, Object> target = new HashMap();
                    target.put("serverId", allEmailGmPo.getServerId());
                    target.put("taskId", allEmailGmPo.getTaskId());
                    target.put("title", allEmailGmPo.getTitle());
                    target.put("content", allEmailGmPo.getText());
                    target.put("itemDict", new Gson().fromJson(allEmailGmPo.getItemDict(), List.class));
                    target.put("coolTime", allEmailGmPo.getCoolTime());
                    target.put("expireTime", allEmailGmPo.getExpireTime());
                    target.put("status", allEmailGmPo.getStatus());
                    target.put("conditionList", allEmailGmPo.getCondition());
                    target.put("channelIds", allEmailGmPo.getChannelIds());
                    allEmailGmPosMap.add(target);
                }
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(allEmailGmPosMap));
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(e.getMessage()));
        } finally {
            return response.toString();
        }
    }
}
