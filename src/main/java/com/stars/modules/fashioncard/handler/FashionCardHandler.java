package com.stars.modules.fashioncard.handler;

import com.stars.core.module.Module;
import com.stars.modules.fashioncard.effect.FashionCardEffect;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public interface FashionCardHandler {

    //获得时装化身道具时调用
    void doAfterGetFashionCard(Map<String, Module> moduleMap, FashionCardEffect effect);

    //其他时刻调用
    void doAfterTransfer(Map<String,Module> moduleMap, FashionCardEffect effect);
}
