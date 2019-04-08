package com.stars.modules.pk.event;

import com.stars.core.event.Event;
import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.List;

/**
 * Created by liuyuheng on 2016/11/2.
 */
public class PermitPkEvent extends Event {
    private long invitee;// 被邀请者id
    private List<FighterEntity> entityList;// 被邀请者FightEntity

    public PermitPkEvent(long invitee, List<FighterEntity> entityList) {
        this.invitee = invitee;
        this.entityList = entityList;
    }

    public PermitPkEvent() {
    }

    public long getInvitee() {
        return invitee;
    }

    public List<FighterEntity> getEntityList() {
        return entityList;
    }
}
