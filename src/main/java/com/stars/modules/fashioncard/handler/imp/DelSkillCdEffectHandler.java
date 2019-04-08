package com.stars.modules.fashioncard.handler.imp;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.fashioncard.FashionCardModule;
import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.effect.imp.DelSkillCdEffectImpl;
import com.stars.modules.fashioncard.handler.FashionCardHandler;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public class DelSkillCdEffectHandler implements FashionCardHandler {
    @Override
    public void doAfterGetFashionCard(Map<String, Module> moduleMap, FashionCardEffect effect) {
        FashionCardModule cardModule = (FashionCardModule) moduleMap.get(MConst.FashionCard);
        DelSkillCdEffectImpl skillCdEffect = (DelSkillCdEffectImpl) effect;
        cardModule.delSkillCd(skillCdEffect.getSkillCdMap());
    }

    @Override
    public void doAfterTransfer(Map<String, Module> moduleMap, FashionCardEffect effect) {
        FashionCardModule cardModule = (FashionCardModule) moduleMap.get(MConst.FashionCard);
        DelSkillCdEffectImpl skillCdEffect = (DelSkillCdEffectImpl) effect;
        cardModule.delSkillCd(skillCdEffect.getSkillCdMap());
    }
}
