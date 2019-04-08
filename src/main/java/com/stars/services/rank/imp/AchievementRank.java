package com.stars.services.rank.imp;

import com.stars.core.db.DBUtil;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.AchievementRankPo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by zhanghaizhen on 2017/8/9.
 */
public class AchievementRank extends AbstractRank {
    @Override
    protected void loadData() throws SQLException {
        List<AchievementRankPo> list = DBUtil.queryList(DBUtil.DB_USER, AchievementRankPo.class,
                "select * from `roleachieverank`");
        for (AchievementRankPo rankPo : list) {
            rankPo.setRankId(rankId);
            addRankTreeSet(null, rankPo);
        }
    }

    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {

    }
}
