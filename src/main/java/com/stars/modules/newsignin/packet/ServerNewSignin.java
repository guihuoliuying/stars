package com.stars.modules.newsignin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.newsignin.NewSigninModule;
import com.stars.modules.newsignin.NewSigninPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017/2/6 10:48
 */
public class ServerNewSignin extends PlayerPacket {
    public static final byte view = 0x00;           //打开页面
    public static final byte singleSign = 0x01;     //签到or补签
    public static final byte accumulateAward = 0x02;//累积奖励

    private byte subtype;
    private String signDate;
    private int times;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype){
            case singleSign:
                signDate = buff.readString();
                break;
            case accumulateAward:
                times = buff.readInt();
                break;
        }
    }
    @Override
    public void execPacket(Player player) {
        NewSigninModule module = module(MConst.SignIn);
        switch (subtype){
            case view:
                module.flushProDataToClient();
                module.flushRoleSigninToClient();
                break;
            case singleSign:
                module.doSignin(signDate);
                break;
            case accumulateAward:
                module.accumulateAward(times);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return NewSigninPacketSet.S_SignIn;
    }


}
