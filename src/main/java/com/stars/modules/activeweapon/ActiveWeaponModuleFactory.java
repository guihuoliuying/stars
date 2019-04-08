package com.stars.modules.activeweapon;

import com.google.common.collect.Maps;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.activeweapon.listenner.ActiveWeaponListenner;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.scene.event.PassStageEvent;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ActiveWeaponModuleFactory extends AbstractModuleFactory<ActiveWeaponModule> {

    public ActiveWeaponModuleFactory() {
        super(new ActiveWeaponPacketSet());
    }

    @Override
    public ActiveWeaponModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ActiveWeaponModule("活跃神兵", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from activeweapon;";
        Map<Integer, ActiveWeaponVo> activeWeaponVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", ActiveWeaponVo.class, sql);
        ActiveWeaponManager.activeWeaponVoMap = activeWeaponVoMap;
        String activeweaponItemshow = DataManager.getCommConfig("activeweapon_itemshow");
        String[] conditionShows = activeweaponItemshow.split("\\|");
        Map<Integer, Map<Integer, Integer>> showItemMap = Maps.newHashMap();
        for (String conditionShwo : conditionShows) {
            String[] items = conditionShwo.split("\\+");
            Map<Integer, Integer> itemShowMap = Maps.newHashMap();
            itemShowMap.put(Integer.parseInt(items[1]), Integer.parseInt(items[2]));
            showItemMap.put(Integer.parseInt(items[0]), itemShowMap);
        }
        ActiveWeaponManager.showItemMap = showItemMap;
        ActiveWeaponManager.activeweaponItemshow = activeweaponItemshow;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        ActiveWeaponListenner activeWeaponListenner = new ActiveWeaponListenner((ActiveWeaponModule) module);
        eventDispatcher.reg(PassStageEvent.class, activeWeaponListenner);
    }
}
