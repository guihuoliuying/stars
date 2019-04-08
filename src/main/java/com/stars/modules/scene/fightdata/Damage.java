package com.stars.modules.scene.fightdata;

/**
 * Created by liuyuheng on 2016/9/22.
 */
public class Damage {
    private String giverId;
    private String receiverId;
    private int value;

    public Damage(String giverId, String receiverId, int value) {
        this.giverId = giverId;
        this.receiverId = receiverId;
        this.value = value;
    }

    public String getGiverId() {
        return giverId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public int getValue() {
        return value;
    }

}
