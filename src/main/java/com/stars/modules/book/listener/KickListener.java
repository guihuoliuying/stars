package com.stars.modules.book.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.book.BookModule;
import com.stars.modules.book.event.KickEvent;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class KickListener extends AbstractEventListener<BookModule> {
    public KickListener(BookModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().handleKickEv((KickEvent) event);
    }
}
