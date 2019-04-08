package com.stars.services.rank.imp;

import com.stars.core.dao.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.rank.packet.ClientRank;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.RoleRankPo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/9/19.
 */
public class GamecaveScoreRank extends AbstractRank {
    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {
    	List<AbstractRankPo> finalList = new LinkedList<>();
    	//只加入洞府积分大于0的人;最后加上自己的数据
    	int size = list.size();
    	for (int i = 0; i < size; i++) {
    		RoleRankPo roleRankPo = (RoleRankPo) list.get(i);
			if (roleRankPo.getGamecaveScore() > 0 || (i == (size - 1))) {
				finalList.add(roleRankPo);
			}
		}
    	
        // 从常用数据拿name,level数据
        for (AbstractRankPo rankPo : finalList) {
			RoleRankPo roleRankPo = (RoleRankPo) rankPo;
            RoleSummaryComponent roleSummary = (RoleSummaryComponent)
                    ServiceHelper.summaryService().getSummaryComponent(roleRankPo.getRoleId(), MConst.Role);
            if (StringUtil.isEmpty(roleRankPo.getRoleName()))
                roleRankPo.setRoleName(roleSummary.getRoleName());
            if (roleRankPo.getRoleJobId() != roleSummary.getRoleJob())
                roleRankPo.setRoleJobId(roleSummary.getRoleJob());
        }
        // send to client
        ClientRank packet = new ClientRank(rankId);
        packet.setList(finalList);

		PlayerUtil.send(roleId, packet);
    }

    @Override
    protected void dailyReset(DbRowDao rankDao) {
    	//更新数据库中的所有数据
        List<RoleRankPo> list = null;
		try {
			list = DBUtil.queryList(DBUtil.DB_USER, RoleRankPo.class, "select * from `allrank`; ");
		} catch (SQLException e) {
			LogUtil.info("GamecaveScoreRank.dailyReset get RoleRankPo list from DB_USER exception:" , e);
			e.printStackTrace();
		}
		if (list != null) {
			for (RoleRankPo roleRankPo : list) {        	
	        	if (roleRankPo.getGamecaveScore() != 0) {
	    			roleRankPo.setGamecaveScore(0);
	        		rankDao.update(roleRankPo);
				}
	        }
		}
    
    	//更新内存中的数据
        Iterator<AbstractRankPo> iterator = treeSet.iterator();
        while (iterator.hasNext()) {
            RoleRankPo roleRankPo = (RoleRankPo) iterator.next();
            if (roleRankPo.getGamecaveScore() != 0) {
    			roleRankPo.setGamecaveScore(0);
        		rankDao.update(roleRankPo);
			}
        }
    }
}
