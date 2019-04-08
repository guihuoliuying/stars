package com.stars.modules.masternotice.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.masternotice.MasterNoticeModule;
import com.stars.modules.masternotice.MasterNoticePacketSet;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ServerMasterNoticePageInfo  extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
    	MasterNoticeModule masterNoticeModule = (MasterNoticeModule)this.module(MConst.MasterNotice);
    	masterNoticeModule.requsetMasterNoticePageInfo();
    }

    @Override
    public short getType() {
        return MasterNoticePacketSet.S_MASTER_PAGE_INFO;
    }  
}
