package com.stars.services.rank.imp;

import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
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
 * 镇妖塔排行榜;
 * Created by panzhenfeng on 2016/10/13.
 */
public class SkyTowerRank extends AbstractRank {
    private  int SkyTowrRankLayerSerialMinLimit = 1;
    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {
        List<AbstractRankPo> finalList = new LinkedList<>();
        RoleSummaryComponent roleSummary = null;
        //只加入镇妖塔层数大于1的人;最后加上自己的数据
        int size = list.size();
        for (int i = 0; i < size; i++) {
            RoleRankPo roleRankPo = (RoleRankPo) list.get(i);
            if (roleRankPo.getSkyTowerLayerSerial() > SkyTowrRankLayerSerialMinLimit || (i == (size - 1))) {
                finalList.add(roleRankPo);
                roleSummary = (RoleSummaryComponent)ServiceHelper.summaryService().getSummaryComponent(roleRankPo.getRoleId(), MConst.Role);
                if (StringUtil.isEmpty(roleRankPo.getRoleName()))
                    roleRankPo.setRoleName(roleSummary.getRoleName());
                if (roleRankPo.getRoleJobId() != roleSummary.getRoleJob())
                    roleRankPo.setRoleJobId(roleSummary.getRoleJob());
            }
        }
        // send to client
        ClientRank packet = new ClientRank(rankId);
        packet.setList(finalList);
        PlayerUtil.send(roleId, packet);
    }

    @Override
    protected void dailyReset(DbRowDao rankDao) {

    }
}
