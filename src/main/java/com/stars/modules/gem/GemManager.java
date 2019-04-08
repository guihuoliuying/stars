package com.stars.modules.gem;

import com.stars.modules.gem.prodata.GemHoleVo;
import com.stars.modules.gem.prodata.GemLevelVo;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 装备系统管理器;
 * Created by panzhenfeng on 2016/6/24.
 */
public class GemManager {
    private static HashMap<Byte, Integer> equipmentTishenMinLevelMap = new HashMap<>();
    private static HashMap<String, GemHoleVo> gemHoleMap = new HashMap<>();
    private static HashMap<Integer, GemLevelVo> gemLevelMap = new HashMap<>();
    /**
     * 用于记录装备的宝石槽的个数;
     */
    private static HashMap<Byte, Integer> equipmentGemHoleCountMap = new HashMap<>();


    public static String getEquipmentTishenKey(int level_, byte equipmentType_, int jobId_) {
        return level_ + "" + equipmentType_ + "" + jobId_;
    }

    public static String getGemHoleKey(byte equipmentType_, byte holeIndex_) {
        return equipmentType_ + "" + holeIndex_;
    }

    public static void setEquipmentTsihenMinLevelMap(byte tishenType, int level) {
        equipmentTishenMinLevelMap.put(tishenType, level);
    }

    public static void setGemHoleVoDatas(HashMap<String, GemHoleVo> gemHoleMap_, HashMap<Byte, Integer> equipmentGemHoleCountMap_) {
        gemHoleMap = gemHoleMap_;
        equipmentGemHoleCountMap = equipmentGemHoleCountMap_;
    }

    public static void setGemLevelVoDatas(HashMap<Integer, GemLevelVo> map) {
        gemLevelMap = map;
    }

    public static Object getEquipmentTishenVo(byte tishenType_, int level_, byte type_, int jobId_) {

        return null;
    }

    /**
     * 内部解析字符串返回vo数据;
     */
    public static Object getEquipmentTishenVo(byte tishenType_, byte equipmentType_, int jobId_, String equipmentTypeStr_) {
        String[] typeValueArr = null;
        switch (tishenType_) {
            case GemConstant.TAB_GEM:
                String[] gemIdArr = equipmentTypeStr_.split("\\+");
                if (gemIdArr != null) {
                    int gemLevelId = 0;
                    List<GemLevelVo> gemLevelVoList = new ArrayList<>();
                    String[] tmpArr = null;
                    for (int k = 0, klen = gemIdArr.length; k < klen; k++) {
                        tmpArr = gemIdArr[k].split(",");
                        if (tmpArr.length > 1) {
                            gemLevelId = Integer.parseInt(tmpArr[1]);
                            GemLevelVo gemLevelVo = GemManager.getGemLevelVo(gemLevelId);
                            if (gemLevelVo != null) {
                                gemLevelVoList.add(gemLevelVo);
                            }
                        }
                    }
                    return gemLevelVoList;
                }
                break;

        }
        return null;
    }

    public static GemHoleVo getGemHoleVo(byte equipType_, byte holeId_) {
        String tmpKey = getGemHoleKey(equipType_, holeId_);
        return gemHoleMap.get(tmpKey);
    }

    public static GemLevelVo getGemLevelVo(int gemLevelId_) {
        return gemLevelMap.get(gemLevelId_);
    }

    /**
     * 通过类型和等级来确定一条数据;
     *
     * @return
     */
    public static GemLevelVo getGemLevelVoByTypeLevel(byte type_, int level_) {
        Collection<GemLevelVo> gemLevelVoColl = gemLevelMap.values();
        for (GemLevelVo gemLevelVo : gemLevelVoColl) {
            if (gemLevelVo.getType() == type_ && gemLevelVo.getLevel() == level_) {
                return gemLevelVo;
            }
        }
        return null;
    }

    /**
     * 获取下一宝石等级的ids,没有的话自动跳过;
     *
     * @param gemLevelIds_
     * @return
     */
    public static List<Integer> getNextGemLevelIds(List<Integer> gemLevelIds_) {
        GemLevelVo gemLevelVo = null;
        List<Integer> rtnList = new ArrayList<>();
        for (int i = 0, len = gemLevelIds_.size(); i < len; i++) {
            gemLevelVo = getNextGemLevelId(gemLevelIds_.get(i));
            if (gemLevelVo != null) {
                rtnList.add(gemLevelVo.getItemId());
            }
        }
        return rtnList;
    }

    /**
     * 获取下一宝石等级的ids,没有的话自动跳过;
     *
     * @param gemLevelId
     * @return
     */
    public static GemLevelVo getNextGemLevelId(Integer gemLevelId) {
        GemLevelVo gemLevelVo = null;
        gemLevelVo = getGemLevelVo(gemLevelId);
        gemLevelVo = getGemLevelVoByTypeLevel(gemLevelVo.getType(), gemLevelVo.getLevel() + 1);
        return gemLevelVo;
    }

    public static boolean isGemLevelCanComposed(int gemLevelId) {
        GemLevelVo gemLevelVo = getGemLevelVo(gemLevelId);
        String composeMateral = gemLevelVo.getCompoundmaterial();
        if (StringUtil.isNotEmpty(composeMateral) && !composeMateral.equals("0")) {
            return true;
        }
        return false;
    }

    public static boolean isGemLevelCanLevelUp(int gemLevelId) {
        GemLevelVo gemLevelVo = getGemLevelVo(gemLevelId);
        String levelupmaterial = gemLevelVo.getLevelupmaterial();
        if (StringUtil.isNotEmpty(levelupmaterial) && !levelupmaterial.equals("0")) {
            return true;
        }
        return false;
    }

    public static int getGemHoleCount(byte equipmentType_) {
        return equipmentGemHoleCountMap.get(equipmentType_);
    }
}
