package com.stars.modules.pk.packet;

import com.stars.modules.pk.PKPacketSet;
import com.stars.multiserver.fight.FightActor;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.main.actor.ActorServer;

/**
 * @author dengzhou
 *
 *客户端发送到战斗服的战斗数据
 *data为客户端包装好的战斗数据
 */
public class ServerPVPData extends Packet {

	private byte[] data;

	public ServerPVPData(){
		
	}
	
	@Override
	public short getType() {
		return PKPacketSet.Server_PVP_Data;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		int len = buff.readInt();
		data = buff.readBytes(len);
	}

	@Override
	public void execPacket() {
//		FightActor actor = (FightActor)ActorServer.getActorSystem().getActor(this.session.getActorId());
		String fightId = RoleId2ActorIdManager.getFightId(getRoleId());
		FightActor actor = null;
		if (fightId != null) {
			actor = (FightActor) com.stars.server.main.actor.ActorServer.getActorSystem().getActor(fightId);
		} else {
			actor = (FightActor) ActorServer.getActorSystem().getActor(this.session.getActorId());
		}
//		FightActor actor = (FightActor) ActorServer.getActorSystem().getActor(fightId);
		if (actor != null) {
			actor.setClientOrders(data);
		}
	}

}
