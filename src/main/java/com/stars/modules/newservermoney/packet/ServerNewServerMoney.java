package com.stars.modules.newservermoney.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.newservermoney.NewServerMoneyModule;
import com.stars.modules.newservermoney.NewServerMoneyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2017/1/5.
 */
public class ServerNewServerMoney extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    @Override
    public void execPacket(Player player) {
        NewServerMoneyModule nsMoneyModule = module(MConst.NewServerMoney);
        switch (reqType) {
            case 1:// 请求数据
                nsMoneyModule.reqData();
                break;
            case 2:// 我的获奖记录
                nsMoneyModule.reqRewardRecord();
                break;
        }
    }

    @Override
    public short getType() {
        return NewServerMoneyPacketSet.S_NSMONEY;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
        }
    }
}
