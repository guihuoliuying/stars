package com.stars.modules.scene;

import com.stars.core.annotation.DependOn;
import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.scene.event.RequestExitFightEvent;
import com.stars.modules.scene.gm.CompleteGmHandler;
import com.stars.modules.scene.gm.EnterStageGmHandler;
import com.stars.modules.scene.imp.city.EscortSafeScene;
import com.stars.modules.scene.imp.city.FamilyScene;
import com.stars.modules.scene.imp.city.SafeCityScene;
import com.stars.modules.scene.imp.fight.DungeonScene;
import com.stars.modules.scene.imp.fight.NewGuideScene;
import com.stars.modules.scene.imp.fight.PkScene;
import com.stars.modules.scene.imp.fight.ProduceDungeonScene;
import com.stars.modules.scene.listener.RequestExitFightListener;
import com.stars.modules.scene.prodata.*;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/19.
 */
@DependOn({MConst.Data, MConst.Skill})
public class SceneModuleFactory extends AbstractModuleFactory<SceneModule> {
    public SceneModuleFactory() {
        super(new ScenePacketSet());
    }

    @Override
    public SceneModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new SceneModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        /* scene handler register */
        SceneManager.register(SceneManager.SCENETYPE_CITY, SafeCityScene.class);
        /**单人PVE关卡;*/
        SceneManager.register(SceneManager.SCENETYPE_DUNGEON, DungeonScene.class);
        /**镇妖塔;*/
        /**PK;*/
        SceneManager.register(SceneManager.SCENETYPE_1V1PK, PkScene.class);
        /** 经验副本 */
        SceneManager.register(SceneManager.SCENETYPE_PRODUCEDUNGEON, ProduceDungeonScene.class);

        /** 家族篝火 */
        SceneManager.register(SceneManager.SCENETYPE_FAMIL, FamilyScene.class);
        /** 新手场景 */
        SceneManager.register(SceneManager.SCENETYPE_NEWGUIDE, NewGuideScene.class);
        /** 押镖队列场景 */
        SceneManager.register(SceneManager.SCENETYPE_ESCORT_SAFE, EscortSafeScene.class);
        /* gm regsiter */
        GmManager.reg("stagecomplete", new CompleteGmHandler());
        GmManager.reg("enterstage", new EnterStageGmHandler());
    }

    @Override
    public void loadProductData() throws Exception {
        loadSafeinfoVo();
        loadNpcVo();
        loadFcd();
        loadMonster();
        loadBuffVo();
        loadStageinfoVo();// 依赖怪物数据,必须放在其后加载
        loadDrama();// 依赖怪物和技能数据,必须放在其后加载
        loadCampVo();
        loadCommondefine();
    }

    private void loadStageinfoVo() throws Exception {
        String sql = "select * from `stageinfo`;";
        Map<Integer, StageinfoVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "stageid", StageinfoVo.class, sql);
        SceneManager.stageVoMap = map;
    }

    /**
     * 加载safeinfo
     *
     * @throws Exception
     */
    private void loadSafeinfoVo() throws Exception {
        String sql = "select * from `safeinfo`; ";
        Map<Integer, SafeinfoVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "safeid", SafeinfoVo.class, sql);
        SceneManager.safeVoMap = map;
        String temp = DataManager.getCommConfig("safe_default");
        if (StringUtil.isEmpty(temp) || "0".equals(temp) || !StringUtil.isNumeric(temp)) {
            throw new IllegalArgumentException("初始安全区配置错误,请检查[commondefine]表[safe_default]字段");
        }
        SceneManager.initSafeStageId = Integer.parseInt(temp);
    }

    private void loadNpcVo() throws Exception {
        String sql = "select * from `npcinfo` where `functions` != '0' or `display` != '0';";
        Map<Integer, NpcInfoVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "npcid", NpcInfoVo.class, sql);
        if (map == null) {
            map = new HashMap<>();
        }
        SceneManager.npcVoMap = map;
        Map<Integer, List<NpcInfoVo>> cityNpcMap = new HashMap<>();
        Map<Integer, List<NpcInfoVo>> fightNpcMap = new HashMap<>();
        List<NpcInfoVo> list = new LinkedList<>();
        for (NpcInfoVo vo : map.values()) {
            switch (vo.getAreaType()) {
                case SceneManager.NPC_CITY_TYPE:
                    list = cityNpcMap.get(vo.getAreaId());
                    if (list == null) {
                        list = new LinkedList<>();
                        cityNpcMap.put(vo.getAreaId(), list);
                    }
                    break;
                case SceneManager.NPC_FIGHT_TYPE:
                    list = fightNpcMap.get(vo.getAreaId());
                    if (list == null) {
                        list = new LinkedList<>();
                        fightNpcMap.put(vo.getAreaId(), list);
                    }
                    break;
                default:
                    break;
            }
            list.add(vo);
        }
        SceneManager.cityNpcMap = cityNpcMap;
        SceneManager.fightNpcMap = fightNpcMap;
    }


    public void loadFcd() throws SQLException {
        String sql = "select * from fcd;";
        Map<Integer, Fcd> fcdMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "parameter", Fcd.class, sql);
        SceneManager.setFcdMap(fcdMap);
    }

    /**
     * 加载怪物相关
     * 存在依赖关系,必须使用这个顺序加载
     */
    private void loadMonster() throws SQLException {
        loadMonsterVo();
        loadMonsterAttribute();
        loadMonsterSpawn();
    }

    private void loadMonsterVo() throws SQLException {
        String sql = "select * from `monster`; ";
        Map<Integer, MonsterVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", MonsterVo.class, sql);
        SceneManager.monsterVoMap = map;
    }

    private void loadMonsterAttribute() throws SQLException {
        String sql = "select * from `monsterattribute`; ";
        Map<Integer, MonsterAttributeVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "stagemonsterid",
                MonsterAttributeVo.class, sql);
        SceneManager.monsterAttributeVoMap = map;
    }

    private void loadMonsterSpawn() throws SQLException {
        String sql = "select * from `monsterspawn`; ";
        Map<Integer, MonsterSpawnVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "monsterspawnid",
                MonsterSpawnVo.class, sql);
        SceneManager.monsterSpawnVoMap = map;
    }

    private void loadBuffVo() throws Exception {
        String sql = "select * from `buff`; ";
        List<BuffVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, BuffVo.class, sql);
        Map<Integer, Map<Integer, BuffVo>> buffVoMap = new HashMap<>();
        for (BuffVo buffVo : list) {
            Map<Integer, BuffVo> map = buffVoMap.get(buffVo.getBuffId());
            if (map == null) {
                map = new HashMap<>();
                buffVoMap.put(buffVo.getBuffId(), map);
            }
            map.put(buffVo.getBuffLv(), buffVo);
        }
        SceneManager.buffVoMap = buffVoMap;
    }

    private void loadDrama() throws Exception {
        String sql = "select * from `drama`; ";
        List<DramaVo> dramaVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, DramaVo.class, sql);
        Map<String, DramaConfig> dramaMap = new HashMap<>();
        for (DramaVo dramaVo : dramaVoList) {
            DramaConfig dramaConfig = dramaMap.get(dramaVo.getDramaId());
            if (dramaConfig == null) {
                dramaConfig = new DramaConfig(dramaVo.getDramaId());
                dramaMap.put(dramaVo.getDramaId(), dramaConfig);
            }
            String funcName = dramaVo.getFunc().trim();
            String param = dramaVo.getParameter().trim();
            if ("npccreate".equals(funcName)) {// 需要怪物vo
                MonsterVo monsterVo = SceneManager.getMonsterVo(Integer.parseInt(param.split(",")[1]));
                if (monsterVo == null) {
                    throw new IllegalArgumentException("drama表配置npccreat怪物数据不存在monsterid=" + param.split(",")[1]);
                }
                dramaConfig.getMonsterVoMap().put(monsterVo.getId(), monsterVo);
            }
            if ("dofire".equals(funcName)) {// 需要技能vo
                SkillVo skillVo = SkillManager.getSkillVo(Integer.parseInt(param.split(",")[1]));
                if (skillVo == null) {
                    throw new IllegalArgumentException("drama表配置dofire技能数据不存在skillid=" + param.split(",")[1]);
                }
                dramaConfig.getSkillVoMap().put(skillVo.getSkillid(), skillVo);
            }
        }
        SceneManager.dramaMap = dramaMap;
    }

    private void loadCampVo() throws SQLException {
        String sql = "select * from `camp`;";
        Map<String, CampVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "starter", CampVo.class, sql);
        SceneManager.campVoMap = map;
    }

    private void loadCommondefine() throws Exception {
        Map<Byte, ReviveConfig> map = new HashMap<>();
        String teamRevive = DataManager.getCommConfig("playerteam_reborn");
        map.put(SceneManager.SCENETYPE_TEAMDUNGEON, parseReviveConfig(SceneManager.SCENETYPE_TEAMDUNGEON, teamRevive));
        String eliteRevive = DataManager.getCommConfig("elitedungeon_reborn");
        map.put(SceneManager.SCENETYPE_ELITEDUNGEON, parseReviveConfig(SceneManager.SCENETYPE_ELITEDUNGEON, eliteRevive));
        String towerRevive = DataManager.getCommConfig("searchtreasure_reborn");
        map.put(SceneManager.SCENETYPE_SEARCHTREASURE, parseReviveConfig(SceneManager.SCENETYPE_SEARCHTREASURE, towerRevive));
        String marryRevive = DataManager.getCommConfig("marrybattle_reborn", "3+3|2+10,2+10,2+20");
        map.put(SceneManager.SCENETYPE_MARRY_DUNGEON, parseReviveConfig(SceneManager.SCENETYPE_MARRY_DUNGEON, marryRevive));

        SceneManager.reviveConfigMap = map;
    }

    private ReviveConfig parseReviveConfig(byte stageType, String reviveStr) throws Exception {
        String[] temp = reviveStr.split("\\|");
        byte[] number = StringUtil.toArray(temp[0], byte[].class, '+');
        byte index = 1;
        Map<Byte, Map<Integer, Integer>> map = new HashMap<>();
        for (String delta : temp[1].split(",")) {
            Map<Integer, Integer> itemMap = StringUtil.toMap(delta, Integer.class, Integer.class, '+', ',');
            map.put(index, itemMap);
            index++;
        }
        return new ReviveConfig(stageType, number[0], number[1], map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(RequestExitFightEvent.class, new RequestExitFightListener(module));
    }
}
