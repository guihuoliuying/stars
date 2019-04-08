package com.stars.modules.dungeon;

import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.dungeon.prodata.WorldinfoVo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class DungeonManager {
    /* 常量 */
    // 最高星级
    public static final byte STARMAX = 3;
    // 评星类型
    public static final byte STARTYPE_TIME = 1;// 时间类型
    public static final byte STARTYPE_KILLMONSTER = 2;// 击杀类型
    // 章节集星奖励
    public static final byte CHAPTER_STAR_NOT_REWARD = 1;// 未领取
    public static final byte CHAPTER_STAR_REWARDED = 2;// 已领取
    // 关卡状态
    public static final byte STAGE_OPEN = 1;// 已开启
    public static final byte STAGE_PASSED = 2;// 已通关
    // 初始章节Id
    public static final int INIT_CHAPTERID = 1;
    public static final int INIT_HERO_CHAPTERID = 101;
    public static final String HERO_STAGE_OPEN_TAG = "herostage";
    // 关卡序号初始值
    public static final byte INIT_STAGESTEP = 1;

    // 关卡产品数据 dungeonId-vo
    public static Map<Integer, DungeoninfoVo> dungeonVoMap = new HashMap<>();
    // 章节产品数据 worldId-vo
    public static Map<Integer, WorldinfoVo> chapterVoMap = new HashMap<>();
    // 章节-关卡产品数据 worldId-dungeonVos
    public static Map<Integer, Map<Byte, DungeoninfoVo>> chapterDungeonVoMap = new HashMap<>();
    // 需要条件解锁的关卡
    public static Map<Integer, DungeoninfoVo> lockDungeonVoMap = new HashMap<>();
    // 经验副本 产品数据 <type,<expDungeonId,vo>>
    public static Map<Byte, Map<Integer, ProduceDungeonVo>> produceDungeonVoMap = new HashMap<>();
    //根据worldStep拿到副本id
    public static Map<Integer, Integer> dungeonSepttoMap = new HashMap<>();
    public static Map<Integer, Integer> shigeDungeonSepettoMap = new HashMap<>();

    public static Map<Byte, DungeoninfoVo> getDungeonVoByWorldId(int chapterId) {
        return chapterDungeonVoMap.get(chapterId);
    }

    public static DungeoninfoVo getDungeonVoByStep(int chapterId, byte step) {
        Map<Byte, DungeoninfoVo> map = chapterDungeonVoMap.get(chapterId);
        return map == null ? null : map.get(step);
    }

    public static DungeoninfoVo getBossDungeonInfoVo(int worldId) {
        Map<Byte, DungeoninfoVo> worldDungeonVos = getDungeonVoByWorldId(worldId);
        if (worldDungeonVos != null) {
            byte maxStep = -1;
            for (DungeoninfoVo vo : worldDungeonVos.values()) {
                byte step = vo.getStep();
                if (step > maxStep) {
                    maxStep = step;
                }
            }

            return worldDungeonVos.get(maxStep);
        }

        return null;
    }

    public static DungeoninfoVo getDungeonVo(int dungeonId) {
        return dungeonVoMap.get(dungeonId);
    }

    public static WorldinfoVo getChapterVo(int chapterId) {
        return chapterVoMap.get(chapterId);
    }

    public static ProduceDungeonVo getProduceDungeonVo(byte type, int expDungeonId) {
        if (!produceDungeonVoMap.containsKey(type))
            return null;
        return produceDungeonVoMap.get(type).get(expDungeonId);
    }

    /**
     * 是否是英雄关卡
     *
     * @param dungeonId
     * @return
     */
    public static boolean isHeroStage(int dungeonId) {
        DungeoninfoVo dungeonVo = getDungeonVo(dungeonId);
        int worldId = dungeonVo.getWorldId();
        WorldinfoVo chapterVo = getChapterVo(worldId);
        return chapterVo.getSort() == 2;
    }
}
