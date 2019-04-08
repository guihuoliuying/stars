package com.stars.modules.pk.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/11/2.
 */
public class FinishPkEvent extends Event {
    private byte result;// 结果,使用SceneManager中战斗副本状态,0=失败;2=胜利
    private long enemyRoleId;

    public FinishPkEvent(byte result, long enemyRoleId) {
        this.result = result;
        this.enemyRoleId = enemyRoleId;
    }

    public byte getResult() {
        return result;
    }

    public long getEnemyRoleId() {
        return enemyRoleId;
    }
}
