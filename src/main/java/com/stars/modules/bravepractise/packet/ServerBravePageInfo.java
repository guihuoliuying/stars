package com.stars.modules.bravepractise.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.bravepractise.BravePractiseModule;
import com.stars.modules.bravepractise.BravePractisePacketSet;

/**
 * 客户端请求勇者试炼 页面数据
 * Created by gaopeidian on 2016/11/17.
 */
public class ServerBravePageInfo  extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
    	BravePractiseModule bravePractiseModule = (BravePractiseModule)this.module(MConst.BravePractise);
    	bravePractiseModule.sendBravePageInfo();
    }

    @Override
    public short getType() {
        return BravePractisePacketSet.S_BRAVE_PAGE_INFO;
    }  
}
