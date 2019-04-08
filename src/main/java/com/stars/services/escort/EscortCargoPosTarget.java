package com.stars.services.escort;

/**
 * Created by wuyuxing on 2016/12/13.
 */
public class EscortCargoPosTarget {
    private byte index;
    private String position;
    private boolean isFinish;

    public EscortCargoPosTarget(byte index, String position) {
        this.index = index;
        this.position = position;
        this.isFinish = false;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean isFinish) {
        this.isFinish = isFinish;
    }
}
