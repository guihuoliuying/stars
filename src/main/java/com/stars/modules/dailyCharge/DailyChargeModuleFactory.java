package com.stars.modules.dailyCharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.dailyCharge.listener.ChargeListenr;
import com.stars.modules.dailyCharge.prodata.DailyChargeInfo;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class DailyChargeModuleFactory extends AbstractModuleFactory<DailyChargeModule> {
    public DailyChargeModuleFactory() {
        super(new DailyChargePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from dailytotalcharge";
        Map<Integer, DailyChargeInfo> dailyChargeInfoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT,"dailytotalid",DailyChargeInfo.class,sql);
        DailyChargeManager.DailyChargeInfoMap = dailyChargeInfoMap;
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public DailyChargeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new DailyChargeModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        ChargeListenr listener = new ChargeListenr((DailyChargeModule)module);
        eventDispatcher.reg(VipChargeEvent.class, listener);
    }
}
