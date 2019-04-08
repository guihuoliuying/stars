package com.stars.modules.camp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.activity.imp.QiChuDaZuoZhanActivity;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/7/20.
 */
public class ServerCampFightPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_MAIN_UI_DATA = 1;//请求主界面数据
    public static final short REQ_TAKE_REWARD = 2;//请求领取奖励
    public static final short REQ_START_MATCHING = 3;//请求开始匹配
    public static final short REQ_CANCEL_MATCHING = 4;//请求取消匹配
    public static final short REQ_CONTINUE_FIGHT = 5;//请求继续战斗
    public static final short REQ_RANK_OF_THE_ROOM = 6;//请求本场积分排行榜
    private int score;
    private String fightUid;

    public ServerCampFightPacket() {
    }

    public ServerCampFightPacket(short subType) {
        this.subType = subType;
    }

    @Override
    public void execPacket(Player player) {
        CampModule module = module(MConst.Camp);
        QiChuDaZuoZhanActivity qiChuDaZuoZhanActivity = (QiChuDaZuoZhanActivity) module.getCampActivityById(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN);
        switch (subType) {
            case REQ_MAIN_UI_DATA: {
                qiChuDaZuoZhanActivity.reqMainUIDate();
            }
            break;
            case REQ_START_MATCHING: {
                qiChuDaZuoZhanActivity.startMatching();
            }
            break;
            case REQ_CANCEL_MATCHING: {
                qiChuDaZuoZhanActivity.cancelMatching();
            }
            break;
            case REQ_TAKE_REWARD: {
                qiChuDaZuoZhanActivity.takeScoreReward(score);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_TAKE_REWARD: {
                score = buff.readInt();
            }
            break;
            case REQ_CONTINUE_FIGHT: {
                fightUid = buff.readString();
            }
        }
    }

    @Override
    public short getType() {
        return CampPackset.S_CAMP_FIGHT;
    }

    public String getFightUid() {
        return fightUid;
    }

    public void setFightUid(String fightUid) {
        this.fightUid = fightUid;
    }

    public short getSubType() {
        return subType;
    }
}
