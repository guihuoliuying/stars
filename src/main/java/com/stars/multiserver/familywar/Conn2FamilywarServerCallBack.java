package com.stars.multiserver.familywar;

import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.core.rpc2.RpcClientConnectedCallback;

/**
 * Created by chenkeyu on 2017-04-27 16:46
 */
public class Conn2FamilywarServerCallBack implements RpcClientConnectedCallback {
    @Override
    public void ontCalled(int serverId) {
        MainRpcHelper.familyWarQualifyingService().registerFamilyWarServer(FamilyWarUtil.getFamilyWarServerId(),
                MultiServerHelper.getServerId());
        int serverOpenday = DataManager.getServerDays();
        int maxDay = FamilyActWarManager.familywar_cycletime_max;
        if (serverOpenday > maxDay) {
            MainRpcHelper.familywarRankService().connectServer(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId());
        }
    }
}
