package com.stars.modules.guest.prodata;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestStageVo {

    private int guestId;    // 门客id
    private int level;  // 星级
    private String attribute;   // 属性
    private String reqItem;     // 需要的材料
    private int reqRoleLevel;   // 需要的角色等级
    private byte color;     // 颜色

    public byte getColor() {
        return color;
    }

    public void setColor(byte color) {
        this.color = color;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getReqItem() {
        return reqItem;
    }

    public void setReqItem(String reqItem) {
        this.reqItem = reqItem;
    }

    public int getReqRoleLevel() {
        return reqRoleLevel;
    }

    public void setReqRoleLevel(int reqRoleLevel) {
        this.reqRoleLevel = reqRoleLevel;
    }
}
