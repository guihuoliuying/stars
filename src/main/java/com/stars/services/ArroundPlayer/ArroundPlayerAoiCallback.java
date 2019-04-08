package com.stars.services.ArroundPlayer;

import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.arroundPlayer.Packet.ClientArroundPlayer;
import com.stars.modules.positionsync.PositionSyncManager;
import com.stars.modules.scene.SceneManager;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.marry.userdata.MarryWedding;
import com.stars.services.postsync.PositionSyncRelationService;
import com.stars.services.postsync.aoi.AoiCallback;
import com.stars.services.postsync.aoi.AoiObject;
import com.stars.services.postsync.aoi.AoiScene;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2017/7/3.
 */
public class ArroundPlayerAoiCallback implements AoiCallback {

    private PositionSyncRelationService relationService;
    private Map<Long, ArroundPlayer> allArroundPlayers;
    private ClientArroundPlayer packet;
    private ConcurrentMap<String, MarryWedding> weddingMap;

    public ArroundPlayerAoiCallback(PositionSyncRelationService relationService, Map<Long, ArroundPlayer> allArroundPlayers, ConcurrentMap<String, MarryWedding> weddingMap, ClientArroundPlayer packet) {
        this.relationService = relationService;
        this.allArroundPlayers = allArroundPlayers;
        this.packet = packet;
        this.weddingMap = weddingMap;
    }

    @Override
    public void exec(AoiScene scene, long roleId, List<AoiObject> aoiObjectList) {
        ArroundPlayer self = allArroundPlayers.get(roleId);
        if (self == null) {
            return;
        }
        /* 根据关系链进行排序 */
        aoiObjectList = relationService.sort(roleId, aoiObjectList);
        /* 处理婚礼场景 */
        if (scene.sceneType() == SceneManager.SCENETYPE_WEDDING) {
            if (self != null && weddingMap.get(self.getArroundId()) != null) {
                MarryWedding wedding =  weddingMap.get(self.getArroundId());
                Set<Long> couple = wedding.getWeddingUnit();
                if (couple != null) {
                    AoiObject bridegroom = null, bride = null;
                    for (Long id : couple) {
                        if (roleId != id) { // 过滤自己
                            if (bridegroom == null) {
                                bridegroom = scene.aoiObject(id);
                            } else {
                                bride = scene.aoiObject(id);
                            }
                        }
                    }
                    if (bridegroom != null) {
                        if (aoiObjectList.contains(bridegroom)) {
                            aoiObjectList.remove(bridegroom);
                        }
                        aoiObjectList.add(0, bridegroom);
                    }
                    if (bride != null) {
                        if (aoiObjectList.contains(bride)) {
                            aoiObjectList.remove(bride);
                        }
                        aoiObjectList.add(0, bride);
                    }
                }
            }
        }

//        print(roleId, aoiObjectList);
        /* 只同步固定的人数 */
        if (aoiObjectList.size() > PositionSyncManager.MaxSyncNum) {
            aoiObjectList = aoiObjectList.subList(0, PositionSyncManager.MaxSyncNum);
        }
        /* 下发数据 */
        packet.setIndex((byte) 0);
        for (AoiObject obj : aoiObjectList) {
            ArroundPlayer player = allArroundPlayers.get(obj.roleId());
            if (player != null) {
                packet.addArroundPlayer(player);
            }
        }
        PacketManager.send(roleId, packet);
    }

    private void print(long roleId, List<AoiObject> aoiObjectList) {
        StringBuilder sb = new StringBuilder();
        ArroundPlayer self = allArroundPlayers.get(roleId);
        sb.append("sync: role=").append(self.getName()).append(", list=[");
        for (AoiObject obj : aoiObjectList) {
            String s = "(";
            ArroundPlayer other = allArroundPlayers.get(obj.roleId());
            s += other.getName();
            s += "|" + (relationService.isCouple(roleId, obj.roleId()) ? "c" : "nc");
            s += "|" + (relationService.isFamilyOffice(roleId, obj.roleId()) ? "fo" : "nfo");
            s += "|" + (relationService.isHighVip(obj.roleId()) ? "hv" : "nhv");
            s += "|" + (relationService.isFriend(roleId, obj.roleId()) ? "f" : "nf");
            s += "|" + (relationService.isFamilyMember(roleId, obj.roleId()) ? "fm" : "nfm");
            s += ")";
            sb.append(s).append(",");
        }
        sb.append("]");
        LogUtil.info(sb.toString());
    }

}
