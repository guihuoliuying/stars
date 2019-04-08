/**
 * 
 */
package com.stars.modules.chat.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.chat.ChatPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengzhou
 *
 */
public class ServerRefuseChannel extends PlayerPacket {
	
	byte type;
	
	Map<Byte,Byte>map;
	
	public ServerRefuseChannel(){
		
	}

	@Override
	public void execPacket(Player player) {
		ChatModule cm = (ChatModule)module(MConst.Chat);
		if (type == 0) {
			cm.sendRefuseChannel2Client();
			return;
		}
		cm.refuseChannel(map);
	}

	@Override
	public short getType() {
		return ChatPacketSet.Server_RefuseChannel;
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		type = buff.readByte();
		if (type == 0) {
			return;
		}
		map = new HashMap<Byte, Byte>();
		byte size = buff.readByte();
		for (int i = 0; i < size; i++) {
			map.put(buff.readByte(), buff.readByte());
		}
	}
	
}
