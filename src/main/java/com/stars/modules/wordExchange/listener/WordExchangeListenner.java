package com.stars.modules.wordExchange.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.wordExchange.WordExchangeModule;

/**
 * Created by huwenjun on 2017/9/7.
 */
public class WordExchangeListenner extends AbstractEventListener<WordExchangeModule> {
    public WordExchangeListenner(WordExchangeModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
