package com.stars.services.friendInvite;

import com.stars.db.DbRow;
import com.stars.modules.friendInvite.userdata.RoleBeInvitePo;
import com.stars.modules.friendInvite.userdata.RoleInvitePo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenxie on 2017/6/12.
 */
public interface InviteService extends Service, ActorService {

    @AsyncInvocation
    void save();
    @AsyncInvocation
    void insert(DbRow dbRow);
    @AsyncInvocation
    void update(DbRow dbRow);
    @AsyncInvocation
    void bindInviteCode(RoleInvitePo roleInvitePoFrom, RoleBeInvitePo roleBeInvitePo);

}
