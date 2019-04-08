package com.stars.modules.gamecave.card;

/**
 * Created by gaopeidian on 2017/1/13.
 */
public enum CardColorEnum {
    CardGreen(1),
    CardBlue(2),
    CardPurple(3),
    CardOrange(4),
            ;

    private int color;
    
    private CardColorEnum(int color) {
        this.color = color;
    }
    
    public int getColor(){
    	return color;
    }
}

