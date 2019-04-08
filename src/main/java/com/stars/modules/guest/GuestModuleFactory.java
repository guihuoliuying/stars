package com.stars.modules.guest;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.guest.listener.EnterSceneListener;
import com.stars.modules.guest.listener.GuestExchangeEventListener;
import com.stars.modules.guest.listener.GuestToolListener;
import com.stars.modules.guest.listener.RoleRenameListenner;
import com.stars.modules.guest.prodata.*;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.scene.event.EnterSceneEvent;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.services.guest.GuestExchangeEvent;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestModuleFactory extends AbstractModuleFactory {

    public GuestModuleFactory() {
        super(new GuestPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from guestinfo";
        ConcurrentMap<Integer, GuestInfoVo> infoMap = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "guestid", GuestInfoVo.class, sql);
//        GuestManager.setInfoMap(infoMap);

        sql = "select * from gueststage";
        List<GuestStageVo> stageList = DBUtil.queryList(DBUtil.DB_PRODUCT, GuestStageVo.class, sql);
        ConcurrentMap<Integer, ConcurrentMap<Integer, GuestStageVo>> stageMap = new ConcurrentHashMap<>();
        for (GuestStageVo stageVo : stageList) {
            ConcurrentMap<Integer, GuestStageVo> guestStage = stageMap.get(stageVo.getGuestId());
            if (guestStage == null) {
                guestStage = new ConcurrentHashMap<>();
                stageMap.put(stageVo.getGuestId(), guestStage);
            }
            guestStage.put(stageVo.getLevel(), stageVo);
        }
//        GuestManager.setStageMap(stageMap);

        sql = "select * from guestfeeling";
        ConcurrentMap<Integer, GuestFeelingVo> feelingMap = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "guestfeelid", GuestFeelingVo.class, sql);
//        GuestManager.setFeelingMap(feelingMap);

        sql = "select * from guestmission";
        ConcurrentMap<Integer, GuestMissionVo> missionMap = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "guemissionid", GuestMissionVo.class, sql);
//        GuestManager.setMissionMap(missionMap);
        ConcurrentMap<Byte, ConcurrentMap<Integer, GuestMissionVo>> qualityMap = new ConcurrentHashMap<>();
        for (GuestMissionVo missionVo : missionMap.values()) {
            ConcurrentMap<Integer, GuestMissionVo> map = qualityMap.get(missionVo.getQuality());
            if (map == null) {
                map = new ConcurrentHashMap<>();
                qualityMap.put(missionVo.getQuality(), map);
            }
            map.put(missionVo.getGueMissionId(), missionVo);
        }
//        GuestManager.setQualityMap(qualityMap);

        sql = "select * from guestrefresh";
        List<GuestRefreshVo> list1 = DBUtil.queryList(DBUtil.DB_PRODUCT, GuestRefreshVo.class, sql);
        ConcurrentMap<String, List<GuestRefreshVo>> refreshMap = new ConcurrentHashMap<>();
        for (GuestRefreshVo freshVo : list1) {
            List<GuestRefreshVo> list = refreshMap.get(freshVo.getGuestCount());
            if (list == null) {
                list = new CopyOnWriteArrayList<>();
                refreshMap.put(freshVo.getGuestCount(), list);
            }
            // 保证priority从大到小排序
            ListIterator<GuestRefreshVo> iter = list.listIterator();
            int index = 0;
            while (iter.hasNext()) {
                GuestRefreshVo next = iter.next();
                if (next.getPriority() > freshVo.getPriority()) {
                    index = iter.nextIndex();
                    continue;
                } else {
                    index = iter.previousIndex();
                    break;
                }
            }
            list.add(index, freshVo);
        }

        GuestManager.setInfoMap(infoMap);
        GuestManager.setStageMap(stageMap);
        GuestManager.setFeelingMap(feelingMap);
        GuestManager.setMissionMap(missionMap);
        GuestManager.setQualityMap(qualityMap);
        GuestManager.setRefreshMap(refreshMap);
        GuestManager.HELP_LIMIT_TIME = DataManager.getCommConfig("guest_helplimittime", 24) * 60 * 60;
        GuestManager.REFRESH_TIMES = DataManager.getCommConfig("guest_rmbrefreshcount", 10);
        GuestManager.RMB_REFRESH_COST = ToolManager.parseString(DataManager.getCommConfig("guest_rmbrefresh"));
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(GuestExchangeEvent.class, new GuestExchangeEventListener(module));
        eventDispatcher.reg(AddToolEvent.class, new GuestToolListener(module));
        eventDispatcher.reg(EnterSceneEvent.class, new EnterSceneListener(module));
        eventDispatcher.reg(RoleRenameEvent.class,new RoleRenameListenner((GuestModule) module));
    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new GuestModule(id, self, eventDispatcher, map);
    }
}
