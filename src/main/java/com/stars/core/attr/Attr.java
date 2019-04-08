package com.stars.core.attr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jx on 2015/3/31.
 */
public enum Attr {
    HP(0, "hp", "生命值"),// 当前生命值
    MP(1, "mp", "法力值"),
    ATTACK(2, "attack", "攻击力"),
    DEFENSE(3, "defense", "防御力"),
    HIT(4, "hit", "命中"),
    AVOID(5, "avoid", "闪避"),
    CRIT(6, "crit", "暴击"),
    ANTICRIT(7, "anticrit", "抗暴值"),
    CRITHURTADD(8, "crithurtadd", "必杀"),
    CRITHURTREDUCE(9, "crithurtreduce", "守护"),
    MAXHP(10, "maxhp", "最大生命值"),
    FOCUSRATE(11,"focusRate","会心"),
    FOCUSREDUCE(12,"focusReduce","抗会心"),
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
        for (Attr e : Attr.values()) {
            EN_INDEX_MAP.put(e.attrName, e.indexId);
            nameMap.put(e.indexId, e.name);
            INDEX_NAME_MAP.put(e.indexId,e.attrName);
        }
    }

    private Attr(int indexId, String attrName, String name) {
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

    /**
     * 通过英文来获得下标
     * @param en
     * @return
     */
    public static int getIndexByteEn(String en) {
        if (en.startsWith("ext")) {
            /** 扩展属性 */
            return AttrExt.getIndexByteEn(en);
        }
        if (EN_INDEX_MAP.containsKey(en)) {
            return EN_INDEX_MAP.get(en);
        }
        return -1;
    }

    /*public static Map<Byte, String> getIndexNameMap() {
        return INDEX_NAME_MAP;
    }*/
    public static String getAttrNameByIndex(int index) {
        if (index >= Attr.values().length) {
            /** 扩展属性 */
            return AttrExt.getAttrNameByIndex(index);
        }
        return INDEX_NAME_MAP.get(index);
    }
}
