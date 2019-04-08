package com.stars.modules.opactbenefittoken;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.opactbenefittoken.gm.OpActBenefitTokenGmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class OpActBenefitTokenModuleFactory extends AbstractModuleFactory<OpActBenefitTokenModule> {

    public OpActBenefitTokenModuleFactory() {
        super(new OpActBenefitTokenPacketSet());
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("opActBenefitToken", new OpActBenefitTokenGmHandler());
    }

    @Override
    public void loadProductData() throws Exception {
        OpActBenefitTokenManager.timesLimit = DataManager.getCommConfig("opActBenefitToken.timesLimit", 2);
    }

    @Override
    public OpActBenefitTokenModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OpActBenefitTokenModule(id, self, eventDispatcher, map);
    }
}
