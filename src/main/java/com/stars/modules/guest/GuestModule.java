package com.stars.modules.guest;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.guest.event.GuestAchieveEvent;
import com.stars.modules.guest.event.GuestAttributeChangeEvent;
import com.stars.modules.guest.packet.ClientGuest;
import com.stars.modules.guest.prodata.*;
import com.stars.modules.guest.userdata.RoleGuest;
import com.stars.modules.guest.userdata.RoleGuestExchange;
import com.stars.modules.guest.userdata.RoleGuestMission;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.operateCheck.OperateCheckModule;
import com.stars.modules.operateCheck.OperateConst;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.util.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestModule extends AbstractModule {
    // recordmap中的key
    private final static String FLUSH_KEY = "guest.flush";
    private final static String ASK_KEY = "guest.ask";
    private final static String FLUSH_YMDH = "guest.flush.ymdh";
    private final static String HELP_TIME = "guest.help.time";

    private Map<Integer, RoleGuest> guestMap = new HashMap<>(); // 门客
    private Set<Integer> feeling = new HashSet<>(); // 情缘,这里只存产品id，不缓存产品数据防止热更产品数据导致数据不一致

    // 任务数据
    private Map<Integer, RoleGuestMission> missionMap = new HashMap<>();
    private Queue<RoleGuestMission> freeSlot = new LinkedList<>();
    /**
     * 加载角色任务数据的时候，将门客-任务的关联关系加载到缓存，不在门客身上保有任务的约束
     * 所以在处理门客和任务的时候留心这个map的数据添加与移除
     */
    private Map<Integer, Integer> guest2mission = new HashMap<>();  // 门客-任务关联结构

    public GuestModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("门客", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from roleguest where roleid = " + id();
        guestMap = DBUtil.queryMap(DBUtil.DB_USER, "guestid", RoleGuest.class, sql);

        sql = "select * from roleguestmission where roleid = " + id();
        List<RoleGuestMission> missionList = DBUtil.queryList(DBUtil.DB_USER, RoleGuestMission.class, sql);
        for (RoleGuestMission mission : missionList) {
            if (mission.getMissionId() == 0) {
                freeSlot.offer(mission);
            } else {
                missionMap.put(mission.getMissionId(), mission);
                if (!StringUtil.isEmpty(mission.getGuestGroup())) {
                    String[] guests = mission.getGuestGroup().split("[+]");
                    for (String guest : guests) {
                        guest2mission.put(Integer.valueOf(guest), mission.getMissionId());
                    }
                }
            }
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        // 计算情缘
        feeling.clear();
        for (RoleGuest guest : guestMap.values()) {
            checkFeeling(guest.getGuestId());
        }
        // 计算属性战力
        updateAttribute(false);

        // 红点计算
        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_MISSION_FINISH);
        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_ASK);
        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_ACTIVE);
        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_UPSTAR);
    }

    @Override
    public void onSyncData() throws Throwable {
        fireGuestAchieveEvent(); //登陆检测成就达成
    }

    /**
     * 更新门客属性
     *
     * @param needSend
     */
    private void updateAttribute(boolean needSend) {
        RoleModule roleModule = module(MConst.Role);
        roleModule.updatePartAttr(MConst.Guest, getGuestAttribute());
        roleModule.updatePartFightScore(MConst.Guest, FormularUtils.calFightScore(getGuestAttribute()));
        if (needSend) {
            roleModule.sendRoleAttr();
            roleModule.sendUpdateFightScore();
        }
        eventDispatcher().fire(new GuestAttributeChangeEvent());
    }

    public int getGuestAllLevel() {
        int totallevel = 0;
        for (RoleGuest guest : guestMap.values()) {
            totallevel += guest.getLevel();
        }
        return totallevel;
    }

    /**
     * 获取门客属性
     *
     * @return
     */
    public Attribute getGuestAttribute() {
        Attribute attribute = new Attribute();
        for (RoleGuest guest : guestMap.values()) {
            GuestStageVo stageVo = GuestManager.getStageVo(guest.getGuestId(), guest.getLevel());
            attribute.addAttribute(new Attribute(stageVo.getAttribute()));
        }
        for (int feelingId : feeling) {
            GuestFeelingVo feelingVo = GuestManager.getFeelingById(feelingId);
            attribute.addAttribute(new Attribute(feelingVo.getAttribute()));
        }
        return attribute;
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        // 刷新任务
        freshMission0();
        // 重置每日求助次数
        context().recordMap().setInt(ASK_KEY, 0);
        // 重置每日刷新次数
        context().recordMap().setInt(FLUSH_KEY, 0);
        // 重置每日给予次数
        context().recordMap().setInt(HELP_TIME, 0);

        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_ASK);

    }

    /**
     * 检查情缘
     *
     * @param guestId
     */
    private void checkFeeling(int guestId) {
        Map<Integer, GuestFeelingVo> map = GuestManager.getFeelingByGuest(guestId);
        Set<Integer> guestSet = guestMap.keySet();
        for (Map.Entry<Integer, GuestFeelingVo> entry : map.entrySet()) {
            if (feeling.contains(entry.getKey())) continue; // 已经满足了，不再进行检测
            if (guestSet.containsAll(entry.getValue().group())) {
                feeling.add(entry.getKey());
            }
        }
    }

    /**
     * 激活门客
     *
     * @param guestId
     */
    public void active(int guestId) {
        GuestStageVo minVo = GuestManager.getMinStageVo(guestId);
        check(guestMap.containsKey(guestId), "guest.active.exist");
        check(minVo == null, "guest.active.not.exist");

        checkUpstarCondition(minVo);

        RoleGuest newGuest = new RoleGuest();
        newGuest.setRoleId(id());
        newGuest.setGuestId(guestId);
        newGuest.setLevel(minVo.getLevel());
        guestMap.put(guestId, newGuest);
        context().insert(newGuest);

        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_ACTIVE);
        res.setGuest(newGuest);
        send(res);

        // 情缘处理
        checkFeeling(guestId);
        //更新属性
        updateAttribute(true);

        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_ACTIVE);

        fireGuestAchieveEvent();//触发门客成就事件
    }

    private void fireGuestAchieveEvent() {
        if (guestMap == null) return;
        GuestAchieveEvent event = new GuestAchieveEvent(guestMap);
        eventDispatcher().fire(event);
    }

    /**
     * 检查升星条件
     *
     * @param stageVo
     */
    private void checkUpstarCondition(GuestStageVo stageVo) {
        RoleModule roleModule = module(MConst.Role);
        ToolModule toolModule = module(MConst.Tool);
        Map<Integer, Integer> tools = ToolManager.parseString(stageVo.getReqItem());
        int reqLevel = stageVo.getReqRoleLevel();
        check(roleModule.getLevel() < reqLevel, "guest.active.level.limit", String.valueOf(reqLevel));
        check(!toolModule.deleteAndSend(tools, EventType.GUEST.getCode()), "guest.active.tool.lack");
    }

    /**
     * 升星
     *
     * @param guestId
     */
    public void upstar(int guestId) {
        RoleGuest guest = guestMap.get(guestId);
        check(guest == null, "guest.upstar.inactive");
        GuestStageVo next = GuestManager.getNextStageVo(guestId, guest.getLevel());
        check(next == null, "guest.upstar.maxstar");
        checkUpstarCondition(next);
        guest.setLevel(next.getLevel());
        context().update(guest);

        // 更新属性
        updateAttribute(true);

        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_UPSTAR);
        res.setGuest(guest);
        send(res);

        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_UPSTAR);

        fireGuestAchieveEvent();//触发门客成就事件
    }

    /**
     * 重置任务槽
     *
     * @param slot
     */
    private void resetMissionSlot(RoleGuestMission slot) {
        slot.setMissionId(0);
        slot.setState(RoleGuestMission.UNDISPATCH);
        slot.setFreshStamp(0);
        slot.setStartStamp(0);
        slot.setGuestGroup("");
        context().update(slot);
        if (freeSlot.contains(slot) == false) {
            freeSlot.offer(slot);
        }
    }

    /**
     * 刷新任务
     */
    private void freshMission0() {
        // 去除过期任务
        Iterator<Map.Entry<Integer, RoleGuestMission>> iter = missionMap.entrySet().iterator();
        while (iter.hasNext()) {
            RoleGuestMission temp = iter.next().getValue();
            if (temp.getState() != RoleGuestMission.UNDISPATCH &&
                    temp.getState() != RoleGuestMission.AWARD) {   // 只会移除未派遣的任务,和已经领取的任务
                continue;
            }
            GuestMissionVo vo = GuestManager.getMissionById(temp.getMissionId());
            if (vo.getReserveSecond() == 0
                    || temp.getFreshStamp() + vo.getReserveSecond() < DateUtil.getSecondTime()
                    || temp.getState() == RoleGuestMission.AWARD) {
                // 没有保留时间，或者过期任务,或者已经领奖
                iter.remove();
                resetMissionSlot(temp);
                continue;
            }
        }

        // 找出refreshvo
        int guestCount = guestMap.size();
        List<GuestRefreshVo> list = GuestManager.getRefreshList(guestCount);
        check(list == null, "guest.fresh.product.null");
        GuestRefreshVo refreshVo = null;
        for (GuestRefreshVo vo : list) {
            Map<Integer, Integer> colorMap = StringUtil.toMap(vo.getGuestColor(), Integer.class, Integer.class, '+', '|');
            boolean b = true;
            for (Map.Entry<Integer, Integer> entry : colorMap.entrySet()) {
                if (getGuestCountByColor(entry.getKey()) < entry.getValue()) {
                    b = false;
                    break;
                }
            }
            if (b) {
                refreshVo = vo;
                break;
            }
        }
        check(refreshVo == null, "guest.fresh.product.null");
        // 随机任务
        String[] refreshs = refreshVo.getRefresh().split("[|]");
        int needTotal = refreshVo.getMissionCount() - missionMap.size();  // 需要刷出的任务数量
        if (needTotal <= 0) {
            return;
        }
        RoleModule rm = module(MConst.Role);
        int roleLv = rm.getLevel();
        List<GuestMissionVo> newList = new ArrayList<>();
        for (String refreshStr : refreshs) {
            String[] ss = refreshStr.split("[+]");
            int need = RandomUtil.rand(Integer.valueOf(ss[1]), Integer.valueOf(ss[2]));
            Map<Integer, GuestMissionVo> mission = GuestManager.getMissionByQuality(Byte.valueOf(ss[0]));
            List<GuestMissionVo> randList = new ArrayList<>();
            for (GuestMissionVo vo : mission.values()) {    // 筛选合适的进行随机
                if (vo.onLevel(roleLv) == false ||
                        missionMap.containsKey(vo.getGueMissionId())) {
                    continue;
                }
                randList.add(vo);
            }
            need = Math.min(need, needTotal);
            if (randList.size() == 0) {
                continue;
            }
            newList.addAll(RandomUtil.powerRandom(randList, "odds", need, false));
            needTotal -= need;
            if (needTotal <= 0) {
                break;
            }
        }
        int stamp = DateUtil.getSecondTime();
        for (GuestMissionVo vo : newList) {
            RoleGuestMission slot = getFreeMissionSlot();
            slot.setMissionId(vo.getGueMissionId());
            slot.setFreshStamp(stamp);
            context().update(slot);
            missionMap.put(slot.getMissionId(), slot);
        }
    }

    /**
     * 获取空闲的任务槽
     *
     * @return
     */
    private RoleGuestMission getFreeMissionSlot() {
        if (freeSlot.size() != 0) {
            return freeSlot.poll();
        } else {
            RoleGuestMission slot = new RoleGuestMission();
            slot.setRoleId(id());
            slot.setMissionSlot(missionMap.size() + 1);
            context().insert(slot);
            return slot;
        }
    }

    /**
     * 指定颜色（星级）门客数量
     *
     * @param color
     * @return
     */
    public int getGuestCountByColor(int color) {
        int count = 0;
        for (RoleGuest guest : guestMap.values()) {
            if (guest.getLevel() == color) {
                count++;
            }
        }
        return count;
    }

    /**
     * 派遣任务
     *
     * @param missionId
     * @param guestGroup
     */
    public void dispatch(int missionId, String guestGroup) {
        check(StringUtil.isEmpty(guestGroup), "guest.dispatch.empty");
        GuestMissionVo missionVo = GuestManager.getMissionById(missionId);
        check(missionVo == null, "guest.dispatch.mission.unexist");
        check(!missionMap.containsKey(missionId), "guest.dispatch.mission.unexist");

        // 检查任务条件
        RoleGuestMission slot = missionMap.get(missionId);
        if (missionVo.getReserveSecond() != 0) {    // 保留时间类型的任务，检查过期
            int now = DateUtil.getSecondTime();
            int valid = missionVo.getReserveSecond();
            check(slot.getFreshStamp() + valid < now, "guest.dispatch.overtime");
        }
        int star = 0;
        String[] guests = guestGroup.split("[+]");
        for (String guest : guests) {   // 检查门客状态
            check(guest2mission.containsKey(Integer.valueOf(guest)), "guest.dispatch.onmission");   // 空闲
            RoleGuest roleGuest = guestMap.get(Integer.valueOf(guest));
            check(roleGuest == null, "guest.upstar.inactive");  // 激活
            GuestInfoVo infoVo = GuestManager.getInfoVo(roleGuest.getGuestId());
            check(!missionVo.checkSort(infoVo.getSort()), "guest.dispatch.unfit");  // 配置条件
            star += roleGuest.getLevel();
        }

        for (int feelingId : getFeeling(guestGroup)) {
            GuestFeelingVo feelingVo = GuestManager.getFeelingById(feelingId);
            String[] funcs = feelingVo.getFunc().split("[+]");
            if (Integer.valueOf(funcs[0]) == 3) {
                star += Integer.valueOf(funcs[1]);
            }
        }

        check(star < missionVo.getReqStar(), "guest_tips_cantgoingstar");
        check(guests.length < missionVo.getReqMember(), "guest_tips_cantgoingguest");
        check(slot.getState() >= RoleGuestMission.ONMISSION, "guest.dispatch.mission.done");

        // 派遣任务
        int stamp = DateUtil.getSecondTime();
        slot.setState(RoleGuestMission.ONMISSION);
        slot.setStartStamp(stamp);
        slot.setGuestGroup(guestGroup);
        for (String guest : guests) {
            guest2mission.put(Integer.valueOf(guest), missionVo.getGueMissionId());
        }
        context().update(slot);

        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_DISPATCH);
        res.setDispatchResult(Packet.TRUE);
        send(res);

        DailyFuntionEvent event = new DailyFuntionEvent(DailyManager.DAILYID_GUEST_MISSION, 1);
        eventDispatcher().fire(event);
    }

    /**
     * 自动刷新
     *
     * @return
     */
    private void autoFlush() {
        int ymdh = context().recordMap().getInt(FLUSH_YMDH, 0);
        int nowYmdh = Integer.valueOf(new SimpleDateFormat("yyyyMMddHH").format(new Date()));
        int nextYmdh = getNextYMDH(ymdh);
        if (nowYmdh >= nextYmdh) {
            freshMission0();
            context().recordMap().setInt(FLUSH_YMDH, nowYmdh);
        }
    }

    /**
     * 获取
     *
     * @param nowYmdh
     * @return
     */
    private int getNextYMDH(int nowYmdh) {
        int hh = nowYmdh % 100;
        int ymd = nowYmdh / 100;
        String configStr = DataManager.getCommConfig("guest_refreshtime");
        String[] config = configStr.split("[+]");
        int next = Integer.valueOf(config[0]);
        for (String s : config) {
            int temp = Integer.valueOf(s);
            if (hh >= temp) {
                continue;
            }
            next = temp;
            break;
        }
        if (hh >= next) {
            ymd++;
        }
        return ymd * 100 + next;
    }

    /**
     * 获取时间戳
     *
     * @param ymdh
     * @return
     */
    private int getStamp(int ymdh) {
        try {
            int next = (int) (new SimpleDateFormat("yyyyMMddHH").parse(String.valueOf(ymdh)).getTime() / 1000);
            return next;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 任务信息
     */
    public void missionInfo() {
        if (missionMap.size() == 0) {
            freshMission0();
        }
        autoFlush();
        for (RoleGuestMission mission : missionMap.values()) {
            checkMissionState(mission);
        }
        int nextYmdh = getNextYMDH(context().recordMap().getInt(FLUSH_YMDH, 0));
        int remainTime = getStamp(nextYmdh) - DateUtil.getSecondTime();
        int flushCount = context().recordMap().getInt(FLUSH_KEY, 0);
        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_MISSION_INFO);
        res.setFlushCount(flushCount);
        res.setRemainTime(remainTime);
        res.setMissionMap(missionMap);
        send(res);

        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_MISSION_FINISH);
    }

    /**
     * 获取情缘
     *
     * @param guestGroup
     * @return
     */
    private Set<Integer> getFeeling(String guestGroup) {
        Set<Integer> set = new HashSet<>();
        String[] guests = guestGroup.split("[+]");
        Set<Integer> guestSet = new HashSet<>();
        for (String guest : guests) {
            guestSet.add(Integer.valueOf(guest));
        }
        for (int feelingId : feeling) {
            GuestFeelingVo vo = GuestManager.getFeelingById(feelingId);
            if (guestSet.containsAll(vo.group())) {
                set.add(feelingId);
            }
        }
        return set;
    }

    /**
     * 检查任务状态
     *
     * @param mission
     * @return
     */
    private byte checkMissionState(RoleGuestMission mission) {
        if (mission.getState() != RoleGuestMission.ONMISSION) {
            // 不是执行状态直接返回
            return mission.getState();
        }

        int now = DateUtil.getSecondTime();
        if (mission.getState() == RoleGuestMission.ONMISSION) {
            // 检查是否完成
            int startTime = mission.getStartStamp();
            GuestMissionVo missionVo = GuestManager.getMissionById(mission.getMissionId());
            int cost = missionVo.getReqTime();
            Set<Integer> feelingSet = getFeeling(mission.getGuestGroup());
            for (int feelingId : feelingSet) {
                GuestFeelingVo vo = GuestManager.getFeelingById(feelingId);
                String[] func = vo.getFunc().split("[+]");
                if (Integer.valueOf(func[0]) == 1) {
                    // 情缘作用，缩减时间
                    float add = missionVo.getReqTime() * (Integer.valueOf(func[1]) / 100.0f);
                    cost -= (int) add;
                }
            }
            if (startTime + cost <= now) {
                mission.setState(RoleGuestMission.FINISH);
                context().update(mission);
                return RoleGuestMission.FINISH;
            }

//            int remain = cost - (now - startTime);  // 任务剩余时间
//            VipModule vipModule = module(MConst.Vip);
//            vipModule.getVipLevel();
//            VipinfoVo vipinfoVo = vipModule.getCurVipinfoVo();
//            if (vipinfoVo != null && vipinfoVo.getGuestComplete() >= remain) {
//                mission.setState(RoleGuestMission.CANFINISH);
//                context().update(mission);
//                return RoleGuestMission.CANFINISH;
//            }
        }
        return RoleGuestMission.UNDISPATCH;
    }

    /**
     * 刷新任务
     */
    public void flush() {
        if (!OperateCheckModule.checkOperate(id(), OperateConst.GUEST_REFRESH, OperateConst.FIVE_HUNDRED_MS)) return;
        // 有 完成 或者 可提前 的任务不能刷新
        for (RoleGuestMission mission : missionMap.values()) {
            check(checkMissionState(mission) == RoleGuestMission.CANFINISH ||
                            checkMissionState(mission) == RoleGuestMission.FINISH,
                    "guest_tips_havecomplete");
        }
        int flushCount = context().recordMap().getInt(FLUSH_KEY, 0);
        VipModule vipModule = module(MConst.Vip);
        VipinfoVo vipinfoVo = vipModule.getCurVipinfoVo();
        int freshTimes = GuestManager.REFRESH_TIMES;
        if (vipinfoVo != null && vipinfoVo.getGuestRefresh() > 0) {
            freshTimes += vipinfoVo.getGuestRefresh();
        }
        check(flushCount >= freshTimes, "guest.fresh.max");
        if (flushCount >= GuestManager.FLUSH_FREE_COUNT) {
            // 超过免费次数，需要扣除道具
            ToolModule toolModule = module(MConst.Tool);
            check(!toolModule.deleteAndSend(GuestManager.RMB_REFRESH_COST, EventType.GUEST.getCode()), "guest.flush.tool.lack");
        }
        freshMission0();
        context().recordMap().setInt(FLUSH_KEY, flushCount + 1);
        missionInfo();
    }

    /**
     * 门客信息
     */
    public void guestInfo() {
        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_GUEST_INFO);
        res.setGuestMap(guestMap);
        res.setGuest2mission(guest2mission);
        send(res);

        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_MISSION_FINISH);
    }

    /**
     * 领取奖励
     *
     * @param missionId
     */
    public void award(int missionId) {
        RoleGuestMission mission = missionMap.get(missionId);
        check(mission == null, "guest.award.mission");
        checkMissionState(mission);
        check(mission.getState() != RoleGuestMission.FINISH, "guest.award.state");

        GuestMissionVo missionVo = GuestManager.getMissionById(missionId);
        Map<Integer, Integer> tool = ToolManager.parseString2(missionVo.getAward());
        // 计算情缘加成
        Set<Integer> feeling = getFeeling(mission.getGuestGroup());
        Map<Integer, Integer> add = new HashMap<>();
        for (int feelingId : feeling) {
            GuestFeelingVo vo = GuestManager.getFeelingById(feelingId);
            String[] func = vo.getFunc().split("[+]");
            if (Integer.valueOf(func[0]) == 2) {
                int toolId = Integer.valueOf(func[1]);
                if (tool.containsKey(toolId)) {
                    int base = tool.get(toolId);
                    int addCount = (int) Math.floor(base * (Integer.valueOf(func[2]) / 100.0));
                    if (add.containsKey(toolId)) {
                        add.put(toolId, add.get(toolId) + addCount);
                    } else {
                        add.put(toolId, addCount);
                    }
                }
            }
        }
        com.stars.util.MapUtil.add(tool, add);
        ToolModule toolModule = module(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(tool, EventType.GUEST.getCode());

        // 闲置门客
        if (!StringUtil.isEmpty(mission.getGuestGroup())) {
            String[] guests = mission.getGuestGroup().split("[+]");
            for (String guest : guests) {
                guest2mission.remove(Integer.valueOf(guest));
            }
        }
        mission.setState(RoleGuestMission.AWARD);
        mission.setGuestGroup("");
        context().update(mission);


        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_AWARD);
        res.setToolMap(map);
        res.setMissionId(missionId);
        send(res);

        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_MISSION_FINISH);
    }

    /**
     * 求助
     *
     * @param guestId
     * @param askClaim
     */
    public void exchangeAsk(int guestId, int level, int askCount, String askClaim) {
        // 检查家族条件
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        check(familyId == 0, "guest.ask.no.family");
        // 检查求助次数
        int askTimes = context().recordMap().getInt(ASK_KEY, 0);
        check(askTimes >= getAskCountMax(), "guest.ask.too.much");
        // 检查stage合法性
        GuestStageVo stageVo = GuestManager.getStageVo(guestId, level);
        check(stageVo == null, "guest.req.error");
        // 检查求助的个数
        GuestInfoVo guestInfoVo = GuestManager.getInfoVo(guestId);
        check(guestInfoVo == null, "guest.active.not.exist");
        //检查是否可以求助
        check(guestInfoVo.getWhetherHelp() == (byte) 0, "guest.cannot.help");
        // 每次最多不能超过配置个数
        check(askCount > guestInfoVo.getHelpCount(), "guest.ask.over.max");
        // 检查求助输入
        check(DirtyWords.hasDirtyWordWithObject(askClaim), "guest.ask.sensitive");
        check(askClaim.getBytes().length > 45, "guest.ask.too.long");

        RoleModule rm = module(MConst.Role);
        String[] items = stageVo.getReqItem().split("[+]");

        RoleGuestExchange exchange = new RoleGuestExchange();
        exchange.setRoleId(id());
        exchange.setStamp(DateUtil.getSecondTime());
        exchange.setItemId(Integer.valueOf(items[0]));
        exchange.setAskClaim(askClaim);
        exchange.setAskCount(askCount);
        exchange.setName(rm.getRoleRow().getName());
        exchange.setGuestId(guestId);
        exchange.setLevel(level);
        exchange.setFamilyId(familyId);

        context().recordMap().setInt(ASK_KEY, askTimes + 1);
        ServiceHelper.guestService().ask(exchange);
        signCalRedPoint(MConst.Guest, RedPointConst.GUEST_ASK);
        //家族交换 日志
        ServerLogModule logger = module(MConst.ServerLog);
        logger.log_personal_family_exchange((byte) 0, Integer.valueOf(items[0]), askCount, 0L);
    }

    /**
     * 最大求助次数
     *
     * @return
     */
    private int getAskCountMax() {
        VipModule vip = module(MConst.Vip);
        VipinfoVo vipinfoVo = vip.getCurVipinfoVo();
        int vipCount = vipinfoVo == null ? 0 : vipinfoVo.getGuestHelpcount();
        return DataManager.getCommConfig("guest_helpcount", 2) + vipCount;
    }

    /**
     * 给予
     *
     * @param askId
     * @param itemId
     */
    public void exchangeGive(long askId, int itemId, int askStamp) {
        FamilyModule familyModule = module(MConst.Family);
        Long familyId = familyModule.getAuth().getFamilyId();
        // 检查自己是不是家族成员
        check(familyId == 0, "guest.give.not.family");
        List<Long> memberList = ServiceHelper.familyMainService().getMemberIdList(familyId, id());
        // 自己给自己
        check(askId == id(), "guest.give.self");
        // 检查对方是不是家族成员
        if (!memberList.contains(askId)) {
            ServiceHelper.guestService().removeFromFamily(askId, familyId);
            warn(I18n.get("guest.give.not.family"));
            return;
        }
        // 检查自己的给予次数是否已达到最大值
        int guestHelpLimitTime = DataManager.getCommConfig("guest_givecount", 50);
        int helpTimes = context().recordMap().getInt(HELP_TIME, 0);
        if (helpTimes >= guestHelpLimitTime) {
            warn(I18n.get("guest.help.limit.time", guestHelpLimitTime));
            return;
        }

        // 检查时效性
        int validTime = GuestManager.HELP_LIMIT_TIME;
        check(askStamp + validTime < DateUtil.getSecondTime(), "guest.give.over.time");

        // 这里先扣除需要的道具，如果不行再加回来
        ToolModule toolModule = module(MConst.Tool);
        check(!toolModule.deleteAndSend(itemId, 1, EventType.GUEST.getCode()), "guest.flush.tool.lack");
        context().recordMap().setInt(HELP_TIME, helpTimes + 1);
        ServiceHelper.guestService().give(id(), askId, askStamp, itemId);
    }

    /**
     * 给予回调
     *
     * @param result
     * @param itemId
     */
    public void giveCallback(boolean result, int itemId) {
        ToolModule toolModule = module(MConst.Tool);
        if (result) {
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            itemVo.getColor();
            String helpAward = DataManager.getCommConfig("guest_giveaward");
            Map<Integer, Integer> color2drop = StringUtil.toMap(helpAward, Integer.class, Integer.class, '+', '|');
            if (color2drop.containsKey(Integer.valueOf(itemVo.getColor()))) {
                DropModule dropModule = module(MConst.Drop);
                Map<Integer, Integer> dropMap = dropModule.executeDrop(color2drop.get(Integer.valueOf(itemVo.getColor())), 1, true);
                toolModule.addAndSend(dropMap, EventType.GUEST.getCode());

                ClientGuest res = new ClientGuest();
                res.setResType(ClientGuest.RES_GIVE_AWARD);
                res.setToolMap(dropMap);
                send(res);
            }
        } else {    // 不成功，需要退回扣除的碎片
            toolModule.addAndSend(itemId, 1, EventType.GUEST.getCode());
        }
    }

    /**
     * 交换信息
     *
     * @param index
     */
    public void exchangeInfo(int index) {
        FamilyModule familyModule = module(MConst.Family);
        Long familyId = familyModule.getAuth().getFamilyId();
        // 检查自己是不是家族成员
        check(familyId == 0, "guest.give.not.family");
        List<Long> memberList = ServiceHelper.familyMainService().getMemberIdList(familyId, id());
        int askCount = context().recordMap().getInt(ASK_KEY, 0);
        ServiceHelper.guestService().info(id(), familyId, index, getAskCountMax() - askCount, memberList);
    }

    /**
     * 通过道具增加任务
     *
     * @param missionId
     */
    public void addMissionByTool(int missionId) {
        GuestMissionVo missionVo = GuestManager.getMissionById(missionId);
        if (missionVo == null) {
            LogUtil.error("门课任务道具配置错误，不存在对应的任务");
            warn(I18n.get("guest.tool.error"));
            return;
        }
        RoleGuestMission slot = getFreeMissionSlot();
        slot.setMissionId(missionVo.getGueMissionId());
        slot.setFreshStamp(DateUtil.getSecondTime());
        context().update(slot);
        missionMap.put(missionId, slot);

        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_OPEN_MISSION);
        send(res);
    }

    /**
     * 判断道具能否使用
     *
     * @param missionId
     * @return
     */
    public boolean canUseTool(int missionId) {
        GuestMissionVo missionVo = GuestManager.getMissionById(missionId);
        if (missionVo == null) {
            warn(I18n.get("guest.tool.error"));
            return false;
        }
        ForeShowSummaryComponent foreShow = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(id(), MConst.ForeShow);
        if (!foreShow.isOpen(ForeShowConst.GUEST)) {
            warn(I18n.get("guest.not.open"));
            return false;
        }
        if (missionMap.containsKey(missionId)) {
            warn(I18n.get("guest.mission.exist"));
            return false;
        }
        return true;
    }

    /**
     * 红点
     *
     * @param redPointIds
     * @param redPointMap
     */
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        ToolModule toolModule = module(MConst.Tool);
        RoleModule roleModule = module(MConst.Role);
        if (redPointIds.contains(RedPointConst.GUEST_ACTIVE)) {
            Set<Integer> guests = GuestManager.getAllGuestId();
            StringBuilder builder = new StringBuilder();
            for (int guestId : guests) {
                if (guestMap.containsKey(guestId)) {
                    continue;
                }
                GuestStageVo min = GuestManager.getMinStageVo(guestId);
                if (min.getReqRoleLevel() > roleModule.getLevel()) {
                    continue;
                }
                Map<Integer, Integer> tool = ToolManager.parseString(min.getReqItem());
                if (toolModule.contains(tool)) {
                    builder.append(guestId).append("+");
                }
            }
            if (builder.length() != 0) {
                builder.delete(builder.length() - 1, builder.length());
                redPointMap.put(RedPointConst.GUEST_ACTIVE, builder.toString());
            } else {
                redPointMap.put(RedPointConst.GUEST_ACTIVE, null);
            }
        }
        if (redPointIds.contains(RedPointConst.GUEST_UPSTAR)) {
            StringBuilder builder = new StringBuilder();
            for (RoleGuest guest : guestMap.values()) {
                GuestStageVo next = GuestManager.getNextStageVo(guest.getGuestId(), guest.getLevel());
                if (next != null && next.getReqRoleLevel() <= roleModule.getLevel()) {
                    Map<Integer, Integer> tool = ToolManager.parseString(next.getReqItem());
                    if (toolModule.contains(tool)) {
                        builder.append(guest.getGuestId()).append("+");
                    }
                }
            }
            if (builder.length() != 0) {
                builder.delete(builder.length() - 1, builder.length());
                redPointMap.put(RedPointConst.GUEST_UPSTAR, builder.toString());
            } else {
                redPointMap.put(RedPointConst.GUEST_UPSTAR, null);
            }
        }
        if (redPointIds.contains(RedPointConst.GUEST_ASK)) {
            if (getAskCountMax() - context().recordMap().getInt(ASK_KEY, 0) > 0) {
                redPointMap.put(RedPointConst.GUEST_ASK, Boolean.TRUE.toString());
            } else {
                redPointMap.put(RedPointConst.GUEST_ASK, null);
            }
        }
        if (redPointIds.contains(RedPointConst.GUEST_MISSION_FINISH)) {
            StringBuilder builder = new StringBuilder();
            for (RoleGuestMission mission : missionMap.values()) {
                int missionState = checkMissionState(mission);
                if (missionState == RoleGuestMission.FINISH) {
                    builder.append(mission.getMissionId()).append("[+]");
                }
            }
            if (builder.length() != 0) {
                builder.delete(builder.length() - 1, builder.length());
                redPointMap.put(RedPointConst.GUEST_MISSION_FINISH, builder.toString());
            } else {
                redPointMap.put(RedPointConst.GUEST_MISSION_FINISH, null);
            }
        }
    }

    public Map<Integer, RoleGuest> getGuestMap() {
        return guestMap;
    }

    public Set<Integer> getFeelingSet() {
        return feeling;
    }

    public String makeFsStr() {
        int initFs = 0;
        int starFs = 0;
        int feelingFs = 0;

        Attribute initAttr = new Attribute();
        Attribute totalAttr = new Attribute();
        Attribute feelingAttr = new Attribute();
        // 获取门客初始战力和升阶战力
        for (RoleGuest po : guestMap.values()) {
            int guestId = po.getGuestId();
            GuestStageVo initVo = GuestManager.getMinStageVo(guestId);
            GuestStageVo stageVo = GuestManager.getStageVo(guestId, po.getLevel());
            if (initVo != null) {
                initAttr.addAttribute(new Attribute(initVo.getAttribute()));
            }
            if (stageVo != null) {
                totalAttr.addAttribute(new Attribute(stageVo.getAttribute()));
            }
        }
        initFs = FormularUtils.calFightScore(initAttr);
        starFs = FormularUtils.calFightScore(totalAttr) - initFs;
        // 获取门客情缘战力
        for (int feelingId : feeling) {
            GuestFeelingVo feelingVo = GuestManager.getFeelingById(feelingId);
            if (feelingVo != null) {
                feelingAttr.addAttribute(new Attribute(feelingVo.getAttribute()));
            }
        }
        feelingFs = FormularUtils.calFightScore(feelingAttr);

        StringBuilder sb = new StringBuilder();
        sb.append("guest_base:").append(initFs).append("#")
                .append("guest_star:").append(starFs).append("#")
                .append("guest_feeling:").append(feelingFs).append("#");
        return sb.toString();
    }

    public void onRoleRename(RoleRenameEvent event) {
        FamilyModule familyModule=module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        ServiceHelper.guestService().updateRoleName(id(),familyId, event.getNewName());
    }
}
