package com.stars.modules.chargegift;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.chargegift.listener.ChargeGiftListener;
import com.stars.modules.data.DataManager;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.actloopreset.event.ActLoopResetEvent;

import java.util.Map;

/**
 * Created by chenxie on 2017/5/18.
 */
public class ChargeGiftModuleFactory extends AbstractModuleFactory<ChargeGiftModule> {

    public ChargeGiftModuleFactory() {
        super(new ChargeGiftPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        ChargeGiftManager.CHARGE_GIFT_DROP_ID = DataManager.getCommConfig("chargegift_dropid", 0);
        ChargeGiftManager.CHARGE_GIFT_MAX_GIFT = DataManager.getCommConfig("chargegift_maxgift", 0);
        ChargeGiftManager.CHARGE_GIFT_MIN_CHARGE = DataManager.getCommConfig("chargegift_mincharge", 0);
    }

    @Override
    public ChargeGiftModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ChargeGiftModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        ChargeGiftListener listener = new ChargeGiftListener((ChargeGiftModule)module);
        eventDispatcher.reg(VipChargeEvent.class, listener);
        eventDispatcher.reg(ActLoopResetEvent.class, listener);
    }
}
