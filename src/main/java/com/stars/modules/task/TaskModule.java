package com.stars.modules.task;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.bravepractise.BravePractiseModule;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.soul.SoulModule;
import com.stars.modules.soul.prodata.SoulLevel;
import com.stars.modules.soul.usrdata.RoleSoul;
import com.stars.modules.task.event.SubmitTaskEvent;
import com.stars.modules.task.packet.ClientRemoveTask;
import com.stars.modules.task.packet.ClientTaskList;
import com.stars.modules.task.packet.ClientTaskProcess;
import com.stars.modules.task.prodata.TaskVo;
import com.stars.modules.task.userdata.*;
import com.stars.modules.tool.ToolModule;

import java.util.*;

public class TaskModule extends AbstractModule {

    /**
     * 已接受任务集合
     */
    private RoleAcceptTaskTable acceptTaskTable;

    /**
     * 可接受任务集合
     */
    private RoleCanAcceptTaskTable canAcceptTaskTable;

    /**
     * 已完成的任务数据对象
     */
    private RoleDoneTaskRaw doneTask;

    /**
     * 可接任务列表
     */
    // private HashSet<Integer>canAcceptTaskList;

    /**
     * 发包通知客户端时用
     */
    private HashSet<Integer> newTask2Client;

    public TaskModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("任务", id, self, eventDispatcher, moduleMap);
        newTask2Client = new HashSet<Integer>();
    }

    @Override
    public void onInit(boolean isCreation) {
        if (acceptTaskTable == null) {
            acceptTaskTable = new RoleAcceptTaskTable(id());
        }
        if (canAcceptTaskTable == null) {
            canAcceptTaskTable = new RoleCanAcceptTaskTable();
        }
        if (doneTask == null) {
            doneTask = new RoleDoneTaskRaw(id());
            context().insert(doneTask);
        }
        checkAllTaskBySort(TaskManager.Task_Sort_ZhuXian, true);
        checkAllTaskBySort(TaskManager.Task_Sort_Daily, false);
        checkAllTaskBySort(TaskManager.Task_Sort_AutoDaily, true);
        RoleModule roleModule = module(MConst.Role);
        updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_UpLevel, "lv"),
                roleModule.getLevel(), true);
        flushNewTask2Client();
    }

    @Override
    public void onDataReq() throws Exception {
        acceptTaskTable = new RoleAcceptTaskTable(id());
        acceptTaskTable.setAcceptTaskMap(DBUtil.queryMap(DBUtil.DB_USER, "taskid", RoleAcceptTask.class,
                "select * from roletask where roleid='" + id() + "'"));
        acceptTaskTable.init();

        canAcceptTaskTable = new RoleCanAcceptTaskTable();
        canAcceptTaskTable.setCanAcceptTaskMap(DBUtil.queryMap(DBUtil.DB_USER, "taskid", RoleCanAcceptTask.class,
                "select * from rolecanaccepttask where roleid='" + id() + "'"));

        doneTask = DBUtil.queryBean(DBUtil.DB_USER, RoleDoneTaskRaw.class,
                "select * from roledonetask where roleid='" + id() + "'");
    }

    // TODO 这个之后可能要改为如果在关卡内，出来时再重置;
    @Override
    public void onDailyReset(Calendar now, boolean isLogin) {
        clearTaskBySort(TaskManager.Task_Sort_HuoDong);
        clearTaskBySort(TaskManager.Task_Sort_Family);
        clearTaskBySort(TaskManager.Task_Sort_AutoDaily);

        clearTask(TaskManager.Task_Reset_Type_DailyReset);
        clearTask(TaskManager.Task_Reset_Type_Active);

        //重置后再看看是否能接取
        checkAllTaskBySort(TaskManager.Task_Sort_Daily, false);
        checkAllTaskBySort(TaskManager.Task_Sort_AutoDaily, false);
    }

    // TODO 这个之后可能要改为如果在关卡内，出来时再重置;
    @Override
    public void onWeeklyReset(boolean isLogin) {

    }

    @Override
    public void onSyncData() {
        Map<Integer, RoleAcceptTask> mp = this.getAcceptTaskTable().getAcceptTaskMap();
        Collection<RoleAcceptTask> cl = mp.values();
        ClientTaskList ctl = new ClientTaskList();
        for (RoleAcceptTask ratb : cl) {
            ctl.addAccpt(ratb);
        }
        Map<Integer, RoleCanAcceptTask> cmp = this.getCanAcceptTaskTable().getCanAcceptTaskMap();
        for (RoleCanAcceptTask canAcceptTask : cmp.values()) {
            ctl.addCanAccept(canAcceptTask.getTaskId());
        }

        ctl.setBravePractiseCount(getBravePractiseCount());
        lazySend(ctl);
        newTask2Client.clear();
    }

    @Override
    public void onMonthlyReset() {

    }

    @Override
    public void onCreation(String name, String account) {
        acceptTaskTable = new RoleAcceptTaskTable(id());
        canAcceptTaskTable = new RoleCanAcceptTaskTable();
        doneTask = new RoleDoneTaskRaw(id());
        context().insert(doneTask);
    }

    /**
     * @param tbv 产品数据任务对象
     * @return 是否满足接取该任务的条件
     */
    private boolean checkTaskCondition(TaskVo tbv, int level) {
        if (tbv == null) {
            return false;
        }

        if (tbv.getActive() == TaskManager.Task_Inactive) {
            return false;
        }

        if (tbv.getReqMinLevel() > 0 && level < tbv.getReqMinLevel()) {
            return false;
        }
        if (tbv.getReqMaxLevel() > 0 && level > tbv.getReqMaxLevel()) {
            return false;
        }
        if (tbv.getReqPreTask() > 0 && !doneTask.containsTask(tbv.getReqPreTask())) {
            return false;
        }
        return true;
    }

    public RoleAcceptTask isInAcceptTaskTable(int taskId_) {
        return acceptTaskTable.getAcceptTaskMap().get(taskId_);
    }

    public RoleCanAcceptTask isInCanAcceptTaskTable(int taskId_) {
        return canAcceptTaskTable.getCanAcceptTaskMap().get(taskId_);
    }

    /**
     * @param taskId 任务ID
     * @return 接取任务
     */
    public RoleAcceptTask acceptTask(int taskId) {
        if (doneTask.containsTask(taskId)) {
            return null;
        }
        TaskVo tbv = TaskManager.getTaskById(taskId);
        RoleAcceptTask roleAcceptTask = new RoleAcceptTask(taskId, id(), 0);
        // 升级任务需要特殊处理一下进度
        if (tbv.getType() == TaskManager.Task_Type_UpLevel) {
            RoleModule rmd = (RoleModule) this.module(MConst.Role);
            roleAcceptTask.setProcess(rmd.getLevel());
        } else if (tbv.getType() == TaskManager.Task_Type_CollectTool
                || tbv.getType() == TaskManager.Task_Type_CollectTool_NotDel) {
            ToolModule tmd = (ToolModule) this.module(MConst.Tool);
            roleAcceptTask.setProcess((int) tmd.getCountByItemId(Integer.parseInt(tbv.getTarget())));
        }
        this.acceptTaskTable.putAcceptTaskRaw(roleAcceptTask, tbv.getSort());

        // this.canAcceptTaskList.remove(taskId);
        RoleCanAcceptTask canAcceptTask = this.canAcceptTaskTable.getCanAcceptTaskRaw(taskId);
        if (canAcceptTask != null) {
            this.canAcceptTaskTable.removeCanAcceptTask(canAcceptTask);
            context().delete(canAcceptTask);
        }

        if (tbv.getType() == TaskManager.Task_Type_TalkWithNpc) {
            // 如果目标NPC和提交NPC是同一NPC,接取任务即标记为可提交
            if (tbv.getSubmitnpc() != 0 && tbv.getTarget().equals(String.valueOf(tbv.getSubmitnpc()))) {
                this.updateRoleAcceptTaskProcess(
                        TaskManager.getTaskKey(TaskManager.Task_Type_TalkWithNpc, tbv.getTarget()), 1, false);
            }
            // 如果接取任务的NPC既是目标对话NPC
            if (tbv.getAcceptnpc() != 0 && tbv.getTarget().equals(String.valueOf(tbv.getAcceptnpc()))) {
                this.updateRoleAcceptTaskProcess(
                        TaskManager.getTaskKey(TaskManager.Task_Type_TalkWithNpc, tbv.getTarget()), 1, false);
            }
        }

        if (tbv.getType() == TaskManager.Task_Type_PassBraveStage) {
            StageinfoVo stageinfoVo = SceneManager.getStageVo(Integer.parseInt(tbv.getTarget()));
            if (stageinfoVo != null) {
                SceneModule sceneModule = (SceneModule) this.module(MConst.Scene);
                sceneModule.enterScene(stageinfoVo.getStageType(), stageinfoVo.getStageId(), stageinfoVo.getStageId());
            }
        }
        if (tbv.getType() == TaskManager.Task_Type_PassGuanKa && roleAcceptTask != null) {
            DungeonModule dungeonModule = (DungeonModule) moduleMap().get(MConst.Dungeon);
            if (dungeonModule.isPassDungeon(Integer.parseInt(tbv.getTarget()))) {
                this.updateRoleAcceptTaskProcess(
                        TaskManager.getTaskKey(TaskManager.Task_Type_PassGuanKa, tbv.getTarget()), 1, false);
            }
        }
        if ((tbv.getType() == TaskManager.Task_Type_SoulLevel || tbv.getType() == TaskManager.Task_Type_SoulStage) && roleAcceptTask != null) {
            SoulModule soulModule = module(MConst.Soul);
            Map<Integer, SoulLevel> soulLevelsMap = soulModule.getSoulLevelsMap();
            RoleSoul roleSoul = soulModule.getRoleSoul();
            for (SoulLevel soulLevel : soulLevelsMap.values()) {
                updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_SoulLevel, soulLevel.getSoulGodType() + ""),
                        soulLevel.getSoulGodLevel(), true);
            }
            updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_SoulStage, "2"),
                    roleSoul.getStage(), true);
        }
        if (tbv.getSort() == TaskManager.Task_Sort_Daily) {
            if (tbv.getType() == TaskManager.Task_Type_RideUpLv) {
                // RideModule ride = (RideModule) moduleMap().get(MConst.Ride);
                // for (int level : ride.getRoleRideLevelMap().values()) {
                // maxLevel = level > maxLevel ? level : maxLevel;
                // }
                RoleModule roleModule = (RoleModule) module(MConst.Role);
                int maxLevel = roleModule.getRideLevelId();
                this.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_RideUpLv, "lv"), maxLevel,
                        true);
            }
            if (tbv.getType() == TaskManager.Task_Type_BuddyUpLv) {
                BuddyModule buddy = (BuddyModule) moduleMap().get(MConst.Buddy);
                int maxLevel = 0;
                for (int level : buddy.getRoleBuddyLevelMap().values()) {
                    maxLevel = level > maxLevel ? level : maxLevel;
                }
                this.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_BuddyUpLv, "lv"),
                        maxLevel, true);
            }
            if (tbv.getType() == TaskManager.Task_Type_EquipStrengthLv) {
                NewEquipmentModule equip = (NewEquipmentModule) moduleMap().get(MConst.NewEquipment);
                int maxLevel = 0;
                for (int level : equip.getRoleStrengthLevelMap().values()) {
                    maxLevel = level > maxLevel ? level : maxLevel;
                }
                this.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_EquipStrengthLv, "lv"),
                        maxLevel, true);
            }

            if (tbv.getType() == TaskManager.Task_Type_PvpCount) {
                NewOfflinePvpModule offlinePvpModule = module(MConst.NewOfflinePvp);
                if (offlinePvpModule != null && offlinePvpModule.getPvpCount() != -1) {
                    this.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_PvpCount, "time"),
                            offlinePvpModule.getPvpCount(), true);
                }
            }
        }

        context().insert(roleAcceptTask);
        // 接取成功打印日志
        if (roleAcceptTask != null) {
            ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
            log.Log_core_task(ThemeType.TASK_START.getOperateName(), "1", taskId, tbv.getSort());
        }
        return roleAcceptTask;
    }

    /**
     * @param taskId 任务ID
     * @return 提交/完成任务 是否成功
     */
    public boolean submitTask(int taskId) {
        RoleAcceptTask ratb = acceptTaskTable.getAcceptTaskRaw(taskId);
        if (ratb == null) {
            return false;
        }
        TaskVo tv = TaskManager.getTaskById(taskId);
        if (ratb.getProcess() < tv.getTargetCount()) {
            return false;
        }
        return submitTask(ratb, false);
    }

    /**
     * @param ratb 任务实体
     * @return 提交任务
     */
    public boolean submitTask(RoleAcceptTask ratb, boolean login) {
        TaskVo tbv = TaskManager.getTaskById(ratb.getTaskId());
        ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
        if (tbv == null) {
            return false;
        }
        if (tbv.getType() == TaskManager.Task_Type_CollectTool) {
            /** 收集类型的任务扣除道具 */
            ToolModule toolModule = module(MConst.Tool);
            if (!toolModule.deleteAndSend(Integer.valueOf(tbv.getTarget()), tbv.getTargetCount(),
                    EventType.SUBMITTASK.getCode())) {
                log.Log_core_task(ThemeType.TASK_FALI.getOperateName(), "1", ratb.getTaskId(), tbv.getSort());
                return false;
            }
        }
        // 处理奖励
        if (tbv.getAwardMap() != null) {
            ((ToolModule) module(MConst.Tool)).addAndSend(tbv.getAwardMap(), EventType.SUBMITTASK.getCode());
        }

//		if (tbv.getSort() == TaskManager.Task_Sort_ZhuXian || tbv.getSort() == TaskManager.Task_Sort_Daily) {// 只有主线任务才会加入到doneTask中
        doneTask.addTask(ratb.getTaskId());
        this.context().update(doneTask);
//		}

        acceptTaskTable.removeAcceptTask(ratb);// 删除已完成的接取任务
        this.context().delete(ratb);

        if (!login) {
            send(new ClientRemoveTask(ratb.getTaskId(), ClientRemoveTask.Flag_Finish_Task));// 通知客户端删除已完成任务
        }
        this.eventDispatcher().fire(new SubmitTaskEvent(ratb.getTaskId()));
        checkNext(ratb.getTaskId());
        checkAllTaskBySort(TaskManager.Task_Sort_Daily, false);
        // 任务提交完成日志

        log.Log_core_task(ThemeType.TASK_WIN.getOperateName(), "1", ratb.getTaskId(), tbv.getSort());
        return true;
    }

    public RoleAcceptTaskTable getAcceptTaskTable() {
        return acceptTaskTable;
    }

    /**
     * 更新任务完成进度
     *
     * @param key   toolId
     * @param count 需要增加的任务进度
     * @param flag  更新进度的方式
     *              true:覆盖，false:累加(默认是累加，只有BranchTaskListener中的坐骑等级，伙伴等级，
     *              装备强化等级才是覆盖进度)
     */
    public void updateRoleAcceptTaskProcess(String key, int count, boolean flag) {
        List<RoleAcceptTask> ls = this.acceptTaskTable.getAcceptTaskByKey(key);
        if (ls == null || ls.size() <= 0) {
            return;
        }
        TaskVo tv = null;
        List<Integer> ts = null;// 存放需要自动提交的任务
        for (RoleAcceptTask ratb : ls) {
            if (flag) {
                ratb.setProcess(count);
            } else {
                ratb.setProcess(ratb.getProcess() + count);
            }
            tv = TaskManager.getTaskById(ratb.getTaskId());
            // 不需要通过NPC提交的任务，完成后自动提交
            if (tv.getSubmitnpc() == 0 && ratb.getState() == TaskManager.Task_State_CanSubmit) {
                if (ts == null) {
                    ts = new ArrayList<Integer>();
                }
                ts.add(ratb.getTaskId());
                continue;
            }
            this.context().update(ratb);
            send(new ClientTaskProcess(ratb.getTaskId(), ratb.getProcess(), ratb.getState()));
        }
        if (ts != null) {
            for (int id : ts) {
                this.submitTask(id);
            }
        }
    }

    public RoleCanAcceptTaskTable getCanAcceptTaskTable() {
        return canAcceptTaskTable;
    }

    public void setAcceptTaskTable(RoleAcceptTaskTable acceptTaskTable) {
        this.acceptTaskTable = acceptTaskTable;
    }

    public RoleAcceptTask getRoleAcceptTask(int taskId) {
        return acceptTaskTable.getAcceptTaskRaw(taskId);
    }

    // 通过类型 检查并添加任务
    public void checkAllTaskBySort(byte sort, boolean login) {
        Map<Integer, TaskVo> map = TaskManager.getTaskVoMap();
        Set<Integer> cls = map.keySet();
        for (int taskId : cls) {
            TaskVo taskVo = TaskManager.getTaskById(taskId);
            boolean flag = taskVo.getSort() == TaskManager.Task_Sort_Daily;
            if (taskVo != null && taskVo.getSort() == sort && (flag ? true : taskVo.getReqPreTask() == 0)) {// 判断是否是任务组中的第一个任务
                RoleModule rmd = (RoleModule) this.module(MConst.Role);
                int level = rmd.getLevel();
                if (checkTaskCondition(taskVo, level)) {
                    // checkTask(taskId ,flag?false: true);
                    checkTask(taskId, login);
                }
            }
        }
    }

    /**
     * @param taskId 检查并加入任务
     * @return true：加入成功 ， false：加入失败
     */
    public boolean checkTask(int taskId, boolean login) {
        if (isInAcceptTaskTable(taskId) != null) {
            return false;
        }
        if (isInCanAcceptTaskTable(taskId) != null) {
            return false;
        }
        // 检查是否有同组的任务在可接受或已接受的任务中
        if (isInAcceptTaskGroup(taskId)) {
            return false;
        }
        TaskVo tv = TaskManager.getTaskById(taskId);
        // 已完成后不接取
        if (doneTask.containsTask(tv.getId())) {
            return false;
        }
        if (tv.getAcceptnpc() == 0) {
            RoleAcceptTask rat = acceptTask(taskId);
            // 接取任务后，如果条件满足且需要自动提交的
            if (!login) {
                newTask2Client.add(taskId);
                flushNewTask2Client();
            }
            if (rat != null && rat.getState() == TaskManager.Task_State_CanSubmit && tv.getSubmitnpc() == 0) {
                submitTask(rat, login);
            }

            if (tv.getType() == TaskManager.Task_Type_PassGuanKa && rat != null) {
                DungeonModule dungeonModule = (DungeonModule) moduleMap().get(MConst.Dungeon);
                if (dungeonModule.isPassDungeon(Integer.parseInt(tv.getTarget()))) {
                    this.updateRoleAcceptTaskProcess(
                            TaskManager.getTaskKey(TaskManager.Task_Type_PassGuanKa, tv.getTarget()), 1, false);
                }
            }
            return true;
        }
        RoleCanAcceptTask roleCanAcceptTask = new RoleCanAcceptTask(id(), taskId);
        canAcceptTaskTable.putCanAcceptTaskRaw(roleCanAcceptTask);
        context().insert(roleCanAcceptTask);

        if (!login) {
            newTask2Client.add(taskId);
            flushNewTask2Client();
        }
        return true;
    }

    public void flushNewTask2Client() {
        if (newTask2Client.size() > 0) {
            ClientTaskList ctl = new ClientTaskList();
            for (int taskId : newTask2Client) {
                RoleAcceptTask rat = acceptTaskTable.getAcceptTaskRaw(taskId);
                if (rat != null) {
                    ctl.addAccpt(rat);
                } else {
                    ctl.addCanAccept(taskId);
                }

            }
            ctl.setBravePractiseCount(getBravePractiseCount());
            send(ctl);
            newTask2Client.clear();
        }
    }

    /**
     * @param taskId 检查是否有下一步任务并加入，有则添加加一步任务并返回true，没有则返回false
     * @return true：有下一步任务 ， false：没有下一步任务
     */
    public boolean checkNext(int taskId) {
        TaskVo taskVo = TaskManager.getTaskById(taskId);
        if (taskVo == null) {
            return false;
        }
        int nextTaskId = taskVo.getNextTaskId();
        if (nextTaskId != 0) {
            return checkTask(nextTaskId, false);
        }

        return false;
    }

    /**
     * @param taskId 检查是否有同组的任务在acceptTask或canAcceptTaskList中
     * @return
     */
    public boolean isInAcceptTaskGroup(int taskId) {
        TaskVo taskVo = TaskManager.getTaskById(taskId);
        if (taskVo == null) {
            return false;
        }

        //FIXME 这里很奇怪 ，命名看的是group的判断，怎么就变成sort了？

        // 支线任务可以接多个
        if (taskVo.getSort() == TaskManager.Task_Sort_Daily) {
            return false;
        }

        if (taskVo.getSort() == TaskManager.Task_Sort_AutoDaily) {
            return false;
        }

        Map<Integer, RoleAcceptTask> acceptTaskMap = acceptTaskTable.getAcceptTaskMap();
        for (RoleAcceptTask task : acceptTaskMap.values()) {
            TaskVo tempTaskVo = TaskManager.getTaskById(task.getTaskId());
            if (tempTaskVo != null && tempTaskVo.getSort() == taskVo.getSort()) {
                return true;
            }
        }

        Map<Integer, RoleCanAcceptTask> canAcceptTaskMap = canAcceptTaskTable.getCanAcceptTaskMap();
        for (RoleCanAcceptTask task : canAcceptTaskMap.values()) {
            TaskVo tempTaskVo = TaskManager.getTaskById(task.getTaskId());
            if (tempTaskVo != null && tempTaskVo.getSort() == taskVo.getSort()) {
                return true;
            }
        }

        return false;
    }

    private void clearTask(byte resetType) {
        // 清除acceptTask的
        Map<Integer, RoleAcceptTask> acceptTaskMap = acceptTaskTable.getAcceptTaskMap();
        if (acceptTaskMap != null) {
            Set<Integer> keySet = acceptTaskMap.keySet();
            Set<Integer> tempKeySet = new HashSet<Integer>(keySet);
            for (Integer taskId : tempKeySet) {
                RoleAcceptTask roleAcceptTask = acceptTaskMap.get(taskId);
                if (roleAcceptTask != null) {
                    TaskVo taskVo = TaskManager.getTaskById(taskId);
                    if (taskVo == null)
                        continue;
                    boolean clearFlag = false;
                    if (resetType == TaskManager.Task_Reset_Type_DailyReset && taskVo.getDailyReset() == TaskManager.Task_Daily_Reset
                            && taskVo.getSort() != TaskManager.Task_Sort_ZhuXian) {
                        clearFlag = true;
                    } else if (resetType == TaskManager.Task_Reset_Type_Active && taskVo.getActive() == TaskManager.Task_Inactive) {
                        clearFlag = true;
                    }
                    if (clearFlag) {
                        acceptTaskTable.removeAcceptTask(roleAcceptTask);
                        this.context().delete(roleAcceptTask);

                        send(new ClientRemoveTask(taskId, ClientRemoveTask.Flag_Delete_Task));// 通知客户端删除该任务
                    }
                }

            }
        }

        // 清除canAcceptTaskList
        Map<Integer, RoleCanAcceptTask> canAcceptTaskMap = canAcceptTaskTable.getCanAcceptTaskMap();
        if (canAcceptTaskMap != null) {
            Set<Integer> keySet = canAcceptTaskMap.keySet();
            Set<Integer> tempKeySet = new HashSet<Integer>(keySet);
            for (Integer taskId : tempKeySet) {
                RoleCanAcceptTask roleCanAcceptTask = canAcceptTaskMap.get(taskId);
                if (roleCanAcceptTask != null) {
                    TaskVo taskVo = TaskManager.getTaskById(taskId);
                    if (taskVo == null)
                        continue;
                    boolean clearFlag = false;
                    if (resetType == TaskManager.Task_Reset_Type_DailyReset && taskVo.getDailyReset() == TaskManager.Task_Daily_Reset
                            && taskVo.getSort() != TaskManager.Task_Sort_ZhuXian) {
                        clearFlag = true;
                    } else if (resetType == TaskManager.Task_Reset_Type_Active && taskVo.getActive() == TaskManager.Task_Inactive) {
                        clearFlag = true;
                    }
                    if (clearFlag) {
                        canAcceptTaskTable.removeCanAcceptTask(roleCanAcceptTask);
                        this.context().delete(roleCanAcceptTask);

                        send(new ClientRemoveTask(taskId, ClientRemoveTask.Flag_Delete_Task));// 通知客户端删除该任务
                    }
                }
            }
        }

        HashMap<Integer, Object> doneTaskMap = doneTask.getDoneTaskMap();
        if (doneTaskMap != null) {
            Set<Integer> keySet = doneTaskMap.keySet();
            Set<Integer> tempKeySet = new HashSet<Integer>(keySet);
            boolean isChange = false;
            for (Integer taskId : tempKeySet) {
                TaskVo taskVo = TaskManager.getTaskById(taskId);
                if (taskVo == null)
                    continue;
                boolean clearFlag = false;
                if (resetType == TaskManager.Task_Reset_Type_DailyReset && taskVo.getDailyReset() == TaskManager.Task_Daily_Reset
                        && taskVo.getSort() != TaskManager.Task_Sort_ZhuXian) {
                    clearFlag = true;
                } else if (resetType == TaskManager.Task_Reset_Type_Active && taskVo.getActive() == TaskManager.Task_Inactive) {
                    clearFlag = true;
                }
                if (clearFlag) {
                    doneTask.removeTask(taskId);
                    isChange = true;
                }
            }
            if (isChange) {
                this.context().update(doneTask);
            }
        }
    }

    private void clearTaskBySort(byte sort) {
        // 清除acceptTask的
        Map<Integer, RoleAcceptTask> acceptTaskMap = acceptTaskTable.getAcceptTaskMap();
        if (acceptTaskMap != null) {
            Set<Integer> keySet = acceptTaskMap.keySet();
            Set<Integer> tempKeySet = new HashSet<Integer>(keySet);
            for (Integer taskId : tempKeySet) {
                RoleAcceptTask roleAcceptTask = acceptTaskMap.get(taskId);
                if (roleAcceptTask != null) {
                    TaskVo taskVo = TaskManager.getTaskById(taskId);
                    if (taskVo != null && taskVo.getSort() == sort) {
                        acceptTaskTable.removeAcceptTask(roleAcceptTask);
                        this.context().delete(roleAcceptTask);

                        send(new ClientRemoveTask(taskId, ClientRemoveTask.Flag_Delete_Task));// 通知客户端删除该任务
                    }
                }
            }
        }

        // 清除canAcceptTaskList
        Map<Integer, RoleCanAcceptTask> canAcceptTaskMap = canAcceptTaskTable.getCanAcceptTaskMap();
        if (canAcceptTaskMap != null) {
            Set<Integer> keySet = canAcceptTaskMap.keySet();
            Set<Integer> tempKeySet = new HashSet<Integer>(keySet);
            for (Integer taskId : tempKeySet) {
                RoleCanAcceptTask roleCanAcceptTask = canAcceptTaskMap.get(taskId);
                if (roleCanAcceptTask != null) {
                    TaskVo taskVo = TaskManager.getTaskById(taskId);
                    if (taskVo != null && taskVo.getSort() == sort) {
                        canAcceptTaskTable.removeCanAcceptTask(roleCanAcceptTask);
                        this.context().delete(roleCanAcceptTask);

                        send(new ClientRemoveTask(taskId, ClientRemoveTask.Flag_Delete_Task));// 通知客户端删除该任务
                    }
                }
            }
        }

        HashMap<Integer, Object> doneTaskMap = doneTask.getDoneTaskMap();
        if (doneTaskMap != null) {
            Set<Integer> keySet = doneTaskMap.keySet();
            Set<Integer> tempKeySet = new HashSet<Integer>(keySet);
            boolean isChange = false;
            for (Integer taskId : tempKeySet) {
                TaskVo taskVo = TaskManager.getTaskById(taskId);
                if (taskVo != null && taskVo.getSort() == sort) {
                    doneTask.removeTask(taskId);
                    isChange = true;
                }
            }
            if (isChange) {
                this.context().update(doneTask);
            }
        }


    }

    /**
     * 获取玩家完成任务的id
     *
     * @return
     */
    public List<Integer> getDoneTask() {
        List<Integer> list = new ArrayList<>();
        for (int taskId : doneTask.getDoneTaskMap().keySet()) {
            list.add(taskId);
        }
        return list;
    }

    public int getBravePractiseCount() {
        BravePractiseModule bravePractiseModule = module(MConst.BravePractise);
        return bravePractiseModule.getDoneCount();
    }
}
