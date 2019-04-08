package com.stars.multiserver.teamPVPGame.stepIns;

import com.stars.modules.data.DataManager;
import com.stars.modules.teampvpgame.TeamPVPGameManager;
import com.stars.modules.teampvpgame.prodata.DoublePVPConfigVo;
import com.stars.multiserver.teamPVPGame.TPGHost;
import com.stars.multiserver.teamPVPGame.TPGTeam;
import com.stars.multiserver.teamPVPGame.TPGUtil;
import com.stars.util.DateUtil;

import java.util.Collection;

public class QuarterTPGStep extends GroupTPGStep {
	@Override
	public void init0(TPGHost tpgFlow, Collection<TPGTeam> teams) {
		initGroup(1, teams);
	}
	@Override
	public void initConfig() throws Exception {
		this.awardTypeId =
				tHost.getTpgType() == TPGUtil.TPG_LOACAL ? TPGUtil.AWARD_LOCAL_QUARTER : TPGUtil.AWARD_REMOTE_QUARTER;
		DoublePVPConfigVo configVo = TeamPVPGameManager.getConfigVo(tHost.getTpgType());
		if (configVo == null)
			return;
		//本服四强赛的开启时间段, 格式为: 周几+hh:mm:ss+hh:mm:ss
		//四强赛的初始预备时间+单场间隔时间+轮次间隔时间, 单位秒
		initConfig(configVo.getFinalFourBattleOpen(), DataManager.getCommConfig("doublepvp_finalfourbattle_timeset"));
		fightScenceId = Integer.parseInt(DataManager.getCommConfig("doublepvp_finfourbattle_stageid"));
		//单场战斗时间
		this.lastTimeOfFight = configVo.getFinalFourBattleTime() * DateUtil.SECOND;
		// 本服/跨服使用提示不同
		String announceNotice = DataManager.getGametext("doublepvp_finalfourbattle_servertips");
		String chatNotice = DataManager.getGametext("doublepvp_finalfourbattle_servermessage");
		if (tHost.getTpgType() == TPGUtil.TPG_REMOTE) {
			announceNotice = DataManager.getGametext("doublepvp_finalfourbattle_servertips_cross");
			chatNotice = DataManager.getGametext("doublepvp_finalfourbattle_servermessage_cross");
		}
		initNoticeConfig(DataManager.getCommConfig("doublepvp_teambattletips_time"), announceNotice, chatNotice);
		initTask(2);
	}

}
