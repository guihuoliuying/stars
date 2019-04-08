package com.stars.modules.elitedungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.elitedungeon.EliteDungeonModule;
import com.stars.modules.elitedungeon.EliteDungeonPacketSet;
import com.stars.modules.elitedungeon.recordmap.RecordMapEliteDungeon;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;

/**
 * Created by gaopeidian on 2017/4/11.
 */
public class ServerEliteDungeonPacket extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    @Override
    public void execPacket(Player player) {
        EliteDungeonModule eliteDungeonModule = module(MConst.EliteDungeon);
        switch (reqType) {
//            case 0:// 请求组队副本
//                teamDungeonModule.sendTeamDungeon();
//                break;
            case 1:// 进入组队副本
                eliteDungeonModule.enterDegeno(false);
                break;
            case 2:// 组队副本回城
                ServiceHelper.eliteDungeonService().backToCity(getRoleId());
                break;
            case 3:// 在副本中死了，且没复活次数了，自动回城
                ServiceHelper.eliteDungeonService().deadInDungeon(getRoleId());
                ServerLogModule serverLogModule = module(MConst.ServerLog);
                serverLogModule.logTeamFinish(null, 0);
                RecordMapEliteDungeon record = eliteDungeonModule.getRecord();
                serverLogModule.logBaseTeamFinish(record.getPlayCount(), 0, 0, 0);
                break;
            case 4:// 退出组队副本界面
                eliteDungeonModule.quitFromTeamPage();
                break;
            case 5:// 死亡没复活次数了，点击离开回城
                eliteDungeonModule.backToCity();
                break;
            case 6:// 强行进入组队副本
                eliteDungeonModule.enterDegeno(true);
                break;
        }
    }

    @Override
    public short getType() {
        return EliteDungeonPacketSet.Server_EliteDungeon;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
    }
}
