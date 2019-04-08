package com.stars.modules.book.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.book.BookModule;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class RoleLevelUpListener extends AbstractEventListener<BookModule> {
    public RoleLevelUpListener(BookModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().holeOpenCheck(false);
    }
}
