package com.stars.core.gmpacket;

import com.stars.db.DBUtil;
import com.stars.modules.gameboard.prodata.GameboardVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/1/6 11:39
 */
public class QueryLoginGamboardGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        List<GameboardVo> gameboardVos = new ArrayList<>();
        try {
            gameboardVos = queryGameboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, gameboardVos.size(), resultToJson(results(gameboardVos)));
        return response.toString();
    }

    private List<GameboardVo> queryGameboard() throws SQLException {
        return DBUtil.queryList(DBUtil.DB_LOGIN, GameboardVo.class, "select * from gameboard");
    }

    private List<Map> results(List<GameboardVo> gameboardVos) {
        List<Map> list = new ArrayList<>();
        Map<String, Object> map;
        for (GameboardVo vo : gameboardVos) {
            if (vo == null) continue;
            map = new HashMap<>();
            map.put("noticeId", vo.getBoardid());
            map.put("serverId", MultiServerHelper.getServerId());
            map.put("title", vo.getTitle());
            map.put("content", vo.getText());
            map.put("startTime", vo.getStartDateGm());
            map.put("endTime", vo.getEndDateGm());
            map.put("channelIds", vo.getPlateform());
            list.add(map);
        }
        return list;
    }

}
