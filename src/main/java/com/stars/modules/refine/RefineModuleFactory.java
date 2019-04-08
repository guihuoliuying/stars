package com.stars.modules.refine;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.refine.listener.AddOrDelToolListener;
import com.stars.modules.refine.prodata.RefineVo;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class RefineModuleFactory extends AbstractModuleFactory {
    public RefineModuleFactory() {
        super(new RefinePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, RefineVo> refineVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "itemid", RefineVo.class, "select * from refine");
        RefineManager.refineVoMap = refineVoMap;
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new RefineModule(MConst.Refine, id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(UseToolEvent.class, new AddOrDelToolListener(module));
        eventDispatcher.reg(AddToolEvent.class, new AddOrDelToolListener(module));
    }
}
