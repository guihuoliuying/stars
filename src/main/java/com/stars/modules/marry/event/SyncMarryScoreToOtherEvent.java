package com.stars.modules.marry.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-07-06.
 */
public class SyncMarryScoreToOtherEvent extends Event {
    private long other;
    private int score;

    public SyncMarryScoreToOtherEvent(long other, int score) {
        this.other = other;
        this.score = score;
    }

    public long getOther() {
        return other;
    }

    public int getScore() {
        return score;
    }
}
