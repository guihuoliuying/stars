package com.stars.modules.newredbag;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.newredbag.listener.FamilyAuthUpdateListener;
import com.stars.modules.newredbag.listener.LoginSuccessListener;
import com.stars.modules.newredbag.prodata.FamilyRedbagVo;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class NewRedbagModuleFactory extends AbstractModuleFactory {

    public NewRedbagModuleFactory() {
        super(new NewRedbagPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from familyredbag";
        ConcurrentMap<Integer, FamilyRedbagVo> map = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "redid", FamilyRedbagVo.class, sql);

        NewRedbagManager.setRedbagMap(map);
        NewRedbagManager.VALID_TIME = DataManager.getCommConfig("family_redtime", 600);
        NewRedbagManager.SELF_COUNT_MAX = DataManager.getCommConfig("family_freecount", 10);
        NewRedbagManager.RECORD_CLEAR_TIME = DataManager.getCommConfig("family_redtimedel", 48) * 60 * 60;
        NewRedbagManager.MAX_PADDING = DataManager.getCommConfig("family_redgoldmax", 300);
    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new NewRedbagModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(LoginSuccessEvent.class, new LoginSuccessListener(module));
        eventDispatcher.reg(FamilyAuthUpdatedEvent.class, new FamilyAuthUpdateListener(module));
    }
}
