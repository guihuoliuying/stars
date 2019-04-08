package com.stars.services.rank.imp;

import com.stars.core.db.DBUtil;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.BestCPRankPo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCpRank extends AbstractRank {
    @Override
    protected void loadData() throws SQLException {
        String sql = "select * from rankbestcp;";
        List<BestCPRankPo> bestCPRankPos = DBUtil.queryList(DBUtil.DB_USER, BestCPRankPo.class, sql);
        for (BestCPRankPo bestCPRankPo : bestCPRankPos) {
            bestCPRankPo.setRankId(rankId);
            addTreeSet(null, bestCPRankPo);
        }
    }

    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {

    }

}
