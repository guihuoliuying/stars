package com.stars.multiserver.camp;

import com.stars.modules.camp.event.CampFightEvent;
import com.stars.modules.camp.packet.ClientCampFightPacket;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.pojo.CampFightMatchInfo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.invocation.ServiceActor;

/**
 * Created by huwenjun on 2017/7/21.
 */
public class CampLocalFightServiceActor extends ServiceActor implements CampLocalFightService {
    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.CampLocalFightService, this);
    }

    @Override
    public void printState() {

    }

    @Override
    public void startMatching(CampFightMatchInfo campFightMatchInfo) {
        MainRpcHelper.campRemoteFightService().startMatching(MultiServerHelper.getCampServerId(), campFightMatchInfo);
    }

    @Override
    public void cancelMatching(CampFightMatchInfo campFightMatchInfo) {
        MainRpcHelper.campRemoteFightService().cancelMatching(MultiServerHelper.getCampServerId(), campFightMatchInfo);

    }

    @Override
    public void matchFinish(int fromServerId, int fightServerId, long roleId) {
        ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_MATCHING_SUCCESS);
        ServiceHelper.roleService().send(roleId, clientCampFightPacket);
        ServiceHelper.roleService().notice(roleId, new CampFightEvent(CampFightEvent.TYPE_MATCHING_SUCCESS));
    }


    @Override
    public void updateFightScore(int fromServerId, long roleId, int score) {
        CampFightEvent campFightEvent = new CampFightEvent(CampFightEvent.TYPE_ADD_DAILY_SCORE);
        campFightEvent.setScore(score);
        ServiceHelper.roleService().notice(roleId, campFightEvent);
    }
}
