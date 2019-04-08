package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.message.KickOffMsg;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.Actor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 运维GM接口
 * 踢除单个/多个/全部玩家下线
 * Created by liuyuheng on 2016/12/9.
 */
public class KickOffPlayerGm extends GmPacketHandler {
    private static String ALL = "all";
    public static int KICK_OFF_STATUS = 0;//踢人进度 0踢人已结束  1踢人下线中
    @Override
    public String handle(HashMap args) {
        // todo:返回结果需要确认一下
        List<Long> resultList = new LinkedList<>();
        String param = (String)args.get("value");
        KICK_OFF_STATUS = 1;
        // 全部玩家
        if (ALL.equals(param)) {
            for (String roleId : PlayerSystem.system().getActors().keySet()) {
                try {
                    LogUtil.info("GM剔除玩家(ALL), roleId=" + roleId);
                    resultList.add(kickOffPlayer(Long.parseLong(roleId)));
                } catch (Exception e) {
                    LogUtil.error("剔除玩家出错, roleId=" + roleId);
                }
            }
        // 多个玩家
        } else if (param.contains(",")) {
            long[] roleIds = new long[0];
            try {
                roleIds = StringUtil.toArray(param, long[].class, ',');
            } catch (Exception e) {
                LogUtil.error("多个角色Id格式错误", e);
                KICK_OFF_STATUS = 0;
            }
            for (long roleId : roleIds) {
                resultList.add(kickOffPlayer(roleId));
            }
        // 单个玩家
        } else {
            long roleId = Long.parseLong(param);
            resultList.add(kickOffPlayer(roleId));
        }
        KICK_OFF_STATUS = 0;
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(0));
        return response.toString();
    }

    public static long kickOffPlayer(long roleId) {
        Player player = PlayerSystem.get(roleId);
        if (player == null){
            LogUtil.error("roleId={}",roleId);
            return -1;
        }
        LogUtil.info("Player={}, roleId={}", player, roleId);
        try {
            player.tell(new KickOffMsg(), Actor.noSender);
            return roleId;
        } catch (Throwable t) {
            LogUtil.error("剔除玩家失败throwabel, roleId=" + roleId, t);
            return -1;
        }
    }

}
