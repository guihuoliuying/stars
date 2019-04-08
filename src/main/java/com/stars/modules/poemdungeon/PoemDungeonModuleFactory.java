package com.stars.modules.poemdungeon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.poemdungeon.prodata.PoemRobotVo;

import java.util.Map;

/**
 * Created by gaopeidian on 2017/5/12.
 */
public class PoemDungeonModuleFactory extends AbstractModuleFactory<PoemDungeonModule> {
    public PoemDungeonModuleFactory() {
        super(new PoemDungeonPacketSet());
    }

    @Override
    public void init() throws Exception {
    	//BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_POEMDUNGEON, PoemDungeonTeamHandler.class); 
    }

    @Override
    public PoemDungeonModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new PoemDungeonModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
    	String sql = "select * from `poemrobot`; ";
        Map<Integer, PoemRobotVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "poemrobotid", PoemRobotVo.class, sql);
        PoemDungeonManager.setPoemRobotVoMap(map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
//    	EliteDungeonListener listener = new EliteDungeonListener((PoemDungeonModule) module);
//    	eventDispatcher.reg(EliteDungeonEnterFightEvent.class, listener);
    }

}
