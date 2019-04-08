package com.stars.modules.newdailycharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.newdailycharge.listener.NewDailyChargeListener;
import com.stars.modules.newdailycharge.prodata.NewDailyChargeInfo;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.actloopreset.event.ActLoopResetEvent;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class NewDailyChargeModuleFactory extends AbstractModuleFactory {
    public NewDailyChargeModuleFactory() {
        super(new NewDailyChargePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from newdailytotalcharge";
        Map<Integer, NewDailyChargeInfo> infoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "newdailytotalid", NewDailyChargeInfo.class, sql);
        NewDailyChargeManager.setNewDailyChargeInfoMap(infoMap);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new NewDailyChargeModule(MConst.NewDailyCharge, id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        NewDailyChargeListener newDailyChargeListener = new NewDailyChargeListener(module);
        eventDispatcher.reg(VipChargeEvent.class, newDailyChargeListener);
        eventDispatcher.reg(ActLoopResetEvent.class, newDailyChargeListener);
    }
}
