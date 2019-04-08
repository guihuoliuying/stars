package com.stars.modules.book.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.book.BookModule;
import com.stars.modules.book.event.BeKickEvent;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class BeKickListener extends AbstractEventListener<BookModule> {
    public BeKickListener(BookModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().handleBeKickEv((BeKickEvent) event);
    }
}
