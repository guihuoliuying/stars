package com.stars.modules.email.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.MConst;
import com.stars.modules.email.EmailModule;
import com.stars.modules.email.event.RemoveEmailEvent;
import com.stars.modules.redpoint.RedPointConst;

/**
 * Created by chenkeyu on 2016/11/29.
 */
public class RemoveEmailListener implements EventListener {
    private EmailModule module;

    public RemoveEmailListener(EmailModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        RemoveEmailEvent removeEmailEvent = (RemoveEmailEvent) event;
        if (removeEmailEvent.getEmailId() == 0) {
            module.removeEmailList();
        } else {
            module.removeEmailList(removeEmailEvent.getEmailId());
        }
        module.signCalRedPoint(MConst.Email, RedPointConst.NEW_EMAIL);
    }
}
