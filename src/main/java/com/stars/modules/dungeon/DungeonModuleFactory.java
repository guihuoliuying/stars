package com.stars.modules.dungeon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.dungeon.gm.ActiveDungeonGmHandler;
import com.stars.modules.dungeon.gm.DungeonPassAllGmHandler;
import com.stars.modules.dungeon.gm.ResetCountGmHandler;
import com.stars.modules.dungeon.listener.ActiveDungeonListener;
import com.stars.modules.dungeon.listener.DungeonListenner;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.dungeon.prodata.WorldinfoVo;
import com.stars.modules.dungeon.summary.DungeonSummaryComponentImpl;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.gm.GmManager;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.services.summary.Summary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class DungeonModuleFactory extends AbstractModuleFactory<DungeonModule> {

    public DungeonModuleFactory() {
        super(new DungeonPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        loadDungeoninfoVo();
        loadWorldinfoVo();
        loadExpDungeonVo();
    }

    @Override
    public void init() {
        GmManager.reg("dungeon.passall", new DungeonPassAllGmHandler());
        GmManager.reg("dungeon.activeall", new ActiveDungeonGmHandler());
        GmManager.reg("dungeon.resetcount", new ResetCountGmHandler());

        GmManager.reg("resetdungeoncount", new ResetCountGmHandler());
        GmManager.reg("activedungeonall", new ActiveDungeonGmHandler());

        Summary.regComponentClass("dungeon", DungeonSummaryComponentImpl.class);
    }

    @Override
    public DungeonModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new DungeonModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        DungeonListenner dungeonListenner = new DungeonListenner((DungeonModule) module);
        eventDispatcher.reg(RoleLevelUpEvent.class, new ActiveDungeonListener((DungeonModule) module));
        eventDispatcher.reg(ForeShowChangeEvent.class, dungeonListenner);
    }

    /**
     * 加载dungeoninfo产品数据
     *
     * @throws Exception
     */
    private void loadDungeoninfoVo() throws Exception {
        String sql = "select * from `dungeoninfo`;";
        Map<Integer, DungeoninfoVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "dungeonid", DungeoninfoVo.class, sql);

        Map<Integer, Map<Byte, DungeoninfoVo>> worldDungeonVoMap = new HashMap<>();
        Map<Integer, DungeoninfoVo> lockDungeonVoMap = new HashMap<>();
        Map<Integer, Integer> dungeonSepttoMap = new HashMap<>();
        Map<Integer, Integer> shigeDungeonSepettoMap = new HashMap<>();
        for (DungeoninfoVo vo : map.values()) {
            Map<Byte, DungeoninfoVo> stepMap = worldDungeonVoMap.get(vo.getWorldId());
            if (stepMap == null) {
                stepMap = new HashMap<>();
                worldDungeonVoMap.put(vo.getWorldId(), stepMap);
            }
            stepMap.put(vo.getStep(), vo);
            if (vo.getReqRoleLevel() != 0 || vo.getReqDungeonId() != 0) {
                lockDungeonVoMap.put(vo.getDungeonId(), vo);
            }
            if (vo.getBossIcon() == 0) {//key = world*1000+step
                dungeonSepttoMap.put(vo.getWorldId() * 1000 + vo.getStep(), vo.getDungeonId());
            } else if (vo.getBossIcon() == 1) {
                shigeDungeonSepettoMap.put(vo.getWorldId() * 1000 + vo.getStep(), vo.getDungeonId());
            }
        }
        DungeonManager.dungeonVoMap = map;
        DungeonManager.dungeonSepttoMap = dungeonSepttoMap;
        DungeonManager.shigeDungeonSepettoMap = shigeDungeonSepettoMap;
        DungeonManager.chapterDungeonVoMap = worldDungeonVoMap;
        DungeonManager.lockDungeonVoMap = lockDungeonVoMap;
    }

    /**
     * 加载worldinfo产品数据
     *
     * @throws Exception
     */
    private void loadWorldinfoVo() throws Exception {
        String sql = "select * from `worldinfo`;";
        Map<Integer, WorldinfoVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "worldid", WorldinfoVo.class, sql);
        DungeonManager.chapterVoMap = map;
    }

    /**
     * 加载expdungeon
     *
     * @throws SQLException
     */
    private void loadExpDungeonVo() throws SQLException {
        String sql = "select * from `producedungeon`; ";
        List<ProduceDungeonVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, ProduceDungeonVo.class, sql);
        Map<Byte, Map<Integer, ProduceDungeonVo>> map = new HashMap<>();
        for (ProduceDungeonVo vo : list) {
            if (!map.containsKey(vo.getType()))
                map.put(vo.getType(), new HashMap<Integer, ProduceDungeonVo>());
            map.get(vo.getType()).put(vo.getProduceDungeonId(), vo);
        }
        DungeonManager.produceDungeonVoMap = map;
    }
}
