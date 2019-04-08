package com.stars.modules.mind.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.mind.MindPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;


/**
 * 客户端请求升级心法(升级也包括激活，即0级到1级)
 * Created by gaopeidian on 2016/9/24.
 */
public class ClientUpgradeMind extends PlayerPacket {
    private int mindId;
    private int mindLevel;
    private byte state;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MindPacketSet.C_UPGRADE_MIND;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(mindId);
    	buff.writeInt(mindLevel);
    	buff.writeByte(state);
    }
    
    public void setMindId(int mindId){
    	this.mindId = mindId;
    }
    
    public void setMindLevel(int mindLevel){
    	this.mindLevel = mindLevel;
    }
    
    public void setState(byte state){
    	this.state = state;
    }
}