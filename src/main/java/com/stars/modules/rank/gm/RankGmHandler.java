package com.stars.modules.rank.gm;

import com.stars.core.module.Module;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.rank.RankManager;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.rank.RankConstant;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-02-26 20:37
 */
public class RankGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        int rankId = Integer.parseInt(args[0]);
        if (rankId != 106 && rankId != 107) {
            com.stars.network.server.packet.PacketManager.send(roleId, new ClientText("请输入正确的排行榜Id!!!"));
            return;
        }
        int maxRank = Integer.parseInt(args[1]);
        switch (rankId) {
            case RankConstant.RANKID_ROLEFAMILYTREASURE:
                if (maxRank > 0) {
                    RankManager.roleRankNum = maxRank;
                } else {
                    RankManager.roleRankNum = Integer.MAX_VALUE;
                }
                break;
            case RankConstant.RANKID_FAMILYTREASURE:
                if (maxRank > 0) {
                    RankManager.familyRankNum = maxRank;
                } else {
                    RankManager.familyRankNum = Integer.MAX_VALUE;
                }
                break;
        }
        PacketManager.send(roleId, new ClientText("设置成功!!!"));
    }
}
