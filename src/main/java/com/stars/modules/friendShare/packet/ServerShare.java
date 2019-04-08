package com.stars.modules.friendShare.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.friendShare.ShareModule;
import com.stars.modules.friendShare.SharePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/6/7.
 */
public class ServerShare extends PlayerPacket {

    public static final byte REQ_VIEW = 0x01;   // 请求打开界面
    public static final byte REQ_SHARE = 0x02;  // 朋友圈分享回调
    public static final byte REQ_AWARD = 0x03;  // 领取分享奖励

    public byte subtype;

    @Override
    public void execPacket(Player player) {
        ShareModule shareModule = module(MConst.FriendShare);
        switch (subtype) {
            case REQ_VIEW:
                shareModule.view();
                break;
            case REQ_SHARE:
                shareModule.share();
                break;
            case REQ_AWARD:
                shareModule.award();
                break;
        }
    }

    @Override
    public short getType() {
        return SharePacketSet.S_SHARE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }

}
