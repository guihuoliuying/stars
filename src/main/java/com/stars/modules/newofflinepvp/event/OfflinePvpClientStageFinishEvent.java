package com.stars.modules.newofflinepvp.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-03-13 10:48
 */
public class OfflinePvpClientStageFinishEvent extends Event {
    private byte finish;
    private int myRank;
    private int updateRank;

    public OfflinePvpClientStageFinishEvent(byte finish, int myRank, int updateRank) {
        this.finish = finish;
        this.myRank = myRank;
        this.updateRank = updateRank;
    }

    public byte getFinish() {
        return finish;
    }

    public int getMyRank() {
        return myRank;
    }

    public int getUpdateRank() {
        return updateRank;
    }
}
