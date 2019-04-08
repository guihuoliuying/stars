package com.stars.services;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.packet.Packet;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/9/1.
 */
public class ServiceUtil {

    public static void sendText(long roleId, String text, String... params) {
        if (roleId > 0) {
            PlayerUtil.send(roleId, new ClientText(text, params));
        }
    }

    /**
     * 下发消息给所有在线玩家
     *
     * @param packet
     * @param exceptRoleIds 不推送的角色Id
     */
//	public static void sendPacketToOnline(Packet packet, long... exceptRoleIds){
//	    for (Actor actor : PlayerSystem.system().getActors().values()) {
//            try {
//                if (actor instanceof Player && !Arrays.asList(exceptRoleIds).contains(((Player) actor).id())) {
//                    PlayerUtil.send(((Player) actor).id(), packet);
//                }
//            } catch (Throwable cause) {
//                LogUtil.error("ServiceUtil.sendPacketToOnline send packet error!", cause);
//            }
//        }
//	}
    
    /**
     * 下发消息给所有在线玩家
     *
     * @param packet
     * @param exceptRoleIds 不推送的角色Id
     */
	public static void sendPacketToOnline(Packet packet, Set<Long> exceptRoleIds){
	    for (Actor actor : PlayerSystem.system().getActors().values()) {
            try {
                if (actor instanceof Player && ((exceptRoleIds == null) || (!exceptRoleIds.contains(((Player) actor).id())))) {
                    PlayerUtil.send(((Player) actor).id(), packet);
                }
            } catch (Throwable cause) {
                LogUtil.error("ServiceUtil.sendPacketToOnline send packet error!", cause);
            }
        }
	}
    
    public static int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

}
