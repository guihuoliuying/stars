package com.stars.modules.push;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.push.event.PushInactivedEvent;
import com.stars.modules.push.event.PushLoginDoneEvent;
import com.stars.modules.push.event.PushLoginInitEvent;
import com.stars.modules.push.prodata.PushVo;
import com.stars.modules.push.trigger.PushTrigger;
import com.stars.modules.push.userdata.RolePushPo;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PushModule extends AbstractModule {

    private Map<Integer, RolePushPo> pushPoMap;
    private long lastCheckTimestamp = now();
    private boolean needDailyReset = false;
    private boolean needWeeklyReset = false;

    public PushModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("push", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        this.pushPoMap = new HashMap<>();
    }

    @Override
    public void onDataReq() throws Throwable {
        this.pushPoMap = DBUtil.queryMap(
                DBUtil.DB_USER, "pushid", RolePushPo.class, "select * from `rolepush` where `roleid`=" + id());
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        long now = System.currentTimeMillis();
        Set<RolePushPo> delPushPoSet = new HashSet<>();
        for (RolePushPo pushPo : pushPoMap.values()) {
            PushVo pushVo = PushManager.getPushVo(pushPo.getPushId());
            if (pushVo == null) {
                delPushPoSet.add(pushPo);
            } else {
                if (pushVo.hasExpirationDate()
                        && (now < pushVo.getBeginTimeMillis() || now > pushVo.getEndTimeMillis())) {
                    delPushPoSet.add(pushPo);
                }
            }
        }

        for (RolePushPo po : delPushPoSet) {
            pushPoMap.remove(po.getPushId());
            context().delete(po);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (isLogin) {
            needDailyReset = true;
        } else {
            resetDaily();
            activePush0(triggerCondition(new LoginSuccessEvent()));
            com.stars.util.LogUtil.info("push|在线每日重置|{}", pushPoMap.values());
        }
    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {
        if (isLogin) {
            needWeeklyReset = true;
        } else {
            resetWeekly();
            activePush0(triggerCondition(new LoginSuccessEvent()));
            com.stars.util.LogUtil.info("push|在线每周重置|{}", pushPoMap.values());
        }
    }

    @Override
    public void onTimingExecute() {
        if (now() > lastCheckTimestamp + 60_000) { // 每一分钟检查一次（暂时这样子）
            long now = now();
            lastCheckTimestamp = now;
            for (RolePushPo po : pushPoMap.values()) {
                if (po.getState() == PushManager.STATE_ACTIVED) {
                    PushVo vo = PushManager.getPushVo(po.getPushId());
                    if (vo != null && (now < vo.getBeginTimeMillis() || now > vo.getEndTimeMillis())) {
                        inactivePush(po.getPushId());
                    }
                }
            }
        }
    }

    public Map<Integer, RolePushPo> getPushPoMapByActivityId(int activityId) {
        Map<Integer, PushVo> voMap = PushManager.getPushVoMapByActivityId(activityId);
        if (voMap == null) {
            return new HashMap<>();
        }
        Map<Integer, RolePushPo> poMap = new HashMap<>();
        for (Integer pushId : voMap.keySet()) {
            RolePushPo po = pushPoMap.get(pushId);
            if (po != null) {
                poMap.put(pushId, po);
            }
        }
        return poMap;
    }

    public Map<Integer, RolePushPo> getPushPoMapByGroup(int activityId, int group) {
        Map<Integer, PushVo> voMap = PushManager.getPushVoMapByGroup(activityId, group);
        Map<Integer, RolePushPo> poMap = new HashMap<>();
        for (Integer pushId : voMap.keySet()) {
            RolePushPo po = pushPoMap.get(pushId);
            if (po != null) {
                poMap.put(pushId, po);
            }
        }
        return poMap;
    }

    public Set<Integer> getActivePushIdByActivityId(int activityId) {
        Set<Integer> pushIdSet = new HashSet<>();
        Map<Integer, PushVo> voMap = PushManager.getPushVoMapByActivityId(activityId);
        if (voMap != null) {
            for (Integer pushId : voMap.keySet()) {
                RolePushPo po = pushPoMap.get(pushId);
                if (po != null && po.isActived()) {
                    pushIdSet.add(pushId);
                }
            }
        }
        return pushIdSet;
    }

    public Set<Integer> getAllActivedPushIdInGroup(int lastActivedPushId) {
        Set<Integer> activedPoSet = new HashSet<>();
        PushVo vo = PushManager.getPushVo(lastActivedPushId);
        if (vo != null) {
            Map<Integer, RolePushPo> poMap = getPushPoMapByGroup(vo.getActivityId(), vo.getGroup());
            for (RolePushPo po : poMap.values()) {
                if (po.isActived()) {
                    activedPoSet.add(po.getPushId());
                }
            }
        }
        return activedPoSet;
    }

    public int getMaxRankActivedPushIdInGroup(int lastActivedPushId) {
        PushVo vo = PushManager.getPushVo(lastActivedPushId);
        if (vo != null) {
            return getMaxRankActivedPushIdInGroup(vo.getActivityId(), vo.getGroup());
        }
        return 0;
    }

    public int getMaxRankActivedPushIdInGroup(int activityId, int groupId) {
        Map<Integer, RolePushPo> poMap = getPushPoMapByGroup(activityId, groupId);
        PushVo maxRankPushVo = null;
        for (RolePushPo po : poMap.values()) {
            if (po.isActived()) {
                PushVo pushVo = PushManager.getPushVo(po.getPushId());
                if (maxRankPushVo == null
                        || maxRankPushVo.getGroupRank() < pushVo.getGroupRank()
                        || (maxRankPushVo.getGroupRank() == pushVo.getGroupRank() && maxRankPushVo.getPushId() > pushVo.getPushId())) {
                    maxRankPushVo = pushVo;
                }
            }
        }
        if (maxRankPushVo != null) {
            return maxRankPushVo.getPushId();
        }
        return 0;
    }

    public void onEvent(Event event) {
        Map<Integer, Map<Integer, PushVo>> map = new HashMap<>();
        boolean isReset = false;
        /* 登陆事件处理（每日重置） */
        if (event instanceof LoginSuccessEvent) {
            if (needDailyReset) { // 每日重置
                needDailyReset = false;
                resetDaily();
                isReset = true;
            }
            if (needWeeklyReset) { // 每周重置
                needWeeklyReset = false;
                resetWeekly();
                isReset = true;
            }
        }
        /* 检查触发条件 */
        Set<Integer> pushIdSet = triggerCondition(event);

        if (event instanceof LoginSuccessEvent) { // 登陆流程
            try {
                // 激活新增
                Map<Integer, PushVo> activedPushVoMap = activePush0(pushIdSet);
                com.stars.util.LogUtil.info("push|登陆初始化|{}", pushPoMap.values());
                // 通知
                eventDispatcher().fire(new PushLoginInitEvent(isReset));
                eventDispatcher().fire(new PushLoginDoneEvent());
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("", cause);
            }

        } else { // 非登陆流程
            try {
                activePush(pushIdSet);
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("", cause);
            }
        }
    }

    public Set<Integer> triggerCondition(Event event) {
        Map<Integer, Map<Integer, PushVo>> map = new HashMap<>();
        /* 检查触发条件 */
        Class<? extends Event> eventClass = event.getClass();
        List<PushTrigger> triggerList = PushManager.getTriggerList(eventClass);
        if (triggerList == null) {
            return new HashSet<>();
        }
        for (PushTrigger trigger : triggerList) {
            try {
                if (trigger.check(event, moduleMap())) {
                    PushVo pushVo = PushManager.getPushVo(trigger.getPushId());
                    if (satisfyTimes(pushVo)
                            && satisfyDate(pushVo)
                            && satisfyCondition(pushVo) && satisfyGroup(pushVo)) {
                        if (!map.containsKey(pushVo.getActivityId())) {
                            map.put(pushVo.getActivityId(), new HashMap<Integer, PushVo>());
                        }
                        Map<Integer, PushVo> groupMap = map.get(pushVo.getActivityId());
                        // add to map
                        if (groupMap.containsKey(pushVo.getGroup())) {
                            if (pushVo.getGroupRank() > groupMap.get(pushVo.getGroup()).getGroupRank()
                                    || (pushVo.getGroupRank() == groupMap.get(pushVo.getGroup()).getGroupRank()
                                    && pushVo.getPushId() > groupMap.get(pushVo.getGroup()).getPushId())) {
                                groupMap.put(pushVo.getGroup(), pushVo);
                            }
                        } else {
                            groupMap.put(pushVo.getGroup(), pushVo);
                        }
                    }
                }
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("精准推送判断异常", cause);
            }
        }

        Set<Integer> pushIdSet = new HashSet<>();
        for (Map<Integer, PushVo> groupMap : map.values()) {
            for (PushVo vo : groupMap.values()) {
                pushIdSet.add(vo.getPushId());
            }
        }
        return pushIdSet;
    }

    /**
     * 外部方法调用时需要谨慎处理（所有条件都在外部判断）
     *
     * @param
     */
    public void activePush(Set<Integer> pushIdSet) {
        Map<Integer, PushVo> activedPushVoMap = activePush0(pushIdSet);
        PushActivedEvent event = new PushActivedEvent();
        for (PushVo vo : activedPushVoMap.values()) {
            event.addPushInfo(vo.getActivityId(), vo.getGroup(), vo.getPushId());
        }
        eventDispatcher().fire(event);
    }

    public Map<Integer, PushVo> activePush0(Set<Integer> pushIdSet) {
        Map<Integer, PushVo> activedPushVoMap = new HashMap<>();
        for (Integer pushId : pushIdSet) {
            PushVo vo = PushManager.getPushVo(pushId);
            if (activePush0(pushId) != 0) {
                activedPushVoMap.put(pushId, vo);
            }
        }
        return activedPushVoMap;
    }

    public int activePush(int pushId) {
        boolean flag = false;
        Map<Integer, PushVo> activedPushVoMap = new HashMap<>();
        PushVo vo = PushManager.getPushVo(pushId);
        if (activePush0(pushId) != 0) {
            activedPushVoMap.put(pushId, vo);
            PushActivedEvent event = new PushActivedEvent();
            event.addPushInfo(vo.getActivityId(), vo.getGroup(), vo.getPushId());
            eventDispatcher().fire(event);
            flag = true;
        }
        return flag ? 1 : 0;
    }

    public int activePush0(int pushId) {
        if (!satisfyTimes(pushId)) { // 判断一下次数
            return 0;
        }
        if (!satisfyGroup(pushId)) {
            return 0;
        }
        RolePushPo po = pushPoMap.get(pushId);
        if (po == null) {
            po = new RolePushPo(id(), pushId);
            pushPoMap.put(pushId, po);
            context().insert(po);
        }
        if (po.getState() == PushManager.STATE_INACTIVED) {
            po.setState(PushManager.STATE_ACTIVED);
            po.setNumberOfTimes(po.getNumberOfTimes() + 1);
            context().update(po);
            return pushId;
        }
        return 0;
    }

    public int inactivePush(int pushId) {
        return inactivePush(pushId, true);
    }

    public int inactivePush(int pushId, boolean needFireEvent) {
        RolePushPo pushPo = pushPoMap.get(pushId);
        if (pushPo == null) {
            return 0;
        }
        // 先将对应推送的状态改成inactived
        if (pushPo != null && pushPo.getState() != PushManager.STATE_INACTIVED) {
            pushPo.setState(PushManager.STATE_INACTIVED);
            context().update(pushPo);
            if (needFireEvent) {
                eventDispatcher().fire(new PushInactivedEvent(pushId));
            }
        }
        // 再检查
        PushVo pushVo = PushManager.getPushVo(pushId);
        Map<Integer, PushVo> groupMap = PushManager.getPushVoMapByGroup(pushVo.getActivityId(), pushVo.getGroup());
        List<PushVo> voList = new ArrayList<>();
        for (PushVo vo : groupMap.values()) {
            try {
                if (satisfyCondition(vo) && satisfyDate(vo) && satisfyTimes(vo) && satisfyGroup(vo)) {
                    voList.add(vo);
                }
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("", cause);
            }
        }
        PushVo maxRankPushVo = null;
        if (voList.size() != 0) {
            for (PushVo vo : voList) {
                if (maxRankPushVo == null
                        || maxRankPushVo.getGroupRank() < vo.getGroupRank()
                        || (maxRankPushVo.getGroupRank() == vo.getGroupRank() && maxRankPushVo.getPushId() > vo.getPushId())) {
                    maxRankPushVo = vo;
                }
            }
            if (maxRankPushVo != null) {
                if (needFireEvent) {
                    return activePush(maxRankPushVo.getPushId());
                } else {
                    return activePush0(maxRankPushVo.getPushId());
                }
            }
        }
        return 0;
    }


    public boolean isActived(int pushId) {
        RolePushPo po = pushPoMap.get(pushId);
        if (po != null) {
            return po.getState() == PushManager.STATE_INACTIVED;
        }
        return false;
    }

    public boolean satisfyCondition(int pushId) {
        return satisfyCondition(PushManager.getPushVo(pushId));
    }

    /**
     * 判断同一组不能重复加入
     *
     * @param vo
     * @return
     */
    private boolean satisfyGroup(PushVo vo) {
        Set<Integer> groupSet = new HashSet<>();
        for (Map.Entry<Integer, RolePushPo> entry : pushPoMap.entrySet()) {
            RolePushPo rolePushPo = entry.getValue();
            if (rolePushPo.getState() == PushManager.STATE_ACTIVED) {
                PushVo pushVo = PushManager.getPushVo(rolePushPo.getPushId());
                int group = pushVo.getGroup();
                groupSet.add(group);
            }
        }
        return !groupSet.contains(vo.getGroup());
    }

    private boolean satisfyGroup(int pushId) {
        return satisfyGroup(PushManager.getPushVo(pushId));
    }

    public boolean satisfyCondition(PushVo vo) {
        if (vo == null) {
            return false;
        }
        return (Long) vo.getCondChecker().eval(moduleMap()) != 0L;
    }

    public boolean satisfyDate(int pushId) {
        return satisfyDate(PushManager.getPushVo(pushId));
    }

    public boolean satisfyDate(PushVo vo) {
        if (vo == null) {
            return false;
        }
        long now = now();
        return !vo.hasExpirationDate()
                || (now >= vo.getBeginTimeMillis() && now <= vo.getEndTimeMillis());
    }

    public boolean satisfyTimes(int pushId) {
        return satisfyTimes(PushManager.getPushVo(pushId));
    }

    public boolean satisfyTimes(PushVo vo) {
        if (vo == null || vo.getTimes() <= 0) {
            return false;
        }
        RolePushPo po = pushPoMap.get(vo.getPushId());
        if (po != null) {
            return po.getNumberOfTimes() < vo.getTimes();
        }
        return true;
    }

    public void finishPush(int pushId, int times) {
        try {
            ServerLogModule serverLogModule = module(MConst.ServerLog);
//            serverLogModule.logPushFinish(pushId, times);
            serverLogModule.logPrecisionPushFinish(pushId);
        } catch (Throwable cause) {
            LogUtil.info("推送|异常:finishPush", cause);
        }
    }

    private void resetDaily() {
        Set<RolePushPo> delPushPoSet = new HashSet<>();
        Map<Integer, RolePushPo> tmpPushPoMap=new HashMap<>(pushPoMap);
        for (RolePushPo po : tmpPushPoMap.values()) {
            PushVo vo = PushManager.getPushVo(po.getPushId());
            if (vo.getType() == PushManager.TYPE_DAY) {
                if (po.isActived()) {
                    po.setNumberOfTimes(0);
                    inactivePush(po.getPushId(), false);
                } else {
                    delPushPoSet.add(po);
                }
            }
        }
        for (RolePushPo po : delPushPoSet) {
            pushPoMap.remove(po.getPushId());
            context().delete(po);
        }
    }

    private void resetWeekly() {
        Set<RolePushPo> delPushPoSet = new HashSet<>();
        Map<Integer, RolePushPo> tmpPushPoMap = new HashMap<>(pushPoMap);
        for (RolePushPo po : tmpPushPoMap.values()) {
            PushVo vo = PushManager.getPushVo(po.getPushId());
            if (vo.getType() == PushManager.TYPE_WEEK) {
                if (po.isActived()) {
                    po.setNumberOfTimes(0);
                    inactivePush(po.getPushId(), false);
                } else {
                    delPushPoSet.add(po);
                }
            }
        }
        for (RolePushPo po : delPushPoSet) {
            pushPoMap.remove(po.getPushId());
            context().delete(po);
        }
    }

}
