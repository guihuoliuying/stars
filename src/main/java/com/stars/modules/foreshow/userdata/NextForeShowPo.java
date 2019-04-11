package com.stars.modules.foreshow.userdata;

/**
 * Created by wuyuxing on 2017/3/14.
 */
public class NextForeShowPo {
    private String openname;
    private boolean showEffect;

    public NextForeShowPo(String openname) {
        this.openname = openname;
        this.showEffect = false;
    }

    public String getOpenname() {
        return openname;
    }

    public void setOpenname(String openname) {
        this.openname = openname;
    }

    public boolean isShowEffect() {
        return showEffect;
    }

    public void setShowEffect(boolean showEffect) {
        this.showEffect = showEffect;
    }
}
