package com.stars.modules.newfirstrecharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.newfirstrecharge.NewFirstRechargeModule;
import com.stars.modules.newfirstrecharge.NewFirstRechargePackets;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class ServerNewFirstRechargePacket extends PlayerPacket {
    private short subType;
    private int activityType;
    private int group;//第几组奖励

    public static final short REQ_MAIN_DATA_UI = 1;//请求加载主界面数据
    public static final short REQ_TAKE_REWARD = 2;//请求领奖
    public static final short REQ_CHOOSE_REWARD = 3;//请求选择奖励

    public ServerNewFirstRechargePacket() {
    }

    @Override
    public void execPacket(Player player) {
        NewFirstRechargeModule newFirstRechargeModule = null;
        switch (activityType) {
            case OperateActivityConstant.ActType_NewFirstRecharge:
                newFirstRechargeModule = module(MConst.NewFirstRechargeModule);
                break;
            case OperateActivityConstant.ActType_NewFirstRecharge1:
                newFirstRechargeModule = module(MConst.NewFirstRechargeModule1);
                break;
        }
        switch (subType) {
            case REQ_MAIN_DATA_UI: {
                newFirstRechargeModule.reqMainUIData();
            }
            break;
            case REQ_TAKE_REWARD: {
                newFirstRechargeModule.reqTakeReward(group);
            }
            break;
            case REQ_CHOOSE_REWARD: {
                newFirstRechargeModule.reqChooseReward(group);
            }
            break;
        }
    }

    @Override
    public short getType() {
        return NewFirstRechargePackets.S_FIRST_RECHARGE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        activityType = buff.readInt();
        subType = buff.readShort();
        switch (subType) {
            case REQ_TAKE_REWARD: {
                group = buff.readInt();
            }
            break;
            case REQ_CHOOSE_REWARD: {
                group = buff.readInt();
            }
        }
    }

}
