package com.stars.services.opactkickback;

import com.stars.modules.opactkickback.userdata.RoleConsumeInfo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;

public interface OpActKickBack extends Service, ActorService {

	void view(long roleId, RoleConsumeInfo info);

}
