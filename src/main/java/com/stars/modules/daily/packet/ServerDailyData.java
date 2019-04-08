package com.stars.modules.daily.packet;


import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.daily.DailyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhanghaizhen on 2017/7/10.
 */
public class ServerDailyData extends PlayerPacket {

    public static final byte REQ_GET_TAG = 0x00; // 请求获得标签列表
    public static final byte REQ_GET_DAILYINFO_BY_TAG = 0x01; //请求返回某个标签下的数据
    public static final byte REQ_CHANGE_LUCK_DRAW_STATUS = 0x02; //请求改变已经抽过签的状态
    public static final byte REQ_DAILYBALL_LEVELUP = 0x03; //请求斗魂珠升级
    public static final byte REQ_DAILYBALL_DATA = 0x04; //请求魂珠数据



    private byte reqType;
    private byte chooseTag;

    @Override
    public short getType() {
        return DailyPacketSet.Server_DailyData;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        switch (reqType){
            case REQ_GET_TAG :
                break;
            case REQ_GET_DAILYINFO_BY_TAG:
                chooseTag = buff.readByte();
                break;
            case REQ_CHANGE_LUCK_DRAW_STATUS:
                break;
            case REQ_DAILYBALL_LEVELUP:
                break;
            case REQ_DAILYBALL_DATA:
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        DailyModule dailyModule = (DailyModule) module(MConst.Daily);
        switch (reqType){
            case REQ_GET_TAG :
                dailyModule.reqGetTagList();
                break;
            case REQ_GET_DAILYINFO_BY_TAG:
                dailyModule.getActivityListByTag(chooseTag);
                break;
            case REQ_CHANGE_LUCK_DRAW_STATUS:
                dailyModule.changeHadDrawTodayStatus();
                break;
            case REQ_DAILYBALL_LEVELUP:
                dailyModule.reqDailyBallLevelup();
                break;
            case REQ_DAILYBALL_DATA:
                dailyModule.sendDailyBall2Client();
                break;
            default:
                break;
        }
    }
}
