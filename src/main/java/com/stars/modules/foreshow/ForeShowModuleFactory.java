package com.stars.modules.foreshow;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.AchievementEvent;
import com.stars.modules.foreshow.gm.ForeShowGmHandler;
import com.stars.modules.foreshow.listener.ForeShowChangeListener;
import com.stars.modules.foreshow.listener.NewRideListener;
import com.stars.modules.foreshow.prodata.ForeShowVo;
import com.stars.modules.foreshow.prodata.ShowSystemVo;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponentImp;
import com.stars.modules.gm.GmManager;
import com.stars.modules.ride.event.NewRideEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.task.event.SubmitTaskEvent;
import com.stars.services.marry.event.MarryBattleEvent;
import com.stars.services.summary.Summary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2016/10/28.
 */
public class ForeShowModuleFactory extends AbstractModuleFactory<ForeShowModule> {
    public ForeShowModuleFactory() {
        super(new ForeShowPacketSet());
    }

    @Override
    public ForeShowModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ForeShowModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        Map<String, ForeShowVo> map = new HashMap<>();
        Map<Integer, ForeShowVo> serialMap = new HashMap<>();
        List<ForeShowVo> foreShowVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, ForeShowVo.class, "select * from open");
        for (ForeShowVo foreShowVo : foreShowVoList) {
            map.put(foreShowVo.getName(), foreShowVo);
            if (foreShowVo.getForeshowserial() > 0) {
                serialMap.put(foreShowVo.getForeshowserial(), foreShowVo);
            }
        }
        ForeShowManager.setForeShowVoMap(map);
        ForeShowManager.setForeShowSerialMap(serialMap);
        loadShowSystem();
    }

    private void loadShowSystem() throws SQLException {
        Map<String, ShowSystemVo> map = new HashMap<>();
        List<ShowSystemVo> showSystemVos = DBUtil.queryList(DBUtil.DB_PRODUCT, ShowSystemVo.class, "select * from showsystem");
        for (ShowSystemVo systemVo : showSystemVos) {
            map.put(systemVo.getSysName(), systemVo);
        }
        ForeShowManager.setShowSystemVoMap(map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        ForeShowChangeListener listener = new ForeShowChangeListener((ForeShowModule) module);
        eventDispatcher.reg(MarryBattleEvent.class, listener);
        eventDispatcher.reg(RoleLevelUpEvent.class, listener);
        eventDispatcher.reg(PassStageEvent.class, listener);
        eventDispatcher.reg(SubmitTaskEvent.class, listener);
        eventDispatcher.reg(AchievementEvent.class, listener);
        eventDispatcher.reg(NewRideEvent.class, new NewRideListener((ForeShowModule) module));
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("open", new ForeShowGmHandler());
        Summary.regComponentClass(MConst.ForeShow, ForeShowSummaryComponentImp.class);
    }
}
