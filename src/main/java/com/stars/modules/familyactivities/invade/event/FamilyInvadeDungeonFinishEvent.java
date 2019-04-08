package com.stars.modules.familyactivities.invade.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/10/21.
 */
public class FamilyInvadeDungeonFinishEvent extends Event {
    private int invadeId;
    private byte result;// 结果,胜利=2;失败=0

    public FamilyInvadeDungeonFinishEvent(int invadeId, byte result) {
        this.invadeId = invadeId;
        this.result = result;
    }

    public int getInvadeId() {
        return invadeId;
    }

    public byte getResult() {
        return result;
    }
}
