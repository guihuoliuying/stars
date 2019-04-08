package com.stars.multiserver.familywar;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.prodata.BuddyinfoVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.multiserver.familywar.flow.FamilyWarKnockoutFlow;
import com.stars.multiserver.familywar.flow.FamilyWarQualifyingFlow;
import com.stars.multiserver.familywar.flow.FamilyWarRemoteFlow;
import com.stars.services.activities.ActConst;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/12/28.
 */
public class FamilyWarUtil {
    public static List<Integer> localStepList = new ArrayList<>();
    public static List<Integer> qualifyingStepList = new ArrayList<>();
    public static List<Integer> remoteStepList = new ArrayList<>();

    public static Map<Integer, SkillVo> getAllRoleSkillVoMap() {
        Map<Integer, Job> jobMap = RoleManager.jobMap;
        Map<Integer, SkillVo> skillVoMap = new HashMap<>();
        for (Map.Entry<Integer, Job> entry : jobMap.entrySet()) {
            Job job = entry.getValue();
            Resource res = RoleManager.getResourceById(job.getModelres());
            List<Integer> skillVoList = res.getSkillList();
            for (int i = 0, len = skillVoList.size(); i < len; i++) {
                SkillVo skillVo = SkillManager.getSkillVo(skillVoList.get(i));
                skillVoMap.put(skillVo.getSkillid(), skillVo);
            }
        }
        return skillVoMap;
    }

    public static Map<Integer, SkillVo> getBuddySkillVoMap(int... buddyIdArray) {
        Map<Integer, SkillVo> skillVoMap = new HashMap<>();
        for (int buddyId : buddyIdArray) {
            BuddyinfoVo vo = BuddyManager.getBuddyinfoVo(buddyId);
            List<Integer> skillIdList = null;
            try {
                skillIdList = StringUtil.toArrayList(vo.getShowskill(), Integer.class, '+');
            } catch (Exception e) {
                LogUtil.error("FamilyWarUtil.getBuddySkillVoMap", e);
            }
            if (skillIdList != null) {
                for (int i = 0, len = skillIdList.size(); i < len; i++) {
                    SkillVo skillVo = SkillManager.getSkillVo(skillIdList.get(i));
                    skillVoMap.put(skillVo.getSkillid(), skillVo);
                }
            }
        }
        return skillVoMap;
    }

    public static boolean isRobot(FighterEntity entity) {
        return entity.getFighterType() == FighterEntity.TYPE_ROBOT
                || entity.getFighterType() == FighterEntity.TYPE_MONSTER;
    }

    public static boolean isPlayer(FighterEntity entity) {
        return entity.getFighterType() == FighterEntity.TYPE_SELF
                || entity.getFighterType() == FighterEntity.TYPE_PLAYER;
    }

    public static int getMainIconStateByLocalFlowStep() {
        int iconState = ActivityFlow.STEP_START_CHECK;
        switch (FamilyWarConst.STEP_OF_SUB_FLOW) {
            case FamilyWarKnockoutFlow.STEP_START_KNOCKOUT:
                iconState = FamilyWarConst.STATE_NOTICE_MASTER;
                break;
            case FamilyWarKnockoutFlow.STEP_BEFORE_QUARTER_FIANLS:
            case FamilyWarKnockoutFlow.STEP_BEFORE_SEMI_FIANLS:
            case FamilyWarKnockoutFlow.STEP_BEFORE_END_FIANLS:
                iconState = FamilyWarConst.STATE_START;
                break;
            case FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS:
            case FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS:
            case FamilyWarKnockoutFlow.STEP_START_FINALS:
                iconState = FamilyWarConst.STATE_UNDERWAY;
                break;
            case FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS:
            case FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS:
            case FamilyWarKnockoutFlow.STEP_END_FINALS:
                iconState = FamilyWarConst.STATE_END;
                break;
            case FamilyWarKnockoutFlow.STEP_END_KNOCKOUT:
            case FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_SEMI:
            case FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINALS:
                iconState = FamilyWarConst.STATE_ICON_DISAPPEAR;
                break;
            default:
                break;
        }
        return iconState;
    }

    public static int getMainIconStateByQualifyingFlowStep() {
        int iconState = ActivityFlow.STEP_START_CHECK;
        switch (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW) {
            case FamilyWarQualifyingFlow.STEP_START_QUALIFYING:
                iconState = FamilyWarConst.STATE_NOTICE_MASTER;
                break;
            case FamilyWarQualifyingFlow.STEP_BEFORE_1ST:
            case FamilyWarQualifyingFlow.STEP_BEFORE_2ND:
            case FamilyWarQualifyingFlow.STEP_BEFORE_3RD:
            case FamilyWarQualifyingFlow.STEP_BEFORE_4TH:
            case FamilyWarQualifyingFlow.STEP_BEFORE_5TH:
                iconState = FamilyWarConst.STATE_START;
                break;
            case FamilyWarQualifyingFlow.STEP_START_1ST:
            case FamilyWarQualifyingFlow.STEP_START_2ND:
            case FamilyWarQualifyingFlow.STEP_START_3RD:
            case FamilyWarQualifyingFlow.STEP_START_4TH:
            case FamilyWarQualifyingFlow.STEP_START_5TH:
                iconState = FamilyWarConst.STATE_UNDERWAY;
                break;
            case FamilyWarQualifyingFlow.STEP_END_1ST:
            case FamilyWarQualifyingFlow.STEP_END_2ND:
            case FamilyWarQualifyingFlow.STEP_END_3RD:
            case FamilyWarQualifyingFlow.STEP_END_4TH:
            case FamilyWarQualifyingFlow.STEP_END_5TH:
                iconState = FamilyWarConst.STATE_END;
                break;
            case FamilyWarQualifyingFlow.STEP_END_QUALIFYING:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_2ND:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_3RD:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TH:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_5TH:
                iconState = FamilyWarConst.STATE_ICON_DISAPPEAR;
                break;
            default:
                break;
        }
        return iconState;
    }

    public static int getMainIconStateByRemoteFlowStep() {
        int iconState = ActivityFlow.STEP_START_CHECK;
        switch (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW) {
            case FamilyWarRemoteFlow.STEP_START_REMOTE:
                iconState = FamilyWarConst.STATE_NOTICE_MASTER;
                break;
            case FamilyWarRemoteFlow.STEP_BEFORE_32TO16:
            case FamilyWarRemoteFlow.STEP_BEFORE_16TO8:
            case FamilyWarRemoteFlow.STEP_BEFORE_8TO4:
            case FamilyWarRemoteFlow.STEP_BEFORE_4TO2:
            case FamilyWarRemoteFlow.STEP_BEFORE_FINNAL:
                iconState = FamilyWarConst.STATE_START;
                break;
            case FamilyWarRemoteFlow.STEP_START_32TO16:
            case FamilyWarRemoteFlow.STEP_START_16TO8:
            case FamilyWarRemoteFlow.STEP_START_8TO4:
            case FamilyWarRemoteFlow.STEP_START_4TO2:
            case FamilyWarRemoteFlow.STEP_START_FINNAL:
                iconState = FamilyWarConst.STATE_UNDERWAY;
                break;
            case FamilyWarRemoteFlow.STEP_END_32TO16:
            case FamilyWarRemoteFlow.STEP_END_16TO8:
            case FamilyWarRemoteFlow.STEP_END_8TO4:
            case FamilyWarRemoteFlow.STEP_END_4TO2:
            case FamilyWarRemoteFlow.STEP_END_FINNAL:
                iconState = FamilyWarConst.STATE_END;
                break;
            case FamilyWarRemoteFlow.STEP_END_REMOTE:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_16TO8:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_8TO4:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TO2:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINNAL:
                iconState = FamilyWarConst.STATE_ICON_DISAPPEAR;
                break;
            default:
                break;
        }
        return iconState;
    }

    private static long getLocalNextBattleTimeL() {
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL);
        long nextBattleTime = 0;
        int nextStep = -1;
        if (FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS
                || FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS) {
            nextStep = FamilyWarConst.STEP_OF_SUB_FLOW + 4;
        } else if (FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS
                || FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS) {
            nextStep = FamilyWarConst.STEP_OF_SUB_FLOW + 3;
        }
        if (FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_START_FINALS
                || FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_END_FINALS) {
            return nextBattleTime;
        }
        if (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(nextStep)) {
            nextBattleTime = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(nextStep));
        }
        return nextBattleTime;
    }

    private static long getQualifyingNextBattleTimeL() {
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_QUALIFYING);
        long nextBattleTime = 0L;
        int nextStep = -1;
        switch (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW) {
            case FamilyWarQualifyingFlow.STEP_START_1ST:
            case FamilyWarQualifyingFlow.STEP_START_2ND:
            case FamilyWarQualifyingFlow.STEP_START_3RD:
            case FamilyWarQualifyingFlow.STEP_START_4TH:
                nextStep = FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW + 4;
                break;
            case FamilyWarQualifyingFlow.STEP_END_1ST:
            case FamilyWarQualifyingFlow.STEP_END_2ND:
            case FamilyWarQualifyingFlow.STEP_END_3RD:
            case FamilyWarQualifyingFlow.STEP_END_4TH:
                nextStep = FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW + 3;
                break;
            default:
                return nextBattleTime;
        }
        if (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(nextStep)) {
            nextBattleTime = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(nextStep));
        }
        return nextBattleTime;
    }

    private static long getRemoteNextBattleTimeL() {
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_REMOTE);
        long nextBattleTime = 0L;
        int nextStep = -1;
        switch (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW) {
            case FamilyWarRemoteFlow.STEP_START_32TO16:
            case FamilyWarRemoteFlow.STEP_START_16TO8:
            case FamilyWarRemoteFlow.STEP_START_8TO4:
            case FamilyWarRemoteFlow.STEP_START_4TO2:
                nextStep = FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW + 4;
                break;
            case FamilyWarRemoteFlow.STEP_END_32TO16:
            case FamilyWarRemoteFlow.STEP_END_16TO8:
            case FamilyWarRemoteFlow.STEP_END_8TO4:
            case FamilyWarRemoteFlow.STEP_END_4TO2:
                nextStep = FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW + 3;
                break;
            default:
                return nextBattleTime;
        }
        if (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(nextStep)) {
            nextBattleTime = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(nextStep));
        }
        return nextBattleTime;
    }

    public static long getNextBattleTimeL(int actConst) {
        if (actConst == ActConst.ID_FAMILY_WAR_LOCAL) {
            return getLocalNextBattleTimeL();
        } else if (actConst == ActConst.ID_FAMILY_WAR_QUALIFYING) {
            return getQualifyingNextBattleTimeL();
        } else {
            return getRemoteNextBattleTimeL();
        }
    }

    /**
     * 获得最近一场比赛的步数
     *
     * @return
     */
    private static int getLocalNearBattleStep() {
        int nextStep = -1;
        if (FamilyWarConst.STEP_OF_SUB_FLOW < FamilyWarKnockoutFlow.STEP_START_FINALS) {
            nextStep = FamilyWarKnockoutFlow.STEP_START_FINALS;
        }
        if (FamilyWarConst.STEP_OF_SUB_FLOW < FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS) {
            nextStep = FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS;
        }
        if (FamilyWarConst.STEP_OF_SUB_FLOW < FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS) {
            nextStep = FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS;
        }
        LogUtil.info("familywar|本服赛程，获得最近一场比赛的步数,当前步数:{},下一场的步数:{}", FamilyWarConst.STEP_OF_SUB_FLOW, nextStep);
        return nextStep;
    }

    private static int getQualifyingNearBattleStep() {
        int nextStep = -1;
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_START_5TH) {
            nextStep = FamilyWarQualifyingFlow.STEP_START_5TH;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_START_4TH) {
            nextStep = FamilyWarQualifyingFlow.STEP_START_4TH;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_START_3RD) {
            nextStep = FamilyWarQualifyingFlow.STEP_START_3RD;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_START_2ND) {
            nextStep = FamilyWarQualifyingFlow.STEP_START_2ND;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_START_1ST) {
            nextStep = FamilyWarQualifyingFlow.STEP_START_1ST;
        }
        LogUtil.info("familywar|跨服海选，获得最近一场比赛的步数,当前步数:{},下一场的步数:{}", FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW, nextStep);
        return nextStep;
    }

    private static int getRemoteNearBattleStep() {
        int nextStep = -1;
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_START_FINNAL) {
            nextStep = FamilyWarRemoteFlow.STEP_START_FINNAL;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_START_4TO2) {
            nextStep = FamilyWarRemoteFlow.STEP_START_4TO2;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_START_8TO4) {
            nextStep = FamilyWarRemoteFlow.STEP_START_8TO4;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_START_16TO8) {
            nextStep = FamilyWarRemoteFlow.STEP_START_16TO8;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_START_32TO16) {
            nextStep = FamilyWarRemoteFlow.STEP_START_32TO16;
        }
        LogUtil.info("familywar|跨服决赛，获得最近一场比赛的步数,当前步数:{},下一场的步数:{}", FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW, nextStep);
        return nextStep;
    }

    private static int getNearBattleStep(int actConst) {
        if (actConst == ActConst.ID_FAMILY_WAR_LOCAL) {
            return getLocalNearBattleStep();
        } else if (actConst == ActConst.ID_FAMILY_WAR_QUALIFYING) {
            return getQualifyingNearBattleStep();
        } else {
            return getRemoteNearBattleStep();
        }
    }

    private static int getLocalNearBattleEndStep() {
        int nextStep = -1;
        if (FamilyWarConst.STEP_OF_SUB_FLOW < FamilyWarKnockoutFlow.STEP_END_FINALS) {
            nextStep = FamilyWarKnockoutFlow.STEP_END_FINALS;
        }
        if (FamilyWarConst.STEP_OF_SUB_FLOW < FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS) {
            nextStep = FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS;
        }
        if (FamilyWarConst.STEP_OF_SUB_FLOW < FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS) {
            nextStep = FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS;
        }
        LogUtil.info("familywar|获得最近一场比赛结束的步数,当前步数:{},下一场的步数:{}", FamilyWarConst.STEP_OF_SUB_FLOW, nextStep);
        return nextStep;
    }

    private static int getQualifyingNearBattleEndStep() {
        int nextStep = -1;
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_END_5TH) {
            nextStep = FamilyWarQualifyingFlow.STEP_END_5TH;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_END_4TH) {
            nextStep = FamilyWarQualifyingFlow.STEP_END_4TH;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_END_3RD) {
            nextStep = FamilyWarQualifyingFlow.STEP_END_3RD;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_END_2ND) {
            nextStep = FamilyWarQualifyingFlow.STEP_END_2ND;
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW < FamilyWarQualifyingFlow.STEP_END_1ST) {
            nextStep = FamilyWarQualifyingFlow.STEP_END_1ST;
        }
        return nextStep;
    }

    private static int getRemoteNearBattleEndStep() {
        int nextStep = -1;
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_END_FINNAL) {
            nextStep = FamilyWarRemoteFlow.STEP_END_FINNAL;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_END_4TO2) {
            nextStep = FamilyWarRemoteFlow.STEP_END_4TO2;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_END_8TO4) {
            nextStep = FamilyWarRemoteFlow.STEP_END_8TO4;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_END_16TO8) {
            nextStep = FamilyWarRemoteFlow.STEP_END_16TO8;
        }
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW < FamilyWarRemoteFlow.STEP_END_32TO16) {
            nextStep = FamilyWarRemoteFlow.STEP_END_32TO16;
        }
        return nextStep;
    }

    private static int getNearBattleEndStep(int actConst) {
        if (actConst == ActConst.ID_FAMILY_WAR_LOCAL) {
            return getLocalNearBattleEndStep();
        } else if (actConst == ActConst.ID_FAMILY_WAR_QUALIFYING) {
            return getQualifyingNearBattleEndStep();
        } else {
            return getRemoteNearBattleEndStep();
        }
    }

    private static int getLocalPreStep() {
        long now = System.currentTimeMillis();
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL);
        if (flowMap == null || flowMap.isEmpty())
            return -1;
        List<Integer> minStepList = new ArrayList<>();
        for (Integer step : localStepList) {
            if (now > ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(step))) {
                minStepList.add(step);
            }
        }
        return Collections.max(minStepList);
    }

    private static int getQualifyingPreStep() {
        long now = System.currentTimeMillis();
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_QUALIFYING);
        if (flowMap == null || flowMap.isEmpty())
            return -1;
        List<Integer> minStepList = new ArrayList<>();
        for (Integer step : qualifyingStepList) {
            if (now > ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(step))) {
                minStepList.add(step);
            }
        }
        LogUtil.info("familywar:{}", minStepList);
        return Collections.max(minStepList);
    }

    private static int getRemotePreStep() {
        long now = System.currentTimeMillis();
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_REMOTE);
        if (flowMap == null || flowMap.isEmpty())
            return -1;
        List<Integer> minStepList = new ArrayList<>();
        for (Integer step : remoteStepList) {
            if (now > ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(step))) {
                minStepList.add(step);
            }
        }
        return Collections.max(minStepList);
    }

    public static int getPreStep(int actConst) {
        if (actConst == ActConst.ID_FAMILY_WAR_LOCAL) {
            return getLocalPreStep();
        } else if (actConst == ActConst.ID_FAMILY_WAR_QUALIFYING) {
            return getQualifyingPreStep();
        } else {
            return getRemotePreStep();
        }
    }


    /**
     * 获得最近一场比赛的开始时间
     *
     * @return
     */
    public static long getNearBattleTimeL(int actConst) {
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(actConst);
        long beginTime = 0;
        int nextStep = getNearBattleStep(actConst);
        if (nextStep != -1 && (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(nextStep))) {
            beginTime = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(nextStep));
        }
        LogUtil.info("familywar|L获得最近一场比赛的开始时间 :{}", beginTime);
        return beginTime;
    }

    public static String getBattleTimeStr(int step, int actConst) {
        String time = "";
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(actConst);
        if (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(step)) {
            time = ActivityFlowUtil.getHHmmStr(flowMap.get(step));
        }
        return time;
    }

    public static long getBattleTimeL(int step, int actConst) {
        long time = 0;
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(actConst);
        if (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(step)) {
            time = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(step));
        }
        return time;
    }

    public static long getBattleTimeLByType(int battleType) {
        if (battleType == FamilyWarConst.Q_BATTLE_TYPE_1ST) {
            return getBattleTimeL(FamilyWarQualifyingFlow.STEP_START_1ST, ActConst.ID_FAMILY_WAR_QUALIFYING);
        } else if (battleType == FamilyWarConst.Q_BATTLE_TYPE_2ND) {
            return getBattleTimeL(FamilyWarQualifyingFlow.STEP_START_2ND, ActConst.ID_FAMILY_WAR_QUALIFYING);
        } else if (battleType == FamilyWarConst.Q_BATTLE_TYPE_3RD) {
            return getBattleTimeL(FamilyWarQualifyingFlow.STEP_START_3RD, ActConst.ID_FAMILY_WAR_QUALIFYING);
        } else if (battleType == FamilyWarConst.Q_BATTLE_TYPE_4TH) {
            return getBattleTimeL(FamilyWarQualifyingFlow.STEP_START_4TH, ActConst.ID_FAMILY_WAR_QUALIFYING);
        } else if (battleType == FamilyWarConst.Q_BATTLE_TYPE_5Th) {
            return getBattleTimeL(FamilyWarQualifyingFlow.STEP_START_5TH, ActConst.ID_FAMILY_WAR_QUALIFYING);
        } else {
            return 0L;
        }
    }

    /**
     * 获得最近一场比赛的开始时间
     *
     * @return
     */
    public static String getNearBattleTimeStr(int actConst) {
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(actConst);
        String nextBattleTime = "";
        int nextStep = getNearBattleStep(actConst);
        if (nextStep != -1 && (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(nextStep))) {
            nextBattleTime = ActivityFlowUtil.getHHmmssStr(flowMap.get(nextStep));
        }
        LogUtil.info("familywar|Str获得最近一场比赛的开始时间 :{}", nextBattleTime);
        return nextBattleTime;
    }

    /**
     * 获得最近一场比赛的结束时间
     *
     * @return
     */
    public static long getNearBattleEndTimeL(int actConst) {
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(actConst);
        long nextBattleEndTime = 0;
        int nextEndStep = getNearBattleEndStep(actConst);
        if (nextEndStep != -1 && (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(nextEndStep))) {
            nextBattleEndTime = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(nextEndStep));
        }
        LogUtil.info("familywar|获得最近一场比赛的结束时间 :{}", nextBattleEndTime);
        return nextBattleEndTime;
    }

    public static String getFixtureName(int fix) {
        switch (fix) {
            case 1:
                return "八强赛";
            case 2:
                return "半决赛";
            case 3:
                return "决赛";
            case 4:
                return "季殿赛";
            default:
                return "";
        }
    }

    public static int getNextBattleType(int preBattleType, int size) {
        if (preBattleType == FamilyWarConst.R_BATTLE_TYPE_INIT) {
            if (size == 32) {
                return FamilyWarConst.R_BATTLE_TYPE_32TO16;
            } else if (size == 16) {
                return FamilyWarConst.R_BATTLE_TYPE_16TO8;
            } else if (size == 8) {
                return FamilyWarConst.R_BATTLE_TYPE_8TO4;
            } else if (size == 4) {
                return FamilyWarConst.R_BATTLE_TYPE_4TO2;
            } else {
                return FamilyWarConst.R_BATTLE_TYPE_FINAL;
            }
        } else if (preBattleType == FamilyWarConst.R_BATTLE_TYPE_32TO16) {
            return FamilyWarConst.R_BATTLE_TYPE_16TO8;
        } else if (preBattleType == FamilyWarConst.R_BATTLE_TYPE_16TO8) {
            return FamilyWarConst.R_BATTLE_TYPE_8TO4;
        } else if (preBattleType == FamilyWarConst.R_BATTLE_TYPE_8TO4) {
            return FamilyWarConst.R_BATTLE_TYPE_4TO2;
        } else if (preBattleType == FamilyWarConst.R_BATTLE_TYPE_4TO2) {
            return FamilyWarConst.R_BATTLE_TYPE_FINAL;
        } else {
            return FamilyWarConst.R_BATTLE_TYPE_OVER;
        }
    }

    public static int getPreBattleType(int thisBattleType) {
        switch (thisBattleType) {
            case FamilyWarConst.R_BATTLE_TYPE_16TO8:
                return FamilyWarConst.R_BATTLE_TYPE_32TO16;
            case FamilyWarConst.R_BATTLE_TYPE_8TO4:
                return FamilyWarConst.R_BATTLE_TYPE_16TO8;
            default:
                return -1;
        }
    }

    public static String getBattleTypeName(int battleType) {
        switch (battleType) {
            case FamilyWarConst.R_BATTLE_TYPE_32TO16:
                return "三十二强";
            case FamilyWarConst.R_BATTLE_TYPE_16TO8:
                return "十六强";
            case FamilyWarConst.R_BATTLE_TYPE_8TO4:
                return "八强";
            case FamilyWarConst.R_BATTLE_TYPE_4TO2:
                return "半决赛";
            case FamilyWarConst.R_BATTLE_TYPE_3RD4TH:
                return "季殿赛";
            case FamilyWarConst.R_BATTLE_TYPE_FINAL:
                return "决赛";
            default:
                return "";
        }
    }

    public static int getFamilyWarServerId() {
        BootstrapConfig config = ServerManager.getServer().getConfig();
        Properties props = config.getProps().get("familywar");
        return Integer.parseInt(props.getProperty("serverId"));
    }

    public static int getMainServerId() {
        BootstrapConfig config = ServerManager.getServer().getConfig();
        Properties props = config.getProps().get("mainServer");
        return Integer.parseInt(props.getProperty("serverId"));
    }

    public static Map<String, FighterEntity> getMonsterFighterEntity(int stageId) {
        Map<String, FighterEntity> retMap = new HashMap<>();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            retMap.putAll(spawnMonster(stageId, monsterSpawnId));
        }
        return retMap;
    }

    private static Map<String, FighterEntity> spawnMonster(int stageId, int monsterSpawnId) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            LogUtil.error("familywar|找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            resultMap.put(monsterUniqueId, monsterEntity);
        }
        return resultMap;
    }

    private static String getSpawnUId(int spawnId) {
        return Integer.toString(spawnId);
    }

    private static String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

}
