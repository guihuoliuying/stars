package com.stars.multiserver.camp;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.event.CampCityChangeEvent;
import com.stars.modules.camp.event.CampLevelUpEvent;
import com.stars.modules.camp.event.RoleInfoChangeEvent;
import com.stars.modules.camp.pojo.CampEquilibrium;
import com.stars.modules.camp.prodata.CampAtrVo;
import com.stars.modules.camp.prodata.CampCityVo;
import com.stars.modules.camp.prodata.CampLevelVo;
import com.stars.modules.camp.prodata.CampRankAwardVo;
import com.stars.multiserver.camp.flow.CampRareOfficerFlow;
import com.stars.multiserver.camp.pojo.CampCityPlayer;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.multiserver.camp.usrdata.RareOfficerRolePo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.CampRoleReputationRankPo;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class CampRemoteMainServiceActor extends ServiceActor implements CampRemoteMainService {
    private CampTypeScale campTypeScale;
    /**
     * 全服阵营数据
     * 《camptype，AllServerCampPo》
     */
    private Map<Integer, AllServerCampPo> allServerCampMap = new HashMap<>();
    /**
     * 各服务器阵营人数组成
     * 《serverid，《camptype，number》》
     */
    private Map<Integer, Map<Integer, Integer>> serverCampRoleNumMap = new ConcurrentHashMap<>();
    /**
     * 各城池排行榜
     * 《cityid,treeset》
     */
    private Map<Integer, TreeSet<CampRoleReputationRankPo>> cityRankMap = new ConcurrentHashMap<>();
    /**
     * 各城市声望排行榜缓存
     * 《cityid,《roleid，CampRoleReputationRankPo》》
     */
    private Map<Integer, Map<Long, CampRoleReputationRankPo>> roleReputationRankMap;
    /**
     * 本轮稀有官职角色
     * 《cityid,《roleid，RareOfficerRolePo》》
     */
    private Map<Integer, Map<Long, RareOfficerRolePo>> currentRoundroleRareOfficerMap = new ConcurrentHashMap<>();
    /**
     * 稀有官职授予的最终排名数据
     */
    private Map<Integer, List<CampRoleReputationRankPo>> lastRoleReputationRankMap;
    /**
     * 阵营 齐楚之战镜像数据  key:cityId
     */
    private Map<Integer, Map<Long, CampPlayerImageData>> campCityFightMap = new HashMap<>();

    private Set<Long> waitDataSet = new HashSet<>();

    /**
     * 城市驻点玩家集合  key:cityId
     */
    private Map<Integer, CampCityPlayer> campCityPlayerMap = new HashMap<>();


    public static final int AVALIABLE_RANK_LIMIT = 50;
    private CampRareOfficerFlow campRareOfficerFlow = new CampRareOfficerFlow();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.campRemoteMainService, this);
        loadAllServerCamp();
        initRankContainer();
        loadRareOfficerRole();
        loadCityPlayer();
        setupScheduler();
    }

    /**
     * 加载稀有官职角色
     */
    private void loadRareOfficerRole() {
        String sql = "select * from rareofficerrole order by rank asc;";
        try {
            List<RareOfficerRolePo> rareOfficerRoles = DBUtil.queryList(DBUtil.DB_COMMON, RareOfficerRolePo.class, sql);
            for (RareOfficerRolePo rareOfficerRole : rareOfficerRoles) {
                int cityId = rareOfficerRole.getCityId();
                Map<Long, RareOfficerRolePo> currentCityRareOfficerRoleMap = currentRoundroleRareOfficerMap.get(cityId);
                if (currentCityRareOfficerRoleMap == null) {
                    currentCityRareOfficerRoleMap = new HashMap<>();
                    currentRoundroleRareOfficerMap.put(cityId, currentCityRareOfficerRoleMap);
                }
                currentCityRareOfficerRoleMap.put(rareOfficerRole.getRoleId(), rareOfficerRole);
            }
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private void loadCityPlayer() {
        String sql = "select * from campcityplayer";
        try {
            Map<Integer, CampCityPlayer> map = DBUtil.queryMap(DBUtil.DB_COMMON, "cityid", CampCityPlayer.class, sql);
//            if(!map.containsKey(300)){
//            	for(int i=1;i<=8;i++){
//            		CampCityPlayer campCityPlayer = map.get(i);
//            		if(campCityPlayer==null) continue;
//            		CampCityPlayer nextCampCityPlayer = map.get(i+1);
//            		if(nextCampCityPlayer!=null){            		
//            			campCityPlayer.setTotalNum(campCityPlayer.getTotalNum()-nextCampCityPlayer.getTotalNum());
//            			
//            			StringBuffer sql1 = new StringBuffer();
//            			String playerInfo = StringUtil.makeString(campCityPlayer.getPlayerMap(), '+', '|');
//            			sql1.append("insert into campcityplayer(cityid,playerinfo,totalnum) values(").append(campCityPlayer.getCityId()).append(",'").append(playerInfo)
//            			.append("',").append(campCityPlayer.getTotalNum()).append(") on duplicate key update playerinfo=values(playerinfo),totalnum=values(totalnum);");
//            			try {
//            				DBUtil.execSql(DBUtil.DB_COMMON, sql1.toString());
//            			} catch (Exception e) {
//            				LogUtil.error("update campcityplayer fail, sql:" + sql1.toString(), e);
//            			}
//            		}
//            	}
//            	for(int i=9;i<=16;i++){
//            		CampCityPlayer campCityPlayer = map.get(i);
//            		if(campCityPlayer==null) continue;
//            		CampCityPlayer nextCampCityPlayer = map.get(i+1);
//            		if(nextCampCityPlayer!=null){            		
//            			campCityPlayer.setTotalNum(campCityPlayer.getTotalNum()-nextCampCityPlayer.getTotalNum());
//            			
//            			StringBuffer sql1 = new StringBuffer();
//            			String playerInfo = StringUtil.makeString(campCityPlayer.getPlayerMap(), '+', '|');
//            			sql1.append("insert into campcityplayer(cityid,playerinfo,totalnum) values(").append(campCityPlayer.getCityId()).append(",'").append(playerInfo)
//            			.append("',").append(campCityPlayer.getTotalNum()).append(") on duplicate key update playerinfo=values(playerinfo),totalnum=values(totalnum);");
//            			try {
//            				DBUtil.execSql(DBUtil.DB_COMMON, sql1.toString());
//            			} catch (Exception e) {
//            				LogUtil.error("update campcityplayer fail, sql:" + sql1.toString(), e);
//            			}
//            		}
//            	}
//            	
//            	StringBuffer sql1 = new StringBuffer();
//    			String playerInfo = "";
//    			sql1.append("insert into campcityplayer(cityid,playerinfo,totalnum) values(").append(300).append(",'").append(playerInfo)
//    			.append("',").append(0).append(") on duplicate key update playerinfo=values(playerinfo),totalnum=values(totalnum);");
//    			try {
//    				DBUtil.execSql(DBUtil.DB_COMMON, sql1.toString());
//    			} catch (Exception e) {
//    				LogUtil.error("update campcityplayer fail, sql:" + sql1.toString(), e);
//    			}
//            }

//            String dSql = "delete from campcityplayer where cityid = 300";
//            try {
//				DBUtil.execSql(DBUtil.DB_COMMON, dSql);
//			} catch (Exception e) {
//				LogUtil.error("update campcityplayer fail, sql:" + dSql, e);
//			}
            campCityPlayerMap = map;
        } catch (Exception e) {
            LogUtil.error("loadCityPlayer fail", e);
        }
    }

    /**
     * 初始化排行榜容器
     */
    private void initRankContainer() {
        roleReputationRankMap = new ConcurrentHashMap<>();
        for (CampCityVo campCityVo : CampManager.campCityVoList) {
            cityRankMap.put(campCityVo.getId(), new TreeSet<CampRoleReputationRankPo>());
            roleReputationRankMap.put(campCityVo.getId(), new ConcurrentHashMap<Long, CampRoleReputationRankPo>());
        }
    }

    /**
     * 加载全服阵营数据
     *
     * @throws SQLException
     */
    private void loadAllServerCamp() throws SQLException {
        String sql = "select * from allservercamp";
        allServerCampMap = DBUtil.queryMap(DBUtil.DB_COMMON, "camptype", AllServerCampPo.class, sql);
        List<String> sqls = new ArrayList<>();
        if (allServerCampMap.size() == 0) {
            for (CampAtrVo campAtrVo : CampManager.campAtrMap.values()) {
                AllServerCampPo allServerCamp = new AllServerCampPo(campAtrVo.getType(), 0, 1, 0);
                allServerCamp.setInsertStatus();
                String changeSql = allServerCamp.getChangeSql();
                sqls.add(changeSql);
                allServerCampMap.put(campAtrVo.getType(), allServerCamp);
                allServerCamp.setDbStatus(DbRow.DB_SAVE);
            }
            DBUtil.execBatch(DBUtil.DB_COMMON, true, sqls);
        }
    }


    /**
     * 安装定时调度器
     */
    private void setupScheduler() {
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SCHEDULE_KEY_PUSH_CAMP_ROLE_NUM, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.campRemoteMainService().caculateCampRoleNum();
                ServiceHelper.campRemoteMainService().notifyAllServerUpdateCampData();
                ServiceHelper.campRemoteMainService().notityAllServerRank();
                ServiceHelper.campRemoteMainService().notifyAllServerRareOfficer(false);
                ServiceHelper.campRemoteMainService().notifyAllServerCityData();
            }
        }, 4L, 5, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SCHEDULE_KEY_SORT, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.campRemoteMainService().sort();
            }
        }, 2L, 2, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SCHEDULE_KEY_AUTO_SAVE, new Runnable() {
            @Override
            public void run() {
                LogUtil.info("begin------------------------------------");
                for (Map.Entry entry : cityRankMap.entrySet()) {
                    int rank = 1;
                    TreeSet<CampRoleReputationRankPo> treeSet = cityRankMap.get(entry.getKey());
                    Iterator<CampRoleReputationRankPo> iterator = treeSet.iterator();
                    while (iterator.hasNext()) {
                        CampRoleReputationRankPo roleReputationRankPo = iterator.next();
                        roleReputationRankPo.setRank(rank);
                        rank++;
                        LogUtil.info(roleReputationRankPo.getCampType() + "|" + entry.getKey() + "|" + roleReputationRankPo.getServerId() + "|" + roleReputationRankPo.getRoleId() + "|" + roleReputationRankPo.getName() + "|" + roleReputationRankPo.getCommonOfficerId() + "|" + roleReputationRankPo.getFightScore() + "|" + roleReputationRankPo.getRank() + "|" + roleReputationRankPo.getReputation());
                    }
                }
                LogUtil.info("end------------------------------------");
                for (Map.Entry entry : cityRankMap.entrySet()) {
                    LogUtil.info("cityId:{},rank size:{}", entry.getKey(), cityRankMap.get(entry.getKey()).size());
                }
            }
        }, 0, 1, TimeUnit.HOURS);
        Map<Integer, String> cronMap = new HashMap<>();
        cronMap.put(1, CampManager.campCityResetMoment.getCron());
        cronMap.put(2, "0 0 * * * ?");
        try {
            campRareOfficerFlow.init(SchedulerHelper.getScheduler(), cronMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("定时授予稀有官职任务初始化失败", e);
        }
    }

    /**
     * 通知各服最新阵营数据
     */
    @Override
    public void notifyAllServerUpdateCampData() {
        for (int serverId : serverCampRoleNumMap.keySet()) {
            try {
                CampRpcHelper.campLocalMainService().pushAllServerCampRoleNum(serverId, allServerCampMap, campTypeScale);
            } catch (Exception exception) {
                LogUtil.error(exception.getMessage(), exception);
            }
        }
    }

    /**
     * 计算阵营角色数量
     */
    @Override
    public void caculateCampRoleNum() {
        Map<Integer, Integer> serverRoleNumMap = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : serverCampRoleNumMap.entrySet()) {
            Map<Integer, Integer> innerCampRoleNumMap = entry.getValue();
            for (Map.Entry<Integer, Integer> innerEntry : innerCampRoleNumMap.entrySet()) {
                Integer oldNum = serverRoleNumMap.get(innerEntry.getKey());
                if (oldNum == null) {
                    oldNum = 0;
                    serverRoleNumMap.put(innerEntry.getKey(), 0);
                }
                serverRoleNumMap.put(innerEntry.getKey(), oldNum + innerEntry.getValue());
            }
        }
        List<String> sqls = new ArrayList<>();
        for (int campType : CampManager.campTypeArr) {
            AllServerCampPo allServerCamp = allServerCampMap.get(campType);
            Integer roleNum = serverRoleNumMap.get(campType);
            if (roleNum == null) {
                continue;
            }
            allServerCamp.setRoleNum(roleNum);
            allServerCamp.setSaveStatus();
            allServerCamp.setUpdateStatus();
            sqls.add(allServerCamp.getChangeSql());
        }
        campTypeScale = getCurrentScale();
        try {
            DBUtil.execBatch(DBUtil.DB_COMMON, true, sqls);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shareCampTypeRoleNum(int serverId, Map<Integer, Integer> campRoleNumMap, Integer fromServer) {
        serverCampRoleNumMap.put(fromServer, campRoleNumMap);
    }

    /**
     * 捐献繁荣度
     *
     * @param campServerId
     * @param prosperousNumMap
     */
    @Override
    public void donateCampProsperous(Integer campServerId, Map<Integer, Integer> prosperousNumMap) {
        List<String> sqls = new ArrayList<>();
        boolean hasCanLevelUpCamp = false;
        for (Map.Entry<Integer, Integer> entry : prosperousNumMap.entrySet()) {
            AllServerCampPo allServerCamp = allServerCampMap.get(entry.getKey());
            boolean canLevelUp = allServerCamp.addProsperousNum(entry.getValue());
            if (canLevelUp) {
                hasCanLevelUpCamp = true;
                CampLevelUpEvent campLevelUpEvent = new CampLevelUpEvent(entry.getKey(), allServerCamp.getLevel());
                for (int serverId : serverCampRoleNumMap.keySet()) {
                    CampRpcHelper.campLocalMainService().handleCampLevelUp(serverId, campLevelUpEvent);
                }
            }
            allServerCamp.setSaveStatus();
            allServerCamp.setUpdateStatus();
            sqls.add(allServerCamp.getChangeSql());
        }
        if (hasCanLevelUpCamp) {
            notifyAllServerUpdateCampData();
        }
        try {
            if (sqls.size() > 0) {
                DBUtil.execBatch(DBUtil.DB_COMMON, true, sqls);
            }
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 获取当前阵营人数差值比例,当前阵营负载情况
     *
     * @return
     */
    @Override
    public CampTypeScale getCurrentScale() {
        CampTypeScale campTypeScale = new CampTypeScale();
        float scale = 0;
        Iterator<Map.Entry<Integer, AllServerCampPo>> iterator = allServerCampMap.entrySet().iterator();
        Map.Entry<Integer, AllServerCampPo> camp1Map = iterator.next();
        int campNum1 = camp1Map.getValue().getRoleNum();
        int lowCampType = camp1Map.getKey();
        Map.Entry<Integer, AllServerCampPo> camp2Map = iterator.next();
        int campNum2 = camp2Map.getValue().getRoleNum();
        /**
         * campNum1永远指少的阵营角色数量
         */
        if (campNum1 > campNum2) {
            int tmp = campNum1;
            campNum1 = campNum2;
            campNum2 = tmp;
            lowCampType = camp2Map.getKey();
        }
        if (campNum2 != 0) {
            scale = (float) ((campNum2 - campNum1) / (campNum2 * 1.0));//人数差值比例
        } else {
            scale = 0;
        }
        campTypeScale.setScale(scale);
        campTypeScale.setLowCampType(lowCampType);
        CampEquilibrium reasonableCampEquilibrium = null;
        int total = 0;
        for (AllServerCampPo allServerCamp : allServerCampMap.values()) {
            total += allServerCamp.getRoleNum();
        }
        if (total >= CampManager.campEquilibriumnum) {
            reasonableCampEquilibrium = CampManager.getReasonableCampEquilibrium(campTypeScale.getScale());
            if (reasonableCampEquilibrium != null) {
                LogUtil.info("camp lost balance:{}", campTypeScale.getLowCampType());
            }
        }
        campTypeScale.setCampEquilibrium(reasonableCampEquilibrium);
        return campTypeScale;
    }

    /**
     * 由各游戏服推送数据各服前50名，最终挑选全专区前50
     *
     * @param campServerId
     * @param map
     * @param gameServerId
     */
    @Override
    public void updateCampCityReputationRank(Integer campServerId, Map<Integer, List<AbstractRankPo>> map, Integer gameServerId) {
        for (List<AbstractRankPo> list : map.values()) {
            for (AbstractRankPo rankPo : list) {
                CampRoleReputationRankPo campRoleReputationRankPo = (CampRoleReputationRankPo) rankPo;
                updateCityRoleReputation(campRoleReputationRankPo);
            }
        }
    }

    /**
     * 更新角色声望排行榜
     *
     * @param roleReputationRankPo
     */
    public void updateCityRoleReputation(CampRoleReputationRankPo roleReputationRankPo) {
        TreeSet<CampRoleReputationRankPo> rankPoTreeSet = cityRankMap.get(roleReputationRankPo.getCityId());
        Map<Long, CampRoleReputationRankPo> reputationRankPoMap = roleReputationRankMap.get(roleReputationRankPo.getCityId());
        CampRoleReputationRankPo oldPo = reputationRankPoMap.get(roleReputationRankPo.getRoleId());
        CampRoleReputationRankPo newPo = (CampRoleReputationRankPo) roleReputationRankPo.copy();
        if (rankPoTreeSet.size() >= AVALIABLE_RANK_LIMIT) {// 排序容器已满
            CampRoleReputationRankPo last = rankPoTreeSet.last();
            if (oldPo != null && rankPoTreeSet.contains(oldPo)) {// 在容器内
                if (last.compareTo(newPo) >= 0) {// 大于等于末端节点,加入排序容器
                    addRankTreeSet(oldPo, newPo);
                } else {// 比末端节点小,移除容器内旧的
                    rankPoTreeSet.remove(oldPo);
                }
            } else if (last.compareTo(newPo) > 0) {// 不在容器内 && 比末端节点大,加入排序容器
                rankPoTreeSet.remove(last);
                newPo.setRank(RankConstant.RANK_INIT);
                addRankTreeSet(oldPo, newPo);
            }
        } else {// 容器未满,直接加入
            newPo.setRank(RankConstant.RANK_INIT);
            addRankTreeSet(oldPo, newPo);
        }
        updateCacheRankMap(newPo);
    }

    /**
     * 加入排序容器先移除原有的
     *
     * @param oldRankPo
     */
    protected void addRankTreeSet(CampRoleReputationRankPo oldRankPo, CampRoleReputationRankPo newRankPo) {
        TreeSet<CampRoleReputationRankPo> rankPoTreeSet = cityRankMap.get(newRankPo.getCityId());
        if (oldRankPo != null) {
            rankPoTreeSet.remove(oldRankPo);
        }
        rankPoTreeSet.add(newRankPo);
    }

    /**
     * 更新角色排名缓存
     *
     * @param roleReputationRankPo
     */
    protected void updateCacheRankMap(CampRoleReputationRankPo roleReputationRankPo) {
        Map<Long, CampRoleReputationRankPo> reputationRankPoMap = roleReputationRankMap.get(roleReputationRankPo.getCityId());
        reputationRankPoMap.put(roleReputationRankPo.getUniqueId(), roleReputationRankPo);
    }

    /**
     * 遍历容器,获得容器内准确排名
     */
    @Override
    public void sort() {

        for (Map.Entry entry : cityRankMap.entrySet()) {
            int rank = 1;
            TreeSet<CampRoleReputationRankPo> treeSet = cityRankMap.get(entry.getKey());
            Map<Long, CampRoleReputationRankPo> cacheRankMap = roleReputationRankMap.get(entry.getKey());
            Iterator<CampRoleReputationRankPo> iterator = treeSet.iterator();
            while (iterator.hasNext()) {
                CampRoleReputationRankPo roleReputationRankPo = iterator.next();
                roleReputationRankPo.setRank(rank);
                cacheRankMap.put(roleReputationRankPo.getUniqueId(), roleReputationRankPo);
                rank++;
            }
        }
    }

    /**
     * 推送全服稀有官职角色
     */
    @Override
    public void notifyAllServerRareOfficer(boolean reset) {
        for (int serverId : serverCampRoleNumMap.keySet()) {
            try {
                if (reset) {
                    CampRpcHelper.campLocalMainService().pushAllServerRareOfficerRoles(serverId, currentRoundroleRareOfficerMap, lastRoleReputationRankMap, reset);
                } else {
                    CampRpcHelper.campLocalMainService().pushAllServerRareOfficerRoles(serverId, currentRoundroleRareOfficerMap, null, reset);
                }
            } catch (Exception exception) {
                LogUtil.error(exception.getMessage(), exception);
            }
        }
    }

    /**
     * 重新授予稀有官职
     */
    @Override
    public void grantRareOfficer() {
        LogUtil.info("重新授予稀有官职");
        removeAllRareOfficer();
        currentRoundroleRareOfficerMap = new HashMap<>();
        lastRoleReputationRankMap = new HashMap<>();
        List<String> sqls = new ArrayList<>();
        for (CampCityVo campCityVo : CampManager.campCityVoList) {
            TreeSet<CampRoleReputationRankPo> treeSet = cityRankMap.get(campCityVo.getId());
            List<CampRoleReputationRankPo> campRoleReputationRankPoList = lastRoleReputationRankMap.get(campCityVo.getId());
            if (campRoleReputationRankPoList == null) {
                campRoleReputationRankPoList = new ArrayList<>(treeSet.size());
                lastRoleReputationRankMap.put(campCityVo.getId(), campRoleReputationRankPoList);
            }
            Iterator<CampRoleReputationRankPo> iterator = treeSet.iterator();
            int rank = 1;
            for (int index = 0; index < treeSet.size(); index++) {
                CampRoleReputationRankPo next = iterator.next();
                next.setRank(rank);
                /**
                 * 产生稀有官职角色
                 */
                if (index < campCityVo.getRareOfficerNum()) {
                    Map<Long, RareOfficerRolePo> roleRareMap = currentRoundroleRareOfficerMap.get(campCityVo.getId());
                    if (roleRareMap == null) {
                        roleRareMap = new HashMap<>();
                        currentRoundroleRareOfficerMap.put(campCityVo.getId(), roleRareMap);
                    }
                    CampRankAwardVo campRankAward = CampManager.getCampRankAward(campCityVo.getId(), rank);
                    RareOfficerRolePo rareOfficerRole = new RareOfficerRolePo(next, campRankAward.getRareoffice(), 0);
                    rareOfficerRole.setInsertStatus();
                    sqls.add(rareOfficerRole.getChangeSql());
                    roleRareMap.put(next.getRoleId(), rareOfficerRole);
                }
                /**
                 * 添加到最后名单
                 */
                campRoleReputationRankPoList.add(next);
                rank++;
            }
        }
        try {
            DBUtil.execBatch(DBUtil.DB_COMMON, false, sqls);
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
        notifyAllServerRareOfficer(true);
        initRankContainer();

    }

    /**
     * 收回所有稀有官职
     */
    private void removeAllRareOfficer() {
        LogUtil.info("移除上轮稀有官职数据");
        try {
            String sql = "delete from rareofficerrole;";
            DBUtil.execSql(DBUtil.DB_COMMON, sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取角色排名
     *
     * @param cityId
     * @param roleId
     * @return
     */
    public CampRoleReputationRankPo getRank(int cityId, long roleId) {
        Map<Long, CampRoleReputationRankPo> campRoleReputationRankPoMap = roleReputationRankMap.get(cityId);
        if (campRoleReputationRankPoMap == null) {
            return null;
        }
        CampRoleReputationRankPo campRoleReputationRankPo = campRoleReputationRankPoMap.get(roleId);
        return campRoleReputationRankPo;
    }

    /**
     * 移除指定成员
     *
     * @param campRoleReputationRankPo
     */
    public void removeRank(CampRoleReputationRankPo campRoleReputationRankPo) {
        Map<Long, CampRoleReputationRankPo> cacheRankMap = roleReputationRankMap.get(campRoleReputationRankPo.getCityId());
        cacheRankMap.remove(campRoleReputationRankPo.getRoleId());
        TreeSet<CampRoleReputationRankPo> treeSet = cityRankMap.get(campRoleReputationRankPo.getCityId());
        if (campRoleReputationRankPo != null && treeSet.contains(campRoleReputationRankPo)) {
            treeSet.remove(campRoleReputationRankPo);
        }
    }

    /**
     * 处理每日繁荣度的自动增幅
     */
    @Override
    public void dailyProperousAdd() {
        Map<Integer, Integer> prosperousMap = new HashMap<>();
        for (AllServerCampPo allServerCampPo : allServerCampMap.values()) {
            CampLevelVo campLevelVo = allServerCampPo.getCampLevelVo();
            prosperousMap.put(allServerCampPo.getCampType(), campLevelVo.getAddprosperous());
        }
        ServiceHelper.campRemoteMainService().donateCampProsperous(null, prosperousMap);
    }

    /**
     * 角色更换城池
     *
     * @param campServerId
     * @param campCityChangeEvent
     */
    @Override
    public void handleChangeCity(Integer campServerId, CampCityChangeEvent campCityChangeEvent) {
        try {
            LogUtil.info("处理换城池:{}", campCityChangeEvent.getRoleId());
            CampRoleReputationRankPo campRoleReputationRankPo = getRank(campCityChangeEvent.getOldCityId(), campCityChangeEvent.getRoleId());
            if (campRoleReputationRankPo != null) {
                removeRank(campRoleReputationRankPo);
                campRoleReputationRankPo.setCityId(campCityChangeEvent.getNewCityId());
                updateCityRoleReputation(campRoleReputationRankPo);
            }

        } catch (Exception e) {
            LogUtil.error("handleChangeCity fail", e);
        } finally {
            try {
                handleCityPlayerImageData(campCityChangeEvent);
            } catch (Exception e) {
                LogUtil.error("handleCityPlayerImageData fail", e);
            }
        }
    }

    /**
     * 将汇合排行榜传输给各个游戏服
     */
    @Override
    public void notityAllServerRank() {
        Map<Integer, Map<Long, CampRoleReputationRankPo>> sortedCityRankMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, TreeSet<CampRoleReputationRankPo>> entry : cityRankMap.entrySet()) {
            TreeSet<CampRoleReputationRankPo> treeSet = entry.getValue();
            Iterator<CampRoleReputationRankPo> iterator = treeSet.iterator();
            Integer cityId = entry.getKey();
            HashMap<Long, CampRoleReputationRankPo> theCityRoleRankMap = new HashMap<>();
            sortedCityRankMap.put(cityId, theCityRoleRankMap);
            int rank = 1;
            while (iterator.hasNext()) {
                CampRoleReputationRankPo campRoleReputationRankPo = iterator.next();
                campRoleReputationRankPo.setRank(rank);
                theCityRoleRankMap.put(campRoleReputationRankPo.getRoleId(), campRoleReputationRankPo);
                rank++;
                if (theCityRoleRankMap.size() >= AVALIABLE_RANK_LIMIT) {
                    break;
                }
            }
        }
        for (int serverId : serverCampRoleNumMap.keySet()) {
            try {
                CampRpcHelper.campLocalMainService().pushAllServerRank(serverId, sortedCityRankMap);
            } catch (Exception exception) {
                LogUtil.error(exception.getMessage(), exception);
            }
        }
    }

    /**
     * 镜像数据处理
     */
    private void handleCityPlayerImageData(CampCityChangeEvent campCityChangeEvent) {
        int oldCity = campCityChangeEvent.getOldCityId();
        int newCity = campCityChangeEvent.getNewCityId();
        long roleId = campCityChangeEvent.getRoleId();
        int serverId = campCityChangeEvent.getServerId();
        CampPlayerImageData newData = campCityChangeEvent.getCampPlayerImageData();
        Iterator<CampCityPlayer> iterator = campCityPlayerMap.values().iterator();
        for (; iterator.hasNext(); ) {
            CampCityPlayer campCityPlayer = iterator.next();
            campCityPlayer.getPlayerMap().remove(roleId);
        }
        CampCityPlayer campCityPlayer = campCityPlayerMap.get(newCity);
        Map<Long, Integer> playerMap = null;
        if (campCityPlayer == null) {
            campCityPlayer = new CampCityPlayer();
            campCityPlayer.setCityId(newCity);
            playerMap = new HashMap<>();
            campCityPlayer.setPlayerMap(playerMap);
            campCityPlayerMap.put(newCity, campCityPlayer);
        }
        playerMap = campCityPlayer.getPlayerMap();
        campCityPlayer.setTotalNum(campCityPlayer.getTotalNum() + 1);
        //数据更新处理
        StringBuffer sql = new StringBuffer();
        if (oldCity > 0) {
            CampCityPlayer oldCampCityPlayer = campCityPlayerMap.get(oldCity);
//            oldCampCityPlayer.setTotalNum(oldCampCityPlayer.getTotalNum()-1);
            String oldPlayerInfo = StringUtil.makeString(oldCampCityPlayer.getPlayerMap(), '+', '|');
            sql.append("insert into campcityplayer(cityid,playerinfo,totalnum) values(").append(oldCity).append(",'").append(oldPlayerInfo)
                    .append("',").append(oldCampCityPlayer.getTotalNum()).append(") on duplicate key update playerinfo=values(playerinfo),totalnum=values(totalnum);");
            try {
                DBUtil.execSql(DBUtil.DB_COMMON, sql.toString());
            } catch (Exception e) {
                LogUtil.error("update campcityplayer fail, sql:" + sql.toString(), e);
            }
        }

        if (playerMap.size() < CampManager.CITY_PLAYER_SAVE_NUM) {
            StringBuffer sql1 = new StringBuffer();
            playerMap.put(roleId, serverId);
            String playerInfo = StringUtil.makeString(playerMap, '+', '|');
            sql1.append("insert into campcityplayer(cityid,playerinfo,totalnum) values(").append(newCity).append(",'").append(playerInfo)
                    .append("',").append(campCityPlayer.getTotalNum()).append(") on duplicate key update playerinfo=values(playerinfo),totalnum=values(totalnum);");
            try {
                DBUtil.execSql(DBUtil.DB_COMMON, sql1.toString());
            } catch (Exception e) {
                LogUtil.error("update campcityplayer fail, sql:" + sql1.toString(), e);
            }
        }

        Map<Long, CampPlayerImageData> newMap = campCityFightMap.get(newCity);
        if (newMap == null) {
            newMap = new HashMap<>();
            campCityFightMap.put(newCity, newMap);
        }
        if (newMap.size() < 100) {
            newMap.put(roleId, newData);
        }
        if (oldCity > 0) {
            Map<Long, CampPlayerImageData> map = campCityFightMap.get(oldCity);
            if (map == null) {
                map = new HashMap<>();
                campCityFightMap.put(oldCity, map);
            }
            map.remove(roleId);
            checkAndAddPlayImage(map, oldCity);
            if (waitDataSet.contains(roleId)) {
                waitDataSet.remove(roleId);
                checkAndAddPlayImage(map, oldCity);
            }
        }
    }

    private void checkAndAddPlayImage(Map<Long, CampPlayerImageData> map, int oldCity) {
        if (map.size() < 100) {//补充镜像数据
            CampCityPlayer campCityPlayer = campCityPlayerMap.get(oldCity);
            Map<Long, Integer> playerMap = campCityPlayer.getPlayerMap();
            if (playerMap.size() >= 100) {
                List<Long> roleIdList = new ArrayList<>();
                for (long id : playerMap.keySet()) {
                    if (!map.containsKey(id)) {
                        roleIdList.add(id);
                        break;
                    }
                }
                int rand = RandomUtil.rand(0, roleIdList.size() - 1);
                long selectId = roleIdList.get(rand);
                Integer serverId = playerMap.get(selectId);
                //向游戏服请求对应玩家的镜像数据
                waitDataSet.add(selectId);
                CampRpcHelper.campLocalMainService().getPlayerImageData(serverId, selectId);
            }
        }
    }

    @Override
    public void addPlayerImageData(int serverId, List<CampPlayerImageData> dataList) {
        for (CampPlayerImageData data : dataList) {
            addPlayerImageData(serverId, data);
        }
    }

    /**
     * 游戏服返回玩家镜像数据
     */
    @Override
    public void addPlayerImageData(int serverId, CampPlayerImageData data) {
        int cityId = data.getCityId();
        Map<Long, CampPlayerImageData> map = campCityFightMap.get(cityId);
        if (map == null) {
            map = new HashMap<>();
            campCityFightMap.put(cityId, map);
        }
        map.put(data.getEntity().getRoleId(), data);
        waitDataSet.remove(data.getEntity().getRoleId());
    }

    /**
     * 同步阵营城池数据到游戏服 （镜像数据，驻点人数）
     */
    @Override
    public void notifyAllServerCityData() {
        Map<Integer, Integer> cityPlayerNumMap = new HashMap<Integer, Integer>();
        Iterator<Integer> iterator = campCityPlayerMap.keySet().iterator();
        int key = 0;
        int num = 0;
        for (; iterator.hasNext(); ) {
            key = iterator.next();
            CampCityPlayer campCityPlayer = campCityPlayerMap.get(key);
            if (campCityPlayer == null) {
                num = 0;
            } else {
                num = campCityPlayer.getTotalNum();
            }
            cityPlayerNumMap.put(key, num);
        }
        if (StringUtil.isEmpty(campCityFightMap)) {
            //向游戏服获取镜像数据
            Map<Integer, Set<Long>> serverRoleMap = new HashMap<>();
            long roleId = 0L;
            int roleServerId = 0;
            int rand = 0;
            Set<Long> playerSet = null;
            Map<Long, Integer> playerMap = null;
            for (CampCityPlayer campCityPlayer : campCityPlayerMap.values()) {
                playerMap = campCityPlayer.getPlayerMap();
                List<Long> randomList = new ArrayList<>(playerMap.keySet());
                int size = randomList.size();
                if (size >= 100) {
                    size = 100;
                }
                for (int i = 0; i < size; i++) {
                    rand = RandomUtil.rand(0, randomList.size() - 1);
                    roleId = randomList.remove(rand);
                    roleServerId = playerMap.get(roleId);
                    playerSet = serverRoleMap.get(roleServerId);
                    if (playerSet == null) {
                        playerSet = new HashSet<>();
                        serverRoleMap.put(roleServerId, playerSet);
                    }
                    playerSet.add(roleId);
                }
            }
            for (int serverId : serverCampRoleNumMap.keySet()) {
                Set<Long> set = serverRoleMap.get(serverId);
                if (StringUtil.isNotEmpty(set)) {
                    CampRpcHelper.campLocalMainService().getPlayerImageData(serverId, set);
                }
            }
        } else {
            Map<Integer, Map<Long, CampPlayerImageData>> tempCampCityFightMap = new HashMap<>(campCityFightMap);
            for (int serverId : serverCampRoleNumMap.keySet()) {
                try {
                    CampRpcHelper.campLocalMainService().pushCampCityFightMap(serverId, tempCampCityFightMap, cityPlayerNumMap);
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 处理转职改名的信息修改
     *
     * @param campServerId
     * @param roleInfoChangeEvent
     * @param gameServerId
     */
    @Override
    public void handleRoleInfoChange(Integer campServerId, RoleInfoChangeEvent roleInfoChangeEvent, Integer gameServerId) {
        List<String> sqls = new ArrayList<>();
        Map<Long, CampRoleReputationRankPo> campRoleReputationRankPoMap = roleReputationRankMap.get(roleInfoChangeEvent.getCityId());
        if (campRoleReputationRankPoMap != null) {
            CampRoleReputationRankPo campRoleReputationRankPo = campRoleReputationRankPoMap.get(roleInfoChangeEvent.getRoleId());
            if (campRoleReputationRankPo != null) {
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
                /**
                 * 排名系数，走更新排行榜逻辑
                 */
                if (roleInfoChangeEvent.getCommonOfficerId() != 0) {
                    updateCityRoleReputation(campRoleReputationRankPo);
                }
            }
        }
        Map<Long, RareOfficerRolePo> rareOfficerRolePoMap = currentRoundroleRareOfficerMap.get(roleInfoChangeEvent.getCityId());
        if (rareOfficerRolePoMap != null) {
            RareOfficerRolePo rareOfficerRole = rareOfficerRolePoMap.get(roleInfoChangeEvent.getRoleId());
            if (rareOfficerRole != null) {
                if (roleInfoChangeEvent.getNewName() != null) {
                    rareOfficerRole.setName(roleInfoChangeEvent.getNewName());
                }
                if (roleInfoChangeEvent.getNewJobId() != null) {
                    rareOfficerRole.setJobId(roleInfoChangeEvent.getNewJobId());
                }
                if (roleInfoChangeEvent.getRareOfficerId() != 0) {
                    rareOfficerRole.setRareOfficerId(roleInfoChangeEvent.getRareOfficerId());
                }
                rareOfficerRole.setSaveStatus();
                rareOfficerRole.setUpdateStatus();
                String changeSql = rareOfficerRole.getChangeSql();
                sqls.add(changeSql);
            }
        }

        try {
            DBUtil.execBatch(DBUtil.DB_COMMON, false, sqls);
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 执行远程gm命令
     *
     * @param campServerId
     */
    @Override
    public void remoteGm(int campServerId) {
        ServiceHelper.campRemoteMainService().grantRareOfficer();
    }

    @Override
    public void printState() {

    }
}
