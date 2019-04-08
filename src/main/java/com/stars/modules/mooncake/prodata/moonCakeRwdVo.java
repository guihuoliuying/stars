package com.stars.modules.mooncake.prodata;

/**
 * Created by zhangerjiang on 2017/9/15.
 */
public class moonCakeRwdVo {
    private int itemId;    //奖励道具ID
    private int count;     //奖励道具数量
    private int score;     //积分


    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "moonCakeRwdVo{" +
                "itemId=" + itemId +
                ", count=" + count +
                ", score=" + score +
                '}';
    }
}
