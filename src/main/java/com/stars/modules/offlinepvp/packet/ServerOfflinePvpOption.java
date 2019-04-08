package com.stars.modules.offlinepvp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.offlinepvp.OfflinePvpPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/10/12.
 */
public class ServerOfflinePvpOption extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    public static final byte REFRESH_ENEMY = 1;// 手动刷新对手
    public static final byte REWARD = 2;// 领取进度奖励
    public static final byte BUY_REFRESH = 3;// 购买刷新次数
    public static final byte BUY_CHALLENGE = 4;// 购买挑战次数

    /* 参数 */
    private byte rewardIndex;// 进度奖励序号

    @Override
    public void execPacket(Player player) {
//        OfflinePvpModule opm = module(MConst.OfflinePvp);
//        switch (reqType) {
//            case REFRESH_ENEMY:
//                opm.executeRefreshEnemy();
//                break;
//            case REWARD:
//                opm.reward(rewardIndex);
//                break;
//            case BUY_REFRESH:
//                opm.buyRefreshNum();
//                break;
//            case BUY_CHALLENGE:
//                opm.buyChallengeNum();
//                break;
//            default:
//                break;
//        }
    }

    @Override
    public short getType() {
        return OfflinePvpPacketSet.S_OFFLINEPVP_OPTION;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case REFRESH_ENEMY:
                break;
            case REWARD:
                this.rewardIndex = buff.readByte();
                break;
            case BUY_REFRESH:
                break;
            case BUY_CHALLENGE:
                break;
            default:
                break;
        }
    }
}
