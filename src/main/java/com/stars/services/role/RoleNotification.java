package com.stars.services.role;

import com.stars.core.event.Event;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.services.Notification;

/**
 * Created by zhaowenshuo on 2016/7/16.
 */
public class RoleNotification extends PlayerPacket implements Notification {

    private Event event;

    public RoleNotification(Event event) {
        this.event = event;
    }

    @Override
    public void execPacket(Player player) {
        eventDispatcher().fire(event);
    }

    @Override
    public short getType() {
        return 0; //
    }
}
