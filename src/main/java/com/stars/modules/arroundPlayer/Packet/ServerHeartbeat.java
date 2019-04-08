package com.stars.modules.arroundPlayer.Packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.arroundPlayer.ArroundPlayerPacketSet;
import com.stars.modules.role.RoleModule;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerHeartbeat extends PlayerPacket {
	
	private int pX;
	private int pY;
	private int pZ;
	private int sceneId;
	private String checkKey;
	private ClientHeartbeat clientHeartbeat = null;

	public ServerHeartbeat(){
		clientHeartbeat = new ClientHeartbeat();
	}

	@Override
	public void execPacket(Player player) {
		ArroundPlayerModule apm = (ArroundPlayerModule)module(MConst.ArroundPlayer);
		apm.heartbeat(sceneId, pX,pY,pZ);
		if (checkKey != null && !checkKey.equals("")) {
			((RoleModule) module(MConst.Role)).heartBeatCheckReq(checkKey);
		}
		PlayerUtil.send(getRoleId(), clientHeartbeat);
	}
	

	@Override
	public short getType() {
		return ArroundPlayerPacketSet.Server_Heartbeat;
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		pX = buff.readInt();
		pY = buff.readInt();
		pZ = buff.readInt();
		sceneId = buff.readInt();
		//是否检测心跳
		checkKey = buff.readString();
	}
}
