package com.stars.modules.retrievereward;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.retrievereward.event.PreDailyRecordResetEvent;
import com.stars.modules.retrievereward.listener.RetrieveRewardListener;
import com.stars.modules.retrievereward.prodata.RetrieveRewardVo;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class RetrieveRewardModuleFactory extends AbstractModuleFactory<RetrieveRewardModule> {
	public RetrieveRewardModuleFactory() {
		super(new RetrieveRewardPacketSet());
	}
	
	@Override
    public RetrieveRewardModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new RetrieveRewardModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
		//GmManager.reg("master", new MasterNoticeGmHandler());
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
		RetrieveRewardListener listener = new RetrieveRewardListener(module);
		eventDispatcher.reg(OperateActivityEvent.class, listener);
    	eventDispatcher.reg(PreDailyRecordResetEvent.class, listener);
    	eventDispatcher.reg(RoleLevelUpEvent.class, listener);
    	eventDispatcher.reg(ForeShowChangeEvent.class, listener);
    }
	
	@Override
    public void loadProductData() throws Exception {
		loadRetrieveRewardVo();
    }
	
	private void loadRetrieveRewardVo() throws SQLException {
        String sql = "select * from `retrievereward`; ";
        Map<Integer, RetrieveRewardVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "retrieverewardid", RetrieveRewardVo.class, sql);
        RetrieveRewardManager.setRetrieveRewardVoMap(map);
    }
}

