package com.stars.services.rank.imp;

import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.rank.packet.ClientRank;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.OpActChargeRankPo;

import java.sql.SQLException;
import java.util.List;

/**
 * create by likang on 2017/4/13
 */

public class OpActChargeRank extends AbstractRank {

	@Override
	protected void loadData() throws SQLException {
		super.loadData();
		List<OpActChargeRankPo> list = DBUtil.queryList(DBUtil.DB_USER, OpActChargeRankPo.class,
				"select * from `opactchargerank`");
		for (OpActChargeRankPo rankPo : list) {
			rankPo.setRankId(rankId);
			addRankTreeSet(null, rankPo);
		}
	}

	@Override
	protected void sendRankList(long roleId, List<AbstractRankPo> list) {
		ClientRank packet = new ClientRank(rankId);
		packet.setList(list);
		PlayerUtil.send(roleId, packet);

	}

}
