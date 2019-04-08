package com.stars.modules.book.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.util.StringUtil;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class BookRead {
    private int bookid;
    private short level;
    private int reqtime;
    private String attr;
    private int fightPower;// 可增加战力
    private Attribute attribute = new Attribute();//属性
    private String lvupreqitem;
    private String describe;
    private short rolelevel;
    private int helpaddtime;

    public BookRead() {}

    public int getBookid() {return this.bookid;}

    public void setBookid(int bookid) {this.bookid = bookid;}

    public short getLevel() {return this.level;}

    public void setLevel(short level) {this.level = level;}

    public int getReqtime() {return this.reqtime;}

    public void setReqtime(int reqtime) {this.reqtime = reqtime;}

    public String getAttr() {return this.attr;}

    public void setAttr(String attr) {
        this.attr = attr;
        if (StringUtil.isEmpty(attr) || "0".equals(attr)) {
            return;
        }
        attribute = new Attribute(attr);
        this.fightPower = FormularUtils.calFightScore(attribute);
    }

    public int getFightPower() {return this.fightPower;}

    public Attribute getAttribute() {return this.attribute;}

    public String getLvupreqitem() {return this.lvupreqitem;}

    public void setLvupreqitem(String lvupreqitem) {this.lvupreqitem = lvupreqitem;}

    public String getDescribe() {return this.describe;}

    public void setDescribe(String describe) {this.describe = describe;}

    public short getRolelevel() {return this.rolelevel;}

    public void setRolelevel(short rolelevel) {this.rolelevel = rolelevel;}

    public int getHelpaddtime() {return this.helpaddtime;}

    public void setHelpaddtime(int helpaddtime) {this.helpaddtime = helpaddtime;}
}
