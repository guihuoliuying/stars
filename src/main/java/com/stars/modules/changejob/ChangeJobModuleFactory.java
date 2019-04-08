package com.stars.modules.changejob;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.changejob.gm.ChangeJobGmHandler;
import com.stars.modules.changejob.prodata.ChangeJobVo;
import com.stars.modules.gm.GmManager;

import java.util.Map;

/**
 * Created by huwenjun on 2017/5/24.
 */
public class ChangeJobModuleFactory extends AbstractModuleFactory<ChangeJobModule> {
    public ChangeJobModuleFactory() {
        super(new ChangeJobPacketSet());
    }



    @Override
    public ChangeJobModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ChangeJobModule("转职功能", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from changejob;";
        Map<Integer, ChangeJobVo> changeSchoolMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "jobtype", ChangeJobVo.class, sql);
        ChangeJobManager.changeJobMap = changeSchoolMap;
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("changejob", new ChangeJobGmHandler());
    }


}
