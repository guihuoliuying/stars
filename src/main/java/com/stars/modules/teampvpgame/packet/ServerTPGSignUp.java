package com.stars.modules.teampvpgame.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.teampvpgame.TeamPVPGameModule;
import com.stars.modules.teampvpgame.TeamPVPGamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/12/19.
 */
public class ServerTPGSignUp extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", TeamPVPGamePacketSet.S_TPG_SIGNUP));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        TeamPVPGameModule tpgModule = module(MConst.TeamPVPGame);
        switch (reqType) {
            case 1:// 提交报名
                tpgModule.submitSignUp();
                break;
            case 2:// 同意报名
                tpgModule.permitSignUp();
                break;
            case 3:// 拒绝报名
                tpgModule.refuseSignUp();
                break;
        }
    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.S_TPG_SIGNUP;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {

        }
    }
}
