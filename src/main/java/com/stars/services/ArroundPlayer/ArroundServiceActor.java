package com.stars.services.ArroundPlayer;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.arroundPlayer.Packet.ClientArroundPlayer;
import com.stars.modules.positionsync.PositionSyncManager;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.family.FamilyAuth;
import com.stars.services.marry.userdata.MarryWedding;
import com.stars.services.postsync.PositionSyncRelationServiceImpl;
import com.stars.services.postsync.aoi.AoiCallback;
import com.stars.services.postsync.aoi.PositionSyncAoiServiceImpl;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class ArroundServiceActor extends ServiceActor implements ArroundPlayerService {

    private Map<String, Map<Long, ArroundPlayer>> sceneArroundPlayers;
    private Map<Long, ArroundPlayer> allArroundPlayers;
    private Map<Long, List<Long>> roleLastFlushRecord;

    private PositionSyncAoiServiceImpl aoiService; // 按距离
    private PositionSyncRelationServiceImpl relationService; // 关系表
    private long lastFlushTimestamp; // 上次同步时间戳

    private ClientArroundPlayer cap;

    public static int SEND_ARROUNDPLAYER_COUNT = 30;

    public ArroundServiceActor() {

    }

    @Override
    public void init() throws Throwable {
        sceneArroundPlayers = new HashMap<>();
        allArroundPlayers = new HashMap<>();
        roleLastFlushRecord = new HashMap<>(); // 废弃
        cap = new ClientArroundPlayer(SEND_ARROUNDPLAYER_COUNT);
        aoiService = new PositionSyncAoiServiceImpl();
        relationService = new PositionSyncRelationServiceImpl();
        ServiceSystem.getOrAdd("arroundPlayerActor", this);

        SchedulerManager.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    ServiceHelper.arroundPlayerService().flush();
                } catch (Throwable cause) {
                    LogUtil.error("", cause);
                }
            }
        }, 10_000, 1_000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{}, sceneArroundPlayers:{}, allArroundPlayers:{}, aoiSceneMap:{}",
                this.getClass().getSimpleName(), sceneArroundPlayers.size(), allArroundPlayers.size(), aoiService.toString());
    }

    @Override
    public void addArroundPlayer(ArroundPlayer newPlayer) {

        ArroundPlayer player = allArroundPlayers.get(newPlayer.getRoleId());
        if (player != null) { // 存在旧有数据的问题
            Map<Long, ArroundPlayer> scenePlayerMap = sceneArroundPlayers.get(player.getSceneId());
            if (scenePlayerMap != null) {
                scenePlayerMap.remove(player.getRoleId());
            }
            aoiService.leave(player.getRoleId(), player.getSceneId());
            player.setSceneId(newPlayer.getSceneId());
            player.setLevel(newPlayer.getLevel());
            player.setX(newPlayer.getX());
            player.setY(newPlayer.getY());
            player.setZ(newPlayer.getZ());
            player.setFamilyId(newPlayer.getFamilyId());
            player.setOriginSceneId(newPlayer.getOriginSceneId());
            player.setFightScore(newPlayer.getFightScore());
            player.setCurFashionId(newPlayer.getCurFashionId());
            player.setDragonBallList(newPlayer.getDragonBallList());
        } else {
            allArroundPlayers.put(newPlayer.getRoleId(), newPlayer);
            player = newPlayer;
        }

        if (!sceneArroundPlayers.containsKey(player.getSceneId())) {
            sceneArroundPlayers.put(player.getSceneId(), new HashMap<Long, ArroundPlayer>());
        }
        sceneArroundPlayers.get(player.getSceneId()).put(player.getRoleId(), player);

        aoiService.enter(player.getRoleId(), player.getSceneId(), player.getSceneType(), player.getX(), player.getZ());
        relationService.addRelation(player.getRoleId());
        relationService.updateVip(player.getRoleId(), newPlayer.getCutVipLevel());
    }

    @Override
    public void removeArroundPlayer(String scenceId, long roleId) {
        ArroundPlayer player = allArroundPlayers.get(roleId);
        if (player != null) {
            allArroundPlayers.remove(roleId);
            Map<Long, ArroundPlayer> scenePlayerMap = sceneArroundPlayers.get(player.getSceneId());
            if (scenePlayerMap != null) {
                scenePlayerMap.remove(roleId);
                if (scenePlayerMap.size() == 0) {
                    sceneArroundPlayers.remove(player.getSceneId()); // 可能有随机场景id
                }
            }
        }
        Map<Long, ArroundPlayer> scenePlayerMap = sceneArroundPlayers.get(scenceId);
        if (scenePlayerMap != null && scenePlayerMap.remove(roleId) != null) {
            if (scenePlayerMap.size() == 0) {
                sceneArroundPlayers.remove(scenceId); // 可能有随机场景id
            }
        }
//        roleLastFlushRecord.remove(roleId);

        aoiService.leave(roleId, scenceId);
    }

    public ArroundPlayer getArroundPlayer(String scenceId, long roleId) {
        Map<Long, ArroundPlayer> map = sceneArroundPlayers.get(scenceId);
        if (map == null) {
            return null;
        }
        return map.get(roleId);
    }

    private List<Long> getLastFlushRecord(long roleId, Map<Long, ArroundPlayer> map) {
//        List<Long> lastRecord = roleLastFlushRecord.get(roleId);
//        if (StringUtil.isEmpty(lastRecord)) return lastRecord;
//        List<Long> newRecord = new ArrayList<>();
//        Set<Long> set = map.keySet();
//        for (Long id : lastRecord) {
//            if (set.contains(id)) {
//                newRecord.add(id);
//            }
//        }
//        return newRecord;
        return null;
    }

    private void updateLastFlushRecord(long roleId, List<Long> list) {
//        roleLastFlushRecord.put(roleId, list);
    }

    @Override
    public void flushArroundPlayers(String scenceId, long roleId, Object sceneMsg) {
//        Map<Long, ArroundPlayer> map = sceneArroundPlayers.get(scenceId);
//        if (map == null || map.size() == 0) {
//            return;
//        }
//        cap.setIndex((byte) 0);
//        List<Long> newRecord = new ArrayList<>();
//        Object[] os = map.values().toArray();
//        if (os.length > SEND_ARROUNDPLAYER_COUNT) {
//            {
//                // 婚宴场景特殊处理，如果婚宴双方在场景，必须同步
//                ArroundPlayer self = map.get(roleId);
//                if (null != self && self.getSceneType() == SceneManager.SCENETYPE_WEDDING) {
//                    MarryWedding wedding = ServiceHelper.marryService().getWeddingSync(self.getArroundId());
//                    if (null != wedding) {
//                        Set<Long> unit = wedding.getWeddingUnit();
//                        for (Long id : unit) {
//                            if (id != roleId && null != map.get(id)) {
//                                arroundPlayerSpecialAccountLog(id, roleId, scenceId);
//                                cap.addArroundPlayer(map.get(id));
//                                newRecord.add(id);
//                            }
//                        }
//                    }
//                }
//            }
//
//            List<Long> lastRecord = getLastFlushRecord(roleId, map);
//            int size = 0;
//            ArroundPlayer ap;
//            if (StringUtil.isNotEmpty(lastRecord)) {
//                for (long id : lastRecord) {
//                    ap = map.get(id);
//                    if (ap == null) continue;
//                    if (ap.getRoleId() != roleId && !newRecord.contains(ap.getRoleId())) {
//                        arroundPlayerSpecialAccountLog(ap.getRoleId(), roleId, scenceId);
//                        cap.addArroundPlayer(ap);
//                        newRecord.add(ap.getRoleId());
//                    }
//                }
//                size = newRecord.size();
//            }
//
//            int delta = os.length / SEND_ARROUNDPLAYER_COUNT;
//            int residue = os.length % SEND_ARROUNDPLAYER_COUNT;
//            Random r = new Random();
//            int index = 0;
//            for (int i = size; i < SEND_ARROUNDPLAYER_COUNT; i++) {
//                if (i == SEND_ARROUNDPLAYER_COUNT - 1 && residue > 0) {
//                    ap = (ArroundPlayer) os[index + r.nextInt(delta + residue)];
//                } else {
//                    ap = (ArroundPlayer) os[index + r.nextInt(delta)];
//                }
//                index = index + delta;
//                if (ap.getRoleId() != roleId && !newRecord.contains(ap.getRoleId())) {
//                    arroundPlayerSpecialAccountLog(ap.getRoleId(), roleId, scenceId);
//                    cap.addArroundPlayer(ap);
//                    newRecord.add(ap.getRoleId());
//                }
//            }
//        } else {
//            for (Object object : os) {
//                ArroundPlayer ap = (ArroundPlayer) object;
//                if (ap.getRoleId() != roleId) {
//                    arroundPlayerSpecialAccountLog(ap.getRoleId(), roleId, scenceId);
//                    cap.addArroundPlayer(ap);
//                    newRecord.add(ap.getRoleId());
//                }
//            }
//        }
//        updateLastFlushRecord(roleId, newRecord);//更新刷新周围玩家记录
//        ServiceHelper.familyEscortService().updateFlushArroundPlayerList(roleId, sceneMsg, newRecord);
//        PlayerUtil.send(roleId, cap);
    }

    private void arroundPlayerSpecialAccountLog(long roleId, long self, String scenceId) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(self, new SpecialAccountEvent(roleId, "出现在" + scenceId + "场景中", false));
        }
    }

    @Override
    public void updateLevel(String scenceId, long roleId, short level) {
        ArroundPlayer p = getArroundPlayer(scenceId, roleId);
        if (p != null) {
            p.setLevel(level);
        }
    }

    @Override
    public void updatePosition(String scenceId, long roleId, int[] p) {
        ArroundPlayer ap = getArroundPlayer(scenceId, roleId);
        if (ap != null) {
            ap.setX(p[0]);
            ap.setY(p[1]);
            ap.setZ(p[2]);
        }
        aoiService.update(roleId, scenceId, p[0], p[2]);
    }

    @Override
    public void updateActiveRideId(String scenceId, long roleId, int activeRideId) {
        ArroundPlayer player = getArroundPlayer(scenceId, roleId);
        if (player != null) {
            player.setActiveRideId(activeRideId);
        }
    }

    @Override
    public void updateCurFashionId(String scenceId, long roleId, int curFashionId) {
        ArroundPlayer player = getArroundPlayer(scenceId, roleId);
        if (player != null) {
            player.setCurFashionId(curFashionId);
        }
    }

    @Override
    public void updateBabyCurFashionId(String scenceId, long roleId, int curFashionId) {
        ArroundPlayer player = getArroundPlayer(scenceId, roleId);
        if (player != null) {
            player.setBabyCurFashionId(curFashionId);
        }
    }

    @Override
    public void updateDragonBallList(String scenceId, long roleId, List<String> dragonBallList) {
        ArroundPlayer player = getArroundPlayer(scenceId, roleId);
        if (player != null) {
            player.setDragonBallList(dragonBallList);
        }
    }

    @Override
    public void updateCurTitleId(String sceneId, long roleId, int curTitleId) {
        ArroundPlayer player = getArroundPlayer(sceneId, roleId);
        if (player != null) {
            player.setCurTitleId(curTitleId);
        }
    }

    public void updateCurVipLevel(String sceneId, long roleId, int curVipLevel) {
        ArroundPlayer player = getArroundPlayer(sceneId, roleId);
        if (player != null) {
            player.setCutVipLevel(curVipLevel);
            relationService.updateVip(roleId, curVipLevel);
        }
    }

    @Override
    public void updateBabyFollow(String scenceId, long roleId, byte follow) {
        ArroundPlayer player = getArroundPlayer(scenceId, roleId);
        if (player != null) {
            player.setBabyFollow(follow);
        }
    }

    @Override
    public void updateCurDeityWeaponType(String sceneId, long roleId, byte deityweaponType) {
        ArroundPlayer player = getArroundPlayer(sceneId, roleId);
        if (player != null) {
            player.setDeityweaponType(deityweaponType);
        }
    }

    @Override
    public void addFriendId(long roleId, Collection<Long> friendIdSet) {
        relationService.addFriendId(roleId, friendIdSet);
    }

    @Override
    public void delFriendId(long roleId, Collection<Long> friendIdSet) {
        relationService.delFriendId(roleId, friendIdSet);
    }

    @Override
    public void updateCoupleId(long roleId, long coupleId) {
        relationService.updateCoupleId(roleId, coupleId);
    }

    @Override
    public void updateFamilyAuth(long roleId, FamilyAuth auth) {
        relationService.updateFamilyAuth(roleId, auth);
    }

    @Override
    public Map<Long, ArroundPlayer> getArroundPlayersBySceneId(String sceneId) {
        Map<Long, ArroundPlayer> players = sceneArroundPlayers.get(sceneId);
        if (players == null || players.size() <= 0) {
            return new HashMap<Long, ArroundPlayer>();
        }

        return players;
    }

    @Override
    public void updateRoleRename(long roleId, String sceneId, String newName) {
        Map<Long, ArroundPlayer> players = sceneArroundPlayers.get(sceneId);
        ArroundPlayer arroundPlayer = players.get(roleId);
        arroundPlayer.setName(newName);
        for (Map.Entry<Long, ArroundPlayer> entry : players.entrySet()) {
            if (entry.getKey() != roleId) {
                flushArroundPlayers(sceneId, entry.getKey(), null);
            }
        }
    }

    @Override
    public void updateCurFashionCardId(String sceneId, long roleId, int curFashionCardId) {
        ArroundPlayer player = getArroundPlayer(sceneId, roleId);
        if (player != null) {
            player.setCurFashionCardId(curFashionCardId);
        }
    }

    @Override
    public void flush() {
        long now = System.currentTimeMillis();
        if (now - lastFlushTimestamp > PositionSyncManager.FlushFrequency) {
            lastFlushTimestamp = now;
            ConcurrentMap<String, MarryWedding> weddingMap = ServiceHelper.marryService().getCurrentWeddingMapSync();
            AoiCallback callback = new ArroundPlayerAoiCallback(relationService, allArroundPlayers, weddingMap, cap);
            aoiService.check(callback);
        }
    }
}
