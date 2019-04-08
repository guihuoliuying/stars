package com.stars.modules.camp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.CampPackset;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class ServerOfficerPacket extends PlayerPacket {
    private short subType;
    private static final short REQ_UPGRADE_OFFICER = 1;//请求升级官职
    private static final short REQ_MY_CAMP_OFFICER = 3;//请求我的阵营官职数据(阵营聊天可能用到)
    private static final short REQ_THE_CITY_RARE_OFFICER_LIST = 4;//请求指定城池的稀有官职列表(天下大势用到)
    private static final short REQ_TAKE_DAILY_REWARD = 5;//领取每日俸禄
    private static final short REQ_MAIN_OFFICER_UI = 6;//官职一览打开数据
    private static final short REQ_OFFICER_UPGRADE_UI = 7;//打开官职升级界面
    private static final short REQ_DONATE_YB_UI = 8;//捐献元宝界面
    private static final short REQ_DONATE_YB = 9;//捐献元宝


    private int cityId;
    private int money;

    @Override
    public void execPacket(Player player) {
        CampModule module = module(MConst.Camp);
        switch (subType) {
            case REQ_UPGRADE_OFFICER: {
                module.reqUpgradeOfficer();
            }
            break;
            case REQ_MY_CAMP_OFFICER: {
                module.reqMyCampAndOfficers();
            }
            break;
            case REQ_THE_CITY_RARE_OFFICER_LIST: {
                module.reqRareOfficerListByCityId(cityId);
            }
            break;
            case REQ_TAKE_DAILY_REWARD: {
                module.takeDailyReward();
            }
            break;
            case REQ_MAIN_OFFICER_UI: {
                module.reqMainOfficerUI();
            }
            break;
            case REQ_OFFICER_UPGRADE_UI: {
                module.reqOfficerUpgradeUI();
            }
            break;
            case REQ_DONATE_YB_UI: {
                module.reqOpenDonateYBUI();
            }
            break;
            case REQ_DONATE_YB: {
                module.reqDonateYB(money);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_THE_CITY_RARE_OFFICER_LIST: {
                cityId = buff.readInt();
            }
            break;
            case REQ_DONATE_YB: {
                money = buff.readInt();
            }
            break;
        }

    }

    @Override
    public short getType() {
        return CampPackset.S_OFFICER;
    }
}
