package com.stars.modules.fashioncard.effect.imp;

import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.prodata.FashionCard;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class AddItemEffectImpl extends FashionCardEffect {

    private Map<Integer, Integer> itemMap = new HashMap<>();

    public AddItemEffectImpl(FashionCard fashionCard) {
        super(fashionCard);
    }

    @Override
    public void parseData(String param) {
        if (param.equals("")) return;
        this.itemMap = StringUtil.toMap(param, Integer.class, Integer.class, '+', ',');
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }
}
