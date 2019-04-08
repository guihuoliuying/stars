package com.stars.modules.newserverrank;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.newserverrank.prodata.NewServerRankVo;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class NewServerRankModuleFactory extends AbstractModuleFactory<NewServerRankModule> {
	public NewServerRankModuleFactory() {
		super(new NewServerRankPacketSet());
	}
	
	@Override
    public NewServerRankModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new NewServerRankModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
		//GmManager.reg("master", new MasterNoticeGmHandler());
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
		
    }
	
	@Override
    public void loadProductData() throws Exception {
		loadNewServerRankVo();
    }
	
	private void loadNewServerRankVo() throws SQLException {
        String sql = "select * from `newserverrank`; ";
        Map<Integer, NewServerRankVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "newserverrankid", NewServerRankVo.class, sql);
        NewServerRankManager.setNewServerRankVoMap(map);
    }
}

