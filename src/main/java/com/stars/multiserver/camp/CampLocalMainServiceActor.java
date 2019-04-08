package com.stars.multiserver.camp;

import com.stars.bootstrap.ServerManager;
import com.stars.core.SystemRecordMap;
import com.stars.core.dao.DbRowDao;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.event.*;
import com.stars.modules.camp.prodata.CampCityVo;
import com.stars.modules.camp.prodata.CampLevelVo;
import com.stars.modules.camp.prodata.CampRankAwardVo;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.multiserver.camp.usrdata.RareOfficerRolePo;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.CampRoleReputationRankPo;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class CampLocalMainServiceActor extends ServiceActor implements CampLocalMainService {
    private Map<Integer, List<Long>> campTypeRoleMap = new HashMap<>();
    private CampTypeScale campTypeScale;
    private DbRowDao campDao = new DbRowDao("campLocalMainServiceActor");
    /**
     * 《camptype，AllServerCampPo》
     */
    private Map<Integer, AllServerCampPo> allServerCampMap = new HashMap<>();
    /**
     * 《camptype，prosperousNum》
     */
    private Map<Integer, Integer> tmpProsperousNumMap = new HashMap<>();//临时数据用于缓存繁荣度，累计起来推送过去
    /**
     * 《cityid，《roleid，CampRoleReputationRankPo》
     */
    private Map<Integer, Map<Long, CampRoleReputationRankPo>> sortedCityRankMap;//阵营服推送过来的排行榜
    /**
     * 《cityid，《roleid，CampRoleReputationRankPo》
     */
    private Map<Integer, List<CampRoleReputationRankPo>> sortedCityRankListMap;//阵营服推送过来的排行榜
    /**
     * 《cityid，《roleid，CampPlayerImageData》
     */
    private Map<Integer, Map<Long, CampPlayerImageData>> campCityFightMap = new HashMap<>();//阵营 齐楚之战镜像数据  key:cityId
    private Map<Integer, Integer> cityPlayerNumMap = new HashMap<>();//城池人数
    Integer campServerId;
    Integer gameServerId;
    /**
     * 本轮稀有官职角色名单
     */
    private Map<Integer, Map<Long, RareOfficerRolePo>> currentRoundRoleRareOfficerMap;
    private Map<Integer, List<RareOfficerRolePo>> currentRoundRoleRareOfficerListMap;
    private Map<Integer, List<CampRoleReputationRankPo>> lastReputationRank;
    /**
     * 各城池稀有官职名单列表
     */
    private Map<Long, RareOfficerRolePo> allCityRareOfficerRoleMap;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("campLocalMainService", this);
        String sql = "select roleid from rolecamp where camptype=%s;";
        for (Integer campType : CampManager.campAtrMap.keySet()) {
            List<Long> roleIds = DBUtil.queryList(DBUtil.DB_USER, Long.class, String.format(sql, campType));
            campTypeRoleMap.put(campType, roleIds);
        }
        resetProusperousPool();
        campServerId = MultiServerHelper.getCampServerId();
        gameServerId = MultiServerHelper.getServerId();
        SchedulerManager.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Map<Integer, Integer> tmpCampTypeRoleNumMap = new HashMap<>();
                for (Map.Entry<Integer, List<Long>> entry : campTypeRoleMap.entrySet()) {
                    tmpCampTypeRoleNumMap.put(entry.getKey(), entry.getValue().size());
                }
                try {
                    MainRpcHelper.campRemoteMainService().shareCampTypeRoleNum(campServerId, tmpCampTypeRoleNumMap, gameServerId);
                    MainRpcHelper.campRemoteMainService().donateCampProsperous(campServerId, tmpProsperousNumMap);
                    ServiceHelper.campLocalMainService().shareLocalReputationRank();
                } catch (Exception e) {
//                    就只是想去掉报错的打印
//                    e.printStackTrace();
                }
                resetProusperousPool();
            }
        }, 10, 5, TimeUnit.SECONDS);
    }

    /**
     * 重置繁荣度池
     */
    private void resetProusperousPool() {
        tmpProsperousNumMap = new HashMap<>();
        for (Integer campType : campTypeRoleMap.keySet()) {
            tmpProsperousNumMap.put(campType, 0);
        }
    }

    /**
     * 连接阵营服并分享阵营人数
     */
    @Override
    public void connectAndShareRoleNum() {
        Map<Integer, Integer> tmpCampTypeRoleNumMap = new HashMap<>();
        for (Map.Entry<Integer, List<Long>> entry : campTypeRoleMap.entrySet()) {
            tmpCampTypeRoleNumMap.put(entry.getKey(), entry.getValue().size());
        }
        MainRpcHelper.campRemoteMainService().shareCampTypeRoleNum(campServerId, tmpCampTypeRoleNumMap, MultiServerHelper.getServerId());

    }

    /**
     * 加入阵营凑个数
     *
     * @param roleId
     * @param campType
     */
    @Override
    public void joinCamp(Long roleId, Integer campType) {
        List<Long> roleIds = campTypeRoleMap.get(campType);
        roleIds.add(roleId);
    }

    /**
     * 获取随机阵营
     *
     * @return
     */
    @Override
    public Integer getRandomCampType() {
        Integer campType;
        if (getAllCampRoleNum() >= CampManager.campEquilibriumnum) {
            double randomValue = 0.5 + Math.min(0.5, Math.max(-0.5, campTypeScale.getScale() * CampManager.randomEnterCampConst));
            int rand = RandomUtil.rand(0, 10);
            if (rand <= randomValue * 10) {
                campType = campTypeScale.getLowCampType();
            } else {
                if (campTypeScale.getLowCampType() == CampManager.campTypeArr[0]) {
                    campType = CampManager.campTypeArr[1];
                } else {
                    campType = CampManager.campTypeArr[0];
                }
            }
        } else {
            int index = RandomUtil.rand(0, 1);
            campType = CampManager.campTypeArr[index];
        }
        return campType;
    }


    @Override
    public void save() {
        campDao.flush();
    }

    /**
     * 由阵营服推送全服阵营数据到各个游戏服
     *
     * @param serverId
     * @param allServerCampMap
     * @param campTypeScale
     */
    @Override
    public void pushAllServerCampRoleNum(int serverId, Map<Integer, AllServerCampPo> allServerCampMap, CampTypeScale campTypeScale) {
        this.allServerCampMap = allServerCampMap;
        this.campTypeScale = campTypeScale;
    }


    /**
     * 捐献繁荣度
     *
     * @param roleId
     * @param prosperousNum
     */
    @Override
    public void donateCampProsperous(long roleId, int campType, int prosperousNum) {
        AllServerCampPo allServerCamp = allServerCampMap.get(campType);
        /**
         * 临时性加上，欺骗用户，最终会被阵营服的数据覆盖
         */
        boolean canUpgrade = allServerCamp.addProsperousNum(prosperousNum);
        tmpProsperousNumMap.put(campType, tmpProsperousNumMap.get(campType) + prosperousNum);
        if (canUpgrade) {
            MainRpcHelper.campRemoteMainService().donateCampProsperous(campServerId, tmpProsperousNumMap);
            resetProusperousPool();
        }

    }

    @Override
    public void handleCampLevelUp(int serverId, CampLevelUpEvent campLevelUpEvent) {
        int campType = campLevelUpEvent.getCampType();
        List<Long> roleIds = campTypeRoleMap.get(campType);
        int newLevel = campLevelUpEvent.getNewLevel();
        CampLevelVo campLevelVo = CampManager.getCampLevelVo(campType, newLevel);
        Integer emailTemplateId = campLevelVo.getEmail();
        ServiceHelper.roleService().notice(roleIds, campLevelUpEvent);
        for (Long roleId : roleIds) {
            ServiceHelper.emailService().sendToSingle(roleId, emailTemplateId, 0L, "系统", null);
        }

    }


    @Override
    public AllServerCampPo getAllServerCampByCampType(int campType) {
        return allServerCampMap.get(campType);
    }

    /**
     * 更新声望数据
     *
     * @param reputationRankPo
     */
    @Override
    public void updateReputation(CampRoleReputationRankPo reputationRankPo) {
        CampRoleReputationRankPo roleReputationRankPo = (CampRoleReputationRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_CAMP_CITY_REPUTATION, reputationRankPo.getRoleId(), reputationRankPo.getCityId());
        CampRoleReputationRankPo newRoleReputationRankPo;
        if (roleReputationRankPo != null) {
            newRoleReputationRankPo = (CampRoleReputationRankPo) roleReputationRankPo.copy();
            newRoleReputationRankPo.setReputation(reputationRankPo.getReputation());
            newRoleReputationRankPo.setFightScore(reputationRankPo.getFightScore());
            newRoleReputationRankPo.setCommonOfficerId(reputationRankPo.getCommonOfficerId());
        } else {
            newRoleReputationRankPo = reputationRankPo;
            newRoleReputationRankPo.setServerId(ServerManager.getServer().getConfig().getServerId());
            newRoleReputationRankPo.setRankId(RankConstant.RANKID_CAMP_CITY_REPUTATION);
        }
        ServiceHelper.rankService().updateRank4CampCity(RankConstant.RANK_TYPE_CAMP_CITY_REPUTATION, newRoleReputationRankPo, newRoleReputationRankPo.getCityId(), reputationRankPo.getCityId());
    }

    @Override
    public void shareLocalReputationRank() {
        Map<Integer, List<AbstractRankPo>> map = new HashMap<>();
        for (CampCityVo campCityVo : CampManager.campCityVoList) {
            Integer cityId = campCityVo.getId();
            List<AbstractRankPo> frontRankList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_CAMP_CITY_REPUTATION, CampRemoteMainServiceActor.AVALIABLE_RANK_LIMIT, cityId);
            map.put(cityId, frontRankList);
        }
        MainRpcHelper.campRemoteMainService().updateCampCityReputationRank(campServerId, map, gameServerId);
    }

    /**
     * 接受阵营服推送过来的全服稀有官职角色
     *
     * @param serverId
     * @param currentRoundroleRareOfficerMap
     * @param lastRoleReputationRankMap
     * @param reset                          重置才发奖
     */
    @Override
    public void pushAllServerRareOfficerRoles(int serverId, Map<Integer, Map<Long, RareOfficerRolePo>> currentRoundroleRareOfficerMap, Map<Integer, List<CampRoleReputationRankPo>> lastRoleReputationRankMap, boolean reset) {
        this.currentRoundRoleRareOfficerMap = currentRoundroleRareOfficerMap;
        if (reset) {
            this.lastReputationRank = lastRoleReputationRankMap;
        }
        this.currentRoundRoleRareOfficerListMap = new HashMap<>();
        this.allCityRareOfficerRoleMap = new HashMap<>();
        for (Map.Entry<Integer, Map<Long, RareOfficerRolePo>> entry : currentRoundroleRareOfficerMap.entrySet()) {
            allCityRareOfficerRoleMap.putAll(entry.getValue());
            List<RareOfficerRolePo> rareOfficerRoleList = new ArrayList<>();
            rareOfficerRoleList.addAll(entry.getValue().values());
            Collections.sort(rareOfficerRoleList);
            currentRoundRoleRareOfficerListMap.put(entry.getKey(), rareOfficerRoleList);
            if (reset) {
                sendRewardOnReset(entry.getKey());
            }
        }
        if (reset) {
            resetReputationData();
        }
    }

    /**
     * 重置阵营声望排行榜数据
     */
    private void resetReputationData() {
        SystemRecordMap.update(CampManager.campReputationResetTimestamp, System.currentTimeMillis());
        ServiceHelper.roleService().noticeAll(new RareOfficerResetEvent());
        ServiceHelper.rankService().resetRank4Camp();
    }


    /**
     * 给各城池稀有官职发奖
     *
     * @param cityId
     */
    private void sendRewardOnReset(Integer cityId) {
        /**
         * 全服排行榜的玩家发放排名奖励
         */
        List<CampRoleReputationRankPo> campRoleReputationRankPoList = lastReputationRank.get(cityId);
        Map<Long, CampRoleReputationRankPo> lastCampRoleReputationRankPoMap = new HashMap<>();
        for (CampRoleReputationRankPo campRoleReputationRankPo : campRoleReputationRankPoList) {
            if (campRoleReputationRankPo.getServerId() == MultiServerHelper.getServerId()) {
                lastCampRoleReputationRankPoMap.put(campRoleReputationRankPo.getRoleId(), campRoleReputationRankPo);
                long roleId = campRoleReputationRankPo.getRoleId();
                int rank = campRoleReputationRankPo.getRank();
                LogUtil.info("send reward to {},rank:{}", roleId, rank);
                CampRankAwardVo campRankAward = CampManager.getCampRankAward(cityId, rank);
                Map<Integer, Integer> reward = campRankAward.getRaward();
                ServiceHelper.emailService().sendToSingle(roleId, campRankAward.getEmail(), 0L, "系统", reward, rank + "");
            }
        }
        /**
         * 为没进全服排名的玩家发送普通奖励
         */
        Set<Long> container = new HashSet<>();
        CampRankAwardVo campRankAward = CampManager.getCampRankAward(cityId, CampRemoteMainServiceActor.AVALIABLE_RANK_LIMIT + 1);
        List<AbstractRankPo> frontRank = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_CAMP_CITY_REPUTATION, 999, cityId);
        for (AbstractRankPo abstractRankPo : frontRank) {
            if (container.contains(abstractRankPo.getUniqueId())) {
                continue;
            }
            if (!lastCampRoleReputationRankPoMap.containsKey(abstractRankPo.getUniqueId())) {
                container.add(abstractRankPo.getUniqueId());
                ServiceHelper.emailService().sendToSingle(abstractRankPo.getUniqueId(), campRankAward.getEmail(), 0L, "系统", campRankAward.getRaward(), CampRemoteMainServiceActor.AVALIABLE_RANK_LIMIT + "+");
            }
        }
    }


    @Override
    public CampTypeScale getCampTypeScale() {
        return campTypeScale;
    }

    @Override
    public int getAllCampRoleNum() {
        int total = 0;
        for (AllServerCampPo allServerCampPo : allServerCampMap.values()) {
            total += allServerCampPo.getRoleNum();
        }
        return total;
    }

    @Override
    public RareOfficerRolePo getAllCityRareOfficerByRoleId(long roleId) {
        if (allCityRareOfficerRoleMap == null) {
            return null;
        }
        return allCityRareOfficerRoleMap.get(roleId);
    }


    @Override
    public List<RareOfficerRolePo> getRareOfficerListByCityId(int cityId) {
        Map<Long, RareOfficerRolePo> rareOfficerRoleMap = this.currentRoundRoleRareOfficerMap.get(cityId);
        if (rareOfficerRoleMap == null) {
            rareOfficerRoleMap = new HashMap<>();
        }
        List<RareOfficerRolePo> rareOfficerRoleList = new ArrayList<>();
        rareOfficerRoleList.addAll(rareOfficerRoleMap.values());
        return rareOfficerRoleList;
    }

    @Override
    public int getHighestRareOfficerCityId(int campType) {
        List<Integer> cityIdList = null;
        AllServerCampPo allServerCampPo = allServerCampMap.get(campType);
        try {
            cityIdList = StringUtil.toArrayList(allServerCampPo.getOpenedCityIds(), Integer.class, '+');
            Collections.sort(cityIdList, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2 - o1;
                }
            });
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

        for (int cityId : cityIdList) {
            Map<Long, RareOfficerRolePo> rareOfficerRolePoMap = this.currentRoundRoleRareOfficerMap.get(cityId);
            if (rareOfficerRolePoMap != null && rareOfficerRolePoMap.size() > 0) {
                return cityId;
            }
        }
        return 1;
    }

    @Override
    public void handleChangeCity(CampCityChangeEvent campCityChangeEvent) {
        CampRoleReputationRankPo rank = (CampRoleReputationRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_CAMP_CITY_REPUTATION, campCityChangeEvent.getRoleId(), campCityChangeEvent.getOldCityId());
        if (rank != null) {
            CampRoleReputationRankPo newRank = (CampRoleReputationRankPo) rank.copy();
            newRank.setCityId(campCityChangeEvent.getNewCityId());
            ServiceHelper.rankService().updateRank4CampCity(RankConstant.RANK_TYPE_CAMP_CITY_REPUTATION, newRank, campCityChangeEvent.getNewCityId(), campCityChangeEvent.getOldCityId());
        }
        MainRpcHelper.campRemoteMainService().handleChangeCity(campServerId, campCityChangeEvent);
    }

    @Override
    public void pushAllServerRank(int serverId, Map<Integer, Map<Long, CampRoleReputationRankPo>> sortedCityRankMap) {
        this.sortedCityRankMap = sortedCityRankMap;
        this.sortedCityRankListMap = new HashMap<>();
        for (Map.Entry<Integer, Map<Long, CampRoleReputationRankPo>> entry : sortedCityRankMap.entrySet()) {
            List<CampRoleReputationRankPo> list = new ArrayList<CampRoleReputationRankPo>(entry.getValue().values());
            Collections.sort(list);
            this.sortedCityRankListMap.put(entry.getKey(), list);
        }
    }

    @Override
    public void pushCampCityFightMap(int serverId, Map<Integer, Map<Long, CampPlayerImageData>> campCityFightMap,
                                     Map<Integer, Integer> cityPlayerNumMap) {
        this.campCityFightMap = campCityFightMap;
        this.cityPlayerNumMap = cityPlayerNumMap;
    }

    @Override
    public void getPlayerImageData(int serverId, Set<Long> roleSet) {
        List<CampPlayerImageData> dataList = new ArrayList<>();
        for (long roleId : roleSet) {
            try {
                Summary summary = ServiceHelper.summaryService().getSummary(roleId);
                if (summary == null) {

                } else {
                    Player player = PlayerSystem.get(roleId);
                    if (player != null) {
                        CampCityFightEvent event = new CampCityFightEvent();
                        event.setOpType(CampCityFightEvent.GET_PLAYERIMAGE);
                        ServiceHelper.roleService().notice(roleId, event);
                    } else {
                        CampPlayerImageData data = getImageData(roleId, summary);
                        if (data == null) continue;
                        dataList.add(data);
                    }
                }
            } catch (Exception e) {
                LogUtil.error("Multi Get PlayerImageData fail", e);
            }
        }
        MainRpcHelper.campRemoteMainService().addPlayerImageData(campServerId, dataList);
    }

    /**
     * 获取玩家镜像数据
     */
    @Override
    public void getPlayerImageData(int serverId, long roleId) {
        try {
            Summary summary = ServiceHelper.summaryService().getSummary(roleId);
            if (summary == null) {

            } else {
                Player player = PlayerSystem.get(roleId);
                if (player != null) {
                    CampCityFightEvent event = new CampCityFightEvent();
                    event.setOpType(CampCityFightEvent.GET_PLAYERIMAGE);
                    ServiceHelper.roleService().notice(roleId, event);
                } else {
                    CampPlayerImageData data = getImageData(roleId, summary);
                    if (data == null) return;
                    MainRpcHelper.campRemoteMainService().addPlayerImageData(campServerId, data);
                }
            }
        } catch (Exception e) {
            LogUtil.error("Get PlayerImageData fail", e);
        }
    }

    private CampPlayerImageData getImageData(long roleId, Summary summary) throws SQLException {
        StringBuffer sql = new StringBuffer();
        sql.append("select * from rolecamp where roleid = ").append(roleId).append(";");
        RoleCampPo roleCampPo = DBUtil.queryBean(DBUtil.DB_USER, RoleCampPo.class, sql.toString());
        if (roleCampPo == null) return null;
        RoleSummaryComponent rsc = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        CampPlayerImageData data = new CampPlayerImageData();
        data.setJob(rsc.getRoleJob());
        data.setCityId(roleCampPo.getCityId());
        data.setCommonOfficerId(roleCampPo.getCommonOfficerId());
        data.setDesignateOfficerId(roleCampPo.getDesignateOfficerId());
        data.setRareOfficerId(roleCampPo.getRareOfficerId());
        Map<String, FighterEntity> map = FighterCreator.createBySummary(FighterEntity.CAMP_ENEMY, summary);
        FighterEntity playerEntity = null;
        for (FighterEntity entity : map.values()) {
            if (entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                playerEntity = entity;
                break;
            }
        }
        if (playerEntity == null) return null;

        data.setEntity(playerEntity);
        data.setServerId(MultiServerHelper.getDisplayServerId());
        return data;
    }

    @Override
    public CampRoleReputationRankPo getRankByCityId(int cityId, long roleId) {
        return sortedCityRankMap.get(cityId).get(roleId);
    }

    @Override
    public List<CampRoleReputationRankPo> getRankListByCityId(int cityId) {
        return this.sortedCityRankListMap.get(cityId);
    }

    @Override
    public Map<Integer, List<CampRoleReputationRankPo>> getCityRankListMap() {
        return this.sortedCityRankListMap;
    }

    @Override
    public Map<Integer, Integer> getCityPlayerNumMap() {
        return cityPlayerNumMap;
    }

    @Override
    public List<RareOfficerRolePo> getCurrentRoundRoleRareOfficerListByCityId(int cityId) {
        if (currentRoundRoleRareOfficerListMap == null) {
            return new ArrayList<>();
        }
        return currentRoundRoleRareOfficerListMap.get(cityId);
    }

    @Override
    public Map<Long, CampPlayerImageData> getCampCityFightMap(int campType) {
        Map<Long, CampPlayerImageData> map = campCityFightMap.get(campType);
        Map<Long, CampPlayerImageData> tempMap = null;
        if (map == null) {
            tempMap = new HashMap<>();
        } else {
            tempMap = new HashMap<>(map);
        }
        return tempMap;
    }

    @Override
    public Map<Integer, AllServerCampPo> getAllServerCampMap() {
        return allServerCampMap;
    }


    @Override
    public void handleRoleInfoChange(RoleInfoChangeEvent roleInfoChangeEvent) {
        AbstractRankPo rank = ServiceHelper.rankService().getRank(RankConstant.RANKID_CAMP_CITY_REPUTATION, roleInfoChangeEvent.getRoleId(), roleInfoChangeEvent.getCityId());
        if (rank != null) {
            CampRoleReputationRankPo campRoleReputationRankPo = (CampRoleReputationRankPo) rank;
            if (roleInfoChangeEvent.getCommonOfficerId() != 0) {
                campRoleReputationRankPo = (CampRoleReputationRankPo) campRoleReputationRankPo.copy();
            }
            if (roleInfoChangeEvent.getNewName() != null) {
                campRoleReputationRankPo.setName(roleInfoChangeEvent.getNewName());
            }
            if (roleInfoChangeEvent.getNewJobId() != null) {
                campRoleReputationRankPo.setJobId(roleInfoChangeEvent.getNewJobId());
            }
            if (roleInfoChangeEvent.getCommonOfficerId() != 0) {
                campRoleReputationRankPo.setCommonOfficerId(roleInfoChangeEvent.getCommonOfficerId());
            }
            if (roleInfoChangeEvent.getRareOfficerId() != 0) {
                campRoleReputationRankPo.setRareOfficerId(roleInfoChangeEvent.getRareOfficerId());
            }
            if (roleInfoChangeEvent.getDesignateOfficerId() != 0) {
                campRoleReputationRankPo.setDesignateOfficerId(roleInfoChangeEvent.getDesignateOfficerId());
            }
            if (roleInfoChangeEvent.getCommonOfficerId() != 0) {
                ServiceHelper.rankService().updateRank4CampCity(RankConstant.RANK_TYPE_CAMP_CITY_REPUTATION, campRoleReputationRankPo, roleInfoChangeEvent.getCityId(), campRoleReputationRankPo.getCityId());
            }
            campDao.update(campRoleReputationRankPo);
            MainRpcHelper.campRemoteMainService().handleRoleInfoChange(campServerId, roleInfoChangeEvent, gameServerId);
        }
    }

    @Override
    public void printState() {

    }

}
