package com.stars.modules.raffle.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.raffle.RaffleModule;
import com.stars.modules.raffle.RafflePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 
 * @author likang by 2017/4/22
 * 
 */

public class ServerRaffleDo extends PlayerPacket {
	private static final long serialVersionUID = -5665124613517016130L;
	private static final byte RAFFLE_ONE_TIME = 1;
	private static final byte RAFFLE_TEN_TIME = 2;
	private byte requestType;

	@Override
	public void execPacket(Player player) {
		RaffleModule raffleModule = module(MConst.Raffle);
		if (raffleModule == null) {
			return;
		}
		switch (requestType){
			case RAFFLE_ONE_TIME:
				raffleModule.doRaffle();
				break;
			case RAFFLE_TEN_TIME:
				raffleModule.doTenRaffle();
				break;
			default:
				break;
		}

	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		requestType = buff.readByte();
	}

	@Override
	public short getType() {
		return RafflePacketSet.S_RaffleDo;
	}

}
