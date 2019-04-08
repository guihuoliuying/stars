package com.stars.modules.newequipment.prodata;

/**
 * Created by zhanghaizhen on 2017/6/8.
 */
public class TokenMeltVo {
    private byte meltType;
    private String param;
    private String meltItem;

    public byte getMeltType() {
        return meltType;
    }

    public void setMeltType(byte meltType) {
        this.meltType = meltType;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getMeltItem() {
        return meltItem;
    }

    public void setMeltItem(String meltItem) {
        this.meltItem = meltItem;
    }
}
