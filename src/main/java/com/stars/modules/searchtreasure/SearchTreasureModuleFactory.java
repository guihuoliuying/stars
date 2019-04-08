package com.stars.modules.searchtreasure;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.scene.event.EnterSceneEvent;
import com.stars.modules.scene.event.RoleReviveEvent;
import com.stars.modules.searchtreasure.listener.SearchTreasureEnterCityListener;
import com.stars.modules.searchtreasure.listener.SearchTreasureReviveListener;
import com.stars.modules.searchtreasure.prodata.SearchContentVo;
import com.stars.modules.searchtreasure.prodata.SearchMapVo;
import com.stars.modules.searchtreasure.prodata.SearchStageVo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仙山探宝系统;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchTreasureModuleFactory extends AbstractModuleFactory<SearchTreasureModule> {

    public SearchTreasureModuleFactory() {
        super(new SearchTreasurePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        initProductSearchMap();
        initProductSearchStage();
        initProductSearchContent();
    }

    private void initProductSearchMap() throws SQLException {
        String sql = "select * from `searchmap`";
        List<SearchMapVo> searchMapVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, SearchMapVo.class, sql);
        SearchTreasureManager.setSearchMapList(searchMapVoList);
    }

    private void initProductSearchStage() throws SQLException{
        String sql = "select * from `searchstage`";
        List<SearchStageVo> searchStageVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, SearchStageVo.class, sql);
        HashMap<Integer, SearchStageVo> searchStageVoHashMap = new HashMap<>();
        SearchStageVo tmpSearchStageVo = null;
        for(int i = 0, len = searchStageVoList.size(); i<len; i++){
            tmpSearchStageVo = searchStageVoList.get(i);
            searchStageVoHashMap.put(tmpSearchStageVo.getStageinfoid(), tmpSearchStageVo);
        }
        SearchTreasureManager.setSearchStageMap(searchStageVoHashMap);
    }

    private void initProductSearchContent() throws SQLException{
        String sql = "select * from `searchcontent`";
        List<SearchContentVo> searchList = DBUtil.queryList(DBUtil.DB_PRODUCT, SearchContentVo.class, sql);
        HashMap<Integer, SearchContentVo> searchContentVoHashMap = new HashMap<>();
        SearchContentVo tmpSearchContentVo = null;
        for(int i = 0, len = searchList.size(); i<len; i++){
            tmpSearchContentVo = searchList.get(i);
            searchContentVoHashMap.put(tmpSearchContentVo.getContentid(), tmpSearchContentVo);
        }
        SearchTreasureManager.setSearchContentMap(searchContentVoHashMap);
    }

    @Override
    public void init() {

    }

    @Override
    public SearchTreasureModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new SearchTreasureModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(EnterSceneEvent.class, new SearchTreasureEnterCityListener(module));
        eventDispatcher.reg(RoleReviveEvent.class, new SearchTreasureReviveListener(module));
    }
}
