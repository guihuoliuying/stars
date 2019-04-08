package com.stars.modules.soul;

import com.stars.modules.soul.prodata.SoulLevel;
import com.stars.modules.soul.prodata.SoulStage;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class SoulManager {
    /**
     * 《type,《level,soullevel》》
     */
    public static Map<Integer, Map<Integer, SoulLevel>> soulTypeMap;
    /**
     * 《stageid，soulstage》
     */
    public static Map<Integer, SoulStage> soulStageMap;
    /**
     * 《stage，《type，《level，soullevel》》》
     */
    public static Map<Integer, Map<Integer, Map<Integer, SoulLevel>>> soulStageLevelMap;

    /**
     * 最大元神阶级
     */
    public static Integer maxSoulStage;
}
