package com.stars.modules.book.prodata;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class BookInfo {
    private int bookid;
    private String name;
    private String icon;//icon图片
    private byte quality;
    private int reqitem;
    private int reqitemmax;
    private String infodesc;
    private byte display;
    private int helpaward;
    private int rank;
    private short maxlv;

    public BookInfo() {

    }

    public int getBookid() {return this.bookid;}

    public void setBookid(int bookid) {this.bookid = bookid;}

    public String getName() {return this.name;}

    public void setName(String name) {this.name = name;}

    public byte getQuality() {return this.quality;}

    public void setQuality(byte quality) {this.quality = quality;}

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getReqitem() {return this.reqitem;}

    public void setReqitem(int reqitem) {this.reqitem = reqitem;}

    public int getReqitemmax() {return this.reqitemmax;}

    public void setReqitemmax(int reqitemmax) {this.reqitemmax = reqitemmax;}

    public String getInfodesc() {return this.infodesc;}

    public void setInfodesc(String infodesc) {this.infodesc = infodesc;}

    public byte getDisplay() {return this.display;}

    public void setDisplay(byte display) {this.display = display;}

    public int getHelpaward() {return this.helpaward;}

    public void setHelpaward(int helpaward) {this.helpaward = helpaward;}

    public int getRank() {return this.rank;}

    public void setRank(int rank) {this.rank = rank;}

    public short getMaxlv() {return this.maxlv;}

    public void setMaxlv(short maxlv) {this.maxlv = maxlv;}
}
