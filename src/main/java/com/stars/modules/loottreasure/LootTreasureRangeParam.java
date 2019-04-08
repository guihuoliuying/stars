package com.stars.modules.loottreasure;

import java.util.Random;

/**
 * 丢失个数的范围类;
 * Created by panzhenfeng on 2016/10/19.
 */
public class LootTreasureRangeParam {
    private int min;
    private int max;
    private int loseMinCount;
    private int loseMaxCount;

    public LootTreasureRangeParam(int min, int max){
        this.min = min;
        this.max = max;
    }

    public LootTreasureRangeParam(int min, int max, int loseMinCount, int loseMaxCount){
        this.min = min;
        this.max = max;
        this.loseMinCount = loseMinCount;
        this.loseMaxCount = loseMaxCount;
    }

    public int isInRange(int value){
        if((min - value)>0){
            return -min;
        }
        if((value - max)>0){
            return max;
        }
        return 0;
    }

    public int getLoseCount(){
        return this.loseMinCount + new Random().nextInt(this.loseMaxCount - this.loseMinCount + 1);
    }
}
