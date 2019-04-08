package com.stars.modules.book;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.book.event.BeKickEvent;
import com.stars.modules.book.event.KickEvent;
import com.stars.modules.book.listener.*;
import com.stars.modules.book.prodata.BookInfo;
import com.stars.modules.book.prodata.BookRead;
import com.stars.modules.book.prodata.BookStage;
import com.stars.modules.book.prodata.OpenHoleInfo;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.vip.event.VipLevelupEvent;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class BookModuleFactory extends AbstractModuleFactory<BookModule> {
    public BookModuleFactory() {
        super(new BookPacketSet());
    }

    @Override
    public BookModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new BookModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(LoginSuccessEvent.class, new BookLoginListener((BookModule) module));
        eventDispatcher.reg(RoleLevelUpEvent.class, new RoleLevelUpListener((BookModule) module));
        eventDispatcher.reg(VipLevelupEvent.class, new VipLevelUpListener((BookModule) module));
        eventDispatcher.reg(KickEvent.class, new KickListener((BookModule) module));
        eventDispatcher.reg(BeKickEvent.class, new BeKickListener((BookModule) module));
        eventDispatcher.reg(AddToolEvent.class, new GetToolBookListener((BookModule) module));
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void loadProductData() throws Exception {
        BookManager.bookInfoMap = loadBookInfo();
        BookManager.bookReadMap = loadBookRead();
        BookManager.bookStageMap = loadBookStage();
        BookManager.openHoleInfoMap = loadOpenHoleInfo();
        BookManager.READ_BOOK_IMMEDIATELY_VIP = DataManager.getCommConfig("book_quickfinish_open", 1);
        BookManager.INTERVAL = DataManager.getCommConfig("book_reqgold", 10);
    }

    private Map<Integer, Map<Short, BookRead>> loadBookRead() throws SQLException {
        String sql = "select * from `bookread`; ";
        List<BookRead> list = DBUtil.queryList(DBUtil.DB_PRODUCT, BookRead.class, sql);
        Map<Integer, Map<Short, BookRead>> map = new HashMap<>();
        for (BookRead bookRead : list) {
            Map<Short, BookRead> tmpmap = map.get(bookRead.getBookid());
            if (null == tmpmap) {
                tmpmap = new HashMap<>();
                map.put(bookRead.getBookid(), tmpmap);
            }
            tmpmap.put(bookRead.getLevel(), bookRead);
        }
        return map;
    }

    private Map<Integer, BookInfo> loadBookInfo() throws SQLException {
        String sql = "select * from `bookinfo`; ";
        Map<Integer, BookInfo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "bookid", BookInfo.class, sql);
        return map;
    }

    private Map<Integer, LinkedList<BookStage>> loadBookStage() throws SQLException {
        String sql = "select * from `bookstage`; ";
        List<BookStage> list = DBUtil.queryList(DBUtil.DB_PRODUCT, BookStage.class, sql);
        Map<Integer, LinkedList<BookStage>> map = new HashMap<>();
        for (BookStage bookStage : list) {
            LinkedList<BookStage> tmplist = map.get(bookStage.getBookid());
            if (null == tmplist) {
                tmplist = new LinkedList<>();
                map.put(bookStage.getBookid(), tmplist);
            }
            tmplist.add(bookStage);
        }
        for (Map.Entry<Integer, LinkedList<BookStage>> entry : map.entrySet()) {
            Collections.sort(entry.getValue(), new BookStageComparator());
        }
        return map;
    }

    private Map<Byte, OpenHoleInfo> loadOpenHoleInfo() throws SQLException {
        String sql = "select * from `bookhole`; ";
        Map<Byte, OpenHoleInfo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "holeid", OpenHoleInfo.class, sql);
        return map;
    }
}
