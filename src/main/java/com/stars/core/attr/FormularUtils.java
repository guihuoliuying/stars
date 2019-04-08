package com.stars.core.attr;

import com.stars.modules.data.DataManager;
import com.stars.util.StringUtil;

/**
 * 公式相关的类;
 * Created by panzhenfeng on 2016/7/25.
 */
public class FormularUtils {

    /**
     * @param attrName_
     * @param attrValue_
     * @return
     */
    public static int calcFightScoreByAttr(String attrName_, int attrValue_) {
        return (int) Math.ceil(attrValue_ *
                (!DataManager.battlePowerRationDic.containsKey(attrName_) ?
                        0 : DataManager.battlePowerRationDic.get(attrName_)));
    }

    /**
     * 计算战力
     *
     * @param attribute
     * @return
     */
    public static int calFightScore(Attribute attribute) {
        if(attribute == null) return 0;
        int total = 0;
        for (byte i = 0; i < attribute.getAttributes().length; i++) {
            String attrName = Attr.getAttrNameByIndex(i);
            total += (int) Math.ceil(attribute.getAttributes()[i] *
                    (!DataManager.battlePowerRationDic.containsKey(attrName) ?
                            0 :DataManager.battlePowerRationDic.get(attrName)));
        }
        return total;
    }

    /**
     * 计算寻宝的复活花费; TODO 这里还没做区分是什么类型的货币;
     */
    public static int calSearchTreasureRelivePrice(int extendReliveCount) {
        String[] tmpArr = DataManager.getCommConfig("searchtreasure_reborncost").split("\\+");
        int itemId = Integer.parseInt(tmpArr[0]);
        int count = Integer.parseInt(tmpArr[1]);
        return itemId * count;
    }

    /**
     * 计算点是否在圆内;
     *
     * @param cx     圆中心x;
     * @param cy     圆中心y;
     * @param radius 圆半径;
     * @param px     点x;
     * @param py     点y;
     * @return
     */
    public static boolean isPointInCircle(float cx, float cy, float radius, float px, float py) {
        return ((px - cx) * (px - cx) + (py - cy) * (py - cy)) <= radius * radius;
    }

    public static String formatColorString(String str, int colorQuality){
        String color = DataManager.getQualityColor(colorQuality);
        if(!StringUtil.isNotEmpty(color)){
            color = DataManager.getQualityColor(DataManager.qualityColorMap.size());
        }
        return "<color="+color+">"+str+"</color>";
    }

}
