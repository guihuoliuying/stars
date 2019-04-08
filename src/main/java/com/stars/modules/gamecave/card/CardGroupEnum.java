package com.stars.modules.gamecave.card;

/**
 * Created by gaopeidian on 2017/1/13.
 */
public enum CardGroupEnum {
	StraightFlush(1),//同花顺
    AllSame(2),//清一色
    Flush(3),//同花
    Three(4),//三张
    Straight(5),//顺子，飞机
    Piar(6),//对子
    HighCard(7),//高牌，其他
            ;

    private int groudType;
    
    private CardGroupEnum(int groudType) {
        this.groudType = groudType;
    }
    
    public int getGroudType(){
    	return groudType;
    }
}

