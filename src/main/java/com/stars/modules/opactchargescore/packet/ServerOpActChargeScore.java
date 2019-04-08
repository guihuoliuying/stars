package com.stars.modules.opactchargescore.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.opactchargescore.OpActChargeScorePacketSet;
import com.stars.services.ServiceHelper;

/**
 * Created by likang
 */
public class ServerOpActChargeScore extends PlayerPacket {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7923479876313921013L;

	@Override
	public void execPacket(Player player) {
		ServiceHelper.opActChargeScore().view(player.id());
	}

	@Override
	public short getType() {
		return OpActChargeScorePacketSet.S_OpActChargeScore;
	}
}
