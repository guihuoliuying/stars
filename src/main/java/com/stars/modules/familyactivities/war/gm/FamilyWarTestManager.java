package com.stars.modules.familyactivities.war.gm;

import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenkeyu on 2017-09-11.
 */
public class FamilyWarTestManager {

    public static Map<String, Long> fightIdTimeStamp = new HashMap<>();
    public static Map<String, Integer> fightId2FightServerId = new HashMap<>();
    public static int outTime;

    public void init(int outTime) {
        FamilyWarTestManager.outTime = outTime;
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.TEST, new Runnable() {
            @Override
            public void run() {
                doTest();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void doTest() {
        if (fightIdTimeStamp.isEmpty()) return;
        Iterator<Map.Entry<String, Long>> timsStamp = fightIdTimeStamp.entrySet().iterator();
        while (timsStamp.hasNext()) {
            Map.Entry<String, Long> entry = timsStamp.next();
            if (outTime > (System.currentTimeMillis() - entry.getValue()) / 1000) {
                LogUtil.info("结束战斗的逻辑|fightId:{}", entry.getKey());
                MainRpcHelper.fightBaseService().stopFight(fightId2FightServerId.get(entry.getKey()),
                        FightConst.T_FAMILY_WAR_ELITE_FIGHT, MultiServerHelper.getServerId(), entry.getKey());
                timsStamp.remove();
            }
        }
    }

    public static void addFightId(String fightId, int fightServerId) {
        fightIdTimeStamp.put(fightId, System.currentTimeMillis());
        fightId2FightServerId.put(fightId, fightServerId);
    }
}
