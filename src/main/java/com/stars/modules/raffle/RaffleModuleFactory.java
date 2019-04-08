package com.stars.modules.raffle;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.raffle.define.RaffleDefineManager;

import java.util.Map;

/**
 * 
 * @author likang 2017/4/20
 *
 */
public class RaffleModuleFactory extends AbstractModuleFactory<RaffleModule> {

	public RaffleModuleFactory() {
		super(new RafflePacketSet());
	}

	@Override
	public RaffleModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		return new RaffleModule("元宝抽奖", id, self, eventDispatcher, map);
	}

	// 加载产品数据,在加载数据时初始化DefineManager

	@Override
	public void loadProductData() throws Exception {
		RaffleDefineManager.instance.reload();
	}

}
