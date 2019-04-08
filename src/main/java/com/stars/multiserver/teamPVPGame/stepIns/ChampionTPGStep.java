package com.stars.multiserver.teamPVPGame.stepIns;

import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.teamPVPGame.TPGFightScence;
import com.stars.multiserver.teamPVPGame.TPGHost;
import com.stars.multiserver.teamPVPGame.TPGTeam;

import java.util.Collection;

public class ChampionTPGStep extends AbstractTPGStep {

	@Override
	public void init0(TPGHost tHost, Collection<TPGTeam> teams) {
		if (teams.size() != 1) {
			throw new RuntimeException("冠军列表不等于1");
		}
		for (TPGTeam tpgTeam : teams) {
			tpgTeam.setStep(this.tHost.getStep());
			tpgTeam.setUpdateStatus();
			insertDBList.add(tpgTeam);
		}
	}

	@Override
	public void initFromDB0(TPGHost tHost) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initConfig() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void doLuaFram(String fightScence, LuaFrameData luaFrameData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doOffLine(long roleId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFightScenceEnd(TPGFightScence scence, TPGTeam winner, TPGTeam loser, Object... params) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean offline(long memberId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enterFight(long initiator) {

	}

	@Override
	public void onReceived(Object message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void maintenance() {
		// TODO Auto-generated method stub

	}

}
