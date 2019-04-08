package com.stars.services.newservermoney;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.data.DataManager;
import com.stars.modules.newservermoney.NewServerMoneyManager;
import com.stars.modules.newservermoney.event.NSMoneyRewardEvent;
import com.stars.modules.newservermoney.packet.ClientNewServerMoney;
import com.stars.modules.newservermoney.prodata.NewServerMoneyVo;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.chat.ChatManager;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerMoneyServiceActor extends ServiceActor implements NewServerMoneyService {
    private int curOperateActId = -1;// 当前活动Id,未开始=-1
    private List<ExecuteTask> executeTasks = new LinkedList<>();// 执行任务
//    static ScheduledExecutorService scheduler;

    public Calendar nowCalendar = Calendar.getInstance();//当前日期
    public Calendar initTaskCalendar = null;//当前任务列表生成的日期

    private Set<Long> rewardRecord;// 获奖记录

    @Override
    public void init() throws Throwable {
        rewardRecord = new HashSet<>();
        ServiceSystem.getOrAdd(SConst.NewServerMoneyService, this);
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerMoney);
        if (curActId != -1) {
            openActivity(curActId);
        }
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},executeTasks.size:{},rewardRecord.size:{}",
                this.getClass().getSimpleName(), executeTasks == null ? 0 : executeTasks.size(), rewardRecord == null ? 0 : rewardRecord.size());
    }

    @Override
    public void openActivity(int activityId) {
        this.curOperateActId = activityId;
        rewardRecord = new HashSet<>();
        initTask();
//        if (scheduler == null || scheduler.isShutdown()) {
//            scheduler = Executors.newScheduledThreadPool(1);
//        }
//        scheduler.scheduleAtFixedRate(new SchedulerTask(), 0, 1, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.NewServerMoney, new SchedulerTask(), 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void closeActivity(int activityId) {
        if (curOperateActId != activityId) {
            return;
        }
        this.curOperateActId = -1;
        // 停止定时线程
        SchedulerManager.shutDownNow(ExcutorKey.NewServerMoney);
//        scheduler.shutdownNow();
        if (!executeTasks.isEmpty()) {
            executeTasks.clear();
        }
        if (!rewardRecord.isEmpty()) {
            rewardRecord.clear();
        }
    }

    @Override
    public void executeTask() {
        //检查并重置发奖任务列表
        checkExecuteTask();

        if (executeTasks.isEmpty())
            return;
        ExecuteTask task = executeTasks.get(0);
        if (System.currentTimeMillis() < task.getExecuteTime()) {
            return;
        }
        // 执行抽奖
        grantReward(task.getMoneyType());
        executeTasks.remove(0);
    }

    @Override
    public void dailyReset() {
        // 清空今日获奖记录
        if (curOperateActId != -1 && !rewardRecord.isEmpty()) {
            rewardRecord.clear();
        }
    }

    public void checkExecuteTask() {
        //判断是否要重置发奖任务列表
        if (curOperateActId != -1 && initTaskCalendar != null) {
            nowCalendar.setTimeInMillis(System.currentTimeMillis());
            //若当前时间跟上次生成任务列表时间不是同一天，则重置发奖任务列表
            if (nowCalendar.get(Calendar.DAY_OF_YEAR) != initTaskCalendar.get(Calendar.DAY_OF_YEAR) ||
                    nowCalendar.get(Calendar.YEAR) != initTaskCalendar.get(Calendar.YEAR)) {
                LogUtil.info("NewServerMoneyServiceActor.checkExecuteTask reset executeTasks activityId=" + curOperateActId);

                executeTasks.clear();
                initTask();
            }
        }
    }

    /**
     * 发奖
     */
    public void grantReward(int grantMoneyType) {
        NewServerMoneyVo vo = NewServerMoneyManager.getMoneyVo(curOperateActId, grantMoneyType);
        if (vo == null)
            return;
        List<Summary> resultList = choose(vo.getLevelLimit(), vo.getPlayerNum());
        // 发奖
        long rewardTime = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder("");
        for (Summary summary : resultList) {
            ServiceHelper.roleService().notice(summary.getRoleId(),
                    new NSMoneyRewardEvent(NSMoneyRewardEvent.TAKE_REWARD, grantMoneyType, rewardTime, curOperateActId));
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(((RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE)).getRoleName());
        }
        // 滚屏/聊天通知
        ServiceHelper.chatService().announce(DataManager.getGametext("newservermoney_boardinfo"),
                builder.toString(), vo.getRewardNotice());
        String message = DataManager.getGametext("newservermoney_messageinfo");
        message = message.replaceFirst("%s", builder.toString()).replaceFirst("%s", vo.getRewardNotice());
        ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_WORLD, 0L, 0L, message, Boolean.TRUE);
        // 更新获奖记录
        updateRewardRecord(resultList);
        // 广播结果
        ClientNewServerMoney packet = new ClientNewServerMoney(ClientNewServerMoney.REWARD_RESULT);
        packet.setMoneyRewardType(grantMoneyType);
        packet.setRewardResult(resultList);
        ServiceUtil.sendPacketToOnline(packet, null);
        ServiceHelper.roleService().noticeAll(new NSMoneyRewardEvent(NSMoneyRewardEvent.SEND_REWARD_RECORD));
    }

    private void initTask() {
        executeTasks = new LinkedList<>();
        Map<Long, ExecuteTask> map = new HashMap<>();
        Map<Integer, NewServerMoneyVo> voMap = NewServerMoneyManager.getMoneyVoMap(curOperateActId);
        if (voMap == null)
            return;
        for (NewServerMoneyVo vo : voMap.values()) {
            long executeTime = DateUtil.hourStrTimeToDateTime(vo.getStartTime()).getTime();
            for (int i = 0; i < vo.getExtractTimes(); i++) {
                executeTime = executeTime + (i == 0 ? 0 : vo.getInterval() * 1000L);
                if (System.currentTimeMillis() >= executeTime) {
                    continue;
                }
                ExecuteTask task = new ExecuteTask(executeTime, vo.getType());
                executeTasks.add(task);
                if (map.containsKey(executeTime)) {
                    LogUtil.error("newservermoney表配置时间重叠,type={},type={}", map.get(executeTime).getMoneyType(),
                            task.getMoneyType());
                    continue;
                }
                map.put(executeTime, task);
            }
        }
        Collections.sort(executeTasks);

        initTaskCalendar = Calendar.getInstance();

        LogUtil.info("NewServerMoneyServiceActor.initTask finish init executeTasks activityId=" + curOperateActId);
    }

    private void updateRewardRecord(List<Summary> list) {
        for (Summary summary : list) {
            rewardRecord.add(summary.getRoleId());
        }
    }

    private List<Summary> choose(int minLevel, int chooseNum) {
        List<Summary> onlines = ServiceHelper.summaryService().getAllOnlineSummary();
        List<Summary> chooseList = new LinkedList<>();// 备选
        for (Summary summary : onlines) {
            if (summary == null || summary.isDummy())
                continue;
            RoleSummaryComponent rsc = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (rsc == null || rsc.isDummy())
                continue;
            if (rsc.getRoleLevel() < minLevel)
                continue;
            if (SpecialAccountManager.isSpecialAccount(summary.getRoleId())) {
                continue;
            }
            chooseList.add(summary);
        }
        List<Summary> resultList = new LinkedList<>();// 结果
        Collections.shuffle(chooseList);
        // 选取
        if (chooseList.size() <= chooseNum) {
            resultList.addAll(chooseList);
        } else {
            List<Summary> rewardList = new LinkedList<>();
            List<Summary> notRewardList = new LinkedList<>();
            for (Summary summary : chooseList) {
                if (rewardRecord.contains(summary.getRoleId())) {
                    rewardList.add(summary);
                } else {
                    notRewardList.add(summary);
                }
            }
            while (resultList.size() < chooseNum) {
                if (!notRewardList.isEmpty()) {
                    resultList.add(notRewardList.remove(0));
                    continue;
                }
                if (!rewardList.isEmpty()) {
                    Random r = new Random();
                    int index = r.nextInt(rewardList.size());
                    resultList.add(rewardList.get(index));
                }
            }
        }
        return resultList;
    }

    class ExecuteTask implements Comparable<ExecuteTask> {
        private long executeTime;
        private int moneyType;

        public ExecuteTask(long executeTime, int moneyType) {
            this.executeTime = executeTime;
            this.moneyType = moneyType;
        }

        public long getExecuteTime() {
            return executeTime;
        }

        public int getMoneyType() {
            return moneyType;
        }

        @Override
        public int compareTo(ExecuteTask o) {
            if (o.getExecuteTime() != this.getExecuteTime()) {
                return (int) (this.getExecuteTime() - o.getExecuteTime());
            }
            return 1;
        }
    }

    class SchedulerTask implements Runnable {

        @Override
        public void run() {
            ServiceHelper.newServerMoneyService().executeTask();
        }
    }

}
