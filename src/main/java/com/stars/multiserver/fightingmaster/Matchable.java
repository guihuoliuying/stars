package com.stars.multiserver.fightingmaster;

/**
 * 匹配接口
 * Created by zhouyaohui on 2016/11/11.
 */
public interface Matchable {

    /**
     * @param other
     * @return 负数 表示 this < other 零表示this == other 正数表示this > other
     * 注意：数值大小表示浮动匹配范围
     */
    int compare(Matchable other);

}
