package com.stars.modules.camp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.handler.CampCityTeamHandler;
import com.stars.modules.camp.event.*;
import com.stars.modules.camp.gm.CampFightGmHandler;
import com.stars.modules.camp.gm.CampGmHandler;
import com.stars.modules.camp.listenner.*;
import com.stars.modules.camp.pojo.CampCityResetMoment;
import com.stars.modules.camp.pojo.CampEquilibrium;
import com.stars.modules.camp.pojo.CampFightOfficerAttr;
import com.stars.modules.camp.pojo.CampFightSingleScoreReward;
import com.stars.modules.camp.prodata.*;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class CampModuleFactory extends AbstractModuleFactory<CampModule> {
    public CampModuleFactory() {
        super(new CampPackset());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from campatr;";
        Map<Integer, CampAtrVo> campAtrMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "type", CampAtrVo.class, sql);
        sql = "select * from camplevel;";
        List<CampLevelVo> campLevelList = DBUtil.queryList(DBUtil.DB_PRODUCT, CampLevelVo.class, sql);
        sql = "select * from campcity;";
        Map<Integer, CampCityVo> campCityMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", CampCityVo.class, sql);
        sql = "select * from commonofficer;";
        Map<Integer, CommonOfficerVo> commonOfficerMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", CommonOfficerVo.class, sql);
        sql = "select * from rareofficer;";
        Map<Integer, RareOfficerVo> rareOfficerMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", RareOfficerVo.class, sql);
        sql = "select * from designateofficer;";
        Map<Integer, DesignateOfficerVo> designateOfficerMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", DesignateOfficerVo.class, sql);
        sql = "select * from camprankaward;";
        List<CampRankAwardVo> campRankAwardList = DBUtil.queryList(DBUtil.DB_PRODUCT, CampRankAwardVo.class, sql);
        sql = "select * from campactivity;";
        Map<Integer, CampActivityVo> campActivityMap = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "id", CampActivityVo.class, sql);
        sql = "select * from campmission;";
        Map<Integer, CampMissionVo> campMissionMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", CampMissionVo.class, sql);
        Integer campEquilibriumnum = DataManager.getCommConfig("camp_equilibriumnum", 0);
        String campEquilibrium = DataManager.getCommConfig("camp_equilibrium");
        CampManager.campDonateUnit = DataManager.getCommConfig("camp_donate", 0);
        CampManager.CampDonateMaxCount = DataManager.getCommConfig("camp_goldnum", 0);
        String[] campEquilibriumGroup = campEquilibrium.split("\\|");
        List<CampEquilibrium> campEquilibriumList = new ArrayList<>();
        for (String campEquilibriumParam : campEquilibriumGroup) {
            CampEquilibrium campEquilibriumObj = CampEquilibrium.parse(campEquilibriumParam);
            campEquilibriumList.add(campEquilibriumObj);
        }
        /**
         * 构建阵营城市map
         */
        Map<Integer, List<CampCityVo>> campCityListMap = new HashMap<>();
        Map<Integer, Map<Integer, CampCityVo>> campCityLvListMap = new HashMap<>();
        for (CampCityVo campCityVo : campCityMap.values()) {
            List<CampCityVo> campCityVos = campCityListMap.get(campCityVo.getCamptype());
            Map<Integer, CampCityVo> campLevelCityMap = campCityLvListMap.get(campCityVo.getCamptype());
            if (campCityVos == null) {
                campCityVos = new ArrayList<>();
                campCityListMap.put(campCityVo.getCamptype(), campCityVos);
            }
            if (campLevelCityMap == null) {
                campLevelCityMap = new HashMap<>();
                campCityLvListMap.put(campCityVo.getCamptype(), campLevelCityMap);
            }
            campLevelCityMap.put(campCityVo.getLevel(), campCityVo);
            campCityVos.add(campCityVo);
        }
        for (List<CampCityVo> list : campCityListMap.values()) {
            Collections.sort(list);
        }
        List<CampCityVo> campCityVoList = new ArrayList<>(campCityMap.values());
        /**
         * 构建阵营等级map
         */
        Map<Integer, Map<Integer, CampLevelVo>> campTypeLevelMap = new HashMap<>();
        for (CampLevelVo campLevelVo : campLevelList) {
            Map<Integer, CampLevelVo> campLevelVoMap = campTypeLevelMap.get(campLevelVo.getCamptype());
            if (campLevelVoMap == null) {
                campLevelVoMap = new HashMap<>();
                campTypeLevelMap.put(campLevelVo.getCamptype(), campLevelVoMap);
            }
            campLevelVoMap.put(campLevelVo.getLevel(), campLevelVo);
        }
        /**
         * 构建普通官职map
         */
        List<CommonOfficerVo> commonOfficerVoList = new ArrayList<>(commonOfficerMap.values());
        Map<Integer, CommonOfficerVo> levelCommonOfficerVoMap = new HashMap<>();
        for (CommonOfficerVo commonOfficerVo : commonOfficerVoList) {
            levelCommonOfficerVoMap.put(commonOfficerVo.getLevel(), commonOfficerVo);
        }
        Collections.sort(commonOfficerVoList);
        /**
         * 构建声望排行榜奖励
         */
        Map<Integer, List<CampRankAwardVo>> campRankArardMap = new HashMap<>();
        for (CampRankAwardVo campRankAwardVo : campRankAwardList) {
            List<CampRankAwardVo> campRankAwardVos = campRankArardMap.get(campRankAwardVo.getType());
            if (campRankAwardVos == null) {
                campRankAwardVos = new ArrayList<>();
                campRankArardMap.put(campRankAwardVo.getType(), campRankAwardVos);
            }
            campRankAwardVos.add(campRankAwardVo);
        }
        CampManager.campCityLvListMap = campCityLvListMap;
        CampManager.commonOfficerVoList = commonOfficerVoList;
        CampManager.campCityListMap = campCityListMap;
        CampManager.campTypeLevelMap = campTypeLevelMap;
        CampManager.campAddLimitNum = (float) DataManager.getCommConfig("camp_addlimitnum", 0) / 1000.0f;
        String campAddrandom = DataManager.getCommConfig("camp_addrandom");
        String[] campAddrandomGroup = campAddrandom.split("\\|");
        CampManager.randomEnterCampConst = Integer.parseInt(campAddrandomGroup[0]);
        Map<Integer, Integer> randomEnterReward = StringUtil.toMap(campAddrandomGroup[1], Integer.class, Integer.class, '+', ',');
        Iterator<Integer> iterator = campAtrMap.keySet().iterator();
        for (int index = 0; index < campAtrMap.size(); index++) {
            CampManager.campTypeArr[index] = iterator.next();
        }
        String campCitymonent = DataManager.getCommConfig("camp_citymonent");
        CampCityResetMoment campCityResetMoment = CampCityResetMoment.parse(campCitymonent);
        CampManager.campCityResetMoment = campCityResetMoment;
        CampManager.randomEnterReward = randomEnterReward;
        CampManager.campEquilibriumList = campEquilibriumList;
        CampManager.campEquilibriumnum = campEquilibriumnum;
        CampManager.campMissionMap = campMissionMap;
        CampManager.campMissionList = new ArrayList<>(campMissionMap.values());
        CampManager.campActivityMap = campActivityMap;
        CampManager.campActivityList = new ArrayList<>(campActivityMap.values());
        CampManager.campRankArardMap = campRankArardMap;
        CampManager.designateOfficerMap = designateOfficerMap;
        CampManager.rareOfficerMap = rareOfficerMap;
        CampManager.commonOfficerMap = commonOfficerMap;
        CampManager.campCityMap = campCityMap;
        CampManager.campCityVoList = campCityVoList;
        CampManager.campAtrMap = campAtrMap;
        CampManager.levelCommonOfficerVoMap = levelCommonOfficerVoMap;

        String cityLvLimit = DataManager.getCommConfig("camp_activityone_citylvlimit");
        CampManager.cityLvLimit = StringUtil.toArray(cityLvLimit, int[].class, '|');
        String offlinepvprobot = DataManager.getCommConfig("camp_activityone_offlinepvprobot");
        CampManager.robotFightValue = StringUtil.toArray(offlinepvprobot, int[].class, '+');
        String commonofficermark = DataManager.getCommConfig("camp_activityone_commonofficermark");
        CampManager.commonOfficerMark = toArrayList(commonofficermark);
        String[] commonMarkStrs = commonofficermark.split("[|]");
        List<int[]> commonOfficerMark = new ArrayList<>();
        for (String commonMarkStr : commonMarkStrs) {
            int[] arr = StringUtil.toArray(commonMarkStr, int[].class, '+');
            if (arr != null) {
                commonOfficerMark.add(arr);
            }
        }
        CampManager.commonOfficerMark = commonOfficerMark;
        String rareofficermark = DataManager.getCommConfig("camp_activityone_rareofficermark");
        CampManager.rareOfficerMark = toArrayList(rareofficermark);
        String mybornpoint = DataManager.getCommConfig("camp_activityone_mybornpoint");
        CampManager.myPosition = StringUtil.toArray(mybornpoint, String[].class, '|');
        String enemybornpoint = DataManager.getCommConfig("camp_activityone_enemybornpoint");
        CampManager.enemyPosition = StringUtil.toArray(enemybornpoint, String[].class, '|');
        CampManager.Camp_Fight_DungeonId = DataManager.getCommConfig("camp_activityone_dungeonid", 0);
        String finishaward = DataManager.getCommConfig("camp_activityone_finishaward");
        String[] awardStrs = finishaward.split("[|]");
        CampManager.winFinishAward = StringUtil.toMap(awardStrs[0], Integer.class, Integer.class, '+', ',');
        CampManager.loseFinishAward = StringUtil.toMap(awardStrs[1], Integer.class, Integer.class, '+', ',');
        String markaward = DataManager.getCommConfig("camp_activityone_markaward");
        CampManager.markAwardList = toArrayList(markaward);
        String citylevelmark = DataManager.getCommConfig("camp_activityone_citylevelmark");
        String[] citylevelmarkStr = citylevelmark.split("[|]");
        CampManager.highCityLevelMark = StringUtil.toMap(citylevelmarkStr[0], Integer.class, Integer.class, '+', ',');
        CampManager.lowCityLevelMark = StringUtil.toMap(citylevelmarkStr[1], Integer.class, Integer.class, '+', ',');
        String awardscale = DataManager.getCommConfig("camp_activityone_awardscale");
        CampManager.awardScaleList = toArrayList(awardscale);
        CampManager.Max_Scale_Cha_Num = CampManager.awardScaleList.get(CampManager.awardScaleList.size() - 1)[1];
        String groupmark = DataManager.getCommConfig("camp_activityone_groupmark");
        CampManager.teamMark = StringUtil.toArray(groupmark, int[].class, '+');
        String matchtimeStr = DataManager.getCommConfig("camp_activityone_matchtime");
        CampManager.matchTime = StringUtil.toArray(matchtimeStr, int[].class, '+');
        CampManager.Cha_CityPlayNum_Limit = DataManager.getCommConfig("camp_activityone_citylvnum", 1);
        /**
         * 齐楚大作战
         */
        String campActivity2Scoreaward = DataManager.getCommConfig("camp_activity2_scoreaward");
        String[] groups = campActivity2Scoreaward.split("\\|");
        Map<Integer, Map<Integer, Integer>> campActivity2ScoreMap = new LinkedHashMap<>();
        for (String group : groups) {
            String[] items = group.split("\\+");
            Map<Integer, Integer> reward = new HashMap<>();
            reward.put(Integer.parseInt(items[1]), Integer.parseInt(items[2]));
            campActivity2ScoreMap.put(Integer.parseInt(items[0]), reward);
        }
        CampManager.campActivity2ScoreMap = campActivity2ScoreMap;

        CampManager.campActivity2RoomLowNum = DataManager.getCommConfig("camp_activity2_roomlownum",2);
        CampManager.campActivity2Mergetime = DataManager.getCommConfig("camp_activity2_mergetime",10);
        CampManager.campActivity2Matchtime = DataManager.getCommConfig("camp_activity2_matchtime",5);
        CampManager.campActivity2MatchNum = DataManager.getCommConfig("camp_activity2_matchnum",4);
        Map<Integer, CampGrade> campGradeMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", CampGrade.class, "select * from campgrade;");
        CampManager.setCampGradeMap(campGradeMap);
        CampManager.campSkillMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", CampSkill.class, "select * from campskill;");
        String campActivity2OfficerAttr = DataManager.getCommConfig("camp_activity2_officerattr");
        String[] campActivity2OfficerAttrGroups = campActivity2OfficerAttr.split("\\|");
        List<CampFightOfficerAttr> campFightOfficerAttrs = new ArrayList<>();
        for (String group : campActivity2OfficerAttrGroups) {
            String[] items = group.split("\\+");
            int min = Integer.parseInt(items[0]);
            int max = Integer.parseInt(items[1]);
            int scale = Integer.parseInt(items[2]);
            String img = items[3];
            campFightOfficerAttrs.add(new CampFightOfficerAttr(min, max, scale, img));
        }
        CampManager.campFightOfficerAttrs = campFightOfficerAttrs;
        String campActivity2SingleScoreAward = DataManager.getCommConfig("camp_activity2_singlescoreaward");
        String[] campActivity2SingleScoreAwardGroups = campActivity2SingleScoreAward.split("\\|");
        List<CampFightSingleScoreReward> CampFightSingleScoreRewards = new ArrayList<>();
        for (String group : campActivity2SingleScoreAwardGroups) {
            String[] items = group.split("\\+");
            int score = Integer.parseInt(items[0]);
            int itemId = Integer.parseInt(items[1]);
            int num = Integer.parseInt(items[2]);
            Map<Integer, Integer> reward = new HashMap<>();
            reward.put(itemId, num);
            CampFightSingleScoreReward campFightSingleScoreReward = new CampFightSingleScoreReward(score, reward);
            CampFightSingleScoreRewards.add(campFightSingleScoreReward);
        }
        CampManager.campFightSingleScoreRewards = CampFightSingleScoreRewards;
        Map<Integer, Integer> campActivity2Defendaward;
        try {
            campActivity2Defendaward = StringUtil.toMap(DataManager.getCommConfig("camp_activity2_defendaward"), Integer.class, Integer.class, '+', '|');
        } catch (NullPointerException e) {
            campActivity2Defendaward = new HashMap<>();
        }
        CampManager.campActivity2Defendaward = campActivity2Defendaward;
        CampManager.campActivity2MatchMaxTime = DataManager.getCommConfig("camp_activity2_matchmaxtime", 0);
        CampManager.campActivity2Positions = StringUtil.toArrayList(DataManager.getCommConfig("camp_activity2_position"), String.class, '|');
        CampManager.campActivity2MaxSingleScoreTime = DataManager.getCommConfig("camp_activity2_maxsinglescoretime", 0);
        CampManager.campActivity2AiAttr = DataManager.getCommConfig("camp_activity2_aiattr", 1000);
        CampManager.campActivity2RoomExpelTime = DataManager.getCommConfig("camp_activity2_room_expel_time", 40);
        CampManager.campActivity2RoomDestroyTime = DataManager.getCommConfig("camp_activity2_room_destroy_time", 55);
    }

    private List<int[]> toArrayList(String data) throws Exception {
        String[] strs = data.split("[|]");
        List<int[]> list = new ArrayList<>();
        for (String str : strs) {
            int[] arr = StringUtil.toArray(str, int[].class, '+');
            if (arr != null) {
                list.add(arr);
            }
        }
        return list;
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("camp", new CampGmHandler());
        GmManager.reg("campfight", new CampFightGmHandler());
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_CAMPCITYFIGHT, CampCityTeamHandler.class);
    }

    @Override
    public CampModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new CampModule("阵营", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        CampLevelUpListenner campLevelUpListenner = new CampLevelUpListenner((CampModule) module);
        RoleOfficerListenner roleOfficerListenner = new RoleOfficerListenner((CampModule) module);
        RoleChangeListenner roleChangeListenner = new RoleChangeListenner((CampModule) module);
        ActivityOrMissionListenner activityOrMissionListenner = new ActivityOrMissionListenner((CampModule) module);
        OfficerChangeListenner officerChangeListenner = new OfficerChangeListenner((CampModule) module);
        CampCityFightListener campCityFightListener = new CampCityFightListener((CampModule) module);
        CampListener campListener = new CampListener((CampModule) module);
        eventDispatcher.reg(CampLevelUpEvent.class, campLevelUpListenner);
        eventDispatcher.reg(AddReputationEvent.class, new ReputationListenner((CampModule) module));
        eventDispatcher.reg(AddProsperousEvent.class, new ProperousListenner((CampModule) module));
        eventDispatcher.reg(RareOfficerResetEvent.class, roleOfficerListenner);
        eventDispatcher.reg(CampCityChangeEvent.class, new CampCityChangeListenner((CampModule) module));
        eventDispatcher.reg(ChangeJobEvent.class, roleChangeListenner);
        eventDispatcher.reg(RoleRenameEvent.class, roleChangeListenner);
        eventDispatcher.reg(ActivityFinishEvent.class, activityOrMissionListenner);
        eventDispatcher.reg(MissionFinishEvent.class, activityOrMissionListenner);
        eventDispatcher.reg(CampCityFightEvent.class, campCityFightListener);
        eventDispatcher.reg(OfficerChangeEvent.class, officerChangeListenner);
        eventDispatcher.reg(AddToolEvent.class, campListener);
        eventDispatcher.reg(CampFightEvent.class, campListener);
    }
}
