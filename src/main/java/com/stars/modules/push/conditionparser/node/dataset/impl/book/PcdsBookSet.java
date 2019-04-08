package com.stars.modules.push.conditionparser.node.dataset.impl.book;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.book.BookManager;
import com.stars.modules.book.BookModule;
import com.stars.modules.book.prodata.BookInfo;
import com.stars.modules.book.userdata.RoleBookUtil;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/5/31.
 */
public class PcdsBookSet extends PushCondDataSet {

    private Iterator<BookInfo> iterator;
    private BookModule bookModule;

    public PcdsBookSet() {
    }

    public PcdsBookSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        iterator = BookManager.bookInfoMap.values().iterator();
        bookModule = module(MConst.Book);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        BookInfo bookVo = iterator.next();
        RoleBookUtil bookPo = bookModule.getRoleBookUtil(bookVo.getBookid());
        return new PcdsBook(bookPo, bookVo);
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsBook.fieldSet();
    }
}
