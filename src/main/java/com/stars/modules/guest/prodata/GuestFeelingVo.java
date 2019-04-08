package com.stars.modules.guest.prodata;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestFeelingVo {

    private int guestFeelId;    // id
    private String guestGroup;  // 门客组
    private String name;    // 情缘名字
    private String describe;    // 情缘描述
    private String attribute;   // 属性
    private String func;    // 任务加成

    private Set<Integer> guestSet = new HashSet<>();

    public int getGuestFeelId() {
        return guestFeelId;
    }

    public void setGuestFeelId(int guestFeelId) {
        this.guestFeelId = guestFeelId;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGuestGroup() {
        return guestGroup;
    }

    public void setGuestGroup(String guestGroup) {
        this.guestGroup = guestGroup;
        String[] guests = guestGroup.split("[+]");
        for (String guest : guests) {
            guestSet.add(Integer.valueOf(guest));
        }
    }

    public boolean contains(int guestId) {
        return guestSet.contains(guestId);
    }

    public Set<Integer> group() {
        return guestSet;
    }
}
