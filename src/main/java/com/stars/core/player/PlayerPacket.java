package com.stars.core.player;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.Module;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Map;

/**
 * Created by zws on 2015/11/30.
 */
public abstract class PlayerPacket extends Packet {

    private Player player;

    public final void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void execPacket() {
        // No-op
    }

    protected final Map<String, Module> moduleMap() {
        return player.moduleMap();
    }

    protected final EventDispatcher eventDispatcher() {
        return player.eventDispatcher();
    }

    protected final <T extends Module> T module(String moduleName) {
        return (T) moduleMap().get(moduleName);
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    public abstract void execPacket(Player player);

    /**
     * 子协议使用 注入player&execute
     *
     * @param packet
     * @param player
     */
    public static void setAndExec(PlayerPacket packet, Player player) {
        packet.setPlayer(player);
        packet.execPacket(player);
    }
}
