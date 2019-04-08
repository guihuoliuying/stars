package com.stars.modules.bestcp520;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.bestcp520.listenner.ActivityListenner;
import com.stars.modules.bestcp520.prodata.BestCP;
import com.stars.modules.bestcp520.prodata.BestCPRankReward;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.util.MapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/20.
 */
@DependOn({MConst.Data})
public class BestCPModuleFactory extends AbstractModuleFactory<BestCPModule> {
    public BestCPModuleFactory() {
        super(new BestCPPacketSet());
    }

    @Override
    public BestCPModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new BestCPModule("最佳组合520", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        String bestCPSql = "select * from bestcp;";
        Map<Integer, BestCP> bestCPMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "cpid", BestCP.class, bestCPSql);
        String cpRankRewardSql = "select * from bestcprankreward;";
        List<BestCPRankReward> bestCPRankRewards = DBUtil.queryList(DBUtil.DB_PRODUCT, BestCPRankReward.class, cpRankRewardSql);
        Map<Integer, List<BestCPRankReward>> cpRankRewardMap = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> rankRewardMap = new HashMap<>();
        Map<Integer, Integer> bestCPRankDisplayMap = new HashMap<>();

        for (BestCPRankReward bestCPRankReward : bestCPRankRewards) {
            Integer cpId = bestCPRankReward.getCpId();
            /**
             * 获取排名奖励产品数据
             */
            List<BestCPRankReward> tmpBestCPRankRewards = cpRankRewardMap.get(cpId);
            if (tmpBestCPRankRewards == null) {
                tmpBestCPRankRewards = new ArrayList<>();
                cpRankRewardMap.put(cpId, tmpBestCPRankRewards);
            }
            tmpBestCPRankRewards.add(bestCPRankReward);
            /**
             * 构建排名奖励map，以防止多次遍历
             */
            Map<Integer, Integer> rankMap = rankRewardMap.get(cpId);
            if (rankMap == null) {
                rankMap = new HashMap<>();
                rankRewardMap.put(cpId, rankMap);
            }
            for (Integer rank = bestCPRankReward.getMinRank(); rank <= bestCPRankReward.getMaxRank(); rank++) {
                rankMap.put(rank, bestCPRankReward.getReward());
            }
            /**
             *获取组合名次上限
             */
            String[] rankRange = bestCPRankReward.getRankRange().split("\\+");
            Integer displayNum = bestCPRankDisplayMap.get(cpId);
            int _displayNum = Integer.parseInt(rankRange[1]);
            if (displayNum == null) {
                bestCPRankDisplayMap.put(cpId, _displayNum);
                continue;
            }
            if (_displayNum > displayNum) {
                displayNum = _displayNum;
                bestCPRankDisplayMap.put(cpId, displayNum);
            }
        }

        String bestCPPersonalReward = DataManager.getCommConfig("bestcp_personalreward");
        String[] groupRewards = bestCPPersonalReward.split(",");
        Map<Integer, Integer> groupRewardMap = new HashMap<>();
        for (String groupReward : groupRewards) {
            String[] items = groupReward.split("\\+");
            groupRewardMap.put(Integer.parseInt(items[1]), Integer.parseInt(items[0]));
        }
        int bestCPTicketItemId = MapUtil.getInt(DataManager.commonConfigMap, "bestcp_ticketitem");
        BestCPManager.groupRewardMap = groupRewardMap;
        BestCPManager.bestCPMap = bestCPMap;
        BestCPManager.cpRankRewardMap = cpRankRewardMap;
        BestCPManager.rankRewardMap = rankRewardMap;
        BestCPManager.bestCPRankDisplayMap = bestCPRankDisplayMap;
        BestCPManager.bestCPTicketItemId = bestCPTicketItemId;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        ActivityListenner activityListenner = new ActivityListenner(module);
        eventDispatcher.reg(OperateActivityEvent.class, activityListenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, activityListenner);
        eventDispatcher.reg(ForeShowChangeEvent.class, activityListenner);
    }
}
