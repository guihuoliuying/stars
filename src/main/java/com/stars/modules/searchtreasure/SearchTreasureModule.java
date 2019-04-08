package com.stars.modules.searchtreasure;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.recordmap.RoleRecord;
import com.stars.core.recordmap.RoleRecordMapImpl;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.imp.fight.SearchTreasureScene;
import com.stars.modules.searchtreasure.packet.ClientSearchTreasureInfo;
import com.stars.modules.searchtreasure.recordmap.RecordMapSearchTreasure;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * 仙山探宝系统模块;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchTreasureModule extends AbstractModule {

    private RecordMapSearchTreasure recordMapSearchTreasure;
    //玩家是否死亡了;
    private boolean isSelfDead = false;
    //记录之前缓存的奖励信息;
    private String cachePreCacheAwards = null;
    //记录之前的道具信息;
    private String cachePreItems = null;
    private int reTryTime = 3;

    public SearchTreasureModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.SearchTreasure, id, self, eventDispatcher, moduleMap);
    }

    private void initRecordMap() throws SQLException {
        recordMapSearchTreasure = new RecordMapSearchTreasure(context(), moduleMap());
    }

//    @Override
//    public void onCreation(String name_, String account_) throws Throwable {
//        initRecordMap();
//    }
//
//    @Override
//    public void onDataReq() throws Exception {
//        initRecordMap();
//    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains((RedPointConst.SEARCHTREASURE_MAPREWARD_CANGET))) {
            List<Integer> mapIdCanGetAwardList = recordMapSearchTreasure.getAllMapIdsByState(SearchTreasureConstant.SEARCH_PROCESS_COMPLETE_NOGET);
            StringBuilder builder = new StringBuilder("");
            if (!mapIdCanGetAwardList.isEmpty()) {
                Iterator<Integer> iterator = mapIdCanGetAwardList.iterator();
                while (iterator.hasNext()) {
                    builder.append(iterator.next()).append("+");
                }
                redPointMap.put(RedPointConst.SEARCHTREASURE_MAPREWARD_CANGET, builder.toString().isEmpty() ? null : builder.toString());
            } else {
                redPointMap.put(RedPointConst.SEARCHTREASURE_MAPREWARD_CANGET, null);
            }
        }
    }


    public void updateRedPoints() {
        signCalRedPoint(MConst.SearchTreasure, RedPointConst.SEARCHTREASURE_MAPREWARD_CANGET);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        try {
            initRecordMap();
            updateRedPoints();
        } catch (Exception e) {
            com.stars.util.LogUtil.error("searchTreasure init error--》roleid:" + id(), e);
            if (reTryTime > 0) {
                com.stars.util.LogUtil.info("searchTreasure fix start:{} ", id());
                reTryTime--;
                String sql = "delete from rolerecords where roleid=" + id() + " and recordkey like 'searchtreasure.%';";
                DBUtil.execSql(DBUtil.DB_USER, sql);
                // 获取玩家的键值对记录
                sql = "select * from `rolerecords` where `roleid`=" + id();
                Map<String, RoleRecord> map = DBUtil.queryMap(DBUtil.DB_USER, "recordkey", RoleRecord.class, sql);
                if (map == null) {
                    map = new HashMap<>();
                }
                context().recordMap(new RoleRecordMapImpl(id(), context(), map));
                onInit(false);
            } else {
                LogUtil.error("searchTreasure roleid:{} retry time more than 3", id());
            }

        }

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        //进度清空
        getRecordMapSearchTreasure().onDailyReset();
        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(getRecordMapSearchTreasure());
        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_INFO);
        send(clientSearchTreasureInfo);
    }

//    /**
//     * 保存当前的信息;
//     */
//    public void saveCurrentInfo() {
//        //fixme 血量现在还没同步到服务端，所以无法获取到, 后面改为角色受伤害后通知服务端;
//        recordMapSearchTreasure.setRemainHp(500);
//        recordMapSearchTreasure.encodeData();
//    }

    public RecordMapSearchTreasure getRecordMapSearchTreasure() {
        return recordMapSearchTreasure;
    }

    public void fireEvent(Event event) {
        eventDispatcher().fire(event);
    }

    /**
     * 标明进入次数+1;
     */
    public void dispatchDailyEvent() {
        getRecordMapSearchTreasure().setHasSearchOnce();
        // 抛出日常活动事件
        eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_SEARCHTREASURE, 1));
        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(recordMapSearchTreasure);
        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_SYNC_DAILYCOUNT);
        send(clientSearchTreasureInfo);
    }

    public void setCurProducedMonsters(Map<String, FighterEntity> monsterMap) {
        recordMapSearchTreasure.setCurProducedMonsters(monsterMap);
    }

    public void removeCurProducedMonster(String monsterUid) {
        recordMapSearchTreasure.removeCurProducedMonster(monsterUid);
    }

    public boolean isInSearchTreasureScene() {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        if (sceneModule.getScene() instanceof SearchTreasureScene) {
            return true;
        }
        return false;
    }

    //复活;
    public void selfRevived() {
        if (!isInSearchTreasureScene()) {
            return;
        }
        //设置下剩余复活次数;
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        byte alreadyReviveNum = roleModule.getReviveNum(SceneManager.SCENETYPE_SEARCHTREASURE);
        recordMapSearchTreasure.updateAlreadyReliveCount();
        isSelfDead = false;
        recordMapSearchTreasure.setCacheItemStr(cachePreCacheAwards);
        recordMapSearchTreasure.setItems(cachePreItems);
    }

    //死亡;
    public void selfDead() {
        if (!isInSearchTreasureScene()) {
            return;
        }
        isSelfDead = true;
        cachePreCacheAwards = recordMapSearchTreasure.getCacheItemStr();
        recordMapSearchTreasure.setCacheItemStr("");
        cachePreItems = recordMapSearchTreasure.getItems();
        recordMapSearchTreasure.setItems("");
    }

    //回城;
    public void backCity() {
        if (isSelfDead) {
            //进度清空
            getRecordMapSearchTreasure().clearMapProcess();
            getRecordMapSearchTreasure().setMapId0();
            ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(getRecordMapSearchTreasure());
            clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_INFO);
            send(clientSearchTreasureInfo);
        }
        isSelfDead = false;
    }

    public void sendInfo() {
        ClientSearchTreasureInfo clientSearchTreasureInfo = new ClientSearchTreasureInfo(getRecordMapSearchTreasure());
        clientSearchTreasureInfo.setOprType(ClientSearchTreasureInfo.TYPE_INFO);
        send(clientSearchTreasureInfo);
    }

    @Override
    public void onOffline() throws Throwable {
        if (isSelfDead == false) {
            if (getRecordMapSearchTreasure().isWaitManualGetAward()) {
                getRecordMapSearchTreasure().manualGetAward();
            }
        }
        backCity();
    }
}
