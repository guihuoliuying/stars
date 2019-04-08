package com.stars.multiserver.rpctest.echo;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;

/**
 * Created by zhaowenshuo on 2016/10/25.
 */
public interface EchoService extends Service, ActorService {

    void println(int serverId, long roleId, String message);

}
