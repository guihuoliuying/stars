package com.stars.services.family.task;

import com.stars.core.persist.DbRowDao;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyTask.FamilyTaskManager;
import com.stars.modules.familyTask.event.FamilyTaskEvent;
import com.stars.modules.familyTask.packet.ClientFamilyTask;
import com.stars.modules.familyTask.prodata.FamilyMissionGroup;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.FamilyConst;
import com.stars.services.family.task.userdata.FamilySeekHelp;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyuxing on 2017/3/24.
 */
public class FamilyTaskServiceActor extends ServiceActor implements FamilyTaskService {

    private ConcurrentHashMap<Long, List<FamilySeekHelp>> seekHelpMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, List<FamilySeekHelp>> waitHandleMap = new ConcurrentHashMap<>();

    private DbRowDao dao;

    @Override
    public void init() throws Throwable {
        //加载数据
        synchronized (FamilyTaskServiceActor.class) {
        	int todayZeroTime = (int)(DateUtil.getZeroTime(Calendar.getInstance())/1000);
        	DBUtil.execUserSql("delete from familytaskseekhelp where time < "+todayZeroTime);
            List<FamilySeekHelp> list = DBUtil.queryList(DBUtil.DB_USER, FamilySeekHelp.class, "select * from familytaskseekhelp");
            FamilySeekHelp familySeekHelp = null;
            long familyId = 0;
            for (int i = 0; i < list.size(); i++) {
                familySeekHelp = list.get(i);
                familyId = familySeekHelp.getFamilyId();
                if (familySeekHelp.getWaitHandle() == 0) {
                    List<FamilySeekHelp> familyHelpList = seekHelpMap.get(familyId);
                    if (familyHelpList == null) {
                        familyHelpList = new ArrayList<>();
                        seekHelpMap.put(familySeekHelp.getFamilyId(), familyHelpList);
                    }
                    familyHelpList.add(familySeekHelp);
                } else {
                    List<FamilySeekHelp> waitList = waitHandleMap.get(familyId);
                    if (waitList == null) {
                        waitList = new ArrayList<>();
                        waitHandleMap.put(familySeekHelp.getFamilyId(), waitList);
                    }
                    waitList.add(familySeekHelp);
                }
            }
        }
        dao = new DbRowDao(SConst.FamilyTaskService);
        ServiceSystem.getOrAdd(SConst.FamilyTaskService, this);
        ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_TASK,
                FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "");
    }

    @Override
    public void printState() {

    }

    @Override
    public void openSeekHelpUI(long familyId, long roleId) {
        List<FamilySeekHelp> list = seekHelpMap.get(familyId);
        FamilyTaskEvent event = new FamilyTaskEvent(FamilyTaskEvent.SEND_HELP_LIST);
        event.setList(list);
        ServiceHelper.roleService().notice(roleId, event);
    }

    @Override
    public boolean seekHelp(int taskid, long familyId, long roleId, String roleName) {
        List<FamilySeekHelp> list = seekHelpMap.get(familyId);
        if (list == null) {
            list = new ArrayList<>();
            seekHelpMap.put(familyId, list);
        }
        for (int i = 0; i < list.size(); i++) {
            FamilySeekHelp familySeekHelp = list.get(i);
            if (familySeekHelp.getRoleId() == roleId && familySeekHelp.getTaskId() == taskid) {
                return false;
            }
        }
        FamilySeekHelp familySeekHelp = new FamilySeekHelp(roleId, roleName, familyId, taskid, (byte) 0);
        list.add(familySeekHelp);
        dao.insert(familySeekHelp);
        ClientFamilyTask cft = new ClientFamilyTask(ClientFamilyTask.RESP_SEEK_HELP_SUCCESS);
        cft.setTaskId(taskid);
        PlayerUtil.send(roleId, cft);
        FamilyMissionGroup missionGroup = FamilyTaskManager.MissionGroupMap.get(taskid);

//		String helpMsg = String.format(missionGroup.getHelpdesc(), roleName, 
//				missionGroup.getReqCode()+" "+missionGroup.getReqCount());
        //[obj:8:roleid:taskid:文字内容:1/]
        StringBuffer content = new StringBuffer();
//		ToolManager.getItemVo(missionGroup.getReqCode());
        content.append("[obj:8:").append(roleId).append(":").append(taskid).append(":")
                .append(missionGroup.getHelpdesc()).append(":").append(missionGroup.getReqCode()).append(":")
                .append(roleName).append(":").append(missionGroup.getReqCount()).append(":1/]");
        ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_FAMILY, 0L, familyId, content.toString(), false);
        return true;
    }

    @Override
    public boolean cancelSeekHelp(int taskid, long familyId, long roleId) {
        if (!seekHelpMap.keySet().contains(familyId)) return false;
        List<FamilySeekHelp> list = seekHelpMap.get(familyId);
        if (StringUtil.isEmpty(list)) return false;
        int removeIndex = -1;
        FamilySeekHelp familySeekHelp = null;
        for (int i = 0; i < list.size(); i++) {
            familySeekHelp = list.get(i);
            if (familySeekHelp.getRoleId() == roleId && familySeekHelp.getTaskId() == taskid) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex == -1) return false;
        list.remove(removeIndex);
        dao.delete(familySeekHelp);
        ClientFamilyTask cft = new ClientFamilyTask(ClientFamilyTask.RESP_CANCEL_SEEK_HELP);
        cft.setTaskId(taskid);
        PlayerUtil.send(roleId, cft);
        return true;
    }

    @Override
    public boolean helpCommit(int taskid, long familyId, long beHelpRoleid, String name) {
        if (!seekHelpMap.keySet().contains(familyId)) return false;
        List<FamilySeekHelp> list = seekHelpMap.get(familyId);
        if (StringUtil.isEmpty(list)) return false;
        int removeIndex = -1;
        FamilySeekHelp familySeekHelp = null;
        for (int i = 0; i < list.size(); i++) {
            familySeekHelp = list.get(i);
            if (familySeekHelp.getRoleId() == beHelpRoleid && familySeekHelp.getTaskId() == taskid) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex == -1) return false;
        Player player = PlayerSystem.get(beHelpRoleid);
        if (player == null) {//被帮助玩家不在线   加入等待处理集合
            List<FamilySeekHelp> waitlist = waitHandleMap.get(familyId);
            if (waitlist == null) {
                waitlist = new ArrayList<>();
                waitHandleMap.put(familyId, waitlist);
            }
            FamilySeekHelp waitObject = null;
            for (int i = 0; i < waitlist.size(); i++) {
                waitObject = waitlist.get(i);
                if (waitObject.getRoleId() == beHelpRoleid && waitObject.getTaskId() == taskid) {
                    return false;
                }
            }
            waitObject = new FamilySeekHelp(beHelpRoleid, familySeekHelp.getRoleName(), familyId, taskid, (byte) 1);
            waitlist.add(waitObject);
            dao.update(waitObject);
        } else {
            FamilyTaskEvent event = new FamilyTaskEvent(FamilyTaskEvent.BE_HELP_COMMIT);
            event.setTaskId(taskid);
            event.setName(name);
            ServiceHelper.roleService().notice(beHelpRoleid, event);
            dao.delete(familySeekHelp);
        }
        list.remove(removeIndex);
        return true;
    }

    /**
     * 聊天窗点击获取任务信息
     */
    @Override
    public void chatGetTaskInfo(long roleId, int taskid, long familyId, long beHelpRoleid) {
        if (seekHelpMap.keySet().contains(familyId)) {
            List<FamilySeekHelp> list = seekHelpMap.get(familyId);
            if (StringUtil.isNotEmpty(list)) {
                int removeIndex = -1;
                FamilySeekHelp familySeekHelp = null;
                for (int i = 0; i < list.size(); i++) {
                    familySeekHelp = list.get(i);
                    if (familySeekHelp.getRoleId() == beHelpRoleid && familySeekHelp.getTaskId() == taskid) {
                        removeIndex = i;
                        break;
                    }
                }
                if (removeIndex >= 0) {
                	if(roleId==beHelpRoleid){
                		PacketManager.send(roleId, new ClientText("请等待家族成员帮助"));
                		return;
                	}
                    ClientFamilyTask cft = new ClientFamilyTask(ClientFamilyTask.RESP_CHAT_TASK_INFO);
                    cft.setPlayer(PlayerSystem.get(roleId));
                    cft.setUserRoleId(familySeekHelp.getRoleId());
                    cft.setUserName(familySeekHelp.getRoleName());
                    cft.setTaskId(taskid);
                    PlayerUtil.send(roleId, cft);
                    return;
                }
            }
        }
        PacketManager.send(roleId, new ClientText("该求助已被其他家族成员完成"));
    }

    @Override
    public void save() {
        dao.flush();
    }

    @Override
    public void leaveFamilyHandle(long familyId, long roleId) {
        if (seekHelpMap.keySet().contains(familyId)) {
            List<FamilySeekHelp> seekHelpList = seekHelpMap.get(familyId);
            FamilySeekHelp familySeekHelp = null;
            int size = seekHelpList.size();
            for (int i = size - 1; i >= 0; i--) {
                familySeekHelp = seekHelpList.get(i);
                if (familySeekHelp.getRoleId() == roleId) {
                    seekHelpList.remove(i);
                    dao.delete(familySeekHelp);
                    break;
                }
            }
        }
    }

    @Override
    public void checkWaitMapAndHandle(long familyId, long roleId) {
        List<FamilySeekHelp> list = waitHandleMap.get(familyId);
        if (StringUtil.isEmpty(list)) return;
        int size = list.size();
        FamilySeekHelp familySeekHelp = null;
        for (int i = size - 1; i >= 0; i--) {
            familySeekHelp = list.get(i);
            if (familySeekHelp.getRoleId() == roleId) {
                list.remove(i);
                FamilyTaskEvent event = new FamilyTaskEvent(FamilyTaskEvent.BE_HELP_COMMIT);
                event.setTaskId(familySeekHelp.getTaskId());
                ServiceHelper.roleService().notice(roleId, event);
                dao.delete(familySeekHelp);
            }
        }
    }
    
    /**
     * 检测任务状态并修正
     */
    @Override
    public void checkHelpListAndFix(long familyId, long roleId, int taskId){
    	List<FamilySeekHelp> list = seekHelpMap.get(familyId);
    	if(StringUtil.isEmpty(list)) return;
    	int size = list.size();
    	FamilySeekHelp familySeekHelp = null;
    	boolean needFix = true;
    	for(int i=0;i<size;i++){
    		familySeekHelp = list.get(i);
    		if(familySeekHelp.getRoleId()==roleId&&familySeekHelp.getTaskId()==taskId){
    			needFix = false;
    			break;
    		}
    	}
    	if(needFix){
    		FamilyTaskEvent event = new FamilyTaskEvent(FamilyTaskEvent.FIX_STATE);
            event.setTaskId(taskId);
            ServiceHelper.roleService().notice(roleId, event);
    	}
    }
    
    /**
     * 重置帮助列表
     */
    public void dailyReset(){
    	seekHelpMap.clear();
    	try {
			DBUtil.execUserSql("delete from familytaskseekhelp");
		} catch (SQLException e) {
			LogUtil.error("family task help List reset fail", e);
		}
    }

}
