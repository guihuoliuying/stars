package com.stars.modules.book.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.book.BookModule;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class VipLevelUpListener extends AbstractEventListener<BookModule> {
    public VipLevelUpListener(BookModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().holeOpenCheck(false);
    }
}
