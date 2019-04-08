package com.stars.services.callboss;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.callboss.CallBossManager;
import com.stars.modules.callboss.event.CallBossStatusChangeEvent;
import com.stars.modules.callboss.packet.ClientCallBossPo;
import com.stars.modules.callboss.packet.ClientCallBossRank;
import com.stars.modules.callboss.packet.ClientCallBossRankList;
import com.stars.modules.callboss.packet.ClientCallBossVo;
import com.stars.modules.callboss.prodata.CallBossVo;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.email.EmailManager;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.callboss.cache.CallBossCache;
import com.stars.services.callboss.cache.CallRecordCache;
import com.stars.services.callboss.cache.RoleDamageCache;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.MapUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liuyuheng on 2016/9/5.
 */
public class CallBossServiceActor extends ServiceActor implements CallBossService {
    private AtomicInteger callBossIdSeq = new AtomicInteger(0);// 召唤自增序列
    private Map<Integer, CallBossCache> bossCacheMap;// boss状态
    private Map<Integer, CallRecordCache> recordCacheMap;// 召唤记录(排行榜),<recordUId,cache>

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("callBossService", this);
        bossCacheMap = new HashMap<>();
        recordCacheMap = new HashMap<>();
        synchronized (CallBossServiceActor.class) {
            // 初始化boss状态
            for (CallBossVo callBossVo : CallBossManager.callBossVoMap.values()) {
                byte status = CallBossConstant.BOSS_STATUS_ERROR_TIME;
                if (callBossVo.getWeekDayList().contains(DateUtil.getChinaWeekDay())
                        && System.currentTimeMillis() >= DateUtil.hourStrTimeToDateTime(CallBossManager.startTime).getTime()
                        && System.currentTimeMillis() <= DateUtil.hourStrTimeToDateTime(CallBossManager.endTime).getTime())
                    status = CallBossConstant.BOSS_STATUS_AVAILABLE;// 可召唤状态
                CallBossCache callBossCache = new CallBossCache(callBossVo.getBossId(), status);
                bossCacheMap.put(callBossVo.getBossId(), callBossCache);
            }
        }
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.CallBoss, new TimingTask(), 0, 2000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void printState() {

    }

    @Override
    public void sendCallBossData(long roleId) {
        ClientCallBossVo clientCallBossVo = new ClientCallBossVo(CallBossManager.callBossVoMap);
        PlayerUtil.send(roleId, clientCallBossVo);
        Map<Integer, CallBossCache> sendMap = new HashMap<>();
        for (int bossId : bossCacheMap.keySet()) {
            CallBossCache copyCache = getCallBossCache(roleId, bossId);
            sendMap.put(copyCache.getBossId(), copyCache);
            if (copyCache.getStatus() != CallBossConstant.BOSS_STATUS_ALIVE)
                continue;
            // 根据缓存伤害值判断能否进入击杀,修改状态
            CallBossVo callBossVo = CallBossManager.getCallBossVo(bossId);
            MonsterAttributeVo monsterAttr = SceneManager.getMonsterAttrVo(callBossVo.getStageMonsterId());
            if (calCallBossHp(monsterAttr.getHp(), callBossVo.getLiveTime(), copyCache.getCallTime()) -
                    copyCache.getReceiveDamage() <= 0) {
                copyCache.setStatus(CallBossConstant.BOSS_STATUS_DEAD);
            }
        }
        ClientCallBossPo clientCallBossPo = new ClientCallBossPo(sendMap);
        PlayerUtil.send(roleId, clientCallBossPo);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "请求召唤boss数据", true));
        }
    }

    @Override
    public void sendCallBossData(long roleId, int bossId) {
        Map<Integer, CallBossCache> sendMap = new HashMap<>();

        CallBossCache copyCache = getCallBossCache(roleId, bossId);
        // 根据缓存伤害值判断能否进入击杀,修改状态
        CallBossVo callBossVo = CallBossManager.getCallBossVo(bossId);
        MonsterAttributeVo monsterAttr = SceneManager.getMonsterAttrVo(callBossVo.getStageMonsterId());
        if (calCallBossHp(monsterAttr.getHp(), callBossVo.getLiveTime(), copyCache.getCallTime()) -
                copyCache.getReceiveDamage() <= 0) {
            copyCache.setStatus(CallBossConstant.BOSS_STATUS_DEAD);
        }

        sendMap.put(copyCache.getBossId(), copyCache);

        ClientCallBossPo clientCallBossPo = new ClientCallBossPo(sendMap);
        PlayerUtil.send(roleId, clientCallBossPo);
    }

    @Override
    public CallBossCache getCallBossCache(long roleId, int bossId) {
        if (!bossCacheMap.containsKey(bossId))
            throw new IllegalArgumentException("找不到boss状态缓存,bossId=" + bossId);
        // copy一个新的
        CallBossCache callBossCache = bossCacheMap.get(bossId).copy();
        CallRecordCache recordCache = recordCacheMap.get(callBossIdSeq.get());
        if (recordCache == null)
            return callBossCache;
        // 防错判断
        if (recordCache.getBossId() != bossId)
            return callBossCache;
        // 注入角色造成的伤害缓存
        callBossCache.setReceiveDamage(recordCache.getRoleDamageRank(roleId) == null ? 0 :
                recordCache.getRoleDamageRank(roleId).getDamage());
        return callBossCache;
    }

    @Override
    public boolean executeCallBoss(long roleId, String roleName, int bossId, byte rewardGroupId) {
        CallBossCache callBossCache = bossCacheMap.get(bossId);
        if (!canExecuteCall() || callBossCache.getStatus() != CallBossConstant.BOSS_STATUS_AVAILABLE) {
            PlayerUtil.send(roleId, new ClientText("callboss_bosscding"));
            return false;
        }
        callBossCache.setCallRoleId(roleId);
        callBossCache.setRoleName(roleName);
        callBossCache.setSelectRewardId(rewardGroupId);
        callBossCache.setCallTime(System.currentTimeMillis());
        callBossCache.setStatus(CallBossConstant.BOSS_STATUS_ALIVE);
        // 创建一个记录
        CallRecordCache callRecordCache = new CallRecordCache(callBossIdSeq.incrementAndGet(), bossId, roleId,
                roleName, rewardGroupId);
        recordCacheMap.put(callBossIdSeq.get(), callRecordCache);
        // 下发boss状态
        ClientCallBossPo packet = new ClientCallBossPo(callBossCache);
        // 先发给召唤者,然后通知在线玩家boss状态改变
        PlayerUtil.send(roleId, packet);
        ServiceHelper.callBossService().innerUpdateBossToOnline(packet, roleId);
        //发滚动公告给在线玩家
        CallBossVo callBossVo = CallBossManager.getCallBossVo(bossId);
        if (callBossVo != null) {
            ServiceHelper.chatService().announce("callboss_information", roleName, callBossVo.getName());
        }

        //通知boss状态发生变化
        ServiceHelper.roleService().noticeAll(new CallBossStatusChangeEvent());

        return true;
    }

    @Override
    public void addRoleDamage(long roleId, String roleName, int bossId, int damage) {
        CallRecordCache callRecordCache = recordCacheMap.get(callBossIdSeq.get());
        if (callRecordCache.getBossId() != bossId)
            return;
        callRecordCache.addRoleDamage(new RoleDamageCache(roleId, roleName, damage));
    }

    @Override
    public void sendDamageRankList(long roleId) {
        ClientCallBossRankList packet = new ClientCallBossRankList(recordCacheMap, roleId);
        PlayerUtil.send(roleId, packet);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "请求召唤boss排行榜列表", true));
        }
    }

    @Override
    public void sendDamageRank(long roleId, int rankUniqueId) {
        List<RoleDamageCache> list = new LinkedList<>();
        CallRecordCache callRecordCache = recordCacheMap.get(rankUniqueId);
        if (callRecordCache == null) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("callboss.rankIdError")));
            return;
        }
        CallBossVo callBossVo = CallBossManager.getCallBossVo(callRecordCache.getBossId());
        int count = 0;
        Iterator<RoleDamageCache> iterator = callRecordCache.getTreeSet().iterator();
        while (iterator.hasNext() && count < callBossVo.getRankDisplayRow()) {
            list.add(iterator.next());
            count++;
        }
        if (callRecordCache.getRoleDamageRank(roleId) != null) {
            list.add(callRecordCache.getRoleDamageRank(roleId));
        }
        // send rank
        ClientCallBossRank packet = new ClientCallBossRank(callRecordCache.getUniqueId(), list);
        PlayerUtil.send(roleId, packet);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "请求召唤boss伤害排行榜", true));
        }
    }

    @Override
    public void dailyReset() {
        CallRecordCache notFinishCache = null;
        if (recordCacheMap.containsKey(callBossIdSeq.get())) {
            CallBossCache bossCache = bossCacheMap.get(recordCacheMap.get(callBossIdSeq.get()).getBossId());
            if (bossCache.getStatus() == CallBossConstant.BOSS_STATUS_ALIVE
                    || bossCache.getStatus() == CallBossConstant.BOSS_STATUS_DEAD) {
                notFinishCache = recordCacheMap.get(callBossIdSeq.get());
            }
        }
        recordCacheMap = new HashMap<>();
        if (notFinishCache != null) {
            recordCacheMap.put(notFinishCache.getUniqueId(), notFinishCache);
        }
    }

    @Override
    public void reward(int recordUId) {
        CallRecordCache recordCache = recordCacheMap.get(recordUId);
        CallBossVo callBossVo = CallBossManager.getCallBossVo(recordCache.getBossId());
        // 先发召唤者奖励(固定+选择)
        Map<Integer, Integer> voCallerReward = callBossVo.getCallReward();
        //callerReward.
//        StringUtil.combineIntegerMap(callerReward, callBossVo.getSelectRewardMap().get(recordCache.getSelectRewardId()));
        Map<Integer, Integer> callerReward = new HashMap<>();
        MapUtil.add(callerReward, voCallerReward);
        MapUtil.add(callerReward, callBossVo.getSelectRewardMap().get(recordCache.getSelectRewardId()));
        // 第一个通配符为召唤出的boss名次，第二个通配为持续时间
        ServiceHelper.emailService().sendToSingle(
                recordCache.getCallRoleId(), 12002, Long.valueOf(recordUId), "系统", callerReward,
                EmailManager.GAMETEXT_PREFIX + callBossVo.getName(), formatTime(recordCache.getCallTime(),
                        callBossVo.getCdTimeMs()));
        // 再发排名奖励
        Iterator<RoleDamageCache> iterator = recordCache.getTreeSet().iterator();
        int count = 0;// 已发奖排名
        int awardRankMax = 0;// 奖励最大排名
        do {
            if (!iterator.hasNext()) break;
            RoleDamageCache roleDamageCache = iterator.next();
            for (Map.Entry<int[], Map<Integer, Integer>> entry : callBossVo.getRankRewardMap().entrySet()) {
                if (awardRankMax < entry.getKey()[1])
                    awardRankMax = entry.getKey()[1];
                if (entry.getKey()[0] <= roleDamageCache.getRank() && roleDamageCache.getRank() <= entry.getKey()[1]) {
                    // 调用邮件接口发奖
                    // 第一个通配符替换为召唤出boss的玩家名，第二个通配符替换为召唤出的boss名称，
                    // 第三个通配符替换为boss持续的时间，如11:30~11:40，第四个通配符替换为玩家在伤害榜中所排的名次
                    ServiceHelper.emailService().sendToSingle(
                            roleDamageCache.getRoleId(), 12001, Long.valueOf(recordUId), "系统", entry.getValue(),
                            recordCache.getCallRoleName(), EmailManager.GAMETEXT_PREFIX + callBossVo.getName(),
                            formatTime(recordCache.getCallTime(), callBossVo.getCdTimeMs()),
                            String.valueOf(roleDamageCache.getRank()));
                    break;
                }
            }
            count++;
        } while (count <= awardRankMax && iterator.hasNext());
    }

    @Override
    public void innerUpdateBossToOnline(PlayerPacket packet, long... exceptRoleIds) {
        for (Actor actor : PlayerSystem.system().getActors().values()) {
            try {
                if (actor instanceof Player && !Arrays.asList(exceptRoleIds).contains(((Player) actor).id())) {
                    PlayerUtil.send(((Player) actor).id(), packet);
                }
            } catch (Throwable cause) {
            }
        }
    }

    /**
     * 拼接策划所需的时间格式字符串
     *
     * @param startstamp
     * @param cdTimeMs
     * @return
     */
    public String formatTime(long startstamp, long cdTimeMs) {
        StringBuilder builder = new StringBuilder("");
        builder.append(DateUtil.getHM(startstamp))
                .append("~")
                .append(DateUtil.getHM(startstamp + cdTimeMs));
        return builder.toString();
    }

    /* 内部使用 */

    /**
     * 定时检测boss状态,改变推送给客户端(异步)
     */
    @Override
    public void checkBossStatus() {
        Map<Integer, CallBossCache> changeMap = new HashMap<>();
        for (CallBossCache bossCache : bossCacheMap.values()) {
            CallBossVo callBossVo = CallBossManager.getCallBossVo(bossCache.getBossId());
            switch (bossCache.getStatus()) {
                case CallBossConstant.BOSS_STATUS_ALIVE:
                    // alive ——》 dead
                    if (System.currentTimeMillis() - bossCache.getCallTime() > callBossVo.getLiveTimeMs()) {
                        bossCache.setStatus(CallBossConstant.BOSS_STATUS_DEAD);
                    }
                    changeMap.put(bossCache.getBossId(), bossCache);
                    break;
                case CallBossConstant.BOSS_STATUS_DEAD:
                    // dead ——》 available
                    if (System.currentTimeMillis() - bossCache.getCallTime() > callBossVo.getCdTimeMs()) {
                        // 通知自己Actor发奖
                        ServiceHelper.callBossService().reward(callBossIdSeq.get());
                        bossCache.setCallRoleId(0);
                        bossCache.setRoleName("");
                        bossCache.setSelectRewardId((byte) 0);
                        bossCache.setStatus(CallBossConstant.BOSS_STATUS_AVAILABLE);
                        changeMap.put(bossCache.getBossId(), bossCache);
                    }
                    break;
                case CallBossConstant.BOSS_STATUS_AVAILABLE:
                    // available ——》 errortime
                    if (System.currentTimeMillis() > DateUtil.hourStrTimeToDateTime(CallBossManager.endTime).getTime()) {
                        bossCache.setStatus(CallBossConstant.BOSS_STATUS_ERROR_TIME);
                        changeMap.put(bossCache.getBossId(), bossCache);
                    }
                    break;
                case CallBossConstant.BOSS_STATUS_ERROR_TIME:
                    // errortime ——》 available
                    // 是否在指定日期&时间
                    if (callBossVo.getWeekDayList().contains(DateUtil.getChinaWeekDay())
                            && System.currentTimeMillis() >= DateUtil.hourStrTimeToDateTime(CallBossManager.startTime).getTime()
                            && System.currentTimeMillis() <= DateUtil.hourStrTimeToDateTime(CallBossManager.endTime).getTime()) {
                        bossCache.setStatus(CallBossConstant.BOSS_STATUS_AVAILABLE);
                        changeMap.put(bossCache.getBossId(), bossCache);
                    }
                    break;
                default:
                    break;
            }
        }
        if (changeMap.isEmpty())
            return;
        ClientCallBossPo packet = new ClientCallBossPo(changeMap);
        ServiceHelper.callBossService().innerUpdateBossToOnline(packet);

        //通知boss状态发生变化
        ServiceHelper.roleService().noticeAll(new CallBossStatusChangeEvent());
    }

    /**
     * 排行榜遍历,更新排名(只排当前召唤boss)
     */
    @Override
    public void rankSort() {
        CallRecordCache recordCache = recordCacheMap.get(callBossIdSeq.get());
        if (recordCache == null)
            return;
        Iterator<RoleDamageCache> iterator = recordCache.getTreeSet().iterator();
        int ranking = 1;
        while (iterator.hasNext()) {
            RoleDamageCache roleDamageCache = iterator.next();
            roleDamageCache.setRank(ranking);
            recordCache.updateRoleDamageRank(roleDamageCache);
            ranking++;
        }
    }

    /**
     * 定时执行任务
     */
    class TimingTask implements Runnable {
        @Override
        public void run() {
            ServiceHelper.callBossService().checkBossStatus();
            ServiceHelper.callBossService().rankSort();
        }
    }

    /**
     * 计算当前召唤boss血量
     * 总血量 - 时间扣除血量(向上取整)
     *
     * @param fullHp
     * @param liveTime
     * @param startTimestamp
     * @return
     */
    private int calCallBossHp(int fullHp, int liveTime, long startTimestamp) {
        return (fullHp - (int) Math.ceil((System.currentTimeMillis() - startTimestamp) / 1000.0 * fullHp / liveTime));
    }

    /* 判断当前是否有冷却中的boss */
    private boolean canExecuteCall() {
        for (CallBossCache bossCache : bossCacheMap.values()) {
            if (bossCache.getStatus() == CallBossConstant.BOSS_STATUS_ALIVE ||
                    bossCache.getStatus() == CallBossConstant.BOSS_STATUS_DEAD) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获得存活boss
     * 红点用
     */
    @Override
    public Set<Integer> getAliveBossIds(long roleId) {
        Set<Integer> ret = new HashSet<Integer>();

        for (int bossId : bossCacheMap.keySet()) {
            CallBossCache copyCache = getCallBossCache(roleId, bossId);
            if (copyCache.getStatus() != CallBossConstant.BOSS_STATUS_ALIVE)
                continue;
            // 根据缓存伤害值判断能否进入击杀,修改状态
            CallBossVo callBossVo = CallBossManager.getCallBossVo(bossId);
            MonsterAttributeVo monsterAttr = SceneManager.getMonsterAttrVo(callBossVo.getStageMonsterId());
            if (calCallBossHp(monsterAttr.getHp(), callBossVo.getLiveTime(), copyCache.getCallTime()) -
                    copyCache.getReceiveDamage() <= 0) {
                copyCache.setStatus(CallBossConstant.BOSS_STATUS_DEAD);
            }

            if (copyCache.getStatus() == CallBossConstant.BOSS_STATUS_ALIVE) {
                ret.add(bossId);
            }
        }

        return ret;
    }
}
