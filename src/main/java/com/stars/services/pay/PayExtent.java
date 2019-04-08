package com.stars.services.pay;

/**
 * Created by zhoujin on 2017/4/5.
 */
public class PayExtent {
    private int id;
    private byte point;
    public PayExtent() {
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public byte getPoint() {
        return this.point;
    }
    public void setPoint(byte point) {
        this.point = point;
    }
}
