package com.stars.modules.push.conditionparser.node.dataset.impl.book;

import com.stars.modules.book.BookManager;
import com.stars.modules.book.prodata.BookInfo;
import com.stars.modules.book.prodata.BookStage;
import com.stars.modules.book.userdata.RoleBookUtil;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/5/31.
 */
public class PcdsBook implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "order", "quality"));
    }

    private RoleBookUtil bookPo;
    private BookInfo bookVo;

    public PcdsBook() {
    }

    public PcdsBook(RoleBookUtil bookPo, BookInfo bookVo) {
        this.bookPo = bookPo;
        this.bookVo = bookVo;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id":
                return bookVo.getBookid();
            case "order":
                if (bookPo != null) {
                    BookStage stage = BookManager.getBookStage(bookPo.getBookId(), bookPo.getBookNum());
                    return stage != null ? stage.getOrder() : 0;
                }
            case "quality":
                return bookVo.getQuality();
        }
        return 0;
    }

    @Override
    public boolean isOverlay() {
        return false;
    }

    @Override
    public long getOverlayCount() {
        return 0;
    }

    @Override
    public boolean isInvalid() {
        return bookVo == null;
    }
}
