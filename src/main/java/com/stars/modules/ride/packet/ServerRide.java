package com.stars.modules.ride.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.ride.RideModule;
import com.stars.modules.ride.RidePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Date;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class ServerRide extends PlayerPacket {

    public static final byte REQ_VIEW = 0x03; // 请求打开界面
//    public static final byte SELECT_RIDE = 0x04;//选中某个坐骑
    public static final byte REQ_UPGRADE_ONE = 0x10; // 升级一次
    public static final byte REQ_UPGRADE_TEN = 0x11; // 升级十次
    public static final byte REQ_GET_ON = 0x12; // 骑乘姿势
    public static final byte REQ_GET_DOWN = 0x13; // 不骑乘姿势
    public static final byte REQ_GET_RIDE = 0x14;//激活坐骑

    public static final byte REQ_UPGRADE_AWAKE_LEVEL_ONE = 0x20; // 觉醒升级一次

    private byte subtype; //
    private int rideId; //
    private int currStage; // 当前阶级
    private int currLevel; // 当前等级

    @Override
    public void execPacket(Player player) {
        RideModule rideModule = (RideModule) module(MConst.Ride);
        switch (subtype) {
            case REQ_VIEW:
                rideModule.view();
                break;
            case REQ_UPGRADE_ONE:
//                rideModule.upgradeOneTimes(currStage, currLevel);已废弃
                break;
            case REQ_UPGRADE_TEN:
                rideModule.oneKeyUpgrade();
                break;
            case REQ_GET_ON:
                rideModule.getOn(rideId);
                break;
            case REQ_GET_DOWN:
                rideModule.getDown();
                break;
            case REQ_GET_RIDE:
            	rideModule.activeRide(rideId);
            	break;
            case REQ_UPGRADE_AWAKE_LEVEL_ONE:
                rideModule.upgradeAwakeLevelOneTimes(rideId);
                break;
            /*case SELECT_RIDE:
                rideModule.setClick(rideId);
                break;*/
        }
    }

    @Override
    public short getType() {
        return RidePacketSet.S_RIDE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_UPGRADE_ONE:
            case REQ_UPGRADE_TEN:
//                currStage = buff.readInt();
//                currLevel = buff.readInt();
                break;
//            case SELECT_RIDE:
            case REQ_GET_ON:
            case REQ_GET_DOWN:
            case REQ_GET_RIDE:
                rideId = buff.readInt();
                break;
            case REQ_UPGRADE_AWAKE_LEVEL_ONE:
                rideId = buff.readInt();
                break;
        }
    }

    public static void main(String[] args) {
        System.out.println(new Date(1499184000L * 1000));
    }
}
