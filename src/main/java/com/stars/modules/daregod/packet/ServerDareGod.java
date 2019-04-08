package com.stars.modules.daregod.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.daregod.DareGodModule;
import com.stars.modules.daregod.DareGodPacketSet;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class ServerDareGod extends PlayerPacket {
    private static final byte VIEW = 0x00;//打开界面
    private static final byte VIEW_RANK = 0x01;//打开排行榜
    private static final byte BUY_TIME = 0x02;//购买次数
    private static final byte GET_TARGET_AWARD = 0x03;//领取目标奖励
    private static final byte ENTER_FIGHT = 0x04;//进入战斗

    private byte subType;
    private int buyTime;
    private int targetId;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        switch (subType) {
            case BUY_TIME:
                buyTime = buff.readInt();
                break;
            case GET_TARGET_AWARD:
                targetId = buff.readInt();
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        DareGodModule dareGod = (DareGodModule) moduleMap().get(MConst.DareGod);
        switch (subType) {
            case VIEW:
                dareGod.view();
                break;
            case VIEW_RANK:
                dareGod.viewRank();
                break;
            case BUY_TIME:
                dareGod.buyFightTime(buyTime);
                break;
            case GET_TARGET_AWARD:
                MainRpcHelper.dareGodService().getTargetAward(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(), dareGod.id(), targetId);
                break;
            case ENTER_FIGHT:
                MainRpcHelper.dareGodService().enterFight(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(), dareGod.id());
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return DareGodPacketSet.S_DAREGOD;
    }
}
