package com.stars.modules.luckydraw.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.luckydraw.LuckyDrawModule;
import com.stars.modules.luckydraw.LuckyDrawPacketSet;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class ServerLuckyDrawPacket extends PlayerPacket {
    private short subType;
    private int actType;
    public final static short REQ_MainUiData = 1;//打开界面的数据
    public final static short REQ_LuckyDraw = 2;//抽奖
    private int time;//抽几次

    @Override
    public void execPacket(Player player) {
        LuckyDrawModule luckyDrawModule = null;
        switch (actType){
            case OperateActivityConstant.ActType_LuckyDraw:{
                luckyDrawModule = module(MConst.LuckyDraw);
            }break;
            case OperateActivityConstant.ActType_LuckyDraw1:{
                luckyDrawModule = module(MConst.LuckyDraw1);
            }break;
            case OperateActivityConstant.ActType_LuckyDraw2:{
                luckyDrawModule = module(MConst.LuckyDraw2);
            }break;
            case OperateActivityConstant.ActType_LuckyDraw3:{
                luckyDrawModule = module(MConst.LuckyDraw3);
            }break;
            case OperateActivityConstant.ActType_LuckyDraw4:{
                luckyDrawModule = module(MConst.LuckyDraw4);
            }break;
        }
        switch (subType) {
            case REQ_MainUiData: {
                luckyDrawModule.reqMainUiData();
            }
            break;
            case REQ_LuckyDraw: {
                luckyDrawModule.reqLuckyDraw(time);
            }
            break;
        }
    }

    @Override
    public short getType() {
        return LuckyDrawPacketSet.S_LUCKY_DRAW;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        actType = buff.readInt();
        subType = buff.readShort();
        switch (subType) {
            case REQ_LuckyDraw: {
                time = buff.readInt();
            }
            break;
        }
    }
}
