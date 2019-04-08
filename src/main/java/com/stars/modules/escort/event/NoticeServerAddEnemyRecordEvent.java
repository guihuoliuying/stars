package com.stars.modules.escort.event;

import com.stars.core.event.Event;

import java.util.List;

/**
 * Created by wuyuxing on 2016/12/15.
 */
public class NoticeServerAddEnemyRecordEvent extends Event {
    private List<Long> enemyList;

    public NoticeServerAddEnemyRecordEvent(List<Long> enemyList) {
        this.enemyList = enemyList;
    }

    public NoticeServerAddEnemyRecordEvent() {
    }

    public List<Long> getEnemyList() {
        return enemyList;
    }
}
