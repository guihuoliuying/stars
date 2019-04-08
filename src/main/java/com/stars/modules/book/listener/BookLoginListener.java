package com.stars.modules.book.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.book.BookModule;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class BookLoginListener extends AbstractEventListener<BookModule> {
    public BookLoginListener(BookModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().playerOnline();
    }
}
