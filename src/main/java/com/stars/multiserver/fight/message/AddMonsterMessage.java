package com.stars.multiserver.fight.message;

import com.stars.modules.pk.packet.ClientUpdatePlayer;

/**
 * Created by zhaowenshuo on 2016/12/9.
 */
public class AddMonsterMessage {

    private ClientUpdatePlayer packet;


    public AddMonsterMessage(ClientUpdatePlayer packet) {
        this.packet = packet;
    }

    public ClientUpdatePlayer getPacket() {
        return packet;
    }
}
