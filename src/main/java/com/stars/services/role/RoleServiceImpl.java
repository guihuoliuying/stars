package com.stars.services.role;

import com.stars.core.event.Event;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/7/22.
 */
public class RoleServiceImpl implements RoleService {

    private ConcurrentMap<Long, Long> onlineRoleSet = new ConcurrentHashMap<>();

    @Override
    public void init() throws Throwable {

    }

    public void notice(long roleId, RoleNotification notification) {
        Player player = PlayerSystem.get(roleId);
        if (player != null) {
            try {
                GameSession session = SessionManager.getSessionMap().get(roleId);
                notification.setSession(session);
                player.tell(notification, Actor.noSender);
            } catch (Throwable t) { // 消息已满，Actor停止的异常都会吞掉
                LogUtil.error("", t);
            }
        }
    }

    public void notice(long roleId, Event event) {
        Player player = PlayerSystem.get(roleId);
        if (player != null) {
            try {
                RoleNotification notification = new RoleNotification(event);
                GameSession session = SessionManager.getSessionMap().get(roleId);
                notification.setSession(session);
                player.tell(notification, Actor.noSender);
            } catch (Throwable t) { // 消息已满，Actor停止的异常都会吞掉
                LogUtil.error("", t);
            }
        }
    }

    @Override
    public void notice(List<Long> roleIdList, Event event) {
        for (Long roleId : roleIdList) {
            Player player = PlayerSystem.get(roleId);
            if (player != null) {
                try {
                    RoleNotification notification = new RoleNotification(event);
                    GameSession session = SessionManager.getSessionMap().get(roleId);
                    notification.setSession(session);
                    player.tell(notification, Actor.noSender);
                } catch (Throwable t) { // 消息已满，Actor停止的异常都会吞掉
                    LogUtil.error("", t);
                }
            }
        }
    }

    public void noticeAll(Event event) {
        for (Actor actor : PlayerSystem.system().getActors().values()) {
            Player player = (Player) actor;
            try {
                RoleNotification notification = new RoleNotification(event);
                GameSession session = SessionManager.getSessionMap().get(player.id());
                notification.setSession(session);
                player.tell(notification, Actor.noSender);
            } catch (Throwable t) {
                LogUtil.error("", t);
            }
        }
    }

    @Override
    public void send(long roleId, Packet packet) {
        PlayerUtil.send(roleId, packet);
    }

    /* 跨服接口 */
    @Override
    public void notice(int serverId, long roleId, Event event) {
        notice(roleId, event);
    }

    @Override
    public void send(int serverId, long roleId, Packet packet) {
        send(roleId, packet);
    }

    @Override
    public void send(int serverId, List<Long> roleIdList, Packet packet) {
        for (long roleId : roleIdList) {
            send(roleId, packet);
        }
    }

    @Override
    public void warn(int serverId, long roleId, String text, String... params) {
        send(roleId, new ClientText(text, params));
    }

    @Override
    public void warn(int serverId, List<Long> roleIdList, String text, String... params) {
        ClientText packet = new ClientText(text, params);
        for (long roleId : roleIdList) {
            send(roleId, packet);
        }
    }

    @Override
    public void exec(int serverId, long roleId, Packet packet) {
        GameSession session = SessionManager.getSessionMap().get(roleId);
        if (session == null) {
            LogUtil.info("session is null, roleId={}, packetType={}", roleId, packet.getType());
        } else {
            packet.setSession(session);
        }
        packet.setRoleId(roleId);
        Player player = PlayerSystem.get(roleId);
        if (player != null) {
            player.tell(packet, Actor.noSender);
        } else {
            LogUtil.info("no player, roleId={}, packetType={}", roleId, packet.getType());
        }
    }
}
