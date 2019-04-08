package com.stars.modules.onlinereward;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.onlinereward.listener.OnlineRewardListener;
import com.stars.modules.onlinereward.prodata.OnlineRewardVo;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class OnlineRewardModuleFactory extends AbstractModuleFactory<OnlineRewardModule> {
	public OnlineRewardModuleFactory() {
		super(new OnlineRewardPacketSet());
	}
	
	@Override
    public OnlineRewardModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new OnlineRewardModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
		//GmManager.reg("master", new MasterNoticeGmHandler());
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
		OnlineRewardListener listener = new OnlineRewardListener(module);
		eventDispatcher.reg(OperateActivityEvent.class, listener);
    	eventDispatcher.reg(LoginSuccessEvent.class, listener);
    	eventDispatcher.reg(RoleLevelUpEvent.class, listener);
    	eventDispatcher.reg(ForeShowChangeEvent.class, listener);
    }
	
	@Override
    public void loadProductData() throws Exception {
		loadOnlineRewardVo();
    }
	
	private void loadOnlineRewardVo() throws SQLException {
        String sql = "select * from `onlinereward`; ";
        Map<Integer, OnlineRewardVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "onlinerewardid", OnlineRewardVo.class, sql);
        OnlineRewardManager.setOnlineRewardVoMap(map);
    }
}

