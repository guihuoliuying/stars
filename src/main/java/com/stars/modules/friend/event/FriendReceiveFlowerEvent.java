package com.stars.modules.friend.event;

import com.stars.core.event.Event;
import com.stars.services.friend.userdata.FriendRolePo;
import com.stars.services.friend.userdata.ReceiveFlowerRecordPo;

/**
 * Created by chenkeyu on 2017/2/20 15:34
 */
public class FriendReceiveFlowerEvent extends Event {
    private FriendRolePo rolePo;
    private ReceiveFlowerRecordPo recordPo;

    public FriendReceiveFlowerEvent(FriendRolePo rolePo, ReceiveFlowerRecordPo recordPo) {
        this.rolePo = rolePo;
        this.recordPo = recordPo;
    }

    public FriendRolePo getRolePo() {
        return rolePo;
    }

    public ReceiveFlowerRecordPo getRecordPo() {
        return recordPo;
    }
}
