package com.stars.modules.gameboard.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.gameboard.GameboardModule;
import com.stars.modules.gameboard.GameboardPacketSet;

/**
 * Created by chenkeyu on 2017/1/5 20:14
 */
public class ServerGameboard extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        GameboardModule module = module(MConst.Gameboard);
        module.popGameboard();
    }

    @Override
    public short getType() {
        return GameboardPacketSet.S_GAMEBOARD;
    }
}
