package com.stars.modules.demologin.message;

import com.stars.network.server.session.GameSession;

/**
 * Created by liuyuheng on 2016/6/28.
 */
public class OfflineMsg extends LoginSyncMsg {

    private long roleId;

    private long roleLoginTime;
    public OfflineMsg(long roleId, GameSession gameSession) {
        this.roleId = roleId;
        setRoleId(roleId);
        setSession(gameSession);
    }
	public long getRoleLoginTime() {
		return roleLoginTime;
	}
	public void setRoleLoginTime(long roleLoginTime) {
		this.roleLoginTime = roleLoginTime;
	}
}
