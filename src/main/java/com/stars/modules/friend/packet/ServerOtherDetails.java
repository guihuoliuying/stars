package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.Summary;

/**
 * Created by zhaowenshuo on 2016/8/16.
 */
public class ServerOtherDetails extends PlayerPacket {

    private long otherId;

    @Override
    public void execPacket(Player player) {
        FriendModule friendModule = module(MConst.Friend);
        LoginModule loginModule = module(MConst.Login);
        Summary selfSummary = null;
        Summary otherSummary = null;
        selfSummary = ServiceHelper.summaryService().getSummary(getRoleId());
        // 获取别人的
        otherSummary = ServiceHelper.summaryService().getSummary(otherId);
        if (selfSummary == null || otherSummary == null) {
            PlayerUtil.send(getRoleId(), new ClientText("请求异常"));
            return;
        }
        // 下发
        ClientOtherDetails detailsPacket = new ClientOtherDetails(
                selfSummary, otherSummary, friendModule.isFriend(otherId), true);
        PlayerUtil.send(getRoleId(), detailsPacket);
        friendModule.fireSpecialAccountLogEvent("请求对方详细信息");
    }

    @Override
    public short getType() {
        return FriendPacketSet.S_OTHER_DETAILS;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.otherId = Long.parseLong(buff.readString());
    }
}
