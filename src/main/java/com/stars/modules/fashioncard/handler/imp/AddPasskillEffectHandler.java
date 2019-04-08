package com.stars.modules.fashioncard.handler.imp;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.effect.imp.AddPasskillEffectImpl;
import com.stars.modules.fashioncard.handler.FashionCardHandler;
import com.stars.modules.skill.SkillModule;

import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public class AddPasskillEffectHandler implements FashionCardHandler {
    @Override
    public void doAfterGetFashionCard(Map<String, Module> moduleMap, FashionCardEffect effect) {
        AddPasskillEffectImpl passkillEffect = (AddPasskillEffectImpl) effect;
        SkillModule skill = (SkillModule) moduleMap.get(MConst.Skill);
        Set<Integer> skillSet = passkillEffect.getPassKillIdSet();
        for (int skillId : skillSet) {
            skill.upRoleSkill(skillId);
        }
    }

    @Override
    public void doAfterTransfer(Map<String, Module> moduleMap, FashionCardEffect effect) {

    }
}
