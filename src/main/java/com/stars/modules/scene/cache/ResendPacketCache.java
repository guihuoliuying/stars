package com.stars.modules.scene.cache;

import com.stars.core.player.PlayerPacket;

/**
 * Created by liuyuheng on 2017/1/17.
 */
public class ResendPacketCache {
    private long timestamp;
    private PlayerPacket packet;

    public ResendPacketCache(long timestamp, PlayerPacket packet) {
        this.timestamp = timestamp;
        this.packet = packet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public PlayerPacket getPacket() {
        return packet;
    }

    public void setPacket(PlayerPacket packet) {
        this.packet = packet;
    }
}
