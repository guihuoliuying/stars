package com.stars.modules.camp.activity.imp;

import com.stars.bootstrap.ServerManager;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.CampTeamMember;
import com.stars.modules.camp.activity.AbstractCampActivity;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.event.ActivityFinishEvent;
import com.stars.modules.camp.event.CampCityFightEvent;
import com.stars.modules.camp.packet.ClientCampCiytFight;
import com.stars.modules.camp.pojo.CampCityFightData;
import com.stars.modules.camp.prodata.CampActivityVo;
import com.stars.modules.camp.prodata.CampCityVo;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.modules.drop.DropModule;
import com.stars.modules.offlinepvp.OfflinePvpManager;
import com.stars.modules.offlinepvp.prodata.OPRobotVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.imp.fight.CampCityFightScene;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.CampUtils;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huzhipeng on 2017/7/19.
 */
public class QiChuZhiZhengActivity extends AbstractCampActivity {
    private int lastChaCityId;
    private List<CampPlayerImageData> enemyList = new ArrayList<>();//齐楚之战敌人列表


    public QiChuZhiZhengActivity(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap, RoleCampPo roleCampPo, RoleCampTimesPo roleCampTimesPo) {
        super(name, id, self, eventDispatcher, moduleMap, roleCampPo, roleCampTimesPo);
    }


    /**
     * 齐楚之战主界面UI
     */
    public void openCityFightUI() {
        if (getRoleCamp() == null) return;
        ClientCampCiytFight packet = new ClientCampCiytFight();
        packet.setOpType(ClientCampCiytFight.SEND_MAIN_UI_INFO);
//        String officerName = "";
//        int officerLevel = 0;
//        int officerQuality = 0;
//        CommonOfficerVo commonOfficer = roleCamp.getCommonOfficer();
//        officerName = commonOfficer.getName();
//        officerLevel = commonOfficer.getLevel();
//        DesignateOfficerVo designateOfficer = roleCamp.getDesignateOfficer();
//        if (designateOfficer != null) {
//            officerName = designateOfficer.getName();
//            officerQuality = designateOfficer.getId();
//        }
//        RareOfficerVo rareOfficer = roleCamp.getRareOfficer();
//        if (rareOfficer != null) {
//            officerName = rareOfficer.getName();
//            officerQuality = rareOfficer.getId();
//        }
        int commonOfficerId = getRoleCamp().getCommonOfficerId();
        int designateOfficerId = getRoleCamp().getDesignateOfficerId();
        int rareOfficerId = getRoleCamp().getRareOfficerId();
        packet.setCommonOfficerId(commonOfficerId);
        packet.setDesignateOfficerId(designateOfficerId);
        packet.setRareOfficerId(rareOfficerId);
        packet.setCityId(getRoleCamp().getCityId());
        packet.setCampType(getRoleCamp().getCampType());
        packet.setCityFightNum(getRoleCampTimes().getCityFightNum());
        send(packet);
    }

    /**
     * 获取城池信息
     */
    public void getCityInfo() {
        if (getRoleCamp() == null) return;
        ClientCampCiytFight packet = new ClientCampCiytFight();
        packet.setOpType(ClientCampCiytFight.SEND_CITY_INFO);
        packet.setCampType(getRoleCamp().getCampType());
        packet.setCityId(getRoleCamp().getCityId());
        send(packet);
    }

    public CampTeamMember createCampTeamMember() {
        CampTeamMember member = new CampTeamMember(BaseTeamManager.TEAM_TYPE_CAMPCITYFIGHT, getRoleCamp());
        return member;
    }

    public void getMatchTime(byte continueFight) {
        if (getRoleCamp() == null) return;
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) return;
        //匹配时间
        int matchTime = RandomUtil.rand(CampManager.matchTime[0], CampManager.matchTime[1]);
        if (continueFight == 1) {
            matchTime = 0;
        }
        ClientCampCiytFight packet = new ClientCampCiytFight();
        packet.setOpType(ClientCampCiytFight.SEND_MATCHTINE);
        packet.setMatchTime(matchTime);
        sendToMember(packet);
        team.setFight(true);
    }

    /**
     * 匹配对手
     */
    public void matchEnemy(int chaCityId) {
        if (getRoleCamp() == null) return;
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) return;
        if (team.getTeamType() != BaseTeamManager.TEAM_TYPE_CAMPCITYFIGHT) return;
        int campType = getRoleCamp().getCampType();
        int cityId = getRoleCamp().getCityId();
        CampCityVo campCityVo = CampManager.campCityMap.get(cityId);
        Integer cityLevel = campCityVo.getLevel();
        int enemyCampType = CampManager.getEnemyCampType(campType);
        CampCityVo enemyCampCityVo = CampManager.campCityMap.get(chaCityId);

        //检测是否可以挑战这城池
        Integer playerNum = ServiceHelper.campLocalMainService().getCityPlayerNumMap().get(chaCityId);
//		if(playerNum==null) return;
//		if(playerNum<CampManager.Cha_CityPlayNum_Limit) return;//敌方城池人数不足
        AllServerCampPo allServerCampPo = ServiceHelper.campLocalMainService().getAllServerCampByCampType(enemyCampType);
        List<CampCityVo> campCityList = allServerCampPo.getOpenedCampCityList();
        List<CampCityVo> campAllCityVoList = allServerCampPo.getCampCityVoList();
        if (!campAllCityVoList.contains(enemyCampCityVo)) return;//非敌方城池
        if (!campCityList.contains(enemyCampCityVo)) return;//未开发城池
        int levelSub = enemyCampCityVo.getLevel() - cityLevel;
        if (levelSub > 0 && levelSub > CampManager.cityLvLimit[1]) {//超过
            return;
        }
        if (levelSub < 0 && Math.abs(levelSub) > CampManager.cityLvLimit[0]) {//低于
            return;
        }

        enemyList.clear();
        Map<Long, CampPlayerImageData> campCityFightMap = ServiceHelper.campLocalMainService().getCampCityFightMap(chaCityId);
        //匹配筛选规则
        Map<Long, BaseTeamMember> members = team.getMembers();
        int memberSize = members.size();
        CampTeamMember teamMember = null;
        RoleCampPo roleCampPo = null;
        int rareOfficerNum = 0;
        int designateOfficerNum = 0;
        int commonNum = 0;
        int meanFightScore = 0;
        for (BaseTeamMember member : members.values()) {
            teamMember = (CampTeamMember) member;
            roleCampPo = teamMember.getRoleCampPo();
            if (roleCampPo.getRareOfficerId() > 0) {
                rareOfficerNum += 1;
            }
            if (roleCampPo.getDesignateOfficerId() > 0) {
                designateOfficerNum += 1;
            }
            meanFightScore += teamMember.getFightSocre();
        }
        meanFightScore = meanFightScore / memberSize;
        commonNum = memberSize - rareOfficerNum - designateOfficerNum;
        List<CampPlayerImageData> rareList = new ArrayList<>();
        List<CampPlayerImageData> designateList = new ArrayList<>();
        List<CampPlayerImageData> commonList = new ArrayList<>();
        Iterator<CampPlayerImageData> iterator = campCityFightMap.values().iterator();
        CampPlayerImageData playerImageData = null;
        for (; iterator.hasNext(); ) {
            playerImageData = iterator.next();
            if (rareOfficerNum > 0 && playerImageData.getRareOfficerId() > 0) {
                rareList.add(playerImageData);
                continue;
            }
            if (designateOfficerNum > 0 && playerImageData.getDesignateOfficerId() > 0) {
                designateList.add(playerImageData);
                continue;
            }
            commonList.add(playerImageData);
        }
        int rand = 0;
        for (int i = 0; i < rareOfficerNum; i++) {//稀有官职处理
            int size = rareList.size();
            if (size > 0) {
                rand = RandomUtil.rand(0, size - 1);
                enemyList.add(rareList.remove(rand));
                continue;
            }
            int dSize = designateList.size();
            if (dSize > 0) {
                rand = RandomUtil.rand(0, dSize - 1);
                enemyList.add(designateList.remove(rand));
                continue;
            }
            int cSize = commonList.size();
            if (cSize > 0) {
                rand = RandomUtil.rand(0, cSize - 1);
                enemyList.add(commonList.remove(rand));
                continue;
            }
        }
        for (int i = 0; i < designateOfficerNum; i++) {//任命官职处理
            int dSize = designateList.size();
            if (dSize > 0) {
                rand = RandomUtil.rand(0, dSize - 1);
                enemyList.add(designateList.remove(rand));
                continue;
            }
            int size = rareList.size();
            if (size > 0) {
                rand = RandomUtil.rand(0, size - 1);
                enemyList.add(rareList.remove(rand));
                continue;
            }
            int cSize = commonList.size();
            if (cSize > 0) {
                rand = RandomUtil.rand(0, cSize - 1);
                enemyList.add(commonList.remove(rand));
                continue;
            }
        }
        for (int i = 0; i < commonNum; i++) {//普通处理
            int cSize = commonList.size();
            if (cSize > 0) {
                rand = RandomUtil.rand(0, cSize - 1);
                enemyList.add(commonList.remove(rand));
                continue;
            }
            int dSize = designateList.size();
            if (dSize > 0) {
                rand = RandomUtil.rand(0, dSize - 1);
                enemyList.add(designateList.remove(rand));
                continue;
            }
            int size = rareList.size();
            if (size > 0) {
                rand = RandomUtil.rand(0, size - 1);
                enemyList.add(rareList.remove(rand));
                continue;
            }
        }
        //机器人
        int size = enemyList.size();
        int needNum = memberSize - size;
        if (needNum > 0) {
            List<CampPlayerImageData> robotList = getRobotByFightScore(meanFightScore, needNum, chaCityId);
            enemyList.addAll(robotList);
        }

        this.lastChaCityId = chaCityId;
        ClientCampCiytFight packet = new ClientCampCiytFight();
        packet.setOpType(ClientCampCiytFight.SEND_READY_FIGHT);
        packet.setCampType(campType);
        packet.setCityId(chaCityId);
        packet.setEnemyList(enemyList);
        packet.setMembers(members);
        sendToMember(packet);
        for (long member : team.getMembers().keySet()) {
            if (member == id()) continue;
            CampCityFightEvent event = new CampCityFightEvent();
            event.setOpType(CampCityFightEvent.SYN_ENEMY_INFO);
            ArrayList<CampPlayerImageData> copyList = new ArrayList<>(enemyList);
            event.setEnemyList(copyList);
            ServiceHelper.roleService().notice(member, event);
        }
    }

    public void sendToMember(Packet packet) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) return;
        for (long member : team.getMembers().keySet()) {
            PlayerUtil.send(member, packet);
        }
    }

    public void setEnemy(List<CampPlayerImageData> teamEnemyList) {
        enemyList = teamEnemyList;
    }

    /**
     * 根据战力获取机器人
     */
    private List<CampPlayerImageData> getRobotByFightScore(int meanFightScore, int count, int chaCityId) {
        CampCityVo enemyCampCityVo = CampManager.campCityMap.get(chaCityId);
        int minValue = (int) ((long) meanFightScore * CampManager.robotFightValue[0] / 1000);
        int maxValue = (int) ((long) meanFightScore * CampManager.robotFightValue[1] / 1000);
        Iterator<OPRobotVo> iterator = OfflinePvpManager.robotVoMap.values().iterator();
        OPRobotVo robotVo = null;
        List<OPRobotVo> selectRobotList = new ArrayList<>();
        int robotFightScore = 0;
        for (; iterator.hasNext(); ) {
            robotVo = iterator.next();
            robotFightScore = robotVo.getRobotFightScore();
            if (robotFightScore >= minValue && robotFightScore <= maxValue) {
                selectRobotList.add(robotVo);
            }
        }
        List<CampPlayerImageData> enemyRobotList = new ArrayList<>();
        if (selectRobotList.size() >= count) {
            int serverId = MultiServerHelper.getDisplayServerId();
            for (int i = 0; i < count; i++) {
                int size = selectRobotList.size();
                int rand = RandomUtil.rand(0, size - 1);
                robotVo = selectRobotList.remove(rand);
                Map<String, FighterEntity> createRobot = FighterCreator.createRobot(FighterEntity.CAMP_ENEMY, robotVo);
                for (FighterEntity entity : createRobot.values()) {
                    if (entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                        entity.setFighterType(FighterEntity.TYPE_ROBOT);
                        CampPlayerImageData data = new CampPlayerImageData();
                        data.setEntity(entity);
                        data.setServerId(serverId);
                        data.setJob(robotVo.getJobId());
                        data.setCityId(chaCityId);
                        data.setCommonOfficerId(2);
                        enemyRobotList.add(data);
                    }
                }
            }
        }
        return enemyRobotList;
    }

    /**
     * 取消匹配
     */
    public void cancelMatching() {
        if (getRoleCamp() == null) {
            return;
        }
//        enemyList.clear();
//        this.lastChaCityId = 0;
        ClientCampCiytFight packet = new ClientCampCiytFight();
        packet.setOpType(ClientCampCiytFight.CANCEL_MATCHING_SUCCESS);
        sendToMember(packet);
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) return;
        team.setFight(false);
    }

    /**
     * 开始战斗
     */
    public void startFight() {
        CampActivityVo campActivityVo = CampManager.campActivityMap.get(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG);
        if ((!campActivityVo.isOpen()) || (!campActivityVo.checkOpenTime())) {
            warn("campactivity2_tips_opentime");
            return;
        }
        if (getRoleCamp() == null) return;
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) return;
        if (id() != team.getCaptainId()) return;
        int teamId = team.getTeamId();
        CampCityFightScene scene = (CampCityFightScene) SceneManager.newScene(SceneManager.SCENETYPE_CAMP_CITY_FIGHT);
        Object[] params = new Object[]{teamId, lastChaCityId, CampManager.Camp_Fight_DungeonId};
        if (!scene.canEnter(moduleMap(), params)) return;
        scene.stageId = CampManager.Camp_Fight_DungeonId;
        Map<String, CampPlayerImageData> enemyMap = new HashMap<>();
        for (CampPlayerImageData data : enemyList) {
            enemyMap.put(data.getEntity().getUniqueId(), data);
        }
        enemyList.clear();
        scene.setEnemyMap(enemyMap);
        scene.addTeamMemberFighter(team.getMembers().values());
//        scene.enter(moduleMap(), new Object[]{teamId, lastChaCityId});
        SceneModule sceneModule = module(MConst.Scene);
        if (sceneModule.getScene() instanceof CampCityFightScene) {
            sceneModule.setScene(null);
        }
        sceneModule.enterScene(scene, SceneManager.SCENETYPE_CAMP_CITY_FIGHT, CampManager.Camp_Fight_DungeonId, params);
        team.setFight(true);
        ServiceHelper.campCityFightService().addToSceneMap(teamId, scene);
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_44.getThemeId(), CampManager.Camp_Fight_DungeonId);
    }

    /**
     * 继续挑战
     */
    public void nextFight() {
        CampActivityVo campActivityVo = CampManager.campActivityMap.get(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG);
        if ((!campActivityVo.isOpen()) || (!campActivityVo.checkOpenTime())) {
            backToCity(true);
            warn("campactivity2_tips_opentime");
            return;
        }
        getMatchTime((byte) 1);
    }

    public void fightEnd(byte result, int integral, List<CampCityFightData> integralList, int chaCityId, boolean teamAddition) {
        try {
            ServerLogModule serverLogModule = module(MConst.ServerLog);
            Map<Integer, Integer> awardMap = new HashMap<Integer, Integer>();
            if (result == 0) {//失败
                awardMap.putAll(CampManager.loseFinishAward);
                serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_44.getThemeId(),
                        serverLogModule.makeJuci(), ThemeType.ACTIVITY_44.getThemeId(), CampManager.Camp_Fight_DungeonId, 0);
            } else {//胜利
                awardMap.putAll(CampManager.winFinishAward);
                serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_44.getThemeId(),
                        serverLogModule.makeJuci(), ThemeType.ACTIVITY_44.getThemeId(), CampManager.Camp_Fight_DungeonId, 0);
            }
            int cityFightNum = getRoleCampTimes().getCityFightNum() + 1;
            getRoleCampTimes().setCityFightNum(cityFightNum);
            context().update(getRoleCampTimes());
            int scale = 1000;//收益比例
            if (cityFightNum >= CampManager.Max_Scale_Cha_Num) {
                scale = CampManager.awardScaleList.get(CampManager.awardScaleList.size() - 1)[2];
            } else {
                for (int[] info : CampManager.awardScaleList) {
                    if (cityFightNum >= info[0] && cityFightNum <= info[1]) {
                        scale = info[2];
                        break;
                    }
                }
            }
            //组队加成
            if (teamAddition) {
                integral = integral + (int) ((long) integral * CampManager.teamMark[0] / 1000) + CampManager.teamMark[1];
            }
            //城池等级差距 积分变化
            CampCityVo campCityVo = CampManager.campCityMap.get(getRoleCamp().getCityId());
            CampCityVo enemyCityVo = CampManager.campCityMap.get(chaCityId);
            int levelSub = campCityVo.getLevel() - enemyCityVo.getLevel();
            if (levelSub > 0) {
                integral = integral - (int) ((long) integral * CampManager.highCityLevelMark.get(levelSub) / 1000);
            } else if (levelSub < 0) {
                integral = integral + (int) ((long) integral * CampManager.highCityLevelMark.get(Math.abs(levelSub)) / 1000);
            }

            int dropId = 0;
            for (int[] awardInfo : CampManager.markAwardList) {
                if (integral >= awardInfo[0] && integral <= awardInfo[1]) {
                    dropId = awardInfo[2];
                    break;
                }
            }
            if (dropId > 0) {
                DropModule dropModule = module(MConst.Drop);
                Map<Integer, Integer> dropMap = dropModule.executeDrop(dropId, 1, true);
                if (StringUtil.isNotEmpty(dropMap)) {
                    Iterator<Map.Entry<Integer, Integer>> iterator = dropMap.entrySet().iterator();
                    Map.Entry<Integer, Integer> entry = null;
                    int itemId = 0;
                    for (; iterator.hasNext(); ) {
                        entry = iterator.next();
                        itemId = entry.getKey();
                        if (awardMap.containsKey(itemId)) {
                            awardMap.put(itemId, awardMap.get(itemId) + entry.getValue());
                        } else {
                            awardMap.put(itemId, entry.getValue());
                        }
                    }
                }
            }
            Iterator<Map.Entry<Integer, Integer>> iterator = awardMap.entrySet().iterator();
            Map.Entry<Integer, Integer> entry = null;
            int itemId = 0;
            int count = 0;
            for (; iterator.hasNext(); ) {
                entry = iterator.next();
                itemId = entry.getKey();
                count = entry.getValue() * scale / 1000;
                if (count > 0) {
                    awardMap.put(itemId, count);
                } else {
                    iterator.remove();
                }
            }
            CampUtils.addExtReward(CampUtils.TYPE_ACTIVITY_REWARD, getRoleCamp().getCampType(), awardMap);
            ClientCampCiytFight packet = new ClientCampCiytFight();
            packet.setOpType(ClientCampCiytFight.FIGHT_END);
            packet.setResult(result);
            packet.setScale(scale);
            packet.setAwardMap(awardMap);
            packet.setIntegralList(integralList);
            send(packet);
            eventDispatcher().fire(new ActivityFinishEvent(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG, awardMap));
        } catch (Exception e) {
            com.stars.util.LogUtil.error("fightEnd fail, roleid:" + id(), e);
        }
    }

    public void getPlayerImageData() {
        try {
            RoleModule roleModule = module(MConst.Role);
            CampPlayerImageData data = new CampPlayerImageData();
            data.setJob(roleModule.getRoleRow().getJobId());
            data.setCityId(getRoleCamp().getCityId());
            data.setCommonOfficerId(getRoleCamp().getCommonOfficerId());
            data.setDesignateOfficerId(getRoleCamp().getDesignateOfficerId());
            data.setRareOfficerId(getRoleCamp().getRareOfficerId());
            FighterEntity playerEntity = FighterCreator.createSelf(moduleMap(), FighterEntity.CAMP_ENEMY);
            data.setEntity(playerEntity);
            data.setServerId(MultiServerHelper.getDisplayServerId());
            Properties campProperties = ServerManager.getServer().getConfig().getProps().get("camp");
            String serverIdStr = campProperties.getProperty("serverId");
            int campServerId = Integer.parseInt(serverIdStr);
            MainRpcHelper.campRemoteMainService().addPlayerImageData(campServerId, data);
        } catch (Exception e) {
            LogUtil.error("getPlayerImageData fail, roleid:" + id(), e);
        }
    }

    /**
     * 回城
     */
    public void backToCity(boolean leave) {
        SceneModule sceneModule = module(MConst.Scene);
        ServiceHelper.campCityFightService().removeMember(id());
        sceneModule.backToCity(Boolean.FALSE);
        if (leave) {
            ServiceHelper.baseTeamService().leaveTeam(id());
        }
    }

    /**
     * 进入战斗 切换为副本场景
     */
    public void changeScene(CampCityFightScene scene) {
        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.setScene(scene);
    }


}
