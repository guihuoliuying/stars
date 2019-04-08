package com.stars.modules.raffle.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.raffle.RaffleModule;
import com.stars.modules.raffle.RafflePacketSet;

/**
 * 
 * @author likang by 2017/4/22
 * 
 */

public class ServerRaffleGetInfo extends PlayerPacket {

	private static final long serialVersionUID = 962898794286558628L;

	@Override
	public void execPacket(Player player) {
		RaffleModule raffleModule = module(MConst.Raffle);
		if (raffleModule == null) {
			return;
		}
		raffleModule.getInfo();
	}

	@Override
	public short getType() {
		return RafflePacketSet.S_RaffleGetInfo;
	}

}
