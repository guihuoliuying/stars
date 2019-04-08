package com.stars.services.guest;

import com.stars.modules.guest.userdata.RoleGuestExchange;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;

/**
 * Created by zhouyaohui on 2017/1/9.
 */
public interface GuestService extends Service, ActorService {

    @AsyncInvocation
    void ask(RoleGuestExchange exchange);

    @AsyncInvocation
    void give(long id, long askId, int askStamp, int itemId);

    @AsyncInvocation
    void onSchedule();

    @AsyncInvocation
    void info(long roleId, long familyId, int index, int askCount, List<Long> memberList);

    @AsyncInvocation
    void removeFromFamily(long askId, Long familyId);

    @AsyncInvocation
    void updateRoleName(long roleId, long familyId, String newName);
}
