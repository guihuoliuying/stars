package com.stars.modules.name.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.name.NamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientRandomName extends PlayerPacket {

	private String name;
	public ClientRandomName(){
	}
	
	public ClientRandomName(String name){
		this.name = name;
	}
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub

	}
	@Override
    public void writeToBuffer(NewByteBuffer buff) {
		buff.writeString(name);
    }
	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return NamePacketSet.Client_Random_Name;
	}

}
