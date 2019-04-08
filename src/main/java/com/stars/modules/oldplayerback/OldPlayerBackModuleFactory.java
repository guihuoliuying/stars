package com.stars.modules.oldplayerback;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.oldplayerback.pojo.ActOldPlayerBackDate;
import com.stars.modules.oldplayerback.pojo.AllRoleLimitConf;
import com.stars.modules.oldplayerback.pojo.RewardPosition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/7/13.
 */
public class OldPlayerBackModuleFactory extends AbstractModuleFactory<OldPlayerBackModule> {
    public OldPlayerBackModuleFactory() {
        super(new OldPalyerBackPacketSet());
    }

    @Override
    public OldPlayerBackModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OldPlayerBackModule("老玩家回归", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        OldPlayerBackManager.ComebackAllRoleLimitConf = AllRoleLimitConf.parse(DataManager.getCommConfig("comeback_reward_limit1"));
        OldPlayerBackManager.ComebackLoginRoleimitLevel = DataManager.getCommConfig("comeback_reward_limit2", 0);
        OldPlayerBackManager.ComebackRewardActTimeStr=DataManager.getCommConfig("comeback_reward_time");
        OldPlayerBackManager.ComebackRewardActTime = ActOldPlayerBackDate.parse(DataManager.getCommConfig("comeback_reward_time"));
        String comebackRewardDrop = DataManager.getCommConfig("comeback_reward_drop");
        String[] group = comebackRewardDrop.split("\\|");
        Map<Integer, RewardPosition> rewardPositionMap = new LinkedHashMap<>();
        for (String item : group) {
            RewardPosition rewardPosition = RewardPosition.parse(item);
            rewardPositionMap.put(rewardPosition.getPosition(), rewardPosition);
        }
        OldPlayerBackManager.dayReward = rewardPositionMap;
    }
}
