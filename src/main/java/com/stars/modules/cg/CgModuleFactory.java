package com.stars.modules.cg;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.cg.prodata.CgGroupVo;
import com.stars.network.PacketSet;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class CgModuleFactory  extends AbstractModuleFactory<CgModule> {

    public CgModuleFactory() {
        super(new CgPacketSet());
    }

    public CgModuleFactory(PacketSet packetSet) {
        super(packetSet);
    }

    @Override
    public CgModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new CgModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void loadProductData() throws Exception {
        loadCgGroupVo();
    }

    private void loadCgGroupVo() throws SQLException {
        String sql = "select * from `cggroup`; ";
        List<CgGroupVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, CgGroupVo.class, sql);
        Map<Integer, CgGroupVo> map = new HashMap<>();
        for (CgGroupVo cgGroupVo : list) {
            map.put(cgGroupVo.getCgGroupId(), cgGroupVo);
        }
        CgManager.cgGroupMap = map;
    }

}
