package com.stars.modules.dungeon.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/12/19.
 */
public class ChapterStarAchieveEvent extends Event {
    private int chapterId;// 章节Id
    private int preStar;// 以前星星数
    private int newStr;// 最新星星数

    public ChapterStarAchieveEvent(int chapterId, int preStar, int newStr) {
        this.chapterId = chapterId;
        this.preStar = preStar;
        this.newStr = newStr;
    }

    public int getChapterId() {
        return chapterId;
    }

    public int getPreStar() {
        return preStar;
    }

    public int getNewStr() {
        return newStr;
    }
}
