package com.stars.modules.role;

import com.stars.AccountRow;
import com.stars.core.attr.Attr;
import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.packet.ClientReconnect;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.event.*;
import com.stars.modules.role.packet.ClientHeartBeatCheck;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.role.packet.ClientRoleResource;
import com.stars.modules.role.prodata.Grade;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.tool.ToolManager;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.*;


public class RoleModule extends AbstractModule {
    private Role role;
    private boolean isLastTimeVigorMax; // 前一次检查体力时是否体力全满
    private boolean isLastTimeBabyEnergyMax;//前一次检查宝宝精力时是否体力全满
    private String arroundId = "";
    public static final String ROLE_AUTO_FLAG = "role.auto.flag"; // 自动战斗标记
    public static int HeartBeatCheckDisTime = 30_000;
    public static int HeartBeatReplyTime = 10_000;

    public long lastSendHeartBeatCheckTime = 0;

    public HashMap<String, Long> heartBeatCheckData;

    public RoleModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("用户", id, self, eventDispatcher, moduleMap);
        heartBeatCheckData = new HashMap<String, Long>();
    }


    public Role getRoleRow() {
        return role;
    }

    @Override
    public void onDataReq() throws Exception {
        String sql = "select * from `role` where `roleid`=" + id();
        role = DBUtil.queryBean(DBUtil.DB_USER, Role.class, sql);


    }


    @Override
    public void onCreation(String name, String account) throws SQLException {
        role = new Role(id());
        role.setVigorMax(RoleManager.getGradeByJobLevel(role.getJobId(), role.getLevel()).getVigorMax());
        resetVigor();
    }

    @Override
    public void onInit(boolean isCreation) {
        updateGradeBaseAttr();
        role.setVigorMax(RoleManager.getGradeByJobLevel(role.getJobId(), role.getLevel()).getVigorMax());
        checkAndRecoverVigor();
    }


    @Override
    public void onSyncData() {
        com.stars.util.LogUtil.info("登录下发用户基础数据,role={}", id());

        ClientRole clientRole = new ClientRole(ClientRole.SEND_ROLE, role);


        send(clientRole);
        /* 发送体力购买次数 */
        ClientRoleResource clientVigor = new ClientRoleResource(ClientRoleResource.VIGOR_BUY);
        clientVigor.setCurrentBuyCount(role.getVigorBuyCount());
        send(clientVigor);
        /* 发送金币购买数据 */
        sendUpdateBuyMoney((byte) 0);
        eventDispatcher().fire(new RoleLevelAchieveEvent(role.getLevel(), role.getLevel())); //成就达成登陆检测
        eventDispatcher().fire(new FightScoreAchieveEvent(id(), role.getFightScore(), role.getFightScore(), role.getLevel()));
    }

    @Override
    public void onTimingExecute() {
        /* 体力恢复 */
        checkAndRecoverVigorWithSending();
        heartBeatCheck();
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        /* 体力恢复 */
        if (resetVigor()) {
            send(new ClientRole(ClientRole.UPDATE_RESOURCE, role));
        }
        /* 发送体力购买次数 */
        role.setVigorBuyCount(0);
        ClientRoleResource clientVigor = new ClientRoleResource(ClientRoleResource.VIGOR_BUY);
        clientVigor.setCurrentBuyCount(role.getVigorBuyCount());
        send(clientVigor);
        /* 金币购买次数 */
        role.setFreeBuyMoneyCount((byte) 0);
        role.setPayBuyMoneyCount((byte) 0);
        sendUpdateBuyMoney((byte) 0);
        /* 重置已复活次数 */
        role.resetReviveNum();
        role.setDailyThrowGoldCount(0);
        context().update(role);
        send(new ClientRole(ClientRole.UPDATE_REVIVE_NUM, role));
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        LoginModule loginModule = module(MConst.Login);
        int channnel = loginModule.getChannnel();

    }


    /**
     * 获得当前主公等级
     *
     * @return
     */
    public int getLevel() {
        return getRoleRow().getLevel();
    }

    /**
     * 判断能否增加经验
     * 达到最大等级的false,其他情况返回true
     *
     * @return
     */
    public boolean canAddExp(int addValue) {
        if (addValue < 0) {
            com.stars.util.LogUtil.info("增加主公经验为负:{}", addValue);
            return false;
        }
        if (RoleManager.getRequestExpByJobLevel(role.getJobId(), role.getLevel() + 1) == -1) {
            return false;
        }
        return true;
    }

    /**
     * 增加玩家经验
     * 需在经过canAddExp检查方法之后调用
     *
     * @param addValue
     */
    public void addExp(int addValue) {
        int levelBefore = role.getLevel();

        role.addExp(addValue);
        context().update(role);
        send(new ClientRole(ClientRole.UPDATE_EXP, role));
        if (getRoleRow().getLevel() > levelBefore) {
            // 升级更新等级对应基础属性、战力
            int lastFightScore = role.getFightScore();
            updateGradeBaseAttr();
            role.setVigorMax(RoleManager.getGradeByJobLevel(role.getJobId(), role.getLevel()).getVigorMax());
            send(new ClientRole(ClientRole.UPDATE_ATTR, role));
            sendUpdateFightScore();

            signCalRedPoint(MConst.Tool, RedPointConst.BAG_USE_BOX);
            // 升级抛出事件
            eventDispatcher().fire(new RoleLevelUpEvent(getRoleRow().getLevel(), levelBefore));
            eventDispatcher().fire(new RoleLevelAchieveEvent(getRoleRow().getLevel(), levelBefore));

        }
    }

    /**
     * 更新角色等级对应基础属性、战力
     */
    private void updateGradeBaseAttr() {
        updatePartAttr(RoleManager.ROLEATTR_GRADEBASE,
                RoleManager.getGradeByJobLevel(role.getJobId(), role.getLevel()).getAttribute());
        // 更新战力;
        Grade grade = RoleManager.getGradeByJobLevel(role.getJobId(), role.getLevel());
        updatePartFightScore(RoleManager.FIGHTSCORE_GRADE, FormularUtils.calFightScore(grade.getAttribute()));
    }

    public void addResource(byte resourceId, int count) {
        addResource(resourceId, count, 0);
    }

    public void addResource(byte resourceId, int count, int clientSystemConstant) {
        if (resourceId == ToolManager.EXP) {
            this.addExp(count);
            return;
        }

        if (resourceId == ToolManager.VIGOR && count > 0) {
            /* 如果玩家当前休力值超过可储存的最大体力上限，不可在添加，同时不处理本次添加操作*/
            //+ count
            if (role.getVigor() >= RoleManager.canSaveMaxVigor) {
                return;
            }
        }

        role.addResource(resourceId, count);
        context().update(role);
        //打印日志
        ClientRole clientRole = new ClientRole(ClientRole.UPDATE_RESOURCE, role);
        clientRole.setClientSystemConstant(clientSystemConstant);
        send(clientRole);
    }

    public int getResource(byte resourceId) {
        return role.getResource(resourceId);
    }

    /**
     * 获得角色所在安全区场景Id
     *
     * @return
     */
    public int getSafeStageId() {
        return role.getSafeStageId();
    }

    /**
     * 更新角色所在安全区场景Id
     *
     * @param safeStageId
     */
    public void updateSafeStageId(int safeStageId) {
        role.setSafeStageId(safeStageId);
        context().update(role);
    }


    public void setLastEnterWeddingSceneId(String lastEnterWeddingSceneId) {
        role.setLastEnterWeddingSceneId(lastEnterWeddingSceneId);
        context().update(role);
    }

    public String getLastEnterWeddingSceneId() {
        return role.getLastEnterWeddingSceneId();
    }

    /**
     * 刷新周围玩家的场景id，用于拼接
     *
     * @param id
     */
    public void updateArroundId(String id) {
        this.arroundId = id;
    }

    public String getArroundId() {
        return this.arroundId;
    }

    /**
     * 更新对应部分属性
     * 调用后需调用下发方法通知客户端更新
     *
     * @param key
     * @param attribute
     */
    public void updatePartAttr(String key, Attribute attribute) {
        role.getAttrMap().put(key, attribute);
        // 计算总属性
        Attribute totalAttr = new Attribute();
        for (Attribute attr : role.getAttrMap().values()) {
            totalAttr.addAttribute(attr);
        }
        role.setTotalAttr(totalAttr);
    }

    /**
     * 更新对应部分战力
     * 调用后需调用下发方法通知客户端更新
     *
     * @param key
     * @param fightScore
     */
    public void updatePartFightScore(String key, int fightScore) {
        role.getFightScoreMap().put(key, fightScore);
        int newFightScore = 0;
        for (int score : role.getFightScoreMap().values()) {
            newFightScore = newFightScore + score;
        }
        int preFightScore = role.getFightScore();
        if (preFightScore != newFightScore) {
            role.setFightScore(newFightScore);
            eventDispatcher().fire(new FightScoreChangeEvent(id(), preFightScore, newFightScore, role.getLevel()));
            eventDispatcher().fire(new FightScoreAchieveEvent(id(), preFightScore, newFightScore, role.getLevel()));
        }

        /* 更新常用/摘要数据 */
        updateRoleSummaryComp();
    }

    /**
     * 下发客户端各部分战力和总战力
     */
    public void sendUpdateFightScore() {
        ClientRole packet = new ClientRole(ClientRole.UPDATE_FIGHTSCORE, role);
        send(packet);
    }

    /**
     * 下发客户端各部分战力和总战力
     * 如果想要客户端不弹出提示,需要调用此接口且参数isShow为false
     */
    public void sendUpdateFightScore(boolean isShow) {
        ClientRole packet = new ClientRole(ClientRole.UPDATE_FIGHTSCORE, role);
        if (!isShow) {
            packet.setIsShow((byte) 0);
        }
        send(packet);
    }

    public int getTitleId() {
        return role.getTitleId();
    }

    /**
     * 获取人物总战力
     */
    public int getFightScore() {
        int fightScore = 0;
        if (role != null) {
            fightScore = role.getFightScore();
        }

        return fightScore;
    }

    /**
     * 更新使用称号
     *
     * @param titleId
     */
    public void updateTitleId(int titleId) {
        role.setTitleId(titleId);
        context().update(role);
        ClientRole clientRole = new ClientRole(ClientRole.UPDATE_TITLE, role);
        send(clientRole);
        updateRoleSummaryComp();
    }

    public void sendRoleAttr() {
        send(new ClientRole(ClientRole.UPDATE_ATTR, role));
    }

    private void checkAndRecoverVigorWithSending() {
        if (checkAndRecoverVigor()) {
            // todo: send remaining time
            send(new ClientRole(ClientRole.UPDATE_RESOURCE, role));
        }
        if (checkAndRecoverBabyEnergy()) {
            ClientRole clientRoleEnergy = new ClientRole(ClientRole.UPDATE_RESOURCE, role);
            clientRoleEnergy.setClientSystemConstant(MConst.CCBABY);
            send(clientRoleEnergy);
        }
    }

    private boolean checkAndRecoverBabyEnergy() {
        int tmpEnergy = role.getBabyEnergy();
        if (isLastTimeBabyEnergyMax) {
            role.setBabyEnergyRecTimestamp(System.currentTimeMillis());
            isLastTimeBabyEnergyMax = false;
            return false;
        }
        role.setBabyEnergy(tmpEnergy);
        role.setBabyEnergyRecTimestamp(System.currentTimeMillis());
        context().update(role);
        return true;
    }

    private boolean checkAndRecoverVigor() {
        int vigorMax = role.getVigorMax();
        int vigor = role.getVigor();
        if (vigor >= vigorMax) {
            isLastTimeVigorMax = true;
            return false;
        }
        if (isLastTimeVigorMax) {
            role.setVigorRecoveryTimestamp(System.currentTimeMillis());
            isLastTimeVigorMax = false;
            return false;
        }
        long delta = System.currentTimeMillis() - role.getVigorRecoveryTimestamp();
        int times = (int) (delta / RoleManager.VIGOR_RECOVERY_INTERVAL);
        if (times <= 0) {
            return false;
        }
        vigor += RoleManager.VIGOR_RECOVERY_NUMBER * times;
        vigor = vigor > vigorMax ? vigorMax : vigor;
        role.setVigor(vigor);
        role.setVigorRecoveryTimestamp(System.currentTimeMillis());
        context().update(role);
        
        /* 更新体力常用/摘要数据 */
        updateRoleSummaryComp();

        return true;
    }

    private boolean resetVigor() {
        if (role.getVigor() < role.getVigorMax()) {
            role.setVigor(role.getVigorMax());
            role.setVigorRecoveryTimestamp(System.currentTimeMillis());
            context().update(role);
            
            /* 更新体力常用/摘要数据 */
            updateRoleSummaryComp();

            return true;
        }
        return false;
    }

    /**
     * 增加好友赠送的体力
     */
    public void innerAddFriendVigor(int vigor) {
        if (vigor <= 0) return;

        if (role.getVigor() >= RoleManager.canSaveMaxVigor) {
            warn("您的体力已经超出限制");
            return;
        }
        role.addResource((byte) ToolManager.VIGOR, vigor); // 增加体力

        context().update(role);
        
        /* 更新体力常用/摘要数据 */
        updateRoleSummaryComp();

        warn(I18n.get("friend.vigor.receiveSuccess", vigor));
        // 同步数据，发送提示
        send(new ClientRole(ClientRole.UPDATE_RESOURCE, role)); // 更新资源相关
    }

    /**
     * 购买体力
     */
    public void buyVigor() {

    }

    /**
     * 单次购买金币
     */
    public void buyMoneyOnce() {

    }

    /**
     * 批量购买金币
     */
    public void buyMoneyMulti() {

    }

    public void sendUpdateBuyMoney(byte result, int... params) {

    }


    private void updateRoleSummaryComp() {
//        try {
//            ServiceHelper.summaryService().updateSummaryComponent(id(), new RoleSummaryComponentImpl(
//                    id(), role.getName(), role.getLevel(), role.getJobId(), role.getFightScore(), role.getTitleId(), role.getTotalAttr(), role.getFightScoreMap()));
//        } catch (Exception e) {
//            LogUtil.error("", e);
//        }
        context().markUpdatedSummaryComponent(MConst.Role);
    }

    /**
     * 获得已复活次数
     *
     * @param stageType
     * @return
     */
    public byte getReviveNum(byte stageType) {
        return role.getReviveNum(stageType);
    }

    /**
     * 更新已复活次数
     *
     * @param stageType
     */
    public void updateReviveNum(byte stageType) {
        role.addReviveNum(stageType);
        context().update(role);
        send(new ClientRole(ClientRole.UPDATE_REVIVE_NUM, role));
    }

    /**
     * 保存自动战斗标记
     */
    public void setAutoFightFlag(byte flag, byte stageType) {
        setByte(ROLE_AUTO_FLAG + stageType, flag);
    }

    public byte getAutoFightFlag(byte stageType) {
        return getByte(ROLE_AUTO_FLAG + stageType);
    }



    @Override
    public void onLog() {
    }

    public int getRideLevelId() {
        return role.getRideLevelId();
    }

    public void updateRideLevelId(int id) {
        role.setRideLevelId(id);
        context().update(role);
    }

    /**
     * 获取创角天数
     *
     * @return
     */
    public int getRoleCreatedDays() {
        Date createDate = DateUtil.toDate(role.getCreateTime());
        if (createDate == null) return 0;
        /** 按自然日算 */
        Date curDate = new Date(System.currentTimeMillis());
        int day = DateUtil.getRelativeDifferDays(createDate, curDate) + 1;    // 创角算第一天
        return day;
    }

    /**
     * 获得创角时间点
     *
     * @return
     */
    public long getRoleCreatedTime() {
        return role.getCreateTime();
    }

    /**
     * 运营gm减少角色资源
     *
     * @param event
     */
    public void gmReduceHandler(ReduceRoleResourceEvent event) {
    }

    /**
     * 运营gm修改角色等级
     *
     * @param value
     */
    public void gmModifyLevelHandler(int value) {
        int result = Math.min(RoleManager.getMaxlvlByJobId(role.getJobId()), Math.max(0, role.getLevel() + value));
        if (result <= 0) {
            result = 1;
        }
        role.setLevel(result);
        context().update(role);
        updateGradeBaseAttr();
        sendRoleAttr();
        sendUpdateFightScore(Boolean.FALSE);

        eventDispatcher().fire(new RoleLevelUpEvent(result, result));
        eventDispatcher().fire(new RoleLevelAchieveEvent(result, result));
    }

    public void heartBeatCheck() {
        if (heartBeatCheckData.size() > 5) {
            heartBeatCheckData.clear();
        }
        long now = System.currentTimeMillis();
        if (now - lastSendHeartBeatCheckTime > HeartBeatCheckDisTime) {
            lastSendHeartBeatCheckTime = now;
            String key = StringUtil.getRandomString(5);
            ClientHeartBeatCheck cBeatCheck = new ClientHeartBeatCheck(key, HeartBeatReplyTime);
            send(cBeatCheck);
            heartBeatCheckData.put(key, now + HeartBeatReplyTime);
        }
    }

    public void heartBeatCheckReq(String key) {
        if (!heartBeatCheckData.containsKey(key)) {
            return;
        }
        long time = heartBeatCheckData.remove(key);//心跳合理返回时间
        long now = System.currentTimeMillis();
        if (now + 2000 < time) {//2s的误差
            //加速行为

            LoginModule lModule = module(MConst.Login);

            send(new ClientText("检测到您有加速行为，将禁止您继续游戏"));
            ClientReconnect client = new ClientReconnect(false);
            client.setReason((byte) 2);//被踢下线
            send(client);
        }
    }

    public void roleAttrCheckReq(Attribute attr) {
        if (attr == null || attr.getNotZeroAttrNum() == 0) {
            return;
        }
        Attribute curAttr = role.getTotalAttr();
        for (int i = 0; i < attr.getAttributes().length; i++) {
            String attrName = Attr.getAttrNameByIndex(i);
            int ServerValue = curAttr.get(attrName);
            int ClientValue = attr.get(attrName);
            if (ClientValue > (ServerValue * 2)) { //误差
//                LogUtil.info("===========战斗属性异常   属性："+attrName);
//                LogUtil.info("===========战斗属性异常   服务端value："+ServerValue);
//                LogUtil.info("===========战斗属性异常   客户端value："+ClientValue);
                //客户端内存被修改
                LoginModule lModule = module(MConst.Login);
                long now = System.currentTimeMillis();
                send(new ClientText("检测到您有修改客户端内存的行为，将禁止您继续游戏"));
                ClientReconnect client = new ClientReconnect(false);
                client.setReason((byte) 2);//被踢下线
                send(client);
                break;
            }
        }
    }

    public void onChangeJob(int newJobId) {
        RoleModule roleModule = module(MConst.Role);
        LoginModule loginModule = module(MConst.Login);
        Role roleRow = roleModule.getRoleRow();
        roleRow.setJobId(newJobId);
        roleModule.context().update(roleRow);
        try {
            AccountRow accountRow = loginModule.getAccountRow();
            List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
            for (AccountRole accountRole : relativeRoleList) {
                if (accountRole.getRoleId().equals(roleModule.id() + "")) {
                    accountRole.roleJobId = newJobId;
                }
            }
            updateRoleSummaryComp();
        } catch (SQLException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
    }

    public int getBabyStage() {
        return role.getBabyStage();
    }

    public int getBabyLevel() {
        return role.getBabyLevel();
    }

    public int getBabyEnergy() {
        return role.getBabyEnergy();
    }
}
