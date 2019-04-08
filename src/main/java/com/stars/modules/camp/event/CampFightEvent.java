package com.stars.modules.camp.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/8/5.
 */
public class CampFightEvent extends Event {
    private String action;
    public static final String TYPE_EXIT = "EXIT";
    public static final String TYPE_ADD_DAILY_SCORE = "ADD_DAILY_SCORE";
    public static final String TYPE_MATCHING_SUCCESS = "MATCHING_SUCCESS";
    private int score;

    public CampFightEvent() {
    }

    public CampFightEvent(String action) {
        this.action = action;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getAction() {
        return action;
    }
}
