package com.stars.modules.book.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.util.StringUtil;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class BookStage {
    private int bookid;
    private int stage;
    private String desctitle;
    private String describe;
    private String attr;
    private byte order;
    private int fightPower;// 可增加战力
    private Attribute attribute = new Attribute();//属性

    public BookStage(){}

    public int getBookid() {return this.bookid;}

    public void setBookid(int bookid) {this.bookid = bookid;}

    public int getStage() {return this.stage;}

    public void setStage(int stage) {this.stage = stage;}

    public String getDesctitle() {return this.desctitle;}

    public void setDesctitle(String describe) {this.desctitle = desctitle;}

    public String getDescribe() {return this.describe;}

    public void setDescribe(String describe) {this.describe = describe;}

    public String getAttr() {return this.attr;}

    public void setAttr(String attr) {
        this.attr = attr;
        if (StringUtil.isEmpty(attr) || "0".equals(attr)) {
            return;
        }
        attribute = new Attribute(attr);
        this.fightPower = FormularUtils.calFightScore(attribute);
    }

    public byte getOrder() {
        return order;
    }

    public void setOrder(byte order) {
        this.order = order;
    }

    public int getFightPower() {return this.fightPower;}

    public Attribute getAttribute() {return this.attribute;}
}
