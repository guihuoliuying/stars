package com.stars.modules.title.listenner;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.title.TitleModule;

/**
 * Created by huwenjun on 2017/4/12.
 */
public class LevelUpListenner implements EventListener {
    TitleModule titleModule;

    public LevelUpListenner(TitleModule titleModule) {
        this.titleModule = titleModule;
    }

    @Override
    public void onEvent(Event event) {
        titleModule.refreshShowTitle();
        titleModule.sendAllTitleData();
    }
}
