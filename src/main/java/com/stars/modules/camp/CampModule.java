package com.stars.modules.camp;

import com.stars.bootstrap.ServerManager;
import com.stars.core.SystemRecordMap;
import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.activity.imp.QiChuDaZuoZhanActivity;
import com.stars.modules.camp.activity.imp.QiChuZhiZhengActivity;
import com.stars.modules.camp.event.*;
import com.stars.modules.camp.packet.*;
import com.stars.modules.camp.pojo.CampEquilibrium;
import com.stars.modules.camp.prodata.*;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.CampUtils;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.multiserver.camp.usrdata.RareOfficerRolePo;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.userdata.CampRoleReputationRankPo;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class CampModule extends AbstractModule {
    private RoleCampPo roleCamp = null;
    private RoleCampTimesPo roleCampTimes = null;
    List<CampMissionVo> campMissionVoList = null;
    private Map<Integer, CampActivity> campActivityMap = new HashMap<>();
    public static final String REDPOINT_QI_CHU_DA_ZUO_ZHAN = "camp.fight.dazuozhan.dailyredpoint";

    public CampModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
        campActivityMap.put(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG, new QiChuZhiZhengActivity(name, id, self, eventDispatcher, moduleMap, roleCamp, roleCampTimes));
        campActivityMap.put(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN, new QiChuDaZuoZhanActivity(name, id, self, eventDispatcher, moduleMap, roleCamp, roleCampTimes));
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from rolecamp where roleid=%s;";
        roleCamp = DBUtil.queryBean(DBUtil.DB_USER, RoleCampPo.class, String.format(sql, id()));
        sql = "select * from rolecamptimes where roleid=%s;";
        roleCampTimes = DBUtil.queryBean(DBUtil.DB_USER, RoleCampTimesPo.class, String.format(sql, id()));
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        for (CampActivity campActivity : campActivityMap.values()) {
            Module module = (Module) campActivity;
            module.onDailyReset(now, isLogin);
        }
        if (roleCampTimes != null) {
            roleCampTimes.reset();
            context().update(roleCampTimes);
        }
        signCalRedPoint(MConst.Camp, RedPointConst.CAMP_DAILY_REWARD); // 俸禄红点
        signCalRedPoint(MConst.Camp, RedPointConst.CAMP_GET_AWARD);

    }

    @Override
    public void onTimingExecute() {
        for (CampActivity campActivity : campActivityMap.values()) {
            Module module = (Module) campActivity;
            module.onTimingExecute();
        }

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleCampTimes == null) {
            roleCampTimes = new RoleCampTimesPo(id(), "", "");
            context().insert(roleCampTimes);
        }
        if (roleCamp != null) {
            refreshMissionList();
            checkAndResetReputation();
            queryMyRareOfficer();
            caculateAttr();
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_UPGRADE_OFFICER);
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_MISSION);
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_DAILY_REWARD); // 俸禄红点
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_GET_AWARD);
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN_REWARD);
        }
    }


    /**
     * 检测重置声望
     */
    public void checkAndResetReputation() {
        long lastReset = getLong(CampManager.campReputationResetTimestamp, 0);
        if (SystemRecordMap.campReputationResetTimestamp > lastReset) {
            setLong(CampManager.campReputationResetTimestamp, SystemRecordMap.campReputationResetTimestamp);
            ToolModule toolModule = module(MConst.Tool);
            long count = toolModule.getCountByItemId(ToolManager.REPUTATION);
            toolModule.deleteAndSend(ToolManager.REPUTATION, (int) count, EventType.CAMP_REPUTATION_RESET.getCode());
        }
    }

    /**
     * 计算阵营模块提供的属性加成
     */
    private void caculateAttr() {
        RoleModule roleModule = module(MConst.Role);
        int commonOfficerId = roleCamp.getCommonOfficerId();
        int rareOfficerId = roleCamp.getRareOfficerId();
        int designateOfficerId = roleCamp.getDesignateOfficerId();
        Attribute attribute = new Attribute();
        Attribute commonAttribute = new Attribute(CampManager.commonOfficerMap.get(commonOfficerId).getAttr());
        if (rareOfficerId != 0) {
            Attribute rareAttribute = new Attribute(CampManager.rareOfficerMap.get(rareOfficerId).getAttr());
            attribute.addAttribute(rareAttribute);
        }
        if (designateOfficerId != 0) {
            Attribute designateAttribute = new Attribute(CampManager.designateOfficerMap.get(designateOfficerId).getAttr());
            attribute.addAttribute(designateAttribute);
        }
        attribute.addAttribute(commonAttribute);
        roleModule.updatePartAttr(RoleManager.ROLEATTR_CAMP, attribute);
        // 更新战力;
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_CAMP, FormularUtils.calFightScore(attribute));
    }


    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.campCityFightService().removeMember(id());
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (roleCamp == null) {
            return;
        }
        // 官职
        if (redPointIds.contains(RedPointConst.CAMP_UPGRADE_OFFICER)) {
            RoleModule roleModule = module(MConst.Role);
            Role roleRow = roleModule.getRoleRow();
            CommonOfficerVo commonOfficer = roleCamp.getCommonOfficer();
            AllServerCampPo allServerCamp = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
            CommonOfficerVo nextLevelCommonOfficerVo = commonOfficer.getNextLevelCommonOfficerVo();
            if (nextLevelCommonOfficerVo != null && commonOfficer.getReqlevel() <= roleRow.getFeats()
                    && commonOfficer.getCamplevel() <= allServerCamp.getLevel()) {
                redPointMap.put(RedPointConst.CAMP_UPGRADE_OFFICER, "");
            } else {
                redPointMap.put(RedPointConst.CAMP_UPGRADE_OFFICER, null);
            }
        }
        // 任务
        if (redPointIds.contains(RedPointConst.CAMP_MISSION)) {
            AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
            RoleModule roleModule = module(MConst.Role);
            boolean needRedPoint = false;
            for (CampMissionVo campMissionVo : campMissionVoList) {
                if (campMissionVo.canJoin(roleCamp, allServerCampPo, roleModule.getRoleRow())) {
                    if (roleCampTimes.getJoinTimesByMisId(campMissionVo.getId()) < campMissionVo.getTargetTime()) {
                        needRedPoint = true;
                        break;
                    }
                }
            }
            if (needRedPoint) {
                redPointMap.put(RedPointConst.CAMP_MISSION, "");
            } else {
                redPointMap.put(RedPointConst.CAMP_MISSION, null);
            }
        }
        // 俸禄
        if (redPointIds.contains(RedPointConst.CAMP_DAILY_REWARD)) {
            if (roleCampTimes.getDailyRewardTimes() == 0) {
                redPointMap.put(RedPointConst.CAMP_DAILY_REWARD, "");
            } else {
                redPointMap.put(RedPointConst.CAMP_DAILY_REWARD, null);
            }
        }
        if (redPointIds.contains(RedPointConst.CAMP_GET_AWARD)) {
            if (!roleCampTimes.getCanGetAwardSet().isEmpty()) {
                redPointMap.put(RedPointConst.CAMP_GET_AWARD, roleCampTimes.getCanGetAward());
            } else {
                redPointMap.put(RedPointConst.CAMP_GET_AWARD, null);
            }
        }
        if (redPointIds.contains(RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN)) {
            int qiChuDaZuoZhanFlag = getInt(REDPOINT_QI_CHU_DA_ZUO_ZHAN, 0);
            if (qiChuDaZuoZhanFlag == 0) {
                redPointMap.put(RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN, "");
            } else {
                redPointMap.put(RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN, null);
            }
            setInt(REDPOINT_QI_CHU_DA_ZUO_ZHAN, 1);
        }
        if (redPointIds.contains(RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN_REWARD)) {
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : CampManager.campActivity2ScoreMap.entrySet()) {
                if (roleCampTimes.getCampFightScore() >= entry.getKey() && getRoleCampTimes().canTakeScoreReward(entry.getKey())) {
                    redPointMap.put(RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN_REWARD, "2");
                    return;
                }
            }
            redPointMap.put(RedPointConst.CAMP_QI_CHU_DA_ZUO_ZHAN_REWARD, null);
        }
    }

    /**
     * 下发主界面数据
     */
    public void reqMyCamp() {
        ClientCampPacket clientCampPacket = new ClientCampPacket(ClientCampPacket.SEND_MY_CAMP_INFO);
        AllServerCampPo allServerCamp = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
        clientCampPacket.setRoleCampPo(roleCamp);
        clientCampPacket.setAllServerCamp(allServerCamp);
        send(clientCampPacket);

    }


    /**
     * 加入指定阵营
     *
     * @param campType
     */
    public void joinCamp(Integer campType) {
        RoleModule roleModule = module(MConst.Role);
        ToolModule toolModule = module(MConst.Tool);
        if (roleCamp != null) {
            warn("已经加入阵营不可重复加入");
            return;
        }
        int allCampRoleNum = ServiceHelper.campLocalMainService().getAllCampRoleNum();
        CampTypeScale campTypeScale = ServiceHelper.campLocalMainService().getCampTypeScale();
        if (campTypeScale == null) {
            warn("请检查阵营服是否成功启动");
            return;
        }
        if (allCampRoleNum >= CampManager.campEquilibriumnum) {
            if (campTypeScale.getScale() >= CampManager.campAddLimitNum) {
                if (campType != null && campType != campTypeScale.getLowCampType()) {
                    warn("camp_desc_fullnum");
                    return;
                }
            }
        }
        if (campType == null) {
            campType = ServiceHelper.campLocalMainService().getRandomCampType();
            /**
             * 随机进入发奖
             */
            toolModule.addAndSend(CampManager.randomEnterReward, EventType.CAMP_ENTER_LOW.getCode());
            ClientAward clientAward = new ClientAward(CampManager.randomEnterReward);
            send(clientAward);
        }
        CampEquilibrium campEquilibrium = campTypeScale.getCampEquilibrium();
        if (campEquilibrium != null) {
            /**
             * 进入人数少阵营发奖
             */
            if (campTypeScale.getLowCampType() == campType) {
                toolModule.addAndSend(campEquilibrium.getReward(), EventType.CAMP_ENTER_LOW.getCode());
                ClientAward clientAward = new ClientAward(campEquilibrium.getReward());
                send(clientAward);
            }
        }
        roleCamp = new RoleCampPo();
        roleCamp.setRoleId(id());
        roleCamp.setCampType(campType);
        CommonOfficerVo commonOfficerVo = CampManager.commonOfficerVoList.get(0);
        roleCamp.setCommonOfficerId(commonOfficerVo.getId());
        context().insert(roleCamp);
        /**
         * 加入默认城池
         */
        AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(campType);
        List<CampCityVo> openedCampCityList = allServerCampPo.getOpenedCampCityList();
        CampCityVo campCityVo = openedCampCityList.get(0);
        joinCity(campCityVo.getId());
        OfficerChangeEvent officeChangeEvent = new OfficerChangeEvent();
        officeChangeEvent.setCommonOfficerId(roleCamp.getCommonOfficerId());
        eventDispatcher().fire(officeChangeEvent);
        ServiceHelper.campLocalMainService().joinCamp(id(), campType);
        CampAtrVo campAtrVo = CampManager.campAtrMap.get(campType);
        String campName = DataManager.getGametext(campAtrVo.getName());
        warn("camp_tips_ensure", campName);
        com.stars.util.LogUtil.info("{}加入阵营:camptype:{}", id(), campType);
        ClientCampPacket clientCampPacket = new ClientCampPacket(ClientCampPacket.SEND_JOIN_SUCCESS);
        send(clientCampPacket);
        /**
         * 更新角色基本信息
         */
        ClientRole clientRole = new ClientRole(ClientRole.UPDATE_BASE, roleModule.getRoleRow());
        clientRole.setRoleCampPo(roleCamp);
        send(clientRole);
        refreshMissionList();
    }

    /**
     * 随机加入阵营
     */
    public void randomJoinCamp() {
        joinCamp(null);
    }


    /**
     * 阵营升级
     *
     * @param campLevelUpEvent
     */
    public void handleCampLevelUp(CampLevelUpEvent campLevelUpEvent) {

    }


    /**
     * 下发所有阵营开放了的城池
     */
    public void reqOpenedCampCities() {
        ClientCampCityPacket clientCampCityPacket = new ClientCampCityPacket(ClientCampCityPacket.SEND_OPENED_CAMP_CITIES);
        Map<Integer, AllServerCampPo> allServerCampMap = ServiceHelper.campLocalMainService().getAllServerCampMap();
        CampTypeScale campTypeScale = ServiceHelper.campLocalMainService().getCampTypeScale();
        clientCampCityPacket.setAllServerCampMap(allServerCampMap);
        clientCampCityPacket.setCampTypeScale(campTypeScale);
        clientCampCityPacket.setRoleCampPo(roleCamp);
        send(clientCampCityPacket);
    }

    /**
     * 刷新下发官职属性
     */
    public void refreshAndSendOfficerAttr() {
        caculateAttr();
        RoleModule roleModule = module(MConst.Role);
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore();
        ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_MY_CAMP_OFFICER);
        clientOfficerPacket.setRoleCampPo(roleCamp);
        send(clientOfficerPacket);
    }

    /**
     * 用功勋升级官职
     */
    public void reqUpgradeOfficer() {
        ToolModule toolModule = module(MConst.Tool);
        RoleModule roleModule = module(MConst.Role);
        Role roleRow = roleModule.getRoleRow();
        int commonOfficerId = roleCamp.getCommonOfficerId();
        CommonOfficerVo commonOfficerVo = CampManager.commonOfficerMap.get(commonOfficerId);
        AllServerCampPo allServerCamp = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
        CommonOfficerVo nextLevelCommonOfficerVo = commonOfficerVo.getNextLevelCommonOfficerVo();
        if (nextLevelCommonOfficerVo == null) {
            warn("官职已达满级无法升级");
            return;
        }
        if (commonOfficerVo.getReqlevel() <= roleRow.getFeats()) {
            if (commonOfficerVo.getCamplevel() <= allServerCamp.getLevel()) {
                toolModule.deleteAndSend(ToolManager.FEATS, commonOfficerVo.getReqlevel(), EventType.CAMP_UPGRADE_OFFICER.getCode());
                roleCamp.setCommonOfficerId(nextLevelCommonOfficerVo.getId());
                CampCityVo campCity = roleCamp.getCampCity();
                if (!campCity.canJoin(nextLevelCommonOfficerVo.getLevel())) {
                    CampCityVo nextLevelCity = campCity.getNextLevelCity();
                    if (joinCity(nextLevelCity.getId())) {
                        ServiceHelper.emailService().sendToSingle(id(), 10500, 0L, "系统", null, DataManager.getGametext(commonOfficerVo.getName()), DataManager.getGametext(nextLevelCommonOfficerVo.getName()), DataManager.getGametext(campCity.getName()), DataManager.getGametext(nextLevelCity.getName()));
                    }
                }
                context().update(roleCamp);
                ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_OFFICER_UPGRADE_SUCCESS);
                send(clientOfficerPacket);
                OfficerChangeEvent officeChangeEvent = new OfficerChangeEvent();
                officeChangeEvent.setCommonOfficerId(roleCamp.getCommonOfficerId());
                eventDispatcher().fire(officeChangeEvent);
                reqOfficerUpgradeUI();
                signCalRedPoint(MConst.Camp, RedPointConst.CAMP_UPGRADE_OFFICER);
            } else {
                warn("已经达到阵营等级的上限无法升级");
            }
        } else {
            warn("功勋不足，无法升级");
        }


    }


    /**
     * 请求我的官职数据
     */
    public void reqMyCampAndOfficers() {
        ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_MY_CAMP_OFFICER);
        clientOfficerPacket.setRoleCampPo(roleCamp);
        send(clientOfficerPacket);
    }

    /**
     * 领取每日俸禄
     */
    public void takeDailyReward() {
        if (roleCampTimes.getDailyRewardTimes() == 0) {
            int commonOfficerId = roleCamp.getCommonOfficerId();
            CommonOfficerVo commonOfficerVo = CampManager.commonOfficerMap.get(commonOfficerId);
            String dayaward = commonOfficerVo.getDayaward();
            Map<Integer, Integer> dayReward = StringUtil.toMap(dayaward, Integer.class, Integer.class, '+', '|');
            int rareOfficerId = roleCamp.getRareOfficerId();
            RareOfficerVo rareOfficerVo = CampManager.rareOfficerMap.get(rareOfficerId);
            if (rareOfficerVo != null) {
                String rareDailyReward = rareOfficerVo.getDayaward();
                Map<Integer, Integer> rareDayReward = StringUtil.toMap(rareDailyReward, Integer.class, Integer.class, '+', '|');
                MapUtil.add(dayReward, rareDayReward);
            }
            CampUtils.addExtReward(CampUtils.TYPE_DAILY_REWARD, getRoleCamp().getCampType(), dayReward);
            ToolModule toolModule = module(MConst.Tool);
            toolModule.addAndSend(dayReward, EventType.CAMP_TAKE_DAILY_OFFICER_REWARD.getCode());
            ClientAward clientAward = new ClientAward(dayReward);
            clientAward.setType((byte) 1);
            send(clientAward);
            roleCampTimes.setDailyRewardTimes(1);
            context().update(roleCampTimes);
            ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_TAKE_REWARD_STATE);
            clientOfficerPacket.setRoleCampTimes(roleCampTimes);
            send(clientOfficerPacket);
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_DAILY_REWARD);
        } else {
            warn("camp_tips_getcash");
        }
    }


    /**
     * 更新个人声望排名
     */
    public void updateReputation() {
        if (roleCamp == null) {
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        Role roleRow = roleModule.getRoleRow();
        int reputation = roleRow.getReputation();
        CampRoleReputationRankPo newRoleReputationRankPo = new CampRoleReputationRankPo(id(), roleRow.getName(), roleRow.getJobId(), roleCamp.getCampType(), roleCamp.getCityId(), reputation, roleModule.getFightScore(), roleCamp.getCommonOfficerId(), roleCamp.getRareOfficerId(), roleCamp.getDesignateOfficerId());
        ServiceHelper.campLocalMainService().updateReputation(newRoleReputationRankPo);
    }

    /**
     * 添加繁荣度
     *
     * @param properous
     */
    public void handleProperousAdd(int properous) {
        if (roleCamp != null) {
            ServiceHelper.campLocalMainService().donateCampProsperous(id(), roleCamp.getCampType(), properous);
        }
        ToolModule toolModule = module(MConst.Tool);
        toolModule.deleteAndSend(ToolManager.PROSPEROUS, properous, EventType.CAMP_DONATE_PROSPEROUS.getCode());
    }

    /**
     * 稀有官职下发时，查询自己是否上榜
     */
    public void queryMyRareOfficer() {
        if (roleCamp == null) {
            return;
        }
        int oldRareOfficerId = roleCamp.getRareOfficerId();
        RareOfficerRolePo officerRole = ServiceHelper.campLocalMainService().getAllCityRareOfficerByRoleId(id());
        if (officerRole != null) {
            roleCamp.setRareOfficerId(officerRole.getRareOfficerId());
            if (officerRole.getRareOfficerId() > oldRareOfficerId) {
                ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_RARE_OFFICER_NOTICE);
                clientOfficerPacket.setRoleCampPo(roleCamp);
                send(clientOfficerPacket);
                refreshAndSendOfficerAttr();
            }
            OfficerChangeEvent officeChangeEvent = new OfficerChangeEvent();
            officeChangeEvent.setRareOfficerId(officerRole.getRareOfficerId());
            eventDispatcher().fire(officeChangeEvent);
            refreshMissionList();
        } else {
            roleCamp.setRareOfficerId(0);
        }
        context().update(roleCamp);
    }

    /**
     * 请求指定城池的稀有官职列表
     *
     * @param cityId
     */
    public void reqRareOfficerListByCityId(int cityId) {
        List<RareOfficerRolePo> rareOfficerRoleList = ServiceHelper.campLocalMainService().getRareOfficerListByCityId(cityId);
        Collections.sort(rareOfficerRoleList);
        ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_THE_CITY_RARE_OFFICER);
        clientOfficerPacket.setRareOfficerRoleList(rareOfficerRoleList);
        clientOfficerPacket.setTheCityId(cityId);
        send(clientOfficerPacket);
    }

    /**
     * 请求当前的阵营负载情况
     */
    public void reqCurrentCampLoad() {
        CampTypeScale campTypeScale = ServiceHelper.campLocalMainService().getCampTypeScale();
        ClientCampPacket clientCampPacket = new ClientCampPacket(ClientCampPacket.SEND_CURRENT_CAMP_INFO);
        clientCampPacket.setCampTypeScale(campTypeScale);
        Map<Integer, AllServerCampPo> allServerCampMap = ServiceHelper.campLocalMainService().getAllServerCampMap();
        clientCampPacket.setAllServerCampPoMap(allServerCampMap);
        send(clientCampPacket);

    }

    /**
     * 请求入驻指定城池
     *
     * @param cityId
     */
    public boolean reqJoinCity(int cityId) {
        if (roleCamp.getCityId() < cityId) {
            warn("禁止入驻低等级城池");
            return false;
        }
        AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
        boolean contains = allServerCampPo.getOpenCityIdList().contains(cityId);
        if (contains) {
            CampCityVo campCityVo = CampManager.campCityMap.get(cityId);
            boolean canJoin = campCityVo.canJoin(roleCamp.getCommonOfficer().getLevel());
            if (canJoin) {
                int oldCityId = roleCamp.getCityId();
                roleCamp.setCityId(cityId);
                context().update(roleCamp);
                CampPlayerImageData data = new CampPlayerImageData();
                RoleModule roleModule = module(MConst.Role);
                data.setJob(roleModule.getRoleRow().getJobId());
                data.setCityId(cityId);
                data.setCommonOfficerId(roleCamp.getCommonOfficerId());
                data.setRareOfficerId(roleCamp.getRareOfficerId());
                data.setDesignateOfficerId(roleCamp.getDesignateOfficerId());
                data.setServerId(MultiServerHelper.getDisplayServerId());
                FighterEntity entity = FighterCreator.createSelf(moduleMap(), FighterEntity.CAMP_ENEMY);
                data.setEntity(entity);
                eventDispatcher().fire(new CampCityChangeEvent(id(), com.stars.bootstrap.ServerManager.getServer().getConfig().getServerId(), oldCityId, cityId, data));
                ClientCampCityPacket clientCampCityPacket = new ClientCampCityPacket(ClientCampCityPacket.SEND_JOIN_CITY_SUCCESS);
                send(clientCampCityPacket);
                return true;
            } else {
                warn("官职等级不符合当前城池");
            }
        } else {
            warn("无法入驻未开放城池");
        }
        return false;
    }

    /**
     * 入驻指定城池(内部使用，无通知)
     *
     * @param cityId
     */
    public boolean joinCity(int cityId) {
        if (roleCamp.getCityId() > cityId) {
            warn("禁止入驻低等级城池");
            return false;
        }
        AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
        boolean contains = allServerCampPo.getOpenCityIdList().contains(cityId);
        if (contains) {
            CampCityVo campCityVo = CampManager.campCityMap.get(cityId);
            boolean canJoin = campCityVo.canJoin(roleCamp.getCommonOfficer().getLevel());
            if (canJoin) {
                int oldCityId = roleCamp.getCityId();
                roleCamp.setCityId(cityId);
                context().update(roleCamp);
                CampPlayerImageData data = new CampPlayerImageData();
                RoleModule roleModule = module(MConst.Role);
                data.setJob(roleModule.getRoleRow().getJobId());
                data.setCityId(cityId);
                data.setCommonOfficerId(roleCamp.getCommonOfficerId());
                data.setRareOfficerId(roleCamp.getRareOfficerId());
                data.setDesignateOfficerId(roleCamp.getDesignateOfficerId());
                data.setServerId(MultiServerHelper.getDisplayServerId());
                FighterEntity entity = FighterCreator.createSelf(moduleMap(), FighterEntity.CAMP_ENEMY);
                data.setEntity(entity);
                eventDispatcher().fire(new CampCityChangeEvent(id(), com.stars.bootstrap.ServerManager.getServer().getConfig().getServerId(), oldCityId, cityId, data));
                return true;
            }
        }
        return false;
    }

    /**
     * 入驻城池改变
     *
     * @param campCityChangeEvent
     */
    public void onChangeCity(CampCityChangeEvent campCityChangeEvent) {
        ServiceHelper.campLocalMainService().handleChangeCity(campCityChangeEvent);
    }

    /**
     * 下发指定城池的排行榜数据
     *
     * @param cityId
     */
    public void sendTheCityRank(int cityId) {
        RoleModule roleModule = module(MConst.Role);
        Role roleRow = roleModule.getRoleRow();
        int reputation = roleRow.getReputation();
        boolean hasOwn = false;
        List<CampRoleReputationRankPo> reputationRankPoList = ServiceHelper.campLocalMainService().getRankListByCityId(cityId);
        List<CampRoleReputationRankPo> reputationRankPoListTmp = new ArrayList<>(reputationRankPoList);
        CampRoleReputationRankPo reputationRankPo = ServiceHelper.campLocalMainService().getRankByCityId(roleCamp.getCityId(), id());
        if (roleCamp.getCityId() == cityId) {
            if (reputationRankPoList.size() > 0) {
                if (reputationRankPo == null) {
                    reputationRankPo = new CampRoleReputationRankPo(id(), com.stars.bootstrap.ServerManager.getServer().getConfig().getServerId(), roleRow.getName(), roleRow.getJobId(), roleCamp.getCampType(), roleCamp.getCityId(), reputation, roleModule.getFightScore(), roleCamp.getCommonOfficerId(), roleCamp.getRareOfficerId(), roleCamp.getDesignateOfficerId());
                    reputationRankPo.setRank(999);
                }
                reputationRankPoListTmp.add(reputationRankPo);
                hasOwn = true;
            }
        }
        ClientCampCityPacket clientCampCityPacket = new ClientCampCityPacket(ClientCampCityPacket.SEND_THE_CITY_RANK);
        clientCampCityPacket.setReputationRankPoList(reputationRankPoListTmp);
        clientCampCityPacket.setHasOwn(hasOwn);
        send(clientCampCityPacket);
    }

    public RoleCampPo getRoleCamp() {
        return roleCamp;
    }

    public RoleCampTimesPo getRoleCampTimes() {
        return roleCampTimes;
    }

    public int getCampType() {
        int campType = 0;
        if (roleCamp != null) {
            campType = roleCamp.getCampType();
        }
        return campType;
    }

    /**
     * 下发所有阵营产品数据
     */
    public void reqAllCampInfo() {
        ClientCampPacket clientCampPacket = new ClientCampPacket(ClientCampPacket.SEND_ALL_CAMP_INFO);
        send(clientCampPacket);
    }

    /**
     * 请求我的阵营状态
     */
    public void reqMyCampState() {
        ClientCampPacket clientCampPacket = new ClientCampPacket(ClientCampPacket.SEND_MY_CAMP_STATE);
        clientCampPacket.setRoleCampPo(roleCamp);
        send(clientCampPacket);

    }

    /**
     * 官职一览数据
     */
    public void reqMainOfficerUI() {
        ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_MAIN_OFFICER_UI);
        RoleModule roleModule = module(MConst.Role);
        Role roleRow = roleModule.getRoleRow();
        int featsNum = roleRow.getFeats();
        clientOfficerPacket.setFeatsNum(featsNum);
        clientOfficerPacket.setReputationNum(roleRow.getReputation());
        Integer fightScore = roleModule.getRoleRow().getFightScoreMap().get(RoleManager.FIGHTSCORE_CAMP);
        clientOfficerPacket.setRoleCampPo(roleCamp);
        clientOfficerPacket.setFightScore(fightScore);
        clientOfficerPacket.setRoleCampTimes(roleCampTimes);
        send(clientOfficerPacket);
    }

    /**
     * 打开官职升级界面
     */
    public void reqOfficerUpgradeUI() {
        RoleModule roleModule = module(MConst.Role);
        Role roleRow = roleModule.getRoleRow();
        ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_OFFICER_UPGRADE_UI);
        clientOfficerPacket.setRoleCampPo(roleCamp);
        clientOfficerPacket.setFeatsNum(roleRow.getFeats());
        send(clientOfficerPacket);
    }

    /**
     * 请求 阵营活动列表
     * 3:已开启未完成>
     * 2:未开启
     * 1:>已完成
     */
    public void reqActivityList() {
        List<CampActivityVo> openedCampAcitivities = new ArrayList<>();
        AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
        for (CampActivityVo campActivityVo : CampManager.campActivityList) {
            if (campActivityVo.isOpen()) {
                CampActivityVo clone = null;
                try {
                    clone = (CampActivityVo) campActivityVo.clone();
                } catch (CloneNotSupportedException e) {
                    com.stars.util.LogUtil.error(e.getMessage(), e);
                }
                if (clone.canJoinTime(roleCampTimes) &&
                        clone.canJoin(roleCamp, allServerCampPo)) {
                    clone.setSort(3);
                } else if (!clone.canJoin(roleCamp, allServerCampPo)) {
                    clone.setSort(2);
                } else if (!clone.canJoinTime(roleCampTimes)) {
                    clone.setSort(1);
                }
                openedCampAcitivities.add(clone);
            }
        }
        Collections.sort(openedCampAcitivities);
        ClientCampActivityPacket clientCampActivityPacket = new ClientCampActivityPacket(ClientCampActivityPacket.SEND_OPENED_ACTIVITY_LIST);
        clientCampActivityPacket.setCampActivityVoList(openedCampAcitivities);
        clientCampActivityPacket.setRoleCampTimes(roleCampTimes);
        send(clientCampActivityPacket);
    }


    /**
     * 刷新任务列表
     */
    public void refreshMissionList() {
        campMissionVoList = new ArrayList<>();
        AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(roleCamp.getCampType());
        if (allServerCampPo == null) {
            com.stars.util.LogUtil.error("请检查是否阵营服没有连接成功");
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        for (CampMissionVo campMissionVo : CampManager.campMissionList) {
            CampMissionVo clone = null;
            try {
                clone = (CampMissionVo) campMissionVo.clone();
            } catch (CloneNotSupportedException e) {
                com.stars.util.LogUtil.error(e.getMessage(), e);
            }
            if (clone.canJoin(roleCamp, allServerCampPo, roleModule.getRoleRow())) {
                campMissionVoList.add(clone);
            }
        }
    }

    /**
     * 请求任务列表
     */
    public void reqMissionList() {
        ClientCampMissionPacket clientCampMissionPacket = new ClientCampMissionPacket(ClientCampMissionPacket.SEND_OPENED_MISSION_LIST);
        clientCampMissionPacket.setCampMissionVoList(campMissionVoList);
        clientCampMissionPacket.setRoleCampTimes(roleCampTimes);
        send(clientCampMissionPacket);
    }

    /**
     * 检测和修改任务状态(发奖)
     *
     * @param type
     * @param targetId
     */
    public void checkMissionState(int type, int targetId) {
        if (campMissionVoList == null) {
            LogUtil.error("阵营|检查任务状态|异常|id:{}|type:{}|stageId:{}", id(), type, targetId);
            return;
        }
        for (CampMissionVo campMissionVo : campMissionVoList) {
            if (campMissionVo.getType() == type) {
                if (!campMissionVo.isComplete(roleCampTimes)) {
                    if (campMissionVo.getTargetId() == targetId) {
                        roleCampTimes.addMissionJoinTimes(campMissionVo.getId());
                        Integer times = roleCampTimes.getJoinTimesByMisId(campMissionVo.getId());
                        if (times == campMissionVo.getTargetTime()) {
                            roleCampTimes.addAwardNotGet(campMissionVo.getId());
                            refreshMissionList();
                            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_MISSION);
                            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_GET_AWARD);
                        }
                        context().update(roleCampTimes);
                    }
                }
            }
        }
    }


    /**
     * 转职时触发
     *
     * @param event
     */
    public void onChangeJob(ChangeJobEvent event) {
        if (roleCamp == null) {
            return;
        }
        RoleInfoChangeEvent roleInfoChangeEvent = new RoleInfoChangeEvent();
        roleInfoChangeEvent.setRoleId(id());
        roleInfoChangeEvent.setCityId(roleCamp.getCityId());
        roleInfoChangeEvent.setNewJobId(event.getNewJobId());
        ServiceHelper.campLocalMainService().handleRoleInfoChange(roleInfoChangeEvent);
    }

    /**
     * 改名时触发
     *
     * @param event
     */
    public void onReName(RoleRenameEvent event) {
        if (roleCamp == null) {
            return;
        }
        RoleInfoChangeEvent roleInfoChangeEvent = new RoleInfoChangeEvent();
        roleInfoChangeEvent.setRoleId(id());
        roleInfoChangeEvent.setCityId(roleCamp.getCityId());
        roleInfoChangeEvent.setNewName(event.getNewName());
        ServiceHelper.campLocalMainService().handleRoleInfoChange(roleInfoChangeEvent);
    }

    /**
     * 执行阵营服上的gm
     */
    public void remoteGm() {
        final Properties campProperties = ServerManager.getServer().getConfig().getProps().get("camp");
        String serverIdStr = campProperties.getProperty("serverId");
        int campServerId = Integer.parseInt(serverIdStr);
        MainRpcHelper.campRemoteMainService().remoteGm(campServerId);
    }

    /**
     * 活动结束触发
     * type:1表示活动，2表示任务
     *
     * @param event
     */
    public void onActivityFinish(ActivityFinishEvent event) {
        int activityId = event.getActivityId();
        checkMissionState(1, activityId);
        ToolModule toolModule = module(MConst.Tool);
        switch (activityId) {
            case CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG: {
                toolModule.addAndSend(event.getReward(), EventType.CAMP_CITY_FIGHT.getCode());
            }
            break;
            case CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN: {
                toolModule.addAndSend(event.getReward(), EventType.CAMP_SINGLE_SCORE_REWARD.getCode());
                getRoleCampTimes().addTakeSingleRewardTime();
                context().update(getRoleCampTimes());
            }
            break;
        }
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.log_camp_activty_mission(1, activityId, event.getReward());
    }

    /**
     * 任务结束触发
     * type:1表示活动，2表示任务
     *
     * @param event
     */
    public void onMissionFinish(MissionFinishEvent event) {
        int missionId = event.getMissionId();
        for (CampMissionVo campMissionVo : campMissionVoList) {
            if (campMissionVo.getId() == event.getMissionId()) {
                if (!campMissionVo.isComplete(roleCampTimes)) {
                    roleCampTimes.addMissionJoinTimes(campMissionVo.getId());
                    Integer times = roleCampTimes.getJoinTimesByMisId(campMissionVo.getId());
                    if (times == campMissionVo.getTargetTime()) {
                        refreshMissionList();
                        roleCampTimes.addAwardNotGet(campMissionVo.getId());
                        signCalRedPoint(MConst.Camp, RedPointConst.CAMP_GET_AWARD);
                        signCalRedPoint(MConst.Camp, RedPointConst.CAMP_MISSION);
                    }
                    context().update(roleCampTimes);
                }
            }
        }

    }

    public void getMissionAward(int missionId) {
        if (roleCampTimes.getCanGetAwardSet().contains(missionId)) {
            ToolModule toolModule = module(MConst.Tool);
            CampMissionVo campMissionVo = CampManager.campMissionMap.get(missionId);
            Map<Integer, Integer> reward = campMissionVo.getReward();
            toolModule.addAndSend(reward, EventType.CAMP_COMPLETE_MISSION.getCode());
            ClientAward clientAward = new ClientAward(reward);
            clientAward.setType((byte) 1);
            send(clientAward);
            roleCampTimes.delAwardNotGet(missionId);
            context().update(roleCampTimes);
            reqMissionList();
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_GET_AWARD);
            ServerLogModule serverLogModule = module(MConst.ServerLog);
            serverLogModule.log_camp_activty_mission(2, missionId, reward);
        } else {
            warn("没有奖励可领取");
        }
    }

    public boolean isMissionComplete(int id) {
        for (CampMissionVo missionVo : campMissionVoList) {
            if (missionVo.getId() == id) {
                if (missionVo.isComplete(roleCampTimes)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 官职改变触发
     *
     * @param event
     */
    public void onOfficerChange(OfficerChangeEvent event) {
        refreshAndSendOfficerAttr();
        RoleInfoChangeEvent roleInfoChangeEvent = new RoleInfoChangeEvent();
        roleInfoChangeEvent.setRoleId(id());
        roleInfoChangeEvent.setCityId(roleCamp.getCityId());
        roleInfoChangeEvent.setCommonOfficerId(roleCamp.getCommonOfficerId());
        roleInfoChangeEvent.setRareOfficerId(roleCamp.getRareOfficerId());
        roleInfoChangeEvent.setDesignateOfficerId(roleCamp.getDesignateOfficerId());
        ServiceHelper.campLocalMainService().handleRoleInfoChange(roleInfoChangeEvent);
    }


    public void onEvent(Event event) {
        if (event instanceof AddToolEvent) {
            signCalRedPoint(MConst.Camp, RedPointConst.CAMP_UPGRADE_OFFICER);
        }
        if (event instanceof CampFightEvent) {
            CampFightEvent campFightEvent = (CampFightEvent) event;
            switch (campFightEvent.getAction()) {
                case CampFightEvent.TYPE_ADD_DAILY_SCORE: {
                    QiChuDaZuoZhanActivity campActivity = (QiChuDaZuoZhanActivity) campActivityMap.get(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN);
                    campActivity.updateCampFightScore(campFightEvent.getScore());
                }
                break;
                case CampFightEvent.TYPE_EXIT: {
                    ServerLogModule serverLogModule = module(MConst.ServerLog);
                    serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_45.getThemeId(),
                            serverLogModule.makeJuci(), ThemeType.ACTIVITY_45.getThemeId(), CampManager.STAGE_ID_CAMP_FIGHT, 0);
                    SceneModule sceneModule = module(MConst.Scene);
                    sceneModule.backToCity(false);
                }
                break;
                case CampFightEvent.TYPE_MATCHING_SUCCESS: {
                    SceneModule sceneModule = module(MConst.Scene);
                    sceneModule.setLastSceneType(SceneManager.SCENETYPE_CAMP_FIGHT);
                    ServerLogModule serverLogModule = module(MConst.ServerLog);
                    serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_45.getThemeId(), CampManager.STAGE_ID_CAMP_FIGHT);
                }
                break;
            }
        }
    }

    /**
     * 捐献元宝换声望
     *
     * @param money
     */
    public void reqDonateYB(int money) {
        if (money > 0 && money <= CampManager.CampDonateMaxCount - roleCampTimes.getDonateCount()) {
            int reputation = CampManager.campDonateUnit * money;
            ToolModule toolModule = module(MConst.Tool);
            boolean success = toolModule.deleteAndSend(ToolManager.BANDGOLD, money, EventType.CAMP_DONATE_YB.getCode());
            if (success) {
                roleCampTimes.addDonateCount(money);
                toolModule.addAndSend(ToolManager.REPUTATION, reputation, EventType.CAMP_DONATE_YB.getCode());
                Map<Integer, Integer> reward = new HashMap<>();
                reward.put(ToolManager.REPUTATION, reputation);
                ClientAward clientAward = new ClientAward(reward);
                send(clientAward);
                reqOpenDonateYBUI();
                context().update(roleCampTimes);
            } else {
                warn("道具数量不足扣除失败");
            }
        } else {
            warn("camp_desc_donatermax");
        }
    }

    /**
     * 请求打开捐献元宝界面
     */
    public void reqOpenDonateYBUI() {
        ClientOfficerPacket clientOfficerPacket = new ClientOfficerPacket(ClientOfficerPacket.SEND_DONATE_YB_UI);
        clientOfficerPacket.setDayDonateYBCount(roleCampTimes.getDonateCount());
        send(clientOfficerPacket);
    }

    public String makeFsStr() {
        StringBuilder sb = new StringBuilder();
        int commonFs = 0;
        int rareFs = 0;
        if (roleCamp == null) {
            sb.append("official_base:0#official_special:0#");
            return sb.toString();
        }
        int commonOfficerId = roleCamp.getCommonOfficerId();
        int rareOfficerId = roleCamp.getRareOfficerId();
        //int designateOfficerId = roleCamp.getDesignateOfficerId();
        if (commonOfficerId != 0) {  //普通官职
            Attribute commonAttribute = new Attribute(CampManager.commonOfficerMap.get(commonOfficerId).getAttr());
            commonFs = FormularUtils.calFightScore(commonAttribute);
        }
        if (rareOfficerId != 0) { //稀有官职
            Attribute rareAttribute = new Attribute(CampManager.rareOfficerMap.get(rareOfficerId).getAttr());
            rareFs = FormularUtils.calFightScore(rareAttribute);
        }


        sb.append("official_base:").append(commonFs).append("#official_special:").append(rareFs).append("#");
        return sb.toString();
    }

    public CampActivity getCampActivityById(int id) {
        return campActivityMap.get(id);
    }
}
