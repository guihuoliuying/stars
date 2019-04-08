package com.stars.modules.familyactivities.war;

import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarEnterSafeSceneEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarFighterAddingSucceededEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarFightingOrNotEvent;
import com.stars.modules.familyactivities.war.gm.FamilyWarGmHandler;
import com.stars.modules.familyactivities.war.gm.FamilyWarQualifyingGmHandler;
import com.stars.modules.familyactivities.war.gm.FamilyWarRemoteGmHandler;
import com.stars.modules.familyactivities.war.listener.FamilyWarListener;
import com.stars.modules.familyactivities.war.prodata.FamilyWarMoraleVo;
import com.stars.modules.familyactivities.war.prodata.FamilyWarRankAwardVo;
import com.stars.modules.fightingmaster.event.FightReadyEvent;
import com.stars.modules.gm.GmManager;
import com.stars.modules.pk.event.BackCityEvent;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.event.FamilyWarRevivePayReqEvent;
import com.stars.multiserver.familywar.event.FamilyWarSendPacketEvent;
import com.stars.multiserver.familywar.event.FamilyWarSupportEvent;
import com.stars.services.activities.ActConst;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.*;

import static com.stars.modules.data.DataManager.*;
import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;

/**
 * Created by zhaowenshuo on 2016/11/22.
 */
public class FamilyActWarModuleFactory extends AbstractModuleFactory<FamilyActWarModule> {

    public FamilyActWarModuleFactory() {
        super(new FamilyActWarPacketSet());
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("fw", new FamilyWarGmHandler());
        GmManager.reg("fwq", new FamilyWarQualifyingGmHandler());
        GmManager.reg("fwr", new FamilyWarRemoteGmHandler());
    }

    @Override
    public FamilyActWarModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FamilyActWarModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        FamilyWarListener listener = new FamilyWarListener((FamilyActWarModule) module);

        eventDispatcher.reg(FightReadyEvent.class, listener);
        eventDispatcher.reg(FamilyWarFighterAddingSucceededEvent.class, listener);
        eventDispatcher.reg(BackCityEvent.class, new FamilyWarListener((FamilyActWarModule) module));
        eventDispatcher.reg(FamilyWarRevivePayReqEvent.class, new FamilyWarListener((FamilyActWarModule) module));
        eventDispatcher.reg(LoginSuccessEvent.class, new FamilyWarListener((FamilyActWarModule) module));
        eventDispatcher.reg(FamilyWarSupportEvent.class, new FamilyWarListener((FamilyActWarModule) module));
        eventDispatcher.reg(FamilyWarSendPacketEvent.class, new FamilyWarListener((FamilyActWarModule) module));
        eventDispatcher.reg(FamilyWarEnterSafeSceneEvent.class, new FamilyWarListener((FamilyActWarModule) module));
        eventDispatcher.reg(FamilyWarFightingOrNotEvent.class, new FamilyWarListener((FamilyActWarModule) module));
    }

    @Override
    public void loadProductData() throws Exception {

        numOfFighterInEliteFight = getCommConfig("familywar_fightmembercount", 5);

        towerTypeMap = StringUtil.toMap(getCommConfig("familywar_towertype"), Integer.class, Byte.class, '+', '|');

        /* 关卡 */
        stageIdOfEliteFight = MapUtil.getInt(commonConfigMap, "familywar_stageid", "\\+", 0, 9301);
        stageIdOfNormalFight = MapUtil.getInt(commonConfigMap, "familywar_stageid", "\\+", 1, 9302);
        stageIdOfStageFight = getCommConfig("familywar_pairmatchstageid", 9401);

        timeLimitOfNormalFight = getCommConfig("familywar_pairlasttime", 120); // 匹配战，战斗时长，秒

        /* 邮件 */
        emailTemplateIdOfAddingToTeamSheet = getCommConfig("familywar_fightmember", 10998);
        emailTemplateIdOfDeletingFromTeamSheet = getCommConfig("familywar_nofightmember", 10999);
        emailTemplateIdOfFamilyBeChosen = getCommConfig("familywar_statusemplateid", 10995);

        /* 积分相关 */
        coefficientA = MapUtil.getInt(commonConfigMap, "familywar_coefficient_a", "\\+", 0, 100);
        coefficientAA = MapUtil.getInt(commonConfigMap, "familywar_coefficient_a", "\\+", 1, 100);
        coefficientD = MapUtil.getInt(commonConfigMap, "familywar_coefficient_d", "\\+", 0, 100);
        coefficientDD = MapUtil.getInt(commonConfigMap, "familywar_coefficient_d", "\\+", 1, 100);
        coefficientB = getCommConfig("familywar_coefficient_b", 100);
        coefficientC = getCommConfig("familywar_coefficient_c", 100);
//        pointsDeltaOfKillFighter = coefficientA; // 转换一下
        pointsDeltaOfDestoryTower = getCommConfig("familywar_pointscore", 100);

        damageRatioThresholdOfPersonalPoints = getCommConfig("familywar_damagepercent", 10.0) / 100.0;
        killCountThresholdOfNotice = getCommConfig("familywar_killnotice", 1);

        /* 士气相关 */
        moraleDeltaOfDestoryTower = getCommConfig("familywar_pointmoraleadd", 100);
        moraleDeltaOfLosingTower = getCommConfig("familywar_pointmoralesub", 100);
        moraleDeltaOfKillFighterInEliteFight = coefficientB;
        moraleDeltaOfKillFighterInNormalFight = coefficientC;
//        moraleDeltaOfWinningNormalFight = getCommConfig("familywar_smallwinscore", 100);
        moraleVoList = loadMoraleVoList();

        // FIXME: 2017-05-24 这里要兼容一下
        timelineOfKnockout = generateKnockoutTimeline(DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL));
        remoteTimeLinePoint = generateRemoteTimeLine();
        timeLineOfRemote = generateTimeLineOfRemote(DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_REMOTE));
        /* 积分达标奖励 */
        eliteMinPointsAwardMap = loadMinPointsAward(getCommConfig("familywar_elitescoreaward")); // 精英
        normalMinPointsAwardMap = loadMinPointsAward(getCommConfig("familywar_scoreaward")); // 匹配
        eliteMinPointsAwardMapForQualify = loadMinPointsAward(getCommConfig("familywar_kuafu_elitescoreaward"));//跨服海选精英
        normalMinPointsAwardMapForQualify = loadMinPointsAward(getCommConfig("familywar_kuafu_scoreaward"));//跨服海选匹配
        eliteMinPointsAwardMapForRemote = loadMinPointsAward(getCommConfig("familywar_finals_elitescoreaward"));//跨服海选匹配
        normalMinPointsAwardMapForRemote = loadMinPointsAward(getCommConfig("familywar_finals_scoreaward"));//跨服海选匹配
        /* 家族排名奖励 */
        familyRankAwardVoMap = loadFamilyRankAwardMap();
        familyRankAwardVoMapByPeriod = loadFamilyRankAwardMapByPeriod();
        /* 每场（精英/匹配）奖励 */
        dropIdOfEliteFightWinAward = MapUtil.getInt(commonConfigMap, "familywar_dropaward", "\\+", 0, 1);
        dropIdOfEliteFightLoseAward = MapUtil.getInt(commonConfigMap, "familywar_dropaward", "\\+", 1, 1);
        dropIdOfEliteQualifyFightWinAward = MapUtil.getInt(commonConfigMap, "familywar_kuafudropaward", "\\+", 0, 1);
        dropIdOfEliteQualifyFightLoseAward = MapUtil.getInt(commonConfigMap, "familywar_kuafudropaward", "\\+", 1, 1);
        dropIdOfEliteRemoteFightWinAward = MapUtil.getInt(commonConfigMap, "familywar_finalsdropaward", "\\+", 0, 1);
        dropIdOfEliteRemoteFightLoseAward = MapUtil.getInt(commonConfigMap, "familywar_finalsdropaward", "\\+", 1, 1);
        dropIdOfNormalFightWinAward = MapUtil.getInt(commonConfigMap, "familywar_smallaward", "\\+", 0, 1);
        dropIdOfNormalFightLoseAward = MapUtil.getInt(commonConfigMap, "familywar_smallaward", "\\+", 1, 1);

        dropIdOfStageFightWinAward = getCommConfig("familywar_pairmatchaward", 1);
        /* 积分排名奖励 --> 在公共排行榜奖励里 */

        /* 普通战场匹配时间 */
        normalMatchInterval = MapUtil.getInt(commonConfigMap, "familywar_intervaltime", 20);

        timeOfNoramlFightWaiting = getCommConfig("familywar_loadingwaittime", 15);

        /* 复活 */
        homeReviveTime = getCommConfig("familywar_homeresurgence", 10);
        payReviveTime = getCommConfig("familywar_localresurgence", 3);
        totalPayRevive = MapUtil.getInt(commonConfigMap, "familywar_localrmbrescount", "\\+", 0, 5);
        revivePay = MapUtil.getInt(commonConfigMap, "familywar_localrmbrescount", "\\+", 1, 5);

        /* 点赞 */
        maxSupportCount = MapUtil.getInt(commonConfigMap, "familywar_praisecount", "\\+", 0, 10);
        dropIdOfSupportAward = MapUtil.getInt(commonConfigMap, "familywar_praisecount", "\\+", 1, 3);

        /** 家族战改版 */
        familywar_lasttime = getCommConfig("familywar_lasttime", 300);
        familywar_intervaltime = getCommConfig("familywar_eliteintervaltime", 30);
//        familywar_pairlasttime = getCommConfig("familywar_pairlasttime", 120);
        familywar_pairstageodd = getCommConfig("familywar_pairstageodd", 50);
        stageIdOfSafe = getCommConfig("familywar_fightsafearea", 903);
        familywar_smallwinscore = getCommConfig("familywar_smallwinscore", 300);
        familywar_smallfailscore = getCommConfig("familywar_smalllosescore", 50);
        familywar_smallpairscore = getCommConfig("familywar_smallpairscore", 100);
        String tmpCycletime = getCommConfig("familywar_cycletime");
        String[] tmpCycletimeStr = tmpCycletime.split("\\+");
        familywar_cycletime_min = Integer.parseInt(tmpCycletimeStr[0]);
        familywar_cycletime_max = Integer.parseInt(tmpCycletimeStr[1]);
        String tmpPairWinScore = getCommConfig("familywar_pairwinscore");
        String[] tmpPairWinScoreStr = tmpPairWinScore.split("\\+");
        familywar_pvpwinscore = Integer.parseInt(tmpPairWinScoreStr[0]);
        familywar_pvewinscore = Integer.parseInt(tmpPairWinScoreStr[1]);
        String tmpEliteWinScore = getCommConfig("familywar_elitewinscore");
        String[] tmpEliteWinScoreStr = tmpEliteWinScore.split("\\+");
        familywar_elitewinscore = Integer.parseInt(tmpEliteWinScoreStr[0]);
        familywar_elitefailscore = Integer.parseInt(tmpEliteWinScoreStr[1]);
        familywar_roll_winer = getGametext("familywar_roll_winer");
        familywar_roll_winer2 = getGametext("familywar_roll_winer2");
        familywar_roll_winer3 = getGametext("familywar_roll_winer3");
        String familywar_coefficient_z = getCommConfig("familywar_coefficient_z");
        String[] tmpFamilyWar_coefficient = familywar_coefficient_z.split("\\+");
        familywar_coefficient_hp = Integer.parseInt(tmpFamilyWar_coefficient[0]);
        familywar_coefficient_attack = Integer.parseInt(tmpFamilyWar_coefficient[1]);
        familywar_coefficient_defense = Integer.parseInt(tmpFamilyWar_coefficient[2]);
        familywar_coefficient_hit = Integer.parseInt(tmpFamilyWar_coefficient[3]);
        familywar_coefficient_avoid = Integer.parseInt(tmpFamilyWar_coefficient[4]);
        familywar_coefficient_crit = Integer.parseInt(tmpFamilyWar_coefficient[5]);
        familywar_coefficient_anticrit = Integer.parseInt(tmpFamilyWar_coefficient[6]);
        String familywar_coefficient_zzs = getCommConfig("familywar_coefficient_zz");
        String[] familywar_coefficient_zz = familywar_coefficient_zzs.split("\\+");
        familywar_coefficient_hp_zz = Integer.parseInt(familywar_coefficient_zz[0]);
        familywar_coefficient_attack_zz = Integer.parseInt(familywar_coefficient_zz[1]);
        familywar_coefficient_defense_zz = Integer.parseInt(familywar_coefficient_zz[2]);
        familywar_coefficient_hit_zz = Integer.parseInt(familywar_coefficient_zz[3]);
        familywar_coefficient_avoid_zz = Integer.parseInt(familywar_coefficient_zz[4]);
        familywar_coefficient_crit_zz = Integer.parseInt(familywar_coefficient_zz[5]);
        familywar_coefficient_anticrit_zz = Integer.parseInt(familywar_coefficient_zz[6]);
        String familywar_coefficient_zzzs = getCommConfig("familywar_coefficient_zzz");
        String[] familywar_coefficient_zzz = familywar_coefficient_zzzs.split("\\+");
        familywar_coefficient_hp_zzz = Integer.parseInt(familywar_coefficient_zzz[0]);
        familywar_coefficient_attack_zzz = Integer.parseInt(familywar_coefficient_zzz[1]);
        familywar_coefficient_defense_zzz = Integer.parseInt(familywar_coefficient_zzz[2]);
        familywar_coefficient_hit_zzz = Integer.parseInt(familywar_coefficient_zzz[3]);
        familywar_coefficient_avoid_zzz = Integer.parseInt(familywar_coefficient_zzz[4]);
        familywar_coefficient_crit_zzz = Integer.parseInt(familywar_coefficient_zzz[5]);
        familywar_coefficient_anticrit_zzz = Integer.parseInt(familywar_coefficient_zzz[6]);
        familywar_score_elitewin = getCommConfig("familywar_score_elitewin", 20);
        familywar_score_pairwin = getCommConfig("familywar_score_pairwin", 20);
        familywar_score_pvewin = getCommConfig("familywar_score_pvewin", 20);
        String familywar_universelist = getCommConfig("familywar_universelist");
        generateServerSize(familywar_universelist);
    }

    private void generateServerSize(String listStr) {
        Map<Integer, Integer> serverSizeMap = new HashMap<>();
        String[] tmp = listStr.split("\\|");
        for (String tmp1 : tmp) {
            String[] tmp2 = tmp1.split("\\+");
            int minValue = Integer.parseInt(tmp2[0]);
            int serverSize = Integer.parseInt(tmp2[1]);
            serverSizeMap.put(minValue, serverSize);
        }
        FamilyActWarManager.serverSizeMap = serverSizeMap;
    }

    private List<Integer> generateKnockoutTimeline(Map<Integer, String> knockoutFlow) {
        List<Integer> timeline = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (Integer pointIndex : timeLinePoint) {
            String cronExpr = knockoutFlow.get(pointIndex);
            timeline.add((int) (ActivityFlowUtil.getTimeInMillisByCronExprByWeek(cronExpr) / 1000));
        }

        return timeline;
    }

    private Map<Integer, List<Integer>> generateTimeLineOfRemote(Map<Integer, String> remoteFlow) {
        Map<Integer, List<Integer>> tmpMap = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : remoteTimeLinePoint.entrySet()) {
            List<Integer> tmpList = tmpMap.get(entry.getKey());
            if (tmpList == null) {
                tmpList = new ArrayList<>();
                tmpMap.put(entry.getKey(), tmpList);
            }
            for (Integer pointIndex : entry.getValue()) {
                String cronExpr = remoteFlow.get(pointIndex);
                tmpList.add((int) (ActivityFlowUtil.getTimeInMillisByCronExprByWeek(cronExpr) / 1000));
            }
        }
        return tmpMap;
    }

    private Map<Integer, List<Integer>> generateRemoteTimeLine() {
        Map<Integer, List<Integer>> tmpMap = new HashMap<>();
        tmpMap.put(FamilyWarConst.R_BATTLE_TYPE_32TO16, new ArrayList<Integer>());
        tmpMap.put(FamilyWarConst.R_BATTLE_TYPE_16TO8, new ArrayList<Integer>());
        tmpMap.put(FamilyWarConst.R_BATTLE_TYPE_8TO4, new ArrayList<Integer>());
        List<Integer> _32TO16List = tmpMap.get(FamilyWarConst.R_BATTLE_TYPE_32TO16);
        _32TO16List.add(1);
        _32TO16List.add(4);
        _32TO16List.add(8);
        _32TO16List.add(12);
        _32TO16List.add(16);
        _32TO16List.add(20);
        _32TO16List.add(22);
        List<Integer> _16TO8List = tmpMap.get(FamilyWarConst.R_BATTLE_TYPE_16TO8);
        _16TO8List.add(1);
        _16TO8List.add(2);
        _16TO8List.add(4);
        _16TO8List.add(8);
        _16TO8List.add(12);
        _16TO8List.add(16);
        _16TO8List.add(18);
        List<Integer> _8TO4List = tmpMap.get(FamilyWarConst.R_BATTLE_TYPE_8TO4);
        _8TO4List.add(1);
        _8TO4List.add(2);
        _8TO4List.add(4);
        _8TO4List.add(8);
        _8TO4List.add(12);
        _8TO4List.add(14);
        return tmpMap;
    }


    // "40+1+1|80+2+1" --> 积分40获得数量为1的itemId为1的道具，积分80获得数量为1的itemId为2的道具
    private Map<Long, Map<Integer, Integer>> loadMinPointsAward(String minPointsAwardStr) throws Exception {
        List<String> list = StringUtil.toArrayList(minPointsAwardStr, String.class, '|');
        Map<Long, Map<Integer, Integer>> map = new HashMap<>();
        for (String s : list) {
            int[] a = StringUtil.toArray(s, int[].class, '+');
            Map<Integer, Integer> toolMap = new HashMap<>();
            toolMap.put(a[1], a[2]);
            map.put((long) a[0], toolMap);
        }
        return map;
    }

    private Map<Integer, FamilyWarRankAwardVo> loadFamilyRankAwardMap() throws SQLException {
        List<FamilyWarRankAwardVo> tempList = DBUtil.queryList(
                DBUtil.DB_PRODUCT, FamilyWarRankAwardVo.class, "select * from `familywaraward`");
        Map<Integer, FamilyWarRankAwardVo> tempMap = DBUtil.queryMap(
                DBUtil.DB_PRODUCT, "id", FamilyWarRankAwardVo.class, "select * from `familywaraward`");
        if (tempList.size() != tempMap.size()) {
            throw new IllegalStateException("familywaraward表，id字段有重复");
        }
        return tempMap;
    }

    private Map<Integer, List<FamilyWarRankAwardVo>> loadFamilyRankAwardMapByPeriod() throws SQLException {
        List<FamilyWarRankAwardVo> tempList = DBUtil.queryList(
                DBUtil.DB_PRODUCT, FamilyWarRankAwardVo.class, "select * from `familywaraward`");
        Map<Integer, List<FamilyWarRankAwardVo>> tempMap = new HashMap<>();
        for (FamilyWarRankAwardVo vo : tempList) {
            List<FamilyWarRankAwardVo> l = tempMap.get(vo.getPeriod());
            if (l == null) {
                tempMap.put(vo.getPeriod(), l = new ArrayList<>());
            }
            l.add(vo);
        }
        return tempMap;
    }

    private List<FamilyWarMoraleVo> loadMoraleVoList() throws SQLException {
        return DBUtil.queryList(DBUtil.DB_PRODUCT, FamilyWarMoraleVo.class, "select * from `familywarmorale`");
    }

}
