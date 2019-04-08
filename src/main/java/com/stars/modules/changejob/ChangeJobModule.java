package com.stars.modules.changejob;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.changejob.event.ChangeJobAchieveEvent;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.changejob.packet.ClientChangeJobPacket;
import com.stars.modules.changejob.prodata.ChangeJobVo;
import com.stars.modules.changejob.userdata.AccountJobActive;
import com.stars.modules.changejob.userdata.RoleChangeJob;
import com.stars.modules.daily5v5.Daily5v5Module;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.demologin.packet.ClientAccountRoleList;
import com.stars.modules.familyactivities.war.FamilyActWarModule;
import com.stars.modules.loottreasure.LootTreasureModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolModule;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by huwenjun on 2017/5/24.
 */
public class ChangeJobModule extends AbstractModule implements AccountRowAware {
    private AccountRow accountRow;
    private List<Integer> jobTypes = null;
    private RoleChangeJob roleChangeJob = null;

    public ChangeJobModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {

        String roleChangeJobSql = "select * from rolechangejob where roleid=%s";
        roleChangeJob = DBUtil.queryBean(DBUtil.DB_USER, RoleChangeJob.class, String.format(roleChangeJobSql, id()));
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleChangeJob == null) {
            RoleModule roleModule = module(MConst.Role);
            roleChangeJob = new RoleChangeJob(id(), roleModule.getRoleRow().getJobId(), 0L);
            context().insert(roleChangeJob);
        }
        refreshActive();
    }

    /**
     * 转职
     *
     * @param jobId
     */
    public void changeJob(Integer jobId) {
        com.stars.util.LogUtil.info("{} start change job：{}", id(), jobId);
        RoleModule roleModule = module(MConst.Role);
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        int myJobId = roleModule.getRoleRow().getJobId();
        if (jobId.equals(myJobId)) {
            return;
        }

        if (!checkEnvironmentCondition()) {
            return;
        }
        ChangeJobVo changeJobVo = ChangeJobManager.changeJobMap.get(jobId);
        boolean success = checkChangeCondition(changeJobVo,true);
        if (success) {
            /**
             * 触发转职事件
             * 1.friendrole
             * 2.家族
             * 3.结婚
             * 4.离线pvp
             * 5.收发花
             * 6，组队
             */
            eventDispatcher().fire(new ChangeJobEvent(jobId));
            fireChageJobAchieveEvent(jobId);
            roleChangeJob.setJobId(jobId);
            roleChangeJob.setChangeTime(System.currentTimeMillis());
            context().update(roleChangeJob);
            /**
             * 特性日志
             */
            StringBuilder sb = new StringBuilder();
            sb.append("code:").append(jobId);
            serverLogModule.log_change_job(ChangeJobConst.SERVER_LOG_OPERATE_ID, ChangeJobConst.SERVER_LOG_CHANGE_JOB, sb.toString(), "");

            ClientChangeJobPacket clientChangeJobPacket = new ClientChangeJobPacket(ClientChangeJobPacket.SEND_CHANGE_JOB_SUCCESS);
            send(clientChangeJobPacket);
        }
    }

    /**
     * 检测环境条件
     * 1.家族战精英场
     * 2.日常5v5
     * 3.夺宝奇兵
     *
     * @return
     */
    private boolean checkEnvironmentCondition() {
        Daily5v5Module daily5v5Module = module(MConst.Daily5v5);
        /**
         * 判断是否处于5v5
         */
        int continueFihgtServerId = daily5v5Module.getContinueFihgtServerId();
        if (continueFihgtServerId != 0) {
            warn("changejob_tips_fightlimit", "函谷战场");
            return false;
        }
        /**
         * 判断是否处于家族战
         */
        FamilyActWarModule familyActWarModule = module(MConst.FamilyActWar);
        if (familyActWarModule.isInEliteFihgt()) {
            warn("changejob_tips_fightlimit", "家族战");
            return false;
        }
        /**
         * 判断是否处于夺宝奇兵
         */
        LootTreasureModule lootTreasureModule = module(MConst.LootTreasure);
        if (lootTreasureModule.isInLootTreasure()) {
            warn("changejob_tips_fightlimit", "夺宝奇兵");
            return false;
        }
        return true;
    }

    /**
     * 检测转职条件
     *
     * @param changeJobVo
     * @return
     */
    private boolean checkChangeCondition(ChangeJobVo changeJobVo, boolean delete) {
        boolean beforeCondition;
        if (roleChangeJob != null) {
            Long changeTime;
            changeTime = roleChangeJob.getChangeTime();
            Date lastChangeTime = new Date(changeTime);
            int secondsBetweenTwoDates = DateUtil.getSecondsBetweenTwoDates(lastChangeTime, new Date());
            beforeCondition = changeJobVo.getChangetime() <= secondsBetweenTwoDates;
            if (!beforeCondition) {
                int betweenTime = changeJobVo.getChangetime() - secondsBetweenTwoDates;
                int hour = betweenTime / 60 / 60;
                int minute = (betweenTime - hour * 60 * 60) / 60;
                int second = betweenTime - hour * 60 * 60 - minute * 60;
                warn("changejob_tips_cooling", hour + "时:" + minute + "分:" + second + "秒");
            }
        } else {
            beforeCondition = true;

        }
        ToolModule toolModule = module(MConst.Tool);
        if (beforeCondition) {
            boolean success = true;
            for (Map.Entry<Integer, Integer> entry : changeJobVo.getReqJobMap().entrySet()) {
                long countByItemId = toolModule.getCountByItemId(entry.getKey());
                success = success && (countByItemId >= entry.getValue());
            }
            if (success) {
                if (delete) {
                    /**
                     * 先扣物品
                     */
                    for (Map.Entry<Integer, Integer> entry : changeJobVo.getReqJobMap().entrySet()) {
                        toolModule.deleteAndSend(entry.getKey(), entry.getValue(), EventType.CHANGE_JOB.getCode());
                    }
                }

            } else {
                warn("changejob_tips_lackitem");
            }
            return success;
        }
        return false;


    }

    /**
     * 刷新激活
     */
    public void refreshActive() {
        RoleModule roleModule = module(MConst.Role);
        try {
            jobTypes = accountRow.getActivedJobs();
            if (accountRow.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                for (Map.Entry<Integer, ChangeJobVo> entry : ChangeJobManager.changeJobMap.entrySet()) {
                    ChangeJobVo changeJobVo = entry.getValue();
                    if (changeJobVo.getChange() == 1) {
                        if (!jobTypes.contains(changeJobVo.getJobType())) {
                            AccountJobActive accountJobActive = new AccountJobActive(accountRow.getName(), changeJobVo.getJobType());
                            context().insert(accountJobActive);
                            jobTypes.add(changeJobVo.getJobType());
                        }
                    }
                }
                int jobId = roleModule.getRoleRow().getJobId();
                if (!jobTypes.contains(jobId)) {
                    AccountJobActive accountJobActive = new AccountJobActive(accountRow.getName(), jobId);
                    context().insert(accountJobActive);
                    jobTypes.add(jobId);
                }
            }
        } catch (InterruptedException e) {
            com.stars.util.LogUtil.info(e.getMessage(), e);
        } finally {
            accountRow.getLoginLock().unlock();
        }
    }

    /**
     * 激活指定职业
     *
     * @param newJobId
     */
    public void activeJob(Integer newJobId) {
        if (!jobTypes.contains(newJobId)) {
            ChangeJobVo changeJobVo = ChangeJobManager.changeJobMap.get(newJobId);
            ToolModule toolModule = module(MConst.Tool);
            ServerLogModule serverLogModule = module(MConst.ServerLog);
            boolean success = checkActiveCondition(changeJobVo);
            if (success) {
                com.stars.util.LogUtil.info("active job roleid:{},jobid:{}",id(),newJobId);
                for (Map.Entry<Integer, Integer> entry : changeJobVo.getReqItemMap().entrySet()) {
                    toolModule.deleteAndSend(entry.getKey(), entry.getValue(), EventType.ACTIVITE_JOB.getCode());
                }
                AccountJobActive accountJobActive = new AccountJobActive(accountRow.getName(), changeJobVo.getJobType());
                context().insert(accountJobActive);
                try {
                    if (accountRow.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                        jobTypes.add(changeJobVo.getJobType());
                    }
                } catch (InterruptedException e) {
                    LogUtil.info(e.getMessage(), e);
                } finally {
                    accountRow.getLoginLock().unlock();
                }
                /**
                 * 特性日志
                 */
                StringBuilder sb = new StringBuilder();
                sb.append("code:").append(newJobId);
                sb.append("#consume@number:").append(StringUtil.makeString(changeJobVo.getReqItemMap(), '@', '&'));
                serverLogModule.log_change_job(ChangeJobConst.SERVER_LOG_OPERATE_ID, ChangeJobConst.SERVER_LOG_ACTIVE, sb.toString(), "");
                warn("changejob_tips_unbindwin");
                sendActivedJobs();
            } else {
                warn("changejob_tips_lackitem");
            }
        }
    }

    public void gotoActiveView(Integer jobId) {
        ClientChangeJobPacket clientChangeJobPacket = new ClientChangeJobPacket(ClientChangeJobPacket.GO_TO_ACTIVE_VIEW);
        clientChangeJobPacket.setActivedJobs(jobTypes);
        clientChangeJobPacket.setLastChangeTime(roleChangeJob.getChangeTime());
        clientChangeJobPacket.setNeedActiveJobId(jobId);
        send(clientChangeJobPacket);
    }

    /**
     * 检测激活条件
     *
     * @param changeJobVo
     * @return
     */
    private boolean checkActiveCondition(ChangeJobVo changeJobVo) {
        boolean success = true;
        ToolModule toolModule = module(MConst.Tool);
        RoleModule roleModule = module(MConst.Role);
        int vipLevel = accountRow.getVipLevel();
        int level = roleModule.getLevel();
        success = success && (changeJobVo.getViplevel() <= vipLevel);
        success = success && (changeJobVo.getLevel() <= level);
        for (Map.Entry<Integer, Integer> entry : changeJobVo.getReqItemMap().entrySet()) {
            long countByItemId = toolModule.getCountByItemId(entry.getKey());
            success = success && (countByItemId >= entry.getValue());
        }
        return success;
    }

    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }

    public boolean isActivedJob(Integer jobId) {
        return jobTypes.contains(jobId);
    }

    /**
     * 下发激活角色列表
     */
    public void sendActivedJobs() {
        ClientChangeJobPacket clientChangeJobPacket = new ClientChangeJobPacket(ClientChangeJobPacket.SEND_ACTIVED_JOBS);
        clientChangeJobPacket.setActivedJobs(jobTypes);
        clientChangeJobPacket.setLastChangeTime(roleChangeJob.getChangeTime());
        send(clientChangeJobPacket);
    }

    public void sendAllJobs() {
        ClientChangeJobPacket clientChangeJobPacket = new ClientChangeJobPacket(ClientChangeJobPacket.SEND_ALL_JOBS);
        send(clientChangeJobPacket);
    }

    public void gotoSelectRoleUI() {
        try {
            ClientAccountRoleList packet = new ClientAccountRoleList(accountRow);
            send(packet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fireChageJobAchieveEvent(int newJobId){
        ChangeJobAchieveEvent event = new ChangeJobAchieveEvent(newJobId);
        eventDispatcher().fire(event);
    }

    /**
     * 请求条件检测
     *
     * @param newJobId
     */
    public void reqConditionCheck(int newJobId) {

        if (!checkEnvironmentCondition()) {
            return;
        }
        ChangeJobVo changeJobVo = ChangeJobManager.changeJobMap.get(newJobId);
        boolean success = checkChangeCondition(changeJobVo,false);
        if (success) {
            ClientChangeJobPacket clientChangeJobPacket = new ClientChangeJobPacket(ClientChangeJobPacket.SEND_CHECK_CONDITION_SUCCESS);
            clientChangeJobPacket.setJobId(newJobId);
            send(clientChangeJobPacket);
        }
    }
}
