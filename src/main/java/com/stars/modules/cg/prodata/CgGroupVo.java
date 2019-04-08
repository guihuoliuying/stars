package com.stars.modules.cg.prodata;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class CgGroupVo {
    private int cgGroupId;
    private String cginfo;
    private String before;
    private String after;
    private String sound;
    private String text;

    public int getCgGroupId() {
        return cgGroupId;
    }

    public void setCgGroupId(int cgGroupId) {
        this.cgGroupId = cgGroupId;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCginfo() {
        return cginfo;
    }

    public void setCginfo(String cginfo) {
        this.cginfo = cginfo;
    }
}
