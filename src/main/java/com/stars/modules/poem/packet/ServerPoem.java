package com.stars.modules.poem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.poem.PoemModule;
import com.stars.modules.poem.PoemPacketSet;

/**
 * Created by gaopeidian on 2017/1/9.
 */
public class ServerPoem  extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
    	PoemModule poemModule = (PoemModule)module(MConst.Poem);
    	poemModule.sendAllPoemData();
    }

    @Override
    public short getType() {
        return PoemPacketSet.S_POEM;
    }  
}
