package com.stars.modules.book.prodata;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class OpenHoleInfo {
    private byte holeid;
    private short lv;
    private short viplv;
    private int itemid;
    private int itemnum;

    public OpenHoleInfo() {}

    public byte getHoleId() {return this.holeid;}

    public void setHoleId(byte holeId) {this.holeid = holeId;}

    public short getLv() {return this.lv;}

    public void setLv(short lv) {this.lv = lv;}

    public short getViplv() {return this.viplv;}

    public void setViplv(short viplv) {this.viplv = viplv;}

    public int getItemId() {return this.itemid;}

    public void setItemId(int itemId) {this.itemid = itemId;}

    public int getItemNum() {return this.itemnum;}

    public void setItemNum(int itemNum) {this.itemnum = itemNum;}
}
