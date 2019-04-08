package com.stars.multiserver.LootTreasure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 野外夺宝伤害排行榜;
 * Created by panzhenfeng on 2016/10/12.
 */
public class LTDamageRank {
    //内部使用,判断是否需要排序号;
    private boolean needSortIndex = false;
    private boolean hasChange = false;
    //维护的排好顺序的列表
    private List<LTDamageRankVo> ltDamageRankVoList = null;

    public LTDamageRank(){
        ltDamageRankVoList = new ArrayList<>();
    }

    //设置伤害值;
    public void setDamage(LTDamageRankVo ltDamageRankVo){
        needSortIndex = true;
        hasChange = true;
        //判断下是否有存储了;
        do{
            if (ltDamageRankVoList.contains(ltDamageRankVo)){
                break;
            }else{
                //判断下是否有名字一样的;
                for(int i = 0, len = ltDamageRankVoList.size(); i<len; i++){
                    if(ltDamageRankVoList.get(i).equals(ltDamageRankVo)){
                        //移除之前的同名DamageRankVo;
                        ltDamageRankVoList.remove(i);
                        break;
                    }
                }
            }
            ltDamageRankVoList.add(ltDamageRankVo);
        }while(false);
        //排序;
        Collections.sort(ltDamageRankVoList);
        ltDamageRankVo.setRank((short)ltDamageRankVoList.size());
    }

    public void sortIndex(){
        sortIndex(false);
    }
    //循环设置索引,即LTDamageRankVo里的rank字段;
    public void sortIndex(boolean force){
        if(needSortIndex || force){
            for(int i = 0, len = ltDamageRankVoList.size(); i<len; i++){
                ltDamageRankVoList.get(i).setRank((short)(i+1));
            }
            needSortIndex = false;
        }
    }

    //获取开始的几个名次数据;
    public List<LTDamageRankVo> getFirstList(int count){
        List<LTDamageRankVo> rtnList = new ArrayList<>();
        int index = -1;
        int size = ltDamageRankVoList.size()-1;
        while(index++ < size && (count-- > 0)){
            rtnList.add(ltDamageRankVoList.get(index));
        }
        return rtnList;
    }

    public List<LTDamageRankVo> getLtDamageRankVoList(){
        return ltDamageRankVoList;
    }


    public boolean isHasChange() {
        return hasChange;
    }

    public void setHasChange(boolean hasChange) {
        this.hasChange = hasChange;
    }
}
