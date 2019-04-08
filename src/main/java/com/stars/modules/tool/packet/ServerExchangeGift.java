package com.stars.modules.tool.packet;

import com.stars.core.gmpacket.giftpackage.ExchangeGiftPackageGm;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;

/**
 * Created by liuyuheng on 2017/2/10.
 */
public class ServerExchangeGift extends PlayerPacket {
    private String activateCode;// 激活码

    public static int LENGTH_LIMIT = 13;

    @Override
    public void execPacket(Player player) {
        if(activateCode == null || activateCode.length() < LENGTH_LIMIT){
            PacketManager.send(getSession(), new ClientText("请输入正确的兑换码"));
            return;
        }

        ServerLogModule serverLogModule = module(MConst.ServerLog);
        int channel = Integer.parseInt(serverLogModule.getMainChannel());
        ExchangeGiftPackageGm packageGm = new ExchangeGiftPackageGm(activateCode, getRoleId(), getRoleId(), channel,
                serverLogModule.getAccount());
        packageGm.checkingGiftId();
    }

    @Override
    public short getType() {
        return ToolPacketSet.S_EXCHANGE_GIFT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.activateCode = buff.readString();
    }
}
