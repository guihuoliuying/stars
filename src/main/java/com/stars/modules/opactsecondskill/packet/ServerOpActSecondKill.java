package com.stars.modules.opactsecondskill.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.opactsecondskill.OpActSecondKillModule;
import com.stars.modules.opactsecondskill.OpActSecondKillPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class ServerOpActSecondKill extends PlayerPacket {
    public static final byte REQ_VIEW = 0x00; // 查看活动信息
    public static final byte REQ_CHANGE_ITEM = 0x01; //更换物品
    public static final byte REQ_BUY_ITEM = 0x02; //购买物品


    private byte subType; //子协议
    private int buyId; //购买道具的id

    @Override
    public short getType() {
        return OpActSecondKillPacketSet.S_OpActSecondSkill;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        super.writeToBuffer(buff);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        switch (subType){
            case REQ_VIEW:
                break;
            case REQ_CHANGE_ITEM:
                break;
            case REQ_BUY_ITEM:
                buyId = buff.readInt();
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        OpActSecondKillModule opActSecondKillModule = module(MConst.OpActSecondKill);
        switch (subType){
            case REQ_VIEW:
                opActSecondKillModule.viewPushInfo();
                break;
            case REQ_CHANGE_ITEM:
                opActSecondKillModule.changeNextItem();
                break;
            case REQ_BUY_ITEM:
                opActSecondKillModule.buyItem(buyId);
                break;
            default:
                break;
        }

    }
}
