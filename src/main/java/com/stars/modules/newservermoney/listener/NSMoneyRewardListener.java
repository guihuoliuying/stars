package com.stars.modules.newservermoney.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.newservermoney.NewServerMoneyModule;
import com.stars.modules.newservermoney.event.NSMoneyRewardEvent;

/**
 * Created by liuyuheng on 2017/1/5.
 */
public class NSMoneyRewardListener extends AbstractEventListener<NewServerMoneyModule> {
    public NSMoneyRewardListener(NewServerMoneyModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        NSMoneyRewardEvent nsMoneyRewardEvent = (NSMoneyRewardEvent) event;
        switch (nsMoneyRewardEvent.getEventType()) {
            case NSMoneyRewardEvent.TAKE_REWARD: {
                module().rewardHandler(event);
            }
            break;
            case NSMoneyRewardEvent.SEND_REWARD_RECORD: {
                module().reqRewardRecord();
            }
            break;
        }
    }
}
