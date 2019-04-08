package com.stars.modules.everydaycharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.everydaycharge.listener.EverydayChargeListener;
import com.stars.modules.everydaycharge.prodata.EverydayChargeVo;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.HashMap;
import java.util.Map;

public class EverydayChargeModuleFactory extends AbstractModuleFactory<EverydayChargeModule> {

	public EverydayChargeModuleFactory() {
		super(new EverydayChargePacketSet());
	}

	@Override
    public void loadProductData() throws Exception {
		String sql = "select * from everydaycharge";
		Map<Integer, EverydayChargeVo> chargeVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "everydayid", EverydayChargeVo.class, sql);
		Map<Integer, Map<Integer, EverydayChargeVo>> everydayChargeVoMap = new HashMap<Integer, Map<Integer,EverydayChargeVo>>();
		for (EverydayChargeVo chargeVo : chargeVoMap.values()) {
			if (everydayChargeVoMap.containsKey(chargeVo.getOperateactid())) {
				everydayChargeVoMap.get(chargeVo.getOperateactid()).put(chargeVo.getEverydayid(), chargeVo);
			} else {
				Map<Integer, EverydayChargeVo> tempMap = new HashMap<>();
				tempMap.put(chargeVo.getEverydayid(), chargeVo);
				everydayChargeVoMap.put(chargeVo.getOperateactid(), tempMap);
			}
		}

		EverydayChargeManager.everydayChargeVoMap = everydayChargeVoMap;
    }
	
    @Override
    public EverydayChargeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new EverydayChargeModule(id, self, eventDispatcher, map);
    }
    
    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	EverydayChargeListener listener = new EverydayChargeListener((EverydayChargeModule) module);
    	eventDispatcher.reg(LoginSuccessEvent.class, listener);
    	eventDispatcher.reg(RoleLevelUpEvent.class, listener);
    	eventDispatcher.reg(VipChargeEvent.class, listener);
    	eventDispatcher.reg(OperateActivityFlowEvent.class, listener);
    }
}
