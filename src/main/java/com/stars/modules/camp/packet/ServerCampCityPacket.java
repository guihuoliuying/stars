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
public class ServerCampCityPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_OPENED_CAMP_CITIES = 2;//请求天下大势界面
    public static final short REQ_JOIN_CAMP_CITY = 3;//请求入驻城池
    public static final short REQ_THE_CITY_RANK = 4;//请求指定城池的排行榜
    private int cityId;

    @Override
    public void execPacket(Player player) {
        CampModule module = module(MConst.Camp);
        switch (subType) {
            case REQ_OPENED_CAMP_CITIES: {
                module.reqOpenedCampCities();
            }
            break;
            case REQ_JOIN_CAMP_CITY: {
                module.reqJoinCity(cityId);
            }
            break;
            case REQ_THE_CITY_RANK: {
                module.sendTheCityRank(cityId);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_JOIN_CAMP_CITY: {
                cityId = buff.readInt();
            }
            break;
            case REQ_THE_CITY_RANK: {
                cityId = buff.readInt();
            }
            break;
        }
    }

    @Override
    public short getType() {
        return CampPackset.S_CITY;
    }
}
