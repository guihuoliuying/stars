package com.stars.core.attr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展属性，用于映射属性名一对多的关系
 * 暂时只考虑一个属性名字最多映射出10个属性，
 * 如有特殊需求不够用，建议考虑水平扩展：例如属性2
 * 与策划约定：
 *  1. 策划扩展属性index 从100开始
 *  2. 属性名 以ext开头
 *  3. 中文属性名 以“扩展”开头
 *  4. 策划配置index个位数字需要与属性名最后数字匹配，例如：100=1 等价 extpenet0=1
 * Created by zhouyaohui on 2016/10/12.
 */
public enum AttrExt {

    PENET(0, "extpenetrate", "扩展穿透"),
    RESIS(1, "extresistance", "扩展抗性"),
    ;
    /** indexId */
    private int indexId;
    /** 属性名 */
    private String attrName;
    /** 名字 */
    private String name;

    //英文所对应的下标
    //key:attact  value:1
    private static ConcurrentHashMap<String, Integer> EN_INDEX_MAP =
            new ConcurrentHashMap<String, Integer>();

    //下标与中文对应
    //key:attact  value:攻击力
    private static Map<Integer, String> nameMap =
            new HashMap<>();

    //下标与英文对应
    private static Map<Integer,String> INDEX_NAME_MAP = new HashMap<>();

    //初始化
    static {
        for (AttrExt e : AttrExt.values()) {
            EN_INDEX_MAP.put(e.attrName, e.indexId);
            nameMap.put(e.indexId, e.name);
            INDEX_NAME_MAP.put(e.indexId,e.attrName);
        }
    }

    private AttrExt(int indexId, String attrName, String name) {
        this.indexId = indexId;
        this.attrName = attrName;
        this.name = name;
    }

    public int getIndexId() {
        return indexId;
    }

    public String getAttrName() {
        return attrName;
    }

    public String getName() {
        return name;
    }

    public static Map<Integer, String> getNameMap() {
        return nameMap;
    }

    public static int getIndexByteEn(String en) {
        String extEn = en.substring(0, en.length() - 1);
        if (!EN_INDEX_MAP.containsKey(extEn)) {
            return -1;
        }
        return Attr.values().length + EN_INDEX_MAP.get(extEn) * 10 + Integer.valueOf(en.substring(en.length() - 1, en.length()));

    }

    public static String getAttrNameByIndex(int index) {
        int realIndex = index - Attr.values().length;
        int enumIndex = realIndex / 10;
        String enumName = INDEX_NAME_MAP.get(enumIndex);
        if (enumName == null) {
            return null;
        }
        return enumName + (realIndex % 10);
    }
}
