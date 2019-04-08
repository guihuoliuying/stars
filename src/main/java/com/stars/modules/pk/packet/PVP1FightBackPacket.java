package com.stars.modules.pk.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.pk.PKPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * @author dengzhou
 *
 *游戏服和战斗服的双向包
 *游戏服通知战斗服发起一场战斗后，战斗服返回给游戏服的数据
 *
 */
public class PVP1FightBackPacket extends PlayerPacket {
	
	private int actorId;
	
	private long inviter;
	
	private long invitee;
	
	
	public PVP1FightBackPacket(){
		
	}
	
	public PVP1FightBackPacket(int actorId){
		this.actorId = actorId;
	}

	@Override
	public short getType() {
		return PKPacketSet.F2SPVPBACK;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeInt(actorId);
		buff.writeLong(inviter);
		buff.writeLong(invitee);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		actorId = buff.readInt();
		inviter = buff.readLong();
		invitee = buff.readLong();
	}

	@Override
	public void execPacket(Player p) {
		
	}

	public int getActorId() {
		return actorId;
	}

	public void setActorId(int actorId) {
		this.actorId = actorId;
	}

	public long getInviter() {
		return inviter;
	}

	public void setInviter(long inviter) {
		this.inviter = inviter;
	}

	public long getInvitee() {
		return invitee;
	}

	public void setInvitee(long invitee) {
		this.invitee = invitee;
	}

}
