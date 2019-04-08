package com.stars.multiserver.fight.message;

/**
 * Created by wuyuxing on 2016/12/13.
 * 单纯只做战斗服lua的命令传递
 */
public class NoticeFightServerAddServerOrder {
    private byte[] data;
    private String fightId;
    private int serverId;

    public NoticeFightServerAddServerOrder() {
    }

    public NoticeFightServerAddServerOrder(String fightId, int serverId,byte[] data) {
        this.data = data;
        this.fightId = fightId;
        this.serverId = serverId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
}
