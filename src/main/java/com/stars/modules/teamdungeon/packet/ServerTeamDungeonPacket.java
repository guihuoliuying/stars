package com.stars.modules.teamdungeon.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.teamdungeon.TeamDungeonModule;
import com.stars.modules.teamdungeon.TeamDungeonPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/11/17.
 */
public class ServerTeamDungeonPacket extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", TeamDungeonPacketSet.Server_TeamDungeon));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        TeamDungeonModule teamDungeonModule = module(MConst.TeamDungeon);
        switch (reqType) {
            case 0:// 请求组队副本
                teamDungeonModule.sendTeamDungeon();
                break;
            case 1:// 进入组队副本
                teamDungeonModule.enterDegeno();
                break;
            case 2:// 组队副本回城
                ServiceHelper.teamDungeonService().backToCity(getRoleId());
                break;
            case 3:// 在副本中死了，且没复活次数了，自动回城
                ServiceHelper.teamDungeonService().deadInDungeon(getRoleId());
                break;
            case 4:// 退出组队副本界面
                teamDungeonModule.quitFromTeamPage();
                break;
            case 5:// 死亡没复活次数了，点击离开回城
                teamDungeonModule.backToCity();
                break;
        }
    }

    @Override
    public short getType() {
        return TeamDungeonPacketSet.Server_TeamDungeon;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {

        }
    }
}
