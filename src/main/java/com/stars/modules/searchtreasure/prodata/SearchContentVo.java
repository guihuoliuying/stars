package com.stars.modules.searchtreasure.prodata;

/**
 * 仙山探宝内容表;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchContentVo {
    private int contentid;
    private byte type;
    private String param;

    public int getContentid() {
        return contentid;
    }

    public void setContentid(int contentid) {
        this.contentid = contentid;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
