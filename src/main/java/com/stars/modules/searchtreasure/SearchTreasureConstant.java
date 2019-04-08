package com.stars.modules.searchtreasure;

/**
 * 仙山夺宝相关常量;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchTreasureConstant {
    /**
     * 内容类型：奖励;
     */
    public final static byte CONTENTTYPE_REWARD = 1;
    /**
     * 内容类型: 击杀怪物;
     */
    public final static byte CONTENTTYPE_MONSTER = 2;

    /**
     * 探索进度: 未探索;
     */
    public final static byte SEARCH_PROCESS_NONE = 0;
    /**
     * 探索进度: 已探索,未领取奖励
     */
    public final static byte SEARCH_PROCESS_COMPLETE_NOGET = 1;
    /**
     * 探索进度: 探索中;
     */
    public final static byte SEARCH_PROCESS_ING = 2;
    /**
     * 探索进度：已探索,已领取奖励
     */
    public final static byte SEARCH_PROCESS_COMPLETE_GETTED = 3;

    public static boolean getStateIsComplete(byte state) {
        return state == SEARCH_PROCESS_COMPLETE_GETTED || state == SEARCH_PROCESS_COMPLETE_NOGET;
    }
}
