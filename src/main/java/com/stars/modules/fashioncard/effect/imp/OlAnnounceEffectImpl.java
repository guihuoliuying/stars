package com.stars.modules.fashioncard.effect.imp;

import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.prodata.FashionCard;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class OlAnnounceEffectImpl extends FashionCardEffect {

    private String announce;
    private int timeInterval;

    public OlAnnounceEffectImpl(FashionCard fashionCard) {
        super(fashionCard);
    }

    @Override
    public void parseData(String param) {
        if (param.equals("")) return;
        String[] tmp = param.split("\\+");
        this.announce = tmp[0];
        this.timeInterval = Integer.parseInt(tmp[1]);
    }

    public String getAnnounce() {
        return announce;
    }

    public int getTimeInterval() {
        return timeInterval;
    }
}
