package com.stars.services.rank.imp;

import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.rank.RankManager;
import com.stars.modules.rank.packet.ClientRank;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyTreasureRankPo;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenkeyu on 2017/2/13 16:59
 */
public class FamilyTreasureRank extends AbstractRank {

    @Override
    protected void loadData() throws SQLException {
        List<FamilyTreasureRankPo> list = DBUtil.queryList(
                DBUtil.DB_USER, FamilyTreasureRankPo.class, "select * from `rankfamilytreasure`");
        for (FamilyTreasureRankPo rankPo : list) {
            rankPo.setRankId(rankId);
            addRankTreeSet(null, rankPo);
        }
    }

    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {
        List<AbstractRankPo> finalList = new LinkedList<>();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            FamilyTreasureRankPo rankPo = (FamilyTreasureRankPo) list.get(i);
            if ((rankPo.getLevel() != 0 && rankPo.getStep() != 0) || (i == (size - 1))) {
                finalList.add(rankPo);
            }
        }

        List<AbstractRankPo> rankPoList = new LinkedList<>();
        for (int i = 0; i < finalList.size() - 1 && i < RankManager.familyRankNum; i++) {
            rankPoList.add(finalList.get(i));
        }
        if (finalList.size() != 0) {
            rankPoList.add(finalList.get(finalList.size() - 1));
        }

        ClientRank packet = new ClientRank(rankId);
        packet.setList(rankPoList);
        PlayerUtil.send(roleId, packet);
    }

    @Override
    protected void removeCacheRank(long familyId) {
        super.removeCacheRank(familyId);
    }
}
