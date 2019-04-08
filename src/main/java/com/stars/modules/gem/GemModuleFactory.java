package com.stars.modules.gem;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.gem.listener.GemRoleLevelUpListener;
import com.stars.modules.gem.listener.GetToolGemListener;
import com.stars.modules.gem.listener.UseToolListenner;
import com.stars.modules.gem.prodata.GemHoleVo;
import com.stars.modules.gem.prodata.GemLevelVo;
import com.stars.modules.gem.summary.GemSummaryComponentImpl;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.services.summary.Summary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 装备的模块工厂;
 * Created by panzhenfeng on 2016/6/24.
 */
public class GemModuleFactory extends AbstractModuleFactory<GemModule> {

    public GemModuleFactory() {
        super(new GemPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        //初始化宝石槽数据;
        initProductGemHoleVo();
        //初始化宝石数据;
        intiProductGemLevelVo();
    }

    private void initProductGemHoleVo() throws SQLException {
        String gemHoleSelectSql = "select * from gemhole";
        //初始化宝石槽数据
        List<GemHoleVo> gemHoleVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, GemHoleVo.class, gemHoleSelectSql);
        GemHoleVo gemHoleVo;
        int length = gemHoleVoList.size();
        String tmpKey;
        HashMap<String, GemHoleVo> gemHoleMap = new HashMap<>();
        HashMap<Byte, Integer> equipmentGemHoleCountMap = new HashMap<>();
        for (int i = 0; i < length; i++) {
            gemHoleVo = gemHoleVoList.get(i);
            tmpKey = GemManager.getGemHoleKey(gemHoleVo.getEquipType(), gemHoleVo.getHoleId());
            gemHoleMap.put(tmpKey, gemHoleVo);
            if (equipmentGemHoleCountMap.containsKey(gemHoleVo.getEquipType()) == false) {
                equipmentGemHoleCountMap.put(gemHoleVo.getEquipType(), 0);
            }
            equipmentGemHoleCountMap.put(gemHoleVo.getEquipType(), equipmentGemHoleCountMap.get(gemHoleVo.getEquipType()) + 1);
        }

        GemManager.setGemHoleVoDatas(gemHoleMap, equipmentGemHoleCountMap);
    }

    private void intiProductGemLevelVo() throws SQLException {
        String gemLevelSelectSql = "select * from gemlevel";
        List<GemLevelVo> gemLevelVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, GemLevelVo.class, gemLevelSelectSql);
        HashMap<Integer, GemLevelVo> gemLevelMap = new HashMap<>();
        GemLevelVo itemVo = null;
        int length = gemLevelVoList.size();
        for (int i = 0; i < length; i++) {
            itemVo = gemLevelVoList.get(i);
            gemLevelMap.put(itemVo.getItemId(), itemVo);
        }
        GemManager.setGemLevelVoDatas(gemLevelMap);
    }

    @Override
    public void init() {
        Summary.regComponentClass(MConst.GEM, GemSummaryComponentImpl.class);
    }

    @Override
    public GemModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new GemModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(AddToolEvent.class, new GetToolGemListener(module));
        eventDispatcher.reg(UseToolEvent.class, new UseToolListenner((GemModule) module));
        eventDispatcher.reg(RoleLevelUpEvent.class, new GemRoleLevelUpListener(module));
    }


}
