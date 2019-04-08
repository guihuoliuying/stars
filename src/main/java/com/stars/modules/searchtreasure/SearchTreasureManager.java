package com.stars.modules.searchtreasure;

import com.stars.modules.searchtreasure.prodata.SearchContentVo;
import com.stars.modules.searchtreasure.prodata.SearchMapVo;
import com.stars.modules.searchtreasure.prodata.SearchStageVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 仙山探宝系统管理器;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchTreasureManager {
    private static List<SearchMapVo> searchMaplist = null;
    private static HashMap<Integer, SearchStageVo> searchStageMap = null;
    private static HashMap<Integer, SearchContentVo> searchContentMap = null;
    private static int firstMapId;

    /**
     * 将探宝里的道具过滤出来;
     * @param awardMap
     * @return
     */
    public static Map<Integer, Integer> filterSearchTreasureItemIds(Map<Integer, Integer> awardMap){
        if(awardMap != null && awardMap.size() > 0){
            Map<Integer, Integer> rtnMap = new HashMap<>();
            Set<Integer> keySets = awardMap.keySet();
            ItemVo itemVo = null;
            for(Integer key : keySets){
                itemVo = ToolManager.getItemVo(key);
                if(itemVo.getType() == ToolManager.TYPE_SEARCHTREASURE){
                    rtnMap.put(key, awardMap.get(key));
                }
            }
            //将奖励从原有的map里移除;
            keySets = rtnMap.keySet();
            for(Integer key : keySets){
                awardMap.remove(key);
            }
            return rtnMap;
        }
        return null;
    }

    public static void setSearchMapList(List<SearchMapVo> list){
        searchMaplist = list;
    }

    public static int getSearchMapListCount(){
        return searchMaplist.size();
    }

    public static void setSearchStageMap(HashMap<Integer, SearchStageVo> map){
        searchStageMap = map;
    }

    public static void setSearchContentMap(HashMap<Integer, SearchContentVo> map){
        searchContentMap = map;
    }

    public static SearchMapVo getSearchMapVo(int mapId){
        for(int i = 0, len=searchMaplist.size(); i<len; i++){
            if (searchMaplist.get(i).getMapId()==mapId){
                return searchMaplist.get(i);
            }
        }
        return null;
    }

    public static  SearchMapVo getFirstSearchMapVo(){
        return searchMaplist.get(0);
    }

    public static SearchStageVo getSearchStageVo(int searchLayerId){
        return searchStageMap.get(searchLayerId);
    }

    public static SearchContentVo getSearchContentVo(int contentid){
        return searchContentMap.get(contentid);
    }
}
