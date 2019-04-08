package com.stars.modules.gamecave.tinygame;

/**
 * Created by gaopeidian on 2017/1/12.
 */
public interface ITinyGame {
    /**开始玩游戏*/
    void start();
    /**退出游戏*/
    void exit();
    /**游戏结束;*/
    void finish(String dataStr);
    
    /**时间更新*/
    void onTimeUpdate();
}
