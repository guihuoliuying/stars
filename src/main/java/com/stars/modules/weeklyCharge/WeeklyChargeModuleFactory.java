package com.stars.modules.weeklyCharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.modules.weeklyCharge.listener.WeeklyChargeListenr;
import com.stars.modules.weeklyCharge.prodata.WeeklyChargeVo;
import com.stars.services.actloopreset.event.ActLoopResetEvent;

import java.util.Map;

/**
 * Created by chenxie on 2017/5/5.
 */
public class WeeklyChargeModuleFactory extends AbstractModuleFactory<WeeklyChargeModule> {

    public WeeklyChargeModuleFactory() {
        super(new WeeklyChargePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from weeklytotalcharge";
        Map<Integer, WeeklyChargeVo> weeklyChargeInfoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT,"weeklytotalid",WeeklyChargeVo.class,sql);
        WeeklyChargeManager.weeklyChargeInfoMap = weeklyChargeInfoMap;
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public WeeklyChargeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new WeeklyChargeModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        WeeklyChargeListenr listener = new WeeklyChargeListenr((WeeklyChargeModule)module);
        eventDispatcher.reg(VipChargeEvent.class, listener);
        eventDispatcher.reg(ActLoopResetEvent.class, listener);
    }
}
