package com.stars.modules.loottreasure.packet;

import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *游戏服请求参加跨服夺宝后反馈，也是个双向包
 *
 */
public class AttendLootTreasureBack extends Packet {
	//0正常
	private byte flag;
	
	private long roleId;
	
	public AttendLootTreasureBack(){
		
	}
	public AttendLootTreasureBack(byte flag){
		this.flag = flag;
	}
	@Override
	public short getType() {
		return LootTreasurePacketSet.ATTEND_LOOTTREASURE_BACK;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(flag);
		buff.writeLong(roleId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		flag = buff.readByte();
		roleId = buff.readLong();
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}
	public byte getFlag() {
		return flag;
	}
	public void setFlag(byte flag) {
		this.flag = flag;
	}
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

}
