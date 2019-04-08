package com.stars.modules.email.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.MConst;
import com.stars.modules.email.EmailModule;
import com.stars.modules.email.event.EmailRedPointEvent;
import com.stars.modules.redpoint.RedPointConst;

/**
 * Created by zhaowenshuo on 2017/4/15.
 */
public class EmailRedPointListener implements EventListener {

    private EmailModule module;

    public EmailRedPointListener(EmailModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        module.setLastEmailRedPointEvent((EmailRedPointEvent) event);
        module.signCalRedPoint(MConst.Email, RedPointConst.NEW_EMAIL);
    }

}
