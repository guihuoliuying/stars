package com.stars.modules.scene;

import com.stars.modules.scene.prodata.*;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by liuyuheng on 2016/7/19.
 */
public class SceneManager {
    // 场景管理器集合
    private static Map<Byte, Class<? extends Scene>> sceneInstans = new HashMap<>();
    /**
     * 场景类型,非城镇场景对应stageinfo表type字段
     */
    public final static byte SCENETYPE_FIGHTPK = -99;           //代表进入战斗服
    public final static byte SCENETYPE_CITY = 0;           //城镇场景
    public final static byte SCENETYPE_DUNGEON = 1;        //一般副本(主线副本)
    public final static byte SCENETYPE_SKYTOWER = 2;       //镇妖塔
    public final static byte SCENETYPE_SEARCHTREASURE = 3; //仙山夺宝;
    public final static byte SCENETYPE_PRODUCEDUNGEON = 4;// 产出副本(经验/银币),根据内部类型区分
    public final static byte SCENETYPE_CALLBOSS = 5;// 召唤BOSS
    public final static byte SCENETYPE_GAMECAVE = 6;// 洞府(小游戏集合的场景);
    public final static byte SCENETYPE_TEAMDUNGEON = 7;// 组队副本;
    public final static byte SCENETYPE_FAMILY_EXPEDITION = 8; // 家族远征
    public final static byte SCENETYPE_FAMIL = 9;   // 家族领地场景
    public final static byte SCENETYPE_LOOTTREASURE_WAIT = 10;// 野外夺宝Wait;
    public final static byte SCENETYPE_LOOTTREASURE_PVE = 11;// 野外夺宝PVE;
    public final static byte SCENETYPE_OFFLINEPVP = 12;// 离线pvp(演武场)
    public final static byte SCENETYPE_LOOTTREASURE_PVP = 13;// 野外夺宝PVP;
    public final static byte SCENETYPE_FAMILY_INVADE = 14;// 家族入侵
    public final static byte SCENETYPE_BRAVE_STAGE = 15;// 勇者试炼关卡
    public final static byte SCENETYPE_FIGHTINGMASTER = 16; // 斗神殿
    public final static byte SCENETYPE_MASTER_NOTICE_STAGE = 17;// 皇榜悬赏关卡
    public final static byte SCENETYPE_TPG = 18;//组队PVP赛
    public final static byte SCENETYPE_ESCORT_SAFE = 19;    // 押镖队列场景
    public final static byte SCENETYPE_FAMILY_WAR_ELITE_FIGHT = 20; // 家族精英战
    public final static byte SCENETYPE_ESCORT_FIGHT = 21;// 押镖战斗场景
    public final static byte SCENETYPE_WEDDING = 22;    // 豪华婚礼场景
    public final static byte SCENETYPE_ROB_ROBOT = 23;    // 机器人镖车关卡
    public final static byte SCENETYPE_FAMILY_WAR_NORMAL_FIGHT = 24; // 家族匹配战
    public final static byte SCENETYPE_POEM = 25; // 诗歌关卡
    public final static byte SCENETYPE_FAMILY_WAR_STAGE_FIGTH = 26;	//家族匹配关卡
    public final static byte SCENETYPE_1V1PK = 100;//pk
    public final static byte SCENETYPE_NEWGUIDE = 101;//新手副本
    public final static byte SCENETYPE_FAMILY_TREASURE = 27;//家族探宝普通关卡
    public final static byte SCENETYPE_FAMILY_TREASURE_SUNDAY = 28;//家族探宝周日关卡
    public final static byte SCENETYPE_ELITEDUNGEON = 29;//组队精英副本
    public final static byte SCENETYPE_NEWOFFLINEPVP = 30;//新版竞技场
	public final static byte SCENETYPE_FAMILY_TASK = 31;//家族任务关卡
    public final static byte SCENETYPE_FAMILY_WAR_SAFE_SCENE = 32;//家族战备战场景
    public final static byte SCENETYPE_FAMILY_ESCORT_SAFE_SCENE = 33;//家族运镖的安全区
    public final static byte SCENETYPE_FAMILY_ESCORT_PVP_SCENE = 34;//家族运镖的PVP场景
    public final static byte SCENETYPE_DAILY_5V5 = 35;//日常5v5
    public final static byte SCENETYPE_POEM_DUNGEON = 36;//诗歌副本组队场景
    public final static byte SCENETYPE_MARRY_DUNGEON = 38; //情义副本
    public final static byte SCENETYPE_BUDDY_DUNGEON = 39;//伙伴关卡
    public final static byte SCENETYPE_OPACT_BENEFIT_TOKEN_DUNGEON = 40;// 符文装备体验关卡
    public final static byte SCENETYPE_RUNE_DUNGEON = 41;// 挑战副本（符文副本）
    public final static byte SCENETYPE_CAMP_CITY_FIGHT = 42;// 齐楚之争
    public final static byte SCENETYPE_GUARD_OFFICIAL = 43;//守护官职
    public final static byte SCENETYPE_CAMP_FIGHT = 44;//秦楚大作战
    public final static byte SCENETYPE_DARE_GOD = 45;//挑战女神


    /**
     * 常量
     */
    public static int initSafeStageId;// 初始安全区场景Id
    // npc所在场景类型
    public static final byte NPC_CITY_TYPE = 1;
    public static final byte NPC_FIGHT_TYPE = 2;
    // 关卡状态
    public static final byte STAGE_FAIL = 0;
    public static final byte STAGE_PROCEEDING = 1;
    public static final byte STAGE_VICTORY = 2;
    public static final byte STAGE_PAUSE = 3;// 暂停状态(剧情/引导需要)
    // 刷怪类型
    public static final byte SPAWNTYPE_INIT = 1;// 默认刷怪
    public static final byte SPAWNTYPE_AREA = 2;// 区域触发刷怪
    // 动态阻挡状态
    public static final byte BLOCK_CLOSE = 0;// 关闭
    public static final byte BLOCK_OPEN = 1;// 开启
    // 怪物状态
    public static final byte MONSTER_ALIVE = 1;// 存活
    public static final byte MONSTER_DEAD = 2;// 死亡
    // 怪物buff等级
    public static final int MONSTER_BUFF_LEVEL = 1;
    // 胜利条件类型
    public static final byte VICTORY_CONDITION_KILL_ALL = 1;// 击杀所有
    public static final byte VICTORY_CONDITION_KILL_APPOINT = 2;// 击杀指定 胜利
    public static final byte VICTORY_CONDITION_TIME = 3;// 一定时间后
    public static final byte VICTORY_CONDITION_KILL_BOSS = 4;// 击杀boss类型怪物
    public static final byte VITORY_CONDITION_MARRY_BATTLE_SCORE = 6; //情义副本积分
    // 失败条件类型
    public static final byte FAIL_CONDITION_SELFDEAD = 1;// 自身死亡
    public static final byte FAIL_CONDITION_TIME = 2;// 一定时间后
    public static final byte FAIL_CONDITION_KILL_APPOINT = 3;// 击杀指定 失败
    // 资源产出副本类型
    public static final byte PRODUCE_ROLEEXP = 1;// 角色经验
    public static final byte PRODUCE_STRENGTHEN_STONE = 2;// 强化石
    public static final byte PRODUCE_RIDEFOOD = 3;//驯化丹
    // 剧情类型
    public static String DRAMA_SAFE = "safe";// 安全区场景
    public static String DRAMA_STAGE = "stage";// 战斗场景
    public static String DRAMA_TASK = "task";// 任务

    /** 刷新周围玩家场景前缀 */
    public static final String ARROUND_SCENE_PREFIX = "scene";

    // 安全区产品数据 safeId-vo
    public static Map<Integer, SafeinfoVo> safeVoMap = new HashMap<>();
    // 战斗场景产品数据 stageId-vo
    public static Map<Integer, StageinfoVo> stageVoMap = new HashMap<>();
    // npc产品数据 npcid-npcvo
    public static Map<Integer, NpcInfoVo> npcVoMap = new HashMap<>();
    /* npc分布数据 */
    // 城镇场景npc areaType=1; safeId-list<NpcVo>
    public static Map<Integer, List<NpcInfoVo>> cityNpcMap = new HashMap<>();
    // 战斗场景npc areaType=2; stageId-List<NpcVo>
    public static Map<Integer, List<NpcInfoVo>> fightNpcMap = new HashMap<>();

    private static Map<String, String> defineMap;
    // 怪物voMap monsterId-vo
    public static Map<Integer, MonsterVo> monsterVoMap = new HashMap<>();
    // 怪物属性voMap stageMonsterId-vo
    public static Map<Integer, MonsterAttributeVo> monsterAttributeVoMap = new HashMap<>();
    // 刷怪voMap monsterSpawnId-vo
    public static Map<Integer, MonsterSpawnVo> monsterSpawnVoMap = new HashMap<>();
    // buffVo <buffId,level-vo>
    public static Map<Integer, Map<Integer, BuffVo>> buffVoMap = new HashMap<>();
    // 剧情数据
    public static Map<String, DramaConfig> dramaMap = new HashMap<>();
    // 阵营关系数据
    public static Map<String, CampVo> campVoMap = new HashMap<>();
    // 复活次数限制 <stageType, vo>
    public static Map<Byte, ReviveConfig> reviveConfigMap = new HashMap<>();

    public static void register(byte sceneType, Class<? extends Scene> clazz) {
        if (sceneInstans.containsKey(sceneType)) {
            throw new IllegalArgumentException("场景管理类已存在");
        }
        sceneInstans.put(sceneType, clazz);
    }

    public static SafeinfoVo getSafeVo(int safeId) {
        return safeVoMap.get(safeId);
    }

    public static StageinfoVo getStageVo(int stageId) {
        return stageVoMap.get(stageId);
    }

    public static List<NpcInfoVo> getNpcVoBySceneId(int sceneId) {
        List<NpcInfoVo> list = cityNpcMap.get(sceneId);
        if (list != null) {
            return list;
        }
        return fightNpcMap.get(sceneId);
    }

    public static Scene newScene(byte sceneType) {
        try {
        	Class<? extends Scene> scene = sceneInstans.get(sceneType);
        	if (scene == null) return null;
            return scene.newInstance();
        } catch (InstantiationException e) {
            com.stars.util.LogUtil.error("获得战斗场景handler失败", e);
        } catch (IllegalAccessException e) {
            LogUtil.error("获得战斗场景handler失败", e);
        }
        return null;
    }

    public static void setFcdMap(Map<Integer, Fcd> defineMap) {
        if (defineMap != null) {
            Map<String, String> dataMap = new HashMap<>();
            Set<Map.Entry<Integer, Fcd>> set = defineMap.entrySet();
            Fcd vo;
            for (Map.Entry<Integer, Fcd> entry : set) {
                vo = entry.getValue();
                dataMap.put(vo.getParameter(), vo.getValue());
            }
            SceneManager.defineMap = dataMap;
        }
    }

    public static Map<String, String> getFcdMap() {
        if (defineMap == null)
            throw new NullPointerException("FcdMap");
        return defineMap;
    }

    public static MonsterVo getMonsterVo(int monsterId) {
        return monsterVoMap.get(monsterId) == null ? null : monsterVoMap.get(monsterId).copy();
    }

    public static MonsterAttributeVo getMonsterAttrVo(int stageMonsterId) {
        return monsterAttributeVoMap.get(stageMonsterId) == null ? null : monsterAttributeVoMap.get(stageMonsterId).copy();
    }

    public static MonsterSpawnVo getMonsterSpawnVo(int monsterSpawnId) {
        return monsterSpawnVoMap.get(monsterSpawnId);
    }

    public static BuffVo getBuffVo(int buffId, int level) {
        Map<Integer, BuffVo> buffLevelMap = buffVoMap.get(buffId);
        if (buffLevelMap == null) {
            return null;
        }
        return buffLevelMap.get(level);
    }

    public static DramaConfig getDramaConfig(String dramaId) {
        return dramaMap.get(dramaId);
    }

    public static ReviveConfig getReviveConfig(byte stageType) {
        return reviveConfigMap.get(stageType);
    }

    public static NpcInfoVo getNpcVo(int npcId) {
        return npcVoMap.get(npcId);
    }
}
