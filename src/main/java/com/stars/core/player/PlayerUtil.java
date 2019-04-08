package com.stars.core.player;

import com.stars.network.server.packet.Packet;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2017/2/25.
 */
public class PlayerUtil {

    public static void send(long roleId, Packet packet) {
        Player player = PlayerSystem.get(roleId);
        if (player != null) {
            player.send(packet);
        } else {
            LogUtil.info("发包异常|找不到玩家|roleId:{},packetType:{}", roleId, String.format("0x%04X", packet.getType()));
        }
    }

}
