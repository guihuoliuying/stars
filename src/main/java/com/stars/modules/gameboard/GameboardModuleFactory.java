package com.stars.modules.gameboard;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.gameboard.prodata.GameboardVo;

import java.util.Map;

/**
 * Created by chenkeyu on 2017/1/5 18:34
 */
public class GameboardModuleFactory extends AbstractModuleFactory<GameboardModule> {
    public GameboardModuleFactory() {
        super(new GameboardPacketSet());
    }

    @Override
    public GameboardModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new GameboardModule(MConst.Gameboard, id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from gameboard";
        Map<Integer, GameboardVo> map = DBUtil.queryMap(DBUtil.DB_USER, "boardid", GameboardVo.class, sql);
        GameboardManager.gameboardVoMap = map;
    }

    @Override
    public void init() throws Exception {

    }
}
