package com.stars.util.ranklist;

/**
 * Created by zhaowenshuo on 2016/4/16.
 */
public class RankObj implements Comparable<RankObj> {

    String key;
    long points;

    public RankObj(String key, long points) {
        this.key = key;
        this.points = points;
    }

    public String getKey() {
        return key;
    }

    public long getPoints() {
        return points;
    }

    @Override
    public int compareTo(RankObj other) {
        if (this.points > other.points) {
            return -1;
        }
        if (this.points < other.points) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "(" + key + "=" + points + ")";
    }
}
