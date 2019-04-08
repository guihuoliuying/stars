package com.stars.modules.mooncake.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.mooncake.MoonCakeModule;
import com.stars.modules.mooncake.MoonCakePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by zhangerjiang on 2017/9/14.
 */
public class ServerMoonCake extends PlayerPacket {

    public static final byte REQ_VIEW = 0x00;   //请求打开界面
    public static final byte REQ_BEGIN = 0x01;  //请求开始游戏
    public static final byte REQ_UPDATE_SCORE = 0x02;//请求更新积分
    public static final byte REQ_QUIT = 0x03;   //主动请求退出游戏，积分不做记录
    public static final byte REQ_FINISH = 0x04; //请求结束游戏
    public static final byte REQ_GETRWD = 0x05; //请求获取奖励
    public static final byte REQ_VIEW_RANK = 0x06;//打开排行榜

    private byte subtype;
    private int score;
    private int nowScore;

    @Override
    public void execPacket(Player player) {
        MoonCakeModule moonCakeModule = module(MConst.MoonCake);
        LogUtil.info("subType:{}", subtype);
        switch (subtype) {
            case REQ_VIEW:
                moonCakeModule.viewMainUI();
                break;
            case REQ_BEGIN:
                moonCakeModule.beginMoonCakeGame();
                break;
            case REQ_UPDATE_SCORE:
                moonCakeModule.updateMaxScore(nowScore);
                break;
            case REQ_QUIT:
                moonCakeModule.endMoonCakeGame(0);
                break;
            case REQ_FINISH:
                moonCakeModule.endMoonCakeGame(nowScore);
                break;
            case REQ_GETRWD:
                moonCakeModule.getScoreReward(score);
                break;
            case REQ_VIEW_RANK:
                ServiceHelper.moonCakeService().viewRank(player.id());
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return MoonCakePacketSet.S_MOONCAKE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_VIEW:
                break;
            case REQ_GETRWD:
                score = buff.readInt();
                break;
            case REQ_UPDATE_SCORE:
                nowScore = buff.readInt();
                break;
            case REQ_QUIT:
                //nowScore = buff.readInt();
                break;
            case REQ_FINISH:
                nowScore = buff.readInt();
                break;
        }
    }
}
