package com.stars.multiserver.fightingmaster;

/**
 * 机器人
 * Created by zhouyaohui on 2016/11/11.
 */
public class Robot extends Fighter implements RobotInterface {

    @Override
    public int compare(Matchable other) {
        Fighter fighter = (Fighter) other;
        return getCharactor().getFightScore() - fighter.getCharactor().getFightScore();
    }
}
