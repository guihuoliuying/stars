package com.stars.modules.induct;

import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.induct.event.InductEvent;
import com.stars.modules.induct.listener.InductListener;
import com.stars.modules.induct.prodata.InductVo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/18.
 */
public class InductModuleFactory extends AbstractModuleFactory<InductModule> {
    public InductModuleFactory() {
        super(new InductPacketSet());
    }

    @Override
    public InductModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new InductModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(InductEvent.class, new InductListener(module));
    }
    
    @Override
    public void loadProductData() throws Exception {
        loadInductVo();
    }

    private void loadInductVo() throws SQLException {
        String sql = "select * from `induct`; ";
        List<InductVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, InductVo.class, sql);
        Map<Integer, InductVo> map = new HashMap<>();
        for (InductVo inductVo : list) {
            if (inductVo.getStep() != InductManager.INIT_STEP) continue;
            map.put(inductVo.getInductid(), inductVo);
        }
        InductManager.inductVoMap = map;
    }
}
