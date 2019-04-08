package com.stars.modules.opactkickback.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.opactkickback.OpActKcikBackModule;
import com.stars.modules.opactkickback.OpActKickBackPacketSet;

public class ServerOpActKickBack extends PlayerPacket {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7362785600183013767L;

	@Override
	public void execPacket(Player player) {
		OpActKcikBackModule kickBackModel = module(MConst.OpActKickBack);
		if (kickBackModel == null) {
			return;
		}
		kickBackModel.view();
	}

	@Override
	public short getType() {
		return OpActKickBackPacketSet.S_OpActKickBack;
	}

}
