package com.stars.modules.elitedungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.elitedungeon.EliteDungeonModule;
import com.stars.modules.elitedungeon.EliteDungeonPacketSet;

/**
 * Created by gaopeidian on 2017/4/11.
 */
public class ServerEliteData extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
    	EliteDungeonModule eliteDungeonModule = module(MConst.EliteDungeon);
    	eliteDungeonModule.sendEliteData();
    }

    @Override
    public short getType() {
        return EliteDungeonPacketSet.Server_EliteData;
    }
}
