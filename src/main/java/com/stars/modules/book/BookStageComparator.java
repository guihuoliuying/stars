package com.stars.modules.book;

import com.stars.modules.book.prodata.BookStage;

import java.util.Comparator;

/**
 * Created by zhouyaohui on 2017/5/12.
 */
public class BookStageComparator implements Comparator<BookStage> {
    @Override
    public int compare(BookStage o1, BookStage o2) {
        return o1.getStage() - o2.getStage();
    }
}
