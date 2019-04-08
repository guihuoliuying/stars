package com.stars.modules.raffle.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.raffle.RafflePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 
 * @author likang by 2017/4/22
 * 
 */

public class ClientRaffleDo extends PlayerPacket {
	private static final long serialVersionUID = -9118260734198062126L;

	private int position;
	private int totalMoney;

	@Override
	public void execPacket(Player player) {

	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(position);
		buff.writeInt(totalMoney);
	}

	@Override
	public short getType() {
		return RafflePacketSet.C_RaffleDo;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(int totalMoney) {
		this.totalMoney = totalMoney;
	}

}
