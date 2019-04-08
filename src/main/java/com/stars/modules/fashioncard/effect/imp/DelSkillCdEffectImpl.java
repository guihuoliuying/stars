package com.stars.modules.fashioncard.effect.imp;

import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.prodata.FashionCard;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class DelSkillCdEffectImpl extends FashionCardEffect {

    private Map<Integer, Integer> skillCdMap = new HashMap<>();

    public DelSkillCdEffectImpl(FashionCard fashionCard) {
        super(fashionCard);
    }

    @Override
    public void parseData(String param) {
        if (param.equals("")) return;
        this.skillCdMap = StringUtil.toMap(param, Integer.class, Integer.class, '+', ',');
    }

    public Map<Integer, Integer> getSkillCdMap() {
        return skillCdMap;
    }
}
