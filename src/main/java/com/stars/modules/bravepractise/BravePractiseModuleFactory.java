package com.stars.modules.bravepractise;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.bravepractise.gm.BravePractiseGmHandler;
import com.stars.modules.bravepractise.listener.BraveListener;
import com.stars.modules.bravepractise.prodata.BraveInfoVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.task.event.SubmitTaskEvent;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/16.
 */
public class BravePractiseModuleFactory extends AbstractModuleFactory<BravePractiseModule> {

	public BravePractiseModuleFactory() {
		super(new BravePractisePacketSet());
	}
	
	@Override
    public BravePractiseModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new BravePractiseModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
        GmManager.reg("brave", new BravePractiseGmHandler());
    }
	
	@Override
    public void loadProductData() throws Exception {
       loadBraveInfo();
       initConfig();
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	eventDispatcher.reg(SubmitTaskEvent.class, new BraveListener(module));
    }  
	
	private void loadBraveInfo() throws SQLException {
        String sql = "select * from `braveinfo`; ";
        Map<Integer, BraveInfoVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "braveid", BraveInfoVo.class, sql);
        BravePractiseManager.setBraveInfoVoMap(map);
    }
	
    private void initConfig() {
    	BravePractiseManager.bravePractiseCount = Integer.valueOf(DataManager.getCommConfig("braveinfo_count"));
    }

}

