package com.stars.modules.book;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.book.event.BeKickEvent;
import com.stars.modules.book.event.BookAchieveEvent;
import com.stars.modules.book.event.BookActiveEvent;
import com.stars.modules.book.event.KickEvent;
import com.stars.modules.book.packet.ClientBook;
import com.stars.modules.book.prodata.BookInfo;
import com.stars.modules.book.prodata.BookRead;
import com.stars.modules.book.prodata.BookStage;
import com.stars.modules.book.prodata.OpenHoleInfo;
import com.stars.modules.book.userdata.RoleBook;
import com.stars.modules.book.userdata.RoleBookUtil;
import com.stars.modules.book.util.BookUtilTmp;
import com.stars.modules.book.util.RoleBookTmp;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.userdata.RoleDungeon;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.vip.VipModule;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class BookModule extends AbstractModule {

    /* 特殊处理/20170522/登录发送补偿 */
    public static final String SPEC_170517_BOOK_COMPENSATION = "spec.170517.book.compensation"; // 典籍补偿

    Map<Integer, RoleBookUtil> roleBookUtilMap = new HashMap<>();
    RoleBook roleBook;

    // 内存数据
    Set<Integer> readingList = new HashSet<>();   // 正在读的典籍列表
    Set<Integer> readedList = new HashSet<>();    // 已读完还没有领悟的典籍列表

    public BookModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("典籍", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleBook = new RoleBook(id());
        context().insert(roleBook);
    }

    @Override
    public void onDataReq() throws Exception {
        String sql = "select * from `rolebookutil` where `roleId`=" + id();
        roleBookUtilMap = DBUtil.queryMap(DBUtil.DB_USER, "bookId", RoleBookUtil.class, sql);
        if (roleBookUtilMap.size() > 0) {
            // 计算每一本书的属性
            for (Map.Entry<Integer, RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
                calBookAttr(entry.getKey());
            }
        }
        String sql1 = "select * from `rolebook` where `roleId`=" + id();
        roleBook = DBUtil.queryBean(DBUtil.DB_USER, RoleBook.class, sql1);
        if (null == roleBook) {
            roleBook = new RoleBook(id());
            context().insert(roleBook);
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        // 检测自动开的孔
        holeOpenCheck(true);
        // 初始正在读和可以领悟的典籍
        bookInit();
        // 刷新属性
        updateBookAttr();
        // 红点
        signCalBookRedPoint();

        /* 特殊处理/20170522/登录发送补偿 */
        {
            if (getByte(SPEC_170517_BOOK_COMPENSATION, (byte) 0) == 0) {
                setByte(SPEC_170517_BOOK_COMPENSATION, (byte) 1);
                DungeonModule dungeonModule = module(MConst.Dungeon);
                RoleDungeon roleDungeon = dungeonModule.getRolePassDungeonMap().get(10304); // 主线关卡3-4
                if (roleDungeon != null) {
                    try {
                        ServiceHelper.emailService().sendToSingle(id(), 10018, 0L, "系统", null);
                    } catch (Exception e) {
                        LogUtil.info("book|异常:compensation|roleId:{}", id());
                    }
                }
            }
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        roleBook.setKickTimes((short) 0);
        roleBook.setBeKickTimes((short) 0);
        context().update(roleBook);
        // 同步被敲打次数
        ServiceHelper.bookService().resetBeKickTimes(id());
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.bookService().playerOffline(id());
        bookStaticLog();
    }

    @Override
    public void onReconnect() throws Throwable {
        playerOnline();
        updateBookAttr();
    }

    @Override
    public void onTimingExecute() {
        int now = DateUtil.getSecondTime();
        List<Integer> removeList = new ArrayList<>();
        for (Integer bookId : readingList) {
            RoleBookUtil book = roleBookUtilMap.get(bookId);
            if (null == book)
                continue;
            if (now >= book.getEndReadTime()) {
                removeList.add(bookId);
            }
        }
        if (removeList.size() > 0) {
            for (Integer bookId : removeList) {
                readingList.remove(bookId);
                ServiceHelper.bookService().syncReadBook(id(), new BookUtilTmp(bookId), (byte) 0);
                readedList.add(bookId);
            }
            // 计算红点
            signCalRedPoint(MConst.Book, RedPointConst.BOOK_READ);
            signCalRedPoint(MConst.Book, RedPointConst.BOOK_LVUP);
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.BOOK_READ)) {
            checkBookReadRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.BOOK_LVUP)) {
            checkBookLvUpRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.BOOK_ACTIVE)) {
            checkBookActiveRedPoint(redPointMap);
        }
    }

    @Override
    public void onSyncData() {
        sendAllActiveBookList();
        fireBookAchievementEvent(); //登陆触发成就达成检测
    }

    public void sendAllActiveBookList() {
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_ACTIVE_BOOK_LIST);
        List<Integer> reslist = new ArrayList<>();
        for (Map.Entry<Integer, RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
            if (entry.getValue().getBookStatus() == BookManager.BOOK_ACTIVE) {
                reslist.add(entry.getKey());
            }
        }
        res.setActiveList(reslist);
        send(res);
    }

    private void checkBookReadRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        if (readingList.size() < roleBook.getHoleSet().size()) {
            for (Map.Entry<Integer, RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
                if (entry.getValue().getBookStatus() == BookManager.BOOK_ACTIVE &&
                        entry.getValue().getStartReadTime() == 0 &&
                        entry.getValue().getEndReadTime() == 0) {
                    BookInfo bookInfo = BookManager.getBookInfo(entry.getKey());
                    if (null == bookInfo) continue;
                    if (entry.getValue().getBookLv() < bookInfo.getMaxlv()) {
                        BookRead bookRead = BookManager.getBookRead(entry.getKey(), entry.getValue().getBookLv());
                        if (null == bookRead) continue;
                        if (bookRead.getRolelevel() != 0) {
                            RoleModule roleModule = (RoleModule) module(MConst.Role);
                            if (roleModule.getLevel() < bookRead.getRolelevel()) continue;
                        }
                        builder.append(entry.getKey()).append("+");
                    }
                }
            }
        }
        redPointMap.put(RedPointConst.BOOK_READ,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    private void checkBookLvUpRedPoint(Map<Integer, String> redPointMap) {
        int now = DateUtil.getSecondTime();
        StringBuilder builder = new StringBuilder("");
        for (Integer bookId : readedList) {
            RoleBookUtil book = roleBookUtilMap.get(bookId);
            if (null == book) continue;
            BookRead bookRead = BookManager.getBookRead(bookId, book.getBookLv());
            if (null == bookRead) continue;
            if (book.getEndReadTime() != 0 && book.getEndReadTime() <= now) {
                ToolModule toolModule = this.module(MConst.Tool);
                Map<Integer, Integer> toolMap = StringUtil.toMap(bookRead.getLvupreqitem(), Integer.class, Integer.class, '+', ',');
                if (toolModule.contains(toolMap)) {
                    builder.append(bookId).append("+");
                }
            }
        }
        redPointMap.put(RedPointConst.BOOK_LVUP,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    private void checkBookActiveRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        for (Map.Entry<Integer, RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
            if (entry.getValue().getBookStatus() == BookManager.BOOK_ACTIVE) continue;
            BookInfo bookInfo = BookManager.getBookInfo(entry.getKey());
            if (entry.getValue().getBookNum() >= bookInfo.getReqitemmax())
                builder.append(entry.getKey()).append("+");
        }
        redPointMap.put(RedPointConst.BOOK_ACTIVE,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    public void signCalBookRedPoint() {
        signCalRedPoint(MConst.Book, RedPointConst.BOOK_READ);
        signCalRedPoint(MConst.Book, RedPointConst.BOOK_LVUP);
        signCalRedPoint(MConst.Book, RedPointConst.BOOK_ACTIVE);
    }

    /**
     * 初始正在读和可领悟典籍列表
     */
    private void bookInit() {
        if (roleBookUtilMap.size() > 0) {
            int now = DateUtil.getSecondTime();
            for (Map.Entry<Integer, RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
                if (entry.getValue().getStartReadTime() > 0 && entry.getValue().getEndReadTime() > 0) {
                    if (now < entry.getValue().getEndReadTime()) {
                        readingList.add(entry.getValue().getBookId());
                    } else {
                        readedList.add(entry.getValue().getBookId());
                    }
                }
            }
        }
    }

    public void playerOnline() {
        List<BookUtilTmp> li = new ArrayList<>();
        for (Integer bookId : readingList) {
            RoleBookUtil book = roleBookUtilMap.get(bookId);
            if (null == book) {
                continue;
            }
            BookUtilTmp tmp = new BookUtilTmp(book.getBookId(), book.getBookLv(), book.getStartReadTime(), book.getEndReadTime());
            li.add(tmp);
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        RoleBookTmp roleBookTmp = new RoleBookTmp();
        roleBookTmp.setRoleId(id());
        roleBookTmp.setName(roleModule.getRoleRow().getName());
        roleBookTmp.setJobId(roleModule.getRoleRow().getJobId());
        roleBookTmp.setLevel(roleModule.getRoleRow().getLevel());
        roleBookTmp.setBeLastKickTime(roleBook.getBeLastKickTime());
        roleBookTmp.setBeKickTimes(roleBook.getBeKickTimes());
        ServiceHelper.bookService().playerOnline(id(), li, roleBookTmp);
    }

    /**
     * private List<RoleBookUtil> getReadBookList() {
     * List<RoleBookUtil> list = new ArrayList<>();
     * int now = DateUtil.getSecondTime();
     * if (roleBookUtilMap.size() > 0) {
     * for (Map.Entry<Integer,RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
     * if (entry.getValue().getBookStatus() == BookManager.BOOK_ACTIVE &&
     * now < entry.getValue().getEndReadTime()) {
     * list.add(entry.getValue());
     * }
     * }
     * }
     * return list;
     * }
     **/

    public void updateBookAttr() {
        Attribute attribute = new Attribute();
        int totalFightScore = 0;
        if (roleBookUtilMap.size() > 0) {
            for (Map.Entry<Integer, RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
                if (null != entry.getValue().getAttribute()) {
                    attribute.addAttribute(entry.getValue().getAttribute());
                    totalFightScore += entry.getValue().getPower();
                }
            }
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            roleModule.updatePartAttr(RoleManager.ROLEATTR_BOOK, attribute);
            // 更新战力
            roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_BOOK, totalFightScore);
        }
    }

    public void updateBookAttrWithSend() {
        updateBookAttr();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore();
    }

    /**
     * 自动开孔检查
     */
    public void holeOpenCheck(boolean init) {
        // 检测自动开的孔
        boolean bChange = false;
        Map<Byte, OpenHoleInfo> openHoleInfoMap = BookManager.openHoleInfoMap;
        if (null == openHoleInfoMap) return;
        for (Map.Entry<Byte, OpenHoleInfo> entry : openHoleInfoMap.entrySet()) {
            if (roleBook.getHoleSet().contains(entry.getKey()))
                continue;
            if (entry.getValue().getItemId() != 0 && entry.getValue().getItemNum() > 0) {
                continue;
            } else {
                // 检查自动开启条件（lv、vip）
                if (entry.getValue().getLv() != 0) {
                    RoleModule roleModule = (RoleModule) module(MConst.Role);
                    if (roleModule.getLevel() < entry.getValue().getLv()) {
                        continue;
                    }
                }
                if (entry.getValue().getViplv() != 0) {
                    VipModule vipModule = (VipModule) module(MConst.Vip);
                    if (vipModule.getVipLevel() < entry.getValue().getViplv()) {
                        continue;
                    }
                }
                roleBook.getHoleSet().add(entry.getKey());
                bChange = true;
            }
        }
        if (bChange) {
            context().update(roleBook);
            if (!init) signCalRedPoint(MConst.Book, RedPointConst.BOOK_READ);
        }
    }

    /**
     * 计算单本典籍属性
     *
     * @param bookId
     */
    public void calBookAttr(int bookId) {
        if (!roleBookUtilMap.containsKey(bookId)) return;
        Attribute attribute = null;
        RoleBookUtil roleBookUtil = roleBookUtilMap.get(bookId);
        BookInfo bookInfo = BookManager.getBookInfo(bookId);
        if (null == bookInfo) return;
        BookStage bookStage;
        if (roleBookUtil.getBookStatus() == BookManager.BOOK_ACTIVE) {
            bookStage = BookManager.getMaxBookStage(bookId);
        } else {
            if (roleBookUtil.getBookNum() >= bookInfo.getReqitemmax()) {
                bookStage = BookManager.getSubMaxBookStage(bookId);
            } else {
                bookStage = BookManager.getBookStage(bookId, roleBookUtil.getBookNum());
            }
        }
        if (null != bookStage) {
            attribute = new Attribute();
            attribute.addAttribute(bookStage.getAttribute());
            if (roleBookUtil.getBookStatus() == BookManager.BOOK_ACTIVE && roleBookUtil.getBookLv() > 0) {
                BookRead bookRead = BookManager.getBookRead(bookId, roleBookUtil.getBookLv());
                if (null != bookRead && null != bookRead.getAttribute()) {
                    attribute.addAttribute(bookRead.getAttribute());
                }
            }
        }
        if (null != attribute) {
            roleBookUtil.setAttribute(attribute);
            roleBookUtil.setPower(FormularUtils.calFightScore(attribute));
        }
    }

    /**
     * 某典籍还可以增加多少碎片
     *
     * @param bookId
     * @param num
     * @return
     */
    public int canAddBookNum(int bookId, int num) {
        if (num <= 0)
            return 0;
        BookInfo bookInfo = BookManager.getBookInfo(bookId);
        if (null == bookInfo)
            return 0;
        int res = 0;
        if (!roleBookUtilMap.containsKey(bookId)) {
            res = bookInfo.getReqitemmax() > num ? num : bookInfo.getReqitemmax();
        } else {
            res = roleBookUtilMap.get(bookId).getBookNum() >= bookInfo.getReqitemmax() ? 0 : bookInfo.getReqitemmax() - roleBookUtilMap.get(bookId).getBookNum();
            res = res >= num ? num : res;
        }
        return res;
    }

    /**
     * 增加典籍进度(物品自动使用)
     *
     * @param bookId
     * @param num
     */
    public void addBookNum(int bookId, int num) {
        if (num <= 0)
            return;
        BookInfo bookInfo = BookManager.getBookInfo(bookId);
        if (null == bookInfo) return;
        byte dbStatus = 0;
        if (!roleBookUtilMap.containsKey(bookId)) {
            RoleBookUtil roleBookUtil = new RoleBookUtil(id());
            roleBookUtil.setBookId(bookId);
            roleBookUtilMap.put(bookId, roleBookUtil);
            dbStatus = 1;
        }
        roleBookUtilMap.get(bookId).setBookNum(roleBookUtilMap.get(bookId).getBookNum() + num);
        calBookAttr(bookId);
        updateBookAttrWithSend();
        if (0 == dbStatus) {
            context().update(roleBookUtilMap.get(bookId));
        } else {
            context().insert(roleBookUtilMap.get(bookId));
        }
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_BOOK_NUM);
        res.setBookId(bookId);
        res.setBookNum(roleBookUtilMap.get(bookId).getBookNum());
        send(res);
        signCalRedPoint(MConst.Book, RedPointConst.BOOK_ACTIVE);
    }

    /**
     * 打开典籍面板
     */

    public void openBookPanel() {
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_OPEN_BOOK_PANEL);
        res.setSelfRoleBook(roleBook);
        res.setSelfBookMap(roleBookUtilMap);
        send(res);
    }

    /**
     * 典籍详情
     *
     * @param bookId
     */

    public void bookDetail(int bookId) {
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_PLAYER_BOOK_DETAIL);
        res.setBookId(bookId);
        res.setSelfBookMap(roleBookUtilMap);
        send(res);
    }

    /**
     * 开孔
     *
     * @param holeId
     */
    public void openHole(byte holeId) {
        if (roleBook.getHoleSet().contains(holeId)) {
            return;
        }
        OpenHoleInfo openHoleInfo = BookManager.getOpenHoleInfo(holeId);
        if (null == openHoleInfo)
            return;
        if (openHoleInfo.getLv() != 0) {
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            if (roleModule.getLevel() < openHoleInfo.getLv()) {
                send(new ClientText("book_desc_reqrolelevel", String.valueOf(openHoleInfo.getLv())));
                return;
            }
        }
        if (openHoleInfo.getViplv() != 0) {
            VipModule vipModule = (VipModule) module(MConst.Vip);
            if (vipModule.getVipLevel() < openHoleInfo.getViplv()) {
                send(new ClientText("book_desc_reqviplevel", String.valueOf(openHoleInfo.getViplv())));
                return;
            }
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (toolModule.deleteAndSend(openHoleInfo.getItemId(), openHoleInfo.getItemNum(), EventType.OPEN_BOOK_HOLE.getCode())) {
            roleBook.openHole(holeId);
            context().update(roleBook);

            // 红点计算
            signCalRedPoint(MConst.Book, RedPointConst.BOOK_READ);

            // 同步前端
            ClientBook res = new ClientBook();
            res.setResType(ClientBook.RES_OPEN_HOLE);
            res.setHoleId(holeId);
            send(res);
        } else {
            // 提示
            send(new ClientText(I18n.get("book.item.no.enough")));
        }
    }

    /**
     * 激活典籍
     *
     * @param bookId
     */
    public void activeBook(int bookId) {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.BOOK)) {
            return;
        }
        RoleBookUtil roleBookUtil = roleBookUtilMap.get(bookId);
        if (null == roleBookUtil)
            return;
        if (roleBookUtil.getBookStatus() == BookManager.BOOK_ACTIVE)
            return;
        BookInfo bookInfo = BookManager.getBookInfo(bookId);
        if (null == bookInfo)
            return;
        if (roleBookUtil.getBookNum() < bookInfo.getReqitemmax()) {
            send(new ClientText(I18n.get("book.collect.not.enough")));
            return;
        }
        roleBookUtil.setBookStatus(BookManager.BOOK_ACTIVE);
        context().update(roleBookUtil);
        calBookAttr(bookId);
        updateBookAttrWithSend();
        // 红点计算
        signCalRedPoint(MConst.Book, RedPointConst.BOOK_READ);
        signCalRedPoint(MConst.Book, RedPointConst.BOOK_ACTIVE);

        eventDispatcher().fire(new BookActiveEvent(bookId));
        fireBookAchievementEvent();

        // 同步前端
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_ACTIVE_BOOK);
        res.setBookId(bookId);
        res.setBookStatus(BookManager.BOOK_ACTIVE);
        send(res);
        ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_BOOK, 1));
    }

    /**
     * 读书
     *
     * @param bookId
     */
    public void readBook(int bookId) {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.BOOK)) {
            return;
        }
        RoleBookUtil roleBookUtil = roleBookUtilMap.get(bookId);
        if (null == roleBookUtil)
            return;
        if (roleBookUtil.getBookStatus() == BookManager.BOOK_NOACTIVE)
            return;
        BookInfo bookInfo = BookManager.getBookInfo(bookId);
        if (null == bookInfo)
            return;
        if (roleBookUtil.getEndReadTime() != 0)
            return;
        if (bookInfo.getMaxlv() <= roleBookUtil.getBookLv()) {
            send(new ClientText(I18n.get("book.learn.max.level")));
            return;
        }
        BookRead bookRead = BookManager.getBookRead(bookId, roleBookUtil.getBookLv());
        if (null == bookRead)
            return;
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (roleModule.getLevel() < bookRead.getRolelevel()) {
            send(new ClientText("book_desc_reqrolelevel", String.valueOf(bookRead.getRolelevel())));
            return;
        }
        // List<RoleBookUtil> list = getReadBookList();
        if (readingList.size() >= roleBook.getHoleSet().size()) {
            send(new ClientText("book_tips_noread"));
            return;
        }
        int now = DateUtil.getSecondTime();
        roleBookUtil.setStartReadTime(now);
        roleBookUtil.setEndReadTime(now + bookRead.getReqtime());
        context().update(roleBookUtil);
        readingList.add(bookId);
        // 红点计算
        signCalBookRedPoint();

        // 同步前端
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_READ_BOOK);
        res.setBookId(bookId);
        res.setStartReadTime(roleBookUtil.getStartReadTime());
        res.setEndReadTime(roleBookUtil.getEndReadTime());
        res.setRemainTime(roleBookUtil.getEndReadTime() - now);
        send(res);
        // 同步service
        BookUtilTmp tmp = new BookUtilTmp(roleBookUtil.getBookId(), roleBookUtil.getBookLv(), roleBookUtil.getStartReadTime(), roleBookUtil.getEndReadTime());
        ServiceHelper.bookService().syncReadBook(id(), tmp, (byte) 1);
        ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_BOOK, 1));
    }

    public void quickReadBook(int bookId) {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.BOOK)) {
            return;
        }
        RoleBookUtil roleBookUtil = roleBookUtilMap.get(bookId);
        if (null == roleBookUtil)
            return;
        if (roleBookUtil.getBookStatus() == BookManager.BOOK_NOACTIVE)
            return;
        BookInfo bookInfo = BookManager.getBookInfo(bookId);
        if (null == bookInfo)
            return;
        if (roleBookUtil.getEndReadTime() == 0 || roleBookUtil.getStartReadTime() == 0)
            return;
        BookRead bookRead = BookManager.getBookRead(bookId, roleBookUtil.getBookLv());
        if (null == bookRead)
            return;
        VipModule vip = module(MConst.Vip);
        if (vip.getVipLevel() < BookManager.READ_BOOK_IMMEDIATELY_VIP) {
            warn(String.format(DataManager.getGametext("book_cheat_lackvip"), BookManager.READ_BOOK_IMMEDIATELY_VIP));
            return;
        }
        int now = DateUtil.getSecondTime();
        int delta = (int) (Math.ceil((double) (roleBookUtil.getEndReadTime() - now) / (double) (BookManager.INTERVAL * 60)));
        if (delta <= 0) {
            warn("book_cheat_done");
            return;
        }
        ToolModule tool = module(MConst.Tool);
        if (tool.deleteAndSend(ToolManager.BANDGOLD, delta, EventType.BOOK_QUICK_FINISH.getCode())) {
            roleBookUtil.setEndReadTime(now);
            context().update(roleBookUtil);
            ClientBook res = new ClientBook();
            res.setResType(ClientBook.RES_BOOK_UPDATE_TIME);
            res.setBookId(bookId);
            res.setStartReadTime(roleBookUtil.getStartReadTime());
            res.setEndReadTime(roleBookUtil.getEndReadTime());
            res.setReqItem(bookRead.getLvupreqitem());
            res.setRemainTime(0);
            send(res);
        } else {
            warn("book_cheat_nomoney");
        }
    }

    /**
     * 领悟
     *
     * @param bookId
     */
    public void learnBook(int bookId) {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.BOOK)) {
            return;
        }
        int now = DateUtil.getSecondTime();
        RoleBookUtil roleBookUtil = roleBookUtilMap.get(bookId);
        if (null == roleBookUtil)
            return;
        if (roleBookUtil.getStartReadTime() == 0)
            return;
        if (now < roleBookUtil.getEndReadTime()) {
            send(new ClientText(I18n.get("book.is.reading")));
            return;
        }
        BookRead curRead = BookManager.getBookRead(bookId, roleBookUtil.getBookLv());
        BookRead nextRead = BookManager.getBookRead(bookId, (short) (roleBookUtil.getBookLv() + 1));
        if (null == nextRead) {
            send(new ClientText(I18n.get("book.learn.max.level")));
            return;
        }
        Map<Integer, Integer> toolMap = StringUtil.toMap(curRead.getLvupreqitem(), Integer.class, Integer.class, '+', ',');
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (!toolModule.deleteAndSend(toolMap, EventType.LEARN_BOOK.getCode())) {
            send(new ClientText(I18n.get("book.item.no.enough")));
            return;
        }
        //check(!toolModule.deleteAndSend(toolMap,EventType.LEARN_BOOK.getCode()),"book.item.no.enough");
        roleBookUtil.setStartReadTime(0);
        roleBookUtil.setEndReadTime(0);
        roleBookUtil.setBookLv((short) (roleBookUtil.getBookLv() + 1));
        context().update(roleBookUtil);
        readedList.remove(bookId);
        // 红点计算
        signCalBookRedPoint();

        calBookAttr(bookId);
        updateBookAttrWithSend();
        ServiceHelper.bookService().syncReadBook(id(), new BookUtilTmp(bookId), (byte) 0);
        // 同步前端
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_LEARN_BOOK);
        res.setBookId(bookId);
        res.setBookLv(roleBookUtil.getBookLv());
        send(res);
        ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_BOOK, 1));
        fireBookAchievementEvent();
    }

    /**
     * 唤醒屈原
     *
     * @param target
     */
    public void awakeQuyuan(long target) {
        // 唤醒的过程先扣次数，如果唤醒失败，再把次数加上来
        if (id() == target)
            return;
        int maxKickTimes = DataManager.getCommConfig("book_activehelpcount", 10);
        if (roleBook.getKickTimes() >= maxKickTimes) {
            send(new ClientText(I18n.get("book.help.quyuan.maxtimes")));
            return;
        }
        roleBook.setKickTimes((short) (roleBook.getKickTimes() + 1));
        context().update(roleBook);
        ServiceHelper.bookService().awakePlayer(id(), target);
    }

    /**
     * 处理唤醒事件
     */
    public void handleKickEv(KickEvent ev) {
        // 先发送敲打结果，如果成功再发送获得奖励
        ClientBook res = new ClientBook();
        res.setResType(ClientBook.RES_AWAKE_QUYUAN);
        res.setResult(ev.getResult());
        res.setTarget("" + ev.getTarget());
        send(res);

        if (ev.getResult() != BookManager.KICK_SUCCESS) {
            short tmpkick = roleBook.getKickTimes();
            if (tmpkick > 0) {
                // 防止重置
                roleBook.setKickTimes((short) (tmpkick - 1));
                context().update(roleBook);
            }
        } else {
            if (null != ev.getToolmap() && ev.getToolmap().size() > 0) {
                ToolModule toolModule = (ToolModule) module(MConst.Tool);
                toolModule.addAndSend(ev.getToolmap(), EventType.AWAKE_QUYUAN.getCode());

                //发获奖提示到客户端
                ClientAward clientAward = new ClientAward(ev.getToolmap());
                send(clientAward);
            }
        }
        ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_BOOK, 1));
    }

    /**
     * 处理被唤醒事件
     */
    public void handleBeKickEv(BeKickEvent ev) {
        int now = DateUtil.getSecondTime();
        roleBook.setBeLastKickTime(now);
        short bekicktimes = roleBook.getBeKickTimes();
        roleBook.setBeKickTimes((short) (bekicktimes + 1));
        context().update(roleBook);
        for (Map.Entry<Integer, RoleBookUtil> entry : roleBookUtilMap.entrySet()) {
            if (entry.getValue().getEndReadTime() > now) {
                BookRead bookRead = BookManager.getBookRead(entry.getValue().getBookId(), entry.getValue().getBookLv());
                if (null == bookRead) {
                    continue;
                } else {
                    RoleBookUtil bookUtil = entry.getValue();
                    entry.getValue().setEndReadTime(bookUtil.getEndReadTime() - bookRead.getHelpaddtime());
                    ClientBook clientBook = new ClientBook();
                    clientBook.setResType(ClientBook.RES_BOOK_UPDATE_TIME);
                    clientBook.setBookId(bookUtil.getBookId());
                    clientBook.setStartReadTime(bookUtil.getStartReadTime());
                    clientBook.setEndReadTime(bookUtil.getEndReadTime());
                    clientBook.setRemainTime(bookUtil.getEndReadTime() - now);
                    send(clientBook);
                }
            }
        }
    }


    private void fireBookAchievementEvent(){
        if (roleBookUtilMap == null)
            return;
        BookAchieveEvent event = new BookAchieveEvent(roleBookUtilMap);
        eventDispatcher().fire(event);
    }

    /**
     * 典籍静态日志
     */
    private void bookStaticLog() {
        String str1 = "";
        String str2 = "";
        for (Map.Entry<Integer, BookInfo> entry : BookManager.bookInfoMap.entrySet()) {
            if (roleBookUtilMap.containsKey(entry.getKey())) {
                RoleBookUtil book = roleBookUtilMap.get(entry.getKey());
                if ("".equals(str1)) {
                    str1 = str1 + entry.getKey() + "@";
                    if (book.getBookNum() > entry.getValue().getReqitemmax() ||
                            book.getBookStatus() == BookManager.BOOK_ACTIVE) {
                        str1 = str1 + "999";
                    } else {
                        str1 = str1 + book.getBookNum();
                    }
                } else {
                    str1 = str1 + "&" + entry.getKey() + "@";
                    if (book.getBookNum() > entry.getValue().getReqitemmax() ||
                            book.getBookStatus() == BookManager.BOOK_ACTIVE) {
                        str1 = str1 + "999";
                    } else {
                        str1 = str1 + book.getBookNum();
                    }
                }
                if (book.getBookLv() > 0) {
                    if ("".equals(str2)) {
                        str2 = str2 + entry.getKey() + "@" + book.getBookLv();
                    } else {
                        str2 = str2 + "&" + entry.getKey() + "@" + book.getBookLv();
                    }
                }
            } else {
                if ("".equals(str1)) {
                    str1 = str1 + entry.getKey() + "@0";
                } else {
                    str1 = str1 + "&" + entry.getKey() + "@0";
                }
            }
        }
        String str = "book_id@number:" + str1 + "#book_id@lv:" + str2;
        ServerLogModule log = module(MConst.ServerLog);
        log.static_4_Log(ThemeType.STATIC_BOOK_LOG.getThemeId(), 1, str);
    }

    public boolean isBookActive(int bookId) {
        RoleBookUtil book = roleBookUtilMap.get(bookId);
        if (null == book) return false;
        return book.getBookStatus() == BookManager.BOOK_ACTIVE;
    }

    public Iterator<RoleBookUtil> roleBookUtilIterator() {
        return roleBookUtilMap.values().iterator();
    }

    public RoleBookUtil getRoleBookUtil(int bookId) {
        return roleBookUtilMap.get(bookId);
    }

    public String makeFsStr() {
        int stageFs = 0;
        int readFs = 0;
        for (RoleBookUtil po : roleBookUtilMap.values()) {
            BookInfo info = BookManager.getBookInfo(po.getBookId());
            BookStage stage;
            if (po.getBookStatus() == BookManager.BOOK_ACTIVE) {
                stage = BookManager.getMaxBookStage(po.getBookId());
            } else {
                if (po.getBookNum() >= info.getReqitemmax()) {
                    stage = BookManager.getSubMaxBookStage(po.getBookId());
                } else {
                    stage = BookManager.getBookStage(po.getBookId(), po.getBookNum());
                }
            }
            if (stage != null) {
                stageFs += FormularUtils.calFightScore(stage.getAttribute());
            }

            if (po.getBookStatus() == BookManager.BOOK_ACTIVE && po.getBookLv() > 0) {
                BookRead read = BookManager.getBookRead(po.getBookId(), po.getBookLv());
                if (null != read && null != read.getAttribute()) {
                    readFs += FormularUtils.calFightScore(read.getAttribute());
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("book_base:").append(stageFs).append("#")
                .append("book_read:").append(readFs).append("#");
        return sb.toString();
    }
}
