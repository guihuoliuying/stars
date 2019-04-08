package com.stars.modules.book.event;

import com.stars.core.event.Event;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class BookActiveEvent extends Event {
    private int bookId;
    public BookActiveEvent(int bookId) {
        this.bookId = bookId;
    }

    public int getBookId() {
        return bookId;
    }
}
