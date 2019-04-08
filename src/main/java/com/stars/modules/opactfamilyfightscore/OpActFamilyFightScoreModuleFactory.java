package com.stars.modules.opactfamilyfightscore;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.services.opactfamilyfightscore.OpActFamilyFightScoreFlow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-21 10:26
 */
public class OpActFamilyFightScoreModuleFactory extends AbstractModuleFactory<OpActFamilyFightScoreModule> {
    public OpActFamilyFightScoreModuleFactory() {
        super(new OpActFamilyFightScorePacketSet());
    }

    @Override
    public OpActFamilyFightScoreModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OpActFamilyFightScoreModule("家族战力冲榜", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        initFlow();
    }

    private void initFlow() throws Exception {
        Map<Integer, String> flowMap = new HashMap<>();
        flowMap.put(OperateActivityConstant.FLOW_STEP_NEW_DAY, "0 0 0 * * ?");
        OpActFamilyFightScoreFlow flow = new OpActFamilyFightScoreFlow();
        flow.init(SchedulerHelper.getScheduler(), flowMap);
    }
}
