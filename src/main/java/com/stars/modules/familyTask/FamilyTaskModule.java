package com.stars.modules.familyTask;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.daily.event.DailyAwardCheckEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.userdata.RoleFamilyPo;
import com.stars.modules.familyTask.packet.ClientFamilyTask;
import com.stars.modules.familyTask.prodata.FamilyMissionGroup;
import com.stars.modules.familyTask.prodata.FamilyMissionInfo;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.task.TaskModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.task.userdata.FamilySeekHelp;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class FamilyTaskModule extends AbstractModule {

    public FamilyTaskModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("FamilyTask", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {

    }

    @Override
    public void onCreation(String name, String account) throws Throwable {

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
//    	int a = getInt("");
        if (!isCreation) {
            FamilyModule familyModule = module(MConst.Family);
            long familyId = familyModule.getAuth().getFamilyId();
            if (familyId == 0) {
                return;
            }
            ServiceHelper.familyTaskService().checkWaitMapAndHandle(familyId, id());
            //检测是否有错误的任务状态并修正
//        	Map<Integer, Byte> familyTaskMap = familyModule.getRoleFamilyPo().getFamilyTaskMap();
//        	Iterator<Entry<Integer, Byte>> iterator = familyTaskMap.entrySet().iterator();
//        	int taskId = 0;
//        	Entry<Integer, Byte> entry = null;
//        	for(;iterator.hasNext();){
//        		entry = iterator.next();
//        		if(entry.getValue()==FamilyTaskManager.SEEK_HELP){        			
//        			taskId = entry.getKey();
//        			ServiceHelper.familyTaskService().checkHelpListAndFix(familyId, id(), taskId);
//        		}
//        	}
            //红点检测
            signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_COMMIT);
            signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_AWARD);
//    		ClientFamilyTask packet = new ClientFamilyTask(ClientFamilyTask.RESP_CHAT_TASK_OPEN);
//    		RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
//    		byte frequency = getByte("family.task.frequency");
//    		byte openState = 1;
//    		if(roleFamilyPo.getMissionId()==0&&frequency>0){
//    			openState = 0;
//    		}
//    		packet.setOpenState(openState);
//    		send(packet);
        }
    }

    @Override
    public void onSyncData() throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        FamilyModule familyModule = module(MConst.Family);
//    	if(familyModule.getAuth().getFamilyId()==0){
//    		return;
//    	}
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        roleFamilyPo.setMissionId(0);
        roleFamilyPo.setFamilyTaskAward((byte) 0);
        roleFamilyPo.getFamilyTaskMap().clear();
        roleFamilyPo.setAskHelpTimes((byte) 0);
        context().update(roleFamilyPo);
        setByte("family.task.frequency", (byte) 0);//每日任务次数重置
        //道具回收
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        toolModule.deleteByType(ToolManager.TYPE_FAMILY_TASK, EventType.FAMILY_TASK_ZERO_RECOVER.getCode());
        //统一放弃任务 在taskmodule的每日重置哩处理

    }

    /**
     * 打开个人家族任务信息界面
     */
    public void openSelfInfoUI(byte opType) {
        FamilyModule familyModule = module(MConst.Family);
        if (familyModule.getAuth().getFamilyId() == 0) {
            return;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        int missionId = roleFamilyPo.getMissionId();
        byte frequency = getByte("family.task.frequency");
        if (missionId == 0 && frequency == 0) {//初始化
            setByte("family.task.frequency", (byte) 1);
            initFamilyTask(familyModule, roleFamilyPo);
            context().update(roleFamilyPo);
        }
        ClientFamilyTask cft = new ClientFamilyTask(opType);
        cft.setPlayer(self());
        cft.setMissionId(roleFamilyPo.getMissionId());
        cft.setFamilyTaskMap(roleFamilyPo.getFamilyTaskMap());
        cft.setAwardState(roleFamilyPo.getFamilyTaskAward());
        byte leftTimes = (byte) (FamilyTaskManager.familymission_count - roleFamilyPo.getAskHelpTimes());
        cft.setLeftTimes(leftTimes);
        send(cft);
    }

    /**
     * 初始化今天的家族任务
     */
    private void initFamilyTask(FamilyModule familyModule, RoleFamilyPo roleFamilyPo) {
        com.stars.util.LogUtil.info("初始化家族任务：start");
        FamilyAuth auth = familyModule.getAuth();
        int familyLevel = auth.getFamilyLevel();
        RoleModule rm = module(MConst.Role);
        int roleLevel = rm.getLevel();
        //获取今天的任务
        FamilyMissionInfo familyMissionInfo = randomMission(familyLevel, roleLevel);
        roleFamilyPo.setMissionId(familyMissionInfo.getId());
        //获取任务组内容
        Map<Integer, Integer> groupMap = familyMissionInfo.getGroupMap();
        Iterator<Entry<Integer, Integer>> iterator = groupMap.entrySet().iterator();
        Entry<Integer, Integer> entry = null;
        int groupId = 0;
        int num = 0;
        List<FamilyMissionGroup> list = null;
        List<FamilyMissionGroup> copyList = new ArrayList<>();
        int taskId = 0;
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        for (; iterator.hasNext(); ) {
            entry = iterator.next();
            groupId = entry.getKey();
            num = entry.getValue();
            list = FamilyTaskManager.GroupListMap.get(groupId);
            copyList.clear();
            copyList.addAll(list);
            for (int i = 0; i < num; i++) {
                taskId = randomTinyTask(copyList);
                familyTaskMap.put(taskId, FamilyTaskManager.HAVE_NOT_COMMIT);
                com.stars.util.LogUtil.info("家族任务初始化，roleid:" + id() + " , 小任务id:" + taskId);
            }
        }
        com.stars.util.LogUtil.info("初始化家族任务：end");
    }

    /**
     * 根据条件筛选后随机任务
     *
     * @param familyLevel
     * @param roleLevel
     * @return
     */
    private FamilyMissionInfo randomMission(int familyLevel, int roleLevel) {
        List<FamilyMissionInfo> selectList = new ArrayList<>();
        Iterator<FamilyMissionInfo> iterator = FamilyTaskManager.MissionInfoMap.values().iterator();
        FamilyMissionInfo familyMissionInfo = null;
        int totalOdds = 0;
        List<Integer> randomList = new ArrayList<>();
        for (; iterator.hasNext(); ) {
            familyMissionInfo = iterator.next();
            if (!familyMissionInfo.matchFamilyLevel(familyLevel)) {
                continue;
            }
            if (!familyMissionInfo.matchRoleLevel(roleLevel)) {
                continue;
            }
            selectList.add(familyMissionInfo);
            totalOdds += familyMissionInfo.getOdds();
            randomList.add(totalOdds);
        }
        int random = RandomUtil.rand(1, totalOdds);
        int target = 0;
        int selectIndex = 0;
        for (int i = 0; i < randomList.size(); i++) {
            target = randomList.get(i);
            if (random <= target) {
                selectIndex = i;
                break;
            }
        }
        return selectList.get(selectIndex);
    }

    /**
     * 随机组内小任务
     */
    private int randomTinyTask(List<FamilyMissionGroup> list) {
        List<Integer> randomList = new ArrayList<>();
        int totalOdds = 0;
        FamilyMissionGroup group = null;
        for (int i = 0; i < list.size(); i++) {
            group = list.get(i);
            totalOdds += group.getOdds();
            randomList.add(totalOdds);
        }
        int random = RandomUtil.rand(1, totalOdds);
        int target = 0;
        int selectIndex = 0;
        for (int i = 0; i < randomList.size(); i++) {
            target = randomList.get(i);
            if (random <= target) {
                selectIndex = i;
                break;
            }
        }
        FamilyMissionGroup familyMissionGroup = list.get(selectIndex);
        list.remove(selectIndex);
        return familyMissionGroup.getId();
    }

    /**
     * 打开家族任务求助信息界面
     */
    public void openSeekHelpUI() {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
        ServiceHelper.familyTaskService().openSeekHelpUI(familyId, id());
    }

    /**
     * 下发求助列表数据
     *
     * @param list
     */
    public void sendSeekHelpList(List<FamilySeekHelp> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        ClientFamilyTask cft = new ClientFamilyTask(ClientFamilyTask.RESP_VIEW_HELP_UI);
        cft.setUserRoleId(id());
        cft.setPlayer(self());
        cft.setHelpList(list);
        send(cft);
    }

    /**
     * 自己提交任务（提交材料，完成任务）
     *
     * @param taskid 小任务id (familymissiongroup 表的id)
     */
    public void commitTask(int taskid) {
        FamilyModule familyModule = module(MConst.Family);
        if (familyModule.getAuth().getFamilyId() == 0) {
            return;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        Byte state = familyTaskMap.get(taskid);
        if (state == null) return;
        if (state == FamilyTaskManager.ALREADY_COMMIT) {//已提交
            com.stars.util.LogUtil.info("自己提交任务已提交，roleid:" + id() + " , 小任务id:" + taskid);
            return;
        }
        com.stars.util.LogUtil.info("自己提交任务开始，roleid:" + id() + " , 小任务id:" + taskid);
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        FamilyMissionGroup group = FamilyTaskManager.MissionGroupMap.get(taskid);
        //扣除物品
        int itemId = group.getReqCode();
        int count = group.getReqCount();
        if (state == FamilyTaskManager.SEEK_HELP) {//求助中   跑service
            long nowCount = toolModule.getCountByItemId(itemId);
            if (count < 0 || nowCount < count) return;//道具不足

            long familyId = familyModule.getAuth().getFamilyId();
            boolean handleResult = ServiceHelper.familyTaskService().helpCommit(taskid, familyId, id(), "");
            if (!handleResult) {
                send(new ClientText("该求助已被其他家族成员完成"));
                openSelfInfoUI(ClientFamilyTask.RESP_VIEW_SELF_UI);
                return;
            }
            toolModule.deleteAndSend(itemId, count, EventType.FAMILY_TASK_SELF_COMMIT.getCode());
        } else {
            boolean deleteSuccess = toolModule.deleteAndSend(itemId, count, EventType.FAMILY_TASK_SELF_COMMIT.getCode());
            if (!deleteSuccess) return;//道具不足
        }
        familyTaskMap.put(taskid, FamilyTaskManager.ALREADY_COMMIT);

        int award = group.getAward();
        DropModule drop = module(MConst.Drop);
        Map<Integer, Integer> dropMap = drop.executeDrop(award, 1, true);
        Map<Integer, Integer> getReward = toolModule.addAndSend(dropMap, EventType.FAMILY_TASK_SELF_COMMIT.getCode());
        //发获奖提示到客户端
        ClientAward clientAward = new ClientAward(getReward);
        send(clientAward);

        com.stars.util.LogUtil.info("自己提交任务成功，roleid:" + id() + " , 小任务id:" + taskid);
        //发送成功通知
        ClientFamilyTask cft = new ClientFamilyTask(ClientFamilyTask.RESP_COMMIT_SUCCESS);
        cft.setTaskId(taskid);
        send(cft);
        context().update(roleFamilyPo);
        if (checkAward()) {
            signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_AWARD);
        }
        signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_COMMIT);
        if (state != FamilyTaskManager.SEEK_HELP) {
            addDailyTaskTimes(false);
        }
    }

    /**
     * 求助
     *
     * @param taskid 小任务id (familymissiongroup 表的id)
     */
    public void seekHelp(int taskid) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        if (roleFamilyPo.getAskHelpTimes() >= FamilyTaskManager.familymission_count) {
            return;
        }
        FamilyMissionGroup group = FamilyTaskManager.MissionGroupMap.get(taskid);
        if (group.getHelp() == 0) return;
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        if (!familyTaskMap.containsKey(taskid)) return;
        if (familyTaskMap.get(taskid) == FamilyTaskManager.ALREADY_COMMIT) return;
        RoleModule rm = module(MConst.Role);
        if (ServiceHelper.familyTaskService().seekHelp(taskid, familyId, id(), rm.getRoleRow().getName())) {
            byte askHelpTimes = (byte) (roleFamilyPo.getAskHelpTimes() + 1);
            roleFamilyPo.setAskHelpTimes(askHelpTimes);
            familyTaskMap.put(taskid, FamilyTaskManager.SEEK_HELP);
            context().update(roleFamilyPo);
            com.stars.util.LogUtil.info("家族任务求助，roleid:" + id() + " , 小任务id:" + taskid);
        }
    }

    /**
     * 取消求助
     *
     * @param taskid 小任务id (familymissiongroup 表的id)
     */
    public void cancelSeekHelp(int taskid) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
        FamilyMissionGroup group = FamilyTaskManager.MissionGroupMap.get(taskid);
        if (group.getHelp() == 0) return;
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        if (!familyTaskMap.containsKey(taskid)) return;
        if (familyTaskMap.get(taskid) == FamilyTaskManager.ALREADY_COMMIT) return;
        if (ServiceHelper.familyTaskService().cancelSeekHelp(taskid, familyId, id())) {
            byte askHelpTimes = (byte) (roleFamilyPo.getAskHelpTimes() - 1);//减少已求助次数
            roleFamilyPo.setAskHelpTimes(askHelpTimes);
            roleFamilyPo.getFamilyTaskMap().put(taskid, FamilyTaskManager.HAVE_NOT_COMMIT);
            context().update(roleFamilyPo);
            com.stars.util.LogUtil.info("家族任务取消求助，roleid:" + id() + " , 小任务id:" + taskid);
        } else {
            send(new ClientText("该求助已被其他家族成员完成"));
            openSelfInfoUI(ClientFamilyTask.RESP_VIEW_SELF_UI);
        }
    }

    /**
     * 帮助提交
     *
     * @param beHelpRoleid 求助者玩家id
     * @param taskId       小任务id (familymissiongroup 表的id)
     * @param helpType     帮助方式     1 元宝完成         2 材料提交
     */
    public void helpCommit(long beHelpRoleid, int taskId, byte helpType) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
        if (id() == beHelpRoleid) {
            send(new ClientText("不能操作自己的求助信息"));
            return;
        }
        FamilyMissionGroup group = FamilyTaskManager.MissionGroupMap.get(taskId);
        boolean handleResult = false;
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        int itemId = group.getReqCode();
        int count = group.getReqCount();
        int reqGold = group.getReqGold();
        if (helpType == 1) {
            //元宝不足
            if (reqGold < 0 || !toolModule.contains(ToolManager.GOLD, reqGold)) return;
        } else {
            //道具不足
            long nowCount = toolModule.getCountByItemId(itemId);
            if (count < 0 || nowCount < count) return;
        }
        RoleModule rm = module(MConst.Role);
        String myName = rm.getRoleRow().getName();
        handleResult = ServiceHelper.familyTaskService().helpCommit(taskId, familyId, beHelpRoleid, myName);
        if (!handleResult) {
            //返回消息
            send(new ClientText("该求助已被其他家族成员完成"));
            openSeekHelpUI();
            return;
        }

        RoleSummaryComponent comp = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(beHelpRoleid, MConst.Role);
//    	ServiceHelper.summaryService().getSummary(0L).getComponent(componentName)
        String itemName = ToolManager.getItemVo(itemId).getName();
        String roleName = comp.getRoleName();
        String gametext = DataManager.getGametext("familymission_desc_helpname");
        itemName = DataManager.getGametext(itemName);

        String commitMsg = String.format(gametext, roleName, itemName + " ", myName);
        ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_FAMILY, 0L, familyId, commitMsg, false);

        DropModule drop = module(MConst.Drop);
        Map<Integer, Integer> goldReward = null;
        if (helpType == 1) {
            toolModule.deleteAndSend(ToolManager.GOLD, reqGold, EventType.FAMILY_TASK_SELF_COMMIT.getCode());
            int goldAward = group.getGoldAward();
            Map<Integer, Integer> dropMap = drop.executeDrop(goldAward, 1, true);
            goldReward = toolModule.addAndSend(dropMap, EventType.FAMILY_TASK_GET_AWARD.getCode());
        } else {
            toolModule.deleteAndSend(itemId, count, EventType.FAMILY_TASK_SELF_COMMIT.getCode());
        }
        int award = group.getHelpaward();
        Map<Integer, Integer> dropMap = drop.executeDrop(award, 1, true);
        Map<Integer, Integer> getReward = toolModule.addAndSend(dropMap, EventType.FAMILY_TASK_GET_AWARD.getCode());
        if (StringUtil.isNotEmpty(goldReward)) {
            int totalNum = 0;
            for (int toolId : goldReward.keySet()) {
                totalNum = goldReward.get(toolId);
                if (getReward.keySet().contains(toolId)) {
                    totalNum = getReward.get(toolId) + totalNum;
                }
                getReward.put(toolId, totalNum);
            }
        }
        com.stars.util.LogUtil.info("帮助提交任务成功，roleid:" + id() + " , 小任务id:" + taskId + " , beHelpRoleid:" + beHelpRoleid);

        //发获奖提示到客户端
        ClientAward clientAward = new ClientAward(getReward);
        send(clientAward);
        //返回结果
//      	ClientFamilyTask cft = new ClientFamilyTask(ClientFamilyTask.RESP_HELP_COMMIT_SUCCESS);
//      	send(cft);
        //刷新求助列表
        openSeekHelpUI();
    }

    /**
     * 被帮助提交  被帮助玩家    获得奖励
     *
     * @param taskId
     */
    public void beHelpCommitAward(int taskId, String name) {
//    	ToolModule toolModule = (ToolModule) module(MConst.Tool);
//    	FamilyMissionGroup group = FamilyTaskManager.MissionGroupMap.get(taskId);
//    	int award = group.getAward();
//    	DropModule drop = module(MConst.Drop);
//    	Map<Integer, Integer> dropMap = drop.executeDrop(award, 1, true);
//    	Map<Integer, Integer> getReward = toolModule.addAndSend(dropMap,EventType.FAMILY_TASK_SELF_COMMIT.getCode());
//    	//发获奖提示到客户端
//    	ClientAward clientAward = new ClientAward(getReward);
//    	send(clientAward);

        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId > 0) {
            RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
            Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
            familyTaskMap.put(taskId, FamilyTaskManager.ALREADY_COMMIT);
            context().update(roleFamilyPo);
            addDailyTaskTimes(true);
            //提示
            if (!StringUtil.isEmpty(name)) {
                FamilyMissionGroup group = FamilyTaskManager.MissionGroupMap.get(taskId);
                int itemId = group.getReqCode();
                String itemName = ToolManager.getItemVo(itemId).getName();
                itemName = DataManager.getGametext(itemName);
                String info = String.format(I18n.get("family.task.behelp.tips"), itemName, name);
                send(new ClientText(info));
            }
        }
        if (checkAward()) {
            signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_AWARD);
        }
        signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_COMMIT);
        openSelfInfoUI(ClientFamilyTask.RESP_REFRESH_SELE_UI);
        com.stars.util.LogUtil.info("被帮助提交任务成功, 小任务id:" + taskId + " , beHelpRoleid:" + id());
    }

    /**
     * 移除任务
     *
     * @param roleid
     * @param taskId
     */
    private void removeteTask(long roleid, int taskId) {

    }

    /**
     * 获取价值任务奖励（完成当日所有任务）
     */
    public void getAward() {
        FamilyModule familyModule = module(MConst.Family);
        if (familyModule.getAuth().getFamilyId() == 0) {
            return;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        byte familyTaskAward = roleFamilyPo.getFamilyTaskAward();
        if (familyTaskAward == 1) {//已领取
//    		warn("family_tips_noapplylist");
            return;
        }
        if (!checkAward()) {
            return;
        }
        int missionId = roleFamilyPo.getMissionId();
        if (missionId == 0) {
            return;
        }
        FamilyMissionInfo familyMissionInfo = FamilyTaskManager.MissionInfoMap.get(missionId);
        DropModule drop = module(MConst.Drop);
        int award = familyMissionInfo.getAward();
        Map<Integer, Integer> dropMap = drop.executeDrop(award, 1, true);
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        Map<Integer, Integer> getReward = toolModule.addAndSend(dropMap, EventType.FAMILY_TASK_GET_AWARD.getCode());
        //发获奖提示到客户端
        ClientAward clientAward = new ClientAward(getReward);
        send(clientAward);
        roleFamilyPo.setFamilyTaskAward((byte) 1);
        context().update(roleFamilyPo);
        LogUtil.info("Get Award Success, roleid:" + id() + " , award:" + award);
        ClientFamilyTask cft = new ClientFamilyTask(ClientFamilyTask.RESP_GET_AWARD_SUCCESS);
        send(cft);
        signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_AWARD);
    }

    /**
     * 聊天窗点击请求求助任务信息
     *
     * @param beHelpRoleid
     * @param taskid
     */
    public void chatGetSeekHelpInfo(long beHelpRoleid, int taskid) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
        ServiceHelper.familyTaskService().chatGetTaskInfo(id(), taskid, familyId, beHelpRoleid);
    }

    /**
     * 创建通用任务
     *
     * @param nomalTaskId 通用任务id
     */
    public void createFamilyTask(int nomalTaskId) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
//    	RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
//    	if()
        TaskModule taskModule = (TaskModule) module(MConst.Task);
        taskModule.checkTask(nomalTaskId, false);
//    	RoleAcceptTask acceptTask = taskModule.checkTask(nomalTaskId, false);
//    	if (acceptTask != null) {
//			send(new ClientTaskProcess(acceptTask.getTaskId(), 
//					acceptTask.getProcess(),acceptTask.getState()));
//		}
    }

    public void leaveFamilyHandle(long familyId) {
        FamilyModule familyModule = module(MConst.Family);
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
//    	Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
//    	Iterator<Entry<Integer, Byte>> iterator = familyTaskMap.entrySet().iterator();
//    	Entry<Integer, Byte> entry = null;
//    	List<Integer> list = new ArrayList<>();
//    	for(;iterator.hasNext();){
//    		entry = iterator.next();
//    		if(entry.getValue()==FamilyTaskManager.SEEK_HELP){
//    			list.add(entry.getKey());
//    		}
//    	}
        ServiceHelper.familyTaskService().leaveFamilyHandle(familyId, id());
        roleFamilyPo.setMissionId(0);
        roleFamilyPo.setFamilyTaskAward((byte) 0);
        roleFamilyPo.getFamilyTaskMap().clear();
        context().update(roleFamilyPo);
//    	ClientFamilyTask packet = new ClientFamilyTask(ClientFamilyTask.RESP_CHAT_TASK_OPEN);
//		byte frequency = getByte("family.task.frequency");
//		byte openState = 1;
//		if(roleFamilyPo.getMissionId()==0&&frequency>0){
//			openState = 0;
//		}
//		packet.setOpenState(openState);
//		send(packet);
    }

    /**
     * 增加日常任务进度
     */
    public void addDailyTaskTimes(boolean beHelp) {
        DailyModule dm = (DailyModule) module(MConst.Daily);
        int commitNum = getCommitNum();
        int dailyCount = dm.getDailyCount(DailyManager.DAILYID_FAMILYTASK) + 1;
//    	if(commitNum<dailyCount){
//    		return;
//    	}
        eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_FAMILYTASK, 1));
        eventDispatcher().fire(new DailyAwardCheckEvent(!beHelp));

    }

    public void setCheckCommit() {
        signCalRedPoint(MConst.FamilyTask, RedPointConst.FAMILY_TASK_COMMIT);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FAMILY_TASK_COMMIT))) {
            checkCommitRedPoint(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FAMILY_TASK_AWARD))) {
            checkAwardRedPoint(redPointMap);
        }
    }

    /**
     * 存在可提交任务 红点
     *
     * @param redPointMap
     */
    private void checkCommitRedPoint(Map<Integer, String> redPointMap) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        if (StringUtil.isEmpty(familyTaskMap)) return;
//    	int canCommitCount = 0;
        boolean red = false;
        Iterator<Entry<Integer, Byte>> iterator = familyTaskMap.entrySet().iterator();
        Entry<Integer, Byte> entry = null;
        int taskid = 0;
        FamilyMissionGroup group = null;
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        for (; iterator.hasNext(); ) {
            entry = iterator.next();
            if (entry.getValue() == FamilyTaskManager.ALREADY_COMMIT) continue;
            taskid = entry.getKey();
            group = FamilyTaskManager.MissionGroupMap.get(taskid);
            //扣除物品
            int itemId = group.getReqCode();
            int count = group.getReqCount();
            long nowCount = toolModule.getCountByItemId(itemId);
            if (count < 0 || nowCount < count) continue;//道具不足
//        	canCommitCount += 1;
            red = true;
            break;
        }
        if (red) {
            redPointMap.put(RedPointConst.FAMILY_TASK_COMMIT, "");
        } else {
            redPointMap.put(RedPointConst.FAMILY_TASK_COMMIT, null);
        }
    }

    private void checkAwardRedPoint(Map<Integer, String> redPointMap) {
        if (checkAward()) {
            redPointMap.put(RedPointConst.FAMILY_TASK_AWARD, "");
        } else {
            redPointMap.put(RedPointConst.FAMILY_TASK_AWARD, null);
        }
    }

    /**
     * 家族任务完成奖励可领取  红点
     *
     * @return
     */
    private boolean checkAward() {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return false;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        if (roleFamilyPo.getFamilyTaskAward() == 1) return false;
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        if (StringUtil.isEmpty(familyTaskMap)) return false;
        Iterator<Entry<Integer, Byte>> iterator = familyTaskMap.entrySet().iterator();
        Entry<Integer, Byte> entry = null;
        for (; iterator.hasNext(); ) {
            entry = iterator.next();
            if (entry.getValue() != FamilyTaskManager.ALREADY_COMMIT) {
                return false;
            }
        }
        return true;
    }

    private int getCommitNum() {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return 0;
        }
        int commitNum = 0;
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        if (roleFamilyPo.getFamilyTaskAward() == 1) return 0;
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        if (StringUtil.isEmpty(familyTaskMap)) return 0;
        Iterator<Entry<Integer, Byte>> iterator = familyTaskMap.entrySet().iterator();
        Entry<Integer, Byte> entry = null;
        for (; iterator.hasNext(); ) {
            entry = iterator.next();
            if (entry.getValue() == FamilyTaskManager.ALREADY_COMMIT) {
                commitNum += 1;
            }
        }
        return commitNum;
    }

    /**
     * 修正任务状态
     *
     * @param taskId
     */
    public void fixTaskState(int taskId) {
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        if (familyId == 0) {
            return;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        familyTaskMap.put(taskId, FamilyTaskManager.ALREADY_COMMIT);
    }

    /**
     * 剩余未完成任务
     *
     * @return
     */
    public int getRemainNotCommitTaskCount() {
        FamilyModule familyModule = module(MConst.Family);
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (!foreShowModule.isOpen(ForeShowConst.FAMILY_TASK)) {
            return 0;
        }
        RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
        int missionId = roleFamilyPo.getMissionId();
        byte frequency = getByte("family.task.frequency");
        FamilyAuth auth = familyModule.getAuth();
        if (auth.getFamilyId() == 0) {// 没有家族
            return 0;
        }
        if (missionId == 0 && frequency == 0) {//初始化
            setByte("family.task.frequency", (byte) 1);
            initFamilyTask(familyModule, roleFamilyPo);
            context().update(roleFamilyPo);
        }
        Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
        int count = 0;
        for (Map.Entry<Integer, Byte> entry : familyTaskMap.entrySet()) {
            if (entry.getValue() != FamilyTaskManager.ALREADY_COMMIT) {
                count++;
            }
        }
        return count;
    }
}
