package com.stars.multiserver.fight.message;

/**
 * 通知战斗服战斗准备完成
 * Created by zhouyaohui on 2016/11/15.
 */
public class NoticeFightServerReady {
    private byte[] data;
    private String fightId;
    private int serverId;

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fighting) {
        this.fightId = fighting;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
