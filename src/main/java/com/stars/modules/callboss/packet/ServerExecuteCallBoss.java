package com.stars.modules.callboss.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.callboss.CallBossModule;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/9/8.
 */
public class ServerExecuteCallBoss extends PlayerPacket {
    private int bossId;
    private byte selectRewardGroup;// 选择召唤奖励组

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", CallBossPacketSet.S_EXECUTE_CALLBOSS));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        CallBossModule callBossModule = (CallBossModule) module(MConst.CallBoss);
        callBossModule.callBoss(bossId, selectRewardGroup);
    }

    @Override
    public short getType() {
        return CallBossPacketSet.S_EXECUTE_CALLBOSS;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.bossId = buff.readInt();
        this.selectRewardGroup = buff.readByte();// 选择召唤奖励组
    }
}
