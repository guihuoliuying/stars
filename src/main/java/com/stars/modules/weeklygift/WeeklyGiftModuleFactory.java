package com.stars.modules.weeklygift;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.modules.weeklygift.listener.WeeklyGiftOpenListener;
import com.stars.modules.weeklygift.prodata.WeeklyGiftVo;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class WeeklyGiftModuleFactory extends AbstractModuleFactory<WeeklyGiftModule> {
    public WeeklyGiftModuleFactory() {
        super(new WeeklyGiftPacketSet());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(ForeShowChangeEvent.class, new WeeklyGiftOpenListener((WeeklyGiftModule) module));
        eventDispatcher.reg(VipChargeEvent.class, new WeeklyGiftOpenListener((WeeklyGiftModule) module));
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from weeklygift";
        Map<Integer, WeeklyGiftVo> weeklyGiftVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "weeklygiftid", WeeklyGiftVo.class, sql);
        WeeklyGiftManager.setWeeklyGiftVoMap(weeklyGiftVoMap);
    }

    @Override
    public WeeklyGiftModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new WeeklyGiftModule(MConst.WeeklyGift, id, self, eventDispatcher, map);
    }
}
