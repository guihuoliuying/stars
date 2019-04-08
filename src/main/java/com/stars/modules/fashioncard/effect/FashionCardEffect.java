package com.stars.modules.fashioncard.effect;

import com.stars.modules.fashioncard.prodata.FashionCard;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public abstract class FashionCardEffect {
    private FashionCard fashionCard;

    public FashionCardEffect(FashionCard fashionCard) {
        this.fashionCard = fashionCard;
    }

    public FashionCard getFashionCard() {
        return fashionCard;
    }

    public abstract void parseData(String param);
}
