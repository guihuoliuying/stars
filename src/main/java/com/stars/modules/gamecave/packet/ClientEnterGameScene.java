package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientEnterGameScene extends PlayerPacket {
	private int safeId;
	private String postionStr;
	
    public ClientEnterGameScene() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_ENTER_GAME_CAVE_SCENE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(safeId);
        buff.writeString(postionStr);
    }
    
    public void setSafeId(int value){
    	this.safeId = value;
    }
    
    public void setPostionStr(String postionStr) {
        this.postionStr = postionStr;
    }
}
