package com.stars.modules.runeDungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.runeDungeon.RuneDungeonManager;
import com.stars.modules.runeDungeon.RuneDungeonModule;
import com.stars.modules.runeDungeon.RuneDungeonPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerRuneDungeon extends PlayerPacket {
	
	private byte opType;
	
	private byte playType;
	
	private int dungeonId;
	
	private long friendId;

	@Override
	public void execPacket(Player player) {
		RuneDungeonModule module = module(MConst.RuneDungeon);
		module.execHandle(this);
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		opType = buff.readByte();
		if(opType==RuneDungeonManager.REQ_UI_INFO){
			playType = buff.readByte();
		}else if(opType==RuneDungeonManager.SELECT_UPDATE_MAIN_UI){
			playType = buff.readByte();
			dungeonId = buff.readInt();
		}else if(opType==RuneDungeonManager.REQ_SELECT_UI_INFO){
			playType = buff.readByte();
		}else if(opType==RuneDungeonManager.REQ_START_FIGHT){
			playType = buff.readByte();
			dungeonId = buff.readInt();
			String friendIdStr = buff.readString();
			friendId = Long.valueOf(friendIdStr);
		}else if(opType==RuneDungeonManager.RESET_DUNGEON){
			dungeonId = buff.readInt();
		}
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return RuneDungeonPacketSet.Server_RuneDungeon;
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public byte getPlayType() {
		return playType;
	}

	public void setPlayType(byte playType) {
		this.playType = playType;
	}

	public int getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(int dungeonId) {
		this.dungeonId = dungeonId;
	}

	public long getFriendId() {
		return friendId;
	}

	public void setFriendId(long friendId) {
		this.friendId = friendId;
	}

}
