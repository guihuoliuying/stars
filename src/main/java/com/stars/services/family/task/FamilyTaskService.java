package com.stars.services.family.task;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

/**
 * Created by wuyuxing on 2017/3/24.
 */
public interface FamilyTaskService extends Service, ActorService {
	
	@DispatchAll
    @AsyncInvocation
    void save();
	
	@AsyncInvocation
	public void openSeekHelpUI(long familyId, long roleId);
	
	public boolean seekHelp(int taskid, long familyId, long roleid, String roleName);
	
	public boolean cancelSeekHelp(int taskid, long familyId, long roleId);
	
	public boolean helpCommit(int taskid, long familyId, long beHelpRoleid, String name);
	
	public void leaveFamilyHandle(long familyId, long roleId);
	
	@AsyncInvocation
	public void chatGetTaskInfo(long roleId, int taskid, long familyId, long beHelpRoleid);
	
	@AsyncInvocation
	public void checkWaitMapAndHandle(long familyId, long roleId); 
	
	@AsyncInvocation
	public void checkHelpListAndFix(long familyId, long roleId, int taskId);
	
	public void dailyReset();
	
}
