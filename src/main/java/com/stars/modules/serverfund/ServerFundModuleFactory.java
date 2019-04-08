package com.stars.modules.serverfund;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverfund.listener.ServerFundListener;
import com.stars.modules.serverfund.prodata.ServerFundVo;
import com.stars.util.MapUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.stars.modules.data.DataManager.commonConfigMap;
import static com.stars.modules.data.DataManager.getCommConfig;

public class ServerFundModuleFactory extends AbstractModuleFactory<ServerFundModule> {

	public ServerFundModuleFactory() {
		super(new ServerFundPacketSet());
	}

	@Override
    public void loadProductData() throws Exception {
		Map<Integer, ServerFundVo> fundVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "fundid", ServerFundVo.class,
				"select * from serverfund");
		Map<Integer, Map<Integer, ServerFundVo>> activityFundVoMap = new ConcurrentHashMap<Integer, Map<Integer,ServerFundVo>>();
		for (ServerFundVo fundVo : fundVoMap.values()) {
			if (activityFundVoMap.containsKey(fundVo.getOperateactid())) {
				activityFundVoMap.get(fundVo.getOperateactid()).put(fundVo.getFundid(), fundVo);
			} else {
				Map<Integer, ServerFundVo> map = new ConcurrentHashMap<>();
				map.put(fundVo.getFundid(), fundVo);
				activityFundVoMap.put(fundVo.getOperateactid(), map);
			}
		}
		
		ServerFundManager.ServerFundVoMap = activityFundVoMap;
		
		ServerFundManager.minVipLevel = getCommConfig("serverfund_viplevel", 1);
		ServerFundManager.moneyId = com.stars.util.MapUtil.getInt(commonConfigMap, "serverfund_cost", "\\+", 0, 1);
		ServerFundManager.moneyCount = MapUtil.getInt(commonConfigMap, "serverfund_cost", "\\+", 1, 1);
    }
	
    @Override
    public ServerFundModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ServerFundModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        // TODO
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	ServerFundListener listener = new ServerFundListener((ServerFundModule) module);
    	eventDispatcher.reg(RoleLevelUpEvent.class, listener);
    }
}
