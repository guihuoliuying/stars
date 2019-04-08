package com.stars.modules.email.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.MConst;
import com.stars.modules.email.EmailModule;
import com.stars.modules.email.event.AddEmailEvent;
import com.stars.modules.redpoint.RedPointConst;

/**
 * Created by chenkeyu on 2016/11/29.
 */
public class AddEmailListener implements EventListener {
    private EmailModule module;

    public AddEmailListener(EmailModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        AddEmailEvent addEmailEvent = (AddEmailEvent) event;
        if (addEmailEvent.getEmailId() != 0) {
            module.addEmailList(addEmailEvent.getEmailId());
        } else {
            module.addEmailList(addEmailEvent.getEmailList());
        }
        module.signCalRedPoint(MConst.Email, RedPointConst.NEW_EMAIL);
    }
}
