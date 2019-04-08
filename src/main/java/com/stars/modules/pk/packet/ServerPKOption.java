package com.stars.modules.pk.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.pk.PKModule;
import com.stars.modules.pk.PKPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

public class ServerPKOption extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    /* 参数 */
    private long invitee;// 邀请目标
    private long invitor;// 发起邀请者

    public ServerPKOption() {

    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", PKPacketSet.Server_PK_Option));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        PKModule pm = module(MConst.Pk);
        switch (reqType) {
            case 1:// 请求收到邀请列表
                pm.sendReceiveInvite();
                break;
            case 2:// 请求切磋记录
                pm.sendPvpRecord();
                break;
            case 3:// 邀请切磋
                pm.invitePK(invitee);
                break;
            case 4:// 同意请求
                pm.permitInvite(invitor);
                break;
            case 5:// 拒绝请求
                pm.refuseInvite(invitor, Boolean.TRUE);
                // 发送最新列表
                pm.sendReceiveInvite();
                break;
            case 6:// 拒绝所有请求
                pm.refuseAll();
                break;
        }
    }

    @Override
    public short getType() {
        return PKPacketSet.Server_PK_Option;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 3:// 邀请切磋
                invitee = Long.parseLong(buff.readString());
                break;
            case 4:// 同意请求
                invitor = Long.parseLong(buff.readString());
                break;
            case 5:// 拒绝请求
                invitor = Long.parseLong(buff.readString());
                break;
        }
    }

}
