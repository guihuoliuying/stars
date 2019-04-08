package com.stars.services.family.activities.treasure;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.SystemRecordMap;
import com.stars.core.dao.DbRowDao;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.treasure.FamilyTreasureConst;
import com.stars.modules.familyactivities.treasure.FamilyTreasureManager;
import com.stars.modules.familyactivities.treasure.event.FamilyTreasureStageEvent;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.family.activities.treasure.userdata.FamilyTreasure;
import com.stars.services.family.activities.treasure.userdata.RoleTreasureDamageRankPo;
import com.stars.services.family.main.FamilyData;
import com.stars.services.family.main.userdata.FamilyPo;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyTreasureRankPo;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/10 14:05
 */
public class FamilyTreasureActor extends ServiceActor implements FamilyTreasureService {
    private static boolean isNormalStarted = false;
    private static boolean isSundayStarted = false;
    private FamilyTreasureFlow flow;
    private Map<Long, FamilyTreasure> ftpList;//<familyId,FamilyTreasureProcess>
    private Map<Long, Map<Long, RoleTreasureDamageRankPo>> damageRankMap;//familyId--<roleId,rankPo>
    private DbRowDao dao;

    @Override
    public void init() throws Throwable {
        dao = new DbRowDao(SConst.FamilyTreasureService);
        ServiceSystem.getOrAdd(SConst.FamilyTreasureService, this);
        ftpList = new HashMap<>();
        damageRankMap = new HashMap<>();
        flow = new FamilyTreasureFlow();
        synchronized (FamilyTreasureActor.class) {
            flow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_TREASURE));
//            loadRoleTreasureDamgeRank();付费测内容
        }
    }

    @Override
    public void printState() {

    }

    /**
     * 上线
     *
     * @param roleId
     * @param familyId
     */
    @Override
    public void online(long roleId, long familyId) {
        FamilyTreasure ftp = ftpList.get(familyId);
        if (ftp == null) {
            try {
                ftp = DBUtil.queryBean(DBUtil.DB_USER, FamilyTreasure.class, "select * from familytreasure where familyid = " + familyId);
                if (ftp == null) {
                    ftp = new FamilyTreasure();
                    ftp.setFamilyId(familyId);
                    ftp.setDamage(0);
                    ftp.setTotalDamage(0);
                    ftp.setLevel(FamilyTreasureManager.getMinLevel());
                    ftp.setStep(FamilyTreasureManager.getMinStepByLevel(FamilyTreasureManager.getMinLevel()));
                    ftp.setLastResetTimestamp(System.currentTimeMillis());
                    dao.insert(ftp);
                }
                ftpList.put(familyId, ftp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (ftp != null) {
            //判断重置时间
            long lastResetTimestamp = SystemRecordMap.familyTreasureResetTimestamp;
            if (ftp.getLastResetTimestamp() < lastResetTimestamp) {
                //进入重置
                ftp.setStep(FamilyTreasureManager.getMinStepByLevel(ftp.getLevel()));
                ftp.setDamage(0);
                ftp.setTotalDamage(0);
                ftp.setLastResetTimestamp(lastResetTimestamp);
                dao.update(ftp);
                updateRank(familyId, false);
            }
        }
        processAndDamageEvent(familyId, roleId, false);
    }

    /**
     * 更新探宝进度
     *
     * @param familyId
     * @param level
     * @param step
     */
    @Override
    public void updateProcess(long familyId, int level, int step, boolean isOver) {
        FamilyTreasure ftp = ftpList.get(familyId);
        ftp.setLevel(level);
        ftp.setStep(step);
        if (!isOver) {
            ftp.setDamage(0);
        }
        dao.update(ftp);
        processAndDamageEvent(familyId, false);
        updateRank(familyId, isOver);
    }

    /**
     * 更新伤害值
     *
     * @param familyId
     * @param damage
     */
    @Override
    public void updateDamage(long familyId, long damage) {
        FamilyTreasure ftp = ftpList.get(familyId);
        ftp.damageInc(damage);
        ftp.totalDamageInc(damage);
        dao.update(ftp);
        processAndDamageEvent(familyId, false);
        updateRank(familyId, false);
    }

    @Override
    public void save() {
        dao.flush();
    }

    /**
     * @param familyId
     * @return
     */
    @Override
    public long getFamilyTreasureDamage(long familyId) {
        return ftpList.get(familyId).getDamage();
    }


    /**
     * 事件派发
     *
     * @param familyId
     */
    private void processAndDamageEvent(long familyId, boolean resetDamage) {
        ServiceHelper.familyMainService().sendEventToOnlineMember(familyId, getEvent(familyId, true, resetDamage));
    }

    /**
     * 事件派发
     *
     * @param familyId
     */
    private void processAndDamageEvent(long familyId, long roleId, boolean resetDamage) {
        ServiceHelper.roleService().notice(roleId, getEvent(familyId, false, resetDamage));
    }


    private FamilyTreasureStageEvent getEvent(long familyId, boolean flush, boolean resetDamage) {
        AbstractRankPo abstractRank = ServiceHelper.rankService().getRank(RankConstant.RANKID_FAMILYTREASURE, familyId);
        FamilyTreasureStageEvent ftse = new FamilyTreasureStageEvent();
        ftse.setLevel(ftpList.get(familyId).getLevel());
        ftse.setStep(ftpList.get(familyId).getStep());
        ftse.setDamage(ftpList.get(familyId).getDamage());
        ftse.setTotalDamage(ftpList.get(familyId).getTotalDamage());
        ftse.setRank(abstractRank == null ? -1 : abstractRank.getRank());
        ftse.setStartType(isNormalStarted ? FamilyTreasureConst.NORMAL_TREASURE : FamilyTreasureConst.SUNDAY_TREASURE);
        ftse.setFlushToClient(flush);
        ftse.setResetDamage(resetDamage);
        return ftse;
    }

    /**
     * 更新家族探宝排行榜
     *
     * @param familyId
     */
    private void updateRank(long familyId, boolean isOver) {
        FamilyData data = ServiceHelper.familyMainService().getFamilyDataClone(familyId);
        FamilyPo familyPo = data.getFamilyPo();
        FamilyTreasure familyTreasure = ftpList.get(familyId);
        ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_FAMILY_TREASURE,
                new FamilyTreasureRankPo(familyId, familyPo.getName(), familyPo.getMasterName(),
                        familyTreasure.getLevel(), familyTreasure.getStep(), familyTreasure.getTotalDamage(), isOver));
    }

    @Override
    public void offline(long familyId) {
        ftpList.remove(familyId);
    }

    @Override
    public void start(int type, boolean startServer) {
        if (FamilyTreasureConst.NORMAL_TREASURE == type) {
            isNormalStarted = true;
            isSundayStarted = false;
            if (!startServer) {
                reset();
            }
        }
        if (FamilyTreasureConst.SUNDAY_TREASURE == type) {
            isSundayStarted = true;
            isNormalStarted = false;
        }
        sendToAllOnlineFamily();
    }

    @Override
    public void end(int type, boolean startServer) {
        if (FamilyTreasureConst.NORMAL_TREASURE == type) {
            isNormalStarted = false;
        }
        if (FamilyTreasureConst.SUNDAY_TREASURE == type) {
            isSundayStarted = false;
        }
    }

    @Override
    public boolean isStart(int type) {
        switch (type) {
            case FamilyTreasureConst.NORMAL_TREASURE:
                return isNormalStarted;
            case FamilyTreasureConst.SUNDAY_TREASURE:
                return isSundayStarted;
        }
        return false;
    }

    @Override
    public int startType() {
        return isNormalStarted ? FamilyTreasureConst.NORMAL_TREASURE : FamilyTreasureConst.SUNDAY_TREASURE;
    }

    @Override
    public int getFamilyTreasureRank(long familyId) {
        return ServiceHelper.rankService().getRank(RankConstant.RANKID_FAMILYTREASURE, familyId).getRank();
    }

    @Override
    public void dissolve(long familyId) {
        if (ftpList.containsKey(familyId)) {
            ftpList.remove(familyId);
        }
        ServiceHelper.rankService().removeRank(RankConstant.RANKID_FAMILYTREASURE, familyId, new FamilyTreasureRankPo(familyId));
    }

    /**
     * 重置在线家族的探宝进度
     * 离线家族会在下一次上线时重置
     */
    private void reset() {
        SystemRecordMap.update("familyTreasureResetTimestamp", System.currentTimeMillis());
        for (FamilyTreasure familyTreasure : ftpList.values()) {
            familyTreasure.setStep(FamilyTreasureManager.getMinStepByLevel(familyTreasure.getLevel()));
            familyTreasure.setDamage(0);
            familyTreasure.setTotalDamage(0);
            familyTreasure.setLastResetTimestamp(SystemRecordMap.familyTreasureResetTimestamp);
            dao.update(familyTreasure);
            processAndDamageEvent(familyTreasure.getFamilyId(), true);
            updateRank(familyTreasure.getFamilyId(), false);
        }

    }

    /**
     * 给所有在线的家族成员抛事件
     */
    private void sendToAllOnlineFamily() {
        for (Long familyId : ftpList.keySet()) {
            processAndDamageEvent(familyId, false);
        }
    }

    /**
     * 起服捞库，付费测内容
     *
     * @throws SQLException
     */
    private void loadRoleTreasureDamgeRank() throws SQLException {
        String sql = "select * from roletreasuredamagerank";
        List<RoleTreasureDamageRankPo> rankPos = DBUtil.queryList(DBUtil.DB_USER, RoleTreasureDamageRankPo.class, sql);
        Map<Long, Map<Long, RoleTreasureDamageRankPo>> roleDamageRankMap = new HashMap<>();
        for (RoleTreasureDamageRankPo rankPo : rankPos) {
            Map<Long, RoleTreasureDamageRankPo> rankPoMap = roleDamageRankMap.get(rankPo.getFamilyId());
            if (rankPoMap == null) {
                rankPoMap = new HashMap<>();
                roleDamageRankMap.put(rankPo.getFamilyId(), rankPoMap);
            }
            rankPoMap.put(rankPo.getRoleId(), rankPo);
        }
        this.damageRankMap = roleDamageRankMap;
    }

    @Override
    public void leaveFamily(long roleId, long familyId) {
        //离开家族会删除该玩家数据
        Map<Long, RoleTreasureDamageRankPo> rankPoMap = damageRankMap.get(familyId);
        if (rankPoMap != null) {
            rankPoMap.remove(roleId);
        }
    }

    @Override
    public void changeLevel(long roleId, long familyId, int level) {
        RoleTreasureDamageRankPo rankPo = damageRankMap.get(familyId).get(roleId);
        rankPo.setLevel(level);
        dao.update(rankPo);
    }

    @Override
    public void changeFightScore(long roleId, long familyId, int fightScore) {
        RoleTreasureDamageRankPo rankPo = damageRankMap.get(familyId).get(roleId);
        rankPo.setFightScore(fightScore);
        dao.update(rankPo);
    }

    @Override
    public void newRank(long roleId, long familyId, int damage, int fightScore, int level) {
        Map<Long, RoleTreasureDamageRankPo> rankPoMap = damageRankMap.get(familyId);
        if (rankPoMap == null) {
            rankPoMap = new HashMap<>();
            damageRankMap.put(familyId, rankPoMap);
        }
        if (rankPoMap.containsKey(roleId)) {
            LogUtil.info("已存在相同玩家数据");
            return;
        }
        RoleTreasureDamageRankPo rankPo = new RoleTreasureDamageRankPo();
        rankPo.setFamilyId(familyId);
        rankPo.setRoleId(roleId);
        rankPo.setLevel(level);
        rankPo.setFightScore(fightScore);
        rankPo.setDamage(damage);
        rankPoMap.put(roleId, rankPo);
    }
}
