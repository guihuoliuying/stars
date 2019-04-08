package com.stars.multiserver.fight.message;

import com.stars.multiserver.packet.NewFighterToFightActor;

/**
 * Created by zhouyaohui on 2016/11/9.
 */
public class AddNewfighterToFightActor {
    private int serverId;
    private NewFighterToFightActor newer;
    private boolean noticeServer;

    public boolean isNoticeServer() {
        return noticeServer;
    }

    public void setNoticeServer(boolean noticeServer) {
        this.noticeServer = noticeServer;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public NewFighterToFightActor getNewer() {
        return newer;
    }

    public void setNewer(NewFighterToFightActor newer) {
        this.newer = newer;
    }
}
