package com.stars.modules.levelSpeedUp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.levelSpeedUp.event.LevelSpeedUpEvent;
import com.stars.modules.levelSpeedUp.listener.LevelSpeedUpListener;
import com.stars.modules.levelSpeedUp.productData.LevelSpeedUpAdditionVo;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.util.Iterator;
import java.util.Map;

public class LevelSpeedUpModuleFactory extends AbstractModuleFactory<LevelSpeedUpModule> {

	public LevelSpeedUpModuleFactory() {
		super(null);
	}
	
	@Override
	public LevelSpeedUpModule newModule(long id, Player self, EventDispatcher eventDispatcher,
                                        Map<String, Module> map) {
		return new LevelSpeedUpModule(id, self, eventDispatcher, map);
	}
	
	@Override
	public void loadProductData() throws Exception {
		loadCommonData();
		
		String sql = "select * from levelspeedupaddition";
		Map<Integer, LevelSpeedUpAdditionVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "levelgad", LevelSpeedUpAdditionVo.class, sql);
		Iterator<Integer> iterator = map.keySet().iterator();
		int levelGad = 0;
		int maxLevelGad = 0;
		for(;iterator.hasNext();){
			levelGad = iterator.next();
			if(maxLevelGad<levelGad){
				maxLevelGad = levelGad;
			}
		}
		LevelSpeedUpManager.MAX_LEVEL_GAD = maxLevelGad;
		LevelSpeedUpManager.gadAdditionMap = map;
		
	}
	
	public void loadCommonData(){
		LevelSpeedUpManager.MEAN_NUM = DataManager.getCommConfig("levelspeedup_topN", 10);
		LevelSpeedUpManager.START_LEVEL = DataManager.getCommConfig("levelspeedup_playerLV", 20);
		LevelSpeedUpManager.OPEN_DAYS_STANDARD =DataManager.getCommConfig("levelspeedup_Days", 1);
		LevelSpeedUpManager.TOP_LEVEL_STANDARD =DataManager.getCommConfig("levelspeedup_topLV", 70);
		LevelSpeedUpManager.GAD_STANDARD =DataManager.getCommConfig("levelspeedup_deltaLV", 5);
	}
	
	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		LevelSpeedUpListener listener = new LevelSpeedUpListener((LevelSpeedUpModule)module);
		eventDispatcher.reg(RoleLevelUpEvent.class, listener);
		eventDispatcher.reg(LevelSpeedUpEvent.class, listener);
	}

}
