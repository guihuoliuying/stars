package com.stars.services.rank.imp;

import com.stars.core.player.PlayerUtil;
import com.stars.db.DBUtil;
import com.stars.modules.rank.packet.ClientRank;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyRankPo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/11/30.
 */
public class FamilyFightScoreRank extends AbstractRank {

    @Override
    protected void loadData() throws SQLException {
        List<FamilyRankPo> list = DBUtil.queryList(
                DBUtil.DB_USER, FamilyRankPo.class, "select * from `rankfamilyfightscore`");

        for (FamilyRankPo rankPo : list) {
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
