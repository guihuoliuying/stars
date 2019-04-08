package com.stars.modules.baby.prodata;

/**
 * Created by chenkeyu on 2017-08-01.
 */
public class BabyFashion {
    private int id;
    private String follow;
    private int monsterId;
    private int moveSpeed;
    private int groupid;
    private String groupName;// '时装组',
    private String name;// '时装名称',
    private String attr;// '属性',
    private int activateCode;// '激活道具ID',
    private String icon;// '标签ICON',

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public int getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(int moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public int getGroupid() {
        return groupid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public int getActivateCode() {
        return activateCode;
    }

    public void setActivateCode(int activateCode) {
        this.activateCode = activateCode;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "BabyFashion{" +
                "id=" + id +
                ", follow='" + follow + '\'' +
                ", monsterId=" + monsterId +
                ", moveSpeed=" + moveSpeed +
                '}';
    }
}
