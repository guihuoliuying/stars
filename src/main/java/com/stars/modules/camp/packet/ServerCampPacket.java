package com.stars.modules.camp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.CampPackset;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class ServerCampPacket extends PlayerPacket {
    private short subType;
    private Integer campType;
    public final static short REQ_MY_CAMP_STATE = 1;//请求我的阵营状态
    public final static short REQ_JOIN_CAMP = 2;//请求加入阵营
    public final static short REQ_RANDOM_JOIN_CAMP = 3;//请求随机加入阵营
    public final static short REQ_CURRENT_CAMP_LOAD = 4;//请求当前阵营负载情况
    public final static short REQ_ALL_CAMP_INFO = 5;//请求所有阵营数据（产品数据）
    public final static short REQ_MY_CAMP_INFO = 6;//请求我的阵营信息，对应阵营信息界面

    @Override
    public void execPacket(Player player) {
        CampModule module = module(MConst.Camp);
        switch (subType) {
            case REQ_MY_CAMP_STATE: {
                module.reqMyCampState();
            }
            break;
            case REQ_JOIN_CAMP: {
                module.joinCamp(campType);
            }
            break;
            case REQ_RANDOM_JOIN_CAMP: {
                module.randomJoinCamp();
            }
            break;
            case REQ_CURRENT_CAMP_LOAD: {
                module.reqCurrentCampLoad();
            }
            break;
            case REQ_ALL_CAMP_INFO: {
                module.reqAllCampInfo();
            }
            break;
            case REQ_MY_CAMP_INFO: {
                module.reqMyCamp();
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_JOIN_CAMP: {
                campType = buff.readInt();
            }
            break;
        }
    }

    @Override
    public short getType() {
        return CampPackset.S_CAMP;
    }
}
