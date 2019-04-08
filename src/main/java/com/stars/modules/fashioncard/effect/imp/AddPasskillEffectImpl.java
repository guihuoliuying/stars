package com.stars.modules.fashioncard.effect.imp;

import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.prodata.FashionCard;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class AddPasskillEffectImpl extends FashionCardEffect {

    private Set<Integer> passKillIdSet = new HashSet<>();

    public AddPasskillEffectImpl(FashionCard fashionCard) {
        super(fashionCard);
    }

    @Override
    public void parseData(String param) {
        if (param.equals("")) return;
        try {
            this.passKillIdSet = StringUtil.toHashSet(param, Integer.class, ',');
        } catch (Exception e) {
            throw new IllegalArgumentException("AddPasskillEffectImpl数据错误");
        }
    }

    public Set<Integer> getPassKillIdSet() {
        return passKillIdSet;
    }
}
