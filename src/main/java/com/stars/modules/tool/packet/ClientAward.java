package com.stars.modules.tool.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.tool.ToolPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ClientAward extends PlayerPacket {

	private Map<Integer, Integer> awrd;
	private byte type = 0;

	public ClientAward(){

	}

	public ClientAward(Map<Integer, Integer> awrd){
		this.awrd = awrd;
	}

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public short getType() {
		return ToolPacketSet.C_TOOL_AWARD;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(type);
		byte size = (byte)awrd.size();
		buff.writeByte(size);
		if (size <= 0) {
			return;
		}
		Set<Entry<Integer, Integer>> set = awrd.entrySet();
		for (Entry<Integer, Integer> entry : set) {
			buff.writeInt(entry.getKey());
			buff.writeInt(entry.getValue());
		}
	}

	public Map<Integer, Integer> getAwrd() {
		return awrd;
	}

	public void setAwrd(Map<Integer, Integer> awrd) {
		this.awrd = awrd;
	}

	public void setAward(int itemid,int count){
		Map<Integer, Integer> map = new HashMap<>();
		map.put(itemid,count);
		this.awrd = map;
	}

	public void setType(byte type) {
		this.type = type;
	}
}
