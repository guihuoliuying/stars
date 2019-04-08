package com.stars.multiserver.fight.handler.phasespk;

import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.stars.multiserver.fight.handler.phasespk.PhasesPkFightManager.*;

/**
 * Created by zhaowenshuo on 2016/11/30.
 */
public class PhasesPkFightManager {

    public static final byte PHASE_INITIAL = 0; // 初始阶段（对战双方信息展示）
    public static final byte PHASE_CLIENT_PREPARATION = 1; // 客户端预加载阶段
    public static final byte PHASE_USER_PREPARATION = 2; // 用户准备阶段
    public static final byte PHASE_FIGHT = 3; // 战斗阶段
    public static final byte PHASE_TIMEOUT = 4; // 战斗超时

    static ConcurrentMap<String, PhasesPkFightTimeRecord> initialPhaseCheckMap; //
    static ConcurrentMap<String, PhasesPkFightTimeRecord> clientPreparationPhaseCheckMap; //
    static ConcurrentMap<String, PhasesPkFightTimeRecord> timeoutPhaseCheckMap; // 意义不大


    static {
        initialPhaseCheckMap = new ConcurrentHashMap<>();
        clientPreparationPhaseCheckMap = new ConcurrentHashMap<>();
        timeoutPhaseCheckMap = new ConcurrentHashMap<>();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.PhasesPk,new PhasesPkFightTimeoutCheckTask(), 500, 500, TimeUnit.MILLISECONDS);
    }

    public static void addInitialPhase(String fightId, long timeLimit) {
        PhasesPkFightTimeRecord record = new PhasesPkFightTimeRecord(now(), timeLimit);
        initialPhaseCheckMap.putIfAbsent(fightId, record);
    }

    public static void addClientPreparationPhase(String fightId, long timeLimit) {
//        LogUtil.error("", new Exception());
        PhasesPkFightTimeRecord record = new PhasesPkFightTimeRecord(now(), timeLimit);
        clientPreparationPhaseCheckMap.putIfAbsent(fightId, record);
    }

    public static boolean removeClientPreparationPhase(String fightId) {
        return clientPreparationPhaseCheckMap.remove(fightId) != null;
    }

    private static long now() {
        return System.currentTimeMillis();
    }

}

class PhasesPkFightTimeRecord {
    long timestamp;
    long timeLimit;

    public PhasesPkFightTimeRecord(long timestamp, long timeLimit) {
        this.timestamp = timestamp;
        this.timeLimit = timeLimit;
    }
}

class PhasesPkFightTimeoutCheckTask implements Runnable {
    @Override
    public void run() {
        // 初始阶段超时检查
        checkPhase(PHASE_INITIAL, initialPhaseCheckMap);
        // 客户端准备超时检查
        checkPhase(PHASE_CLIENT_PREPARATION, clientPreparationPhaseCheckMap);
        checkPhase(PHASE_TIMEOUT, timeoutPhaseCheckMap);
    }

    private void checkPhase(byte phase, ConcurrentMap<String, PhasesPkFightTimeRecord> checkMap) {
        long now = System.currentTimeMillis();
        // 初始阶段超时检查
        List<String> initialPhaseFightIdList = new ArrayList<>(checkMap.keySet());
        for (String fightId : initialPhaseFightIdList) {
            PhasesPkFightTimeRecord record = checkMap.get(fightId);
            if (record == null) {
                continue;
            }
            if (now - record.timestamp > record.timeLimit) {
                record = checkMap.remove(fightId);
                if (record == null) {
                    continue;
                }
                Actor actor = ActorServer.getActorSystem().getActor(fightId);
                if (actor == null) {
                    continue;
                }
                try {
                    actor.tell(new PhasePkFightTimeoutMessage(fightId, phase), Actor.noSender);
                } catch (Exception e) {
                    LogUtil.error("", e);
                }
            }
        }
    }
}
