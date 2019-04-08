package com.stars.modules.ride;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.gm.GmManager;
import com.stars.modules.ride.event.RideLevelUpEvent;
import com.stars.modules.ride.gm.RideGmHandler;
import com.stars.modules.ride.listener.AddToolEventListener;
import com.stars.modules.ride.listener.DelToolListener;
import com.stars.modules.ride.listener.RideLevelUpListener;
import com.stars.modules.ride.listener.RoleLevelUpListener;
import com.stars.modules.ride.prodata.RideAwakeLvlVo;
import com.stars.modules.ride.prodata.RideInfoVo;
import com.stars.modules.ride.prodata.RideLevelVo;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.modules.vip.event.VipLevelupEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/19.
 */
public class RideModuleFactory extends AbstractModuleFactory<RideModule> {

    public RideModuleFactory() {
        super(new RidePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, RideInfoVo> infoVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "rideid", RideInfoVo.class,
                "select * from `rideinfo`");
        Map<Integer, RideLevelVo> levelVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", RideLevelVo.class, "select * from `ridelevel`");
        Map<Integer, Map<Integer, RideLevelVo>> rideLevelVoMap = new HashMap<>();
        for (RideLevelVo levelVo : levelVoMap.values()) {
        	levelVo.calcFightScore();
        	Map<Integer, RideLevelVo> stageMap = rideLevelVoMap.get(levelVo.getStagelevel());
        	if (stageMap == null) {
        		stageMap = new HashMap<>();
        		rideLevelVoMap.put(levelVo.getStagelevel(), stageMap);
        	}
        	stageMap.put(levelVo.getLevel(), levelVo);
        }

        List<RideAwakeLvlVo> rideAwakeLvlVoList = DBUtil.queryList(
                DBUtil.DB_PRODUCT, RideAwakeLvlVo.class, "select * from rideawakelvl");
        Map<Integer, Map<Integer, RideAwakeLvlVo>> rideAwakeLevelVoMap = new HashMap<>();
        for (RideAwakeLvlVo vo : rideAwakeLvlVoList) {
            Map<Integer, RideAwakeLvlVo> tempMap = rideAwakeLevelVoMap.get(vo.getRideId());
            if (tempMap == null) {
                rideAwakeLevelVoMap.put(vo.getRideId(), tempMap = new HashMap<Integer, RideAwakeLvlVo>());
            }
            tempMap.put(vo.getAwakeLevel(), vo);
        }

        // 赋值
        RideManager.rideInfoVoMap = infoVoMap;
        RideManager.rideLevelVoMap = rideLevelVoMap;
        RideManager.rideLevelIdMap = levelVoMap;
        RideManager.rideAwakeLevelVoMap = rideAwakeLevelVoMap;

    }

    @Override
    public RideModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new RideModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("ride", new RideGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(AddToolEvent.class,new AddToolEventListener((RideModule) module));
        eventDispatcher.reg(RoleLevelUpEvent.class,new RoleLevelUpListener((RideModule) module));
        eventDispatcher.reg(VipLevelupEvent.class,new RoleLevelUpListener((RideModule) module));
        eventDispatcher.reg(RideLevelUpEvent.class,new RideLevelUpListener((RideModule) module));
        eventDispatcher.reg(UseToolEvent.class,new DelToolListener((RideModule) module));
    }
}
