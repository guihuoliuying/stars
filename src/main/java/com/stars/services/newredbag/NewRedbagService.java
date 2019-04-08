package com.stars.services.newredbag;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by zhouyaohui on 2017/2/13.
 */
public interface NewRedbagService extends Service, ActorService {

    @AsyncInvocation
    void add(long familyId, long roleId, int redbagId, int count, String roleName, int jobId);

    @AsyncInvocation
    void send(SendInfo info);

    @AsyncInvocation
    void get(long roleId, String name, int jobId, long familyId, String uniqueKey);

    @AsyncInvocation
    void schedule();

    @AsyncInvocation
    void online(long familyId, long roleId, boolean needSend);

    @AsyncInvocation
    void offlineOrExitFamily(long family, long roleId);

    @AsyncInvocation
    void viewMain(long familyId, long id, int remainCount);

    @AsyncInvocation
    void record(long roleId, long familyId, int index);

    @AsyncInvocation
    void recordDetail(long id, long familyId, String redbagKey);

    @AsyncInvocation
    void updateFamilyAuth(long roleId, long familyId, long prevFamilyId);
}
