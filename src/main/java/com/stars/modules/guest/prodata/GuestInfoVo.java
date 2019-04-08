package com.stars.modules.guest.prodata;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestInfoVo {

    private int guestId;    // 门客id
    private String name;    // 门客名字
    private String describe;    // 描述
    private String image;   // 图片
    private byte sort;      // 排序
    private int helpCount;  // 每次求助获得的数量
    private String headIcon;    // 头像
    private byte quality;   // 品质
    private byte whetherHelp; //是否能求助

    public byte getQuality() {
        return quality;
    }

    public void setQuality(byte quality) {
        this.quality = quality;
    }

    public String getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public byte getSort() {
        return sort;
    }

    public void setSort(byte sort) {
        this.sort = sort;
    }

    public int getHelpCount() {
        return helpCount;
    }

    public void setHelpCount(int helpCount) {
        this.helpCount = helpCount;
    }

    public byte getWhetherHelp() {
        return whetherHelp;
    }

    public void setWhetherHelp(byte whetherHelp) {
        this.whetherHelp = whetherHelp;
    }

}
