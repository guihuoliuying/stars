package com.stars.modules.deityweapon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.deityweapon.listener.DeityWeaponListener;
import com.stars.modules.deityweapon.prodata.DeityWeaponLevelVo;
import com.stars.modules.deityweapon.prodata.DeityWeaponVo;
import com.stars.modules.deityweapon.summary.DeityWeaponSummaryComponentImpl;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.services.summary.Summary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 神兵数据工厂;
 * Created by panzhenfeng on 2016/12/14.
 */
public class DeityWeaponModuleFactory extends AbstractModuleFactory<DeityWeaponModule> {

    public DeityWeaponModuleFactory() {
        super(new DeityWeaponPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        //初始化神兵数据;
        initDeityWeaponVo();
        //初始化神兵数据;
        initDeityWeaponLevelVo();
    }

    private void initDeityWeaponVo() throws Exception {
        String deityWeaponSelectSql = "select * from deityweapon";
        List<DeityWeaponVo> deityWeaponVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, DeityWeaponVo.class, deityWeaponSelectSql);
        Map<Integer, DeityWeaponVo> deityWeaponVoMap = new HashMap<>();
        Map<Integer, DeityWeaponVo> itemDeityWeaponVoMap = new HashMap<>();
        Map<Integer, Map<Byte, DeityWeaponVo>> sumMap = new HashMap<>();
        Map<Byte, DeityWeaponVo> tmpMap = null;
        DeityWeaponVo deityWeaponVo;
        for (int i = 0, len = deityWeaponVoList.size(); i < len; i++) {
            deityWeaponVo = deityWeaponVoList.get(i);
            deityWeaponVoMap.put(deityWeaponVo.getDeityweaponId(), deityWeaponVo);
            itemDeityWeaponVoMap.put(deityWeaponVo.getItemId(), deityWeaponVo);
            if (!sumMap.containsKey(deityWeaponVo.getJobid())) {
                sumMap.put(deityWeaponVo.getJobid(), new ConcurrentHashMap<Byte, DeityWeaponVo>());
            }
            tmpMap = sumMap.get(deityWeaponVo.getJobid());
            tmpMap.put(deityWeaponVo.getType(), deityWeaponVo);
        }

        DeityWeaponManager.setDeityWeaponVoMap(sumMap);
        DeityWeaponManager.setGetDeityWeaponVoDic(deityWeaponVoMap);
        DeityWeaponManager.itemDeityWeaponVoMap=itemDeityWeaponVoMap;
    }


    private void initDeityWeaponLevelVo() throws Exception {
        String deityWeaponlevelSelectSql = "select * from deityweaponlvl";
        List<DeityWeaponLevelVo> deityWeaponlevelList = DBUtil.queryList(DBUtil.DB_PRODUCT, DeityWeaponLevelVo.class, deityWeaponlevelSelectSql);
        Map<Byte, Map<Integer, DeityWeaponLevelVo>> sumMap = new HashMap<>();
        Map<Integer, DeityWeaponLevelVo> tmpMap = new HashMap<>();
        Map<Byte, Integer> initTypeLevelMap = new HashMap<>();
        Map<Byte, Integer> maxTypeLevelMap = new HashMap<>();
        DeityWeaponLevelVo deityWeaponLevelVo = null;
        byte type = 0;
        int deityweaponLevel = 0;
        for (int i = 0, len = deityWeaponlevelList.size(); i < len; i++) {
            deityWeaponLevelVo = deityWeaponlevelList.get(i);
            type = deityWeaponLevelVo.getType();
            deityweaponLevel = deityWeaponLevelVo.getDeityweaponlvl();
            if (!sumMap.containsKey(type)) {
                sumMap.put(type, new HashMap<Integer, DeityWeaponLevelVo>());
            }
            tmpMap = sumMap.get(type);
            tmpMap.put(deityweaponLevel, deityWeaponLevelVo);
            //判断是否有记录对应类型了;
            if (!initTypeLevelMap.containsKey(type)) {
                initTypeLevelMap.put(type, deityweaponLevel);
                maxTypeLevelMap.put(type, deityweaponLevel);
            } else {
                if (initTypeLevelMap.get(type) > deityweaponLevel) {
                    initTypeLevelMap.put(type, deityweaponLevel);
                }
                if (maxTypeLevelMap.get(type) < deityweaponLevel) {
                    maxTypeLevelMap.put(type, deityweaponLevel);
                }
            }
        }

        DeityWeaponManager.setDeityWeaponLevelVoMap(sumMap);
        DeityWeaponManager.setInitDeityWeaponLevelVoMap(initTypeLevelMap);
        DeityWeaponManager.setMaxDeityWeaponLevelVoMap(maxTypeLevelMap);
    }

    @Override
    public void init() {
        Summary.regComponentClass(MConst.Deity, DeityWeaponSummaryComponentImpl.class);
    }

    @Override
    public DeityWeaponModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new DeityWeaponModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	DeityWeaponListener listener = new DeityWeaponListener((DeityWeaponModule)module);
    	eventDispatcher.reg(AddToolEvent.class, listener);
    }
}
