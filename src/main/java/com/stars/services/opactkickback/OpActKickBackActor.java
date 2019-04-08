package com.stars.services.opactkickback;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.opactkickback.packet.ClientOpActKickBack;
import com.stars.modules.opactkickback.userdata.RoleConsumeInfo;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.invocation.ServiceActor;

public class OpActKickBackActor extends ServiceActor implements OpActKickBack {

	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.OpenActKickBack, this);
	}

	@Override
	public void printState() {

	}

	@Override
	public void view(long roleId, RoleConsumeInfo info) {
		ClientOpActKickBack msg = new ClientOpActKickBack();
		msg.setConsume(info.getConsume());
		msg.setHashGet(info.getSendAwardList());
		PlayerUtil.send(roleId, msg);
	}
}
