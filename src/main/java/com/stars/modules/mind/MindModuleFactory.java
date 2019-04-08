package com.stars.modules.mind;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.mind.listener.AddToolListener;
import com.stars.modules.mind.listener.RoleLvUpMindListener;
import com.stars.modules.mind.listener.UseToolListener;
import com.stars.modules.mind.prodata.MindLevelVo;
import com.stars.modules.mind.prodata.MindVo;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 心法的模块工厂;
 * Created by gaopeidian on 2016/9/21.
 */
public class MindModuleFactory extends AbstractModuleFactory<MindModule> {

	public MindModuleFactory() {
		super(new MindPacketSet());
	}
	
	@Override
    public MindModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new MindModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void loadProductData() throws Exception {
        //初始化心法的产品数据;
		initProductMind();
        //初始化心法等级的产品数据;
		initProductMindLevel();
    }

	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		eventDispatcher.reg(UseToolEvent.class, new UseToolListener((MindModule) module));
		eventDispatcher.reg(AddToolEvent.class, new AddToolListener((MindModule) module));
		eventDispatcher.reg(RoleLevelUpEvent.class, new RoleLvUpMindListener((MindModule) module));
	}

	private void initProductMind() throws SQLException {
        String sql = "select * from `mind`; ";
        Map<Integer, MindVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "mindid", MindVo.class, sql);
        MindManager.setMindVoMap(map);
    }

    private void initProductMindLevel() throws SQLException {
    	String sql = "SELECT * FROM `mindlevel`;";   	
    	List<MindLevelVo> tmpMindLevelVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, MindLevelVo.class, sql);
        Map<String, MindLevelVo> map = new HashMap<String, MindLevelVo>();
        for (MindLevelVo mindLevelVo : tmpMindLevelVoList) {
			int mindId = mindLevelVo.getMindId();
			int mindLevel = mindLevelVo.getLevel();
			String key = MindManager.getMindLevelKey(mindId, mindLevel);
			map.put(key, mindLevelVo);
		}
            
		MindManager.setMindLevelVoMap(map);
    }
}

