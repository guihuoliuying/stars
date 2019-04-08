package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.gem.GemModule;
import com.stars.modules.gem.GemPacketSet;

/**
 * Created by panzhenfeng on 2017/1/11.
 */
public class ServerRoleGemInfo  extends PlayerPacket {

    public ServerRoleGemInfo() {

    }

    @Override
    public void execPacket(Player player) {
        GemModule gemModule = (GemModule) this.module(MConst.GEM);
        if(gemModule != null){
            gemModule.syncGemInfo();
        }
    }

    @Override
    public short getType() {
        return GemPacketSet.S_ROLE_GEM_INFO;
    }
}
