package com.stars.modules.book.userdata;

import com.stars.core.attr.Attribute;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class RoleBookUtil extends DbRow {
    private long roleId;
    private int bookId;                 // 书籍id
    private int bookNum;                // 已书籍数量
    private short bookLv;               // 书籍领悟等级
    private byte bookStatus;            // 书籍是否激活状态(0未激活、1已激活)
    private int startReadTime;          // 书籍开始读时间
    private int endReadTime;            // 书籍结束读时间

    // 内存数据
    private Attribute attribute;
    private int power;

    public RoleBookUtil(){

    }

    public RoleBookUtil(long roleId) {
        this.roleId = roleId;
        this.bookId = 0;
        this.bookLv = 0;
        this.bookNum = 0;
        this.bookStatus = 0;
        this.startReadTime = 0;
        this.endReadTime = 0;
    }

    public long getRoleId() {
        return this.roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getBookId() {
        return this.bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getBookNum() {
        return this.bookNum;
    }

    public void setBookNum(int bookNum) {
        this.bookNum = bookNum;
    }

    public short getBookLv() {
        return this.bookLv;
    }

    public void setBookLv(short bookLv) {
        this.bookLv = bookLv;
    }

    public byte getBookStatus() {
        return this.bookStatus;
    }

    public void setBookStatus(byte bookStatus) {
        this.bookStatus = bookStatus;
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

    public Attribute getAttribute() {return this.attribute;}

    public void setAttribute(Attribute attribute) {this.attribute = attribute;}

    public int getPower() {return this.power;}

    public void setPower(int power) {this.power = power;}

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolebookutil", " `roleid`=" + roleId + " and bookid=" + bookId);
    }

    @Override
    public String getDeleteSql() {
        return null;
    }
}
