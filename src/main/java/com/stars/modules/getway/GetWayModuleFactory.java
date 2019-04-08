package com.stars.modules.getway;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.getway.prodata.GetWayVo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class GetWayModuleFactory extends AbstractModuleFactory<GetWayModule> {

    public GetWayModuleFactory() {
        super(new GetWayPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, GetWayVo> getWayVoMap = DBUtil.queryMap(
                DBUtil.DB_PRODUCT, "getwayid", GetWayVo.class, "select * from getway");

        GetWayManager.getWayVoMap = getWayVoMap;
    }

    @Override
    public GetWayModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new GetWayModule(id, self, eventDispatcher, map);
    }
}
