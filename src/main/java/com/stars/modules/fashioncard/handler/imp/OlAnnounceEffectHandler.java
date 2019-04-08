package com.stars.modules.fashioncard.handler.imp;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.fashioncard.FashionCardModule;
import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.effect.imp.OlAnnounceEffectImpl;
import com.stars.modules.fashioncard.handler.FashionCardHandler;
import com.stars.modules.role.RoleModule;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public class OlAnnounceEffectHandler implements FashionCardHandler {
    @Override
    public void doAfterGetFashionCard(Map<String, Module> moduleMap, FashionCardEffect effect) {
        //do noting
    }

    @Override
    public void doAfterTransfer(Map<String, Module> moduleMap, FashionCardEffect effect) {
        OlAnnounceEffectImpl announceEffect = (OlAnnounceEffectImpl) effect;
        FashionCardModule cardModule = (FashionCardModule) moduleMap.get(MConst.FashionCard);
        if ((System.currentTimeMillis() - cardModule.getLastAnnounceTimestamp()) / (1000 * 60)
                >= announceEffect.getTimeInterval()) {
            RoleModule role = (RoleModule) moduleMap.get(MConst.Role);
            String roleName = role.getRoleRow().getName();
            ServiceHelper.chatService().announce(String.format(DataManager.getGametext(announceEffect.getAnnounce()), roleName));
            cardModule.setLasetAnnounceTimestamp(System.currentTimeMillis());
        }
    }
}
