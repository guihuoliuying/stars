package com.stars.modules.fashioncard.handler.imp;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.effect.imp.AddItemEffectImpl;
import com.stars.modules.fashioncard.handler.FashionCardHandler;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public class AddItemEffectHandler implements FashionCardHandler {
    @Override
    public void doAfterGetFashionCard(Map<String, Module> moduleMap, FashionCardEffect effect) {
        AddItemEffectImpl itemEffect = (AddItemEffectImpl) effect;
        Map<Integer, Integer> itemMap = itemEffect.getItemMap();
        ToolModule tool = (ToolModule) moduleMap.get(MConst.Tool);
        tool.addAndSend(new HashMap<>(itemMap), EventType.FASHION_CARD.getCode());
        ClientAward clientAward = new ClientAward();
        clientAward.setType((byte) 1);
        clientAward.setAwrd(itemMap);
        tool.send(clientAward);
    }

    @Override
    public void doAfterTransfer(Map<String, Module> moduleMap, FashionCardEffect effect) {

    }
}
