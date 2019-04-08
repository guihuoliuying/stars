package com.stars.multiserver.camp;

import com.stars.services.ServiceHelper;
import com.stars.core.rpc2.RpcClientConnectedCallback;

/**
 * Created by huwenjun on 2017/7/7.
 */
public class Conn2CampCallBack implements RpcClientConnectedCallback {
    @Override
    public void ontCalled(int serverId) {
        ServiceHelper.campLocalMainService().connectAndShareRoleNum();
    }
}
