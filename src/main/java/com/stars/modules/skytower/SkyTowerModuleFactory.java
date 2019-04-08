package com.stars.modules.skytower;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.skytower.prodata.SkyTowerVo;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 镇妖塔的模块工厂;
 * Created by panzhenfeng on 2016/8/10.
 */
public class SkyTowerModuleFactory extends AbstractModuleFactory<SkyTowerModule> {

    public SkyTowerModuleFactory() {
        super(new SkyTowerPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        //初始化镇妖塔产品数据;
        initProductSkyTower();
    }

    private void initProductSkyTower() throws SQLException {
        String skytowerSelSql = "select * from skytower";
        List<SkyTowerVo> skyTowerVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, SkyTowerVo.class, skytowerSelSql);
        HashMap<Integer, SkyTowerVo> skyTowerVoHashMap = new HashMap<>();
        HashMap<Integer,SkyTowerVo> skyTowerVoByLayerSerialHashMap = new HashMap<>();
        List<SkyTowerVo> challengeSkyTowerVoList = new ArrayList<>();
        int initLayerId = 0;
        int maxLayerId = 0;
        SkyTowerVo skyTowerVo = null;
        for(int i = 0, len = skyTowerVoList.size(); i<len; i++){
            skyTowerVo = skyTowerVoList.get(i);
            skyTowerVoHashMap.put(skyTowerVo.getLayerId(), skyTowerVo);
            skyTowerVoByLayerSerialHashMap.put(skyTowerVo.getLayerSerial(),skyTowerVo);
            if(i==0){
                initLayerId = skyTowerVo.getLayerId();
            }else if(i == len-1){
                maxLayerId = skyTowerVo.getLayerId();
            }
            if(StringUtil.isNotEmpty(skyTowerVo.getChallengeSucReward())){
                challengeSkyTowerVoList.add(skyTowerVo);
            }
        }

        SkyTowerManager.setInitLayerId(initLayerId);
        SkyTowerManager.setMaxLayerId(maxLayerId);
        SkyTowerManager.setSkyTowerMapData(skyTowerVoHashMap);
        SkyTowerManager.setSkyTowerVoByLayerSerialHashMap(skyTowerVoByLayerSerialHashMap);
        SkyTowerManager.setChallengeSkyTowerVoList(challengeSkyTowerVoList);
    }

    @Override
    public void init() {

    }

    @Override
    public SkyTowerModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new SkyTowerModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {

    }


}
