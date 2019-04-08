package com.stars.modules.opactfightscore;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.services.opactfightscore.OpActFightScoreFlow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-21 10:41
 */
public class OpActFightScoreModuleFactory extends AbstractModuleFactory<OpActFightScoreModule> {
    public OpActFightScoreModuleFactory() {
        super(new OpActFightScorePacketSet());
    }

    @Override
    public OpActFightScoreModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OpActFightScoreModule("个人战力冲榜", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        initFlow();
    }

    private void initFlow() throws Exception {
        Map<Integer, String> flowMap = new HashMap<>();
        flowMap.put(OperateActivityConstant.FLOW_STEP_NEW_DAY, "0 0 0 * * ?");
        OpActFightScoreFlow flow = new OpActFightScoreFlow();
        flow.init(SchedulerHelper.getScheduler(), flowMap);
    }
}
