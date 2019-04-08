package com.stars.services.rank.imp;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.rank.RankManager;
import com.stars.modules.rank.packet.ClientRank;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.RoleRankPo;
import com.stars.util.StringUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenkeyu on 2017/2/13 16:59
 */
public class RoleFamilyTreasureRank extends AbstractRank {
    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {
        List<AbstractRankPo> finalList = new LinkedList<>();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            RoleRankPo roleRankPo = (RoleRankPo) list.get(i);
            if ((roleRankPo.getAccDamage() > 0 || (i == (size - 1)))) {
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
        List<AbstractRankPo> rankPoList = new LinkedList<>();
        for (int i = 0; i < finalList.size() - 1 && i < RankManager.roleRankNum; i++) {
            rankPoList.add(finalList.get(i));
        }
        rankPoList.add(finalList.get(finalList.size() - 1));
        ClientRank packet = new ClientRank(rankId);
        packet.setList(rankPoList);
        PlayerUtil.send(roleId, packet);
    }

    @Override
    protected void removeCacheRank(long roleId) {
        super.removeCacheRank(roleId);
    }
}
