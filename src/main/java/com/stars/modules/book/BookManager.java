package com.stars.modules.book;

import com.stars.modules.book.prodata.BookInfo;
import com.stars.modules.book.prodata.BookRead;
import com.stars.modules.book.prodata.BookStage;
import com.stars.modules.book.prodata.OpenHoleInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class BookManager {
    // 常量
    public static byte BOOK_NOACTIVE = 0;
    public static byte BOOK_ACTIVE = 1;

    public static byte KICK_FAIL = 0;
    public static byte KICK_SUCCESS = 1;
    public static byte KICK_PLAYER_OFFLINE = 2;
    public static byte KICK_BEKICK_LIMIT = 3;
    public static byte KICK_CD = 4;
    public static byte KICK_BOOK_READED = 5;


    public static byte IOCN_NOMAL = 0;
    public static byte IOCN_FRIEND = 1;
    public static byte IOCN_FAMILY = 2;

    public static int READ_BOOK_IMMEDIATELY_VIP;

    public static int INTERVAL;//快速完成 需要消耗1元宝的时间单位

    public static Map<Byte, OpenHoleInfo> openHoleInfoMap = new HashMap<>();
    public static Map<Integer, Map<Short, BookRead>> bookReadMap = new HashMap<>();
    public static Map<Integer, BookInfo> bookInfoMap = new HashMap<>();
    public static Map<Integer, LinkedList<BookStage>> bookStageMap = new HashMap<>();

    public static OpenHoleInfo getOpenHoleInfo(byte holeId) {
        return openHoleInfoMap.get(holeId);
    }

    public static BookRead getBookRead(int bookId, short bookLv) {
        if (!bookReadMap.containsKey(bookId))
            return null;
        return bookReadMap.get(bookId).get(bookLv);
    }

    public static BookInfo getBookInfo(int bookId) {
        return bookInfoMap.get(bookId);
    }

    public static BookStage getBookStage(int bookId, int num) {
        if (!bookStageMap.containsKey(bookId))
            return null;
        LinkedList<BookStage> list = bookStageMap.get(bookId);
        if (null == list) return null;
        int curStage = -1;
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).getStage() > num) {
                break;
            }
            curStage = i;
        }
        if (-1 == curStage) return null;
        return list.get(curStage);
    }

    public static BookStage getMaxBookStage(int bookId) {
        if (!bookStageMap.containsKey(bookId))
            return null;
        LinkedList<BookStage> list = bookStageMap.get(bookId);
        if (null == list || list.size() <= 0) return null;
        int size = list.size();
        return list.get(size - 1);
    }

    /**
     * 获取典籍倒数第二个阶段属性
     *
     * @param bookId
     * @return
     */
    public static BookStage getSubMaxBookStage(int bookId) {
        if (!bookStageMap.containsKey(bookId))
            return null;
        LinkedList<BookStage> list = bookStageMap.get(bookId);
        if (null == list || list.size() <= 1) return null;
        return list.get(list.size() - 2);
    }
}
