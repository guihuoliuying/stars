package com.stars.modules.poem;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.gm.GmManager;
import com.stars.modules.poem.gm.PoemGmHandler;
import com.stars.modules.poem.listener.PoemListener;
import com.stars.modules.poem.prodata.PoemVo;
import com.stars.modules.scene.event.PassStageEvent;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/1/9.
 */
public class PoemModuleFactory extends AbstractModuleFactory<PoemModule> {

	public PoemModuleFactory() {
		super(new PoemPacketSet());
	}
	
	@Override
    public PoemModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new PoemModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
		GmManager.reg("poem", new PoemGmHandler());
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
		PoemListener listener = new PoemListener(module);
    	eventDispatcher.reg(PassStageEvent.class, listener);
	}
	
	@Override
    public void loadProductData() throws Exception {
		initPoems();
    }
	
	private void initPoems() throws SQLException {
        String sql = "select * from `poems`; ";
        Map<Integer, PoemVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "poemsid", PoemVo.class, sql);
        PoemManager.setPoemVoMap(map);
    }
}

