package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

public class ClientEnterRuneDungeon extends ClientEnterDungeon{
	
	private int buffId;
	
	private int addNum;//增加层数
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		super.writeToBuffer(buff);
		buff.writeInt(buffId);
		buff.writeInt(addNum);
	}

	public int getBuffId() {
		return buffId;
	}

	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}

	public int getAddNum() {
		return addNum;
	}

	public void setAddNum(int addNum) {
		this.addNum = addNum;
	}
	
}
