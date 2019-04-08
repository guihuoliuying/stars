package com.stars.modules.newsignin.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.newsignin.NewSigninModule;

/**
 * Created by chenkeyu on 2017-02-21 10:57
 */
public class RoleLvUpListener implements EventListener {
    private NewSigninModule signin;

    public RoleLvUpListener(NewSigninModule signin) {
        this.signin = signin;
    }

    @Override
    public void onEvent(Event event) {
        signin.doChangeEvent();
    }
}
