package com.stars.modules.role.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.RolePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerHeartBeatCheck extends PlayerPacket {
	
	private String key;
	
	public ServerHeartBeatCheck(){}

	@Override
	public void execPacket(Player player) {
		((RoleModule)module(MConst.Role)).heartBeatCheckReq(key);
	}

	@Override
	public short getType() {
		return RolePacketSet.S_HEARTBEAT_CHECK;
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		key = buff.readString();
	}

}
