package com.stars.core.module.newmodule;

public interface ModuleV2 {

    void onLoad();

    void onInit();

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
