package com.stars.modules.trump;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.modules.trump.listener.AddToolEventListener;
import com.stars.modules.trump.listener.UseToolListener;
import com.stars.modules.trump.prodata.TrumpKarmaVo;
import com.stars.modules.trump.prodata.TrumpLevelVo;
import com.stars.modules.trump.prodata.TrumpVo;
import com.stars.modules.trump.summary.TrumpSumaryComponentImp;
import com.stars.services.summary.Summary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/9/18.
 */
public class TrumpModuleFactory extends AbstractModuleFactory<TrumpModule> {

    public TrumpModuleFactory() {
        super(new TrumpPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        TrumpManager.trumpMap = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "trumpid", TrumpVo.class, "select * from trump");
        TrumpManager.trumpKarmaMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", TrumpKarmaVo.class, "select * from trumpkarma");
        List<TrumpLevelVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, TrumpLevelVo.class, "select * from trumplevel");
        ConcurrentMap<Integer, ConcurrentMap<Short, TrumpLevelVo>> levelMap = new ConcurrentHashMap<>();
        ConcurrentMap<Integer, Short> maxMap = new ConcurrentHashMap<>();
        for (TrumpLevelVo levelVo : list) {
            ConcurrentMap<Short, TrumpLevelVo> map = levelMap.get(levelVo.getTrumpId());
            if (map == null) {
                map = new ConcurrentHashMap<>();
                levelMap.put(levelVo.getTrumpId(), map);
            }
            map.put(levelVo.getLevel(), levelVo);
            if (maxMap.get(levelVo.getTrumpId()) == null ||
                    maxMap.get(levelVo.getTrumpId()) < levelVo.getLevel()) {
                maxMap.put(levelVo.getTrumpId(), levelVo.getLevel());
            }
        }
        TrumpManager.levelMap = levelMap;
        TrumpManager.maxLevel = maxMap;
    }

    @Override
    public void init() throws Exception {
        Summary.regComponentClass("trump", TrumpSumaryComponentImp.class);
    }

    @Override
    public TrumpModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new TrumpModule("法宝", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(AddToolEvent.class, new AddToolEventListener((TrumpModule) module));
        eventDispatcher.reg(UseToolEvent.class, new UseToolListener((TrumpModule) module));
    }
}
