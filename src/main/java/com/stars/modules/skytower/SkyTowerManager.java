package com.stars.modules.skytower;

import com.stars.modules.skytower.prodata.SkyTowerVo;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 镇妖塔的管理器;
 * Created by panzhenfeng on 2016/8/10.
 */
public class SkyTowerManager {
    private static List<SkyTowerVo> challengeSkyTowerVoList = null;
    private static HashMap<Integer, SkyTowerVo> skytowerVoMap = null;
    private static HashMap<Integer, SkyTowerVo> skyTowerVoByLayerSerialHashMap = null;
    //初始的层id;
    private static int initLayerId = 0;
    //最大的层id;
    private static int maxLayerId = 0;

    public static void setSkyTowerMapData(HashMap<Integer, SkyTowerVo> map) {
        skytowerVoMap = map;
    }

    public static void setSkyTowerVoByLayerSerialHashMap(HashMap<Integer, SkyTowerVo> skyTowerVoByLayerSerialHashMap) {
        SkyTowerManager.skyTowerVoByLayerSerialHashMap = skyTowerVoByLayerSerialHashMap;
    }

    public static void setChallengeSkyTowerVoList(List<SkyTowerVo> list){
        challengeSkyTowerVoList = list;
    }

    /**
     * 获取从layerId开始的下一个含有挑战奖励的数据;
     * @param layerId
     * @return
     */
    public static SkyTowerVo getNextChallengeSkyTowerVo(int layerId, boolean isContainSelf){
        SkyTowerVo skyTowerVo = null;
        for(int i = 0, len = challengeSkyTowerVoList.size(); i<len; i++){
            skyTowerVo = challengeSkyTowerVoList.get(i);
            if(StringUtil.isNotEmpty(skyTowerVo.getChallengeSucReward()) && !skyTowerVo.getChallengeSucReward().equals("0")){
                if(isContainSelf){
                    if(skyTowerVo.getLayerId() >= layerId){
                        return  skyTowerVo;
                    }
                }else{
                    if(skyTowerVo.getLayerId() > layerId){
                        return  skyTowerVo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 是否是挑战层;
     * @param startLayerId
     * @param endLayerId
     * @param isContainStartLayerId 是否包含开始的层id;
     * @return
     */
    public static List<Integer> isHasChallengeLayerBetween(int startLayerId, int endLayerId, boolean isContainStartLayerId){
        SkyTowerVo skyTowerVo = null;
        List<Integer> rtnList = new ArrayList<>();
        int convertStartLayerId = startLayerId;
        if(isContainStartLayerId == false){
            convertStartLayerId += 1;
        }
        for (int i = convertStartLayerId; i<=endLayerId; i++){
            skyTowerVo = getSkyTowerById(i);
            if(skyTowerVo.isChallengeLayer()){
                rtnList.add(i);
            }
        }
        return rtnList;
    }


    public static SkyTowerVo getSkyTowerById(int id){
        return skytowerVoMap.get(id);
    }

    public static SkyTowerVo getSkyTowerByLayerSerialId(int layerSerialId){
        return skyTowerVoByLayerSerialHashMap.get(layerSerialId);
    }


    public static void setInitLayerId(int id){
        initLayerId = id;
    }

    public static int getInitLayerId() {
        return initLayerId;
    }

    public static int getMaxLayerId() {
        return maxLayerId;
    }

    public static void setMaxLayerId(int maxLayerId) {
        SkyTowerManager.maxLayerId = maxLayerId;
    }
}
