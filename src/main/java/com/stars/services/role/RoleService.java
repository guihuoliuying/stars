package com.stars.services.role;

import com.stars.core.event.Event;
import com.stars.network.server.packet.Packet;
import com.stars.services.Service;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/10/25.
 */
public interface RoleService extends Service {

    void notice(long roleId, RoleNotification notification);

    void notice(long roleId, Event event);

    void notice(List<Long> roleIdList, Event event);

    void noticeAll(Event event);

    void send(long roleId, Packet packet);

    /* 跨服rpc接口 */
    void notice(int serverId, long roleId, Event event);

    void send(int serverId, long roleId, Packet packet);

    void send(int serverId, List<Long> roleIdList, Packet packet);

    void warn(int serverId, long roleId, String text, String... params);

    void warn(int serverId, List<Long> roleIdList, String text, String... params);

    /**
     * 注意packet必须把session置为null
     * @param serverId
     * @param roleId
     * @param packet
     */
    void exec(int serverId, long roleId, Packet packet);

}
