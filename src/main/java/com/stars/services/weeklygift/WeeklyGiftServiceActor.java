package com.stars.services.weeklygift;

import com.stars.core.dao.DbRowDao;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.operateactivity.rolelimit.ActLevelLimit;
import com.stars.modules.operateactivity.rolelimit.ActRoleLimitBase;
import com.stars.modules.weeklygift.WeeklyGiftManager;
import com.stars.modules.weeklygift.event.WeeklyGiftEvent;
import com.stars.modules.weeklygift.packet.ClientWeeklyGift;
import com.stars.modules.weeklygift.prodata.WeeklyGiftVo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.weeklygift.userdata.RoleWeeklyGift;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class WeeklyGiftServiceActor extends ServiceActor implements WeeklyGiftService {
    private Map<Long, RoleWeeklyGift> roleWeeklyGiftMap;
    private DbRowDao rowDao;
    private boolean isOpen;
    private OperateActVo operateActVo;
    private ActOpenTime5 openTime5;
    private List<Long> finishRole;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.WeeklyGiftService, this);
        roleWeeklyGiftMap = new HashMap<>();
        rowDao = new DbRowDao(SConst.WeeklyGiftService, DBUtil.DB_USER);
        this.operateActVo = OperateActivityManager.getOperateActVo(OperateActivityManager.getFirstActIdbyActType(OperateActivityConstant.ActType_WeeklyGift));
        this.openTime5 = (ActOpenTime5) ActOpenTimeBase.newActOpenTimeBaseByStr(operateActVo.getOpenTime());
        loadUserData();
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WeeklyGift);
        if (curActId != -1 && !isOpen) {
            initActivity();
        }
    }

    private void loadUserData() {
        try {
            String sql = "select * from roleweeklygift where markfinish = 0";
            Map<Long, RoleWeeklyGift> giftMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", RoleWeeklyGift.class, sql);
            this.roleWeeklyGiftMap = giftMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initActivity() {
        if (isOpen)
            return;
        String sql = "select r.roleid,r.level,a.viplevel from account as a,role as r,accountrole as ar where r.roleid = ar.roleid and ar.account = a.name";
        try {
            isOpen = true;
            List<_HashMap> list = DBUtil.queryList(DBUtil.DB_USER, _HashMap.class, sql);
            if (list == null || list.size() <= 0) {
                return;
            }
            for (_HashMap hashMap : list) {
                long roleId = hashMap.getLong("r.roleid");
                int level = hashMap.getInt("r.level");
                int vipLv = hashMap.getInt("a.viplevel");
                if (roleWeeklyGiftMap.containsKey(roleId)) {
                    continue;
                }
                if (!isConform(roleId, level, operateActVo)) {
                    continue;
                }
                RoleWeeklyGift weeklyGift = new RoleWeeklyGift();
                weeklyGift.setRoleId(roleId);
                weeklyGift.setLevel(level);
                weeklyGift.setVipLevel(vipLv);
                weeklyGift.setMarkfinish(0L);
                updateGiftId(weeklyGift);
                roleWeeklyGiftMap.put(weeklyGift.getRoleId(), weeklyGift);
                rowDao.insert(weeklyGift);
            }

//            LogUtil.info("周惠礼包活动开始了,size:{},roleList:{}", roleWeeklyGiftMap.size(), roleWeeklyGiftMap.keySet());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeActivity() {
        isOpen = false;
        List<Long> finishRole = new ArrayList<>();
        for (RoleWeeklyGift weeklyGift : roleWeeklyGiftMap.values()) {
            if (havDays(weeklyGift))
                continue;
//            weeklyGift.setMarkfinish(System.currentTimeMillis());
//            rowDao.update(weeklyGift);
            finishRole.add(weeklyGift.getRoleId());
        }
        for (long roleId : finishRole) {
            roleWeeklyGiftMap.remove(roleId);
        }
        this.finishRole = new ArrayList<>(finishRole);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.WeeklyGift, new Runnable() {
            @Override
            public void run() {
                markFinish();
            }
        }, 120, 120, TimeUnit.SECONDS);
    }

    private void markFinish() {
        try {
            SchedulerManager.shutDownNow(ExcutorKey.WeeklyGift);
            StringBuilder sb = new StringBuilder();
            for (long roleId : finishRole) {
                sb.append(roleId).append(",");
            }
            if (sb.length() <= 0)
                return;
            sb.deleteCharAt(sb.length() - 1);
            LogUtil.info("role s in :{}", sb.toString());
            String sql = "update roleweeklygift set markfinish =" + System.currentTimeMillis() + " where roleid in (" + sb.toString() + ")";
            DBUtil.execSql(DBUtil.DB_USER, sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean havDays(RoleWeeklyGift weeklyGift) {
        Map<Integer, Integer> giftDays = weeklyGift.getGiftDaysMap();
        for (Map.Entry<Integer, Integer> entry : giftDays.entrySet()) {
            WeeklyGiftVo giftVo = WeeklyGiftManager.getWeeklyGiftVoMap().get(entry.getKey());
            if (giftVo == null) {
                LogUtil.info("havDays|没有产品数据|roleId:{},key:{}", weeklyGift.getRoleId(), entry.getKey());
                continue;
            }
            if (entry.getValue() != 0 && giftVo.getDays() > entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void printState() {

    }

    @Override
    public void doCharge(long roleId, int vipLv, int level, int charge) {
        RoleWeeklyGift weeklyGift = roleWeeklyGiftMap.get(roleId);
        if (isOpen && weeklyGift != null) {
            weeklyGift.addCharge(charge);
            weeklyGift.setVipLevel(vipLv);
            weeklyGift.setLevel(level);
            updateCharge(weeklyGift);
            rowDao.update(weeklyGift);
            LogUtil.info("周惠礼包|玩家充值:{}", weeklyGift);
        }
    }

    private void updateCharge(RoleWeeklyGift weeklyGift) {
        List<WeeklyGiftVo> vos = WeeklyGiftManager.getVos(weeklyGift.getGiftDaysMap().keySet());
        for (WeeklyGiftVo vo : vos) {
            if (weeklyGift.getTotalCharge() >= vo.getChargeNum()
                    && weeklyGift.getGiftDaysMap().get(vo.getWeeklyGiftId()) == 0) {
                Map<Integer, Integer> toolMap = DropUtil.executeDrop(vo.getDropId(), 1);
                ServiceHelper.emailService().sendToSingle(weeklyGift.getRoleId(), vo.getEmailTemplateId(), 0L, "系统",
                        toolMap, Integer.toString(vo.getChargeNum()), "1/" + vo.getDays());
                weeklyGift.addGiftDays(vo.getWeeklyGiftId(), 1);
                ServiceHelper.roleService().notice(weeklyGift.getRoleId(), new WeeklyGiftEvent(vo.getWeeklyGiftId(), vo.getChargeNum(), toolMap));
                LogUtil.info("周惠礼包首次超过|roleId:{},giftId:{},charge:{}", weeklyGift.getRoleId(), vo.getWeeklyGiftId(), weeklyGift.getTotalCharge());
            }
        }
    }

    private boolean isConform(long roleId, int level, OperateActVo operateActVo) {
//        ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(roleId, MConst.ForeShow);
//        if (!fsSummary.isOpen(ForeShowConst.WEEKLYGIFT))
//            return false;
        if (!isShow(operateActVo.getRoleLimitMap(), null, level))
            return false;
        return true;
    }

    private void updateGiftId(RoleWeeklyGift weeklyGift) {
        List<WeeklyGiftVo> vos = WeeklyGiftManager.getVos(weeklyGift.getVipLevel(), weeklyGift.getLevel());
        for (WeeklyGiftVo vo : vos) {
            weeklyGift.addGiftDays(vo.getWeeklyGiftId(), 0);
        }
    }

    private boolean isShow(Map<Integer, ActRoleLimitBase> roleLimitMap, ForeShowSummaryComponent fsSummary, int level) {
        if (roleLimitMap != null) {
            for (ActRoleLimitBase roleLimitBase : roleLimitMap.values()) {
                if (!isFit(roleLimitBase, fsSummary, level)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isFit(ActRoleLimitBase showLimitBase, ForeShowSummaryComponent fsSummary, int level) {
        if (showLimitBase instanceof ActLevelLimit) {
            ActLevelLimit actLevelLimit = (ActLevelLimit) showLimitBase;
            return level >= actLevelLimit.getLevel();
        }
//        else if (showLimitBase instanceof ActSystemLimit) {
//            ActSystemLimit actSystemLimit = (ActSystemLimit) showLimitBase;
//            return fsSummary.isOpen(actSystemLimit.getSystemName());
//        }
        return false;
    }

    @Override
    public void view(long roleId) {
        LogUtil.info("周惠礼包|roleId:{}", roleId);
        RoleWeeklyGift weeklyGift = roleWeeklyGiftMap.get(roleId);
        if (isOpen && weeklyGift != null) {
            ClientWeeklyGift gift = new ClientWeeklyGift(ClientWeeklyGift.C_PRODUCT);
            List<WeeklyGiftVo> vos = WeeklyGiftManager.getVos(weeklyGift.getGiftDaysMap().keySet());
            gift.setVos(vos);
            gift.setActTitle(DataManager.getGametext(operateActVo.getName()));
            gift.setTimeText(String.format(DataManager.getGametext(operateActVo.getTimedesc()), openTime5.getStartDateString(), openTime5.getEndDateString()));
            gift.setRuleText(DataManager.getGametext(operateActVo.getRuledesc()));
            ServiceHelper.roleService().send(roleId, gift);
            ClientWeeklyGift gift1 = new ClientWeeklyGift(ClientWeeklyGift.C_USER);
            gift1.setCharge(weeklyGift.getTotalCharge());
            gift1.setGiftDays(weeklyGift.getGiftDaysMap());
            ServiceHelper.roleService().send(roleId, gift1);
        }
    }

    @Override
    public void dailyReset() {
        LogUtil.info("周惠礼包每日重置|size:{},roleList:{}", roleWeeklyGiftMap.size(), roleWeeklyGiftMap.keySet());
        List<Long> finishRole = new ArrayList<>();
        for (RoleWeeklyGift weeklyGift : roleWeeklyGiftMap.values()) {
            Map<Integer, Integer> giftDayMap = weeklyGift.getGiftDaysMap();
            if (StringUtil.isEmpty(giftDayMap))
                continue;
            for (Map.Entry<Integer, Integer> entry : giftDayMap.entrySet()) {
                WeeklyGiftVo vo = WeeklyGiftManager.getWeeklyGiftVoMap().get(entry.getKey());
                if (vo == null) {
                    LogUtil.info("周惠礼包找不到产品数据|roleId:{},key:{}", weeklyGift.getRoleId(), entry.getKey());
                    continue;
                }
                if (vo.getDays() > entry.getValue() && entry.getValue() != 0) {
                    Map<Integer, Integer> toolMap = DropUtil.executeDrop(vo.getDropId(), 1);
                    ServiceHelper.emailService().sendToSingle(weeklyGift.getRoleId(), vo.getEmailTemplateId(), 0L, "系统",
                            toolMap, Integer.toString(vo.getChargeNum()), (entry.getValue() + 1) + "/" + vo.getDays());
                    weeklyGift.addGiftDays(vo.getWeeklyGiftId(), entry.getValue() + 1);
                }
            }
            if (!isOpen && !havDays(weeklyGift)) {
//                weeklyGift.setMarkfinish(System.currentTimeMillis());
                finishRole.add(weeklyGift.getRoleId());
            }
            rowDao.update(weeklyGift);
        }
        for (long roleId : finishRole) {
            roleWeeklyGiftMap.remove(roleId);
        }
        if (finishRole.isEmpty())
            return;
        this.finishRole = new ArrayList<>(finishRole);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.WeeklyGift, new Runnable() {
            @Override
            public void run() {
                markFinish();
            }
        }, 120, 120, TimeUnit.SECONDS);
    }

    @Override
    public void initRoleData(long roleId, int vipLv, int level, int charge) {
        if (isOpen && !roleWeeklyGiftMap.containsKey(roleId)) {
            RoleWeeklyGift weeklyGift = new RoleWeeklyGift();
            weeklyGift.setRoleId(roleId);
            weeklyGift.setVipLevel(vipLv);
            weeklyGift.setLevel(level);
            weeklyGift.setTotalCharge(charge);
            weeklyGift.setMarkfinish(0L);
            updateGiftId(weeklyGift);
            updateCharge(weeklyGift);
            rowDao.insert(weeklyGift);
            roleWeeklyGiftMap.put(roleId, weeklyGift);
            LogUtil.info("周惠礼包|玩家 {} 触发初始化流程 vipLv:{},level:{},charge:{}", roleId, vipLv, level, charge);
        }
    }

    @Override
    public void save() {
        rowDao.flush();
    }
}
