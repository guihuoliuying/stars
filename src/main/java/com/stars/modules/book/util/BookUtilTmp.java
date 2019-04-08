package com.stars.modules.book.util;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class BookUtilTmp {
    private int bookId;                 // 书籍id
    private short bookLv;               // 书籍领悟等级
    private int startReadTime;          // 书籍开始读时间
    private int endReadTime;            // 书籍结束读时间

    public BookUtilTmp() {}

    public BookUtilTmp(int bookId) {
        this.bookId = bookId;
    }

    public BookUtilTmp(int bookId,short bookLv,int startReadTime,int endReadTime){
        this.bookId = bookId;
        this.bookLv = bookLv;
        this.startReadTime = startReadTime;
        this.endReadTime = endReadTime;
    }

    public int getBookId() {
        return this.bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public short getBookLv() {
        return this.bookLv;
    }

    public void setBookLv(short bookLv) {
        this.bookLv = bookLv;
    }

    public int getStartReadTime() {
        return this.startReadTime;
    }

    public void setStartReadTime(int startReadTime) {
        this.startReadTime = startReadTime;
    }

    public int getEndReadTime() {
        return this.endReadTime;
    }

    public void setEndReadTime(int endReadTime) {
        this.endReadTime = endReadTime;
    }
}
