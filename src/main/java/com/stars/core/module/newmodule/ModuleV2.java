package com.stars.core.module.newmodule;

public interface ModuleV2 {

    void onLoad();

    /**
     * 1. 战力加减、属性加减
     * 2. 战力排行榜刷新
     * 3. 称号检查 - 战力第一
     * <p>
     * 1、2、3层次是递进的
     * <p>
     * 进入场景初始化
     * <p>
     * A. 基于自身初始化 onInitOfSelf
     * B. 基于扩展初始化（如战力、属性等）onInitOfExtension
     * C. 基于集成初始化（排行榜、称号、成就、精准推送）onInitOfIntegration
     * <p>
     * D. 进入场景 enterScene
     */
    void onInit(); // selfInit, ext

    void onSync();

    void onOffline();

    void onReconnect();

    void onRemove();

    /* 重置函数 */
    void on0ClockReset();

    void on5ClockReset();

    void onWeeklyReset();

    void onMonthlyReset();

    /* 杂项 */
    void updateSummary();

    void calcRedPoints();

}
